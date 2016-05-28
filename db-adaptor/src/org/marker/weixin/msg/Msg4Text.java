package org.marker.weixin.msg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Msg4Text extends Msg {
	private String content;
	private String msgId;

	public Msg4Text(Msg received) {
		super(received, Msg4Head.MsgType.MSG_TYPE_TEXT);
	}

	public Msg4Text(Msg received, String content){
		super(received, Msg4Head.MsgType.MSG_TYPE_TEXT);
		setContent(content);
	}
	
	public Msg4Text(Msg4Head head) {
		super(head, Msg4Head.MsgType.MSG_TYPE_TEXT);
	}
	
	public Msg4Text(Msg4Head head, String content) {
		super(head, Msg4Head.MsgType.MSG_TYPE_TEXT);
		setContent(content);
	}

	public void write(Document document) {
		Element root = document.createElement("xml");
		this.head.write(root, document);
		Element contentElement = document.createElement("Content");
		contentElement.setTextContent(this.content);
		root.appendChild(contentElement);
		document.appendChild(root);
	}

	public void read(Document document) {
		this.content = document.getElementsByTagName("Content").item(0).getTextContent();
		Node note = document.getElementsByTagName("MsgId").item(0);
		if(note != null){
			this.msgId = note.getTextContent();
		}
	}

	public String getContent() {
		return this.content;
	}

	public Msg4Text setContent(String content) {
		this.content = content;
		return this;
	}

	public String getMsgId() {
		return this.msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonMap(super.toJsonMap(flag));
		jm.putJsonable("text", new Jsonable(){

			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putString("content", content);
				return jm;
			}

			@Override
			public void fromJsonMap(JsonMap jsonMap, int flag) {
				
			}
			
		}, 0);
		
		return jm;
	}

}
