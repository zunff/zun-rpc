# 手写RPC框架
## RPC框架架构图
![img.png](./img/img.png)

## 一、服务消费者、提供者定义

### 1.1 提供者
1. 定义UserService接口的具体方法实现
2. 启动web服务器，这里使用Vertx，也可以使用Tomcat
   - Web服务器的作用是接收http请求，并根据body中的信息从注册器中获取Service实现类，调用其中的方法，并封装方法调用的返回值，最后响应HTTP请求
```java
public class ProviderMain {

    public static void main(String[] args) {

        //服务启动时将服务注册到注册器中
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        VertxHttpServer vertxHttpServer = new VertxHttpServer();

        vertxHttpServer.doStart(8088);

    }
}
```

### 1.2 消费者
1. 消费者正常调用方法，如同调用本地方法一样即可，但要注意，Service接口要放在common包中，被消费者和提供者共用
```java
public class ConsumerMain {
    public static void main(String[] args) {

        //获取UserServiceImpl示例
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);

        User user = userService.getUser("ZunF");
        if (user == null) {
            System.out.println("user == null");
        } else {
            System.out.println(user);
        }
    }
}
```
## 二、RPC框架
### 2.1 序列化器
代理对象和Web服务器之间的交互，其中的数据需要先进行序列化（转化为byte数组），才能够在网络上传输。
#### 2.1.1 序列化器接口
```java
 public interface Serializer {

    /**
     * 序列化
     * @param object 需要序列化的对象
     * @return 序列化结果
     * @param <T> 需要进行序列化的类
     * @throws IOException IO异常
     */
    <T> byte[] serialize(T object) throws IOException;


    /**
     * 反序列化
     * @param bytes 序列化byte数组
     * @param type 反序列化的目标类Class对象
     * @return 反序列化的结果
     * @param <T> 反序列化的目标类型
     * @throws IOException IO异常
     */
    <T> T deserialize(byte[] bytes, Class<T> type) throws IOException;
}
```
#### 2.1.2 Jdk序列化器
```java
public class JdkSerializer implements Serializer {
    /**
     * 序列化
     *
     * @param object 需要序列化的对象
     * @param <T>    需要进行序列化的类，注意，这个类需要实现Serializable接口
     * @return 序列化结果
     * @throws IOException IO异常
     */
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 反序列化
     *
     * @param bytes 序列化byte数组
     * @param type  反序列化的目标类Class对象
     * @param <T>   反序列化的目标类型，注意，这个类需要实现Serializable接口
     * @return 反序列化的结果
     * @throws IOException IO异常
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        try {
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            objectInputStream.close();
        }
    }
}
```

### 2.2 Web服务器
用于服务提供者启动，接收请求
```java
public class VertxHttpServer implements HttpServer {
    @Override
    public void doStart(int port) {
        //创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        //创建 HTTP 服务器
        io.vertx.core.http.HttpServer server = vertx.createHttpServer();

        //设置请求的处理器
        server.requestHandler(new VertxHttpServerHandler());

        //启动 HTTP 服务器并监听指定端口
        server.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("Server is now listening on port " + port);
            } else {
                System.out.println("Failed to start server: " + result.cause());
            }
        });
    }
}
```

### 2.3 代理工厂、代理类
用于创建代理对象，向Web服务器发送网络请求，调用服务提供者的代码，并处理方法返回值，让用户无感地远程调用方法
#### 2.3.1 代理工厂
```java
public class ServiceProxyFactory {


    public static <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ServiceProxy());
    }

}
```

