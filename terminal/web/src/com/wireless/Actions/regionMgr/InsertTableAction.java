package com.wireless.Actions.regionMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.system.Terminal;
import com.wireless.util.JObject;

public class InsertTableAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		response.setContentType("text/json; charset=utf-8");
		JObject jObject = new JObject();

		try {
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			String tableAddName = request.getParameter("tableAddName");
			String tableAddAlias = request.getParameter("tableAilas");
			String tableAddMincost = request.getParameter("tableAddMincost");
			String tableAddSerRate = request.getParameter("tableAddSerRate");
			String regionId = request.getParameter("regionID"); 

			Table.InsertBuilder builder = new Table.InsertBuilder(Integer.parseInt(tableAddAlias), Integer.parseInt(restaurantID), Short.parseShort(regionId))
												.setMiniCost(Integer.valueOf(tableAddMincost))
												.setServiceRate(Float.valueOf(tableAddSerRate))
												.setTableName(tableAddName);
												 
			TableDao.insert(VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF), builder);

			jObject.initTip(true, "操作成功，已成功插入餐台信息啦！！");

		} catch (Exception e) {
			jObject.initTip(false, e.getMessage());
			
		} finally {
			JSONObject json = JSONObject.fromObject(jObject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
}