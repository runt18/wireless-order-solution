package com.wireless.Actions.inventoryMgr.materialManagement;

import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.protocol.Terminal;
import com.wireless.util.BusinessUtil;

public class RestaurantInventory {

	public RestaurantInventory() {
	}

	public ArrayList getInventoryPoint(String in_pin, String in_datetime,
			String in_depts, String in_materialCates) {

		ArrayList outInvenList = new ArrayList();
		String sql = " ";

		String deptString = "";
		String cateString = "";
		String timeCondition = "";
		BusinessUtil businessUtil = new BusinessUtil();

		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(in_pin),
					Terminal.MODEL_STAFF);

			if (in_depts.equals("")) {
				ArrayList deptsList = businessUtil.getAllDepts(in_pin);
				for (int i = 0; i < deptsList.size(); i++) {
					deptString = deptString + deptsList.get(i) + ",";
				}
				deptString = deptString.substring(0, deptString.length() - 1);
			} else {
				deptString = in_depts;
			}

			if (in_materialCates.equals("")) {
				ArrayList cateList = businessUtil.getAllMaterialCate(in_pin);
				for (int i = 0; i < cateList.size(); i++) {
					cateString = cateString + cateList.get(i) + ",";
				}
				cateString = cateString.substring(0, cateString.length() - 1);
			} else {
				cateString = in_materialCates;
			}

			if (!in_datetime.equals("")) {
				timeCondition = " and a.date <= '" + in_datetime + "'";
			}

			sql = " SELECT SUM(a.amount) as count, a.material_id, c.name as material_name, b.cate_id, b.name as cate_name FROM "
					+ Params.dbName
					+ ".material_detail a, "
					+ Params.dbName
					+ ".material_cate b, "
					+ Params.dbName
					+ ".material c where c.restaurant_id = b.restaurant_id and c.cate_id = b.cate_id "
					+ " and a.restaurant_id = c.restaurant_id and a.material_id = c.material_id "
					+ " and a.restaurant_id = "
					+ term.restaurantID
					+ timeCondition
					+ " and a.dept_id in ("
					+ deptString
					+ ") and b.cate_id in ("
					+ cateString
					+ ") group by a.material_id, c.name, b.cate_id, b.name ";

			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while (dbCon.rs.next()) {
				ArrayList materialInvenList = new ArrayList();
				materialInvenList.add(dbCon.rs.getFloat("count"));
				materialInvenList.add(dbCon.rs.getInt("material_id"));
				materialInvenList.add(dbCon.rs.getString("material_name"));
				materialInvenList.add(dbCon.rs.getInt("cate_id"));
				materialInvenList.add(dbCon.rs.getString("cate_name"));
				outInvenList.add(materialInvenList);
			}

			dbCon.disconnect();

			return outInvenList;

		} catch (Exception e) {

			e.printStackTrace();
			return outInvenList;
		}
	}

	public ArrayList getInventoryPeriodDetail(String in_pin,
			String in_beginDatetime, String in_endDatetime, String in_depts,
			String in_materialCates, int in_type) {

		ArrayList outInvenList = new ArrayList();
		String sql = " ";

		String deptString = "";
		String cateString = "";
		String timeCondition = "";
		BusinessUtil businessUtil = new BusinessUtil();

		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(in_pin),
					Terminal.MODEL_STAFF);

			if (in_depts.equals("")) {
				ArrayList deptsList = businessUtil.getAllDepts(in_pin);
				for (int i = 0; i < deptsList.size(); i++) {
					deptString = deptString + deptsList.get(i) + ",";
				}
				deptString = deptString.substring(0, deptString.length() - 1);
			} else {
				deptString = in_depts;
			}

			if (in_materialCates.equals("")) {
				ArrayList cateList = businessUtil.getAllMaterialCate(in_pin);
				for (int i = 0; i < cateList.size(); i++) {
					cateString = cateString + cateList.get(i) + ",";
				}
				cateString = cateString.substring(0, cateString.length() - 1);
			} else {
				cateString = in_materialCates;
			}

			if (!in_beginDatetime.equals("")) {
				timeCondition = " and a.date > '" + in_beginDatetime + "' ";
			}
			if (!in_endDatetime.equals("")) {
				timeCondition = timeCondition + " and a.date <= '"
						+ in_endDatetime + "' ";
			}

			sql = " SELECT SUM(a.amount) as count, a.material_id, c.name as material_name, b.cate_id, b.name as cate_name FROM "
					+ Params.dbName
					+ ".material_detail a, "
					+ Params.dbName
					+ ".material_cate b, "
					+ Params.dbName
					+ ".material c where c.restaurant_id = b.restaurant_id and c.cate_id = b.cate_id "
					+ " and a.restaurant_id = c.restaurant_id and a.material_id = c.material_id "
					+ " and a.restaurant_id = "
					+ term.restaurantID
					+ timeCondition
					+ " and a.dept_id in ("
					+ deptString
					+ ") and b.cate_id in ("
					+ cateString
					+ ") and type = "
					+ in_type + " group by a.material_id, c.name, b.cate_id, b.name ";

			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while (dbCon.rs.next()) {
				ArrayList materialInvenList = new ArrayList();
				materialInvenList.add(dbCon.rs.getFloat("count"));
				materialInvenList.add(dbCon.rs.getInt("material_id"));
				materialInvenList.add(dbCon.rs.getString("material_name"));
				materialInvenList.add(dbCon.rs.getInt("cate_id"));
				materialInvenList.add(dbCon.rs.getString("cate_name"));
				outInvenList.add(materialInvenList);
			}

			dbCon.disconnect();

			return outInvenList;

		} catch (Exception e) {

			e.printStackTrace();
			return outInvenList;
		}
	}
}
