package com.wireless.Actions.menuMgr.pricePlan;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.util.WebParams;

public class InsertPricePlanAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = request.getParameter("restaurantID");
			String name = request.getParameter("name");
			String status = request.getParameter("status");
			PricePlan pricePlan = new PricePlan(0, name, PricePlan.Status.valueOf(Integer.valueOf(status)), Integer.valueOf(restaurantID));
			MenuDao.insertPricePlan(pricePlan);
			jobject.initTip(true, "操作成功, 已添加新价格方案信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
