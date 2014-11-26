package org.marker.weixin.api;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Status implements Jsonable{

	private int code;
	private String desc;

	public Status(int code, String desc){
		this.code = code;
		this.desc = desc;
	}
	
	public int getCode(){
		return this.code;
	}
	
	public String getDesc(){
		return this.desc;
	}
	
	public boolean isOk(){
		return code == 0;
	}
	
	@Override
	public int hashCode(){
		return code * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Status)){
			return false;
		}else{
			return code == ((Status)obj).code;
		}
	}
	
	@Override
	public String toString(){
		return "errCode = " + code + ",errMsg = " + desc;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		return null;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		this.code = jsonMap.getInt("errcode");
		this.desc = jsonMap.getString("errmsg");
	}
	
	public static Jsonable.Creator<Status> JSON_CREATOR = new Jsonable.Creator<Status>() {
		@Override
		public Status newInstance() {
			return new Status(0, "");
		}
	};

}
