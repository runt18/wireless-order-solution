package com.wireless.Actions.inventoryMgr.inventoryOperation;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pack.ErrorCode;
import com.wireless.protocol.Terminal;

public class InventoryCheckAction extends Action {
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
			/*
			 * "materialID" : material, "price" : currPrice, "amount" :
			 * stockDiff, "staff" : staff, "type" : 7
			 */

			int materialID = Integer.parseInt(request
					.getParameter("materialID"));
			float currPrice = Float.parseFloat(request
					.getParameter("currPrice"));
			float checkPrice = Float.parseFloat(request
					.getParameter("checkPrice"));

			SimpleDateFormat tempDate = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss");
			String date = tempDate.format(new java.util.Date());

			String amountString = request.getParameter("amountInfo");

			String staff = request.getParameter("staff");
			String type = request.getParameter("type");

			// 庫存明細
			String sql = "";
			int sqlRowCount = 0;

			String[] deptAmounts = amountString.split("；");
			for (int i = 0; i < deptAmounts.length; i++) {
				String[] thisDeptAmount = deptAmounts[i].split(",");
				sql = "INSERT INTO "
						+ Params.dbName
						+ ".material_detail"
						+ "( restaurant_id, material_id, price, price_prev, date, amount, amount_prev, type, staff, dept_id ) "
						+ " VALUES("
						+ term.restaurantID
						+ ", "
						+ materialID
						+ ", "
						+ checkPrice
						+ ", "
						+ currPrice
						+ ", '"
						+ date
						+ "', "
						+ (float) Math.round((Float
								.parseFloat(thisDeptAmount[1]) - Float
								.parseFloat(thisDeptAmount[0])) * 100) / 100
						+ ", " + thisDeptAmount[0] + ", " + type + ", '"
						+ staff + "', " + i + " ) ";
				sqlRowCount = dbCon.stmt.executeUpdate(sql);
			}

			// 庫存現狀
			// 价格 = （库存量 × 价格 + 新入库数量 × 新入库价格）/ （库存量 + 新入库数量）
			// 更新價錢
			sql = "UPDATE " + Params.dbName + ".material_dept"
					+ " SET price = " + checkPrice + " WHERE restaurant_id = "
					+ term.restaurantID + " AND material_id =  " + materialID;
			sqlRowCount = dbCon.stmt.executeUpdate(sql);

			// 更新庫存量
			for (int i = 0; i < deptAmounts.length; i++) {
				String[] thisDeptAmount = deptAmounts[i].split(",");

				sql = "UPDATE " + Params.dbName + ".material_dept"
						+ " SET stock = " + thisDeptAmount[1]
						+ " WHERE restaurant_id = " + term.restaurantID
						+ " AND material_id =  " + materialID
						+ " AND dept_id = " + i + " ";
				sqlRowCount = dbCon.stmt.executeUpdate(sql);

			}

			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", "盘点成功！");

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
