package com.alibaba.middleware.race.rpc.api.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.middleware.race.rpc.api.RpcProvider;
import com.alibaba.middleware.race.rpc.async.ResponseCallbackListener;
import com.alibaba.middleware.race.rpc.context.RpcContext;
import com.alibaba.middleware.race.rpc.model.RpcRequest;
import com.alibaba.middleware.race.rpc.model.RpcResponse;

public class RpcProviderImpl extends RpcProvider{
	private static final Logger LOG = Logger.getLogger(RpcProviderImpl.class.getName());
	
	private String version;
	private int    timeout;
	private Class<?>  interfaceclazz;
	private Object interfaceimpl;
	private final RpcProviderConnectionFactory pFactory;
	private final ExecutorService executor;
	private final ServerThread serverThread;
	private final boolean waitForCallback;
	//private final Map<String, Class<?> > serviceMap;
	public RpcProviderImpl() {
		version = null;
		this.pFactory = new RpcProviderConnectionFactory(8888,true);
		this.executor = Executors.newFixedThreadPool(10);
		this.serverThread = new ServerThread();
		this.serverThread.setDaemon(true);
		this.waitForCallback = false;
		//this.serviceMap = new HashMap<String, Class<?> >();
	}

    /**
     * init Provider
     */
    private void init(){
        //TODO
    }

    /**
     * set the interface which this provider want to expose as a service
     * @param serviceInterface
     */
    @Override
	public RpcProvider serviceInterface(Class<?> serviceInterface){
        //TODO
    	interfaceclazz = serviceInterface;
        return this;
    }

    /**
     * set the version of the service
     * @param version
     */
    @Override
	public RpcProvider version(String version){
        //TODO
    	this.version = version;
        return this;
    }

    /**
     * set the instance which implements the service's interface
     * @param serviceInstance
     */
    @Override
	public RpcProvider impl(Object serviceInstance){
        //TODO
    	interfaceimpl = serviceInstance;
        return this;
    }

    /**
     * set the timeout of the service
     * @param timeout
     */
    @Override
	public RpcProvider timeout(int timeout){
        //TODO
    	this.timeout = timeout;
        return this;
    }

    /**
     * set serialize type of this service
     * @param serializeType
     */
    @Override
	public RpcProvider serializeType(String serializeType){
        //TODO
        return this;
    }

    /**
     * publish this service
     * if you want to publish your service , you need a registry server.
     * after all , you cannot write servers' ips in config file when you have 1 million server.
     * you can use ZooKeeper as your registry server to make your services found by your consumers.
     */
    @Override
	public void publish() {
        //TODO
    	this.serverThread.run();
    }
    /**************************************************************
     *  服务线程
     */
    class ServerThread extends Thread {

        // Whether the server is running
        private volatile boolean running = false;

        @Override
        public void run() {
          LOG.info("Starting RPC server");
          try {
            running = true;
            while (running) {
              // Thread blocks here waiting for requests
            	RpcConnection connection = pFactory.createConnection();
              if (running && !executor.isShutdown()) {
                if (connection.isClosed()) {
                  // Connection was closed, don't execute
                  try {
                    Thread.sleep(100);
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                } else {
                  executor.execute(new ConnectionHandler(connection));
                }
              }
            }
          } catch (IOException ex) {
        	ex.printStackTrace();
            stopServer();
          } finally {
            running = false;
          }
        }

        private boolean isRunning() {
          return running;
        }

        private void stopServer() {
          if (isRunning()) {
            LOG.info("Shutting down RPC server");
            running = false;
            if (!executor.isShutdown()) {
              executor.shutdownNow();
            }
            try {
              pFactory.close();
            } catch (IOException e) {
              LOG.log(Level.WARNING, "Error while shutting down server", e);
            }
          }
        }
    }
    /******************************************************************
     *  每个线程的处理句柄
     */
    class ConnectionHandler implements Runnable {

      private final RpcConnection connection;

      ConnectionHandler(RpcConnection connection) {
        this.connection = connection;
      }

      @Override
      public void run() {
        try {
          // Parse request
          RpcRequest req = (RpcRequest)connection.receiveMessage();
          
          RpcContext.props.putAll((Map)req.getProps());
          if (waitForCallback) {
            forwardAsynRpc(req);
          } else {
            forwardBlockingRpc(req);
          }
        } catch (IOException e) {
          sendResponse(handleError("Bad request data from client", e));
        }
      }

      private void forwardAsynRpc(RpcRequest rpcRequest) {
        // Create callback to pass to the forwarder
        ResponseCallbackListener rpcCallback =
            new ResponseCallbackListener() {
          @Override
          public void onResponse(Object rpcResponse) {
            sendResponse(rpcResponse);
          }
          @Override
          public void onTimeout(){
        	  
          }
          @Override
          public void onException(Exception e){
        	sendResponse(e);
          }
        };
        try {
          doAsynRpc(rpcRequest, rpcCallback);
        } catch (Exception e) {
          sendResponse(handleError(e.getMessage(), e.getCause()));
        }
      }

      private void doAsynRpc(RpcRequest rpcRequest, ResponseCallbackListener rpcCallback) throws Exception {
  		// TODO Auto-generated method stub
    	 Method method;
      	 RpcResponse response;
      	 try{
    		method = interfaceimpl.getClass().getDeclaredMethod(rpcRequest.getMethodName());
    		Object resp = null;
    		try{
    			resp = method.invoke(interfaceimpl,rpcRequest.getParameters());
    		} catch(Exception e){
    			rpcCallback.onException(e);
    		}
    		response = new RpcResponse();
    		//response.setErrorMsg("");
    		response.setAppResponse(resp);
    	 } catch(Exception e){
    		throw e;
    	 }
    	 rpcCallback.onResponse(response);
  	  }

	  private void forwardBlockingRpc(RpcRequest rpcRequest) {
        // Forward request
        try {
          RpcResponse rpcResponse = doBlockingRpc(rpcRequest);
          sendResponse(rpcResponse);
        } catch (Exception e) {
          sendResponse(handleError(e.getMessage(),  e.getCause()));
        }
      }

      private RpcResponse doBlockingRpc(RpcRequest rpcRequest) throws InterruptedException{
		// TODO Auto-generated method stub
    	Method method;
    	RpcResponse response;
    	try{
    		method = interfaceimpl.getClass().getDeclaredMethod(rpcRequest.getMethodName());
    	}catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    	Object resp;
    	try{
    		resp = method.invoke(interfaceimpl,rpcRequest.getParameters());
    	}catch(Throwable e){
    		//返回异常
    		LOG.info("服务端返回异常！ ");
    		sendResponse(e);
    		return null;
    	}
    	response = new RpcResponse();
    	//response.setErrorMsg("");
    	response.setAppResponse(resp);
		return response;
	  }

	  private void sendResponse(Object rpcResponse) {
        try {
          if (connection.isClosed()) {
            // Connection was closed for some reason
            LOG.warning("Connection closed");
            return;
          }
          connection.sendMessage(rpcResponse);
        } catch (IOException e) {
          //LOG.log(Level.WARNING, "Error while writing", e);
        } finally {
          try {
            connection.close(); 
          } catch (IOException e) {
            // It's ok
            LOG.log(Level.WARNING, "Error while closing I/O", e);
          }
        }
      }

      private RpcResponse handleError(String msg,Throwable throwable) {
        LOG.log(Level.WARNING, msg, throwable);
        RpcResponse resp = new RpcResponse();
        resp.setErrorMsg(msg);
        return resp;
      }
    }
}

