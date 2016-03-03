package com.wireless.Actions.dishesOrder;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;

public class QueryOrderAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setContentType("text/json;charset=utf-8");
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String queryType = request.getParameter("queryType");
		final String tableId = request.getParameter("tableID");
		final String orderId = request.getParameter("orderID");
		
		final JObject jObject = new JObject();
		try {
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final Order order;
			if(queryType != null && queryType.trim().equalsIgnoreCase("history")){
				if (orderId != null && !orderId.trim().isEmpty()){
					order = OrderDao.getById(staff, Integer.parseInt(orderId), DateType.HISTORY);
				}else{
					order = null;
				}
			}else{
				if(tableId != null && !tableId.trim().isEmpty()){
					order = OrderDao.getByTableId(staff, Integer.parseInt(tableId));
				} else if (orderId != null && !orderId.trim().isEmpty()){
					order = OrderDao.getById(staff, Integer.parseInt(orderId), DateType.TODAY);
				}else{
					order = null;
				}
			}			
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("order", order, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			
		} catch (BusinessException | SQLException e) {
			e.printStackTrace();
			jObject.initTip(e);
			
		} catch (Exception e) {
			e.printStackTrace();
			jObject.initTip4Exception(e);
			
		} finally {
			response.getWriter().print(jObject.toString());
		}
		return null;
	}

}
