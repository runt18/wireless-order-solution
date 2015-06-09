package com.wireless.Actions.smsMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.sms.SMStatDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.sms.SMSDetail;
import com.wireless.pojo.sms.SMStat;
import com.wireless.pojo.staffMgr.Staff;

public class OperateSmsAction extends DispatchAction {
	
	/**
	 * 餐厅增加短信条数
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String count = request.getParameter("count");
		String restaurantId = request.getParameter("restaurantId");
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			Staff mStaff = StaffDao.verify(Integer.parseInt(pin));
			mStaff.setRestaurantId(Integer.parseInt(restaurantId));
			SMStatDao.update(mStaff, 
					new SMStat.UpdateBuilder(mStaff.getRestaurantId(), SMSDetail.Operation.ADD).setAmount(Integer.parseInt(count)));
			jobject.initTip(true, "增加短信成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
	
	/**
	 * 餐厅减少短信条数
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward reduce(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String count = request.getParameter("count");
		String restaurantId = request.getParameter("restaurantId");
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			Staff mStaff = StaffDao.verify(Integer.parseInt(pin));
			mStaff.setRestaurantId(Integer.parseInt(restaurantId));
			SMStatDao.update(mStaff,
					new SMStat.UpdateBuilder(mStaff.getRestaurantId(), SMSDetail.Operation.DEDUCT).setAmount(Integer.parseInt(count)));
			jobject.initTip(true, "减少短信成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
