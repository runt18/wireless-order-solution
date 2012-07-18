package com.wireless.db.billStatistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import sun.jdbc.odbc.OdbcDef;

import com.wireless.db.DBCon;
import com.wireless.db.QueryMenu;
import com.wireless.db.VerifyPin;
import com.wireless.dbObject.SingleOrderFood;
import com.wireless.dbReflect.SingleOrderFoodReflector;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.BackFood;
import com.wireless.protocol.Department;
import com.wireless.protocol.Terminal;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryBackFood {
	
	public final static int QUERY_BY_DEPT = 0;		//按部门显示
	public final static int QUERY_BY_REASON = 1;	//按原因显示
	public final static int QUERY_BY_FOOD = 2;		//按菜品显示	
	
	public final static int ORDER_BY_COUNT = 0;		//按数量排序
	public final static int ORDER_BY_PRICE = 1;		//按金额排序
	
	/**
	 * 
	 * @param pin
	 * @param queryDate
	 * @param orderBy
	 * @return
	 */
	public static BackFood[] getBackFoodByDept(long pin, DutyRange queryDate, int orderBy){
		BackFood[] list = null;
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
			
			SingleOrderFood[] orderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, 
					"AND B.restaurant_id=" + term.restaurantID + " " + 
					"AND B.order_date BETWEEN '" + dutyRange.getOnDuty() + "' AND '" + dutyRange.getOffDuty() + "'", 
					null);		
			
			/**
			 * Get all the basic info to department 
			 */
			HashMap<Department, BackFood> deptBackFoodDetail = new HashMap<Department, BackFood>();
			Department[] departmentDetail = QueryMenu.queryDepartments(dbCon, term.restaurantID, null, null);
			for(Department dept : departmentDetail){
				deptBackFoodDetail.put(dept, new BackFood(dept.name, ""));
			}
			
			/**
			 * Put the temporary department
			 */
			deptBackFoodDetail.put(new Department("临时菜", Department.DEPT_TEMP, term.restaurantID), new BackFood("临时菜", ""));
			
			/**
			 * Calculate the orderCount to each department during this period
			 */
			for(SingleOrderFood singleOrderFood : orderFoods){
				BackFood bfDetail = deptBackFoodDetail.get(singleOrderFood.kitchen.dept);
				if(bfDetail != null && singleOrderFood.orderCount < 0){
					bfDetail.setDeptID(singleOrderFood.kitchen.dept.deptID);
					bfDetail.setCount(Math.abs(bfDetail.getCount()) + Math.abs(singleOrderFood.orderCount));
//					bfDetail.setPrice(singleOrderFood.unitPrice);
					bfDetail.setTotalPrice(bfDetail.getTotalPrice() + Math.abs(singleOrderFood.orderCount) * singleOrderFood.unitPrice);
					deptBackFoodDetail.put(singleOrderFood.kitchen.dept, bfDetail);
				}
			}
			
			Iterator iter = deptBackFoodDetail.entrySet().iterator();
			while(iter.hasNext()){
				Entry entry = (Entry)iter.next();
				BackFood val = (BackFood)entry.getValue();
				if(val.getCount() <= 0){
					iter.remove();
				}
			}
			
			list = deptBackFoodDetail.values().toArray(new BackFood[deptBackFoodDetail.values().size()]);
			
			if(orderBy == QueryBackFood.ORDER_BY_COUNT){
				Arrays.sort(list, new Comparator<BackFood>(){
					@Override
					public int compare(BackFood r1, BackFood r2) {
						if(r1.getCount() > r2.getCount()){
							return -1;
						}else if(r1.getCount() < r2.getCount()){
							return 1;
						}else{
							return 0;
						}
					}					
				});
			}else if(orderBy == QueryBackFood.ORDER_BY_PRICE){
				Arrays.sort(list, new Comparator<BackFood>(){
					@Override
					public int compare(BackFood r1, BackFood r2) {
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
			
			
		} catch(Exception e) {
			System.out.println(e.getMessage());
		} finally{
			dbCon.disconnect();
		}
		
		return list;
	}
	
	/**
	 * 
	 * @param pin
	 * @param duty
	 * @param orderBy
	 * @param deptID
	 * @return
	 */
	public static BackFood[] getBackFood(long pin, DutyRange queryDate, int orderBy, String deptID){
		BackFood[] list = null;
		
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
			
			List<BackFood> tpList = new ArrayList();
			BackFood item = null;
			for(int i = 0; i < orderFoods.length; i++){
				SingleOrderFood singleOrderFood = orderFoods[i];
				if(singleOrderFood.orderCount <= 0){
					item = new BackFood();
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
			list = tpList.toArray(new BackFood[tpList.size()]);
			if(orderBy == QueryBackFood.ORDER_BY_COUNT){
				Arrays.sort(list, new Comparator<BackFood>(){
					@Override
					public int compare(BackFood r1, BackFood r2) {
						if(r1.getCount() > r2.getCount()){
							return -1;
						}else if(r1.getCount() < r2.getCount()){
							return 1;
						}else{
							return 0;
						}
					}					
				});
			}else if(orderBy == QueryBackFood.ORDER_BY_PRICE){
				Arrays.sort(list, new Comparator<BackFood>(){
					@Override
					public int compare(BackFood r1, BackFood r2) {
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
