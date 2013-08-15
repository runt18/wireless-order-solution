package com.wireless.Actions.passwordConfig;

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

public class SetPasswordAction extends Action {
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
			 * pin :
			 * 
			 */

			String pin = (String) request.getSession().getAttribute("pin");

			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			// get parameter
			String adminPwd = request.getParameter("adminPwd");
			String financePwd = request.getParameter("financePwd");
			String managerPwd = request.getParameter("managerPwd");
			String cashierPwd = request.getParameter("cashierPwd");
			String orderCancelPwd = request.getParameter("orderCancelPwd");

			String sql = "";
			if (!adminPwd.equals("<special_message:not_change>")) {
				if (adminPwd.equals("")) {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd = '' WHERE id=" + staff.getRestaurantId();
				} else {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd = md5('" + adminPwd + "') WHERE id="
							+ staff.getRestaurantId();
				}
				dbCon.stmt.executeUpdate(sql);
			}
			
			
			if (!financePwd.equals("<special_message:not_change>")) {
				if (financePwd.equals("")) {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd2 = '' WHERE id=" + staff.getRestaurantId();
				} else {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd2 = md5('" + financePwd + "') WHERE id="
							+ staff.getRestaurantId();
				}
				dbCon.stmt.executeUpdate(sql);
			}
			
			
			if (!managerPwd.equals("<special_message:not_change>")) {
				if (managerPwd.equals("")) {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd3 = '' WHERE id=" + staff.getRestaurantId();
				} else {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd3 = md5('" + managerPwd + "') WHERE id="
							+ staff.getRestaurantId();
				}
				dbCon.stmt.executeUpdate(sql);
			}
			
			
			if (!cashierPwd.equals("<special_message:not_change>")) {
				if (cashierPwd.equals("")) {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd4 = '' WHERE id=" + staff.getRestaurantId();
				} else {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd4 = md5('" + cashierPwd + "') WHERE id="
							+ staff.getRestaurantId();
				}
				dbCon.stmt.executeUpdate(sql);
			}
			
			
			if (!orderCancelPwd.equals("<special_message:not_change>")) {
				if (orderCancelPwd.equals("")) {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd5 = '' WHERE id=" + staff.getRestaurantId();
				} else {
					sql = "UPDATE " + Params.dbName + ".restaurant "
							+ " SET pwd5 = md5('" + orderCancelPwd + "') WHERE id="
							+ staff.getRestaurantId();
				}
				dbCon.stmt.executeUpdate(sql);
			}
						

			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", "保存密码成功！");

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