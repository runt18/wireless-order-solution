package com.wireless.Actions.orderMgr;

import java.sql.SQLException;
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
import com.wireless.pojo.util.DateType;
import com.wireless.util.DataPaging;

public class QueryDetailAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String orderID = request.getParameter("orderID");
		final String tableID = request.getParameter("tableID");
		final String queryType = request.getParameter("queryType");
		final String isPaging = request.getParameter("isPaging");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		
		final JObject jObject = new JObject();

		try{

			Staff staff = StaffDao.verify(Integer.parseInt(pin));

			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			List<OrderFood> list = null;

			if (queryType.equalsIgnoreCase("today")) {
				list = OrderFoodDao.getSingleDetail(staff, new ExtraCond(DateType.TODAY).setOrder(Integer.parseInt(orderID)), " ORDER BY OF.order_date ");
			}else if (queryType.equalsIgnoreCase("TodayByTbl")) {
				list = OrderFoodDao.getSingleDetailByTableId(staff, Integer.parseInt(tableID));
				if(orderID != null && !orderID.trim().isEmpty()){
					final float totalPrice = OrderDao.getById(staff, Integer.parseInt(orderID), DateType.TODAY).calcTotalPrice();
					jObject.setExtra(new Jsonable() {
						
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
			
			if(list != null){
				if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
					list = DataPaging.getPagingData(list, Boolean.parseBoolean(isPaging), start, limit);
				}
				jObject.setRoot(list);
			}

		}catch(BusinessException | SQLException e){
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
	
}
