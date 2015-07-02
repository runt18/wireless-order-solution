package com.wireless.Actions.weixin.query;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.book.BookDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;

public class WXQueryBookAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String name = request.getParameter("name");
		String phone = request.getParameter("phone");
		String status = request.getParameter("status");
		String bookDate = request.getParameter("bookDate");
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			String fid = request.getParameter("fid");
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, fid);
			Staff staff = StaffDao.getAdminByRestaurant(dbCon, rid);
			
			BookDao.ExtraCond extra = new BookDao.ExtraCond();
			
			if(name != null && !name.isEmpty()){
				extra.setMember(name);
			}
			if(phone != null && !phone.isEmpty()){
				extra.setTele(phone);
			}
			if(status != null && !status.isEmpty() && !status.equals("-1")){
				extra.setStatus(Book.Status.valueOf(Integer.parseInt(status)));
			}
			if(bookDate != null && !bookDate.isEmpty()){
				extra.setBookDate(bookDate);
			}
			
			List<Book> bookList = BookDao.getByCond(dbCon, staff, extra);
			
			jobject.setRoot(bookList);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
		
	
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
