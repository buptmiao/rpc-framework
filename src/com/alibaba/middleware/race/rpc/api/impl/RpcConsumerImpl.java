package com.alibaba.middleware.race.rpc.api.impl;


import com.alibaba.middleware.race.rpc.aop.ConsumerHook;
import com.alibaba.middleware.race.rpc.api.RpcConsumer;
import com.alibaba.middleware.race.rpc.async.ResponseCallbackListener;
import com.alibaba.middleware.race.rpc.async.ResponseFuture;
import com.alibaba.middleware.race.rpc.context.RpcContext;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.Callable;
/**
 * Created by huangsheng.hs on 2015/3/26.
 */
public class RpcConsumerImpl extends RpcConsumer implements InvocationHandler {
	private final static Logger LOG = Logger.getLogger(RpcConsumerImpl.class.getName());
	
    private Class<?> interfaceClazz;
    private String version;
    private int    timeout;
    private ConsumerHook hook;
//    private String asyncCallMethod;
    private RpcConsumerConnectionFactory cFactory;
    private final ExecutorService executor;
    private final Map<String,ResponseCallbackListener > asynMap;
    public RpcConsumerImpl() {
    	interfaceClazz = null;
    	version = null;
    	timeout = Integer.MAX_VALUE;
    //	asyncCallMethod = null;
    	asynMap = new HashMap<String,ResponseCallbackListener >();
    	
    	String hostip = System.getProperty("SIP");
    	//System.out.println(hostip);
    	this.cFactory = new RpcConsumerConnectionFactory(hostip,8888,true);
    	this.executor = Executors.newFixedThreadPool(10);
    }

  

    /**
     * set the interface which this consumer want to use
     * actually,it will call a remote service to get the result of this interface's methods
     *
     * @param interfaceClass
     * @return
     */
    @Override
	public RpcConsumer interfaceClass(Class<?> interfaceClass) {
        this.interfaceClazz = interfaceClass;
        return this;
    }

    ;

    /**
     * set the version of the service
     *
     * @param version
     * @return
     */
    @Override
	public RpcConsumerImpl version(String version) {
        //TODO
    	this.version = version;
        return this;
    }

    /**
     * set the timeout of the service
     * consumer's time will take precedence of the provider's timeout
     *
     * @param clientTimeout
     * @return
     */
    @Override
	public RpcConsumer clientTimeout(int clientTimeout) {
        //TODO
    	this.timeout = clientTimeout;
        return this;
    }

    /**
     * register a consumer hook to this service
     * @param hook
     * @return
     */
    public RpcConsumer hook(ConsumerHook hook) {
    	this.hook = hook;
        return this;
    }

