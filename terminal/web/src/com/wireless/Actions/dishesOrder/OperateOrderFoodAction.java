package com.wireless.Actions.dishesOrder;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqGiftOrderFood;
import com.wireless.pack.req.ReqTransFood;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.Order.CommentBuilder;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.sccon.ServerConnector;

public class OperateOrderFoodAction extends DispatchAction{
	
	/**
	 * 账单使用优惠券
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward coupon(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String) request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final String orderId = request.getParameter("orderId");
		final String coupons = request.getParameter("coupons");
		
		JObject jobject = new JObject();
		try{
			Order.CouponBuilder builder = new Order.CouponBuilder(Integer.parseInt(orderId));
			if(coupons != null && !coupons.isEmpty()){
				for(String couponId : coupons.split(",")){
					builder.addCoupon(Integer.parseInt(couponId));
				}
			}			
			OrderDao.coupon(staff, builder);
			
		}catch(BusinessException | SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 转菜
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward transFood(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String orderId = request.getParameter("orderId");
		String tableId = request.getParameter("tableId");
		String transFoods = request.getParameter("transFoods");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try {
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Order.TransferBuilder builder = new Order.TransferBuilder(Integer.parseInt(orderId), new Table.Builder(Integer.parseInt(tableId)));
			
			Order actualOrder = OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY);
			
			if(transFoods != null && transFoods.equals("-1")){
				for (OrderFood of : actualOrder.getOrderFoods()) {
					builder.add(of);
				}				
			}else{
				String transFood[] = transFoods.split(",");
				for (OrderFood of : actualOrder.getOrderFoods()) {
					if(of.getId() == Integer.parseInt(transFood[0])){
						of.setCount(Float.parseFloat(transFood[1]));
						builder.add(of);
					}
				}				
			}
			

			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTransFood(staff, builder));
			
			if(resp.header.type == Type.ACK){
				jobject.initTip(true, ("转菜成功."));
				
			}else if(resp.header.type == Type.NAK){
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				jobject.initTip(false, errCode.getCode(), errCode.getDesc());
				
			}else{
				jobject.initTip(false, ("转菜失败，请重新确认."));
			}
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(IOException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9997, "服务器请求不成功，请重新检查网络是否连通.");
			e.printStackTrace();
		}catch(NumberFormatException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9998, "菜品提交的数量不正确，请检查后重新提交.");
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}		
		return null;
	}
	
	/**
	 * 已点菜赠送
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward giftOrderFood(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String orderId = request.getParameter("orderId");
		String giftFood = request.getParameter("giftFood");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try {
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Order.GiftBuilder builder = null;
			
			Order actualOrder = OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY);
			
			String giftFoods[] = giftFood.split(",");
			for (OrderFood of : actualOrder.getOrderFoods()) {
				if(of.getFoodId() == Integer.parseInt(giftFoods[0]) && !of.isGift()){
					of.setCount(Float.parseFloat(giftFoods[1]));
					builder = new Order.GiftBuilder(Integer.parseInt(orderId), of);
				}
			}				
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqGiftOrderFood(staff, builder));
			
			if(resp.header.type == Type.ACK){
				jobject.initTip(true, ("赠送成功."));
				
			}else if(resp.header.type == Type.NAK){
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				jobject.initTip(false, errCode.getCode(), errCode.getDesc());
				
			}else{
				jobject.initTip(false, ("赠送失败，请重新确认."));
			}
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(IOException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9997, "服务器请求不成功，请重新检查网络是否连通.");
			e.printStackTrace();
		}catch(NumberFormatException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9998, "菜品提交的数量不正确，请检查后重新提交.");
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}		
		return null;
	}	
	
	/**
	 * 更新限量沽清菜品
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateFoodLimit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String foodId = request.getParameter("foodId");
		String limitAmount = request.getParameter("amount");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try {
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			FoodDao.update(staff, new Food.LimitRemainingBuilder(new Food(Integer.parseInt(foodId)), Integer.parseInt(limitAmount)));
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}		
		return null;
	}	
	
	/**
	 * 修改备注
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateComment(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String orderId = request.getParameter("orderId");
		String comment = request.getParameter("comment");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try {
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			OrderDao.comment(staff, new CommentBuilder(Integer.parseInt(orderId), comment));
			jobject.initTip(true, "备注成功");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}		
		return null;
	}	
	
	/**
	 * 多台开席
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward multiOpenTable(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		String multiTableOrderFoodsString = request.getParameter("multiTableOrderFoods");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try {
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String[] multiTableOrderFoods = multiTableOrderFoodsString.split("<li>");
			
			Order.InsertMultiBuilder builder = new Order.InsertMultiBuilder();
			for (int i = 0; i < multiTableOrderFoods.length; i++) {
				Order.InsertBuilder insertBuilder = JObject.parse(Order.InsertBuilder.JSON_CREATOR, 0, multiTableOrderFoods[i]);
				builder.add(insertBuilder);
			}
			OrderDao.insertMulti(staff, builder);
			
			jobject.initTip(true, "多台开席成功");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}		
		return null;
	}	
	
	/**
	 * 设置服务费方案
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward service(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		
		try{
			
			final String pin = (String)request.getAttribute("pin");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			int servicePlanId = Integer.parseInt(request.getParameter("planId"));
			int orderId = Integer.parseInt(request.getParameter("orderId"));
			
			OrderDao.service(staff, new Order.ServiceBuilder(orderId, servicePlanId));
			
			jobject.initTip(true, "设置服务费方案成功");
		
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
}
