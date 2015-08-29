package com.wireless.Actions.weixin.query;

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

import com.wireless.db.book.BookDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WXQueryBookAction extends DispatchAction{
	
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
	public ActionForward bookList(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
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
			DutyRange duty = new DutyRange(begin, end);
			
			extra.setBookRange(duty);
			extra.addStatus(Book.Status.CREATED);
			
			List<Book> list = BookDao.getByCond(staff, extra);
			
			//获取详细的order & orderFoods信息
			List<Book> books = new ArrayList<>();
			for (Book book : list) {
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
