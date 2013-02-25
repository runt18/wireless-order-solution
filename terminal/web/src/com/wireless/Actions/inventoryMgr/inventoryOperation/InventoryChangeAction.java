package com.wireless.Actions.inventoryMgr.inventoryOperation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.dbObject.MaterialDetail;
import com.wireless.exception.BusinessException;
import com.wireless.pack.ErrorCode;
import com.wireless.protocol.Terminal;

public class InventoryChangeAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. 1st example, filter the order
			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321 2nd
			 * example, filter the order date greater than or equal 2011-7-14
			 * 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
			 * 
			 * pin : the pin the this terminal supplierID: supplierName:
			 * supplierAddress: supplierContact: supplierPhone:
			 */

			String pin = request.getParameter("pin");
			
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// get the query condition
			int materialID = Integer.parseInt(request
					.getParameter("materialID"));
			String date = request.getParameter("date");
			int deptIDOut = Integer.parseInt(request.getParameter("deptIDOut"));
			int deptIDIn = Integer.parseInt(request.getParameter("deptIDIn"));
			float amount = Float.parseFloat(request.getParameter("amount"));
			String staff = request.getParameter("staff");
			String remark = request.getParameter("remark");

			// 調入
			String sql = "INSERT INTO "
					+ Params.dbName
					+ ".material_detail"
					+ "( restaurant_id, material_id, date, dept_id, dept2_id, amount, type, staff, comment ) "
					+ " VALUES(" + term.restaurantID + ", " + materialID
					+ ", '" + date + "', " + deptIDIn + ", " + deptIDOut + ", "
					+ amount + ", " + MaterialDetail.TYPE_IN + ", '" + staff
					+ "', '" + remark + "' ) ";
			int sqlRowCount = dbCon.stmt.executeUpdate(sql);

			sql = " SELECT stock FROM " + Params.dbName
					+ ".material_dept WHERE restaurant_id = "
					+ term.restaurantID + " AND material_id = " + materialID
					+ " AND dept_id =  " + deptIDIn;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			dbCon.rs.next();
			float thisStock = dbCon.rs.getFloat("stock");
			dbCon.rs.close();

			sql = "UPDATE " + Params.dbName + ".material_dept"
					+ " SET stock = "
					+ (float) Math.round((thisStock + amount) * 100) / 100
					+ " WHERE restaurant_id = " + term.restaurantID
					+ " AND material_id =  " + materialID + " AND dept_id =  "
					+ deptIDIn;
			sqlRowCount = dbCon.stmt.executeUpdate(sql);

			// 調出
			sql = "INSERT INTO "
					+ Params.dbName
					+ ".material_detail"
					+ "( restaurant_id, material_id, date, dept_id, dept2_id, amount, type, staff, comment ) "
					+ " VALUES(" + term.restaurantID + ", " + materialID
					+ ", '" + date + "', " + deptIDOut + ", " + deptIDIn + ", "
					+ amount * (-1) + ", " + MaterialDetail.TYPE_OUT + ", '"
					+ staff + "', '" + remark + "' ) ";
			sqlRowCount = dbCon.stmt.executeUpdate(sql);

			sql = " SELECT stock FROM " + Params.dbName
					+ ".material_dept WHERE restaurant_id = "
					+ term.restaurantID + " AND material_id = " + materialID
					+ " AND dept_id =  " + deptIDOut;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			dbCon.rs.next();
			thisStock = dbCon.rs.getFloat("stock");
			dbCon.rs.close();

			sql = "UPDATE " + Params.dbName + ".material_dept"
					+ " SET stock = "
					+ (float) Math.round((thisStock - amount) * 100) / 100
					+ " WHERE restaurant_id = " + term.restaurantID
					+ " AND material_id =  " + materialID + " AND dept_id =  "
					+ deptIDOut;
			sqlRowCount = dbCon.stmt.executeUpdate(sql);

			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", "调拨成功！");

			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");

			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				jsonResp = jsonResp.replace("$(value)", "终端已过期，请重新确认");

			} else {
				jsonResp = jsonResp.replace("$(value)", "未处理错误");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");

		} catch (IOException e) {
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");

		} finally {
			dbCon.disconnect();
			// just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}

		return null;
	}

}