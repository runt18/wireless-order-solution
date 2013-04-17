package com.wireless.Actions.regionMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.system.Terminal;
import com.wireless.util.JObject;

public class DeleteTableAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		response.setContentType("text/json; charset=utf-8");
		JObject jObject = new JObject();
		
		try {
			String pin = request.getParameter("pin");
			String tableID = request.getParameter("tableID");

			TableDao.deleteById(VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF), Integer.valueOf(tableID));
			
			jObject.initTip(true, "操作成功，已成功删除一个餐台啦！！");

		} catch (BusinessException e) {
			jObject.initTip(false, e.getMessage());
			
		} catch (SQLException e){
			jObject.initTip(false, e.getMessage());
			
		}finally {
			JSONObject json = JSONObject.fromObject(jObject);
			response.getWriter().print(json);
		}
		return null;
	}
}