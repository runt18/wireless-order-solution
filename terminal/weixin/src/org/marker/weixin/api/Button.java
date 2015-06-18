package org.marker.weixin.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Button implements Jsonable{

	public static class ViewBuilder extends ButtonBuilder{
		public ViewBuilder(String name, String url){
			super(name);
			super.type = Type.VIEW;
			super.url = url;
		}
	}

	public static class ClickBuilder extends ButtonBuilder {

		public ClickBuilder(String name, String key) {
			super(name);
			super.key = key;
			super.type = Type.CLICK;
		}
		
	}
	
	public static class ScanPushBuilder extends ButtonBuilder {
		public ScanPushBuilder(String name, String key) {
			super(name);
			super.key = key;
			super.type = Type.SCAN_PUSH;
		}
		
	}
	
	public static class ScanMsgBuilder extends ButtonBuilder {
		public ScanMsgBuilder(String name, String key) {
			super(name);
			super.key = key;
			super.type = Type.SCAN_MSG;
		}
		
	}	

	public abstract static class ButtonBuilder{
		private final String name;
		private Type type;
		private String key;
		private String url;
		
		private final List<Button> children = new ArrayList<Button>(3);
		
		ButtonBuilder(String name){
			this.name = name;
		}
		
		
		public ButtonBuilder addChild(ButtonBuilder builder){
			if(children.size() > 5){
				throw new IllegalStateException("一级菜单最多包含5个二级菜单");
			}else{
				children.add(builder.build());
			}
			return this;
		}
		
		public Button build(){
			return new Button(this);
		}
	}
	
	public static enum Type {
		CLICK("click"),
		VIEW("view"),
		SCAN_PUSH("scancode_push"),
		SCAN_MSG("scancode_waitmsg");
		private final String val;

		Type(String val) {
			this.val = val;
		}

		@Override
		public String toString() {
			return val;
		}

	}

	private Type type; // click|view
	private String name;
	private String key;
	private String url;

	private final List<Button> children = new ArrayList<Button>();

	private Button(){}
	
	private Button(ButtonBuilder builder){
		this.type = builder.type;
		this.name = builder.name;
		this.url = builder.url;
		this.key = builder.key;
		this.children.addAll(builder.children);
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Button> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public Button addChild(Button sub){
		if(children.size() > 5){
			throw new IllegalStateException("一级菜单最多包含5个二级菜单");
		}else{
			children.add(sub);
		}
		return this;
	}

	static enum Key4Json{
		BUTTON("button"),
		TYPE("type"),
		NAME("name"),
		KEY("key"),
		SUB_BUTTON("sub_button"),
		URL("url");
		
		final String key;
		Key4Json(String key) {
			this.key = key;
		}
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString(Key4Json.NAME.key, this.name);
		jm.putString(Key4Json.TYPE.key, this.type.val);
		if(type == Type.CLICK || type == Type.SCAN_PUSH || type == Type.SCAN_MSG){
			jm.putString(Key4Json.KEY.key, this.key);
		}else if(type == Type.VIEW){
			jm.putString(Key4Json.URL.key, this.url);
		}
		
		if(!this.children.isEmpty()){
			jm.putJsonableList(Key4Json.SUB_BUTTON.key, this.children, 0);
		}
		
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		//TODO
	}
}
