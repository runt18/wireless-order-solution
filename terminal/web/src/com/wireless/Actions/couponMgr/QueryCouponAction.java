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
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.Promotion;
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
	
	public ActionForward defaultCoupon(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String formId = request.getParameter("fid");
		String openId = request.getParameter("oid");
				
		int rid = 0;
		int mid = 0;
		DBCon dbCon = new DBCon();
		dbCon.connect();
		rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
		Staff staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
		mid = WeixinMemberDao.getBoundMemberIdByWeixin(dbCon, openId, formId);
		dbCon.disconnect();
		
		
		JObject jobject = new JObject();
		List<Coupon> list = new ArrayList<>();
		try{
			CouponDao.ExtraCond extra = new ExtraCond();
			extra.setMember(mid);
			extra.addPromotionStatus(Promotion.Status.PROGRESS);
			extra.addPromotionStatus(Promotion.Status.PUBLISH);
			
			List<Coupon> coupons = CouponDao.getByCond(staff, extra, null);
			if(!coupons.isEmpty()){
				Coupon coupon = coupons.get(0);
				if(coupon.getCouponType().getImage() != null){
					coupon.getCouponType().setImage(OssImageDao.getById(staff, coupon.getCouponType().getImage().getId()));
				} 
				list.add(coupon);
			}
			jobject.setRoot(list);
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
	
	
	public ActionForward byCondtion(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String formId = request.getParameter("fid");
		String openId = request.getParameter("oid");
		String pId = request.getParameter("pId");
		
		int rid = 0;
		int mid = 0;
		DBCon dbCon = new DBCon();
		dbCon.connect();
		rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
		Staff staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
		mid = WeixinMemberDao.getBoundMemberIdByWeixin(dbCon, openId, formId);
		dbCon.disconnect();
		
		
		JObject jobject = new JObject();
		try{
			CouponDao.ExtraCond extra = new CouponDao.ExtraCond();
			extra.setMember(mid);
			extra.setPromotion(Integer.parseInt(pId));
			extra.setStatus(Coupon.Status.PUBLISHED);
			
			List<Coupon> list = CouponDao.getByCond(staff, extra, null);
			jobject.setRoot(list);
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
