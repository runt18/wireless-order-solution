package com.wireless.Actions.couponMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.staffMgr.Staff;

public class OperateCouponAction extends DispatchAction{

	/**
	 * 获取优惠券
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final String issueMode = request.getParameter("issueMode");
		final String issueAssociateId = request.getParameter("issueAssociateId");
		final String memberId = request.getParameter("memberId");
		
		JObject jObject = new JObject();
		try{
			final CouponDao.ExtraCond extraCond = new CouponDao.ExtraCond();
			if(issueMode != null && !issueMode.isEmpty()){
				if(issueAssociateId != null && !issueAssociateId.isEmpty()){
					extraCond.setIssueMode(Coupon.IssueMode.valueOf(Integer.parseInt(issueMode)), Integer.parseInt(issueAssociateId));
				}else{
					extraCond.setIssueMode(Coupon.IssueMode.valueOf(Integer.parseInt(issueMode)), 0);
				}
			}
			
			if(memberId != null && !memberId.isEmpty()){
				extraCond.setMember(Integer.parseInt(memberId));
			}
			
			jObject.setRoot(CouponDao.getByCond(staff, extraCond, null));
			
		}catch(SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 发券
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward issue(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		Coupon.IssueMode issueMode = Coupon.IssueMode.valueOf(Integer.parseInt(request.getParameter("issueMode")));
		String promotions = request.getParameter("promotions");
		String members = request.getParameter("members");
		String orderId = request.getParameter("orderId");
		String comment = request.getParameter("comment");
		
		Coupon.IssueBuilder builder = null;
		
		if(issueMode == Coupon.IssueMode.FAST){
			builder = Coupon.IssueBuilder.newInstance4Fast();
			
		}else if(issueMode == Coupon.IssueMode.ORDER){
			builder = Coupon.IssueBuilder.newInstance4Order(Integer.parseInt(orderId));
			
		}else if(issueMode == Coupon.IssueMode.WX_SUBSCRIBE){
			builder = Coupon.IssueBuilder.newInstance4WxSubscribe();
			
		}
		
		//设置发放备注
		if(comment != null && !comment.isEmpty()){
			builder.setComment(comment);
		}
		
		//设置发放的优惠券类型
		for(String promotionId : promotions.split(",")){
			builder.addPromotion(Integer.parseInt(promotionId));
		}
		
		//设置优惠券的发放对象
		for(String memberId : members.split(",")){
			builder.addMember(Integer.parseInt(memberId));
		}
		
		JObject jobject = new JObject();
		try{
			CouponDao.issue(staff, builder);
			
			jobject.initTip(true, "优惠券发放成功");
			
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
	
	public ActionForward draw(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		String couponId = request.getParameter("couponId");
		
		String formId = request.getParameter("fid");
		int rid = 0;
		DBCon dbCon = new DBCon();
		dbCon.connect();
		rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
		
		JObject jobject = new JObject();
		try{
			CouponDao.draw(StaffDao.getByRestaurant(dbCon, rid).get(0), Integer.parseInt(couponId), Coupon.DrawType.MANUAL);
			
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