#### 2.3.2 代理类
```java
public class ServiceProxy implements InvocationHandler {


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Serializer serializer = new JdkSerializer();

        //构造请求参数
        RpcRequest request = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .paramTypes(method.getParameterTypes())
                .params(args)
                .build();

        try {
            byte[] bytes = serializer.serialize(request);
            //发送请求
            try (HttpResponse httpResponse = HttpRequest
                    .post("http://localhost:8088")
                    .body(bytes).execute()){

                //处理接口执行结果并返回
                byte[] result = httpResponse.bodyBytes();
                //反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
```
## 三、全局配置化
1. 可以利用Properties类对指定的.properties文件进行读取，读取的结果是一个类似于Map的数据类型，通过key就能够获取到value。
```properties
# application.properties  
app.name=My Application  
app.version=1.0.0  
server.port=8080
```
```java
public class PropertiesDemo {  
  
    public static void main(String[] args) {  
        Properties properties = new Properties();  
  
        try (FileInputStream fis = new FileInputStream("src/main/resources/application.properties")) {  
            // 从输入流中加载属性列表  
            properties.load(fis);  
  
            // 读取并打印属性  
            String appName = properties.getProperty("app.name");  
            String appVersion = properties.getProperty("app.version");  
            String serverPort = properties.getProperty("server.port");  
  
            System.out.println("App Name: " + appName);  
            System.out.println("App Version: " + appVersion);  
            System.out.println("Server Port: " + serverPort);  
  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
}
```
2. 利用第三方工具类，例如Hutools的Props，这种封装过的工具类会更加方便
3. 想要读取.yaml文件，Properties是不行的，得引入第三方工具类
4. 项目中的配置，分为通用、consumer、provider 三个部分

```java
public class RpcConfig {

    //consumer、provider 通用配置 begin

    /**
     * 服务提供者监听端口
     */
    private int serverPort = 8088;

    /**
     * 序列化器
     */
    private String serializer = "jdk";

    //end


    //consumer begin

    /**
     * 服务提供者地址
     */
    private String serverHost = "http://localhost";

    /**
     * 是否使用模拟数据
     */
    private boolean isMock = false;

    //end


    //provider begin

    //end
}

```



## 四、创建Mock数据
- 也就是模拟数据，当服务提供端并没有搭建好时，服务消费者想要跑通流程，就需要要用到Mock数据，用来模拟服务提供端的行为
1. 消费者配置类添加一个配置项：
```java
    /**
     * 是否使用模拟数据
     */
    private boolean isMock = false;
```
2. 代理工厂中判断是否开启模拟数据，开启了就用模拟数据代理类来代理对象
```java
if (RpcApplication.getRpcConsumerConfig().isMock()) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader()
                                          , new Class[]{clazz}, new MockServiceProxy());
}
```
3. 如果开启了就根据方法返回类型返回默认值
```java
public class MockServiceProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //根据方法的返回值类型，返回特定数值
        Class<?> returnType = method.getReturnType();
        if (returnType.isPrimitive()) {
            if (returnType == boolean.class) {
                return false;
            } else if (returnType == byte.class) {
                return (byte) 0;
            } else if (returnType == short.class) {
                return (short) 0;
            } else if (returnType == int.class) {
                return 0;
            } else if (returnType == long.class) {
                return 0L;
            } else if (returnType == float.class) {
                return 0F;
            } else if (returnType == double.class) {
                return 0D;
            } else if (returnType == char.class) {
                return (char) 0;
            } else if (returnType == void.class) {
                return null;
            }
        }
        return null;
    }
}
```

## 五、序列化器与SPI机制
### 5.1 主流序列化方式对比
**1）JSON**

优点：

- 可读性强
- 跨语言支持广泛

缺点：

- 序列化后数据量相对较大
- 不能很好地处理复杂的数据结构和循环引用

**2）Hession**

优点：

- 二进制序列化，序列化后数据量较小
- 支持跨语言，适用于分布式系统间的调用

缺点：

- 性能相对JSON略低，因为要将对象转换成二进制格式
- 对象必须实现Serializable接口

**3）kryo**

优点：

- 高性能，序列化和反序列化速度快
- 支持循环引用和自定义序列化器，适用于复杂的对象结构
- 无需对象实现Serializable接口

缺点：

- 只适用于Java
- 序列化格式不易读懂，不便于调试

**4）Protobuf**

优点：

- 高效的二进制序列化，序列化后数据极小
- 支持跨语言，并提供了多种语言的实现库

缺点：

- 配置十分麻烦
- 序列化格式不易读懂，不便于调试



#### 5.1.1 Json序列化器

