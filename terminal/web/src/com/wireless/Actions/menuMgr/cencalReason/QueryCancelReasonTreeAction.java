package com.wireless.Actions.menuMgr.cencalReason;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;

public class QueryCancelReasonTreeAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		List<CancelReason> list = new ArrayList<CancelReason>();
		CancelReason item = null;
		StringBuffer jsb = new StringBuffer();
		try{
			
			String pin = (String)request.getAttribute("pin");
			list = CancelReasonDao.get(StaffDao.verify(Integer.parseInt(pin)));
			
			// 系统默认
			list.add(0, new CancelReason(1, "无原因", 0));
			
			for (int i = 0; i < list.size(); i++) {
				item = list.get(i);
				jsb.append(i > 0 ? "," : "");
				jsb.append("{");
				jsb.append("text:'" + item.getReason() + "'");
				jsb.append(",reasonID:" + item.getId());
				jsb.append(",restaurantID:'" + item.getRestaurantID() + "'");
				jsb.append(",leaf:true");
				jsb.append("}");
			}
		}catch(BusinessException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + jsb.toString() + "]");
		}
		return null;
	}

}
