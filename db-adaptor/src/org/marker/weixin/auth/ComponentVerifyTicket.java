package org.marker.weixin.auth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.alibaba.fastjson.JSONObject;
import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class ComponentVerifyTicket implements Jsonable{

	private String ticket;
	private String infoType;
	private String appId;
	
	public static ComponentVerifyTicket newInstance(InputStream in, String timestamp, String nonce, String msgSignature) throws AesException, SAXException, IOException, ParserConfigurationException{
		
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
		
		String encrypt = document.getDocumentElement().getElementsByTagName("Encrypt").item(0).getTextContent();

		String format = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><Encrypt><![CDATA[%1$s]]></Encrypt></xml>";
		String fromXML = String.format(format, encrypt);
		
		WXBizMsgCrypt pc = new WXBizMsgCrypt(AuthParam.TOKEN, AuthParam.ENCRYPT_AES_KEY, AuthParam.APP_ID);
		String decryptedMsg = pc.decryptMsg(msgSignature, timestamp, nonce, fromXML);
		
		document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(decryptedMsg.getBytes()));
		
		ComponentVerifyTicket ticket = new ComponentVerifyTicket();
		ticket.ticket = document.getDocumentElement().getElementsByTagName("ComponentVerifyTicket").item(0).getTextContent();
		ticket.infoType = document.getDocumentElement().getElementsByTagName("InfoType").item(0).getTextContent();
		ticket.appId = document.getDocumentElement().getElementsByTagName("AppId").item(0).getTextContent();
		
		return ticket;
	}
	
	public String getTicket(){
		return this.ticket;
	}
	
	public String getInfoType(){
		return this.infoType;
	}
	
	public String getAppId(){
		return this.appId;
	}
	
	@Override
	public String toString(){
		return JSONObject.toJSONString(this.toJsonMap(0));
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("ticket", ticket);
		jm.putString("appId", appId);
		jm.putString("infoType", infoType);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		this.ticket = jsonMap.getString("ticket");
		this.appId = jsonMap.getString("appId");
		this.infoType = jsonMap.getString("infoType");
	}
	
	public static Jsonable.Creator<ComponentVerifyTicket> JSON_CREATOR = new Jsonable.Creator<ComponentVerifyTicket>() {
		@Override
		public ComponentVerifyTicket newInstance() {
			return new ComponentVerifyTicket();
		}
	};
	
}
