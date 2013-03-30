package com.wireless.Actions.tasteMgr;

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
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.protocol.Terminal;

public class InsertTasteAction extends Action {
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
			 * pin : the pin the this terminal tableNumber tableName region
			 * tableMincost
			 * 
			 */

			String pin = request.getParameter("pin");
			
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			/**
			 * “口味分类”的值如下： 0 - 口味 ， 1 - 做法， 2 - 规格 “计算方式”的值如下：0 - 按价格，1 - 按比例
			 */
			// get the query condition
			String tasteNumber = request.getParameter("tasteNumber");
			String tasteName = request.getParameter("tasteName");
			String tastePrice = request.getParameter("tastePrice");
			String tasteRate = request.getParameter("tasteRate");
//			String cal = request.getParameter("cal");
			String type = request.getParameter("type");
			int cal = 0;
			
			if(type != null && Integer.parseInt(type) == 0){
				cal = 0;
			}else if(type != null && Integer.parseInt(type) == 2){
				cal = 1;
			}
			
			/**
			 * 
			 */
			String sql = "INSERT INTO "
					+ Params.dbName + ".taste"
					+ "( restaurant_id, taste_alias, preference, price, category, rate, calc ) "
					+ " VALUES(" 
					+ term.restaurantID + ", " 
					+ tasteNumber + ", "
					+ "'" + tasteName + "', "
					+ (tastePrice == null ? 0.00 : Float.parseFloat(tastePrice)) + ", " 
					+ (type == null ? 0 : Integer.parseInt(type)) + "," 
					+ (tasteRate == null ? 0.00 : Float.parseFloat(tasteRate)) + "," 
					+ cal 
					+ " ) ";

			dbCon.stmt.executeUpdate(sql);

			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", "添加新口味成功！");

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
