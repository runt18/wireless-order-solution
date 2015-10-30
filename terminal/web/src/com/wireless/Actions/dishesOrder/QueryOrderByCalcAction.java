package com.wireless.Actions.dishesOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.orderMgr.PayOrder;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public class QueryOrderByCalcAction extends Action{
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		
		try {
			
			String pin = (String)request.getAttribute("pin");
			String tid = request.getParameter("tableID");
			String oid = request.getParameter("orderID");
			String calc = request.getParameter("calc");
			final Order.PayBuilder payBuilder;
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));

			if(tid != null && !tid.trim().isEmpty()){
				Table table = TableDao.getById(staff, Integer.parseInt(tid));
				if(table.isBusy()){
					payBuilder = Order.PayBuilder.build4Normal(table.getOrderId()).setCustomNum(table.getCustomNum());
				}else{
					throw new BusinessException(FrontBusinessError.ORDER_NOT_EXIST);
				}
				
			} else if (oid != null && !oid.trim().isEmpty()){
				payBuilder = Order.PayBuilder.build4Normal(Integer.valueOf(oid));
				
			}else{
				throw new BusinessException("缺少餐台号或账单号");
			}
			
			final Order order;
			if(calc != null && Boolean.valueOf(calc)){
				
				String eraseQuota = request.getParameter("eraseQuota");
				String customNum = request.getParameter("customNum");
				if(eraseQuota != null){
					payBuilder.setErasePrice(Integer.valueOf(eraseQuota));
				}
				if(customNum != null){
					payBuilder.setCustomNum(Short.valueOf(customNum));
				}
				
				order = PayOrder.calc(staff, payBuilder);
				
			}else{
				order = PayOrder.calc(staff, payBuilder);
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
