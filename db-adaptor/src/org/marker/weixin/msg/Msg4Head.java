package org.marker.weixin.msg;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Msg4Head {
	
	public static enum MsgType{
		MSG_TYPE_UNKNOWN(1, "unknown"),
		MSG_TYPE_TEXT(2, "text"),
		MSG_TYPE_IMAGE(3, "image"),
		MSG_TYPE_MUSIC(4, "music"),
		MSG_TYPE_LOCATION(5, "location"),
		MSG_TYPE_LINK(6, "link"),
		MSG_TYPE_IMAGE_TEXT(7, "news"),
		MSG_TYPE_EVENT(8, "event"),
		MSG_TYPE_VOICE(9, "voice"),
		MSG_TYPE_VIDEO(10, "video");
		
		private final String val;
		private final int type;
		
		MsgType(int type, String val){
			this.type = type;
			this.val = val;
		}
		
		public int getType(){
			return this.type;
		}
		
		public String getVal(){
			return this.val;
		}
		
		public static MsgType valueOf(String val, int flag){
			for(MsgType type : values()){
				if(type.val.equalsIgnoreCase(val)){
					return type;
				}
			}
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
		
		public static MsgType valueOf(int typeVal){
			for(MsgType type : values()){
				if(type.type == typeVal){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(" + typeVal + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.val;
		}
	}
	
	private String toUserName;
	private String fromUserName;
	private String createTime;
	private MsgType msgType;

	public Msg4Head() {
		this.createTime = new SimpleDateFormat("yyyyMMdd").format(new Date());
	}

	public void write(Element root, Document document) {
		Element toUserNameElement = document.createElement("ToUserName");
		toUserNameElement.setTextContent(this.toUserName);
		Element fromUserNameElement = document.createElement("FromUserName");
		fromUserNameElement.setTextContent(this.fromUserName);
		Element createTimeElement = document.createElement("CreateTime");
		createTimeElement.setTextContent(this.createTime);
		Element msgTypeElement = document.createElement("MsgType");
		msgTypeElement.setTextContent(this.msgType.val);

		root.appendChild(toUserNameElement);
		root.appendChild(fromUserNameElement);
		root.appendChild(createTimeElement);
		root.appendChild(msgTypeElement);
	}

	public void read(Document document) {
		this.toUserName = document.getElementsByTagName("ToUserName").item(0).getTextContent();
		this.fromUserName = document.getElementsByTagName("FromUserName").item(0).getTextContent();
		this.createTime = document.getElementsByTagName("CreateTime").item(0).getTextContent();
		this.msgType = MsgType.valueOf(document.getElementsByTagName("MsgType").item(0).getTextContent(), 0);
	}

	public String getToUserName() {
		if(this.toUserName == null){
			return "";
		}
		return this.toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public String getFromUserName() {
		if(this.fromUserName == null){
			return "";
		}
		return this.fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}

	public String getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public MsgType getMsgType() {
		return this.msgType;
	}

	public void setMsgType(MsgType msgType) {
		this.msgType = msgType;
	}
}
