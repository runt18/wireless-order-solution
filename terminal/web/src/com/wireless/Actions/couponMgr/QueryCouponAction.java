package com.wireless.Actions.couponMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.staffMgr.Staff;

public class QueryCouponAction extends DispatchAction{

/*	public ActionForward execute(ActionMapping mapping, ActionForm form,
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
		
	}*/
	public ActionForward byId(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String formId = request.getParameter("fid");
		
		int rid = 0;
		Staff staff;
		if(pin != null){
			staff = StaffDao.verify(Integer.parseInt(pin));
		}else{
			DBCon dbCon = new DBCon();
			dbCon.connect();
			rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
			staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
			dbCon.disconnect();
		}
		
		String couponId = request.getParameter("couponId");
		
		JObject jobject = new JObject();
		List<Coupon> list = new ArrayList<>();
		try{
			Coupon coupon = CouponDao.getById(staff, Integer.parseInt(couponId));
/*			String couponImg = "http://" + getServlet().getInitParameter("oss_bucket_image")
	        		+ "." + getServlet().getInitParameter("oss_outer_point") 
	        		+ "/" + staff.getRestaurantId() + "/" + coupon.getCouponType().getImage();
			coupon.getCouponType().setImage(couponImg);*/
			list.add(coupon);
			jobject.setRoot(list);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
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
