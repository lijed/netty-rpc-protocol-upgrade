# 基于Netty RPC框架

该框架由四部分组成:

- RPC Protocol   

  定义了消息的协议，编解码， server端及client端的实现

- server API definition

  定义了服务的API

- Service Provider

  服务的提供者

- Service Consumer

  服务的消费端
  
- 服务的注册与发现

# RPC Protocol

RPC 协议module 定义了消息的协议，数据的序列化，编码和解码，RPC server 端，Client端，server端服务的发布，消费端服务的引用。 

###   消息的协议

###   数据的序列化

###   编码和解码

###  RPC server 端

### Client端

## Server端服务的发布

实现@GpRemoteService的解析和调用服务注册

### @GpRemoteService的解析

SpringRpcProviderBean完成@GPRemoteService的解析及服务注册。 

@GPRemoteService的解析， 把service里方法放入到Map里，key为服务接口的全类名 +“.”+方法名， value为方法，方便RpcServer端的调用。 

```java
@Slf4j
public class SpringRpcProviderBean implements InitializingBean, BeanPostProcessor {
    private String serverAddress;
    private int port;
    private IRegistryService registryService;

    public SpringRpcProviderBean(int port, IRegistryService registryService) throws UnknownHostException {
        this.serverAddress = InetAddress.getLocalHost().getHostAddress();;
        this.port = port;

        this.registryService = registryService;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Start to deploy netty server to {} on port {}", serverAddress, port);
        new Thread(() -> new NettyServer(serverAddress, port).startNettyServer()).start();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        //判读server是否被GpRemoteService标记
        final boolean isGpRemoteService = bean.getClass().isAnnotationPresent(GpRemoteService.class);

        if (isGpRemoteService) {
            final Method[] declaredMethods = bean.getClass().getDeclaredMethods();
            //key的格式： 接口的全类名+ "." + 方法名字
            String key;
            final String serviceName = bean.getClass().getInterfaces()[0].getName();
            for (Method declaredMethod : declaredMethods) {
                key = serviceName + "." +  declaredMethod.getName();
                Mediator.beanMethods.put(key, new BeanMethod(bean, declaredMethod));
            }

            //服务注册到注册中心
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceAddress(serverAddress);
            serviceInfo.setServicePort(port);
            serviceInfo.setServiceName(serviceName);

            try {
                registryService.register(serviceInfo);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("register serivce {} failed",serviceName,e);
            }
        }
        return bean;
    }
    
    
 


```

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeanMethod {
    private Object bean;
    private Method method;
}
```



Mediator 用来保存服务接口的方法的保存，和 服务方法的调用

```java
public class Mediator {
    public static Map<String, BeanMethod> beanMethods = new ConcurrentHashMap<>();

    private Mediator() {
    }

    private static volatile Mediator instance;

    public static Mediator getInstance() {
        if (instance == null) {
            synchronized (Mediator.class) {
                if (instance == null) {
                    instance = new Mediator();
                }
            }
        }
        return instance;
    }

