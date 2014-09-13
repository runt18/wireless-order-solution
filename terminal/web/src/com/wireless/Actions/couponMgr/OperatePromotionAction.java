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
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DateRange;
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
			
			CouponType.InsertBuilder typeInsertBuilder = new CouponType.InsertBuilder(couponName, Integer.parseInt(price)).setComment("活动优惠劵").setImage(image).setExpired(DateUtil.parseDate(expiredDate));
			Promotion.CreateBuilder promotionCreateBuilder = new Promotion.CreateBuilder(title, new DateRange(beginDate, endDate), body, typeInsertBuilder)
					.setPoint(point != null && !point.isEmpty()?Integer.parseInt(point):0)
			  		.setType(Promotion.Type.valueOf(Integer.parseInt(pType))
);
			
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
	
	public ActionForward getPromotionTree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		StringBuilder pTree = new StringBuilder();
		try{
				pTree.append("{")
				.append("text:'已创建'")
				.append(", status : " + Promotion.Status.CREATED.getVal())
				.append(",expanded:true")
				.append(",children:[" + children(staff, new PromotionDao.ExtraCond().setStatus(Status.CREATED)) + "]") 
				.append("}");	
				
				String publish = children(staff, new PromotionDao.ExtraCond().setStatus(Status.PUBLISH));
				if(!publish.isEmpty()){
					pTree.append(",{")
					.append("text:'已发布'")
					.append(", status : " + Promotion.Status.PUBLISH.getVal())
					.append(",expanded:true")
					.append(",children:[" + publish + "]") 
					.append("}");	
				}
				
				String progress = children(staff, new PromotionDao.ExtraCond().setStatus(Status.PROGRESS));
				if(!progress.isEmpty()){
					pTree.append(",{")
					.append("text:'进行中'")
					.append(", status : " + Promotion.Status.PROGRESS.getVal())
					.append(",expanded:true")
					.append(",children:[" + progress + "]") 
					.append("}");	
				}
	
				String finish = children(staff, new PromotionDao.ExtraCond().setStatus(Status.FINISH));
				if(!finish.isEmpty()){
					pTree.append(",{")
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
