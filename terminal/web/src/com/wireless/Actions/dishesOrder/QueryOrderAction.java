package com.wireless.Actions.dishesOrder;

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
		JObject jobject = new JObject();
		try {
			
			/**
			 * The parameters looks like below. 1st example, query order by
			 * table id pin=0x1 & tableID=201 2nd example, query order by order
			 * id pin=0x01 & orderID=40 pin : the pin the this terminal tableID
			 * : the order with this table ID to query
			 */
			String pin = (String)request.getAttribute("pin");
			String queryType = request.getParameter("queryType");
			String tid = request.getParameter("tableID");
			//String restaurantID = (String)request.getAttribute("restaurantID");
			String oid = request.getParameter("orderID");
			
			final Order order;
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(queryType != null && queryType.trim().equals("History")){
				if (oid != null && !oid.trim().isEmpty()){
					order = OrderDao.getById(staff, Integer.valueOf(oid), DateType.HISTORY);
				}else{
					order = null;
				}
			}else{
				if(tid != null && !tid.trim().isEmpty()){
					order = OrderDao.getByTableId(staff, Integer.parseInt(tid));
				} else if (oid != null && !oid.trim().isEmpty()){
					order = OrderDao.getById(staff, Integer.valueOf(oid), DateType.TODAY);
				}else{
					order = null;
				}
			}			
			
			jobject.setExtra(new Jsonable(){
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
			
		} catch (BusinessException e) {
			e.printStackTrace();
			jobject.initTip(e);
			
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
			
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
