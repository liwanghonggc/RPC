package com.lwh.rpc.zookeeper;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lwh.rpc.helper.IPHelper;
import com.lwh.rpc.helper.PropertyConfigureHelper;
import com.lwh.rpc.model.InvokerService;
import com.lwh.rpc.model.ProviderService;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 注册中心实现
 */
public class RegisterCenter implements IRegisterCenter4Invoker, IRegisterCenter4Provider, IRegisterCenter4Governance {

    private static RegisterCenter registerCenter = new RegisterCenter();

    /**
     * 服务提供者列表,key服务提供者接口,value服务提供者服务方法列表
     */
    private static final Map<String, List<ProviderService>> providerServiceMap = Maps.newConcurrentMap();

    /**
     * 服务端ZK服务元信息,选择服务(第一次直接从zk拉取,后续由zk的监听机制主动更新)
     */
    private static final Map<String, List<ProviderService>> serviceMetaData4Consume = Maps.newConcurrentMap();

    /**
     * 从配置文件中获取ZK的服务地址列表
     */
    private static String ZK_SERVICE = PropertyConfigureHelper.getZkService();

    /**
     * 从配置文件中获取ZK会话超时时间配置
     */
    private static int ZK_SESSION_TIME_OUT = PropertyConfigureHelper.getZkSessionTimeout();

    /**
     * 从配置文件中获取ZK连接超时时间配置
     */
    private static int ZK_CONNECTION_TIME_OUT = PropertyConfigureHelper.getZkConnectionTimeout();

    /**
     * 组装zk根路径/appkey路径
     */
    private static String ROOT_PATH = "/rpc_config_register";
    public static String PROVIDER_TYPE = "provider";
    public static String INVOKER_TYPE = "consumer";
    private static volatile ZkClient zkClient = null;

    private RegisterCenter() {

    }

    public static RegisterCenter singleton() {
        return registerCenter;
    }

