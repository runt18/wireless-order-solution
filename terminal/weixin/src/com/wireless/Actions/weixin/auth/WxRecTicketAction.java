package com.wireless.Actions.weixin.auth;

import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.maker.weixin.auth.ComponentAccessToken;
import org.maker.weixin.auth.ComponentVerifyTicket;
import org.maker.weixin.auth.PreAuthCode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.qq.weixin.mp.aes.WXBizMsgCrypt;

public class WxRecTicketAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String timestamp = request.getParameter("timestamp");
		final String nonce = request.getParameter("nonce");
		final String msgSignature = request.getParameter("msg_signature");
		
//	    BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
//	    StringBuilder buffer = new StringBuilder();
//	    String line;
//	    while ((line = in.readLine()) != null){
//	      buffer.append(line);
//	    }
//	    buffer.toString();
//		
//	    System.out.println(buffer.toString());
//	    System.out.println("------------------------------------------------------------");
	    
	    ComponentVerifyTicket ticket = ComponentVerifyTicket.newInstance(request.getInputStream(), timestamp, nonce, msgSignature);
	    AuthParam.COMPONENT_VERIFY_TICKET = ticket;

		File logFile = new File("/home/yzhang/www/wx-term/ticket.txt");
		if(!logFile.exists()){
			logFile.createNewFile();
		}
		FileWriter logWriter = new FileWriter(logFile, false);
		logWriter.write(ticket.toString());
		logWriter.close();
		
	    ComponentAccessToken accessToken = ComponentAccessToken.newInstance(ticket);
	    AuthParam.COMPONENT_ACCESS_TOKEN = accessToken;
	    //System.out.println(AuthParam.COMPONENT_ACCESS_TOKEN);
	    
	    AuthParam.PRE_AUTH_CODE = PreAuthCode.newInstance(accessToken);
	    //System.out.println(AuthParam.PRE_AUTH_CODE);
	    
	    response.getWriter().print("success");
	    
		return null;
	}
	
	public static void main(String[] args) throws Exception {

		//
		// 第三方回复公众平台
		//

		// 需要加密的明文
		String encodingAesKey = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFG";
		String token = "pamtest";
		String timestamp = "1409304348";
		String nonce = "xxxxxx";
		String appId = "wxb11529c136998cb6";
		String replyMsg = " 中文<xml><ToUserName><![CDATA[oia2TjjewbmiOUlr6X-1crbLOvLw]]></ToUserName><FromUserName><![CDATA[gh_7f083739789a]]></FromUserName><CreateTime>1407743423</CreateTime><MsgType><![CDATA[video]]></MsgType><Video><MediaId><![CDATA[eYJ1MbwPRJtOvIEabaxHs7TX2D-HV71s79GUxqdUkjm6Gs2Ed1KF3ulAOA9H1xG0]]></MediaId><Title><![CDATA[testCallBackReplyVideo]]></Title><Description><![CDATA[testCallBackReplyVideo]]></Description></Video></xml>";

		WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
		String mingwen = pc.encryptMsg(replyMsg, timestamp, nonce);
		System.out.println("加密后: " + mingwen);

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		StringReader sr = new StringReader(mingwen);
		InputSource is = new InputSource(sr);
		Document document = db.parse(is);

		Element root = document.getDocumentElement();
		NodeList nodelist1 = root.getElementsByTagName("Encrypt");
		NodeList nodelist2 = root.getElementsByTagName("MsgSignature");

		String encrypt = nodelist1.item(0).getTextContent();
		String msgSignature = nodelist2.item(0).getTextContent();

		String format = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><Encrypt><![CDATA[%1$s]]></Encrypt></xml>";
		String fromXML = String.format(format, encrypt);

		//
		// 公众平台发送消息给第三方，第三方处理
		//

		// 第三方收到公众号平台发送的消息
		String result2 = pc.decryptMsg(msgSignature, timestamp, nonce, fromXML);
		System.out.println("解密后明文: " + result2);
		
		//pc.verifyUrl(null, null, null, null);
	}
}
