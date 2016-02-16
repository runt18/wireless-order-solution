package com.wireless.Actions.weixin.book;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.beeCloud.BeeCloud;
import com.wireless.beeCloud.Bill;
import com.wireless.db.book.BookDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.sccon.ServerConnector;

public class WxOperateBookAction extends DispatchAction {
	
	/**
	 * 新建微信预订
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jObject = new JObject();
		final String bookDate = request.getParameter("bookDate");
		final String member = request.getParameter("member");
		final String phone = request.getParameter("phone");
		final String count = request.getParameter("count");
		final String region = request.getParameter("region");
		final String foods = request.getParameter("foods");
		final String wxPay = request.getParameter("wxPay");
		final String fid = request.getParameter("fid");
		final String branchId = request.getParameter("branchId");
		//final String openId = request.getParameter("oid");
		try{
			final int restaurantId;
			if(branchId != null && !branchId.isEmpty()){
				restaurantId = Integer.parseInt(branchId);
			}else{
				restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			}	
			final Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
			final Book.InsertBuilder4Weixin insertBuilder = new Book.InsertBuilder4Weixin().setBookDate(bookDate)
															  .setMember(member)
															  .setTele(phone)
															  .setAmount(Integer.parseInt(count))
															  .setRegion(region);
			if(foods != null && !foods.isEmpty()){
				for (String of : foods.split("&")) {
					String orderFoods[] = of.split(",");
					OrderFood orderFood = new OrderFood(FoodDao.getById(staff, Integer.parseInt(orderFoods[0])));
					orderFood.setCount(Float.parseFloat(orderFoods[1]));
					
					insertBuilder.addOrderFood(orderFood, staff);
				}
			}

			final int bookId = BookDao.insert(staff, insertBuilder);

			final JsonMap result = new JsonMap();
			result.putInt("bookId", bookId);
			
			if(wxPay != null && !wxPay.isEmpty() && Boolean.parseBoolean(wxPay)){
				result.putBoolean("wxPay", true);
			}else{
				//打印预订信息
				try{
					ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildBook(staff, bookId).build());
					if(resp.header.type == Type.ACK){
						jObject.initTip(true, "预订信息打印成功");
					}else{
						jObject.initTip(true, "预订成功");
					}
				}catch(IOException | BusinessException ignored){
					ignored.printStackTrace();
				}
			}
			
			jObject.setRoot(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					return result;
				}

				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					
				}
				
			});
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 更新预订账单的微信支付金额
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward wxPay(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jObject = new JObject();
		final String fid = request.getParameter("fid");
		final String openId = request.getParameter("oid");
		final String bookId = request.getParameter("bookId");
		final String branchId = request.getParameter("branchId");
		try{
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			}	
			final Staff staff = StaffDao.getAdminByRestaurant(rid);

			final Book book = BookDao.getById(staff, Integer.parseInt(bookId));
			if(book.hasOrder() && book.getOrder().calcTotalPrice() > 0){
				Restaurant restaurant = RestaurantDao.getById(rid);
				if(restaurant.hasBeeCloud()){
					BeeCloud app = BeeCloud.registerApp(restaurant.getBeeCloudAppId(), restaurant.getBeeCloudAppSecret());
					final String billNo = System.currentTimeMillis() + "";
					Bill.Response beeCloudResponse = app.bill().ask(new Bill.Request().setChannel(Bill.Channel.WX_JSAPI)
																					  .setOpenId(openId)
																					  .setBillNo(billNo)
																					  .setTotalFee(Float.valueOf((book.getOrder().calcTotalPrice() * 100)).intValue())
																					  .setTotalFee(1)
																					  .setTitle(restaurant.getName() + "微信预订支付(" + bookId + ")"), 
					new Callable<ProtocolPackage>(){

						@Override
						public ProtocolPackage call() throws Exception {
							try{
								//更新预订账单中的微信支付的金额状态
								BookDao.update(staff, new Book.WxPayBuilder(Integer.parseInt(bookId), book.getOrder().calcTotalPrice()));
							}catch(BusinessException e){
								e.printStackTrace();
							}
							//打印预订信息
							ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildBook(staff, Integer.parseInt(bookId)).build());
//							if(resp.header.type == Type.ACK){
//								jObject.initTip(true, "预订信息打印成功");
//							}else{
//								jObject.initTip(true, "预订成功");
//							}
							return resp;
						}
						
					});
					if(beeCloudResponse.isOk()){
						jObject.setExtra(beeCloudResponse);
					}else{
						throw new BusinessException(beeCloudResponse.getErrDetail() + "," + beeCloudResponse.getResultMsg());
					}
				}else{
					throw new BusinessException("对不起，您的公众号还没开通微信支付");
				}
			}else{
				throw new BusinessException("微信支付的金额不能小于0");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 获取区域
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward region(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String fid = request.getParameter("fid");
		final String branchId = request.getParameter("branchId");
		try{
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			}	
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			jObject.setRoot(RegionDao.getByStatus(staff, Region.Status.BUSY));
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}		
		return null;
	}
	
	/**
	 * 通过id获取邀请函内容
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward invitation(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String id = request.getParameter("bookId");
		final String fid = request.getParameter("fid");
		final String branchId = request.getParameter("branchId");
		try{
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			}	
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			final Restaurant rest = RestaurantDao.getById(rid);
			final WxRestaurant wxRest = WxRestaurantDao.get(staff);
			jObject.setRoot(BookDao.getById(staff, Integer.parseInt(id)));
			
			jObject.setExtra(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("rest", rest, 0);
					jm.putJsonable("wxRest", wxRest, 0);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					
				}
			});
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
	
	/**
	 * 获取微信预定列表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String fid = request.getParameter("fid");
		try{
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			final BookDao.ExtraCond extraCond = new BookDao.ExtraCond();
			
			//只显示当前的预订订单
			SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");
			String begin = yyyymmdd.format(new Date());
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, 1);
			String end = yyyymmdd.format(c.getTime());
			
			extraCond.setBookRange(new DutyRange(begin, end));
			
			//只显示【创建】状态的预订订单
			extraCond.addStatus(Book.Status.CREATED);
			
			final List<Book> result = new ArrayList<Book>();
			
			//集团下需要显示所有门店的预订订单
			if(staff.isGroup()){
				for(Restaurant branches : RestaurantDao.getById(staff.getRestaurantId()).getBranches()){
					result.addAll(BookDao.getByCond(StaffDao.getAdminByRestaurant(branches.getId()), extraCond));
				}
			}
			
			//获取集团的预订订单
			result.addAll(BookDao.getByCond(staff, extraCond));
			
			//按下单日期降序显示
			Collections.sort(result, new Comparator<Book>(){
				@Override
				public int compare(Book o1, Book o2) {
					if(o1.getBookDate() > o2.getBookDate()){
						return -1;
					}else if(o1.getBookDate() < o2.getBookDate()){
						return 1;
					}else{
						return 0;
					}
				}
			});
			
			//获取详细的order & orderFoods信息
			for(int i = 0; i < result.size(); i++) {
				result.set(i, BookDao.getById(StaffDao.getAdminByRestaurant(result.get(i).getRestaurantId()), result.get(i).getId()));
			}
			
			jObject.setRoot(result);
			
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
