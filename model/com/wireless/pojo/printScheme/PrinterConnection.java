package com.wireless.pojo.printScheme;

public class PrinterConnection {

	public static class InsertBuilder{
		private final String source;
		private final String dest;
		
		public InsertBuilder(String source, String dest){
			this.source = source;
			this.dest = dest;
		}
		
		public PrinterConnection build(){
			return new PrinterConnection(this);
		}
	}
	
	private int id;
	private int restaurantId;
	private String source;
	private String dest;
	private long lastConnection;
	
	private PrinterConnection(InsertBuilder builder){
		setSource(builder.source);
		setDest(builder.dest);
	}
	
	public PrinterConnection(int id){
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
	
	public String getSource() {
		if(source == null){
			return "";
		}
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getDest(){
		if(dest == null){
			return "";
		}
		return this.dest;
	}
	
	public void setDest(String dest){
		this.dest = dest;
	}
	
	public long getLastConnected() {
		return lastConnection;
	}
	
	public void setLastConnected(long connectDate) {
		this.lastConnection = connectDate;
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PrinterConnection)){
			return false;
		}else{
			return id == ((PrinterConnection)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return "[" + getSource() + "->" + getDest() + "]";
	}
}
