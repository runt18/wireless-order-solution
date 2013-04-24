package com.wireless.pojo.inventoryOperation;
import java.util.*;
public class Material{
	private Integer materialId;
	private Integer cateId;
	private Float amount;
	private Float price;
	private String name;
	private Integer status;
	private String lastModStaff;
	private Date lastModDate;

	public Integer getMaterialId(){
		return materialId;
	}
	public Integer getCateId(){
		return cateId;
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
	public void setCateId(Integer cateId){
		this.cateId = cateId;
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

	public static class FormFields{
		public static final String MATERIAL_ID ="materialId";
		public static final String CATE_ID ="cateId";
		public static final String AMOUNT ="amount";
		public static final String PRICE ="price";
		public static final String NAME ="name";
		public static final String STATUS ="status";
		public static final String LAST_MOD_STAFF ="lastModStaff";
		public static final String LAST_MOD_DATE ="lastModDate";
	}

	public static class TableFields{
		public static final String MATERIAL_ID ="MATERIAL_ID";
		public static final String CATE_ID ="CATE_ID";
		public static final String AMOUNT ="AMOUNT";
		public static final String PRICE ="PRICE";
		public static final String NAME ="NAME";
		public static final String STATUS ="STATUS";
		public static final String LAST_MOD_STAFF ="LAST_MOD_STAFF";
		public static final String LAST_MOD_DATE ="LAST_MOD_DATE";
	}

	@Override
	public String toString(){
		return "[Material]:"+"[materialId="+materialId+",cateId="+cateId+",amount="+amount+",price="+price+",name="+name+",status="+status+",lastModStaff="+lastModStaff+",lastModDate="+lastModDate+"]";
	}

	@Override
	public int hashCode(){
		return materialId*31+17;
	}

	@Override
	public boolean equals(Object obj){
		if(obj == null || ! (obj instanceof Material)){
			return false;
		}
		else{
			return (this.materialId == ((Material)obj).materialId);
		}
	}

}
