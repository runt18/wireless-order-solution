package org.marker.weixin.msg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Msg4Video extends Msg {
	private String mediaId;
	private String thumbMediaId;
	private String msgId;

	public Msg4Video(Msg received) {
		super(received, Msg4Head.MsgType.MSG_TYPE_VIDEO);
	}

	public Msg4Video(Msg4Head head) {
		super(head);
	}

	public void write(Document document) {
		Element root = document.createElement("xml");
		this.head.write(root, document);
		Element videoElement = document.createElement("Video");
		Element mediaIdElement = document.createElement("MediaId");
		Element thumbMediaIdElement = document.createElement("ThumbMediaId");
		videoElement.appendChild(mediaIdElement);
		videoElement.appendChild(thumbMediaIdElement);
		root.appendChild(videoElement);
		document.appendChild(root);
	}

	public void read(Document document) {
		this.mediaId = getElementContent(document, "MediaId");
		this.thumbMediaId = getElementContent(document, "ThumbMediaId");
		this.msgId = getElementContent(document, "MsgId");
	}

	public String getMediaId() {
		return this.mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String getThumbMediaId() {
		return this.thumbMediaId;
	}

	public void setThumbMediaId(String thumbMediaId) {
		this.thumbMediaId = thumbMediaId;
	}

	public String getMsgId() {
		return this.msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
}
