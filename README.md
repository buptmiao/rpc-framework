# rpc-framework
一个基于socket的RPC通信框架

####  一、特点：
  1、支持异步调用
  2、支持超时时间设置
  3、支持上下文传递
  4、支持远程异常抛出，即远程服务器抛出的异常可以在客户端捕获
  5、支持hook，即可以在方法调用前后做一些其他工作，方便扩展
  
  
### 二、使用：
  
  [testServer](https://github.com/buptmiao/rpc-framework/blob/master/src/com/alibaba/middleware/race/rpc/api/TestProvider.java "testProvider")
  [testClient](https://github.com/buptmiao/rpc-framework/blob/master/src/com/alibaba/middleware/race/rpc/api/TestClient.java "testClient")
  
  
  
### 三、不足：
  通信基于socket BIO方式，效率比较低
  序列化方式采用的java自带的序列化方式
  
