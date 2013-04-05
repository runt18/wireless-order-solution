package com.wireless.Actions.regionMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.regionMgr.RegionDao;
import com.wireless.pojo.system.Region;
import com.wireless.pojo.system.Table;
import com.wireless.util.JObject;

public class InsertTableAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		response.setContentType("text/json; charset=utf-8");
		JObject jObject = new JObject();

		try {

			String restaurantID = request.getParameter("restaurantID");
			String tableAddNumber = request.getParameter("tableAddNumber");
			String tableAddName = request.getParameter("tableAddName");
			String tableAddAilas = request.getParameter("tableAddAilas");
			String tableAddMincost = request.getParameter("tableAddMincost");
			String tableAddSerRate = request.getParameter("tableAddSerRate");

			Table table = new Table();
			table.setRestaurantID(Integer.valueOf(restaurantID));
			table.setTableID(Integer.valueOf(tableAddNumber));

			Region region = new Region();
			table.setRegion(region);

			table.getRegion().setRegionID(Integer.valueOf(tableAddAilas));
			table.setTableName(tableAddName);
			table.setMimnmuCost(Float.valueOf(tableAddMincost));

			table.setServiceRate(Float.valueOf(tableAddSerRate));// ????

			RegionDao.insertTableInfo(table);

			jObject.initTip(true, "操作成功，已成功插入餐台信息啦！！");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JSONObject json = JSONObject.fromObject(jObject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
}