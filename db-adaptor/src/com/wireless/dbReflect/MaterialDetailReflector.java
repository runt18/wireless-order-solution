package com.wireless.dbReflect;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.dbObject.MaterialDetail;

/**
 * The DB reflector is designed to the bridge between the MaterialDetail instance of
 * protocol and database.
 * 
 * @author Ying.Zhang 
 */
public class MaterialDetailReflector {
	/**
	 * Create the material details from database table 'mateial_detail' according an extra
	 * condition. Note that the database should be connected before invoking
	 * this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the material detail
	 * @param orderClause
	 *            the order clause to search the material detail
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static MaterialDetail[] getMaterialDetail(DBCon dbCon, String extraCond,	String orderClause) throws SQLException {
		String sql;
		sql = " SELECT " +
			  " MATE_DETAIL.restaurant_id AS restaurant_id, MATE_DETAIL.supplier_id AS supplier_id, MATE_DETAIL.food_id AS food_id, " +
			  " MATE_DETAIL.price AS price, MATE_DETAIL.date AS date, MATE_DETAIL.staff AS staff, MATE_DETAIL.dept_id AS dept_id, " +
			  " MATE_DETAIL.amount AS amount, MATE_DETAIL.type AS type, " +
			  " FOOD.name AS food_name, FOOD.food_alias AS food_alias" +
			  " FROM " + Params.dbName + ".material_detail MATE_DETAIL" +
			  " LEFT OUTER JOIN " + Params.dbName + ".food FOOD " +
			  " ON MATE_DETAIL.food_id = FOOD.food_id " +  
			  " WHERE 1=1 "	+
			  (extraCond == null ? "" :  " " + extraCond) +
			  (orderClause == null ? "" : " " + orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<MaterialDetail> materialDetails = new ArrayList<MaterialDetail>();
		while (dbCon.rs.next()) {
			MaterialDetail materialDetail = new MaterialDetail();
			int restaurantID = dbCon.rs.getInt("restaurant_id");
			materialDetail.date = dbCon.rs.getTimestamp("date").getTime();
			
			materialDetail.food.setRestaurantId(restaurantID);
			materialDetail.food.setAliasId(dbCon.rs.getInt("food_alias"));
			materialDetail.food.setName(dbCon.rs.getString("food_name"));
			
			materialDetail.dept.restaurantID = restaurantID;
			materialDetail.dept.deptID = dbCon.rs.getShort("dept_id");
			
			materialDetail.staff = dbCon.rs.getString("staff");
			materialDetail.amount = dbCon.rs.getFloat("amount");
			materialDetail.price = dbCon.rs.getFloat("price");
			materialDetail.type = dbCon.rs.getInt("type");
			
			materialDetails.add(materialDetail);
		}
		
		return materialDetails.toArray(new MaterialDetail[materialDetails.size()]);
	}
}
