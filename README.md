# rpc-framework
一个基于socket的RPC通信框架

####  一、特点：
  *1、支持同步和异步调用
  *2、支持超时时间设置
  *3、支持上下文传递
  *4、支持远程异常抛出，即远程服务器抛出的异常可以在客户端捕获
  *5、支持hook，即可以在方法调用前后做一些其他工作，方便扩展
  
  
### 二、使用：


####服务端

  1、服务端初始化
  
```java

  static{
		RpcProvider rpcProvider = null;
		rpcProvider = new RpcProviderImpl();
		rpcProvider.serviceInterface(RaceTestService.class)
	            .impl(new RaceTestServiceImpl())
	            .version("1.0.0.api")
	            .timeout(3000)
	            .serializeType("java").publish();  
  }
```


####客户端

  1、客户端初始化
```java  

   private static RaceTestService apiService;
	 static {
	        consumer = new RpcConsumerImpl();
	        apiService = (RaceTestService) consumer
	                .interfaceClass(RaceTestService.class)
	                .version("1.0.0.api")
	                .clientTimeout(3000)
	                .hook((ConsumerHook)new RaceConsumerHook()).instance();
	 }
```
  2、  异步调用getString方法
```java
   //标记异步调用getString
   consumer.asynCall("getString");
   //调用getString(),把服务端的返回结果放在Future返回结果中
   String nullValue = apiService.getString();
   Assert.assertEquals(null, nullValue);
   try {
      String result = (String) ResponseFuture.getResponse(3000);
      Assert.assertEquals("this is a rpc framework", result);
   } catch (InterruptedException e) {
      e.printStackTrace();
   } finally {
      consumer.cancelAsyn("getString");
   }
```
####更多例子参见：
  [testServer](https://github.com/buptmiao/rpc-framework/blob/master/src/com/alibaba/middleware/race/rpc/api/TestProvider.java "testProvider")
  
  
  [testClient](https://github.com/buptmiao/rpc-framework/blob/master/src/com/alibaba/middleware/race/rpc/api/TestClient.java "testClient")
  
  
  
