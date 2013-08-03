package com.wireless.Actions.staffMgr;

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
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.Util;

public class InsertStaffAction extends Action {
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
			 * "pin" : pin, "staffNumber" : staffAddNumber, "staffName" :
			 * staffAddName, "staffPwd" : staffAddPwd, "staffQuota" :
			 * staffAddQuota
			 * 
			 */

			String pin = request.getParameter("pin");

			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			// get the query condition
			int staffNumber = Integer.parseInt(request
					.getParameter("staffNumber"));
			String staffName = request.getParameter("staffName");
			String staffPwd = request.getParameter("staffPwd");
			String pwdMd5 = Util.getMD5Str(staffPwd);
			float staffQuota = Float.parseFloat(request
					.getParameter("staffQuota"));

			// 造pin
			long newPin = Util.getNewPIN(dbCon);

			/**
			 * 
			 */
			// 插terminal
			String sql = "INSERT INTO "
					+ Params.dbName
					+ ".terminal"
					+ "( pin, restaurant_id, model_id, model_name, owner_name, gift_quota ) "
					+ " VALUES(" + newPin + ", " + staff.getRestaurantId()
					+ ", 255, 'Staff', '" + staffName + "', " + staffQuota
					+ " ) ";

			dbCon.stmt.executeUpdate(sql);

			sql = " SELECT terminal_id FROM " + Params.dbName
					+ ".terminal WHERE model_id = 255 AND pin = " + newPin;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			dbCon.rs.next();
			int newTerminalID = dbCon.rs.getInt(1);

			// 插staff
			sql = "INSERT INTO "
					+ Params.dbName
					+ ".staff"
					+ "(  restaurant_id, terminal_id, staff_alias, name, pwd ) "
					+ " VALUES(" + staff.getRestaurantId() + ", " + newTerminalID
					+ ", " + staffNumber + ", '" + staffName + "', '" + pwdMd5
					+ "' ) ";

			dbCon.stmt.executeUpdate(sql);

			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", "添加新员工成功！");

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
