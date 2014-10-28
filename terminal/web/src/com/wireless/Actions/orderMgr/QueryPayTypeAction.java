package com.wireless.Actions.orderMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.orderMgr.PayTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.dishesOrder.PayType;

public class QueryPayTypeAction extends DispatchAction {
	
	public ActionForward allPayType(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		try{
			List<PayType> list = PayTypeDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), null);
			jobject.setRoot(list);
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
	
	public ActionForward exceptMember(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		try{
			List<PayType> list = PayTypeDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), new PayTypeDao.ExtraCond()
																							.addType(PayType.Type.DESIGNED)
																							.addType(PayType.Type.EXTRA));
			jobject.setRoot(list);
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
	
	public ActionForward onlyExtra(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		try{
			List<PayType> list = PayTypeDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), new PayTypeDao.ExtraCond()
																							.addType(PayType.Type.EXTRA));
			jobject.setRoot(list);
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
