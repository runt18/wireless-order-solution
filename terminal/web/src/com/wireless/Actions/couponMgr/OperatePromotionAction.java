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

import com.wireless.db.DBCon;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.Promotion.Status;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class OperatePromotionAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String title = request.getParameter("title");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String body = request.getParameter("body");
		String pType = request.getParameter("pType");
		String point = request.getParameter("point");
		String members = request.getParameter("members");
		
		String couponName = request.getParameter("couponName");
		String price = request.getParameter("price");
		String expiredDate = request.getParameter("expiredDate");
		String image = request.getParameter("image");		
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			body = body.replaceAll("&", "&amp;")
					   .replaceAll("<", "&lt;")
					   .replaceAll(">", "&gt;")
					   .replaceAll("\"", "&quot;")
					   .replaceAll("\n\r", "&#10;")
					   .replaceAll("\r\n", "&#10;")
					   .replaceAll("\n", "&#10;")
					   .replaceAll(" ", "&#032;")
					   .replaceAll("'", "&#039;")
					   .replaceAll("!", "&#033;");
			
			
			Promotion.CreateBuilder promotionCreateBuilder;
			if(Promotion.Type.valueOf(Integer.parseInt(pType)) == Promotion.Type.DISPLAY_ONLY){
				promotionCreateBuilder = Promotion.CreateBuilder.newInstance(title, new DateRange(beginDate, endDate), body);
			}else{
//				CouponType.InsertBuilder typeInsertBuilder = new CouponType.InsertBuilder(couponName, Integer.parseInt(price)).setComment("活动优惠劵").setImage(image).setExpired(DateUtil.parseDate(expiredDate));
				promotionCreateBuilder = Promotion.CreateBuilder.newInstance(title, new DateRange(beginDate, endDate), body, Promotion.Type.valueOf(Integer.parseInt(pType)), new CouponType.InsertBuilder(couponName, Integer.parseInt(price)).setComment("活动优惠劵").setImage(image).setExpired(DateUtil.parseDate(expiredDate)));
				if(point != null && !point.isEmpty()){
					promotionCreateBuilder.setPoint(Integer.parseInt(point));
				}
				
			}
			
			
			String[] memberList = members.split(",");
			
			for (String member : memberList) {
				promotionCreateBuilder.addMember(Integer.parseInt(member));
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
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}

	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pId = request.getParameter("id");
		String title = request.getParameter("title");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String body = request.getParameter("body");
		String pType = request.getParameter("pType");
		String point = request.getParameter("point");
		String members = request.getParameter("members");
		
		String couponTypeId = request.getParameter("cId");
		String couponName = request.getParameter("couponName");
		String price = request.getParameter("price");
		String expiredDate = request.getParameter("expiredDate");
		String image = request.getParameter("image");		
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			body = body.replaceAll("&", "&amp;")
					   .replaceAll("<", "&lt;")
					   .replaceAll(">", "&gt;")
					   .replaceAll("\"", "&quot;")
					   .replaceAll("\n\r", "&#10;")
					   .replaceAll("\r\n", "&#10;")
					   .replaceAll("\n", "&#10;")
					   .replaceAll(" ", "&#032;")
					   .replaceAll("'", "&#039;")
					   .replaceAll("!", "&#033;");
			
			
			Promotion.UpdateBuilder promotionUpdateBuilder;
			if(Promotion.Type.valueOf(Integer.parseInt(pType)) == Promotion.Type.DISPLAY_ONLY){
				promotionUpdateBuilder = new Promotion.UpdateBuilder(Integer.parseInt(pId)).setRange(new DateRange(beginDate, endDate))
										 .setTitle(title)
										 .setBody(body);
			}else{
				CouponType.UpdateBuilder typeUpdateBuilder = new CouponType.UpdateBuilder(Integer.parseInt(couponTypeId), couponName).setComment("").setPrice(Integer.parseInt(price)).setExpired(expiredDate);
				if(image != null && !image.isEmpty()){
					typeUpdateBuilder.setImage(image);
				}
				promotionUpdateBuilder = new Promotion.UpdateBuilder(Integer.parseInt(pId)).setRange(new DateRange(beginDate, endDate))
										 .setTitle(title)
										 .setBody(body)
										 .setCouponTypeBuilder(typeUpdateBuilder);
				if(point != null && !point.isEmpty()){
					promotionUpdateBuilder.setPoint(Integer.parseInt(point));
				}
			}
			
			
			String[] memberList = members.split(",");
			
			for (String member : memberList) {
				promotionUpdateBuilder.addMember(Integer.parseInt(member));
			}
			
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
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}	
	
	public ActionForward getPromotion(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String formId = request.getParameter("fid");
		
		int rid = 0;
		Staff staff;
		if(pin != null){
			staff = StaffDao.verify(Integer.parseInt(pin));
		}else{
			DBCon dbCon = new DBCon();
			dbCon.connect();
			rid = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, formId);
			staff = StaffDao.getByRestaurant(dbCon, rid).get(0);
			dbCon.disconnect();
		}
		String promotionId = request.getParameter("promotionId");
		
		JObject jobject = new JObject();
		try{
			Promotion promo = PromotionDao.getById(staff, Integer.parseInt(promotionId));
			
			String image = "http://" + getServlet().getInitParameter("oss_bucket_image")
	        		+ "." + getServlet().getInitParameter("oss_outer_point") 
	        		+ "/" + staff.getRestaurantId() + "/" + promo.getCouponType().getImage();
			
			promo.getCouponType().setImage(image);
			List<Promotion> p_List = new ArrayList<>();
			p_List.add(promo);
			jobject.setRoot(p_List);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
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
			
			if(p.getType() != Promotion.Type.DISPLAY_ONLY){
				String image = "http://" + getServlet().getInitParameter("oss_bucket_image")
		        		+ "." + getServlet().getInitParameter("oss_outer_point") 
		        		+ "/" + staff.getRestaurantId() + "/" + p.getCouponType().getImage();
				
				p.getCouponType().setImage(image);				
			}

			
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
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;		
		
	}		
	
	public ActionForward publish(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String promotionId = request.getParameter("promotionId");
		
		JObject jobject = new JObject();
		try{
			PromotionDao.publish(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(promotionId));
			
			jobject.initTip(true, "活动发布成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;		
		
	}		
	
	public ActionForward cancelPublish(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String promotionId = request.getParameter("promotionId");
		
		JObject jobject = new JObject();
		try{
			PromotionDao.cancelPublish(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(promotionId));
			
			jobject.initTip(true, "活动撤销成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;		
		
	}	
	
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
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;		
		
	}		
	
	public ActionForward finish(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String promotionId = request.getParameter("promotionId");
		
		JObject jobject = new JObject();
		try{
			PromotionDao.finish(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(promotionId));
			
			jobject.initTip(true, "活动结束成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;		
		
	}		
	
	public ActionForward getPromotionTree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		StringBuilder pTree = new StringBuilder();
		try{
			
				String p_create = children(staff, new PromotionDao.ExtraCond().setStatus(Status.CREATED));
				if(!p_create.isEmpty()){
					pTree.append("{")
					.append("text:'已创建'")
					.append(", status : " + Promotion.Status.CREATED.getVal())
					.append(",expanded:true")
					.append(",children:[" + p_create + "]") 
					.append("}");						
				}

				
				String publish = children(staff, new PromotionDao.ExtraCond().setStatus(Status.PUBLISH));
				if(!publish.isEmpty()){
					if(!pTree.toString().isEmpty()){
						pTree.append(",");
					}
					pTree.append("{")
					.append("text:'已发布'")
					.append(", status : " + Promotion.Status.PUBLISH.getVal())
					.append(",expanded:true")
					.append(",children:[" + publish + "]") 
					.append("}");	
				}
				
				String progress = children(staff, new PromotionDao.ExtraCond().setStatus(Status.PROGRESS));
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
	
				String finish = children(staff, new PromotionDao.ExtraCond().setStatus(Status.FINISH));
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
		List<Promotion> p_List = PromotionDao.getByCond(staff, extra);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < p_List.size(); i++) {
			if(i > 0){
				sb.append(",");
			}
			sb.append("{")
			.append("text:'" + p_List.get(i).getTitle() + "'")
			.append(",leaf:true")
			.append(",status:" + p_List.get(i).getStatus().getVal())
			.append(",title:'" + p_List.get(i).getTitle() + "'")
			.append(",pType:" + p_List.get(i).getType().getVal())
			.append(",id:" + p_List.get(i).getId())
			.append("}");
		}
		return sb.toString();	

	}	
	
}
