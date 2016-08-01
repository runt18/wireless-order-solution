package com.wireless.pojo.distributionMgr;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.stockMgr.StockAction;

public class DistributionDetailReport implements Jsonable {
	private long oriStockDate;
	private int id;
	private int associateId;
	private StockAction.SubType subType;
	private Material material;
	private Restaurant stockOutRestaurant;
	private Restaurant stockInRestaurant;
	private int stockActionId;
	private String operator;
	private int operatorId;
	private float stockOutAmount;
	private float stockInAmount;
	private float stockOutMoney;
	private float stockInMoney;
	
	public float getStockOutAmount() {
		return stockOutAmount;
	}

	public void setStockOutAmount(float stockOutAmount) {
		this.stockOutAmount = stockOutAmount;
	}

	public float getStockInAmount() {
		return stockInAmount;
	}

	public void setStockInAmount(float stockInAmount) {
		this.stockInAmount = stockInAmount;
	}

	public float getStockOutMoney() {
		return stockOutMoney;
	}

	public void setStockOutMoney(float stockOutMoney) {
		this.stockOutMoney = stockOutMoney;
	}

	public float getStockInMoney() {
		return stockInMoney;
	}

	public void setStockInMoney(float stockInMoney) {
		this.stockInMoney = stockInMoney;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public int getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}

	public Integer getStockActionId() {
		return stockActionId;
	}

	public void setStockActionId(int stockActionId) {
		this.stockActionId = stockActionId;
	}

	public long getOriStockDate(){
		return this.oriStockDate;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAssociateId() {
		return associateId;
	}

	public void setAssociateId(Integer associateId) {
		this.associateId = associateId;
	}

	public StockAction.SubType getSubType() {
		return subType;
	}

	public void setSubType(StockAction.SubType subType) {
		this.subType = subType;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Restaurant getStockOutRestaurant() {
		return stockOutRestaurant;
	}

	public void setStockOutRestaurant(Restaurant stockOutRestaurant) {
		this.stockOutRestaurant = stockOutRestaurant;
	}

	public Restaurant getStockInRestaurant() {
		return stockInRestaurant;
	}

	public void setStockInRestaurant(Restaurant stockInRestaurant) {
		this.stockInRestaurant = stockInRestaurant;
	}

	public void setOriStockDate(Long oriStockDate) {
		this.oriStockDate = oriStockDate;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putLong("oriStockDate", this.oriStockDate);
		jm.putString("oriStockDateFormat", new SimpleDateFormat("yyyy-MM-dd").format(new Date(this.oriStockDate)));
		jm.putInt("associateId", this.associateId);
		jm.putJsonable("material", this.material, 0);
		jm.putJsonable("stockInRestaurant", this.stockInRestaurant, 0);
		jm.putJsonable("stockOutRestaurant", this.stockOutRestaurant, 0);
		jm.putInt("stockActionId", this.stockActionId);
		if(this.subType != null){
			jm.putInt("subTypeValue", this.subType.getVal());
			jm.putString("subTypeText", this.subType.getText());
		}
		jm.putInt("operateId", this.operatorId);
		jm.putString("operator", this.operator);
		jm.putFloat("stockOutAmount", this.stockOutAmount);
		jm.putFloat("stockOutMoney", this.stockOutMoney);
		jm.putFloat("stockInAmount", this.stockInAmount);
		jm.putFloat("stockInMoney", this.stockInMoney);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		// TODO Auto-generated method stub
		
	}
	
}
