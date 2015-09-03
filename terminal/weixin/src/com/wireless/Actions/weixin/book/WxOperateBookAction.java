package com.wireless.Actions.weixin.book;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

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
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WxOperateBookAction extends DispatchAction {
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String bookDate = request.getParameter("bookDate");
		String member = request.getParameter("member");
		String phone = request.getParameter("phone");
		String count = request.getParameter("count");
		String region = request.getParameter("region");
		String foods = request.getParameter("foods");
		try{
			int restaurantId = WxRestaurantDao.getRestaurantIdByWeixin(request.getParameter("fid"));
			Staff staff = StaffDao.getAdminByRestaurant(restaurantId);
			Book.InsertBuilder4Weixin insertBuilder = new Book.InsertBuilder4Weixin().setBookDate(bookDate)
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
			
			
			BookDao.insert(staff, insertBuilder);
			jobject.setSuccess(true);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
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
