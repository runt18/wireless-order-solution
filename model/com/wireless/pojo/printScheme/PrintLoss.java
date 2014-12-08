package com.wireless.pojo.printScheme;

import java.io.UnsupportedEncodingException;

public class PrintLoss {

	public static class InsertBuilder{
		private final byte[] content;
		
		public InsertBuilder(byte[] content){
			this.content = content;
		}
		
		public PrintLoss build(){
			return new PrintLoss(this);
		}
	}
	
	private int id;
	private int restaurantId;
	private byte[] content;
	private long birthDate;

	private PrintLoss(InsertBuilder builder){
		setContent(builder.content);
	}
	
	public PrintLoss(int id){
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
	
	public byte[] getContent() {
		return content;
	}
	
	public void setContent(byte[] content) {
		this.content = content;
	}
	
	public long getBirthDate() {
		return birthDate;
	}
	
	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PrintLoss)){
			return false;
		}else{
			return id == ((PrintLoss)obj).id;
		}
	}
	
	@Override
	public String toString(){
		if(content != null){
			try {
				return new String(content, "GBK");
			} catch (UnsupportedEncodingException e) {
				return "";
			}
		}else{
			return "";
		}
	}
}
