package com.wireless.db.weixin.menuAction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Head;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4Text;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.wireless.exception.BusinessException;

public class WxMenuAction {

	public static class Msg4Action extends Msg{
		
		private Msg msg;
		
		public Msg4Action(Msg4Head header, WxMenuAction menuAction) throws SAXException, IOException, ParserConfigurationException{
			super(header);
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(menuAction.getAction().getBytes("utf-8")));
			Msg4Head head = new Msg4Head();
			head.read(document);
			head.setFromUserName(header.getToUserName());
			head.setToUserName(header.getFromUserName());
			if (head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_TEXT) {
				msg = new Msg4Text(head);
				msg.read(document);
			}
		}

		@Override
		public void write(Document document) {
			msg.write(document);
		}

		@Override
		public void read(Document document) {
			throw new UnsupportedOperationException();
		}
	}
	
	public static class InsertBuilder4Text{
		private final InsertBuilder builder;
		public InsertBuilder4Text(String text){
			builder = new InsertBuilder(MsgType.MSG_TYPE_TEXT, new Msg4Text(InsertBuilder.head4Insert, text));
		}
		
		public WxMenuAction build() throws BusinessException{
			return builder.build();
		}
	}
	
	private static class InsertBuilder{

		private static final Msg4Head head4Insert = new Msg4Head(){
			@Override
			public String getToUserName() {
				return "$(to_user)";
			}

			@Override
			public String getFromUserName() {
				return "$(from_user)";
			}
		};
		
		private final Msg4Head.MsgType msgType;
		private final Msg action;
		
		InsertBuilder(Msg4Head.MsgType msgType, Msg action){
			this.msgType = msgType;
			this.action = action;
		}
		
		public WxMenuAction build() throws BusinessException{
			return new WxMenuAction(this);
		}
	}
	
	public static class UpdateBuilder4Text{
		private final UpdateBuilder builder;
		public UpdateBuilder4Text(int id, String text){
			builder = new UpdateBuilder(id, MsgType.MSG_TYPE_TEXT, new Msg4Text(InsertBuilder.head4Insert, text));
		}
		
		public WxMenuAction build() throws BusinessException{
			return builder.build();
		}
	}
	
	private static class UpdateBuilder{
		private final int id;
		private final MsgType msgType;
		private final Msg action;
		public UpdateBuilder(int id, MsgType msgType, Msg action){
			this.id = id;
			this.msgType = msgType;
			this.action = action;
		}
		
		public WxMenuAction build() throws BusinessException{
			return new WxMenuAction(this);
		}
	}
	
	private int id;
	private int restaurantId;
	private Msg4Head.MsgType msgType;
	private String action;
	
	private WxMenuAction(UpdateBuilder builder) throws BusinessException{
		setId(builder.id);
		setMsgType(builder.msgType);
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			builder.action.write(document);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter sw = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(sw)); 
			setAction(sw.toString());
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}
	
	private WxMenuAction(InsertBuilder builder) throws BusinessException{
		setMsgType(builder.msgType);
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			builder.action.write(document);
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StringWriter sw = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(sw)); 
			setAction(sw.toString());
		} catch (Exception e) {
			throw new BusinessException(e.getMessage());
		}
	}
	
	public WxMenuAction(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public Msg4Head.MsgType getMsgType() {
		return msgType;
	}
	
	public void setMsgType(Msg4Head.MsgType msgType) {
		this.msgType = msgType;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
}
