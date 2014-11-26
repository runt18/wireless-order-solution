package org.marker.weixin.msg;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Msg4Head {
	
	public static enum MsgType{
		MSG_TYPE_UNKNOWN("unknown"),
		MSG_TYPE_TEXT("text"),
		MSG_TYPE_IMAGE("image"),
		MSG_TYPE_MUSIC("music"),
		MSG_TYPE_LOCATION("location"),
		MSG_TYPE_LINK("link"),
		MSG_TYPE_IMAGE_TEXT("news"),
		MSG_TYPE_EVENT("event"),
		MSG_TYPE_VOICE("voice"),
		MSG_TYPE_VIDEO("video");
		
		private final String val;
		
		MsgType(String val){
			this.val = val;
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
		return this.toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}

	public String getFromUserName() {
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
