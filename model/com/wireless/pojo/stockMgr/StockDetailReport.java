package com.wireless.pojo.stockMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.util.DateUtil;

public class StockDetailReport implements Jsonable {

//	private int id;
//	private long date;
//	private String oriStockId;
//	private String materialName; 
//	private String supplier; 
//	private String deptIn;
//	private String deptOut;
//	private SubType stockActionSubType; 
//	private float stockActionAmount;
//	private float stockDetailPrice;
//	private float remaining;
//	private String operater;
//	private float totalMoney = 0;
	
	private StockAction stockAction;
	private StockActionDetail stockActionDetail;
	
	public StockDetailReport(StockAction stockAction, StockActionDetail stockActionDetail){
		this.stockAction = stockAction;
		this.stockActionDetail = stockActionDetail;
	}
	
	public StockDetailReport(){
		
	}
	
	public void setStockAction(StockAction action){
		this.stockAction = action;
	}
	
	public void setStockActonDetail(StockActionDetail detail){
		this.stockActionDetail = detail;
	}
	
	public StockActionDetail getStockActionDetail(){
		return this.stockActionDetail;
	}
	
	public StockAction getStockAction(){
		return this.stockAction;
	}
	
//	public int getId() {
//		return id;
//	}
//	public void setId(int id) {
//		this.id = id;
//	}
//	public long getDate() {
//		return date;
//	}
//	public void setDate(long date) {
//		this.date = date;
//	}
//	public String getOriStockId() {
//		if(oriStockId == null || oriStockId.trim().length() == 0){
//			oriStockId = "----";
//		}
//		return oriStockId;
//	}
//	public void setOriStockId(String oriStockId) {
//		this.oriStockId = oriStockId;
//	}
//
//	public String getDeptIn() {
//		return deptIn;
//	}
//	public void setDeptIn(String deptIn) {
//		this.deptIn = deptIn;
//	}
//	public String getDeptOut() {
//		return deptOut;
//	}
//	public void setDeptOut(String deptOut) {
//		this.deptOut = deptOut;
//	}
//	public SubType getStockActionSubType() {
//		return stockActionSubType;
//	}
//	public void setStockActionSubType(int val){
//		this.stockActionSubType = SubType.valueOf(val);
//	}
//	public void setStockActionSubType(SubType stockActionSubType) {
//		this.stockActionSubType = stockActionSubType;
//	}
//	public float getStockActionAmount() {
//		return stockActionAmount;
//	}
//	public void setStockActionAmount(float stockActionAmount) {
//		this.stockActionAmount = stockActionAmount;
//	}
//	
//	public float getStockDetailPrice() {
//		return stockDetailPrice;
//	}
//	public void setStockDetailPrice(float stockDetailPrice) {
//		this.stockDetailPrice = stockDetailPrice;
//	}
//	public float getRemaining() {
//		return remaining;
//	}
//	public void setRemaining(float remaining) {
//		this.remaining = remaining;
//	}
//	
//	public float totalMoney(){
//		return this.totalMoney;
//	}
//	public String getMaterialName() {
//		return materialName;
//	}
//	public void setMaterialName(String materialName) {
//		this.materialName = materialName;
//	}
//	public String getOperater() {
//		return operater;
//	}
//	public void setOperater(String operater) {
//		this.operater = operater;
//	}
//	public String getSupplier() {
//		return supplier;
//	}
//	public void setSupplier(String supplier) {
//		this.supplier = supplier;
//	}
//	public float getTotalMoney() {
//		return totalMoney;
//	}
//	public void setTotalMoney(float totalMoney) {
//		this.totalMoney = totalMoney;
//	}
	@Override
	public JsonMap toJsonMap(int flag) {
		
		if(this.stockAction == null || this.stockActionDetail == null){
			return null;
		}

		
		JsonMap jm = new JsonMap();
		
		jm.putString("date", DateUtil.formatToDate(this.stockAction.getOriStockDate()));
		jm.putString("oriStockId", this.stockAction.getOriStockId());
		jm.putString("materialName", this.stockActionDetail.getName());
		jm.putString("supplier", this.stockAction.getSupplier().getName());
		
//		jm.putString("date", DateUtil.formatToDate(this.getDate()));
//		jm.putString("oriStockId", this.getOriStockId());
//		jm.putString("materialName", this.getMaterialName());
//		jm.putString("supplier", this.getSupplier());
		
		if(this.stockAction.getSubType() == SubType.INIT || this.stockAction.getSubType() == SubType.STOCK_IN || this.stockAction.getSubType() == SubType.MORE || this.stockAction.getSubType() == SubType.SPILL){
			
			jm.putString("dept", this.stockAction.getDeptIn().getName());
			jm.putString("stockInSubType", this.stockAction.getSubType().getText());
			jm.putFloat("stockInAmount", this.stockActionDetail.getAmount());
			jm.putFloat("stockInMoney", this.stockActionDetail.getAmount() * this.stockActionDetail.getPrice());
			jm.putString("stockOutSubType", "----");
			jm.putString("stockOutAmount", "----");
			jm.putString("stockOutMoney", "----");
			
//			jm.putString("dept", this.getDeptIn());
//			jm.putString("stockInSubType", this.getStockActionSubType().getText());
//			jm.putFloat("stockInAmount", this.getStockActionAmount());
//			jm.putFloat("stockInMoney", this.totalMoney());
//			jm.putString("stockOutSubType", "----");
//			jm.putString("stockOutAmount", "----");
//			jm.putString("stockOutMoney", "----");
			
		}else if(this.stockAction.getSubType() == SubType.STOCK_IN_TRANSFER){
			
			jm.putString("dept", this.stockAction.getDeptOut().getName() + " -> " + this.stockAction.getDeptIn().getName());
			jm.putString("stockInSubType", this.stockAction.getSubType().getText());
			jm.putFloat("stockInAmount", this.stockActionDetail.getAmount());
			jm.putFloat("stockInMoney", this.stockActionDetail.getAmount() * this.stockActionDetail.getPrice());
			jm.putString("stockOutSubType", "----");
			jm.putString("stockOutAmount", "----");
			jm.putString("stockOutMoney", "----");
			
//			jm.putString("dept", this.getDeptOut() + " -> " + this.getDeptIn());
//			jm.putString("stockInSubType", this.getStockActionSubType().getText());
//			jm.putFloat("stockInAmount", this.getStockActionAmount());
//			jm.putFloat("stockInMoney", this.totalMoney());
//			jm.putString("stockOutSubType", "----");
//			jm.putString("stockOutAmount", "----");
//			jm.putString("stockOutMoney", "----");
			
		}else if(this.stockAction.getSubType() == SubType.STOCK_OUT_TRANSFER){
			
			jm.putString("dept", this.stockAction.getDeptOut().getName()+ " -> " + this.stockAction.getDeptIn().getName());
			jm.putString("stockInSubType", "----");
			jm.putString("stockInAmount", "----");
			jm.putString("stockInMoney", "----");
			jm.putString("stockOutSubType", this.stockAction.getSubType().getText());
			jm.putFloat("stockOutAmount", this.stockActionDetail.getAmount());
			jm.putFloat("stockOutMoney", this.stockActionDetail.getAmount() * this.stockActionDetail.getPrice());
			
//			jm.putString("dept", this.getDeptOut()+ " -> " + this.getDeptIn());
//			jm.putString("stockInSubType", "----");
//			jm.putString("stockInAmount", "----");
//			jm.putString("stockInMoney", "----");
//			jm.putString("stockOutSubType", this.getStockActionSubType().getText());
//			jm.putFloat("stockOutAmount", this.getStockActionAmount());
//			jm.putFloat("stockOutMoney", this.totalMoney());
			
		}else{
			
			jm.putString("dept", this.stockAction.getDeptOut().getName());
			jm.putString("stockInSubType", "----");
			jm.putString("stockInAmount", "----");
			jm.putString("stockInMoney", "----");
			jm.putString("stockOutSubType", this.stockAction.getSubType() != null ? this.stockAction.getSubType().getText() : "---");
			jm.putFloat("stockOutAmount", this.stockActionDetail.getAmount());
			jm.putFloat("stockOutMoney", this.stockActionDetail.getAmount() * this.stockActionDetail.getPrice());
			
//			jm.putString("dept", this.getDeptOut());
//			jm.putString("stockInSubType", "----");
//			jm.putString("stockInAmount", "----");
//			jm.putString("stockInMoney", "----");
//			jm.putString("stockOutSubType", this.getStockActionSubType().getText());
//			jm.putFloat("stockOutAmount", this.getStockActionAmount());
//			jm.putFloat("stockOutMoney", this.totalMoney());
		}

		jm.putFloat("remaining", this.stockActionDetail.getRemaining());
		jm.putString("operater", this.stockAction.getApprover());
		
//		jm.putFloat("remaining", this.getRemaining());
//		jm.putString("operater", this.getOperater());
		
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
