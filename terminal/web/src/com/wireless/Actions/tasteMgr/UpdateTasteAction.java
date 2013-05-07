package com.wireless.Actions.tasteMgr;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.protocol.Terminal;

public class UpdateTasteAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		JSONObject json = new JSONObject();
		try{
			response.setContentType("text/json; charset=utf-8");
			PrintWriter out = null;
			out = response.getWriter();
			String restaurantID = request.getParameter("restaurantID");//餐厅ID
			String pin = request.getParameter("pin");//获取pin值
			String modTastes = request.getParameter("modTastes");
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			restaurantID = term.restaurantID+"";
			dbCon.disconnect();
//			boolean success = TasteRefDao.update(term,modTastes);
//			JSONObject json = new JSONObject();
//			if(success){
//				json.put("success", success);
//				json.put("message","操作成功!");
//			}
//			else{
//				json.put("success", success);
//				json.put("message","操作失败!");
//			}
//			out.write(json.toString());
			String[] records = modTastes.split("record_separator");//记录
			dbCon.connect();
			for(int i = 0;i < records.length; i++){
				String[] fields = records[i].split("field_separator");
				String tID = fields[0];
				String tAlias = fields[1];
				String tName = fields[2];
				String tPrice = fields[3];
				String tRate = fields[4];
				String tCategory = fields[5];
				String sql = "UPDATE "+Params.dbName+".taste SET "+Params.dbName+".taste.preference = '"+tName+"',"+Params.dbName+".taste.rate="+tRate+","+Params.dbName+".taste.calc = "+tCategory+","+Params.dbName+".taste.price = "+tPrice+","+Params.dbName+".taste.taste_alias = "+tAlias+" WHERE "+Params.dbName+".taste.restaurant_id = "+term.restaurantID+" AND "+Params.dbName+".taste.taste_id = "+tID;
				dbCon.stmt.addBatch(sql);
			}
			dbCon.stmt.executeBatch();
			out.flush();
			out.close();
			json.put("success", true);
			json.put("message","操作成功!");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(json.toString());
		}
		return null;
	}

}
