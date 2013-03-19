package com.wireless.db.regionMgr;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.system.Region;
import com.wireless.pojo.system.Table;

public class RegionDao {

	public static void updateRegion(Region region) throws Exception {
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
		} catch (Exception e) {
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}

	public static void updateTableInfo(Table table) throws Exception {
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
		} catch (Exception e) {
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}

	public static void insertTableInfo(Table table) throws Exception {
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			String insertTableSQL = "";
			insertTableSQL = "INSERT INTO "
					+ Params.dbName
					+ ".table (`table_alias`, `restaurant_id`, `name`, `region_id`, `minimum_cost`, `service_rate`) VALUES( "
					+ table.getTableAlias() + ", " + table.getRestaurantID()
					+ ", '" + table.getTableName() + "', "
					+ table.getRegion().getRegionID() + ", "
					+ table.getMimnmuCost() + "," + table.getServiceRate()
					+ " )";
			dbCon.stmt.execute(insertTableSQL);
			dbCon.conn.commit();// 提交；
		} catch (Exception e) {
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}

	public static void deleteTable4RowIndex(Table table) throws Exception {
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			String deleteTableSQL = "";
			deleteTableSQL = "DELETE FROM " + Params.dbName
					+ ".table WHERE restaurant_id=" + table.getRestaurantID()
					+ " AND table_id=" + table.getTableID() + "";
			dbCon.stmt.execute(deleteTableSQL);
			dbCon.conn.commit();
		} catch (Exception e) {
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
}