    /**
     * 服务端将服务提供者信息注册到zk对应节点下,服务端启动后会调用此方法,注册信息
     * 注册完以后会使用ZK的监听机制,若服务列表有变化会同步到本地缓存
     * @param serviceMetaData
     */
    @Override
    public void registerProvider(final List<ProviderService> serviceMetaData) {
        if (CollectionUtils.isEmpty(serviceMetaData)) {
            return;
        }

        //连接zk,注册服务
        synchronized (RegisterCenter.class) {
            for (ProviderService provider : serviceMetaData) {
                String serviceItfKey = provider.getServiceItf().getName();

                List<ProviderService> providers = providerServiceMap.get(serviceItfKey);
                if (providers == null) {
                    providers = Lists.newArrayList();
                }
                providers.add(provider);
                //放入本地缓存
                providerServiceMap.put(serviceItfKey, providers);
            }

            if (zkClient == null) {
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }

            //创建ZK命名空间/当前部署应用APP命名空间/,此处我们配置的APP_KEY是rpc
            String APP_KEY = serviceMetaData.get(0).getAppKey();
            String ZK_PATH = ROOT_PATH + "/" + APP_KEY;
            //不存在则创建服务提供者节点
            boolean exist = zkClient.exists(ZK_PATH);
            if (!exist) {
                zkClient.createPersistent(ZK_PATH, true);
            }

            for (Map.Entry<String, List<ProviderService>> entry : providerServiceMap.entrySet()) {
                ProviderService providerService = entry.getValue().get(0);
                //服务分组,groupName我们配置的是default
                String groupName = providerService.getGroupName();
                //创建服务提供者
                String serviceNode = entry.getKey();
                //servicePath形如/rpc_config_register/rpc/default/com.lwh.test.HelloService/provider
                String servicePath = ZK_PATH + "/" + groupName + "/" + serviceNode + "/" + PROVIDER_TYPE;
                exist = zkClient.exists(servicePath);
                if (!exist) {
                    zkClient.createPersistent(servicePath, true);
                }

                //创建当前服务器节点
                //服务端口
                int serverPort = providerService.getServerPort();
                //服务权重
                int weight = providerService.getWeight();
                //服务工作线程
                int workerThreads = providerService.getWorkerThreads();
                String localIp = IPHelper.localIp();
                //servicepath后面的部分形如,[10.128.20.8|8081|2|100|default]
                String currentServiceIpNode = servicePath + "/" + localIp + "|" + serverPort + "|" + weight
                        + "|" + workerThreads + "|" + groupName;
                exist = zkClient.exists(currentServiceIpNode);
                if (!exist) {
                    //创建临时节点
                    zkClient.createEphemeral(currentServiceIpNode);
                }

                //监听注册服务的变化,同时更新数据到本地缓存
                zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
                    @Override
                    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                        if (currentChilds == null) {
                            currentChilds = Lists.newArrayList();
                        }

                        //存活的服务IP列表
                        List<String> activeServiceIpList = Lists.newArrayList(Lists.transform(currentChilds, new Function<String, String>() {
                            @Override
                            public String apply(String input) {
                                return StringUtils.split(input, "|")[0];
                            }
                        }));

                        refreshActiveService(activeServiceIpList);
                    }
                });
            }
        }
    }

    @Override
    public Map<String, List<ProviderService>> getProviderServiceMap() {
        return providerServiceMap;
    }

    /**
     * 消费端初始化服务提供者信息本地缓存
     *
     * @param remoteAppKey
     * @param groupName
     */
    @Override
    public void initProviderMap(String remoteAppKey, String groupName) {
        if (MapUtils.isEmpty(serviceMetaData4Consume)) {
            serviceMetaData4Consume.putAll(fetchOrUpdateServiceMetaData(remoteAppKey, groupName));
        }
    }

    @Override
    public Map<String, List<ProviderService>> getServiceMetaDataMap4Consume() {
        return serviceMetaData4Consume;
    }

    /**
     * 消费端将消费注册信息注册到zk对应节点下
     *
     * @param invoker
     */
    @Override
    public void registerInvoker(InvokerService invoker) {
        if (invoker == null) {
            return;
        }

        //连接zk注册服务
        synchronized (RegisterCenter.class) {
            if (zkClient == null) {
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }

            //创建ZK命名空间/当前部署应用APP命名空间
            boolean exist = zkClient.exists(ROOT_PATH);
            if (!exist) {
                zkClient.createPersistent(ROOT_PATH, true);
            }

            //创建服务消费者节点
            String remoteAppKey = invoker.getRemoteAppKey();
            String groupName = invoker.getGroupName();
            String serviceNode = invoker.getServiceItf().getName();
            String servicePath = ROOT_PATH + "/" + remoteAppKey + "/" + groupName + "/" + serviceNode + "/" + INVOKER_TYPE;
            exist = zkClient.exists(servicePath);
            if (!exist) {
                zkClient.createPersistent(servicePath, true);
            }

            //创建当前服务器节点
            String localIp = IPHelper.localIp();
            String currentServiceIpNode = servicePath + "/" + localIp;
            exist = zkClient.exists(currentServiceIpNode);
            if (!exist) {
                //这里创建临时节点
                zkClient.createEphemeral(currentServiceIpNode);
            }
        }
    }


    /**
     * 利用ZK自动刷新当前存活的服务提供者列表数据
     *
     * @param serviceIpList
     */
    private void refreshActiveService(List<String> serviceIpList) {
        doRefresh(serviceIpList, providerServiceMap);
    }


    private void refreshServiceMetaDataMap(List<String> serviceIpList) {
        doRefresh(serviceIpList, serviceMetaData4Consume);
    }

    private void doRefresh(List<String> serviceIpList, Map<String, List<ProviderService>> serviceMap) {
        if (serviceIpList == null) {
            serviceIpList = Lists.newArrayList();
        }

        Map<String, List<ProviderService>> currentServiceMetaDataMap = Maps.newHashMap();

        for (Map.Entry<String, List<ProviderService>> entry : serviceMap.entrySet()) {
            String serviceItkey = entry.getKey();
            List<ProviderService> serviceList = entry.getValue();

            List<ProviderService> providerServiceList = currentServiceMetaDataMap.get(serviceItkey);
            if (providerServiceList == null) {
                providerServiceList = Lists.newArrayList();
            }

            for (ProviderService serviceMetaData : serviceList) {
                if (serviceIpList.contains(serviceMetaData.getServerIp())) {
                    providerServiceList.add(serviceMetaData);
                }
            }

            currentServiceMetaDataMap.put(serviceItkey, providerServiceList);
        }

        serviceMap.clear();
        serviceMap.putAll(currentServiceMetaDataMap);
    }

    private Map<String, List<ProviderService>> fetchOrUpdateServiceMetaData(String remoteAppKey, String groupName) {
        final Map<String, List<ProviderService>> providerServiceMap = Maps.newConcurrentMap();

        //连接zk
        synchronized (RegisterCenter.class) {
            if (zkClient == null) {
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }
        }

        //从ZK获取服务提供者列表,providerPath形如/rpc_config_register/rpc/default/
        String providerPath = ROOT_PATH + "/" + remoteAppKey + "/" + groupName;
        List<String> providerServices = zkClient.getChildren(providerPath);

        for (String serviceName : providerServices) {
            String servicePath = providerPath + "/" + serviceName + "/" + PROVIDER_TYPE;
            List<String> ipPathList = zkClient.getChildren(servicePath);
            for (String ipPath : ipPathList) {
                String serverIp = StringUtils.split(ipPath, "|")[0];
                String serverPort = StringUtils.split(ipPath, "|")[1];
                int weight = Integer.parseInt(StringUtils.split(ipPath, "|")[2]);
                int workerThreads = Integer.parseInt(StringUtils.split(ipPath, "|")[3]);
                String group = StringUtils.split(ipPath, "|")[4];

                List<ProviderService> providerServiceList = providerServiceMap.get(serviceName);
                if (providerServiceList == null) {
                    providerServiceList = Lists.newArrayList();
                }

                ProviderService providerService = new ProviderService();

                try {
                    providerService.setServiceItf(ClassUtils.getClass(serviceName));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }

                providerService.setServerIp(serverIp);
                providerService.setServerPort(Integer.parseInt(serverPort));
                providerService.setWeight(weight);
                providerService.setWorkerThreads(workerThreads);
                providerService.setGroupName(group);
                providerServiceList.add(providerService);

                providerServiceMap.put(serviceName, providerServiceList);

                //监听注册服务的变化,同时更新数据到本地缓存
                zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
                    @Override
                    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
                        if (currentChilds == null) {
                            currentChilds = Lists.newArrayList();
                        }
                        currentChilds = Lists.newArrayList(Lists.transform(currentChilds, new Function<String, String>() {
                            @Override
                            public String apply(String input) {
                                return StringUtils.split(input, "|")[0];
                            }
                        }));
                        refreshServiceMetaDataMap(currentChilds);
                    }
                });
            }
        }
        return providerServiceMap;
    }

    /**
     * 获取服务提供者列表与服务消费者列表
     *
     * @param serviceName
     * @param appKey
     * @return
     */
    @Override
    public Pair<List<ProviderService>, List<InvokerService>> queryProvidersAndInvokers(String serviceName, String appKey) {
        //服务消费者列表
        List<InvokerService> invokerServices = Lists.newArrayList();
        //服务提供者列表
        List<ProviderService> providerServices = Lists.newArrayList();

        //连接zk
        if (zkClient == null) {
            synchronized (RegisterCenter.class) {
                if (zkClient == null) {
                    zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
                }
            }
        }

        String parentPath = ROOT_PATH + "/" + appKey;
        //获取ROOT_PATH + APP_KEY 注册中心子目录列表
        List<String> groupServiceList = zkClient.getChildren(parentPath);
        if (CollectionUtils.isEmpty(groupServiceList)) {
            return Pair.of(providerServices, invokerServices);
        }

        for (String group : groupServiceList) {
            String groupPath = parentPath + "/" + group;
            //获取ROOT_PATH + APP_KEY + group 注册中心子目录列表
            List<String> serviceList = zkClient.getChildren(groupPath);
            if (CollectionUtils.isEmpty(serviceList)) {
                continue;
            }
            for (String service : serviceList) {
                //获取ROOT_PATH + APP_KEY + group + service 注册中心子目录列表
                String servicePath = groupPath + "/" + service;
                List<String> serviceTypes = zkClient.getChildren(servicePath);
                if (CollectionUtils.isEmpty(serviceTypes)) {
                    continue;
                }

                for (String serviceType : serviceTypes) {
                    if (StringUtils.equals(serviceType, PROVIDER_TYPE)) {
                        //获取ROOT_PATH + APP_KEY + group + service + serviceType 注册中心子目录列表
                        String providerPath = servicePath + "/" + serviceType;
                        List<String> providers = zkClient.getChildren(providerPath);
                        if (CollectionUtils.isEmpty(providers)) {
                            continue;
                        }

                        //获取服务提供者信息
                        for (String provider : providers) {
                            String[] providerNodeArr = StringUtils.split(provider, "|");

                            ProviderService providerService = new ProviderService();
                            providerService.setAppKey(appKey);
                            providerService.setGroupName(group);
                            providerService.setServerIp(providerNodeArr[0]);
                            providerService.setServerPort(Integer.parseInt(providerNodeArr[1]));
                            providerService.setWeight(Integer.parseInt(providerNodeArr[2]));
                            providerService.setWorkerThreads(Integer.parseInt(providerNodeArr[3]));
                            providerServices.add(providerService);
                        }
                    } else if (StringUtils.equals(serviceType, INVOKER_TYPE)) {
                        //获取ROOT_PATH + APP_KEY + group + service + serviceType 注册中心子目录列表
                        String invokerPath = servicePath + "/" + serviceType;
                        List<String> invokers = zkClient.getChildren(invokerPath);
                        if (CollectionUtils.isEmpty(invokers)) {
                            continue;
                        }

                        //获取服务消费者信息
                        for (String invoker : invokers) {
                            InvokerService invokerService = new InvokerService();
                            invokerService.setRemoteAppKey(appKey);
                            invokerService.setGroupName(group);
                            invokerService.setInvokerIp(invoker);
                            invokerServices.add(invokerService);
                        }
                    }
                }
            }
        }
        return Pair.of(providerServices, invokerServices);
    }

}
