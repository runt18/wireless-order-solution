package com.wireless.db.tasteRef;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.util.SortedList;

public class TasteRefDao {
	
	private static class TasteRefCnt implements Comparable<TasteRefCnt>{
		
		private static enum GroupCate{
			BY_FOOD(1, "菜品关联"),
			BY_KITCHEN(2, "厨房关联"),
			BY_DEPT(3, "部门关联");
			
			private final int val;
			private final String desc;
			
			GroupCate(int val, String desc){
				this.val = val;
				this.desc = desc;
			}
			
			public int compare(GroupCate o) {
				if(val < o.val){
					return -1;
				}else if(val > o.val){
					return 1;
				}else{
					return 0;
				}
			}
			
			@Override
			public String toString(){
				return desc;
			}
		}
		
		private final int tasteId;
		private final GroupCate groupCate;
		private final int refCnt;
		
		public TasteRefCnt(int tasteId, GroupCate groupCate, int refCnt){
			this.tasteId = tasteId;
			this.groupCate = groupCate;
			this.refCnt = refCnt;
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj == null || !(obj instanceof TasteRefCnt)){
				return false;
			}else{
				return tasteId == ((TasteRefCnt)obj).tasteId;
			}
		}
		
		@Override
		public int hashCode(){
			return tasteId * 31 + 17;
		}