需要解决一个问题：RpcResponse的data属性为Object类型，RpcRequest的params为Object类型。**在反序列化时，Object类型和泛型的类型都会被擦除**，也就导致jackson不知道把它反序列化成什么类型的变量，导致最后反序列化的结果是LinkedHashMap类型，所以如果想要正常得显示Object类型，需要保存原来的类型，并在反序列化时，反序列化成原来所希望的类型，而不是LinkedHashMap。

BUG：

![截屏2024-05-06 16.03.51](./img/截屏2024-05-06 16.03.51.png)

期待：

![截屏2024-05-06 16.24.36](./img/截屏2024-05-06 16.24.36.png)

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zjh.rpc.model.RpcRequest;
import com.zjh.rpc.model.RpcResponse;
import com.zjh.rpc.serializer.Serializer;
import java.io.IOException;

public class JsonSerializer implements Serializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T t = OBJECT_MAPPER.readValue(bytes, type);
        //RpcResponse的data属性为Object类型，RpcRequest的params为Object类型
        //在反序列化时，Object类型和泛型的类型都会被擦除，也就导致jackson不知道把它反序列化成什么类型的变量
        //导致最后反序列化的结果是LinkedHashMap类型
        //所以如果想要正常得显示Object类型，需要保存原来的类型，并在反序列化时，反序列化成原来所希望的类型，而不是LinkedHashMap
        handleTypeError(t);
        return t;
    }

    /**
     * 处理RpcResponse的data、RpcRequest的params属性，被反序列化为LinkedHashMap的错误
     *
     * @param t   RpcResponse或RpcRequest对象
     * @param <T> RpcResponse或RpcRequest类
     * @throws IOException IO异常
     */
    private <T> void handleTypeError(T t) throws IOException {
        if (t instanceof RpcRequest) {
            RpcRequest rpcRequest = (RpcRequest) t;
            Class<?>[] paramTypes = rpcRequest.getParamTypes();
            Object[] params = rpcRequest.getParams();
            for (int i = 0; i < params.length; i++) {
                //如果类型不同，重新处理类型
                if (!params[i].getClass().equals(paramTypes[i])) {
                    byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(params[i]);
                    params[i] = OBJECT_MAPPER.readValue(bytes, paramTypes[i]);
                }
            }
        } else if (t instanceof RpcResponse) {
            RpcResponse rpcResponse = (RpcResponse) t;
            Object data = rpcResponse.getData();
            Class<?> dataType = rpcResponse.getDataType();
            //如果类型不同，重新处理类型
            if (!data.getClass().equals(dataType)) {
                byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(data);
                rpcResponse.setData(OBJECT_MAPPER.readValue(bytes, dataType));
            }
        }
    }
}

```

#### 5.1.2 Hessian序列化器

```java
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.zjh.rpc.serializer.Serializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(bos);
        ho.writeObject(object);
        return bos.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        HessianInput hi = new HessianInput(bis);
        return (T) hi.readObject(type);
    }
}

