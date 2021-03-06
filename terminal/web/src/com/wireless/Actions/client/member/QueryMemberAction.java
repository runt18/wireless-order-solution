
package com.wireless.Actions.client.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.MemberCondDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberDao.ActiveExtraCond;
import com.wireless.db.member.MemberDao.IdleExtraCond;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberCond;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.SQLUtil;

public class QueryMemberAction extends DispatchAction {
	
	public ActionForward count(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		String memberType = request.getParameter("memberTypes");
		String[] memberTypes = memberType.split(",");
		String pin = (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		List<Jsonable> mts = new ArrayList<Jsonable>();
		String extra = "";
		JObject jobject = new JObject();
		int sum = 0;
		try{
			for (int i = 0; i < memberTypes.length; i++) {
				extra += " AND M.restaurant_id = " + staff.getRestaurantId();
				extra += " AND M.member_type_id = " + memberTypes[i];
				Map<Object, Object> paramsSet = new HashMap<Object, Object>();
				paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
				final MemberType mt = MemberTypeDao.getById(staff, Integer.parseInt(memberTypes[i]));
				final int mc = MemberDao.getMemberCount(paramsSet);
				mts.add(new Jsonable() {
					
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putString("name", mt.getName());
						jm.putInt("memberCount", mc);
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
				});
				sum += mc;
				extra = "";
			}
			final int mTotal = sum;
			mts.add(new Jsonable() {
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("name", "总数");
					jm.putInt("memberCount", mTotal);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});
			
			jobject.setRoot(mts);
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 搜索
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String id = request.getParameter("id");
		final String memberType = request.getParameter("memberType");
		final String memberCardOrMobileOrName = request.getParameter("memberCardOrMobileOrName");
		final String memberMinBalance = request.getParameter("memberMinBalance");
		final String memberMaxBalance = request.getParameter("memberMaxBalance");
		final String maxTotalMemberCost = request.getParameter("MaxTotalMemberCost");
		final String minTotalMemberCost = request.getParameter("MinTotalMemberCost");
		final String consumptionMinAmount = request.getParameter("consumptionMinAmount");
		final String consumptionMaxAmount = request.getParameter("consumptionMaxAmount");
		final String minFansAmount = request.getParameter("minFansAmount");
		final String maxFansAmount = request.getParameter("maxFansAmount");
		final String minCommissionAmount = request.getParameter("minCommissionAmount");
		final String maxCommissionAmount = request.getParameter("maxCommissionAmount");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String referrer = request.getParameter("referrer");
		final String beginBirth = request.getParameter("beginBirthday");
		final String endBirth = request.getParameter("endBirthday");
		final String searchType = request.getParameter("sType");
		final String forDetail = request.getParameter("forDetail");
		final String needSum = request.getParameter("needSum");
		final String orderBy = request.getParameter("orderBy");
		
		final JObject jobject = new JObject();
		
		try{

			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final MemberDao.ExtraCond extraCond = new MemberDao.ExtraCond();
			
			if(id != null && !id.trim().isEmpty() && Integer.valueOf(id.trim()) > 0){
				extraCond.setId(Integer.parseInt(id));
			}else{
				if(memberType != null && !memberType.trim().isEmpty() && !memberType.equals("-1"))
					extraCond.setMemberType(Integer.parseInt(memberType));
				
				if(memberCardOrMobileOrName != null && !memberCardOrMobileOrName.trim().isEmpty()){
					if(searchType == null || searchType.isEmpty()){
						extraCond.setFuzzyName(memberCardOrMobileOrName);
					}else if(searchType.equals("1")){
						extraCond.setMobile(memberCardOrMobileOrName);
					}else if(searchType.equals("2")){
						extraCond.setWeixinCard(memberCardOrMobileOrName);
					}else if(searchType.equals("3")){
						extraCond.setCard(memberCardOrMobileOrName);
					}
				}
				
				if(minTotalMemberCost != null && !minTotalMemberCost.isEmpty()){
					extraCond.greaterTotalConsume(Integer.parseInt(minTotalMemberCost));
				}
				
				if(maxTotalMemberCost != null && !maxTotalMemberCost.isEmpty()){
					extraCond.lessTotalConsume(Integer.parseInt(maxTotalMemberCost));
				}
				
				if(consumptionMinAmount != null && !consumptionMinAmount.isEmpty()){
					extraCond.greaterConsume(Integer.parseInt(consumptionMinAmount));
				}
				
				if(consumptionMaxAmount != null && !consumptionMaxAmount.isEmpty()){
					extraCond.lessConsume(Integer.parseInt(consumptionMaxAmount));
				}
				
				if(beginDate != null && !beginDate.isEmpty()){
					extraCond.setRange(new DutyRange(beginDate, endDate));
				}
				if(memberMinBalance != null && !memberMinBalance.isEmpty()){
					extraCond.greaterBalance(Float.parseFloat(memberMinBalance));
				}
				
				if(memberMaxBalance != null && !memberMaxBalance.isEmpty()){
					extraCond.lessBalance(Float.parseFloat(memberMaxBalance));
				}
				
				if(referrer != null && !referrer.isEmpty()){
					extraCond.setReferrer(Integer.parseInt(referrer));
				}
				
				if(beginBirth != null && !beginBirth.isEmpty() && endBirth != null && !endBirth.isEmpty()){
					extraCond.setBirthday(beginBirth, endBirth);
				}
				
				if(branchId != null && !branchId.isEmpty()){
					extraCond.setBranch(Integer.parseInt(branchId));
				}
				
				if(minFansAmount != null && !minFansAmount.isEmpty() && maxFansAmount != null && !maxFansAmount.isEmpty()){
					extraCond.setFansRange(Integer.valueOf(minFansAmount), Integer.valueOf(maxFansAmount));
				}
				
				if(minCommissionAmount != null && !minCommissionAmount.isEmpty() && maxCommissionAmount != null && !maxCommissionAmount.isEmpty()){
					extraCond.setCommissionRange(Float.valueOf(minCommissionAmount), Float.valueOf(maxCommissionAmount));
				}
				
			}
			
			String orderClause = null;
			if(orderBy != null){
				if(orderBy.equals("create")){
					orderClause = " ORDER BY M.member_id ";
				}else if(orderBy.equals("consumeMoney")){
					orderClause = " ORDER BY M.total_consumption DESC ";
				}else if(orderBy.equals("consumeAmount")){
					orderClause = " ORDER BY M.consumption_amount DESC ";
				}else if(orderBy.equals("point")){
					orderClause = " ORDER BY M.total_point DESC ";
				}
			}			
			
			List<Member> result = MemberDao.getByCond(staff, extraCond, orderClause);
			jobject.setTotalProperty(result.size());
			
			Member sumMember = null;
			if(needSum != null && !needSum.isEmpty()){
				sumMember = new Member(-1);
				float baseBalance = 0, extraBalance = 0;
				for (Member m : result) {
					baseBalance += m.getBaseBalance();
					extraBalance += m.getExtraBalance();
				}
				
				sumMember.setMemberType(new MemberType(-1));
				sumMember.setBaseBalance(baseBalance);
				sumMember.setExtraBalance(extraBalance);				
			}

			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				result = DataPaging.getPagingData(result, true, start, limit);
			}
			
			if(forDetail != null && !forDetail.isEmpty()){
				for(int i = 0; i < result.size(); i++){
					result.set(i, MemberDao.getById(staff, result.get(i).getId()));
				}
			}
			
			if(needSum != null && !needSum.isEmpty()){
				result.add(sumMember);
			}
			
			jobject.setRoot(result);
			
		}catch(BusinessException e){
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
	
	public ActionForward idle(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		
		try{
			
			List<Member> list = MemberDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), IdleExtraCond.instance(), null);
			
			jobject.setRoot(list);
			
			jobject.setExtra(IdleExtraCond.instance());	
			
		}catch(BusinessException e){
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
	
	public ActionForward active(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		
		try{
			
			List<Member> list = MemberDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), MemberDao.ActiveExtraCond.instance(), null);
			
			jobject.setRoot(list);
			
			jobject.setExtra(ActiveExtraCond.instance());	
			
		}catch(BusinessException e){
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
	 * 根据order获取member
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward byOrder(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String orderId = request.getParameter("orderId");
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MemberOperation mo = MemberOperationDao.getLastConsumptionByOrder(staff, new Order(Integer.parseInt(orderId)));
			Member m = MemberDao.getById(staff, mo.getMemberId());
			
			final List<Coupon> coupons = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(m.getId()).setStatus(Coupon.Status.ISSUED), null);
			jobject.setRoot(m);
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					if(!coupons.isEmpty()){
						jm.putJsonableList("coupons", coupons, Coupon.COUPON_JSONABLE_SIMPLE);
					}
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});			
		}catch(BusinessException e){
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
	 * 反结账获取会员
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward memberRepaid(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String sv = request.getParameter("sv");
		//0: 模糊搜索, 1 : 根据手机号, 2: 微信卡号, 3:实体卡号
		String s_type = request.getParameter("st");
		
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		List<Member> membersByType = null;
		
		MemberDao.ExtraCond extra = new MemberDao.ExtraCond();
		
	
		try{
			if(sv != null && !sv.trim().isEmpty()){
				
				if(s_type == null || s_type.isEmpty() || Integer.parseInt(s_type) == 0){
					extra.setFuzzyName(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);					
				}else if(Integer.parseInt(s_type) == 1){
					extra.setMobile(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);
				}else if(Integer.parseInt(s_type) == 2){
					extra.setWeixinCard(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);
				}else if(Integer.parseInt(s_type) == 3){
					extra.setCard(sv);
					membersByType = MemberDao.getByCond(staff, extra, null);
				}

				if(membersByType.isEmpty()){
					throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
				}
			}else{
				throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
			}	
			
			membersByType.set(0, MemberDao.getById(staff, membersByType.get(0).getId()));
			
			final List<Member> members = membersByType;
			
			final List<Coupon> coupons = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(members.get(0).getId()).setStatus(Coupon.Status.ISSUED), null);
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();

					if(members.size() > 1){
						jm.putJsonableList("members", members, 0);
					}else{
						jm.putJsonable("member", members.get(0), 0);					
					}
					if(!coupons.isEmpty()){
						jm.putJsonableList("coupons", coupons, Coupon.COUPON_JSONABLE_SIMPLE);
					}
					
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			
		}catch(BusinessException e){
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
	 * 会员分析
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward byMemberCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String memberCondId = request.getParameter("memberCondId");
		final String memberType = request.getParameter("memberType");
		final String memberCondMinConsume = request.getParameter("memberCondMinConsume");
		final String memberCondMaxConsume = request.getParameter("memberCondMaxConsume");
		final String memberCondMinAmount = request.getParameter("memberCondMinAmount");
		final String memberCondMaxAmount = request.getParameter("memberCondMaxAmount");
		final String memberCondMinBalance = request.getParameter("memberCondMinBalance");
		final String memberCondMaxBalance = request.getParameter("memberCondMaxBalance");
		final String memberCondBeginDate = request.getParameter("memberCondBeginDate");
		final String memberCondEndDate = request.getParameter("memberCondEndDate");
		final String memberCondMinFansAmount =request.getParameter("memberCondMinFansAmount");
		final String memberCondMaxFansAmount =request.getParameter("memberCondMaxFansAmount");
		final String memberCondMinCommission = request.getParameter("memberCondMinCommission");
		final String memebrCondMaxCommission = request.getParameter("memberCondMaxCommission");
		final String recentlyBirthday = request.getParameter("recentlyBirthday");
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final MemberCond memberCond;
			if(memberCondId != null && !memberCondId.isEmpty()){
				memberCond = MemberCondDao.getById(staff, Integer.parseInt(memberCondId));
			}else{
				memberCond = new MemberCond(-1);
				int minAmount = 0, maxAmount = 0;
				float minConsume = 0, maxConsume = 0, minBalance = 0, maxBalance = 0;
				
				//设置时间段
				memberCond.setRange(new DutyRange(memberCondBeginDate, memberCondEndDate));
				
				if(memberType != null && !memberType.isEmpty() && !memberType.equals("-1")){
					memberCond.setMemberType(new MemberType(Integer.parseInt(memberType)));
				}
				if(memberCondMinConsume != null && !memberCondMinConsume.isEmpty()){
					minConsume = Float.parseFloat(memberCondMinConsume);
				}
				if(memberCondMaxConsume != null && !memberCondMaxConsume.isEmpty()){
					maxConsume = Float.parseFloat(memberCondMaxConsume);
				}
				if(memberCondMinAmount != null && !memberCondMinAmount.isEmpty()){
					minAmount = Integer.parseInt(memberCondMinAmount);
				}
				if(memberCondMaxAmount != null && !memberCondMaxAmount.isEmpty()){
					maxAmount = Integer.parseInt(memberCondMaxAmount);
				}
				if(memberCondMinBalance != null && !memberCondMinBalance.isEmpty()){
					minBalance = Float.parseFloat(memberCondMinBalance);
				}
				if(memberCondMaxBalance != null && !memberCondMaxBalance.isEmpty()){
					maxBalance = Float.parseFloat(memberCondMaxBalance);
				}
				
				memberCond.setMaxBalance(maxBalance);
				memberCond.setMaxConsumeAmount(maxAmount);
				memberCond.setMaxConsumeMoney(maxConsume);
				memberCond.setMinBalance(minBalance);
				memberCond.setMinConsumeAmount(minAmount);
				memberCond.setMinConsumeMoney(minConsume);
				
				//距离生日天数
				if(recentlyBirthday != null && !recentlyBirthday.isEmpty() && !recentlyBirthday.equals("-1")){
					memberCond.setRecentlyBirthday(Integer.parseInt(recentlyBirthday));
				}
				
				//粉丝数
				if(memberCondMinFansAmount != null && !memberCondMinFansAmount.isEmpty()){
					memberCond.setMinFansAmount(Integer.valueOf(memberCondMinFansAmount));
				}
				
				if(memberCondMaxFansAmount != null && !memberCondMaxFansAmount.isEmpty()){
					memberCond.setMaxFansAmount(Integer.valueOf(memberCondMaxFansAmount));
				}
				
				//佣金总额
				if(memberCondMinCommission != null && !memberCondMinCommission.isEmpty()){
					memberCond.setMinCommissionAmount(Float.valueOf(memberCondMinCommission));
				}
				
				if(memebrCondMaxCommission != null && !memebrCondMaxCommission.isEmpty()){
					memberCond.setMaxCommissionAmount(Float.valueOf(memebrCondMaxCommission));
				}
			}
			
			
			jObject.setRoot(MemberDao.getByCond(staff, new MemberDao.ExtraCond(memberCond), null));
		}catch(BusinessException e){
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
	
}
