package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.system.Region;
import com.wireless.pojo.system.Table;
import com.wireless.protocol.Terminal;

public class RegionDao {

	/**
	 * 修改区域
	 * 
	 * @param region
	 * @throws Exception
	 */
	public static void updateRegion(Region region) throws SQLException,
			BusinessException {
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			String updateRegionSQL = "";
			updateRegionSQL = "UPDATE " + Params.dbName + ".region "
					+ " SET name = '" + region.getRegionName() + "'"
					+ " WHERE restaurant_id=" + region.getRestaurantID()
					+ " AND region_id = " + region.getRegionID();
			if (dbCon.stmt.executeUpdate(updateRegionSQL) == 0) {
				throw new BusinessException("操作失败，修改区域信息失败了！！");
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
			String updateTableSQL = "";
			updateTableSQL = "UPDATE " + Params.dbName + ".table SET name = '"
					+ table.getTableName() + "',region_id = '"
					+ table.getRegion().getRegionID() + "',minimum_cost = '"
					+ table.getMimnmuCost() + "',service_rate = '"
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
					+ table.getRegion().getRegionID() + ", "
					+ table.getMimnmuCost() + "," + table.getServiceRate()
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
	public static List<Region> exec(DBCon dbCon, Terminal term,
			String extraCond, String orderClause) throws SQLException {
		List<Region> regions = new ArrayList<Region>();
		try {
			dbCon.connect();

			String sql = " SELECT " + " region_id, restaurant_id, name "
					+ " FROM " + Params.dbName + ".region "
					+ " WHERE restaurant_id = " + term.restaurantID
					+ (extraCond == null ? "" : extraCond)
					+ (orderClause == null ? "" : orderClause);
			dbCon.rs = dbCon.stmt.executeQuery(sql);

			while (dbCon.rs.next()) {
				regions.add(new Region(dbCon.rs.getShort("region_id"), dbCon.rs
						.getString("name"), dbCon.rs.getInt("restaurant_id")));
			}
			return regions;
		} catch (SQLException e) {
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
}