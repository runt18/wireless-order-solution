package com.wireless.Actions.bookMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.book.BookDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.staffMgr.Staff;

public class QueryBookAction extends DispatchAction{
	
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
			
			String pin = (String) request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
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
		
	public ActionForward checkout(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String id = request.getParameter("id");
		String pin = (String) request.getAttribute("pin");
		try{
			jobject.setRoot(BookDao.getById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(id)));
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
