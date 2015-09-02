package com.wireless.Actions.couponMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.member.MemberCondDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.Promotion.Status;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class OperatePromotionAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		String title = request.getParameter("title");
//		String beginDate = request.getParameter("beginDate");
//		String endDate = request.getParameter("endDate");
		String body = request.getParameter("body");
		String entire = request.getParameter("entire");
		String pType = request.getParameter("pRule");
		String point = request.getParameter("point");
//		String members = request.getParameter("members");
//		String oriented = request.getParameter("oriented");
		
		String couponName = request.getParameter("couponName");
		String price = request.getParameter("price");
		String expiredDate = request.getParameter("expiredDate");
		String image = request.getParameter("image");		
		
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			
			Promotion.CreateBuilder promotionCreateBuilder;
			
			if(Promotion.Rule.valueOf(Integer.parseInt(pType)) == Promotion.Rule.DISPLAY_ONLY){
				promotionCreateBuilder = Promotion.CreateBuilder.newInstance4Display(title, body, entire)
																//.setRange(beginDate, endDate)
																;
			}else{
				CouponType.InsertBuilder typeInsertBuilder = new CouponType.InsertBuilder(couponName, Integer.parseInt(price), DateUtil.parseDate(expiredDate)).setComment("活动优惠劵");
				if(image != null && !image.isEmpty()){
					typeInsertBuilder.setImage(Integer.parseInt(image));
				}
				
				promotionCreateBuilder = Promotion.CreateBuilder.newInstance(title, body, Promotion.Rule.valueOf(Integer.parseInt(pType)), typeInsertBuilder, entire)
															    //.setRange(beginDate, endDate)
															    ;
				if(point != null && !point.isEmpty() && Promotion.Rule.valueOf(Integer.parseInt(pType)) != Promotion.Rule.FREE){
					promotionCreateBuilder.setPoint(Integer.parseInt(point));
				}
			}				
			
//			if(Integer.parseInt(oriented) == Promotion.Oriented.SPECIFIC.getVal() && members != null){
//				String[] memberList = members.split(",");
//				for (String member : memberList) {
//					promotionCreateBuilder.addMember(Integer.parseInt(member));
//				}				
//			}

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

	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pId = request.getParameter("id");
		String title = request.getParameter("title");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String body = request.getParameter("body");
		String entire = request.getParameter("entire");
		//String rule = request.getParameter("pRule");
		//String point = request.getParameter("point");
		//String members = request.getParameter("members");
		//String oriented = request.getParameter("oriented");
		
		String couponTypeId = request.getParameter("cId");
		String couponName = request.getParameter("couponName");
		String price = request.getParameter("price");
		String expiredDate = request.getParameter("expiredDate");
		String image = request.getParameter("image");		
		
		String orientedId = request.getParameter("oriented");
		String condId = request.getParameter("condId");
		
		String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		JObject jobject = new JObject();
		try{
			
			final Promotion.UpdateBuilder promotionUpdateBuilder = new Promotion.UpdateBuilder(Integer.parseInt(pId));
			
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
			
			//优惠活动发布对象
			if(orientedId != null && !orientedId.isEmpty()){
				Promotion.Oriented oriented = Promotion.Oriented.valueOf(Integer.parseInt(orientedId));
				if(oriented == Promotion.Oriented.ALL){
					promotionUpdateBuilder.setAllMember();
					
				}else if(oriented == Promotion.Oriented.EMPTY){
					promotionUpdateBuilder.setMemberEmpty();
					
				}else if(oriented == Promotion.Oriented.SPECIFIC){
					for(Member member :	MemberDao.getByCond(staff, new MemberDao.ExtraCond(MemberCondDao.getById(staff, Integer.parseInt(condId))), null)){
						promotionUpdateBuilder.addMember(member);
					}
				}
			}
			
			PromotionDao.update(staff, promotionUpdateBuilder);
			
			jobject.initTip(true, "活动修改成功");
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
	
	public ActionForward getPromotion(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		String pin = (String) request.getAttribute("pin");
		String openId = request.getParameter("fid");
		
		int rid = 0;
		Staff staff;
		if(pin != null){
			staff = StaffDao.verify(Integer.parseInt(pin));
		}else{
			rid = WxRestaurantDao.getRestaurantIdByWeixin(openId);
			staff = StaffDao.getAdminByRestaurant(rid);
		}
		String promotionId = request.getParameter("promotionId");
		
		JObject jobject = new JObject();
		try{
			Promotion promo = PromotionDao.getById(staff, Integer.parseInt(promotionId));
			jobject.setRoot(promo);
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
			PromotionDao.delete(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(promotionId));
			
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
		String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		String rule = request.getParameter("rule");
		
		PromotionDao.ExtraCond extra = new PromotionDao.ExtraCond();
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
	
	private String children(Staff staff, PromotionDao.ExtraCond extra) throws SQLException{
		List<Promotion> promotions = PromotionDao.getByCond(staff, extra);
		StringBuilder sb = new StringBuilder();
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
