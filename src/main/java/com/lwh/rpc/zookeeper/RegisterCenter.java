package com.lwh.rpc.zookeeper;

import com.google.common.collect.Maps;
import com.lwh.rpc.helper.PropertyConfigureHelper;
import com.lwh.rpc.model.InvokerService;
import com.lwh.rpc.model.ProviderService;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.zookeeper.client.ZooKeeperSaslClient;

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

    private static String ROOT_PATH = "/config_register";
    public static String PROVIDER_TYPE = "provider";
    public static String INVOKER_TYPE = "consumer";
    private static volatile ZkClient zkClient = null;

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
    public void registerProvider(List<ProviderService> serviceMetaData) {

    }

    @Override
    public Map<String, List<ProviderService>> getProviderServiceMap() {
        return null;
    }
}
