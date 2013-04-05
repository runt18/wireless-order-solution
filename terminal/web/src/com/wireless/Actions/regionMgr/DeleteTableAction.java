package com.wireless.Actions.regionMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.regionMgr.RegionDao;
import com.wireless.util.JObject;

public class DeleteTableAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		response.setContentType("text/json; charset=utf-8");
		JObject jObject = new JObject();
		try {
			String tableID = request.getParameter("tableID");
			String restaurantID = request.getParameter("restaurantID");

			RegionDao.deleteTable4RowIndex(Integer.parseInt(tableID),
					Integer.parseInt(restaurantID));
			
			jObject.initTip(true, "操作成功，已成功删除一个餐台啦！！");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JSONObject json = JSONObject.fromObject(jObject);
			response.getWriter().print(json);
		}
		return null;
	}
}