package org.marker.weixin.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.marker.weixin.HandleMessageListener;
import org.marker.weixin.api.Token;
import org.marker.weixin.auth.AuthorizerToken;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Head;
import org.marker.weixin.msg.Msg4Image;
import org.marker.weixin.msg.Msg4Link;
import org.marker.weixin.msg.Msg4Location;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.msg.Msg4Video;
import org.marker.weixin.msg.Msg4Voice;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class WxSession {
	private final List<HandleMessageListener> listeners = new ArrayList<>(3);
	private InputStream is;
	private OutputStream os;
	private static DocumentBuilder builder;

	static {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private static TransformerFactory tffactory = TransformerFactory.newInstance();

	public static WxSession newInstance(){ 
		return new WxSession();
	}
	
	public void process(InputStream is, OutputStream os) {
		this.os = os;
		this.is = is;
		try {
			Document document = builder.parse(this.is);
			Msg4Head head = new Msg4Head();
			head.read(document);
			if (head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_TEXT) {
				Msg4Text msg = new Msg4Text(head);
				msg.read(document);
				onTextMsg(msg);
			} else if (head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_IMAGE) {
				Msg4Image msg = new Msg4Image(head);
				msg.read(document);
				onImageMsg(msg);
			} else if (head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_EVENT) {
				Msg4Event msg = new Msg4Event(head);
				msg.read(document);
				onEventMsg(msg);
			} else if (head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_LINK) {
				Msg4Link msg = new Msg4Link(head);
				msg.read(document);
				onLinkMsg(msg);
			} else if (head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_LOCATION) {
				Msg4Location msg = new Msg4Location(head);
				msg.read(document);
				onLocationMsg(msg);
			} else if (head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_VOICE) {
				Msg4Voice msg = new Msg4Voice(head);
				msg.read(document);
				onVoiceMsg(msg);
			} else if (head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_VIDEO) {
				Msg4Video msg = new Msg4Video(head);
				msg.read(document);
				onVideoMsg(msg);
			} else {
				onErrorMsg(-1);
			}
		} catch (SAXException | IOException e) {
			e.printStackTrace();
		}
	}

	public void callback(Msg msg) {
		Document document = builder.newDocument();
		msg.write(document);
		try {
			Transformer transformer = tffactory.newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(new OutputStreamWriter(this.os, "utf-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void callback(AuthorizerToken token, Callable<Msg> callable){
		try {
			this.os.write(0);
			this.os.flush();
			callable.call().send(token);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void callback(Token token, Callable<Msg> callable){
		try {
			this.os.write(0);
			this.os.flush();
			callable.call().send(token);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			if (this.is != null) {
				this.is.close();
			}
			if (this.os != null) {
				this.os.flush();
				this.os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addOnHandleMessageListener(HandleMessageListener handleMassge) {
		this.listeners.add(handleMassge);
	}

	public void removeOnHandleMessageListener(HandleMessageListener handleMassge) {
		this.listeners.remove(handleMassge);
	}

	public void onTextMsg(Msg4Text msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onTextMsg(msg);
		}
	}

	public void onImageMsg(Msg4Image msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onImageMsg(msg);
		}
	}

	public void onEventMsg(Msg4Event msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onEventMsg(msg);
		}
	}

	public void onLinkMsg(Msg4Link msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onLinkMsg(msg);
		}
	}

	public void onLocationMsg(Msg4Location msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onLocationMsg(msg);
		}
	}

	public void onErrorMsg(int errorCode) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onErrorMsg(errorCode);
		}
	}

	public void onVoiceMsg(Msg4Voice msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onVoiceMsg(msg);
		}
	}

	public void onVideoMsg(Msg4Video msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onVideoMsg(msg);
		}
	}
}
