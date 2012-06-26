package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.QueryMenu;
import com.wireless.dbObject.MaterialDetail;
import com.wireless.dbObject.SingleOrderFood;
import com.wireless.dbReflect.MaterialDetailReflector;
import com.wireless.dbReflect.SingleOrderFoodReflector;
import com.wireless.protocol.Department;
import com.wireless.protocol.Terminal;

public class QuerySaleDetails {
	
	public final static int QUERY_BY_DEPT = 0;	//按部门查询
	public final static int QUERY_BY_FOOD = 1;	//按菜品查询
	
	public final static int ORDER_BY_PROFIT = 0;	//按毛利排序
	public final static int ORDER_BY_SALES = 1;		//按销售量排序
	
	public static class Result{
		
		Result(String item){
			this.item = item;
		}
		
		public String item;			//部门或菜品名称
		public float income;		//营业额
		public float discount;		//折扣额
		public float gifted;		//赠送额
		public float cost;			//成本
		public float costRate;		//成本率
		public float profit;		//毛利
		public float profitRate;	//毛利率
		public float salesAmount;	//销量
		public float avgPrice;		//均价
		public float avgCost;		//单位成本
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param onDuty
	 * @param offDuty
	 * @param queryType
	 * @param orderType
	 * @return
	 * @throws SQLException
	 */
	public static Result[] exec(DBCon dbCon, Terminal term, String onDuty, String offDuty, int queryType, int orderType) throws SQLException{
	
		SingleOrderFood[] orderFoods = new SingleOrderFood[0];
		/**
		 * Get the single order food information
		 */
		orderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, 
							"AND B.restaurant_id=" + term.restaurant_id + " " + 
							"AND B.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
							null);				
		
		/**
		 * Get the material detail information
		 */
		MaterialDetail[] materialDetails = new MaterialDetail[0];
		materialDetails = MaterialDetailReflector.getMaterialDetail(dbCon, 
							" AND MATE_DETAIL.restaurant_id=" + term.restaurant_id + " " +
							" AND MATE_DETAIL.type=" + MaterialDetail.TYPE_CONSUME +
							" AND MATE_DETAIL.date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
							"");
		
		if(queryType == QUERY_BY_DEPT){
			HashMap<Department, Result> deptSalesDetail = new HashMap<Department, Result>();
			for(Department dept : QueryMenu.queryDepartments(dbCon, term.restaurant_id, null, null)){
				deptSalesDetail.put(dept, new Result(dept.name));
			}
			
			/**
			 * Calculate the gift, discount, income to each department during this period
			 */
			for(SingleOrderFood singleOrderFood : orderFoods){
				Result salesDetail = deptSalesDetail.get(singleOrderFood.kitchen.dept);
	
				if(salesDetail != null){
					if(singleOrderFood.food.isGift()){
						salesDetail.gifted += singleOrderFood.calcPriceWithTaste();
					}else{
						salesDetail.income += singleOrderFood.calcPriceWithTaste();
					}
					
					if(singleOrderFood.discount < 1){
						salesDetail.discount += singleOrderFood.calcDiscountPrice();
					}
					
					deptSalesDetail.put(singleOrderFood.kitchen.dept, salesDetail);
				}
			}
			
			/**
			 * Calculate the cost to each department during this period
			 */
			for(MaterialDetail materialDetail : materialDetails){
				Result salesDetail = deptSalesDetail.get(materialDetail.dept);
				if(salesDetail != null){
					salesDetail.cost += materialDetail.calcPrice();
				}
			}

			/**
			 * Remove the invalid department sales detail record
			 */
			Iterator<Map.Entry<Department, Result>> iter = deptSalesDetail.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry<Department, Result> entry = iter.next();
				Result saleDetail = entry.getValue();
				if(saleDetail.gifted == 0 && saleDetail.income == 0 &&
				   saleDetail.discount == 0 && saleDetail.cost == 0){
					iter.remove();
				}
			}
			
			/**
			 * Calculate the profit, cost rate, profit rate to each department
			 */
			for(Department dept : deptSalesDetail.keySet()){
				Result salesDetail = deptSalesDetail.get(dept);
				
				salesDetail.gifted = (float)Math.round(salesDetail.gifted * 100) / 100;
				salesDetail.discount = (float)Math.round(salesDetail.discount * 100) / 100;
				salesDetail.income = (float)Math.round(salesDetail.income * 100) / 100;
				salesDetail.cost = (float)Math.round(salesDetail.cost * 100) / 100;
				
				salesDetail.profit = salesDetail.income - salesDetail.cost;
				salesDetail.profitRate = salesDetail.profit / salesDetail.income;
				salesDetail.costRate = salesDetail.cost / salesDetail.income;
				
				deptSalesDetail.put(dept, salesDetail);
			}
			
			Result[] result = deptSalesDetail.values().toArray(new Result[deptSalesDetail.values().size()]);
			/**
			 * Sort the department sales detail in descending order by profit
			 */
			Arrays.sort(result, new Comparator<Result>(){

				@Override
				public int compare(Result result1, Result result2) {
					if(result1.profit == result2.profit){
						return 0;
					}else if(result1.profit > result2.profit){
						return 1;
					}else{
						return -1;
					}
				}
				
			});
			
			return result;
			
		}else if(queryType == QUERY_BY_FOOD){
			
			return null;
		}else{
			return new Result[0];
		}
	}
}