    public Object invoke(RpcRequest request) {
        String key = request.getClassName() + "." + request.getMethodName();
        final BeanMethod beanMethod = beanMethods.get(key);
        if (beanMethod != null) {
            try {
                return beanMethod.getMethod().invoke(beanMethod.getBean(), request.getParas());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
```



### 配置实现

```java
 **/

@Data
@ConfigurationProperties(prefix = "gp.rpc")
public class RpcServerProperties {
    private int servicePort;

    //注册中心的地址
    private String registryAddress;

    //注册中心的类型
    private byte registryType;

}
```



```java
@Configuration
@EnableConfigurationProperties(RpcServerProperties.class)
public class SpringRpcServerAutoConfiguration {

    @Bean
    public SpringRpcProviderBean springRpcProviderBean(RpcServerProperties rpcServerProperties) throws UnknownHostException {
        int port = rpcServerProperties.getServicePort();
        final IRegistryService registryService = RegistryFactory.createRegistryService(
                rpcServerProperties.getRegistryAddress(), 		RegistryType.findByCode(rpcServerProperties.getRegistryType()));
        return new SpringRpcProviderBean(port, registryService);
    }
}
```



## 消费端服务的引用

#### 1. 定义服务消费端注解 @GpRemoteReferrence

```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Autowired
public @interface GpRemoteReferrence {
}
```

#### 2. 解析添加@GpRemoteReferrence的服务应用属性

 解析添加@GpRemoteReferrence注解的属性，声明一个Bean，实现BeanFactoryPostProcessor，找到有@GpRemoteReferrence的fields， 然后为每一个field创建一个BeanDefinition且每个BeanDefinition的className为一个FactoryBean, 最后把这些BeanDefinition放入BeanDefinitionRegistry。

还要多说两句关于FactoryBean,FactoryBean用来构建Service的动态代理，在动态代理里来完成RPC 的消息的编码及消息的发送，和接收到response后的消息的解码。

#####  SpringRpcReferenceBean 

 SpringRpcReferenceBean是FactoryBean实现，用来构建服务接口的动态代理

```java
/**
 * Description:
 * 工厂Bean用于构建复杂的bean
 * @Author: Administrator
 * Created: 2021/9/19
 **/
public class SpringRpcReferenceBean<T> implements FactoryBean<T> {

    private Object object;
    private Class<?> serviceClass;
    private String registryAddress;
    private byte registryType;


    public SpringRpcReferenceBean() {}

    /**
     *  //进一步优化：cglib 或者javaassitent
     */
    public void init() {
        IRegistryService registryService = RegistryFactory.createRegistryService(registryAddress, RegistryType.findByCode(registryType));
        //利用JDK的动态代理
        this.object = (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class<?>[]{serviceClass},
                new RpcClientInvokerProxy(registryService));
    }

    @Override
    public T getObject() throws Exception {
        return (T) this.object;
    }

    @Override
    public Class<?> getObjectType() {
        return this.serviceClass;
    }

    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setRegistryType(byte registryType) {
        this.registryType = registryType;
    }
}

```



JDK的动态代理实现，主要实现了InvocationHandler

```java

public class RpcClientInvokerProxy<T> implements InvocationHandler {

   //服务注册，用来重注册中心获取服务的地址
    private IRegistryService registryService;

    public RpcClientInvokerProxy(IRegistryService registryService) {
        this.registryService = registryService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        for (Method decMethod: method.getDeclaringClass().getDeclaredMethods()) {
            if (!method.equals(decMethod)){
                return method.invoke(proxy, args);
            }
        }

        //构建RpcProtocol
        RpcProtocol<RpcRequest> rpcProtocol = new RpcProtocol<RpcRequest>();

        Header header = new Header();
        header.setMagic(RpcConstant.MAGIC);
        header.setSerialType(SerialType.JSON_SERIAL.code());
        header.setReqType(ReqType.REQUEST.code());
        final Long requestId = RequestHolder.REQUEST_ID.incrementAndGet();
        header.setRequestId(requestId);

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParas(args);

        rpcProtocol.setHeader(header);
        rpcProtocol.setContent(rpcRequest);
        //send 消息到服务端
        NettyClient nettyClient = new NettyClient();

        //在异步收到服务端请求后，通知client - future.getPromise().get().getData();
        RpcFuture<RpcResponse> future= new RpcFuture<RpcResponse>(new DefaultPromise<RpcResponse>(new DefaultEventLoop()));
        RequestHolder.REQUEST_MAP.put(requestId, future);

        nettyClient.sendRequest(rpcProtocol, registryService);

        //阻塞获取消息的response并返回
        return future.getPromise().get().getData();
    }
}

```



#####  @GpRemoteReferrence注解的解析

声明一个SpringRpcReferrencePostProcessor，在BeanFactory实例化后且BeanDefinition创建后，SpringBean还没有实例化前，我们来完成GpRemoteReferrence注解的解析，此处用到BeanFactoryPostProcessor。



```java
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

```

![image-20210920110342969](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20210920110342969.png)



AnnotationConfigApplicationContext实现BeanDefinitionRegistry。



##### Referrence 自动配置

RpcReferenceAutoConfiguration来完成这些功能

```java
@Configuration
public class RpcReferenceAutoConfiguration implements EnvironmentAware {
    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment=environment;
    }

    @Bean
    public SpringRpcReferencePostProcessor postProcessor(){
        RpcClientProperties rc=new RpcClientProperties();
        rc.setRegistryAddress(this.environment.getProperty("gp.client.registryAddress"));
      /*  int port=Integer.parseInt();
        rc.setRegistryAddress(port);*/
        rc.setRegistryType(Byte.parseByte(this.environment.getProperty("gp.client.registryType")));
        return new SpringRpcReferencePostProcessor(rc);
    }
}
```

## Server API definition

service服务接口的定义，供其他模块引用。 如 service provider, service consumer。 

比如声明一个UserService接口

```java
@Data
public class User {
    private String userName;
    private int age;
}


public interface IUserService {
     String saveUser(User user);
}
```

## Service Provider

service 服务接口的实现, 添加@GpRemoteService 注解，暴露服务且服务会放到IOC 容器

### 服务实现

一定要添加GpRemoteService

```java
@GpRemoteService
public class UserServiceImpl implements IUserService {
    @Override
    public String saveUser(User user) {
        System.out.println(user.toString());
        return "add user " + user.getUserName() + " successfully";
    }
}
```

### 配置

Service的provider需要配置 注册中心的类型及地址，还是RPC server的 port number。

application.properties

```properties
gp.rpc.servicePort=20880
gp.rpc.registryType=0
gp.rpc.registryAddress=192.168.221.128:2181

```



### 服务的启动

ServiceProvider是一个SpringBoot项目。 

```
@ComponentScan(basePackages = {"com.me.learn.rpc.spring.service", "com.me.learn.rpc.service"})
@SpringBootApplication
public class NettyRpcProvider {
    public static void main(String[] args) {
        SpringApplication.run(NettyRpcProvider.class, args);
    }
}
```

> @ComponentScan(basePackages = {"com.me.learn.rpc.spring.service", "com.me.learn.rpc.service"}),   让Spring IOC容器扫描解析GpRemoteService注解的Bean。

## Service Consumer

服务的消费端，在使用服务的地方，只需加入@GpRemoteReferrence就可以自动注入服务的动态代理。 

### 服务的引用

```java
@RestController
public class TestController {

    @GpRemoteReferrence
    private IUserService userService;

    @GetMapping("/user")
    public String saveUser() {
        User user= new User();
        user.setUserName("Jed Li");
        user.setAge(18);
        return userService.saveUser(user);
    }
}
```

### 配置

在服务消费端需要配置注册中心的类型及注册中心的地址。 

application.properties

```properties
gp.client.registryAddress=192.168.3.111:2181,192.168.3.112:2181,192.168.3.113:2181
gp.client.registryType=0
```

### 启动类配置

```java
@ComponentScan(basePackages = {"com.me.learn.rpc.spring.referrence","com.me.learn.rpc.controller"})
@SpringBootApplication
public class RpcConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RpcConsumerApplication.class, args);
    }
}
```

## 服务的注册与发现

完成服务的注册与发现，服务的负载均衡

# maven 依赖

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.me.learn</groupId>
        <artifactId>netty-rpc-protocol</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
    <artifactId>netty-rpc-registry</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>netty-rpc-registry</name>
    <description>服务的注册到注册中心和负载均衡</description>


    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.11</version>
            <scope>test</scope>
        </dependency>
        
        
        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-framework</artifactId>
            <version>4.2.0</version>
        </dependency>

        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-recipes</artifactId>
            <version>4.2.0</version>
        </dependency>

        <dependency>
            <groupId>org.apache.curator</groupId>
            <artifactId>curator-x-discovery</artifactId>
            <version>4.2.0</version>
        </dependency>
        
    </dependencies>
</project>
```



