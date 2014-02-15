package com.wireless.Actions.distMgr.discountPlan;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class UpdateDiscountPlanRateAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String discountID = request.getParameter("discountID");
			String rate = request.getParameter("rate");
			
			
			Discount.UpdatePlanBuilder builder = new Discount.UpdatePlanBuilder(Integer.parseInt(discountID));
			
			for (Kitchen k : KitchenDao.getByType(staff, Kitchen.Type.NORMAL)) {
				builder.add(k, Float.parseFloat(rate));
			}
			
			DiscountDao.updatePlan(staff, builder);
			
			jobject.initTip(true, "操作成功, 已修改该方案下所有分厨折扣信息.");
		
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
