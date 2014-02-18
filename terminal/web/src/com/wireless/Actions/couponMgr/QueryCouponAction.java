package com.wireless.Actions.couponMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.coupon.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.coupon.Coupon;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class QueryCouponAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String pin = (String) request.getAttribute("pin");
		String typeId = request.getParameter("couponTypeId");
		String status = request.getParameter("status");
		String memberName = request.getParameter("memberName");
		String memberMobile = request.getParameter("memberMobile");
		
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		JObject jobject = new JObject();
		List<Coupon> list = null;
		String extra = "";
		try{
			if(typeId != null && !typeId.isEmpty() && !typeId.equals("-1")){
				extra += " AND C.coupon_type_id = " + typeId;
			}
			if(status != null && !status.isEmpty()){
				extra += " AND C.status = " + status;
			}
			if(memberMobile != null && !memberMobile.isEmpty()){
				extra += " AND M.mobile like '%" + memberMobile + "%' ";
			}
			if(memberName != null && !memberName.isEmpty()){
				extra += " AND M.name like '%" + memberName + "%' ";
			}
			
			list = CouponDao.getByCond(staff, extra, null);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				list = DataPaging.getPagingData(list, isPaging, start, limit);
				jobject.setRoot(list);
			}
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
}
