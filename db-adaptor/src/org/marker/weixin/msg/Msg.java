package org.marker.weixin.msg;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.marker.weixin.api.BaseAPI;
import org.marker.weixin.api.Status;
import org.marker.weixin.api.Token;
import org.marker.weixin.auth.AuthorizerToken;
import org.w3c.dom.Document;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public abstract class Msg implements Jsonable{
	
	
	protected Msg(Msg4Head head){
		this.head = head;
	}
	
	protected Msg(Msg4Head head, Msg4Head.MsgType msgType){
		this(head);
		this.head.setMsgType(msgType);
	}
	
	protected Msg(Msg received, Msg4Head.MsgType msgType){
		this.head = new Msg4Head();
		this.head.setFromUserName(received.getToUserName());
		this.head.setToUserName(received.getFromUserName());
		this.head.setMsgType(msgType);
	}
	
	protected Msg4Head head;

	public abstract void write(Document paramDocument);

	public abstract void read(Document paramDocument);

	protected String getElementContent(Document document, String element) {
		if (document.getElementsByTagName(element).getLength() > 0) {
			return document.getElementsByTagName(element).item(0).getTextContent();
		}
		return null;
	}

	public Status send(AuthorizerToken token) throws ClientProtocolException, IOException{
		return BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/message/custom/send?access_token=" + token.getAccessToken(), this);
	}
	
	public Status send(Token token) throws ClientProtocolException, IOException{
		return BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/message/custom/send?access_token=" + token.getAccessToken(), this);
	}
	
	public Msg4Head getHead() {
		return this.head;
	}

	void setHead(Msg4Head head) {
		this.head = head;
	}

	public String getToUserName() {
		return this.head.getToUserName();
	}

	void setToUserName(String toUserName) {
		this.head.setToUserName(toUserName);
	}

	public String getFromUserName() {
		return this.head.getFromUserName();
	}

	void setFromUserName(String fromUserName) {
		this.head.setFromUserName(fromUserName);
	}

	public String getCreateTime() {
		return this.head.getCreateTime();
	}

	void setCreateTime(String createTime) {
		this.head.setCreateTime(createTime);
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("touser", this.head.getToUserName());
		jm.putString("msgtype", this.head.getMsgType().getVal());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
