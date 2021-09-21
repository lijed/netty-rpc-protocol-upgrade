/*
 * Copyright 2021 tu.cn All right reserved. This software is the
 * confidential and proprietary information of tu.cn ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Tu.cn
 */
package com.me.learn.rpc.spring.referrence;

import com.me.learn.annotation.GpRemoteReferrence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/

@Slf4j
public class SpringRpcReferrencePostProcessor implements ApplicationContextAware, BeanClassLoaderAware, BeanFactoryPostProcessor {

    private ApplicationContext applicationContext;
    private ClassLoader classLoader;
    private RpcClientProperties rpcClientProperties;

    private final Map<String, BeanDefinition> rpcReferrenceBeanDefinitionMap = new ConcurrentHashMap<>();

    public SpringRpcReferrencePostProcessor(RpcClientProperties rpcClientProperties) {
        this.rpcClientProperties = rpcClientProperties;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * spring容器加载了Bean的定义文件后，在bean实例化之前执行
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName= beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                final Class<?> aClass = ClassUtils.resolveClassName(beanClassName, classLoader);
                ReflectionUtils.doWithFields(aClass, this::parseRpcReferrence);
            }
        }

        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) beanFactory;
        this.rpcReferrenceBeanDefinitionMap.forEach((beanName, beanDefinition) -> {
            if (applicationContext.containsBean(beanName)) {
                log.warn("springcontext already registered bean {}", beanName);
            }  else {
                beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
                log.info("registered RpcReferrenceBean {} success", beanName);
            }
        });
    }

    private void parseRpcReferrence(Field field) {
        final boolean annotationPresent = field.isAnnotationPresent(GpRemoteReferrence.class);
        if (annotationPresent) {
            final BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SpringRpcReferenceBean.class);
            // @Bean注解里initMethod
            beanDefinitionBuilder.setInitMethodName("init");
            beanDefinitionBuilder.addPropertyValue("serviceClass", field.getType());
                   /*builder.addPropertyValue("serviceAddress",rpcClientProperties.getServiceAddress());
            builder.addPropertyValue("servicePort",rpcClientProperties.getServicePort());*/
            beanDefinitionBuilder.addPropertyValue("registryAddress", rpcClientProperties.getRegistryAddress());
            beanDefinitionBuilder.addPropertyValue("registryType", rpcClientProperties.getRegistryType());
            BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
            rpcReferrenceBeanDefinitionMap.put(field.getType().getName(), beanDefinition);
        }
    }

}
