package com.alibaba.middleware.race.rpc.api;

import java.util.Map;

import org.junit.Assert;

//import org.junit.Assert;

import com.alibaba.middleware.race.rpc.aop.ConsumerHook;
import com.alibaba.middleware.race.rpc.api.impl.RpcConsumerImpl;
import com.alibaba.middleware.race.rpc.async.ResponseFuture;
import com.alibaba.middleware.race.rpc.demo.service.RaceConsumerHook;
import com.alibaba.middleware.race.rpc.demo.service.RaceDO;
import com.alibaba.middleware.race.rpc.demo.service.RaceException;
import com.alibaba.middleware.race.rpc.demo.service.RaceServiceListener;
import com.alibaba.middleware.race.rpc.demo.service.RaceTestService;


public class TestClient {
	 private static RpcConsumer consumer;
	    private static RaceTestService apiService;
	    static {
	        consumer = new RpcConsumerImpl();
	        apiService = (RaceTestService) consumer
	                .interfaceClass(RaceTestService.class)
	                .version("1.0.0.api")
	                .clientTimeout(3000)
	                .hook((ConsumerHook)new RaceConsumerHook()).instance();
	    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 //-----------------------------------------------
		// testException
//        try {
//            Integer result = apiService.throwException();
//        } catch (RaceException e) {
//            Assert.assertEquals("race", e.getFlag());
//            e.printStackTrace();
//        }
//		
//		Assert.assertNotNull(apiService.getMap());
//	     Assert.assertEquals("this is a rpc framework", apiService.getString());
//	     Assert.assertEquals(new RaceDO(), apiService.getDO());
//
//	     Map<String, Object> resultMap = apiService.getMap();
//	     Assert.assertTrue(resultMap.containsKey("hook key"));
//	     Assert.assertTrue(resultMap.containsValue("this is pass by hook"));
//	     


	  //-----------------------------------------------
	     // testFutureCall
	     consumer.asynCall("getString");
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
      //--------------------------------------
         // testTimeOut 
//	     long beginTime = System.currentTimeMillis();
//	     try {
//	            boolean result = apiService.longTimeMethod();
//	     } catch (Exception e) {
//	            long period = System.currentTimeMillis() - beginTime;
//	           
//	            Assert.assertTrue(period < 3100);
//	            System.out.println("timeout succ");
//	     }
//	 //--------------------------------------------
//	     // testAsynCall
//	     RaceServiceListener listener = new RaceServiceListener();
//	     consumer.asynCall("getDO", listener);
//	     RaceDO nullDo = apiService.getDO();
//	     Assert.assertEquals(null, nullDo);
//	     try {
//	         RaceDO resultDo = (RaceDO) listener.getResponse();
//	     } catch (InterruptedException e) {
//	     }
	   
	     System.out.println("Succ");
	}
}
