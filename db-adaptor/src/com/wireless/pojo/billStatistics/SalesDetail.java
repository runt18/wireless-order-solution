package com.wireless.pojo.billStatistics;

public class SalesDetail {
	
	public SalesDetail(){
		
	}
	
	public SalesDetail(String item){
		this.item = item;
	}
	
	private String item;		//部门或菜品名称
	private float income;		//营业额
	private float discount;		//折扣额
	private float gifted;		//赠送额
	private float cost;			//成本
	private float costRate;		//成本率
	private float profit;		//毛利
	private float profitRate;	//毛利率
	private float salesAmount;	//销量
	private float avgPrice;		//均价
	private float avgCost;		//单位成本
	
	public String getItem() {
		return item;
	}
	public void setItem(String item) {
		this.item = item;
	}
	public float getIncome() {
		return income;
	}
	public void setIncome(float income) {
		this.income = income;
	}
	public float getDiscount() {
		return discount;
	}
	public void setDiscount(float discount) {
		this.discount = discount;
	}
	public float getGifted() {
		return gifted;
	}
	public void setGifted(float gifted) {
		this.gifted = gifted;
	}
	public float getCost() {
		return cost;
	}
	public void setCost(float cost) {
		this.cost = cost;
	}
	public float getCostRate() {
		return costRate;
	}
	public void setCostRate(float costRate) {
		this.costRate = costRate;
	}
	public float getProfit() {
		return profit;
	}
	public void setProfit(float profit) {
		this.profit = profit;
	}
	public float getProfitRate() {
		return profitRate;
	}
	public void setProfitRate(float profitRate) {
		this.profitRate = profitRate;
	}
	public float getSalesAmount() {
		return salesAmount;
	}
	public void setSalesAmount(float salesAmount) {
		this.salesAmount = salesAmount;
	}
	public float getAvgPrice() {
		return avgPrice;
	}
	public void setAvgPrice(float avgPrice) {
		this.avgPrice = avgPrice;
	}
	public float getAvgCost() {
		return avgCost;
	}
	public void setAvgCost(float avgCost) {
		this.avgCost = avgCost;
	}
	
	
}
