package com.wireless.Actions.couponMgr;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.member.MemberCondDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponOperationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberCond;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class OperateCouponAction extends DispatchAction{

	public ActionForward getOperations(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String staffId = request.getParameter("staffId");
		final String begin = request.getParameter("beginDate");
		final String end = request.getParameter("endDate");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		final String operate = request.getParameter("operate");
		final String operateType = request.getParameter("operateType");
		final String memberFuzzy = request.getParameter("memberFuzzy");
		final String couponId = request.getParameter("couponId");
		final String couponTypeId = request.getParameter("couponTypeId");
		final JObject jObject = new JObject();
		try{
			
			final CouponOperationDao.ExtraCond extraCond = new CouponOperationDao.ExtraCond();
			
			if(staffId != null && !staffId.isEmpty()){
				extraCond.setStaff(Integer.parseInt(staffId));
			}
			
			if(begin != null && !begin.isEmpty() && end != null && !end.isEmpty()){
				DutyRange range = DutyRangeDao.exec(staff, begin, end);
				if(range != null){
					extraCond.setRange(range);
				}else{
					extraCond.setRange(begin, end);
				}
			}
			
			if(opening != null && !opening.isEmpty() && ending != null && !ending.isEmpty()){
				extraCond.setHourRange(opening, ending);
			}
			
			if(operate != null && !operate.isEmpty()){
				extraCond.setOperate(CouponOperation.Operate.valueOf(Integer.parseInt(operate)));
			}
			
			if(operateType != null && !operateType.isEmpty()){
				if(operateType.equalsIgnoreCase("issue")){
					extraCond.setOperateType(CouponOperation.OperateType.ISSUE);
				}else if(operateType.equalsIgnoreCase("use")){
					extraCond.setOperateType(CouponOperation.OperateType.USE);
				}
			}
			
			if(memberFuzzy != null && !memberFuzzy.isEmpty()){
				extraCond.setMemberFuzzy(memberFuzzy);
			}
			
			if(couponId != null && !couponId.isEmpty()){
				extraCond.setCoupon(Integer.parseInt(couponId));
			}
			
			if(couponTypeId != null && !couponTypeId.isEmpty()){
				extraCond.setCouponType(Integer.parseInt(couponTypeId));
			}
			
			//获取优惠券的操作记录
			final List<CouponOperation> result = CouponOperationDao.getByCond(staff, extraCond);

			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				jObject.setTotalProperty(result.size());
				CouponOperation total = new CouponOperation(0);
				total.setOperate(CouponOperation.Operate.FAST_ISSUE);
				for (CouponOperation operation : result) {
					total.setCouponPrice(total.getCouponPrice() + operation.getCouponPrice());
				}
				total.setCouponName("共" + result.size() + "条");
				List<CouponOperation> limitResult = DataPaging.getPagingData(result, true, start, limit);
				limitResult.add(total);
				jObject.setRoot(limitResult);
				
			}else{
				jObject.setRoot(result);
			}
			
		}catch(SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 * 根据账单和会员获取可用的优惠券
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getAvailableByOrder(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final String memberId = request.getParameter("memberId");
		final String orderId = request.getParameter("orderId");
		final JObject jObject = new JObject();
		
		try{
			//获取账单已用的优惠券
			final List<Coupon> result = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setOperation(CouponOperation.Operate.ORDER_USE, Integer.parseInt(orderId)), null);
			
			//获取会员可用的优惠券
			result.addAll(CouponDao.getByCond(staff, new CouponDao.ExtraCond().setStatus(Coupon.Status.ISSUED).setMember(Integer.parseInt(memberId)), null));
			
			//过滤已过期的优惠券
			Iterator<Coupon> iter = result.iterator();
			while(iter.hasNext()){
				Coupon item = iter.next();
				if(item.isExpired()){
					iter.remove();
				}
			}
			
			jObject.setRoot(result);
			
			
		}catch(SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString(Coupon.COUPON_JSONABLE_SIMPLE));
		}
		return null;
	}
	
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
		response.setContentType("text/json; charset=utf-8");
		final String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final String status = request.getParameter("status");
		final String memberId = request.getParameter("memberId");
		final String operation = request.getParameter("operate");
		final String associateId = request.getParameter("associateId");
		final String expired = request.getParameter("expired");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		
		JObject jObject = new JObject();
		try{
			final CouponDao.ExtraCond extraCond = new CouponDao.ExtraCond();
			if(status != null && !status.isEmpty()){
				if(status.equalsIgnoreCase("issued")){
					extraCond.setStatus(Coupon.Status.ISSUED);
				}else if(Pattern.compile("[0-9]*").matcher(status).matches()){
					extraCond.setStatus(Coupon.Status.valueOf(Integer.parseInt(status)));
				}
			}
			
			if(operation != null && !operation.isEmpty()){
				if(associateId != null && !associateId.isEmpty()){
					extraCond.setOperation(CouponOperation.Operate.valueOf(Integer.parseInt(operation)), Integer.parseInt(associateId));
				}else{
					extraCond.setOperation(CouponOperation.Operate.valueOf(Integer.parseInt(operation)), 0);
				}
			}
			
			if(memberId != null && !memberId.isEmpty()){
				extraCond.setMember(Integer.parseInt(memberId));
			}
			
			if(expired != null && !expired.isEmpty()){
				extraCond.isExpired(Boolean.parseBoolean(expired));
			}
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				jObject.setTotalProperty(CouponDao.getByCond(staff, extraCond.setOnlyAmount(true), null).size());
				jObject.setRoot(CouponDao.getByCond(staff, extraCond.setOnlyAmount(false), " LIMIT " + start + ", " + limit));
			}else{
				jObject.setRoot(CouponDao.getByCond(staff, extraCond, null));
			}
		}catch(SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString(Coupon.COUPON_JSONABLE_SIMPLE));
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
		
		CouponOperation.Operate issueMode = CouponOperation.Operate.valueOf(Integer.parseInt(request.getParameter("issueMode")));
		final String promotions = request.getParameter("promotions");
		final String members = request.getParameter("members");
		final String condId = request.getParameter("condId");
		final String orderId = request.getParameter("orderId");
		final String comment = request.getParameter("comment");
		
		JObject jobject = new JObject();
		try{
			Coupon.IssueBuilder builder = null;
			
			if(issueMode == CouponOperation.Operate.FAST_ISSUE){
				builder = Coupon.IssueBuilder.newInstance4Fast();
				
			}else if(issueMode == CouponOperation.Operate.ORDER_ISSUE){
				builder = Coupon.IssueBuilder.newInstance4Order(Integer.parseInt(orderId));
				
			}else if(issueMode == CouponOperation.Operate.WX_SUBSCRIBE_ISSUE){
				builder = Coupon.IssueBuilder.newInstance4WxSubscribe();
			
			}else if(issueMode == CouponOperation.Operate.BATCH_ISSUE){
				builder = Coupon.IssueBuilder.newInstance4Batch();
				
			}else{
				throw new BusinessException("【" + issueMode.toString() + "】的发券类型不正确");
			}
			
			//设置发放备注
			if(comment != null && !comment.isEmpty()){
				builder.setComment(comment);
			}
			
			//设置发放的优惠券类型
			if(promotions != null && !promotions.isEmpty()){
				for(String eachPromotion : promotions.split(";")){
					int promotionId = Integer.parseInt(eachPromotion.split(",")[0]);
					int amount = Integer.parseInt(eachPromotion.split(",")[1]);
					builder.addPromotion(promotionId, amount);
				}
			}			
			
			if(members != null && !members.isEmpty()){
				//设置优惠券的发放对象
				for(String memberId : members.split(",")){
					builder.addMember(Integer.parseInt(memberId));
				}
			}else if(condId != null && !condId.isEmpty()){
				//根据会员条件筛选设置发放对象
				final MemberCond memberCond = MemberCondDao.getById(staff, Integer.parseInt(condId));
				for(Member member : MemberDao.getByCond(staff, new MemberDao.ExtraCond(memberCond), null)){
					builder.addMember(member);
				}
			}
			
			CouponDao.issue(staff, builder);
			
			jobject.initTip(true, "优惠券发放成功");
			
		}catch(BusinessException | SQLException e){
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
	 * 快速用券
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward coupon(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final String useMode = request.getParameter("useMode");
		//final String useAssociateId = request.getParameter("useAssociateId");
		final String useTo = request.getParameter("useTo");
		final String coupons = request.getParameter("coupons");
		
		JObject jobject = new JObject();
		try{
			if(CouponOperation.Operate.valueOf(Integer.parseInt(useMode)) == CouponOperation.Operate.FAST_USE){
				Coupon.UseBuilder builder = Coupon.UseBuilder.newInstance4Fast(Integer.parseInt(useTo));
				if(coupons != null && !coupons.isEmpty()){
					for(String couponId : coupons.split(",")){
						builder.addCoupon(Integer.parseInt(couponId));
					}
				}
				CouponDao.use(staff, builder);

			}else{
				throw new BusinessException("优惠券使用方法必须是快速使用");
			}
			
		}catch(BusinessException | SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

	/**
	 * 优惠券使用情况
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward status(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
		String pId = request.getParameter("promotionId");
		
		JObject jobject = new JObject();
		try{
			final int couponIssued = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setPromotion(Integer.parseInt(pId)).setStatus(Coupon.Status.ISSUED).setOnlyAmount(true), null).size();
			final int couponUsed = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setPromotion(Integer.parseInt(pId)).setStatus(Coupon.Status.USED).setOnlyAmount(true), null).size();
			final int couponExpired = CouponDao.getByCond(staff, new CouponDao.ExtraCond().isExpired(true), null).size();
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putInt("couponIssued", couponIssued);
					jm.putInt("couponUsed", couponUsed);
					jm.putInt("couponExpired", couponExpired);
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
