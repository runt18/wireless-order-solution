package com.wireless.util;

import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.protocol.Terminal;

public class BusinessUtil {
	public BusinessUtil() {

	}

	public ArrayList getAllDepts(String in_pin) {
		ArrayList outInvenList = new ArrayList();
		String sql = " ";

		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(in_pin),
					Terminal.MODEL_STAFF);

			sql = " select dept_id from  " + Params.dbName
					+ ".department where restaurant_id = " + term.restaurant_id;

			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while (dbCon.rs.next()) {
				outInvenList.add(dbCon.rs.getInt("dept_id"));
			}

			dbCon.disconnect();

			return outInvenList;
		} catch (Exception e) {
			e.printStackTrace();
			return outInvenList;
		}
	}

	public ArrayList getAllMaterialCate(String in_pin) {
		ArrayList outInvenList = new ArrayList();
		String sql = " ";

		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(in_pin),
					Terminal.MODEL_STAFF);

			sql = " select cate_id from  " + Params.dbName
					+ ".material_cate where restaurant_id = "
					+ term.restaurant_id;

			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while (dbCon.rs.next()) {
				outInvenList.add(dbCon.rs.getInt("cate_id"));
			}

			dbCon.disconnect();

			return outInvenList;
		} catch (Exception e) {
			e.printStackTrace();
			return outInvenList;
		}
	}

}
