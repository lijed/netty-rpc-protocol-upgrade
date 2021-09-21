package com.me.learn.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 1. 这个注解标记一个对外发布的服务，同时将这个服务放到IOC容器
 *
 * 2. 被这个注解标记的服务注册到注册中心
 *
 *
 *
 * @Author: Administrator
 * Created: 2021/9/19
 **/
@Target(value = ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface GpRemoteService {
}
