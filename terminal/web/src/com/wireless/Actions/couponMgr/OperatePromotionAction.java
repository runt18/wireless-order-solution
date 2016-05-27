package com.wireless.Actions.couponMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.Promotion.Status;
import com.wireless.pojo.promotion.PromotionTrigger;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class OperatePromotionAction extends DispatchAction{

	/**
	 * 新建优惠活动
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String title = request.getParameter("title");
		final String body = request.getParameter("body");
		final String entire = request.getParameter("entire");
		//String pType = request.getParameter("pRule");
		//String point = request.getParameter("point");
		
		final String couponName = request.getParameter("couponName");
		final String price = request.getParameter("price");
		final String expiredDate = request.getParameter("expiredDate");
		final String image = request.getParameter("image");
		final String issueRule = request.getParameter("issueRule");
		final String issueSingleMoney = request.getParameter("singleMoney");
		final String useRule = request.getParameter("useRule");
		final String useSingleMoney = request.getParameter("useSingleMoney");
		
		final String pin = (String) request.getAttribute("pin");
		final JObject jobject = new JObject();
		try{
			
			final CouponType.InsertBuilder typeInsertBuilder = new CouponType.InsertBuilder(couponName, Integer.parseInt(price), DateUtil.parseDate(expiredDate)).setComment("活动优惠劵");
			if(image != null && !image.isEmpty()){
				typeInsertBuilder.setImage(Integer.parseInt(image));
			}
			
			final Promotion.CreateBuilder promotionCreateBuilder = Promotion.CreateBuilder.newInstance(title, body, typeInsertBuilder, entire);
			
			//发券规则
			if(issueRule != null && !issueRule.isEmpty()){
				if(Integer.parseInt(issueRule) == PromotionTrigger.IssueRule.FREE.getVal()){
					//免费发券
					promotionCreateBuilder.setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4Free());
				}else if(Integer.parseInt(issueRule) == PromotionTrigger.IssueRule.SINGLE_EXCEED.getVal()){
					//单次消费满几多发券
					if(issueSingleMoney != null && !issueSingleMoney.isEmpty()){
						promotionCreateBuilder.setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4SingleExceed(Integer.parseInt(issueSingleMoney)));
					}
				}else if(Integer.parseInt(issueRule) == PromotionTrigger.IssueRule.WX_SUBSCRIBE.getVal()){
					//微信关注发券
					promotionCreateBuilder.setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4Wx());
				}
			}
			
			//用券规则
			if(useRule != null && !useRule.isEmpty()){
				if(Integer.parseInt(useRule) == PromotionTrigger.UseRule.FREE.getVal()){
					promotionCreateBuilder.setUseTrigger(PromotionTrigger.InsertBuilder.newIssue4Free());
				}else if(Integer.parseInt(useRule) == PromotionTrigger.UseRule.SINGLE_EXCEED.getVal()){
					if(useSingleMoney != null && !useSingleMoney.isEmpty()){
						promotionCreateBuilder.setUseTrigger(PromotionTrigger.InsertBuilder.newIssue4SingleExceed(Integer.parseInt(useSingleMoney)));
					}
				} 
			}
			
			final int promotionId = PromotionDao.create(StaffDao.verify(Integer.parseInt(pin)), promotionCreateBuilder);
			
			jobject.initTip(true, "活动创建成功");
			jobject.setRoot(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putInt("pid", promotionId);
					return jm;
				}
				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
				}
			});
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
	 * 修改优惠活动
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String promotionId = request.getParameter("id");
		final String title = request.getParameter("title");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String body = request.getParameter("body");
		final String entire = request.getParameter("entire");
		
		final String couponTypeId = request.getParameter("cId");
		final String couponName = request.getParameter("couponName");
		final String price = request.getParameter("price");
		final String expiredDate = request.getParameter("expiredDate");
		final String image = request.getParameter("image");		
		final String issueRule = request.getParameter("issueRule");
		final String issueSingleMoney = request.getParameter("issueSingleMoney");
		final String useRule = request.getParameter("useRule");
		final String useSingleMoney = request.getParameter("useSingleMoney");
		
		//String orientedId = request.getParameter("oriented");
		
		final String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final JObject jobject = new JObject();
		try{
			
			final Promotion.UpdateBuilder promotionUpdateBuilder = new Promotion.UpdateBuilder(Integer.parseInt(promotionId));
			
			//优惠券类型
			if(couponTypeId != null && !couponTypeId.isEmpty()){
				CouponType.UpdateBuilder typeUpdateBuilder = new CouponType.UpdateBuilder(Integer.parseInt(couponTypeId), couponName);
				if(price != null && !price.isEmpty()){
					typeUpdateBuilder.setPrice(Integer.parseInt(price));
				}
				if(expiredDate != null && !expiredDate.isEmpty()){
					typeUpdateBuilder.setExpired(expiredDate);
				}
				if(image != null && !image.isEmpty()){
					typeUpdateBuilder.setImage(new OssImage(Integer.parseInt(image)));
				}
				promotionUpdateBuilder.setCouponTypeBuilder(typeUpdateBuilder);
			}
			
			//优惠活动Title
			if(title != null && !title.isEmpty()){
				promotionUpdateBuilder.setTitle(title);
			}
			
			//优惠活动正本
			if(entire != null && !entire.isEmpty()){
				promotionUpdateBuilder.setBody(body, entire);
			}
			
			//优惠活动日期
			if(beginDate != null && !beginDate.isEmpty() && endDate != null && !endDate.isEmpty()){
				promotionUpdateBuilder.setRange(beginDate, endDate);
			}else if(beginDate != null && !beginDate.isEmpty() && (endDate == null || endDate.isEmpty())){
				promotionUpdateBuilder.setRange(DateUtil.parseDate(beginDate), 0);
			}else if((beginDate == null || beginDate.isEmpty()) && endDate != null && !endDate.isEmpty()){
				promotionUpdateBuilder.setRange(0, DateUtil.parseDate(endDate));
			}
			
			//发券规则
			if(issueRule != null && !issueRule.isEmpty()){
				if(Integer.parseInt(issueRule) == PromotionTrigger.IssueRule.FREE.getVal()){
					//免费发券
					promotionUpdateBuilder.setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4Free());
				}else if(Integer.parseInt(issueRule) == PromotionTrigger.IssueRule.SINGLE_EXCEED.getVal()){
					//单次消费满几多发券
					promotionUpdateBuilder.setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4SingleExceed(Integer.parseInt(issueSingleMoney)));
				}else if(Integer.parseInt(issueRule) == PromotionTrigger.IssueRule.WX_SUBSCRIBE.getVal()){
					//微信关注发券
					promotionUpdateBuilder.setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4Wx());
				}else if(Integer.parseInt(issueRule) == PromotionTrigger.IssueRule.WX_SCAN.getVal()){
					//扫码发券
					promotionUpdateBuilder.setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4WxScan());
				}
			}
			
			//用券规则
			if(useRule != null && !useRule.isEmpty()){
				if(Integer.parseInt(useRule) == PromotionTrigger.UseRule.FREE.getVal()){
					promotionUpdateBuilder.setUseTrigger(PromotionTrigger.InsertBuilder.newUse4Free());
				}else if(Integer.parseInt(useRule) == PromotionTrigger.UseRule.SINGLE_EXCEED.getVal()){
					promotionUpdateBuilder.setUseTrigger(PromotionTrigger.InsertBuilder.newUse4SingleExceed(Integer.parseInt(useSingleMoney)));
				} 
			}
			
			PromotionDao.update(staff, promotionUpdateBuilder);
			
			jobject.initTip(true, "活动修改成功");
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
	
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json; charset=utf-8");
		//System.out.println(response.getContentType());
		
		final String pin = (String) request.getAttribute("pin");
		
		final String promotionId = request.getParameter("promotionId");
		final String status = request.getParameter("status");
		final String issueTriggers = request.getParameter("issueTriggers");
		final String useTriggers = request.getParameter("useTriggers");
		final String orderId = request.getParameter("orderId");
		
		final JObject jobject = new JObject();
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		
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
			
			jobject.setRoot(PromotionDao.getByCond(staff, extraCond));
		}catch(SQLException | BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;		
		
	}	
	
//	public ActionForward getById(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
//		String pin = (String) request.getAttribute("pin");
//		
//		Staff staff = StaffDao.verify(Integer.parseInt(pin));
//		String promotionId = request.getParameter("promotionId");
//		
//		JObject jobject = new JObject();
//		try{
//			final Promotion p = PromotionDao.getById(staff, Integer.parseInt(promotionId));
//			
//			final Promotion promo = p;
//			List<Coupon> p_List = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setPromotion(p.getId()), null);
//			List<Member> members = new ArrayList<>();
//			for (Coupon coupon : p_List) {
//				members.add(coupon.getMember());
//			}
//			final List<Member> memberList = members;
//			jobject.setExtra(new Jsonable() {
//				
//				@Override
//				public JsonMap toJsonMap(int flag) {
//					JsonMap jm = new JsonMap();
//					jm.putJsonable(promo, 0);
//					jm.putJsonableList("members", memberList, 0);
//					return jm;
//				}
//				
//				@Override
//				public void fromJsonMap(JsonMap jsonMap, int flag) {
//					
//				}
//			});
//		}catch(BusinessException e){
//			e.printStackTrace();
//			jobject.initTip(e);
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
	
	
	public ActionForward delete(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		String promotionId = request.getParameter("promotionId");
		
		JObject jobject = new JObject();
		try{
			PromotionDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(promotionId));
			
			jobject.initTip(true, "活动删除成功");
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
	
	public ActionForward getPromotionTree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final String rule = request.getParameter("rule");
		
		final PromotionDao.ExtraCond extra = new PromotionDao.ExtraCond();
		if(rule != null && !rule.isEmpty() && !rule.equals("-1")){
			extra.setRule(Promotion.Rule.valueOf(Integer.parseInt(rule)));
		}
		
		StringBuilder promotionTree = new StringBuilder();
		try{
			
			String created = children(staff, extra.setStatus(Status.CREATED));
			if(!created.isEmpty()){
				promotionTree.append("{")
				.append("text:'已创建'")
				.append(", id : -2")
				.append(", status : " + Promotion.Status.CREATED.getVal())
				.append(", expanded : true")
				.append(", children : [" + created + "]") 
				.append("}");						
			}

			String progress = children(staff, extra.setStatus(Status.PROGRESS));
			if(!progress.isEmpty()){
				if(!promotionTree.toString().isEmpty()){
					promotionTree.append(",");
				}
				promotionTree.append("{")
				.append("text:'进行中'")
				.append(", id : -3")
				.append(", status : " + Promotion.Status.PROGRESS.getVal())
				.append(", expanded : true")
				.append(", children : [" + progress + "]") 
				.append("}");	
			}

			String finish = children(staff, extra.setStatus(Status.FINISH));
			if(!finish.isEmpty()){
				if(!promotionTree.toString().isEmpty()){
					promotionTree.append(",");
				}
				promotionTree.append("{")
				.append("text:'已结束'")
				.append(", id : -4")
				.append(", status : " + Promotion.Status.FINISH.getVal())
				.append(", expanded : true")
				.append(", children : [" + finish + "]") 
				.append("}");	
			}
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + promotionTree.toString() + "]");
		}
		
		return null;		
		
	}	
	
	private String children(Staff staff, PromotionDao.ExtraCond extra) throws SQLException, BusinessException{
		final List<Promotion> promotions = PromotionDao.getByCond(staff, extra);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < promotions.size(); i++) {
			if(i > 0){
				sb.append(",");
			}
			String text = promotions.get(i).getTitle();
//			if(promotions.get(i).getType() == Promotion.Type.WELCOME){
//				text += "<font color=\"red\"> -- 欢迎活动</font>";
//			}
			
//			if(promotions.get(i).getStatus() == Promotion.Status.FINISH){
//				text += "<font color=\"red\"> -- 已过期</font>";
//			}
			
			sb.append("{")
			.append("text:'" + text + "'")
			.append(",leaf:true")
			.append(",status:" + promotions.get(i).getStatus().getVal())
			.append(",title:'" + promotions.get(i).getTitle() + "'")
			.append(",pRule:" + promotions.get(i).getRule().getVal())
			.append(",pType:" + promotions.get(i).getType().getVal())
			.append(",id:" + promotions.get(i).getId())
			.append("}");
		}
		return sb.toString();	

	}	
	
}
