package com.wireless.Actions.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.system.SystemSetting;
import com.wireless.util.WebParams;

public class QuerySystemSettingAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		JObject jobject = new JObject();
		SystemSetting set = null;
		try{
			
			String restaurantID = (String) request.getAttribute("restaurantID");
			if(restaurantID == null || restaurantID.trim().length() == 0){
				jobject.initTip(false, "操作失败,获取餐厅编号失败!");
				return null;
			}
			set = SystemDao.getSystemSettingById(Integer.parseInt(restaurantID));

			
			jobject.getOther().put("systemSetting", set);
		} catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
		} finally{			
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
