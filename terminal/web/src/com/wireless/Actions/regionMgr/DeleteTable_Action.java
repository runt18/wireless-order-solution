package com.wireless.Actions.regionMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.pojo.system.Table;
import com.wireless.util.JObject;

public class DeleteTable_Action extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		response.setContentType("text/json; charset=utf-8");
		JObject jObject = new JObject();
		try {
			String tableID = request.getParameter("tableID");
			System.out.println(tableID);
			String restaurantID = request.getParameter("restaurantID");
			System.out.println(restaurantID);
			
			Table table = new Table();
			table.setTableID(Integer.valueOf(tableID));
			table.setRestaurantID(Integer.valueOf(restaurantID));
			
			RegionDao.deleteTable4RowIndex(table);
			jObject.initTip(true, "操作成功，已成功删除一个餐台啦！！");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JSONObject json = JSONObject.fromObject(jObject);
			response.getWriter().print(json);
		}
		return null;
	}

	public static void deleteTable4RowIndex(Table table) throws Exception{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			String deleteTableSQL = "";
			deleteTableSQL = "DELETE FROM "+Params.dbName+".table WHERE restaurant_id="+table.getRestaurantID()+" AND table_id="+table.getTableID()+"";
			dbCon.stmt.execute(deleteTableSQL);
			dbCon.conn.commit();
		} catch (Exception e) {
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
}
