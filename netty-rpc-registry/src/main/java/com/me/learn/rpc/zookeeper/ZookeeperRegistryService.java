/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.zookeeper;

import com.me.learn.rpc.IRegistryService;
import com.me.learn.rpc.ServiceInfo;
import com.me.learn.rpc.loadbalance.ILoadBalancer;
import com.me.learn.rpc.loadbalance.RandomLoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceInstanceBuilder;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.Collection;
import java.util.List;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/
@Slf4j
public class ZookeeperRegistryService implements IRegistryService {

    private static final String REGISTRY_PATH = "/registry";
    private CuratorFramework curatorFramework;
    private ServiceDiscovery<ServiceInfo> serviceDiscovery;
    private RandomLoadBalance loadBalance;

    public ZookeeperRegistryService(String registryAddress) throws Exception {

        CuratorFramework client = CuratorFrameworkFactory.newClient(registryAddress, new ExponentialBackoffRetry(1000, 3));
        client.start();


        JsonInstanceSerializer<ServiceInfo> jsonInstanceSerializer = new JsonInstanceSerializer<>(ServiceInfo.class);
        this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInfo.class)
                .client(client)
                .basePath(REGISTRY_PATH)
                .serializer(jsonInstanceSerializer)
                .build();
        this.serviceDiscovery.start();

        loadBalance = new RandomLoadBalance();

    }

    @Override
    public void register(ServiceInfo serviceInfo) throws Exception {
        log.info("begin registry service instance to zookeeper server");
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                .name(serviceInfo.getServiceName())
                .address(serviceInfo.getServiceAddress())
                .port(serviceInfo.getServicePort())
                .payload(serviceInfo)
                .build();
        this.serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public ServiceInfo discovery(String serviceName) throws Exception {
        log.info("begin discover service instance from zookeeper server");

        //服务发现
        final Collection<ServiceInstance<ServiceInfo>> serviceInstances =  this.serviceDiscovery.queryForInstances(serviceName);

        //动态路由
        ServiceInstance<ServiceInfo> serviceInstance =  loadBalance.select((List<ServiceInstance<ServiceInfo>>) serviceInstances);
        if (serviceInstance != null) {
            return serviceInstance.getPayload();
        }

        return null;
    }
}
