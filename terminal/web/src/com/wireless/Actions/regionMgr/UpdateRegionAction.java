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
import com.wireless.util.JObject;

public class UpdateRegionAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		response.setContentType("text/json; charset=utf-8");
		JObject jObject = new JObject();

		try {

			String restaurantID = request.getParameter("restaurantID");
			String regionID = request.getParameter("regionID");
			String regionName = request.getParameter("regionName");

			Region region = new Region();
			region.setRestaurantID(Integer.parseInt(restaurantID));
			region.setRegionID(Integer.parseInt(regionID));
			region.setRegionName(regionName);

			RegionDao.updateRegion(region);

			jObject.initTip(true, "操作成功，已成功修改区域信息啦！！");
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JSONObject json = JSONObject.fromObject(jObject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
}
