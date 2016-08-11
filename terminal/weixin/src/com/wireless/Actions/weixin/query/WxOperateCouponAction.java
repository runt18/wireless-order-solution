package com.wireless.Actions.weixin.query;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.MemberDao;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponOperationDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.promotion.CouponDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.WxMember;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.PromotionTrigger;
import com.wireless.pojo.staffMgr.Staff;

public class WxOperateCouponAction extends DispatchAction{
	
	public ActionForward getDetails(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String fromId = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		final String operate = request.getParameter("operate");
		final String associateId = request.getParameter("associateId");
		final String limit = request.getParameter("limit");
		
		final JObject jObject = new JObject();
		try{
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			final WxMember wxMember = WxMemberDao.getBySerial(staff, oid);
			
			final CouponOperationDao.ExtraCond extraCond = new CouponOperationDao.ExtraCond().setMember(wxMember.getMemberId());
			if(operate != null && !operate.isEmpty()){
				for(String each : operate.split(",")){
					if(each.equalsIgnoreCase("order_use")){
						extraCond.addOperation(CouponOperation.Operate.ORDER_USE);
					}else if(each.equalsIgnoreCase("order_issue")){
						extraCond.addOperation(CouponOperation.Operate.ORDER_ISSUE);
					}
				}
			}
			
			if(associateId != null && !associateId.isEmpty()){
				extraCond.setAssociateId(Integer.parseInt(associateId));
			}
			
			List<CouponOperation> result = CouponOperationDao.getByCond(staff, extraCond);
			
			if(limit != null && !limit.isEmpty()){
				if(result.size() > Integer.parseInt(limit)){
					result = result.subList(0, Integer.parseInt(limit));
				}
			}
			
			Collections.sort(result, CouponOperation.BY_DATE);
			
			jObject.setRoot(result);
			
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		final String fromId = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		final String couponId = request.getParameter("cid");
		final String promotionId = request.getParameter("pid");
		final String status = request.getParameter("status");
		final String filter = request.getParameter("filter");
		
		JObject jObject = new JObject();
		try{
			
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			CouponDao.ExtraCond extraCond = new CouponDao.ExtraCond();
			
			if(couponId != null && !couponId.isEmpty()){
				extraCond.setId(Integer.parseInt(couponId));
			}
			
			if(status != null && !status.isEmpty()){
				if(status.equalsIgnoreCase("issued")){
					extraCond.setStatus(Coupon.Status.ISSUED);
				}else if(status.equals("used")){
					extraCond.setStatus(Coupon.Status.USED);
				}
			}
			
			if(promotionId != null && !promotionId.isEmpty()){
				extraCond.setPromotion(Integer.parseInt(promotionId));
			}
			
			if(oid != null && !oid.isEmpty()){
				extraCond.setMember(MemberDao.getByWxSerial(staff, oid));
			}
			
			if(filter != null && !filter.isEmpty()){
				extraCond.setFilter(ExtraCond.Filter.valueOf(Integer.parseInt(filter)));
			}
			
			final List<Coupon> result = CouponDao.getByCond(staff, extraCond, null);
			for(Coupon coupon : result){
				if(coupon.getCouponType().hasImage()){
					coupon.getCouponType().setImage(OssImageDao.getById(staff, coupon.getCouponType().getImage().getId()));
				}
			}
			
			jObject.setRoot(result);
			
		}catch(SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	
    public ActionForward getPointChangeCoupon(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
    	final String fromId = request.getParameter("fid");
		final String oid = request.getParameter("oid");
		final String promotionId = request.getParameter("pid");
		final String status = request.getParameter("status");
		final String issueTriggers = request.getParameter("issueTriggers");
		final String useTriggers = request.getParameter("useTriggers");
		final String orderId = request.getParameter("orderId");
		
		JObject jObject = new JObject();
		try{
			
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
		
			final PromotionDao.ExtraCond extraCond = new PromotionDao.ExtraCond();
			
			if(promotionId != null && !promotionId.isEmpty()){
				extraCond.setPromotionId(Integer.parseInt(promotionId));
			}
			
			if(status != null && !status.isEmpty()){
				if(status.equalsIgnoreCase("progress")){
					extraCond.setStatus(Promotion.Status.PROGRESS);
				}
			}
			
			//发券规则
			if(issueTriggers != null && !issueTriggers.isEmpty()){
				for(String issueTrigger : issueTriggers.split(",")){
					PromotionTrigger.IssueRule issueRule = PromotionTrigger.IssueRule.valueOf(Integer.parseInt(issueTrigger));
					if(issueRule.isSingleExceed()){
						extraCond.addIssueRule(issueRule, Integer.valueOf(orderId));
					}else if(issueRule.isPointExchange()){
						extraCond.addIssueRule(issueRule, MemberDao.getByWxSerial(staff, oid).getId());
					}else{
						extraCond.addIssueRule(issueRule);
					}
				}
			}
			
			//用券规则
			if(useTriggers != null && !useTriggers.isEmpty()){
				for(String useTrigger : useTriggers.split(",")){
					PromotionTrigger.UseRule useRule = PromotionTrigger.UseRule.valueOf(Integer.parseInt(useTrigger));
					if(useRule.isSingleExceed()){
						extraCond.addUseRule(useRule, Integer.parseInt(orderId));
					}else{
						extraCond.addUseRule(useRule);
					}
				}
			}
			
			jObject.setRoot(PromotionDao.getByCond(staff, extraCond));
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
    	
    }
    
	//积分消费
	public ActionForward pointConsume(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		final String fromId = request.getParameter("fid");
		final String openId = request.getParameter("oid");
		final String promotionId = request.getParameter("promotionId");
		JObject jobject = new JObject();
		
		try{
			
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			final Member.PointExchangeBuilder pointExchangeBuilder = new Member.PointExchangeBuilder(MemberDao.getByWxSerial(staff, openId).getId());
			//设置发放的优惠券
			if(promotionId != null && !promotionId.isEmpty()){
				pointExchangeBuilder.addPromotion(Integer.parseInt(promotionId), 1);
			}		
			
			MemberDao.pointConsume(staff, pointExchangeBuilder);
			jobject.initTip(true, "操作成功, 会员积分消费成功.");
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}finally{
			response.getWriter().print(jobject.toString());
		}	
		
		return null;
		
		
	}
	
	public ActionForward getById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		String fromId = request.getParameter("fid");
		
		final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
		final Staff staff = StaffDao.getAdminByRestaurant(rid);
		
		JObject jobject = new JObject();
		try{
			jobject.setRoot(CouponDao.getById(staff, Integer.parseInt(request.getParameter("cid"))));
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
