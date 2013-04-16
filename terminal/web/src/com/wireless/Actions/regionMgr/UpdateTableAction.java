package com.wireless.Actions.regionMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.util.JObject;

public class UpdateTableAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		DBCon dbCon = new DBCon();
		response.setContentType("text/json; charset=utf-8");
		JObject jObject = new JObject();

		try {

			String restaurantID = request.getParameter("restaurantID");
			String tableID = request.getParameter("tableID");
			String tableName = request.getParameter("tableName");
			String tableRegion = request.getParameter("tableRegion");
			String tableMincost = request.getParameter("tableMincost");
			String tableServiceRate = request.getParameter("tableServiceRate");

			Table table = new Table();
			table.setRestaurantID(Integer.valueOf(restaurantID));
			table.setTableID(Integer.valueOf(tableID));
			table.setTableName(tableName.trim());
			table.setMimnmuCost(Float.valueOf(tableMincost));
			table.setServiceRate(Float.valueOf(tableServiceRate));
			

			Region region = new Region();// 一定要实例化；否则会出现NullPointExection异常的；
			table.setRegion(region);
			table.getRegion().setId(Short.valueOf(tableRegion));

			TableDao.updateById(null, table);

			jObject.initTip(true, "操作成功，已成功修改餐台信息啦！！");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JSONObject json = JSONObject.fromObject(jObject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
}