package com.wireless.Actions.weixin.operate;

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
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.json.JObject;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;

public class WXOperateBookAction extends DispatchAction {
	
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
}
