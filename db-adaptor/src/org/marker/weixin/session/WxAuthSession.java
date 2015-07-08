package org.marker.weixin.session;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.marker.weixin.msg.Msg;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;

public class WxAuthSession extends WxSession{

	private class EncryptedMsg extends Msg{
		
		private final Msg plainMsg;
		
		EncryptedMsg(Msg plainMsg) {
			super(plainMsg, plainMsg.getHead().getMsgType());
			this.plainMsg = plainMsg;
		}

		@Override
		public void write(Document document) {
			try {
				//Read the document to plain msg.
				Document document4Plain = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				plainMsg.write(document4Plain); 
				
				//Encrypted the plain document msg.
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				TransformerFactory.newInstance().newTransformer().transform(new DOMSource(document4Plain), result);
				WXBizMsgCrypt pc = new WXBizMsgCrypt(WxAuthSession.this.token, WxAuthSession.this.encodingAesKey, WxAuthSession.this.appId);
				String encryptedContent = pc.encryptMsg(writer.toString(), WxAuthSession.this.timestamp, WxAuthSession.this.nonce);
				
//				System.out.println("encrypted : " + encryptedContent);
				
				//Copy the encrypted document to result.
		        Document encryptedDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(encryptedContent.getBytes()));
		        Node copiedRoot = document.importNode(encryptedDocument.getDocumentElement(), true);
		        document.appendChild(copiedRoot);
		        
			} catch (ParserConfigurationException | TransformerException | TransformerFactoryConfigurationError | AesException | SAXException | IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void read(Document document) {
			throw new UnsupportedOperationException();
		}
	}
	
	private final String timestamp;
	private final String nonce;
	private final String msgSignature;
	private final String token;
	private final String encodingAesKey;
	private final String appId;
	
	public WxAuthSession(String timestamp, String nonce, String msgSignature, String token, String encodingAesKey, String appId){
		this.timestamp = timestamp;
		this.nonce = nonce;
		this.msgSignature = msgSignature;
		this.token = token;
		this.encodingAesKey = encodingAesKey;
		this.appId = appId;
	}
	
	@Override
	public void process(InputStream is, OutputStream os) {
		try{
			final Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
			final String encrypt = document.getDocumentElement().getElementsByTagName("Encrypt").item(0).getTextContent();
			
			final String format = "<xml><ToUserName><![CDATA[toUser]]></ToUserName><Encrypt><![CDATA[%1$s]]></Encrypt></xml>";
			final String fromXML = String.format(format, encrypt);
			
			final WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
			final String decryptedMsg = pc.decryptMsg(msgSignature, timestamp, nonce, fromXML);
			
			//System.out.println("decrypted : " + decryptedMsg);
			
			super.process(new ByteArrayInputStream(decryptedMsg.getBytes()), os);
			
		}catch(ParserConfigurationException | SAXException | IOException | AesException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void callback(Msg msg) {
		super.callback(new EncryptedMsg(msg));
	}
}
