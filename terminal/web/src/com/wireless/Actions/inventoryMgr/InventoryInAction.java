package com.wireless.Actions.inventoryMgr;

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
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

public class InventoryInAction extends Action {
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
			if (pin.startsWith("0x") || pin.startsWith("0X")) {
				pin = pin.substring(2);
			}
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Integer.parseInt(pin, 16),
					Terminal.MODEL_STAFF);

			// get the query condition

			int supplierID = Integer.parseInt(request
					.getParameter("supplierID"));
			int materialID = Integer.parseInt(request
					.getParameter("materialID"));
			float price = Float.parseFloat(request.getParameter("price"));
			String date = request.getParameter("date");
			int deptID = Integer.parseInt(request.getParameter("deptID"));
			float amount = Float.parseFloat(request.getParameter("amount"));
			String staff = request.getParameter("staff");
			String type = request.getParameter("type");

			// 庫存明細
			String sql = "INSERT INTO "
					+ Params.dbName
					+ ".material_detail"
					+ "( restaurant_id, supplier_id, material_id, price, date, dept_id, amount, type, staff ) "
					+ " VALUES(" + term.restaurant_id + ", " + supplierID
					+ ", " + materialID + ", " + price + ", '" + date + "', "
					+ deptID + ", " + amount + ", " + type + ", '" + staff
					+ "' ) ";
			int sqlRowCount = dbCon.stmt.executeUpdate(sql);

			// 庫存現狀
			// 价格 = （库存量 × 价格 + 新入库数量 × 新入库价格）/ （库存量 + 新入库数量）
			// 更新價錢
			sql = " SELECT sum(stock) AS stock, max(price) AS price FROM "
					+ Params.dbName + ".material_dept WHERE restaurant_id = "
					+ term.restaurant_id + " AND material_id = " + materialID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			dbCon.rs.next();
			float totalStock = dbCon.rs.getFloat("stock");
			float thisPrice = dbCon.rs.getFloat("price");
			dbCon.rs.close();

			float allPrice = (float) Math
					.round((totalStock * thisPrice + amount * price) * 100) / 100;
			float allStock = (float) Math.round((totalStock + amount) * 100) / 100;
			float newPrice = (float) Math.round((allPrice / allStock) * 100) / 100;
			System.out.println("totalStock: " + totalStock
					+ "   ,  thisPrice: " + thisPrice + "  , allPrice:"
					+ allPrice + "  , :allStock" + allStock + "  , newPrice"
					+ newPrice + "  , ");

			sql = "UPDATE " + Params.dbName + ".material_dept"
					+ " SET price = " + newPrice + " WHERE restaurant_id = "
					+ term.restaurant_id + " AND material_id =  " + materialID;
			sqlRowCount = dbCon.stmt.executeUpdate(sql);

			// 更新庫存量
			sql = " SELECT stock FROM " + Params.dbName
					+ ".material_dept WHERE restaurant_id = "
					+ term.restaurant_id + " AND material_id = " + materialID
					+ " AND dept_id =  " + deptID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			dbCon.rs.next();
			float thisStock = dbCon.rs.getFloat("stock");
			dbCon.rs.close();

			sql = "UPDATE " + Params.dbName + ".material_dept"
					+ " SET stock = "
					+ (float) Math.round((thisStock + amount) * 100) / 100
					+ " WHERE restaurant_id = " + term.restaurant_id
					+ " AND material_id =  " + materialID + " AND dept_id =  "
					+ deptID;
			sqlRowCount = dbCon.stmt.executeUpdate(sql);

			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", "入库成功！");

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
			System.out.println(jsonResp);
			out.write(jsonResp);
		}

		return null;
	}

}