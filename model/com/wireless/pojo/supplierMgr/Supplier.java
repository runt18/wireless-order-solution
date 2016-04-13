package com.wireless.pojo.supplierMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Supplier implements Jsonable{

	private int supplierId;
	private int restaurantId;
	private String name;
	private String tele;
	private String addr;
	private String contact;
	private String comment;
	
	
	public int getId() {
		return supplierId;
	}

	public void setSupplierid(int supplierid) {
		this.supplierId = supplierid;
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
	@Override
	public int hashCode(){
		return supplierId * 31 + 17;
	}
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Supplier)){
			return false;
		}else{
			return supplierId == ((Supplier)obj).supplierId && restaurantId == ((Supplier)obj).restaurantId;
		}
		
	}
	@Override
	public String toString(){
		return "supplier(" +
				"supplier_id = " + supplierId +
				", restaurant_id = " + restaurantId +
				", name = " + getName() +
				", tele = " + getTele() +
				", addr = " + getAddr() +
				", contact = " + getContact() +
				", comment = " + getComment() + ")";
		
	}
	public Supplier(){}
	
	public Supplier(int supplierId){
		this.supplierId = supplierId;
	}
	
	public Supplier(String name, String tele, String addr, String contact, String comment){
		this.name = name;
		this.tele = tele;
		this.addr = addr;
		this.comment = comment;
		this.contact = contact;
	}
	
	public Supplier(int restaurantId, String name, String tele, String addr, String contact, String comment){
		this.restaurantId = restaurantId;
		this.name = name;
		this.tele = tele;
		this.addr = addr;
		this.comment = comment;
		this.contact = contact;
	}
	
	public Supplier(int supplierId, int restaurantId, String name, String tele, String addr, String contact, String comment){
		this.supplierId = supplierId;
		this.restaurantId = restaurantId;
		this.name = name;
		this.tele = tele;
		this.addr = addr;
		this.comment = comment;
		this.contact = contact;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("supplierID", this.supplierId);
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
