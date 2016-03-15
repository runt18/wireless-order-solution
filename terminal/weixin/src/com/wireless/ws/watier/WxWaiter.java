package com.wireless.ws.watier;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.Session;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.weixin.order.WxOrder;

public class WxWaiter {

	public static enum MsgType{
		CALL_PAY(1, "呼叫结账"),
		WX_ORDER(2, "微信下单"),
		WX_BOOK(3, "微信预订");
		
		private final int val;
		private final String desc;
		
		MsgType(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static class Msg implements Jsonable{
		private final MsgType type;
		private final String content;
		
		public Msg(MsgType type, String content){
			this.type = type;
			this.content = content;
		}

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putInt("type", type.val);
			jm.putString("typeText", this.type.desc);
			jm.putString("content", this.content);
			jm.putString("hour", DateUtil.format(System.currentTimeMillis(), DateUtil.Pattern.HOUR));
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jm, int flag) {
		}
		
		@Override
		public String toString(){
			return "msg type = " + this.type.val + ", content = " + this.content;
		}
		
	}
	
	public static class Msg4CallPay extends Msg{
		public Msg4CallPay(String table, String expectPayType) {
			super(MsgType.CALL_PAY, "【" + table + "】的客人呼叫" + (expectPayType != null ? "使用" + expectPayType : "") + "结账，请留意处理");
		}
	}
	
	public static class Msg4WxOrder extends Msg{
		public Msg4WxOrder(WxOrder wxOrder) {
			super(MsgType.WX_ORDER, "【" + wxOrder.getTable().getName() + "】的会员【" + wxOrder.getMember().getName() + "】有微信下单，请留意处理");
		}
	}

    private final Set<Session> sessions = new CopyOnWriteArraySet<>();
	private final int restaurantId;
	
	public WxWaiter(int restaurantId){
		this.restaurantId = restaurantId;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof WxWaiter)){
			return false;
		}else{
			return ((WxWaiter)obj).restaurantId == this.restaurantId;
		}
	}
	
	public void send(Msg msg){
		for(Session session : sessions){
			try{
				session.getBasicRemote().sendText(JSONObject.toJSONString(msg.toJsonMap(0)));
			}catch(IOException e){
				try {
					session.close();
				} catch (IOException ignored) {
					ignored.printStackTrace();
				}
				sessions.remove(session);
				e.printStackTrace();
			}
		}
	}
	
	WxWaiter add(Session session){
		this.sessions.add(session);
		return this;
	}
	
	WxWaiter remove(Session session){
		this.sessions.remove(session);
		return this;
	}
	
	@Override
	public int hashCode(){
		return restaurantId * 31 + 17;
	}
	
	@Override
	public String toString(){
		return "restaurantId = " + this.restaurantId + 
			   ", session size = " + this.sessions.size();
	}
}
