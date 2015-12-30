package com.wireless.Actions.bookMgr;

import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.book.BookDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class OperateBookAction extends DispatchAction{
	
	/**
	 * 查找预订信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String bookId = request.getParameter("bookId");
		final String name = request.getParameter("name");
		final String phone = request.getParameter("phone");
		final String status = request.getParameter("status");
		final String bookDate = request.getParameter("bookDate");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String tableId = request.getParameter("tableId");
		final String detail = request.getParameter("detail");
		try{
			
			final String pin = (String) request.getAttribute("pin");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final BookDao.ExtraCond extraCond = new BookDao.ExtraCond();
			
			if(bookId != null && !bookId.isEmpty()){
				extraCond.setId(Integer.parseInt(bookId));
			}
			if(name != null && !name.isEmpty()){
				extraCond.setMember(name);
			}
			if(phone != null && !phone.isEmpty()){
				extraCond.setTele(phone);
			}
			if(status != null && !status.isEmpty() && !status.equals("-1")){
				extraCond.addStatus(Book.Status.valueOf(Integer.parseInt(status)));
			}
			if(tableId != null && !tableId.isEmpty()){
				extraCond.setTable(Integer.parseInt(tableId));
			}
			
			if(bookDate != null && !bookDate.isEmpty()){
				DutyRange range = new DutyRange(DateUtil.parseDate(bookDate), DateUtil.parseDate(bookDate + " 23:59:59"));
				extraCond.setBookRange(range);
			}else if(beginDate != null && !beginDate.isEmpty()){
				DutyRange range = new DutyRange(DateUtil.parseDate(beginDate), DateUtil.parseDate(endDate + " 23:59:59"));
				extraCond.setBookRange(range);				
			}else{
				Calendar c = Calendar.getInstance();
				DutyRange range = new DutyRange(DateUtil.parseDate(c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH)+1) + "-" + c.get(Calendar.DATE)), 0);
				extraCond.setBookRange(range);
			}
			
			final List<Book> result = BookDao.getByCond(staff, extraCond);
			if(detail != null && !detail.isEmpty() && Boolean.parseBoolean(detail)){
				for(int i = 0; i < result.size(); i++){
					result.set(i, BookDao.getById(staff, result.get(i).getId()));
				}
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
	
	/**
	 * 入座
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward seat(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String bookId = request.getParameter("bookId");
		String bookOrdersText = request.getParameter("bookOrders");
		JObject jobject = new JObject();
		
		String[] bookOrders = bookOrdersText.split("<li>");
		
		Book.SeatBuilder seatBuilder = new Book.SeatBuilder(Integer.parseInt(bookId));
		
		for (int i = 0; i < bookOrders.length; i++) {
			Order.InsertBuilder builder = JObject.parse(Order.InsertBuilder.JSON_CREATOR, 0, bookOrders[i]);
			seatBuilder.addOrder(builder);
		}
		
		try{
			String pin = (String) request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			BookDao.seat(staff, seatBuilder);		
			
			jobject.initTip(true, "入座成功");
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
	 * 手动添加预订
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String bookDate = request.getParameter("bookDate");
		final String member = request.getParameter("member");
		final String tele = request.getParameter("tele");
		final String amount = request.getParameter("amount");
		final String cate = request.getParameter("cate");
		final String reserved = request.getParameter("reserved");
		final String comment = request.getParameter("comment");
		final String staffId = request.getParameter("staff");
		final String money = request.getParameter("money");
		final String tables = request.getParameter("tables");
		final String orderFoods = request.getParameter("orderFoods");
		
		JObject jObject = new JObject();
		
		try{
			final String pin = (String) request.getAttribute("pin");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final Staff emloyee = StaffDao.getById(Integer.parseInt(staffId));
			Book.InsertBuilder4Manual insertBuilder = new Book.InsertBuilder4Manual().setBookDate(bookDate)
					 .setMember(member)
					 .setTele(tele)
					 .setAmount(Integer.parseInt(amount))
					 .setCategory(cate)
					 .setReserved(Integer.parseInt(reserved) * 60)
					 .setComment(comment)
					 .setStaff(emloyee)
					 .setMoney(Float.parseFloat(money));
			
			for (String tableString : tables.split("&")) {
				insertBuilder.addTable(TableDao.getById(staff, Integer.parseInt(tableString)));
			}
			
			if(orderFoods != null && !orderFoods.isEmpty()){
				for (String orderFood : orderFoods.split("&")) {
					OrderFood of = JObject.parse(OrderFood.JSON_CREATOR, 0, orderFood);
					insertBuilder.addOrderFood(of, emloyee);
				}
			}
			
			BookDao.insert(staff, insertBuilder);
			
			jObject.initTip(true, "添加成功");
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
	 * 删除book
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String bookId = request.getParameter("bookId");
		JObject jobject = new JObject();		
		try{
			String pin = (String) request.getAttribute("pin");
			BookDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(bookId));	
			
			jobject.initTip(true, "删除成功");
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
	 * 确认 & 修改book
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String bookId = request.getParameter("bookId");
		String bookDate = request.getParameter("bookDate");
		String member = request.getParameter("member");
		String tele = request.getParameter("tele");
		String amount = request.getParameter("amount");
		String cate = request.getParameter("cate");
		String reserved = request.getParameter("reserved");
		String comment = request.getParameter("comment");
		String staffId = request.getParameter("staff");
		String money = request.getParameter("money");
		String tables = request.getParameter("tables");
		String orderFoods = request.getParameter("orderFoods");
		
		JObject jobject = new JObject();
		try{
			String pin = (String) request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			Staff emloyee = StaffDao.getById(Integer.parseInt(staffId));
			
			Book.ConfirmBuilder insertBuilder = (Book.ConfirmBuilder)new Book.ConfirmBuilder(Integer.parseInt(bookId)).setBookDate(bookDate)
					 .setMember(member)
					 .setTele(tele)
					 .setAmount(Integer.parseInt(amount))
					 .setCategory(cate)
					 .setReserved(Integer.parseInt(reserved) * 60)
					 .setComment(comment)
					 .setStaff(emloyee)
					 .setMoney(Float.parseFloat(money));
			
			for (String tableString : tables.split("&")) {
				insertBuilder.addTable(TableDao.getById(staff, Integer.parseInt(tableString)));
			}
			
			if(!orderFoods.isEmpty()){
				for (String orderFood : orderFoods.split("&")) {
					OrderFood of = JObject.parse(OrderFood.JSON_CREATOR, 0, orderFood);
					insertBuilder.addOrderFood(of, emloyee);
				}
			}
			
			BookDao.confirm(staff, insertBuilder);
			
			jobject.initTip(true, "修改成功");
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
