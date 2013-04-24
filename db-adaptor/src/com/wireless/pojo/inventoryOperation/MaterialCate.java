package com.wireless.pojo.inventoryOperation;
import java.util.*;
public class MaterialCate{
	private Integer cateId;
	private Integer restaurantId;
	private String name;
	private Integer type;
	private Integer parentId;

	public Integer getCateId(){
		return cateId;
	}
	public Integer getRestaurantId(){
		return restaurantId;
	}
	public String getName(){
		return name;
	}
	public Integer getType(){
		return type;
	}
	public Integer getParentId(){
		return parentId;
	}

	public void setCateId(Integer cateId){
		this.cateId = cateId;
	}
	public void setRestaurantId(Integer restaurantId){
		this.restaurantId = restaurantId;
	}
	public void setName(String name){
		this.name = name;
	}
	public void setType(Integer type){
		this.type = type;
	}
	public void setParentId(Integer parentId){
		this.parentId = parentId;
	}

	public static class FormFields{
		public static final String CATE_ID ="cateId";
		public static final String RESTAURANT_ID ="restaurantId";
		public static final String NAME ="name";
		public static final String TYPE ="type";
		public static final String PARENT_ID ="parentId";
	}

	public static class TableFields{
		public static final String CATE_ID ="CATE_ID";
		public static final String RESTAURANT_ID ="RESTAURANT_ID";
		public static final String NAME ="NAME";
		public static final String TYPE ="TYPE";
		public static final String PARENT_ID ="PARENT_ID";
	}

	@Override
	public String toString(){
		return "[MaterialCate]:"+"[cateId="+cateId+",restaurantId="+restaurantId+",name="+name+",type="+type+",parentId="+parentId+"]";
	}

	@Override
	public int hashCode(){
		return cateId*31+17;
	}

	@Override
	public boolean equals(Object obj){
		if(obj == null || ! (obj instanceof MaterialCate)){
			return false;
		}
		else{
			return (this.cateId == ((MaterialCate)obj).cateId);
		}
	}

}
