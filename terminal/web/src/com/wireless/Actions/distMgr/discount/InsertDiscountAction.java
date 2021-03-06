package com.wireless.Actions.distMgr.discount;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.distMgr.Discount;

public class InsertDiscountAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			
			String discountName = request.getParameter("discountName");
			String rate = request.getParameter("rate");
			String isDefault = request.getParameter("isDefault");
			
			Discount.InsertBuilder builder = new Discount.InsertBuilder(discountName)
												.setRate(Float.valueOf(rate));
			
			if(isDefault != null && !isDefault.isEmpty()){
				builder.setDefault();
			}
			
			DiscountDao.insert(StaffDao.verify(Integer.parseInt(pin)), builder);
			
			jobject.initTip(true,  "操作成功, 已添加新折扣方案!");
			
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
