package com.wireless.Actions.menuMgr.basic;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;

public class DeleteMenuAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jobject = new JObject();
		
		try {			
			
			final String pin = (String)request.getAttribute("pin");
			final String foodID = request.getParameter("foodID");
			final String branchId = request.getParameter("branchId");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			FoodDao.deleteById(staff, Integer.parseInt(foodID));
			
			jobject.initTip(true, "操作成功, 已删除菜品信息.");
			try{
				//TODO Delete the image from OSS
			}catch(Exception e){
				jobject.setMsg(jobject.getMsg() + "但删除菜品图片失败.");
			}
		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}

		return null;
	}
	
	
}
