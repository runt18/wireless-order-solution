package com.wireless.Actions.weixin.query;

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
import com.wireless.db.member.MemberDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponDao.DrawType;
import com.wireless.db.promotion.CouponDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.staffMgr.Staff;

public class WXQueryCouponAction extends DispatchAction{

	public ActionForward byId(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		String pin = (String) request.getAttribute("pin");
		String formId = request.getParameter("fid");
		
		int rid = 0;
		Staff staff;
		if(pin != null){
			staff = StaffDao.verify(Integer.parseInt(pin));
		}else{
			rid = WxRestaurantDao.getRestaurantIdByWeixin(formId);
			staff = StaffDao.getAdminByRestaurant(rid);
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
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward defaultCoupon(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String formId = request.getParameter("fid");
		String openId = request.getParameter("oid");
				
		int rid = 0;
		rid = WxRestaurantDao.getRestaurantIdByWeixin(formId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
		
		JObject jobject = new JObject();
		List<Coupon> list = new ArrayList<>();
		try{
			CouponDao.ExtraCond extra = new ExtraCond();
			extra.setMember(MemberDao.getByWxSerial(staff, openId));
			extra.addPromotionStatus(Promotion.Status.PROGRESS);
			extra.addPromotionStatus(Promotion.Status.CREATED);
			
			List<Coupon> coupons = CouponDao.getByCond(staff, extra, null);
			if(!coupons.isEmpty()){
				list.add(CouponDao.getById(staff, coupons.get(0).getId()));
			}
			jobject.setRoot(list);
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
	
	
	public ActionForward byCondtion(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String formId = request.getParameter("fid");
		String openId = request.getParameter("oid");
		String pId = request.getParameter("pId");
		
		int rid = 0;
		rid = WxRestaurantDao.getRestaurantIdByWeixin(formId);
		Staff staff = StaffDao.getAdminByRestaurant(rid);
		
		
		JObject jobject = new JObject();
		try{
			CouponDao.ExtraCond extra = new CouponDao.ExtraCond();
			extra.setMember(MemberDao.getByWxSerial(staff, openId));
			extra.setPromotion(Integer.parseInt(pId));
			extra.setStatus(Coupon.Status.CREATED);
			
			List<Coupon> list = CouponDao.getByCond(staff, extra, null);
			for(int i = 0; i < list.size(); i++){
				list.set(i, CouponDao.getById(staff, list.get(0).getId()));
			}
			jobject.setRoot(list);
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
	
	public ActionForward draw(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String couponId = request.getParameter("couponId");
		
		String formId = request.getParameter("fid");
		int rid = 0;
		DBCon dbCon = new DBCon();
		dbCon.connect();
		rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
		
		JObject jobject = new JObject();
		try{
			CouponDao.draw(StaffDao.getByRestaurant(dbCon, rid).get(0), Integer.parseInt(couponId), DrawType.MANUAL);
			
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
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}	
	
}
