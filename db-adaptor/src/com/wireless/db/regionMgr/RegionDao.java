package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.util.SQLUtil;

public class RegionDao {

	/**
	 * Update a specified region.
	 * @param region
	 * 			the region to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			if the region to update does NOT exist
	 */
	public static void update(Region region) throws SQLException, BusinessException {
		DBCon dbCon = new DBCon();
		try {
			
			dbCon.connect();
			update(dbCon, region);
			
		} finally {
			dbCon.disconnect();
		}
	}

	/**
	 * Update a specified region.
	 * @param dbCon
	 * 			the database connection
	 * @param region
	 * 			the region to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			if the region to update does NOT exist
	 */
	public static void update(DBCon dbCon, Region region) throws SQLException, BusinessException{
		
		String sql;
		
		sql = " UPDATE " + Params.dbName + ".region " + 
			  " SET name = '" + region.getName() + "'" +
			  " WHERE restaurant_id=" + region.getRestaurantId() +
			  " AND region_id = " + region.getId();
		
		if (dbCon.stmt.executeUpdate(sql) == 0) {
			throw new BusinessException("操作失败，修改区域信息失败了！！");
		}
	}
	
	/**
	 * Get the region according to specified condition.
	 * @param params
	 * 			the extra condition defined in a map
	 * @return the region list holding result
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 */
	public static List<Region> getRegion(DBCon dbCon, Map<Object, Object> params) throws SQLException {
		List<Region> list = new ArrayList<Region>();
		String querySQL = " SELECT A.region_id, A.restaurant_id, A.name " +
						  " FROM " + Params.dbName + ".region A" +
						  " WHERE 1 = 1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);	
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while (dbCon.rs.next()) {
			list.add(new Region(dbCon.rs.getShort("region_id"), 
								dbCon.rs.getString("name"), 
								dbCon.rs.getInt("restaurant_id"))
			);
		}
		
		dbCon.rs.close();
		
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<Region> getRegion(Map<Object, Object> params) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return RegionDao.getRegion(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * @deprecated
	 * 修改餐台信息
	 * 
	 * @param table
	 * @throws Exception
	 */
	public static void updateTableInfo(Table table) throws SQLException,
			BusinessException {
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			String updateTableSQL;
			updateTableSQL = "UPDATE " + Params.dbName + ".table SET name = '"
					+ table.getTableName() + "',region_id = '"
					+ table.getRegion().getId() + "',minimum_cost = '"
					+ table.getMinimumCost() + "',service_rate = '"
					+ table.getServiceRate() + "' WHERE restaurant_id="
					+ table.getRestaurantID() + " AND table_id = "
					+ table.getTableID();
			if (dbCon.stmt.executeUpdate(updateTableSQL) == 0) {
				throw new BusinessException("操作失败，修改餐台信息失败了！！");
			}
			dbCon.conn.commit();// 提交；
		} catch (SQLException e) {
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}

	/**
	 * @deprecated
	 * 插入餐台信息
	 * 
	 * @param table
	 * @throws Exception
	 */
	public static void insertTableInfo(Table table) throws SQLException {
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			String insertTableSQL = "";
			insertTableSQL = "INSERT INTO "
					+ Params.dbName
					+ ".table (`table_alias`, `restaurant_id`, `name`, `region_id`, `minimum_cost`, `service_rate`) VALUES( "
					+ table.getTableID() + ", " + table.getRestaurantID()
					+ ", '" + table.getTableName() + "', "
					+ table.getRegion().getId() + ", "
					+ table.getMinimumCost() + "," + table.getServiceRate()
					+ " )";
			dbCon.stmt.execute(insertTableSQL);
			dbCon.conn.commit();// 提交；
		} catch (SQLException e) {
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}

	/**
	 * @deprecated
	 * 根据table_id来删除餐台
	 * 
	 * @param tID
	 *            餐台ID
	 * 
	 * @param rID
	 *            餐馆ID
	 * 
	 * @throws Exception
	 */
	public static void deleteTable4RowIndex(int tID, int rID)
			throws SQLException {
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			String deleteTableSQL = "";
			deleteTableSQL = "DELETE FROM " + Params.dbName
					+ ".table WHERE restaurant_id=" + rID + " AND table_id="
					+ tID + "";
			dbCon.stmt.execute(deleteTableSQL);
			dbCon.conn.commit();
		} catch (SQLException e) {
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}

	/**
	 * 查询区域信息
	 * 
	 * @param dbCon
	 * @param term
	 * @param extraCond
	 * @param orderClause
	 * @return regions
	 * @throws Exception
	 */
//	public static List<Region> exec(DBCon dbCon, Terminal term,
//			String extraCond, String orderClause) throws SQLException {
//		List<Region> regions = new ArrayList<Region>();
//		try {
//			dbCon.connect();
//
//			String sql = " SELECT " + " region_id, restaurant_id, name "
//					+ " FROM " + Params.dbName + ".region "
//					+ " WHERE restaurant_id = " + term.restaurantID
//					+ (extraCond == null ? "" : extraCond)
//					+ (orderClause == null ? "" : orderClause);
//			dbCon.rs = dbCon.stmt.executeQuery(sql);
//
//			while (dbCon.rs.next()) {
//				regions.add(new Region(dbCon.rs.getShort("region_id"), dbCon.rs
//						.getString("name"), dbCon.rs.getInt("restaurant_id")));
//			}
//			return regions;
//		} catch (SQLException e) {
//			throw e;
//		} finally {
//			dbCon.disconnect();
//		}
//	}
	

}