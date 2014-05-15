package com.wireless.pojo.stockMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.util.DateUtil;

public class StockDetailReport implements Jsonable {

	private int id;
	private long date;
	private String oriStockId;
	private String deptIn;
	private String deptOut;
	private SubType stockActionSubType; 
	private float stockActionAmount;
	private float stockDetailPrice;
	private float remaining;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getDate() {
		return date;
	}
	public void setDate(long date) {
		this.date = date;
	}
	public String getOriStockId() {
		if(oriStockId.trim().length() == 0 || oriStockId == null){
			oriStockId = "----";
		}
		return oriStockId;
	}
	public void setOriStockId(String oriStockId) {
		this.oriStockId = oriStockId;
	}

	public String getDeptIn() {
		return deptIn;
	}
	public void setDeptIn(String deptIn) {
		this.deptIn = deptIn;
	}
	public String getDeptOut() {
		return deptOut;
	}
	public void setDeptOut(String deptOut) {
		this.deptOut = deptOut;
	}
	public SubType getStockActionSubType() {
		return stockActionSubType;
	}
	public void setStockActionSubType(int val){
		this.stockActionSubType = SubType.valueOf(val);
	}
	public void setStockActionSubType(SubType stockActionSubType) {
		this.stockActionSubType = stockActionSubType;
	}
	public float getStockActionAmount() {
		return stockActionAmount;
	}
	public void setStockActionAmount(float stockActionAmount) {
		this.stockActionAmount = stockActionAmount;
	}
	
	public float getStockDetailPrice() {
		return stockDetailPrice;
	}
	public void setStockDetailPrice(float stockDetailPrice) {
		this.stockDetailPrice = stockDetailPrice;
	}
	public float getRemaining() {
		return remaining;
	}
	public void setRemaining(float remaining) {
		this.remaining = remaining;
	}
	
	public float totalMoney(){
		return this.stockActionAmount * this.stockDetailPrice;
	}
	
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("id", this.getId());
		jm.put("date", DateUtil.formatToDate(this.getDate()));
		jm.put("oriStockId", this.getOriStockId());
		if(this.getStockActionSubType() == SubType.STOCK_IN || this.getStockActionSubType() == SubType.MORE || this.getStockActionSubType() == SubType.SPILL){
			jm.put("dept", this.getDeptIn());
			jm.put("stockInSubType", this.getStockActionSubType().getText());
			jm.put("stockInAmount", this.getStockActionAmount());
			jm.put("stockInMoney", this.totalMoney());
			jm.put("stockOutSubType", "----");
			jm.put("stockOutAmount", "----");
			jm.put("stockOutMoney", "----");
			
		}else if(this.getStockActionSubType() == SubType.STOCK_IN_TRANSFER){
			jm.put("dept", this.getDeptOut() + " -> " + this.getDeptIn());
			jm.put("stockInSubType", this.getStockActionSubType().getText());
			jm.put("stockInAmount", this.getStockActionAmount());
			jm.put("stockInMoney", this.totalMoney());
			jm.put("stockOutSubType", "----");
			jm.put("stockOutAmount", "----");
			jm.put("stockOutMoney", "----");
		}else if(this.getStockActionSubType() == SubType.STOCK_OUT_TRANSFER){
			jm.put("dept", this.getDeptOut()+ " -> " + this.getDeptIn());
			jm.put("stockInSubType", "----");
			jm.put("stockInAmount", "----");
			jm.put("stockInMoney", "----");
			jm.put("stockOutSubType", this.getStockActionSubType().getText());
			jm.put("stockOutAmount", this.getStockActionAmount());
			jm.put("stockOutMoney", this.totalMoney());
		}
		else{
			jm.put("dept", this.getDeptOut());
			jm.put("stockInSubType", "----");
			jm.put("stockInAmount", "----");
			jm.put("stockInMoney", "----");
			jm.put("stockOutSubType", this.getStockActionSubType().getText());
			jm.put("stockOutAmount", this.getStockActionAmount());
			jm.put("stockOutMoney", this.totalMoney());
		}

		jm.put("remaining", this.getRemaining());
		
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
