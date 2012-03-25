package com.wireless.Actions.menuMgr;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import com.wireless.util.Util;

public class UpdateMenuAction extends Action {
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
			 * pin : the pin the this terminal
			 * dishNumber: 
			 * dishName:
			 * dishSpill:
			 * dishPrice:
			 * kitchen:
			 * isSpecial :  
			 * isRecommend : 
			 * isFree :   
			 * isStop : 
			 * 
			 */
			byte SPECIAL = 0x01;	/* 特价 */
			byte RECOMMEND = 0x02;	/* 推荐 */ 
			byte SELL_OUT = 0x04;	/* 售完 */
			byte GIFT = 0x08;		/* 赠送 */
			byte CUR_PRICE = 0x10;	/* 时价 */
			
			String pin = request.getParameter("pin");
			
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);
			

			// get the query condition
			int foodID = Integer.parseInt(request.getParameter("foodID"));
			String dishName = request.getParameter("dishName");
			String dishSpill = request.getParameter("dishSpill");
			float dishPrice = Float.parseFloat(request.getParameter("dishPrice"));
			int kitchen = Integer.parseInt(request.getParameter("kitchen"));
			
			String isSpecial = request.getParameter("isSpecial");
			String isRecommend = request.getParameter("isRecommend");
			String isFree = request.getParameter("isFree");
			String isStop = request.getParameter("isStop");
			String isCurrPrice = request.getParameter("isCurrPrice");
			

			
			/**
			 * 
			 */
			int status = 0x00;
			if(isSpecial.equals("true")){
				status |= SPECIAL;
			};
			if(isRecommend.equals("true")){
				status |= RECOMMEND;
			};
			if(isStop.equals("true")){
				status |= SELL_OUT;
			};
			if(isFree.equals("true")){ 
				status |= GIFT;
			};
			if(isCurrPrice.equals("true")){ 
				status |= CUR_PRICE;
			};
			
			String sql = "UPDATE " + Params.dbName + ".food " +
					" SET name = '" + dishName + "', " + 
					" pinyin = '"+ dishSpill + "', " + 
					" unit_price =  " + dishPrice + ", " + 
					" kitchen = " + kitchen + ", " + 
					" status =  " + status + 
					" WHERE restaurant_id=" + term.restaurant_id
					+ " AND food_id = " + foodID;

			int sqlRowCount = dbCon.stmt.executeUpdate(sql);

			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", "菜品修改成功！");
			
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
