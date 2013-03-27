package com.wireless.Actions.tableMgr;

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
import com.wireless.exception.ProtocolError;
import com.wireless.protocol.Terminal;

public class UpdateTableAction extends Action {
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
			 * pin : the pin the this terminal modKitchens:
			 * 修改記錄格式:id{field_separator
			 * }name{field_separator}phone{field_separator
			 * }contact{field_separator
			 * }address{record_separator}id{field_separator
			 * }name{field_separator}
			 * phone{field_separator}contact{field_separator}address
			 * 
			 */

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// get parameter
			String modTables = request.getParameter("modTables");

			/**
			 * 
			 */
			String[] tables = modTables.split(" record_separator ");
			for (int i = 0; i < tables.length; i++) {

				String[] fieldValues = tables[i].split(" field_separator ");

				String sql = "UPDATE " + Params.dbName + ".table "
						+ " SET name = '" + fieldValues[1] + "', "
						+ " region_id = " + fieldValues[2] + ", "
						+ " minimum_cost = " + fieldValues[3] + ", "
						+ " service_rate = " + fieldValues[4] + " "
						+ " WHERE restaurant_id=" + term.restaurantID
						+ " AND table_id = " + fieldValues[0];

				dbCon.stmt.executeUpdate(sql);
			}

			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", "餐台修改成功！");

			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			if (e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED) {
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");

			} else if (e.getErrCode() == ProtocolError.TERMINAL_EXPIRED) {
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
			// System.out.println(jsonResp);
			out.write(jsonResp);
		}

		return null;
	}

}
