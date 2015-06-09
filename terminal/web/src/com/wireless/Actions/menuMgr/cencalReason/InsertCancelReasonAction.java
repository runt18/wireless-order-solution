package com.wireless.Actions.menuMgr.cencalReason;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.crMgr.CancelReason.InsertBuilder;
import com.wireless.pojo.staffMgr.Staff;

public class InsertCancelReasonAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String reason = request.getParameter("reason");
			CancelReason.InsertBuilder  builder = new InsertBuilder(staff.getRestaurantId(), reason);
			CancelReasonDao.insert(staff, builder);
			jobject.initTip(true, "操作成功, 已添加退菜原因信息.");
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
