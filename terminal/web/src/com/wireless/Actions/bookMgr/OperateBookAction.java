package com.wireless.Actions.bookMgr;

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
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.staffMgr.Staff;

public class OperateBookAction extends DispatchAction{
	
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
		String bookOrderFoodsText = request.getParameter("bookOrderFoods");
		JObject jobject = new JObject();
		
		String[] bookOrderFoods = bookOrderFoodsText.split("<li>");
		
		Book.SeatBuilder seatBuilder = new Book.SeatBuilder(Integer.parseInt(bookId));
		
		for (int i = 0; i < bookOrderFoods.length; i++) {
			Order.InsertBuilder builder = JObject.parse(Order.InsertBuilder.JSON_CREATOR, 0, bookOrderFoods[i]);
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
	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
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
			
			if(!orderFoods.isEmpty()){
				for (String orderFood : orderFoods.split("&")) {
					OrderFood of = JObject.parse(OrderFood.JSON_CREATOR, 0, orderFood);
					insertBuilder.addOrderFood(of, emloyee);
				}
			}
			
			BookDao.insert(staff, insertBuilder);
			
			jobject.initTip(true, "添加成功");
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
