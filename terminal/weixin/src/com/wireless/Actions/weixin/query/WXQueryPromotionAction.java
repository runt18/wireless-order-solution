package com.wireless.Actions.weixin.query;

import java.sql.SQLException;
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
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.json.JObject;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.staffMgr.Staff;

public class WXQueryPromotionAction extends DispatchAction{
	
//	public ActionForward hasWelcomePage(ActionMapping mapping, ActionForm form,
//			HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//		String pin = (String) request.getAttribute("pin");
//		String formId = request.getParameter("fid");
//		
//		int rid = 0;
//		Staff staff;
//		if(pin != null){
//			staff = StaffDao.verify(Integer.parseInt(pin));
//		}else{
//			DBCon dbCon = new DBCon();
//			dbCon.connect();
//			rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
//			staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
//			dbCon.disconnect();
//		}
//		
//		JObject jobject = new JObject();
//		try{
//			PromotionDao.ExtraCond extra = new PromotionDao.ExtraCond();
//			extra.setType(Promotion.Type.WELCOME);
//			extra.addStatus(Promotion.Status.PROGRESS).addStatus(Promotion.Status.CREATED).addStatus(Promotion.Status.PUBLISH);
//			
//			List<Promotion> list = PromotionDao.getByCond(staff, extra);
//			
//			jobject.setRoot(list);
//			
//		}catch(SQLException e){
//			e.printStackTrace();
//			jobject.initTip(e);
//		}catch(Exception e){
//			e.printStackTrace();
//			jobject.initTip4Exception(e);
//		}finally{
//			response.getWriter().print(jobject.toString());
//		}
//		
//		return null;		
//		
//	}	
	
	
	public ActionForward promotions(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String formId = request.getParameter("fid");
		String oid = request.getParameter("oid");
		
		int rid = 0;
		Staff staff;
		if(pin != null){
			staff = StaffDao.verify(Integer.parseInt(pin));
		}else{
			DBCon dbCon = new DBCon();
			dbCon.connect();
			rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
			staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
			dbCon.disconnect();
		}
		
		JObject jobject = new JObject();
		try{
			
			List<Coupon> coupons = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(MemberDao.getByWxSerial(staff, oid)).setPromotionType(Promotion.Type.NORMAL), null);
			
			if(!coupons.isEmpty()){
				coupons.get(0).setPromotion(PromotionDao.getById(staff, coupons.get(0).getPromotion().getId()));
				jobject.setRoot(coupons.get(0));
			}else{
				jobject.initTip(false, "无相关活动");
			}
			
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString(Coupon.COUPON_JSONABLE_WITH_PROMOTION));
		}
		
		return null;		
		
	}		
}
