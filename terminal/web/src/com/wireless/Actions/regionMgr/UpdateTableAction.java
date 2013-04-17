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

public class UpdateTableAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setContentType("text/json; charset=utf-8");
		
		JObject jObject = new JObject();

		try {

			String pin = request.getParameter("pin");
			String tableID = request.getParameter("tableID");
			String tableName = request.getParameter("tableName");
			String tableRegion = request.getParameter("tableRegion");
			String tableMincost = request.getParameter("tableMincost");
			String tableServiceRate = request.getParameter("tableServiceRate");

			Table.UpdateBuilder builder = new Table.UpdateBuilder(Integer.valueOf(tableID)).setMiniCost(Integer.valueOf(tableMincost))
																						   .setRegionId(Short.valueOf(tableRegion))
																						   .setServiceRate(Float.valueOf(tableServiceRate))
																						   .setTableName(tableName);
			
			TableDao.updateById(VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF), builder.build());

			jObject.initTip(true, "操作成功，已成功修改餐台信息啦！！");

		} catch (Exception e) {
			jObject.initTip(false, e.getMessage());
			
		} finally {
			JSONObject json = JSONObject.fromObject(jObject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
}