package com.wireless.pojo.staffMgr;

import java.util.Locale;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;


public class Device implements Parcelable{

	public final static int DEVICE_PARCELABLE_SIMPLE = 0;
	public final static int DEVICE_PARCELABLE_COMPLEX = 1;
	
	public static enum Model{
		ANDROID(1, "Android"),
		iOS(2, "iOS"),
		WP(3, "WindowsPhone");
		
		private final int val;
		private final String desc;
		
		Model(int val, String desc){
			this.val = val;
			this.desc = desc;
		}

		@Override
		public String toString(){
			return desc;
		}
		
		public static Model valueOf(int val){
			for(Model model : values()){
				if(model.val == val){
					return model;
				}
			}
			throw new IllegalArgumentException("The model(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	public static enum Status{
		IDLE(1, "空闲"),
		WORK(2, "使用");
		
		private final int val;
		private final String desc;
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return desc;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	public static class InsertBuilder{
		
		private final int restaurantId;
		private Model model = Model.ANDROID;
		private final String deviceId;
		
		public InsertBuilder(String deviceId, int restaurantId){
			this.deviceId = deviceId;
			this.restaurantId = restaurantId;
		}
		
		public InsertBuilder setModel(Model model){
			this.model = model;
			return this;
		}
		
		public Device build(){
			return new Device(this);
		}
	}
	
	public static class UpdateBuilder{
		
		private final int id;
		private final String deviceId;
		private final int restaurantId;
		private Model model;
		private Status status;
		
		UpdateBuilder(int id, String deviceId, int restaurantId){
			this.id = id;
			this.deviceId = deviceId;
			this.restaurantId = restaurantId;
		}
		
		public int getId(){
			return this.id;
		}
		
		public String getDeviceId(){
			return this.deviceId.trim().toUpperCase(Locale.getDefault());
		}
		
		public int getRestaurantId(){
			return this.restaurantId;
		}
		
		public UpdateBuilder setModel(Model model){
			this.model = model;
			return this;
		}
		
		public Model getModel(){
			return this.model;
		}
		
		public UpdateBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public Status getStatus(){
			return this.status;
		}
		
	}
	
	private int id;
	private int restaurantId;
	private String deviceId;
	private Model model = Model.ANDROID;
	private Status status = Status.IDLE;
	
	private Device(InsertBuilder builder){
		setRestaurantId(builder.restaurantId);
		setDeviceId(builder.deviceId);
		setModel(builder.model);
		setStatus(Status.WORK);
	}
	
	public Device(String deviceId){
		setDeviceId(deviceId);
	}
	
	Device(){
		
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
	
	public String getDeviceId() {
		if(deviceId == null){
			return "";
		}
		return deviceId.trim().toUpperCase(Locale.getDefault());
	}
	
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setModel(Model model) {
		if(model != null){
			this.model = model;
		}
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		if(status != null){
			this.status = status;
		}
	}
	
	@Override
	public String toString(){
		return getModel().desc + ":" + getDeviceId();
	}
	
	@Override
	public int hashCode(){
		return getDeviceId().hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Device)){
			return false;
		}else{
			return getDeviceId().equals(((Device)obj).getDeviceId());
		}
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == DEVICE_PARCELABLE_SIMPLE){
			dest.writeString(getDeviceId());
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == DEVICE_PARCELABLE_SIMPLE){
			setDeviceId(source.readString());
		}
	}
	
	public final static Parcelable.Creator<Device> CREATOR = new Parcelable.Creator<Device>(){

		@Override
		public Device newInstance() {
			return new Device();
		}
		
		@Override
		public Device[] newInstance(int size){
			return new Device[size];
		}
		
	};
}
