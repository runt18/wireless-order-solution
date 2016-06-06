package com.wireless.pojo.supplierMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Supplier implements Jsonable{

	private int id;
	private int restaurantId;
	private String name;
	private String tele;
	private String addr;
	private String contact;
	private String comment;
	
	public Supplier(){}
	
	public Supplier(int supplierId){
		this.id = supplierId;
	}
	
	public Supplier(InsertBuilder builder){
		this.restaurantId = builder.getRestaurantId();
		this.name = builder.getName();
		this.tele = builder.getTele();
		this.addr = builder.getAddr();
		this.contact = builder.getContact();
		this.comment = builder.getComment();
	}
	
	public Supplier(UpdateBuilder builder){
		this.id = builder.getId();
		this.restaurantId = builder.getRestaurantId();
		this.name = builder.getName();
		this.tele = builder.getTele();
		this.addr = builder.getAddr();
		this.contact = builder.getContact();
		this.comment = builder.getComment();
	}
	
	public int getId() {
		return id;
	}

	public void setId(int supplierid) {
		this.id = supplierid;
	}

	public int getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantid(int restaurantid) {
		this.restaurantId = restaurantid;
	}

	public String getName() {
		if(name == null){
			name = "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTele() {
		if(tele == null){
			tele = "";
		}
		return tele;
	}

	public void setTele(String tele) {
		this.tele = tele;
	}

	public String getAddr() {
		if(addr == null){
			addr = "";
		}
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getContact() {
		if(contact == null){
			contact = "";
		}
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getComment() {
		if(comment == null){
			comment = "";
		}
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public static class InsertBuilder{
		private int restaurantId;
		private String name;
		private String tele;
		private String addr;
		private String contact;
		private String comment;
		
		public int getRestaurantId() {
			return restaurantId;
		}
		
		public InsertBuilder setRestaurantId(int restaurantId) {
			this.restaurantId = restaurantId;
			return this;
		}
		
		public String getName() {
			return name;
		}
		
		public InsertBuilder setName(String name) {
			this.name = name;
			return this;
		}
		
		public String getTele() {
			return tele;
		}
		
		public InsertBuilder setTele(String tele) {
			this.tele = tele;
			return this;
		}
		
		public String getAddr() {
			return addr;
		}
		
		public InsertBuilder setAddr(String addr) {
			this.addr = addr;
			return this;
		}
		
		public String getContact() {
			return contact;
		}
		
		public InsertBuilder setContact(String contact) {
			this.contact = contact;
			return this;
		}
		
		public String getComment() {
			return comment;
		}
		
		public InsertBuilder setComment(String comment) {
			this.comment = comment;
			return this;
		}
		
		public Supplier build(){
			return new Supplier(this);
		}
	}
	
	
	public static class UpdateBuilder{
		private int id;
		private int restaurantId;
		private String name;
		private String tele;
		private String addr;
		private String contact;
		private String comment;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public int getId() {
			return id;
		}
		
		public UpdateBuilder setId(int id) {
			this.id = id;
			return this;
		}
		
		public int getRestaurantId() {
			return restaurantId;
		}
		
		public UpdateBuilder setRestaurantId(int restaurantId) {
			this.restaurantId = restaurantId;
			return this;
		}
		
		public String getName() {
			return name;
		}
		
		public UpdateBuilder setName(String name) {
			this.name = name;
			return this;
		}
		
		public String getTele() {
			return tele;
		}
		
		public UpdateBuilder setTele(String tele) {
			this.tele = tele;
			return this;
		}
		
		public String getAddr() {
			return addr;
		}
		
		public UpdateBuilder setAddr(String addr) {
			this.addr = addr;
			return this;
		}
		
		public String getContact() {
			return contact;
		}
		public UpdateBuilder setContact(String contact) {
			this.contact = contact;
			return this;
		}
		
		public String getComment() {
			return comment;
		}
		
		public UpdateBuilder setComment(String comment) {
			this.comment = comment;
			return this;
		}
		
		public Supplier build(){
			return new Supplier(this);
		}
		
	}
	
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Supplier)){
			return false;
		}else{
			return id == ((Supplier)obj).id && restaurantId == ((Supplier)obj).restaurantId;
		}
		
	}
	@Override
	public String toString(){
		return "supplier(" +
				"supplier_id = " + id +
				", restaurant_id = " + restaurantId +
				", name = " + getName() +
				", tele = " + getTele() +
				", addr = " + getAddr() +
				", contact = " + getContact() +
				", comment = " + getComment() + ")";
		
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("supplierID", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putString("name", this.name);
		jm.putString("tele", this.tele);
		jm.putString("addr", this.addr);
		jm.putString("contact", this.contact);
		jm.putString("comment", this.comment);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}



}
