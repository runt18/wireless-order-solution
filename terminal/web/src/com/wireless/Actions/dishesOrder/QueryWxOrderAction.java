package com.wireless.Actions.dishesOrder;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

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
}
