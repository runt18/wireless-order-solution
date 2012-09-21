package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.db.DBCon;
import com.wireless.db.QueryMenu;
import com.wireless.db.VerifyPin;
import com.wireless.dbObject.SingleOrderFood;
import com.wireless.dbReflect.SingleOrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.CancelledFood;
import com.wireless.protocol.Department;
import com.wireless.protocol.Terminal;

public class QueryCancelledFood {
	
	public final static int QUERY_BY_DEPT = 0;		//按部门显示
	public final static int QUERY_BY_REASON = 1;	//按原因显示
	public final static int QUERY_BY_FOOD = 2;		//按菜品显示	
	
	public final static int ORDER_BY_COUNT = 0;		//按数量排序
	public final static int ORDER_BY_PRICE = 1;		//按金额排序
	
	/**
	 * Get the cancelled foods to each departments.
	 * @param DBCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to be queried
	 * @param queryDate
	 * 			the date range to query
	 * @param orderBy
	 * 			the category in which the list sort
	 * @return
	 */
	public static CancelledFood[] getCancelledFoodByDept(DBCon dbCon, Terminal term, DutyRange queryDate, int orderBy) throws SQLException{
		
		CancelledFood[] result = null;
		
		/**
		 * Get the duty range between on and off duty date
		 */
		DutyRange dutyRange = QueryDutyRange.exec(dbCon, term, queryDate.getOnDuty(), queryDate.getOffDuty());
		
		if(dutyRange == null){
			return null;
		}
		
		SingleOrderFood[] orderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, 
				"AND B.restaurant_id=" + term.restaurantID + " " + 
				"AND B.order_date BETWEEN '" + dutyRange.getOnDuty() + "' AND '" + dutyRange.getOffDuty() + "'", 
				null);		
		
		/**
		 * Get all the basic info to department 
		 */
		HashMap<Department, CancelledFood> deptCancelledFoodDetail = new HashMap<Department, CancelledFood>();
		Department[] departmentDetail = QueryMenu.queryDepartments(dbCon, " AND DEPT.restaurant_id=" + term.restaurantID, null);
		for(Department dept : departmentDetail){
			deptCancelledFoodDetail.put(dept, new CancelledFood(dept.name, ""));
		}
		
		/**
		 * Put the temporary department
		 */
		deptCancelledFoodDetail.put(new Department("临时菜", Department.DEPT_TEMP, term.restaurantID, Department.TYPE_RESERVED), new CancelledFood("临时菜", ""));
		
		/**
		 * Calculate the orderCount to each department during this period
		 */
		for(SingleOrderFood singleOrderFood : orderFoods){
			CancelledFood cancelledDetail = deptCancelledFoodDetail.get(singleOrderFood.kitchen.dept);
			if(cancelledDetail != null && singleOrderFood.orderCount < 0){
				cancelledDetail.setDeptID(singleOrderFood.kitchen.dept.deptID);
				cancelledDetail.setCount(Math.abs(cancelledDetail.getCount()) + Math.abs(singleOrderFood.orderCount));
//				bfDetail.setPrice(singleOrderFood.unitPrice);
				cancelledDetail.setTotalPrice(cancelledDetail.getTotalPrice() + Math.abs(singleOrderFood.orderCount) * singleOrderFood.unitPrice);
				deptCancelledFoodDetail.put(singleOrderFood.kitchen.dept, cancelledDetail);
			}
		}
		
		Iterator<Map.Entry<Department, CancelledFood>> iter = deptCancelledFoodDetail.entrySet().iterator();
		while(iter.hasNext()){
			Entry<Department, CancelledFood> entry = iter.next();
			CancelledFood val = entry.getValue();
			if(val.getCount() <= 0){
				iter.remove();
			}
		}
		
		result = deptCancelledFoodDetail.values().toArray(new CancelledFood[deptCancelledFoodDetail.values().size()]);
		
		if(orderBy == QueryCancelledFood.ORDER_BY_COUNT){
			Arrays.sort(result, new Comparator<CancelledFood>(){
				@Override
				public int compare(CancelledFood r1, CancelledFood r2) {
					if(r1.getCount() > r2.getCount()){
						return -1;
					}else if(r1.getCount() < r2.getCount()){
						return 1;
					}else{
						return 0;
					}
				}					
			});
		}else if(orderBy == QueryCancelledFood.ORDER_BY_PRICE){
			Arrays.sort(result, new Comparator<CancelledFood>(){
				@Override
				public int compare(CancelledFood r1, CancelledFood r2) {
					if(r1.getTotalPrice() > r2.getTotalPrice()){
						return -1;
					}else if(r1.getTotalPrice() < r2.getTotalPrice()){
						return 1;
					}else{
						return 0;
					}
				}					
			});
		}
		
