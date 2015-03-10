package com.wireless.Actions.orderMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.orderMgr.OrderFoodDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.OrderFood;
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
			String tableID = request.getParameter("tableID");
			String queryType = request.getParameter("queryType");
			
			
			if (queryType.equals("Today")) {
				list = OrderFoodDao.getSingleDetail(staff, new ExtraCond(DateType.TODAY).setOrder(Integer.parseInt(orderID)), " ORDER BY OF.order_date ");
			}else if (queryType.equals("TodayByTbl")) {
				list = OrderFoodDao.getSingleDetailByTableId(staff, Integer.parseInt(tableID));
				if(orderID != null && !orderID.trim().isEmpty()){
					final float totalPrice = OrderDao.getById(staff, Integer.parseInt(orderID), DateType.TODAY).calcTotalPrice();
					jobject.setExtra(new Jsonable() {
						
						@Override
						public JsonMap toJsonMap(int flag) {
							JsonMap jm = new JsonMap();
							jm.putFloat("detailTotalPrice", totalPrice);
							return jm;
						}
						
						@Override
						public void fromJsonMap(JsonMap jsonMap, int flag) {
							
						}
					});
				}
			}else {
				list = OrderFoodDao.getSingleDetail(staff, new ExtraCond(DateType.HISTORY).setOrder(Integer.parseInt(orderID)), " ORDER BY OF.order_date ");
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
