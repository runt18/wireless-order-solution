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
			  " restaurant_id, supplier_id, food_id, price, date, staff, dept_id, amount, type" +
			  " FROM " + Params.dbName + ".material_detail MATE_DETAIL" +
			  " WHERE 1=1 "	+
			  (extraCond == null ? "" :  " " + extraCond) +
			  (orderClause == null ? "" : " " + orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<MaterialDetail> materialDetails = new ArrayList<MaterialDetail>();
		while (dbCon.rs.next()) {
			MaterialDetail materialDetail = new MaterialDetail();
			int restaurantID = dbCon.rs.getInt("restaurant_id");
			materialDetail.date = dbCon.rs.getTimestamp("date").getTime();
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