		return result;
	}
	
	/**
	 * Get the cancelled foods to each departments.
	 * @param pin
	 * 			the pin to query
	 * @param queryDate
	 * 			the date range to query
	 * @param orderBy
	 * 			the category in which the list sort
	 * @return
	 */
	public static CancelledFood[] getCancelledFoodByDept(long pin, DutyRange queryDate, int orderBy) throws SQLException, BusinessException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();					
			
			return getCancelledFoodByDept(dbCon, VerifyPin.exec(dbCon, pin, Terminal.MODEL_STAFF), queryDate, orderBy);			
			
		}finally{
			dbCon.disconnect();
		}

	}
	
	/**
	 * 
	 * @param pin
	 * @param duty
	 * @param orderBy
	 * @param deptID
	 * @return
	 */
	public static CancelledFood[] getCancelledFood(long pin, DutyRange queryDate, int orderBy, String deptID){
		CancelledFood[] list = null;
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, Terminal.MODEL_STAFF);
			
			/**
			 * Get the duty range between on and off duty date
			 */
			DutyRange dutyRange = QueryDutyRange.exec(dbCon, term, queryDate.getOnDuty(), queryDate.getOffDuty());
			
			if(dutyRange == null){
				return null;
			}
			
			String dept = "";
			if(deptID != null && deptID.trim().length() > 0){
				String[] tp = deptID.split(",");
				for(int i = 0; i < tp.length; i++){
					dept += (i > 0 ? "," : "");
					dept += tp[i];
				}
			}
			
			SingleOrderFood[] orderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, 
					" AND B.restaurant_id=" + term.restaurantID + " " + 
					" AND B.order_date BETWEEN '" + dutyRange.getOnDuty() + "' AND '" + dutyRange.getOffDuty() + "'" +  
					(dept.length() != 0 && dept != "-1" ? " AND A.dept_id IN(" + dept + ")" : ""),
					null);
			
			if(orderFoods == null || orderFoods.length == 0){
				return null;
			}
			
			List<CancelledFood> tpList = new ArrayList<CancelledFood>();
			CancelledFood item = null;
			for(int i = 0; i < orderFoods.length; i++){
				SingleOrderFood singleOrderFood = orderFoods[i];
				if(singleOrderFood.orderCount <= 0){
					item = new CancelledFood();
					item.setOrderDate(singleOrderFood.orderDate);
					item.setOrderID(singleOrderFood.orderID);
					item.setFoodID(singleOrderFood.food.foodID);
					item.setFoodName(singleOrderFood.food.name);
					item.setDeptID(singleOrderFood.kitchen.dept.deptID);
					item.setDeptName(singleOrderFood.kitchen.dept.name);
					item.setPrice(singleOrderFood.unitPrice);
					item.setCount(Math.abs(singleOrderFood.orderCount));
					item.setWaiter(singleOrderFood.staff.name);
					item.setReason("");
					tpList.add(item);
				}
			}			
			list = tpList.toArray(new CancelledFood[tpList.size()]);
			if(orderBy == QueryCancelledFood.ORDER_BY_COUNT){
				Arrays.sort(list, new Comparator<CancelledFood>(){
					@Override
					public int compare(CancelledFood r1, CancelledFood r2) {
						if(r1.getCount() > r2.getCount()){
							return -1;
						}else if(r1.getCount() < r2.getCount()){
							return 1;
						}else{
							return 0;
						}
					}					
				});
			}else if(orderBy == QueryCancelledFood.ORDER_BY_PRICE){
				Arrays.sort(list, new Comparator<CancelledFood>(){
					@Override
					public int compare(CancelledFood r1, CancelledFood r2) {
						if(r1.getTotalPrice() > r2.getTotalPrice()){
							return -1;
						}else if(r1.getTotalPrice() < r2.getTotalPrice()){
							return 1;
						}else{
							return 0;
						}
					}					
				});
			}
			
		} catch(Exception e){
			System.out.println(e.getMessage());
		} finally{
			dbCon.disconnect();					
		}
		return list;
	}
	
	
}
