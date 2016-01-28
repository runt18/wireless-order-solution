package com.wireless.Actions.weixin.book;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.wireless.db.DBCon;
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
		final String wxPayMoney = request.getParameter("wxPayMoney");
		final String fid = request.getParameter("fid");
		//final String openId = request.getParameter("oid");
		try{
			final int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(fid);
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

			jObject.setRoot(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putInt("bookId", bookId);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					
				}
				
			});
			
			if(wxPayMoney != null && !wxPayMoney.isEmpty() && Float.parseFloat(wxPayMoney) > 0){
				//TODO
			}else{
				//打印预订信息
				ProtocolPackage resp = ServerConnector.instance().ask(ReqPrintContent.buildBook(staff, bookId).build());
				if(resp.header.type == Type.ACK){
					jObject.initTip(true, "预订信息打印成功");
				}else{
					jObject.initTip(true, "预订成功");
				}
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
		final String wxPayMoney = request.getParameter("wxPayMoney");
		try{
			final int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			final Staff staff = StaffDao.getAdminByRestaurant(restaurantId);

			if(wxPayMoney != null && !wxPayMoney.isEmpty()){
				Restaurant restaurant = RestaurantDao.getById(restaurantId);
				if(restaurant.hasBeeCloud()){
					BeeCloud app = BeeCloud.registerApp(restaurant.getBeeCloudAppId(), restaurant.getBeeCloudAppSecret());
					final String billNo = System.currentTimeMillis() + "";
					Bill.Response beeCloudResponse = app.bill().ask(new Bill.Request().setChannel(Bill.Channel.WX_JSAPI)
																					  .setOpenId(openId)
																					  .setBillNo(billNo)
																					  .setTotalFee(Integer.parseInt(wxPayMoney))
																					  .setTitle(restaurant.getName() + "微信预订支付(" + bookId + ")"), 
					new Callable<ProtocolPackage>(){

						@Override
						public ProtocolPackage call() throws Exception {
							System.out.println("wx pay success");
							try{
								//更新预订账单中的微信支付的金额状态
								BookDao.update(staff, new Book.WxPayBuilder(Integer.parseInt(bookId), Float.parseFloat(wxPayMoney)));
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
		JObject jobject = new JObject();
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			String fid = request.getParameter("fid");
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, fid);
			Staff staff = StaffDao.getAdminByRestaurant(dbCon, rid);
			jobject.setRoot(RegionDao.getByStatus(staff, Region.Status.BUSY));
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
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
		JObject jobject = new JObject();
		String id = request.getParameter("bookId");
		String fid = request.getParameter("fid");
		final WxRestaurant wxRest;
		final Restaurant rest;
		try{
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			rest = RestaurantDao.getById(rid);
			wxRest = WxRestaurantDao.get(staff);
			jobject.setRoot(BookDao.getById(staff, Integer.parseInt(id)));
			
			jobject.setExtra(new Jsonable() {
				
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
	 * 获取微信预定列表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String fid = request.getParameter("fid");
		try{
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			BookDao.ExtraCond extra = new BookDao.ExtraCond();
			
			SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");
			String begin = yyyymmdd.format(new Date());
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, 1);
			String end = yyyymmdd.format(c.getTime());
			
			extra.setBookRange(new DutyRange(begin, end));
			extra.addStatus(Book.Status.CREATED);
			
			//获取详细的order & orderFoods信息
			List<Book> books = new ArrayList<>();
			for (Book book : BookDao.getByCond(staff, extra)) {
				books.add(BookDao.getById(staff, book.getId()));
			}
			
			jobject.setRoot(books);
			
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