```

#### 5.1.3 Kryo 序列化器

```java
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.zjh.rpc.serializer.Serializer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoSerializer implements Serializer {

    /**
     * kryo线程不安全，使用ThreadLocal保证每一个线程只有一个kryo
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        //设置动态注册序列化和反序列化类，不提前注册所有类（可能有安全问题）
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Output output = new Output(bos);
        kryo.writeObject(output, object);
        output.close();
        return bos.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        Input input = new Input(bis);
        T t = kryo.readObject(input, type);
        input.close();
        return t;
    }
}

```



### 5.2 SPI机制

SPI（Service Provider Inteface）服务提供者接口，主要用于实现模块化开发和插件化扩展。

SPI机制允许服务提供者通过特定的配置流程，将自己的实现注册到系统中，然后通过反射机制动态地加载这些实现，而不需要修改源码，从而实现解耦、提高了可扩展性。

#### 5.2.1 系统实现SPI机制

Java内已经实现了SPI机制的相关接口，可以直接使用：

1）首先在`resource`资源目录下创建`META-INF/service`目录，并创建一个命名为：要实现的接口全类名的空文件

![image-20240506144316849](./img/image-20240506144316849.png) 

2）在文件中填写需要自己定制的接口实现类的全类名

![image-20240506144642529](./img/image-20240506144642529.png) 

3）直接使用系统内置的 `ServiceLoader` 动态加载指定接口的实现类，代码如下：

```java
// 指定序列化器
Serializer serializer = null;
ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
for (Serializer service : serviceLoader) {
	serializer = service;
}
```

#### 5.2.2 自定义实现SPI机制

考虑到系统提供的SPI接口不能让用户在配置文件中通过配置选择使用哪个序列化器，所以自定义实现一个可以通过key=>value中的key来选择不同的序列化器全类名value的SPI机制。

1）系统提供的序列化器文件存储在 `resource/META-INF/rpc/system` 目录下；用户自定义的存储在 `resource/META-INF/rpc/custom` 目录下。如果 system 与 custom 中出现key冲突，保留custom中的配置。

2）`resource/META-INF/rpc/system`放在rpm-core项目中， `resource/META-INF/rpc/custom` 存放在用户自己的项目目录中，因为消费者和提供者都需要使用这个序列化器，所以建议将文件放在双方都依赖的common项目中。

![image-20240507115217580](./img/image-20240507115217580.png) 

 3）自定义SPI加载器：

```java
import cn.hutool.core.io.resource.ResourceUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI 服务加载器
 *
 * @author zunf
 * @date 2024/5/7 09:41
 */
public class SpiLoader {

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{"META-INF/rpc/system/", "META-INF/rpc/custom/"};

    /**
     * 已加载的全类名。接口全类名 => (key => 接口实现类全类名)
     */
    private static final Map<String, Map<String, Class<?>>> LOAD_CLASS_MAP = new ConcurrentHashMap<>();

    /**
     * 服务对象缓存。全类名 => 类对象实例
     */
    private static final Map<String, Object> SPI_OBJECT_MAP = new ConcurrentHashMap<>();


    /**
     * 加载 META-INF/rpc/system、custom 下的类路径，custom定义的类优先级高，可覆盖system中的设置
     *
     * @param loadClass 需要加载的接口
     */
    public static void load(Class<?> loadClass) {
        //全类名所对应注册的实现类map
        Map<String, Class<?>> keyClassMap = new HashMap<>();

        for (String scanDir : SCAN_DIRS) {
            //扫描 system、custom 下的接口全类名文件
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] split = line.split("=");
                        if (split.length == 2) {
                            String key = split[0];
                            String className = split[1];
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //缓存到全局对象中
        LOAD_CLASS_MAP.put(loadClass.getName(), keyClassMap);
    }


    public static <T> T getSpiObject(Class<T> clazz, String key) {
        Map<String, Class<?>> keyClassMap = LOAD_CLASS_MAP.get(clazz.getName());
        if (keyClassMap == null || !keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoad 不存在 %s => %s", clazz.getName(), key));
        }
        //获取要加载的类型
        Class<?> aClass = keyClassMap.get(key);
        //从对象缓存中获取对象
        if (!SPI_OBJECT_MAP.containsKey(aClass.getName())) {
            synchronized (SPI_OBJECT_MAP) {
                if (!SPI_OBJECT_MAP.containsKey(aClass.getName())) {
                    try {
                        SPI_OBJECT_MAP.put(aClass.getName(), aClass.newInstance());
                    } catch (Exception e) {
                        throw new RuntimeException(String.format("%s 类实例化失败", aClass.getName()), e);
                    }
                }
            }
        }
        return (T) SPI_OBJECT_MAP.get(aClass.getName());
    }
}

```

4）序列化器工厂

```java
import com.zjh.rpc.serializer.Serializer;
import com.zjh.rpc.spi.SpiLoader;

/**
 * 序列化器工厂
 *
 * @author zunf
 * @date 2024/5/7 10:46
 */
public class SerializerFactory {

    static {
        SpiLoader.load(Serializer.class);
    }

    public static Serializer getInstance(String key) {
        return SpiLoader.getSpiObject(Serializer.class, key);
    }

}
```





