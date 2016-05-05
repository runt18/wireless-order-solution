package com.wireless.Actions.dishesOrder;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.book.BookDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;

public class QueryWxOrderAction extends DispatchAction {
	
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String orderId = request.getParameter("orderId");
		final String code = request.getParameter("code");
		final String id = request.getParameter("id");
		final String detail = request.getParameter("detail");
		final String status = request.getParameter("status");
		
		JObject jObject = new JObject();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final WxOrderDao.ExtraCond extraCond = new WxOrderDao.ExtraCond();
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			
			if(orderId != null && !orderId.isEmpty()){
				extraCond.setOrder(Integer.parseInt(orderId));
			}
			
			if(code != null && !code.isEmpty()){
				extraCond.setCode(Integer.parseInt(code));
			}
			
			if(status != null && !status.isEmpty()){
				for(String eachStatus : status.split(",")){
					extraCond.addStatus(WxOrder.Status.valueOf(Integer.parseInt(eachStatus)));
				}
			}
			
			if(detail != null && !detail.isEmpty()){
				if(Boolean.parseBoolean(detail)){
					List<WxOrder> result = WxOrderDao.getByCond(staff, extraCond, null);
					for(int i = 0; i < result.size(); i++){
						result.set(i, WxOrderDao.getById(staff, result.get(i).getId()));
					}
					jObject.setRoot(result);
				}
			}else{
				jObject.setRoot(WxOrderDao.getByCond(staff, extraCond, null));
			}
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	public ActionForward delectById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		JObject jobject = new JObject();		
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		try{
			WxOrderDao.deleteById(staff, Integer.parseInt(id));
			jobject.initTip(true, "删除成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}		
		
		return null;
	}
	
}
