package com.wireless.pojo.billStatistics;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;

public class SalesDetail implements Jsonable{
	
	public SalesDetail(){
		
	}
	
	public SalesDetail(Food food){
		this.food = food;
		this.kitchen = food.getKitchen();
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
	private String restaurant;	//门店信息
	private Food food;			//菜品信息
	private float income;		//营业额
	private float tasteIncome;	//口味营业额
	private float discount;		//折扣额
	private float gifted;		//赠送额
	private float cost;			//成本
	private float costRate;		//成本率
	private float profit;		//毛利
	private float profitRate;	//毛利率
	private float salesAmount;	//销量
	private float avgPrice;		//均价
	private float avgCost;		//单位成本
	
	public String getRestaurant(){
		if(this.restaurant == null){
			return "";
		}
		return this.restaurant;
	}
	
	public void setResturant(String restaurant){
		this.restaurant = restaurant;
	}
	
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
	
	public float getTasteIncome() {
		return this.tasteIncome;
	}
	
	public void setTasteIncome(float tasteIncome) {
		this.tasteIncome = tasteIncome;
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

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putFloat("income", this.income);
		jm.putString("restaurant", this.restaurant);
		jm.putFloat("tasteIncome", this.tasteIncome);
		jm.putFloat("discount", this.discount);
		jm.putFloat("gifted", this.gifted);
		jm.putFloat("couponed", this.gifted);
		jm.putFloat("cost", this.cost);
		jm.putFloat("costRate", this.costRate);
		jm.putFloat("profit", this.profit);
		jm.putFloat("profitRate", this.profitRate);
		jm.putFloat("salesAmount", this.salesAmount);
		jm.putFloat("avgPrice", this.avgPrice);
		jm.putFloat("avgCost", this.avgCost);
		jm.putJsonable("dept", this.dept, Department.DEPT_JSONABLE_COMPLEX);
		jm.putJsonable("kitchen", this.kitchen, Kitchen.KITCHEN_JSONABLE_COMPLEX);
		jm.putJsonable("food", this.food, Food.FOOD_JSONABLE_COMPLEX);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

}
