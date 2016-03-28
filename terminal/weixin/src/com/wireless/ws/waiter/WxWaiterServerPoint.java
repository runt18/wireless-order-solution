package com.wireless.ws.waiter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/ws/wx-waiter/{restaurantId}")
public class WxWaiterServerPoint {
 
    public static final List<WxWaiter> waiters = new CopyOnWriteArrayList<>();
    
    public WxWaiterServerPoint() {
    	
    }
 
    public static WxWaiter getWxWaiter(int restaurantId){
        int index = waiters.indexOf(new WxWaiter(restaurantId));
        if(index >= 0){
        	return waiters.get(index);
        }else{
        	return null;
        }
    }
    
    /**
     * 打开连接
     * 
     * @param session
     * @param restaurantId
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "restaurantId") String restaurantId) {
 
        WxWaiter waiter = getWxWaiter(Integer.parseInt(restaurantId));
        if(waiter != null){
        	waiter.add(session);
        }else{
        	waiters.add(new WxWaiter(Integer.parseInt(restaurantId)).add(session));
        }
        
        System.out.println(restaurantId + ">" + "joined...");
        
    }
 
    /**
     * 关闭连接
     */
    @OnClose
    public void onClose(Session session, CloseReason reason, @PathParam(value = "restaurantId") String restaurantId) {
        WxWaiter waiter = getWxWaiter(Integer.parseInt(restaurantId));
        if(waiter != null){
        	waiter.remove(session);
        }
    	
    	System.out.println(restaurantId + ">" + "closed..." + reason);
    }
 
    /**
     * 接收信息
     * 
     * @param message
     * @param restaurantId
     */
    @OnMessage
    public void onMessage(String message, @PathParam(value = "restaurantId") String restaurantId) {
        System.out.println((restaurantId + ">" + message));
    }
 
    /**
     * 错误信息响应
     * 
     * @param throwable
     */
    @OnError
    public void onError(@PathParam(value = "restaurantId") String restaurantId, Session session, Throwable throwable) {
        System.out.println(restaurantId + ">" + throwable.getMessage());
    }
 
}