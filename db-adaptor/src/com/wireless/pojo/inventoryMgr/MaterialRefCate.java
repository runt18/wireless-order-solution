package com.wireless.pojo.inventoryMgr;

import java.util.Date;

public class MaterialRefCate {
	private Integer allCount;
	private Integer materialId;
	private Float amount;
	private Float price;
	private String name;
	private String cateName;
	private Integer status;
	private String lastModStaff;
	private Date lastModDate;
	public Integer getMaterialId(){
		return materialId;
	}
	public Float getAmount(){
		return amount;
	}
	public Float getPrice(){
		return price;
	}
	public String getName(){
		return name;
	}
	public Integer getStatus(){
		return status;
	}
	public String getLastModStaff(){
		return lastModStaff;
	}
	public Date getLastModDate(){
		return lastModDate;
	}
	public void setMaterialId(Integer materialId){
		this.materialId = materialId;
	}
	public void setAmount(Float amount){
		this.amount = amount;
	}
	public void setPrice(Float price){
		this.price = price;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setStatus(Integer status){
		this.status = status;
	}
	public void setLastModStaff(String lastModStaff){
		this.lastModStaff = lastModStaff;
	}
	public void setLastModDate(Date lastModDate){
		this.lastModDate = lastModDate;
	}
	public String getCateName() {
		return cateName;
	}
	public void setCateName(String cateName) {
		this.cateName = cateName;
	}
	public Integer getAllCount() {
		return allCount;
	}
	public void setAllCount(Integer allCount) {
		this.allCount = allCount;
	}
}
