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

import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Head;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.wireless.exception.BusinessException;

public class WxMenuAction {

	public static class MsgProxy{
		
		private Msg msg;
		
		public MsgProxy(WxMenuAction menuAction) throws SAXException, IOException, ParserConfigurationException{
			this(new Msg4Head(), menuAction);
		}
		
		public MsgProxy(Msg4Head header, WxMenuAction menuAction) throws SAXException, IOException, ParserConfigurationException{
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(menuAction.getAction().getBytes("utf-8")));
			Msg4Head head = new Msg4Head();
			head.read(document);
			head.setFromUserName(header.getToUserName());
			head.setToUserName(header.getFromUserName());
			if (head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_TEXT) {
				msg = new Msg4Text(head);
				msg.read(document);
			}else if(head.getMsgType() == Msg4Head.MsgType.MSG_TYPE_IMAGE_TEXT){
				msg = new Msg4ImageText(head);
				msg.read(document);
			}
		}

		public Msg toMsg(){
			return this.msg;
		}
	}
	
	public static class InsertBuilder4ImageText{
		private final InsertBuilder builder;
		private final Msg4ImageText msg = new Msg4ImageText(InsertBuilder.head4Insert);

		public InsertBuilder4ImageText(Data4Item item){
			msg.addItem(item);
			builder = new InsertBuilder(MsgType.MSG_TYPE_IMAGE_TEXT, msg, Cate.NORMAL);
		}
		
		public InsertBuilder4ImageText(Data4Item item, Cate cate){
			msg.addItem(item);
			builder = new InsertBuilder(MsgType.MSG_TYPE_IMAGE_TEXT, msg, cate);
		}
		
		public InsertBuilder4ImageText addItem(Data4Item item){
			msg.addItem(item);
			return this;
		}
		
		public WxMenuAction build() throws BusinessException{
			return builder.build();
		}
	}
	
	public static class InsertBuilder4Text{
		private final InsertBuilder builder;
		
		public InsertBuilder4Text(String text){
			builder = new InsertBuilder(MsgType.MSG_TYPE_TEXT, new Msg4Text(InsertBuilder.head4Insert, text), Cate.NORMAL);
		}
		
		public InsertBuilder4Text(String text, Cate cate){
			builder = new InsertBuilder(MsgType.MSG_TYPE_TEXT, new Msg4Text(InsertBuilder.head4Insert, text), cate);
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
		private final Cate cate;
		
		InsertBuilder(Msg4Head.MsgType msgType, Msg action, Cate cate){
			this.msgType = msgType;
			this.action = action;
			this.cate = cate;
		}
		
		public WxMenuAction build() throws BusinessException{
			return new WxMenuAction(this);
		}
	}
	
	public static class UpdateBuilder4ImageText{
		private final UpdateBuilder builder;
		private final Msg4ImageText msg = new Msg4ImageText(InsertBuilder.head4Insert);
		public UpdateBuilder4ImageText(int id, Data4Item item){
			msg.addItem(item);
			builder = new UpdateBuilder(id, MsgType.MSG_TYPE_IMAGE_TEXT, msg);
		}
		
		public UpdateBuilder4ImageText addItem(Data4Item item){
			this.msg.addItem(item);
			return this;
		}
		
		public UpdateBuilder4ImageText setCate(Cate cate){
			this.builder.setCate(cate);
			return this;
		}
		
		public UpdateBuilder builder() throws BusinessException{
			return builder;
		}
	}
	
	public static class UpdateBuilder4Text{
		private final UpdateBuilder builder;
		public UpdateBuilder4Text(int id, String text){
			builder = new UpdateBuilder(id, MsgType.MSG_TYPE_TEXT, new Msg4Text(InsertBuilder.head4Insert, text));
		}
		
		public UpdateBuilder4Text setCate(Cate cate){
			this.builder.setCate(cate);
			return this;
		}
		
		public UpdateBuilder builder() throws BusinessException{
			return builder;
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private final MsgType msgType;
		private final Msg action;
		private Cate cate;
		public UpdateBuilder(int id, MsgType msgType, Msg action){
			this.id = id;
			this.msgType = msgType;
			this.action = action;
		}

		public UpdateBuilder setCate(Cate cate){
			this.cate = cate;
			return this;
		}
		
		public boolean isCateChanged(){
			return this.cate != null;
		}
		
		public WxMenuAction build() throws BusinessException{
			return new WxMenuAction(this);
		}
	}
	
	public static enum Cate{
		NORMAL(1, "普通"),
		SUBSCRIBE_REPLY(2, "关注回复");
		
		private final int val;
		private final String desc;
		
		Cate(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return val;
		}
		
		public static Cate valueOf(int val){
			for(Cate cate : values()){
				if(cate.val == val){
					return cate;
				}
			}
			throw new IllegalArgumentException("The (val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private int id;
	private int restaurantId;
	private Msg4Head.MsgType msgType;
	private String action;
	private Cate cate;
	
	private WxMenuAction(UpdateBuilder builder) throws BusinessException{
		setId(builder.id);
		setMsgType(builder.msgType);
		setCate(builder.cate);
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
		setCate(builder.cate);
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
	
	public void setCate(Cate cate){
		this.cate = cate;
	}
	
	public Cate getCate(){
		return this.cate;
	}
}
