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
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.Promotion.Status;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class OperatePromotionAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		String title = request.getParameter("title");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String body = request.getParameter("body");
		String entire = request.getParameter("entire");
		String pType = request.getParameter("pRule");
		String point = request.getParameter("point");
		String members = request.getParameter("members");
		String oriented = request.getParameter("oriented");
		
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
																.setRange(beginDate, endDate);
			}else{
				CouponType.InsertBuilder typeInsertBuilder = new CouponType.InsertBuilder(couponName, Integer.parseInt(price), DateUtil.parseDate(expiredDate)).setComment("活动优惠劵");
				if(image != null && !image.isEmpty()){
					typeInsertBuilder.setImage(Integer.parseInt(image));
				}
				
				promotionCreateBuilder = Promotion.CreateBuilder.newInstance(title, 
						 body,
						 Promotion.Rule.valueOf(Integer.parseInt(pType)),
						 typeInsertBuilder,
						 entire).setRange(beginDate, endDate);
				if(point != null && !point.isEmpty() && Promotion.Rule.valueOf(Integer.parseInt(pType)) != Promotion.Rule.FREE){
					promotionCreateBuilder.setPoint(Integer.parseInt(point));
				}
			}				
			
			if(Integer.parseInt(oriented) == Promotion.Oriented.SPECIFIC.getVal() && members != null){
				String[] memberList = members.split(",");
				for (String member : memberList) {
					promotionCreateBuilder.addMember(Integer.parseInt(member));
				}				
			}

			PromotionDao.create(StaffDao.verify(Integer.parseInt(pin)), promotionCreateBuilder);
			
			jobject.initTip(true, "活动创建成功");
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
		String pRule = request.getParameter("pRule");
		String point = request.getParameter("point");
		//String members = request.getParameter("members");
		//String oriented = request.getParameter("oriented");
		
		String couponTypeId = request.getParameter("cId");
		String couponName = request.getParameter("couponName");
		String price = request.getParameter("price");
		String expiredDate = request.getParameter("expiredDate");
		String image = request.getParameter("image");		
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			
			
			Promotion.UpdateBuilder promotionUpdateBuilder;
			if(Promotion.Rule.valueOf(Integer.parseInt(pRule)) == Promotion.Rule.DISPLAY_ONLY){
				promotionUpdateBuilder = new Promotion.UpdateBuilder(Integer.parseInt(pId)).setRange(beginDate, endDate)
										 .setTitle(title)
										 .setBody(body, entire);
			}else{
				CouponType.UpdateBuilder typeUpdateBuilder = new CouponType.UpdateBuilder(Integer.parseInt(couponTypeId), couponName).setComment("").setPrice(Integer.parseInt(price)).setExpired(expiredDate);
				if(image != null && !image.isEmpty()){
					typeUpdateBuilder.setImage(new OssImage(Integer.parseInt(image)));
				}
				promotionUpdateBuilder = new Promotion.UpdateBuilder(Integer.parseInt(pId)).setRange(beginDate, endDate)
										 .setTitle(title)
										 .setBody(body, entire)
										 .setCouponTypeBuilder(typeUpdateBuilder);
				if(point != null && !point.isEmpty()){
					promotionUpdateBuilder.setPoint(Integer.parseInt(point));
				}
			}
			
			
//			String[] memberList = members.split(",");
//			
//			if(Integer.parseInt(oriented) == Promotion.Oriented.SPECIFIC.getVal()){
//				for (String member : memberList) {
//					promotionUpdateBuilder.addMember(Integer.parseInt(member));
//				}
//			}else{
//				promotionUpdateBuilder.setAllMember();
//			}
			
			PromotionDao.update(StaffDao.verify(Integer.parseInt(pin)), promotionUpdateBuilder);
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
	
	public ActionForward getPromotion(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
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
			List<Promotion> promotions = new ArrayList<>();
			promotions.add(promo);
			jobject.setRoot(promotions);
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
	
	public ActionForward getById(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		String promotionId = request.getParameter("promotionId");
		
		JObject jobject = new JObject();
		try{
			final Promotion p = PromotionDao.getById(staff, Integer.parseInt(promotionId));
			
			final Promotion promo = p;
			List<Coupon> p_List = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setPromotion(p.getId()), null);
			List<Member> members = new ArrayList<>();
			for (Coupon coupon : p_List) {
				members.add(coupon.getMember());
			}
			final List<Member> memberList = members;
			jobject.setExtra(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable(promo, 0);
					jm.putJsonableList("members", memberList, 0);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
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
	
//	public ActionForward publish(ActionMapping mapping, ActionForm form,
//			HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//		String pin = (String) request.getAttribute("pin");
//		String promotionId = request.getParameter("promotionId");
//		
//		JObject jobject = new JObject();
//		try{
//			PromotionDao.publish(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(promotionId));
//			
//			jobject.initTip(true, "活动发布成功");
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
	
//	public ActionForward cancelPublish(ActionMapping mapping, ActionForm form,
//			HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//		String pin = (String) request.getAttribute("pin");
//		String promotionId = request.getParameter("promotionId");
//		
//		JObject jobject = new JObject();
//		try{
//			PromotionDao.cancelPublish(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(promotionId));
//			
//			jobject.initTip(true, "活动撤销成功");
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
	
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
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
	
//	public ActionForward finish(ActionMapping mapping, ActionForm form,
//			HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//		String pin = (String) request.getAttribute("pin");
//		String promotionId = request.getParameter("promotionId");
//		
//		JObject jobject = new JObject();
//		try{
//			PromotionDao.finish(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(promotionId));
//			
//			jobject.initTip(true, "活动结束成功");
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
	
	public ActionForward getPromotionTree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		StringBuilder pTree = new StringBuilder();
		try{
			
				String p_create = children(staff, new PromotionDao.ExtraCond().addStatus(Status.CREATED));
				if(!p_create.isEmpty()){
					pTree.append("{")
					.append("text:'已创建'")
					.append(", status : " + Promotion.Status.CREATED.getVal())
					.append(",expanded:true")
					.append(",children:[" + p_create + "]") 
					.append("}");						
				}

				
				String progress = children(staff, new PromotionDao.ExtraCond().addStatus(Status.PROGRESS));
				if(!progress.isEmpty()){
					if(!pTree.toString().isEmpty()){
						pTree.append(",");
					}
					pTree.append("{")
					.append("text:'进行中'")
					.append(", status : " + Promotion.Status.PROGRESS.getVal())
					.append(",expanded:true")
					.append(",children:[" + progress + "]") 
					.append("}");	
				}
	
				String finish = children(staff, new PromotionDao.ExtraCond().addStatus(Status.FINISH));
				if(!finish.isEmpty()){
					if(!pTree.toString().isEmpty()){
						pTree.append(",");
					}
					pTree.append("{")
					.append("text:'已结束'")
					.append(", status : " + Promotion.Status.FINISH.getVal())
					.append(",expanded:true")
					.append(",children:[" + finish + "]") 
					.append("}");	
				}
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + pTree.toString() + "]");
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
			
			if(promotions.get(i).getStatus() == Promotion.Status.FINISH){
				text += "<font color=\"red\"> -- 已过期</font>";
			}
			
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
	
	/**
	 * 获取欢迎活动
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
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
//			if(list.size() > 0){
//				list.get(0).setCouponType(CouponTypeDao.getById(staff, list.get(0).getCouponType().getId()));
//			}
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
	
}
