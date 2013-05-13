package com.wireless.pojo.supplierMgr;

public class Supplier {

	private int supplierid;
	private int restaurantid;
	private String name;
	private String tele;
	private String addr;
	private String contact;
	private String comment;
	
	
	public int getSupplierid() {
		return supplierid;
	}

	public void setSupplierid(int supplierid) {
		this.supplierid = supplierid;
	}

	public int getRestaurantid() {
		return restaurantid;
	}

	public void setRestaurantid(int restaurantid) {
		this.restaurantid = restaurantid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTele() {
		return tele;
	}

	public void setTele(String tele) {
		this.tele = tele;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public int hashCode(){
		return supplierid * 31 + 17;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Supplier)){
			return false;
		}else{
			return supplierid == ((Supplier)obj).supplierid && restaurantid == ((Supplier)obj).restaurantid;
		}
		
	}
	
	public String toString(){
		return "supplier(" +
				"supplier_id = " + supplierid +
				", restaurant_id = " + restaurantid +
				", name = " + name +
				", tele = " + tele +
				", addr = " + addr +
				", contact = " + contact +
				", comment = " + comment + ")";
		
	}
	
	public Supplier(int supplierid){
		this.supplierid = supplierid;
	}
	
	public Supplier(int supplierid, int restaurantid){
		this.supplierid = supplierid;
		this.restaurantid = restaurantid;
	}
	
	public Supplier(int supplierid, int restaurantid, String name, String tele, String addr){
		this.supplierid = supplierid;
		this.restaurantid = restaurantid;
		this.name = name;
		this.tele = tele;
		this.addr = addr;
	}


}