## 服务注册与发现的接口的定义

```java
public interface IRegistryService {

    void register(ServiceInfo serviceInfo) throws Exception;

    ServiceInfo discovery(String serviceName) throws Exception;
}
```

## 服务注册与发现的zookeeper实现

```java
@Slf4j
public class ZookeeperRegistryService implements IRegistryService {

    private static final String REGISTRY_PATH = "/registry";
    private CuratorFramework curatorFramework;
    private ServiceDiscovery<ServiceInfo> serviceDiscovery;


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
        ServiceInstance<ServiceInfo> serviceInstance =  new RandomLoadBalance().select((List<ServiceInstance<ServiceInfo>>) serviceInstances);
        if (serviceInstance != null) {
            return serviceInstance.getPayload();
        }

        return null;
    }
}
```

## 负载均衡的接口定义及实现

### 接口定义

```java
public interface ILoadBalancer {
    ServiceInstance<ServiceInfo> select(List<ServiceInstance<ServiceInfo>> services);
}


public abstract class AbstractLoadBalancer implements ILoadBalancer {
    @Override
    public ServiceInstance<ServiceInfo> select(List<ServiceInstance<ServiceInfo>> services) {
        if (services == null || services.isEmpty()) {
            return  null;
        }
        
        return doSelect(services);
    }

    protected abstract ServiceInstance<ServiceInfo> doSelect(List<ServiceInstance<ServiceInfo>> services);
}
```

### 随机负载均衡的实现

```
public class RandomLoadBalance extends AbstractLoadBalancer {
    @Override
    protected ServiceInstance<ServiceInfo> doSelect(List<ServiceInstance<ServiceInfo>> services) {
        int totalServiceCount = services.size();
        Random random = new Random();
        return services.get(random.nextInt(totalServiceCount));
    }
}
```

# 参考资料及要点

1. FactoryBean
2. Spring Bean的生命周期每个阶段的回调事件，借助回调事件来完成业务逻辑的处理
