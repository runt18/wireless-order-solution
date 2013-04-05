package com.wireless.Actions.tasteMgr;

import java.io.PrintWriter;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;

public class UpdateTasteAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
//-----------------------------------old code and old requirement start-------------------------------------
//		DBCon dbCon = new DBCon();
//
//		String jsonResp = "{success:$(result), data:'$(value)'}";
//		PrintWriter out = null;
//		try {
//			// 解决后台中文传到前台乱码
//			response.setContentType("text/json; charset=utf-8");
//			out = response.getWriter();
//
//			/**
//			 * The parameters looks like below. 1st example, filter the order
//			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321 2nd
//			 * example, filter the order date greater than or equal 2011-7-14
//			 * 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
//			 * 
//			 * pin : the pin the this terminal modKitchens:
//			 * 修改記錄格式:id{field_separator
//			 * }name{field_separator}phone{field_separator
//			 * }contact{field_separator
//			 * }address{record_separator}id{field_separator
//			 * }name{field_separator}
//			 * phone{field_separator}contact{field_separator}address
//			 * 
//			 */
//
//			String pin = request.getParameter("pin");
//			
//			dbCon.connect();
//			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
//					Terminal.MODEL_STAFF);
//
//			// get parameter
//			String modTastes = request.getParameter("modTastes");
//
//			/**
//			 * 
//			 */
//			String[] tastes = modTastes.split(" record_separator ");
//			for (int i = 0; i < tastes.length; i++) {
//
//				String[] fieldValues = tastes[i].split(" field_separator ");
//
//				String sql = "UPDATE " + Params.dbName + ".taste "
//						+ " SET preference = '" + fieldValues[1] + "', "
//						+ " price = " + fieldValues[2] + ", " + " rate = "
//						+ fieldValues[3] + ", " + " calc = " + fieldValues[4]
//						+ ", " + " category = " + fieldValues[5] + " "
//						+ " WHERE restaurant_id=" + term.restaurantID
//						+ " AND taste_id = " + fieldValues[0];
//
//				dbCon.stmt.executeUpdate(sql);
//			}
//
//			jsonResp = jsonResp.replace("$(result)", "true");
//			jsonResp = jsonResp.replace("$(value)", "口味修改成功！");
//
//			dbCon.rs.close();
//
//		} catch (BusinessException e) {
//			e.printStackTrace();
//			jsonResp = jsonResp.replace("$(result)", "false");
//			if (e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED) {
//				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
//
//			} else if (e.getErrCode() == ProtocolError.TERMINAL_EXPIRED) {
//				jsonResp = jsonResp.replace("$(value)", "终端已过期，请重新确认");
//
//			} else {
//				jsonResp = jsonResp.replace("$(value)", "未处理错误");
//			}
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//			jsonResp = jsonResp.replace("$(result)", "false");
//			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//			jsonResp = jsonResp.replace("$(result)", "false");
//			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
//
//		} finally {
//			dbCon.disconnect();
//			// just for debug
//			//System.out.println(jsonResp);
//			out.write(jsonResp);
//		}
//
//		return null;
//-----------------------------------old code and old requirement end-------------------------------------
		DBCon dbCon = new DBCon();
		String restaurantID = request.getParameter("restaurantID");//餐厅ID
		String pin = request.getParameter("pin");//获取pin值
		PrintWriter out = null;
		response.setContentType("text/json; charset=utf-8");
		out = response.getWriter();
		String modTastes = request.getParameter("modTastes");
		String[] records = modTastes.split("record_separator");//记录
		dbCon.connect();
		for(int i = 0;i < records.length; i++){
			String[] fields = records[i].split("field_separator");
			String tID = fields[0];
			String tName = fields[1];
			String tPrice = fields[2];
			String tRate = fields[3];
			String tCalc = fields[4];
			String tCategory = fields[5];
			String sql = "UPDATE taste SET taste.preference = '"+tName+"',taste.price = "+tPrice+",taste.category = '"+tCategory+"',taste.rate = "+tRate+",taste.calc = "+tCalc+" WHERE taste.taste_id = "+tID+";";
			dbCon.stmt.addBatch(sql);
		}
		dbCon.stmt.executeBatch();
		dbCon.disconnect();
		JSONObject json = new JSONObject();
		json.put("success", true);
		json.put("message","操作成功!");
		out.write(json.toString());
		out.flush();
		out.close();
		return null;
	}

}
