package org.marker.weixin.msg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Msg4Image extends Msg {
	private String picUrl;
	private String msgId;
	private String mediaId;

	public Msg4Image(Msg received) {
		super(received, Msg4Head.MsgType.MSG_TYPE_IMAGE);
	}

	public Msg4Image(Msg4Head head) {
		super(head);
	}

	public void write(Document document) {
		Element root = document.createElement("xml");
		this.head.write(root, document);
		Element imageElement = document.createElement("Image");
		Element mediaIdElement = document.createElement("MediaId");
		imageElement.appendChild(mediaIdElement);
		root.appendChild(imageElement);
		document.appendChild(root);
	}

	public void read(Document document) {
		this.picUrl = document.getElementsByTagName("PicUrl").item(0).getTextContent();
		this.mediaId = getElementContent(document, "MediaId");
		this.msgId = document.getElementsByTagName("MsgId").item(0).getTextContent();
	}

	public String getPicUrl() {
		return this.picUrl;
	}

	public Msg4Image setPicUrl(String picUrl) {
		this.picUrl = picUrl;
		return this;
	}

	public String getMsgId() {
		return this.msgId;
	}

	public Msg4Image setMsgId(String msgId) {
		this.msgId = msgId;
		return this;
	}

	public String getMediaId() {
		return this.mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}
}
