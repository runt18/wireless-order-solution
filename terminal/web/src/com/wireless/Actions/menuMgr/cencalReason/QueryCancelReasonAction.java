package com.wireless.Actions.menuMgr.cencalReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.CancelReason;
import com.wireless.util.JObject;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryCancelReasonAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		List<CancelReason> list = new ArrayList<CancelReason>();
		JObject jobject = new JObject();
		try{
			String extra = "", orderBy = null;
			String restaurantID = request.getParameter("restaurantID");
			extra += (" AND A.restaurant_id = " + restaurantID);
			
			Map<String, Object> params = new HashMap<String, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, orderBy);
			list = MenuDao.getCancelReason(params);
			list = list != null ? list : new ArrayList<CancelReason>();
			list.add(0, new CancelReason(0, 1, "无原因"));
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.errCode, e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			jobject.setRoot(list);
			JSONObject centent = JSONObject.fromObject(jobject);
			response.getWriter().print(centent.toString());
		}
		return null;
	}

}
