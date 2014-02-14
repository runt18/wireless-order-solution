package com.wireless.Actions.menuMgr.cencalReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.util.SQLUtil;

public class QueryCancelReasonAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		List<CancelReason> list = new ArrayList<CancelReason>();
		JObject jobject = new JObject();
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String extra = "", orderBy = null;
			String restaurantID = (String) request.getAttribute("restaurantID");
			extra += (" AND A.restaurant_id = " + restaurantID);
			
			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, orderBy);
			list = MenuDao.getCancelReason(params);
			list = list != null ? list : new ArrayList<CancelReason>();
			list.add(0, new CancelReason(1, "无原因", 0));
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			jobject.setRoot(list);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
