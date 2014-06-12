package com.wireless.Actions.orderMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.orderMgr.OrderFoodDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;

public class QueryDetailAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		List<OrderFood> list = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String orderID = request.getParameter("orderID");
			String restaurantID = (String)request.getAttribute("restaurantID");
			String talias = request.getParameter("tableAlias");
			String queryType = request.getParameter("queryType");
			
			
			if (queryType.equals("Today")) {
				list = OrderFoodDao.getSingleDetail(staff, new ExtraCond(DateType.TODAY).setOrderId(Integer.parseInt(orderID)), " ORDER BY OF.order_date ");
			}else if (queryType.equals("TodayByTbl")) {
				Table t = new Table();
				t.setRestaurantId(Integer.valueOf(restaurantID));
				t.setTableAlias(Integer.valueOf(talias));
				list = OrderFoodDao.getSingleDetailByTable(staff, t);
			}else {
				list = OrderFoodDao.getSingleDetail(staff, new ExtraCond(DateType.HISTORY).setOrderId(Integer.parseInt(orderID)), " ORDER BY OF.order_date ");
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(list != null){
				list = DataPaging.getPagingData(list, isPaging, start, limit);
				jobject.setRoot(list);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
