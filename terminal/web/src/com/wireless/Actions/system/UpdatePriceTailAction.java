package com.wireless.Actions.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.system.SystemDao;
import com.wireless.pojo.system.SystemSetting;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdatePriceTailAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		SystemSetting set = null;
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String priceTail = request.getParameter("priceTail");
			String eraseQuota = request.getParameter("eraseQuota");
			
			if(restaurantID == null || restaurantID.trim().isEmpty()){
				jobject.initTip(false, "操作失败, 获取餐厅信息失败.");
				return null;
			}
			
			if( priceTail == null ||  priceTail.trim().isEmpty()){
				jobject.initTip(false, "操作失败, 获取收款金额尾数处理方式失败.");
				return null;
			}
			if( eraseQuota == null ||  eraseQuota.trim().isEmpty()){
				jobject.initTip(false, "操作失败, 获取抹数金额上限失败.");
				return null;
			}
			
			set = new SystemSetting();
			set.getSetting().setPriceTail(Integer.parseInt(priceTail));
			set.getSetting().setEraseQuota(Integer.parseInt(eraseQuota));
			set.setRestaurantID(Integer.parseInt(restaurantID));
			
			SystemDao.updatePriceTail(set);
			jobject.initTip(true, "操作成功, 已修改收款设置.");
			
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
		} finally {
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}
	
}