		/**
		 * The rule to sort the taste reference count as below.
		 * If the category is the same, the more the reference count, the higher the rank.
		 * If the category is NOT the same, the level is as below.
		 * TASTE_BY_FOOD > TASTE_BY_KITCHEN > TASTE_BY_DEPT
		 */
		@Override
		public int compareTo(TasteRefCnt o) {
			int group = groupCate.compare(o.groupCate);
			if(group == 0){
				if(refCnt > o.refCnt){
					return -1;
				}else if(refCnt < o.refCnt){
					return 1;
				}else{
					return 0;
				}
			}else{
				return group;
			}
		}
	}
	
	private final static int TASTE_REF_NUM = 10;
	
	public static final class Result{
		private final int elapsed;
		Result(int elapsed){
			this.elapsed = elapsed;
		}
		@Override
		public String toString(){
			return "The calculation to smart taste reference takes " + elapsed + " sec.";
		}
	}
	
	/**
	 * Calculate the taste reference count to all the foods with smart taste reference.
	 * @return the result to smart taste reference calculation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */	
	public static Result exec() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the taste reference count to all the foods with smart taste reference.
	 * @param dbCon
	 * 			the database connection
	 * @return the result to smart taste reference calculation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Result exec(DBCon dbCon) throws SQLException{
		
		long beginTime = System.currentTimeMillis();

		String sql;
		
		//Delete all the kitchen taste before updating.
		sql = " DELETE FROM " + Params.dbName + ".food_taste";
		dbCon.stmt.executeUpdate(sql);
		
		//Select and insert the food taste.
		sql = 	" INSERT INTO " + Params.dbName + ".food_taste " +
				" (food_id, taste_id, restaurant_id, ref_cnt) " +
				" SELECT A.food_id, A.taste_id, MAX(restaurant_id) AS restaurant_id, COUNT(*) AS ref_cnt " +
				" FROM " +
				" ( " +
				" SELECT " + 
				" OFH.order_id, OFH.food_id, NTGH.taste_id, MAX(OFH.restaurant_id) AS restaurant_id " +
				" FROM " + Params.dbName + ".normal_taste_group_history NTGH " +
				" JOIN " + Params.dbName + ".taste_group_history TGH ON NTGH.normal_taste_group_id = TGH.normal_taste_group_id " + " AND NTGH.normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID +
				" JOIN " + Params.dbName + ".order_food_history OFH ON TGH.taste_group_id = OFH.taste_group_id " + " AND OFH.is_temporary = 0 " + " AND TGH.taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
				" JOIN " + Params.dbName + ".order_history OH ON OFH.order_id = OH.id AND OH.order_date BETWEEN DATE_SUB(NOW(), INTERVAL 90 DAY) AND NOW() " +
				" JOIN " + Params.dbName + ".food F ON F.food_id = OFH.food_id " +
				" GROUP BY OFH.order_id, OFH.food_id, NTGH.taste_id " +
				" HAVING SUM(OFH.order_count) > 0 " +
				" ) AS A " +
				" GROUP BY A.food_id, A.taste_id ";
		dbCon.stmt.executeUpdate(sql);
		
		//Delete all the kitchen taste before updating.
		sql = " DELETE FROM " + Params.dbName + ".kitchen_taste";
		dbCon.stmt.executeUpdate(sql);
		
		//Update the kitchen taste
		sql = " INSERT INTO " + Params.dbName + ".kitchen_taste" +
			  " (kitchen_id, taste_id, restaurant_id, ref_cnt) " +
			  " SELECT FOOD.kitchen_id, FOOD_TASTE.taste_id, MAX(FOOD.restaurant_id), SUM(FOOD_TASTE.ref_cnt) " +
			  " FROM " + 
			  Params.dbName + ".food_taste FOOD_TASTE, " +
			  Params.dbName + ".food FOOD " +
			  " WHERE " +
			  " FOOD_TASTE.food_id = FOOD.food_id " +
			  " GROUP BY FOOD.kitchen_id, FOOD_TASTE.taste_id ";
		dbCon.stmt.executeUpdate(sql);
		
		//Delete all the department taste before updating.
		sql = " DELETE FROM " + Params.dbName + ".dept_taste";
		dbCon.stmt.executeUpdate(sql);
		
		//Update the department taste
		sql = " INSERT INTO " + Params.dbName + ".dept_taste" +
			  " (dept_id, taste_id, restaurant_id, ref_cnt) " +
			  " SELECT KITCHEN.dept_id, KITCHEN_TASTE.taste_id, MAX(KITCHEN.restaurant_id), SUM(KITCHEN_TASTE.ref_cnt) " +
			  " FROM " + 
			  Params.dbName + ".kitchen_taste KITCHEN_TASTE, " +
			  Params.dbName + ".kitchen KITCHEN " +
			  " WHERE " +
			  " KITCHEN_TASTE.kitchen_id = KITCHEN.kitchen_id " +
			  " GROUP BY KITCHEN.dept_id, KITCHEN_TASTE.taste_id ";
		dbCon.stmt.executeUpdate(sql);		
		
		Map<Food, List<TasteRefCnt>> result = new HashMap<>();
		
		//Get the result from food taste.
		sql = " SELECT * FROM " + Params.dbName + ".food_taste";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Food f = new Food(dbCon.rs.getInt("food_id"));
			f.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			List<TasteRefCnt> associatedTaste = result.get(f);
			if(associatedTaste != null){
				associatedTaste.add(new TasteRefCnt(dbCon.rs.getInt("taste_id"), TasteRefCnt.GroupCate.BY_FOOD, dbCon.rs.getInt("ref_cnt")));
			}else{
				List<TasteRefCnt> tasteRefCnt = SortedList.newInstance();
				tasteRefCnt.add(new TasteRefCnt(dbCon.rs.getInt("taste_id"), TasteRefCnt.GroupCate.BY_FOOD, dbCon.rs.getInt("ref_cnt")));
				result.put(f, tasteRefCnt);
			}
		}
		dbCon.rs.close();
		
		for(Map.Entry<Food, List<TasteRefCnt>> entry : result.entrySet()){
			
			StringBuilder excludeTaste = new StringBuilder();
			excludeTaste.append(Integer.MIN_VALUE);
			for(TasteRefCnt refCnt : entry.getValue()){
				excludeTaste.append("," + refCnt.tasteId);
			}
			
			//Get the result from kitchen taste
			sql = " SELECT * " +
				  " FROM " + Params.dbName + ".kitchen_taste KT " +
				  " JOIN " + Params.dbName + ".food F " +
				  " ON F.kitchen_id = KT.kitchen_id " + 
				  " AND F.food_id = " + entry.getKey().getFoodId() +
				  " AND KT.taste_id NOT IN (" + excludeTaste + ")";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				entry.getValue().add(new TasteRefCnt(dbCon.rs.getInt("taste_id"), TasteRefCnt.GroupCate.BY_KITCHEN, dbCon.rs.getInt("ref_cnt")));
				excludeTaste.append("," + dbCon.rs.getInt("taste_id"));
			}
			dbCon.rs.close();
			
			//Get the result from department taste
			sql = " SELECT * " +
				  " FROM " + Params.dbName + ".dept_taste DT " +
				  " JOIN " + Params.dbName + ".food F " +
				  " JOIN " + Params.dbName + ".kitchen K " +
				  " ON K.dept_id = DT.dept_id AND K.restaurant_id = DT.restaurant_id " +
				  " AND K.kitchen_id = F.kitchen_id " +
				  " AND F.food_id = " + entry.getKey().getFoodId() +
  				  " AND DT.taste_id NOT IN (" + excludeTaste + ")";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				entry.getValue().add(new TasteRefCnt(dbCon.rs.getInt("taste_id"), TasteRefCnt.GroupCate.BY_DEPT, dbCon.rs.getInt("ref_cnt")));
			}
			dbCon.rs.close();
		}
		
		//Delete the original food taste rank record.
		sql = " DELETE FROM " + Params.dbName + ".food_taste_rank";
		dbCon.stmt.executeUpdate(sql);
	
		dbCon.stmt.clearBatch();
		for(Map.Entry<Food, List<TasteRefCnt>> entry : result.entrySet()){
			int tasteRank = 1;
			for(TasteRefCnt refCnt : entry.getValue()){				
				if(tasteRank <= TASTE_REF_NUM){
					sql =  " INSERT INTO " + Params.dbName + ".food_taste_rank" +
							" (food_id, taste_id, restaurant_id, rank) " +
							" VALUES(" +
							entry.getKey().getFoodId() + ", " +
							refCnt.tasteId + ", " +
							entry.getKey().getRestaurantId() + ", " +
							tasteRank++ + ")";
					dbCon.stmt.addBatch(sql);
				}else{
					break;
				}
			}
		}
		dbCon.stmt.executeBatch();
		
		return new Result((int)(System.currentTimeMillis() - beginTime) / 1000);
	}
	
	
}

