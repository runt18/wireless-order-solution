package com.wireless.Actions.menuMgr.material;

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

public class DeleteFoodMaterialAction extends Action {
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
			 * pin : the pin the this terminal dishNumber:
			 * 
			 */
			String pin = request.getParameter("pin");
			
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// get the query condition
			int foodID = Integer.parseInt(request.getParameter("foodID"));
			int materialID = Integer.parseInt(request
					.getParameter("materialID"));

			/**
			 * 
			 */
			String sql = "DELETE FROM " + Params.dbName + ".food_material "
					+ "WHERE restaurant_id=" + term.restaurantID
					+ " AND food_id = " + foodID + " AND material_id = "
					+ materialID;
			;

			dbCon.stmt.executeUpdate(sql);

			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", "关联食材删除成功！");

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
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}

		return null;
	}

}
