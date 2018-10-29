package com.lwh.rpc.zookeeper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lwh.rpc.helper.PropertyConfigureHelper;
import com.lwh.rpc.model.InvokerService;
import com.lwh.rpc.model.ProviderService;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author lwh
 * @date 2018-10-27
 * @desp 注册中心实现
 */
public class RegisterCenter implements IRegisterCenter4Invoker, IRegisterCenter4Provider, IRegisterCenter4Governance{

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
    private static String ROOT_PATH = "/config_register";
    public static String PROVIDER_TYPE = "provider";
    public static String INVOKER_TYPE = "consumer";
    private static volatile ZkClient zkClient = null;

    private RegisterCenter(){

    }

    public static RegisterCenter singleton(){
        return registerCenter;
    }

    @Override
    public void registerProvider(List<ProviderService> serviceMetaData) {
        if(CollectionUtils.isEmpty(serviceMetaData)){
            return;
        }

        //连接zk,注册服务
        synchronized (RegisterCenter.class){
            for(ProviderService provider : serviceMetaData){
                String serviceItfKey = provider.getServiceItf().getName();

                List<ProviderService> providers = providerServiceMap.get(serviceItfKey);
                if(providers == null){
                    providers = Lists.newArrayList();
                }
                providers.add(provider);
                //放入本地缓存
                providerServiceMap.put(serviceItfKey, providers);
            }

            if(zkClient == null){
                zkClient = new ZkClient(ZK_SERVICE, ZK_SESSION_TIME_OUT, ZK_CONNECTION_TIME_OUT, new SerializableSerializer());
            }

            //创建ZK命名空间/当前部署应用APP命名空间/
            String APP_KEY = serviceMetaData.get(0).getAppKey();
            String ZK_PATH = ROOT_PATH + "/" + APP_KEY;
            //不存在则创建服务提供者节点
            boolean exist = zkClient.exists(ZK_PATH);
            if(!exist){
                zkClient.createPersistent(ZK_PATH, true);
            }

            for(Map.Entry<String, List<ProviderService>> entry : providerServiceMap.entrySet()){
                ProviderService providerService = entry.getValue().get(0);
                //服务分组
                String groupName = providerService.getGroupName();
                //创建服务提供者
                String serviceNode = entry.getKey();
                String servicePath = ZK_PATH + "/" + groupName + "/" + serviceNode + "/" + PROVIDER_TYPE;
                exist = zkClient.exists(servicePath);
                if(!exist){
                    zkClient.createPersistent(servicePath, true);
                }

                //创建当前服务器节点
                //服务端口
                int serverPort = providerService.getServerPort();
                //服务权重
                int weight = providerService.getWeight();
                //服务工作线程
                int workerThreads = providerService.getWorkerThreads();
                String localIp = null;
            }
        }
    }

    @Override
    public Pair<List<ProviderService>, List<InvokerService>> queryProvidersAndInvokers(String serviceName, String appKey) {
        return null;
    }

    @Override
    public void initProviderMap(String remoteAppKey, String groupName) {

    }

    @Override
    public Map<String, List<ProviderService>> getServiceMetaDataMap4Consume() {
        return null;
    }

    @Override
    public void registerInvoker(InvokerService invoker) {

    }

    @Override
    public Map<String, List<ProviderService>> getProviderServiceMap() {
        return null;
    }
}
