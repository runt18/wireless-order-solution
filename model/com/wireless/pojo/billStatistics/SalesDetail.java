package com.wireless.pojo.billStatistics;

import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;

public class SalesDetail {
	
	public SalesDetail(){
		
	}
	
	public SalesDetail(Food pt){
		this.food = new Food(pt);
		this.kitchen = pt.getKitchen();
	}
	
	public SalesDetail(Kitchen kitchen){
		this.kitchen = kitchen;
		this.dept = kitchen.getDept();
	}
	
	public SalesDetail(Department dept){
		this.dept = dept;
	}
	
	private Department dept;	//部门信息
	private Kitchen kitchen;	//厨房信息
	private Food food;			//菜品信息
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
	
	public Department getDept() {
		return dept;
	}
	public void setDept(Department dept) {
		this.dept = dept;
	}
	public Kitchen getKitchen() {
		return kitchen;
	}
	public void setKitchen(Kitchen kitchen) {
		this.kitchen = kitchen;
	}
	public Food getFood() {
		return food;
	}
	public void setFood(Food food) {
		this.food = food;
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