    /**
     * return an Object which can cast to the interface class
     *
     * @return
     */
    @Override
	public Object instance() {
        //TODO return an Proxy
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class[]{this.interfaceClazz},this);
    }

    /**
     * mark a async method,default future call
     *
     * @param methodName
     */
    @Override
	public void asynCall(String methodName) {
        asynCall(methodName, null);
    }

    /**
     * mark a async method with a callback listener
     *
     * @param methodName
     * @param callbackListener
     */
    @Override
	public <T extends ResponseCallbackListener> 
    void asynCall(String methodName, T callbackListener) {
        //TODO
    	asynMap.put(methodName, callbackListener);
    }

    @Override
	public void cancelAsyn(String methodName) {
        //TODO
    	if(asynMap.containsKey(methodName))
    		asynMap.remove(methodName);
    	
    }


    /**
     * Processes a method invocation on a proxy instance and returns
     * the result.  This method will be invoked on an invocation handler
     * when a method is invoked on a proxy instance that it is
     * associated with.
     *
     * @param proxy  the proxy instance that the method was invoked on
     * @param method the {@code Method} instance corresponding to
     *               the interface method invoked on the proxy instance.  The declaring
     *               class of the {@code Method} object will be the interface that
     *               the method was declared in, which may be a superinterface of the
     *               proxy interface that the proxy class inherits the method through.
     * @param args   an array of objects containing the values of the
     *               arguments passed in the method invocation on the proxy instance,
     *               or {@code null} if interface method takes no arguments.
     *               Arguments of primitive types are wrapped in instances of the
     *               appropriate primitive wrapper class, such as
     *               {@code java.lang.Integer} or {@code java.lang.Boolean}.
     * @return the value to return from the method invocation on the
     * proxy instance.  If the declared return type of the interface
     * method is a primitive type, then the value returned by
     * this method must be an instance of the corresponding primitive
     * wrapper class; otherwise, it must be a type assignable to the
     * declared return type.  If the value returned by this method is
     * {@code null} and the interface method's return type is
     * primitive, then a {@code NullPointerException} will be
     * thrown by the method invocation on the proxy instance.  If the
     * value returned by this method is otherwise not compatible with
     * the interface method's declared return type as described above,
     * a {@code ClassCastException} will be thrown by the method
     * invocation on the proxy instance.
     * @throws Throwable the exception to throw from the method
     *                   invocation on the proxy instance.  The exception's type must be
     *                   assignable either to any of the exception types declared in the
     *                   {@code throws} clause of the interface method or to the
     *                   unchecked exception types {@code java.lang.RuntimeException}
     *                   or {@code java.lang.Error}.  If a checked exception is
     *                   thrown by this method that is not assignable to any of the
     *                   exception types declared in the {@code throws} clause of
     *                   the interface method, then an
     *                   {@link UndeclaredThrowableException} containing the
     *                   exception that was thrown by this method will be thrown by the
     *                   method invocation on the proxy instance.
     * @see UndeclaredThrowableException
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    	//直接调用远程方法
    	hook.before(null);
    	RpcRequest req = new RpcRequest();
    	req.setMethodName(method.getName());
    	req.setParameters(args);
    	//添加上下文
    	if(!RpcContext.getProps().isEmpty()){
    		req.setProps(RpcContext.getProps());
    	}
    	String methodname = method.getName();
    	if(asynMap.containsKey(methodname)){
    		try{
    			callAsynMethod(req,asynMap.get(methodname));
    		} catch(InterruptedException e){
    			e.printStackTrace();
    			throw e;
    		}
    	}else{
    		try{
    			Object resp = callBlockMethod(req);
    			if(resp instanceof Throwable){
    				//LOG.info("invoke throw");
    				throw ((Throwable)resp).getCause();
    			}
    			
    			Object result = ((RpcResponse)resp).getAppResponse();
    			return result;
    		} catch(InterruptedException e){
    			//e.printStackTrace();
    			throw e;
    		}
    	}
    	
    	hook.after(null);
        return null;
    }
    public void callAsynMethod(RpcRequest req,final ResponseCallbackListener done ) throws InterruptedException{
    	
    	final RpcConnection connection;
    	try{
    		connection = this.cFactory.createConnection(timeout);
    	} catch(IOException e){
    		e.printStackTrace();
    		return;
    	}
    	try{
    		connection.sendMessage(req);;
    	}catch(IOException e){
    		e.printStackTrace();
    		return;
    	}
    	ResponseFuture.setFuture(executor.submit(new Callable<Object>() {
    		@Override
    		public Object call(){
    			try{
    				Object resp = connection.receiveMessage();
    				if(done != null){
    					// 接收到异常
    					if(resp instanceof Exception){
    						done.onException((Exception)resp);
    					}else{ //接收到响应
    						done.onResponse(((RpcResponse)resp).getAppResponse());
    					}
    				}
    				return resp;
    			} catch(IOException e){
    				e.printStackTrace();
    			} catch(RuntimeException e){
    				e.printStackTrace();
    				throw e;
    			}
    			finally{
    				close(connection);
    			}
    			return null;
    		}
    	}));

    }
    public Object callBlockMethod(RpcRequest req) throws RuntimeException{
    	RpcConnection connection = null;
    	try{
    		connection = this.cFactory.createConnection(timeout);
    		connection.sendMessage(req);
    		return connection.receiveMessage();
    	}catch(IOException e){
    		//e.printStackTrace();
    	}finally{
    		close(connection);
    	}
    	return null;
    }
    	
    private void close(RpcConnection connection){
    	try{
    		connection.close();
    	}catch(IOException e){

    	}
    }
}
