package com.wireless.pojo.client;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class MemberType {
	
	public static final String OLD_DISCOUNTID_KEY = "OLD_DISCOUNTID_KEY";
	
	private int typeID;
	private int restaurantID;
	private String name;
	private int discountID;
	private int discountType;
	private double discountRate;
	private double chargeRate;
	private double exchangeRate;
	private int attribute;
	private Map other;
	
	public MemberType(){
		this.other = new HashMap();
	}
	
	public int getTypeID() {
		return typeID;
	}
	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getDiscountID() {
		return discountID;
	}
	public void setDiscountID(int discountID) {
		this.discountID = discountID;
	}
	public int getDiscountType() {
		return discountType;
	}
	public void setDiscountType(int discountType) {
		this.discountType = discountType;
	}
	public double getDiscountRate() {
		return discountRate;
	}
	public void setDiscountRate(double discountRate) {
		this.discountRate = discountRate;
	}
	public double getChargeRate() {
		return chargeRate;
	}
	public void setChargeRate(double chargeRate) {
		this.chargeRate = chargeRate;
	}
	public double getExchangeRate() {
		return exchangeRate;
	}
	public void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	public int getAttribute() {
		return attribute;
	}
	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}
	public Map getOther() {
		return other;
	}
	public void setOther(Map other) {
		this.other = other;
	}
	
	
}
