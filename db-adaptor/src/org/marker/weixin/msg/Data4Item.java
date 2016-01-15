package org.marker.weixin.msg;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Data4Item implements Jsonable{
	private String title;
	private String description;
	private String picUrl;
	private String url;

	public Data4Item() {
	}

	public Data4Item(String title, String description, String picUrl, String url) {
		this.title = title;
		this.description = description;
		this.picUrl = picUrl;
		this.url = url;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPicUrl() {
		return this.picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getUrl() {
		if(this.url == null){
			return "";
		}
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean hasUrl(){
		return getUrl().length() != 0;
	}
	
	@Override
	public String toString(){
		return "title = " + getTitle() +
			   (this.description != null ? ", desc = " + this.description : "") +
			   (this.picUrl != null ? ", picUrl = " + this.picUrl : "") +
			   (this.url != null ? ", url = " + this.url : "");
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("title", title);
		jm.putString("description", description);
		jm.putString("picUrl", getPicUrl());
		jm.putString("url", url);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
}
