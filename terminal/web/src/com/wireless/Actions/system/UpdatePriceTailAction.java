package com.wireless.Actions.system;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.system.SystemSetting;

public class UpdatePriceTailAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		SystemSetting set = null;
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = (String) request.getAttribute("restaurantID");
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
			set.getSetting().setPriceTail(Setting.Tail.valueOf(Integer.parseInt(priceTail)));
			set.getSetting().setEraseQuota(Integer.parseInt(eraseQuota));
			set.setRestaurantID(Integer.parseInt(restaurantID));
			
			SystemDao.updatePriceTail(set);
			jobject.initTip(true, "操作成功, 已修改收款设置.");
			
		} catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
}
