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

import com.wireless.db.promotion.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.staffMgr.Staff;

public class QueryCouponAction extends DispatchAction{

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
	
	public ActionForward byCondtion(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pId = request.getParameter("pId");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String status = request.getParameter("status");
		Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
		
		JObject jobject = new JObject();
		try{
			CouponDao.ExtraCond extra = new CouponDao.ExtraCond();
			extra.setPromotion(Integer.parseInt(pId));
			
			if(status != null && !status.isEmpty()){
				extra.setStatus(Coupon.Status.valueOf(Integer.parseInt(status)));
			}
			
			//List<Coupon> list = CouponDao.getByCond(staff, extra, null);
			jobject.setTotalProperty(CouponDao.getByCond(staff, extra.setOnlyAmount(true), null).size());
			jobject.setRoot(CouponDao.getByCond(staff, extra.setOnlyAmount(false), " LIMIT " + start + ", " + limit));
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString(Coupon.COUPON_JSONABLE_SIMPLE));
		}
		return null;
	}	
	
	public ActionForward status(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
		String pId = request.getParameter("promotionId");
		
		JObject jobject = new JObject();
		try{
			final int couponPublished = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setPromotion(Integer.parseInt(pId)).setOnlyAmount(true), null).size();
			final int couponDrawn = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setPromotion(Integer.parseInt(pId)).setStatus(Coupon.Status.ISSUED).setOnlyAmount(true), null).size();
			final int couponUsed = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setPromotion(Integer.parseInt(pId)).setStatus(Coupon.Status.USED).setOnlyAmount(true), null).size();
			
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putInt("couponPublished", couponPublished);
					jm.putInt("couponDrawn", couponDrawn);
					jm.putInt("couponUsed", couponUsed);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}	
}
