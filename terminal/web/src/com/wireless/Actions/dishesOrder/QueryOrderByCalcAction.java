package com.wireless.Actions.dishesOrder;

import java.io.IOException;
import java.sql.SQLException;

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
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class QueryOrderByCalcAction extends Action{
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		
		try {
			
			final String pin = (String)request.getAttribute("pin");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final String tid = request.getParameter("tableID");
			final String oid = request.getParameter("orderID");
			final String eraseQuota = request.getParameter("eraseQuota");
			final String customNum = request.getParameter("customNum");
			final String orientedDisplay = request.getParameter("orientedDisplay");
			
			final Order.PayBuilder payBuilder;

			if (oid != null && !oid.trim().isEmpty()){
				payBuilder = Order.PayBuilder.build4Normal(Integer.valueOf(oid));
				
			}else if(tid != null && !tid.trim().isEmpty()){
				Table table = TableDao.getById(staff, Integer.parseInt(tid));
				if(table.isBusy()){
					payBuilder = Order.PayBuilder.build4Normal(table.getOrderId()).setCustomNum(table.getCustomNum());
				}else{
					throw new BusinessException(FrontBusinessError.ORDER_NOT_EXIST);
				}
				
			}else{
				throw new BusinessException("缺少餐台号或账单号");
			}
			

			if(eraseQuota != null && !eraseQuota.isEmpty()){
				payBuilder.setErasePrice(Integer.valueOf(eraseQuota));
			}
			
			if(customNum != null && !customNum.isEmpty()){
				payBuilder.setCustomNum(Short.valueOf(customNum));
			}
			
			final Order order = PayOrder.calc(staff, payBuilder);
			
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
			
			//显示客显
			if(orientedDisplay != null && !orientedDisplay.isEmpty()){
				ReqPrintContent req2ndDisplay = ReqPrintContent.build2ndDisplay(staff, order.getActualPrice());
				for(String printerId : orientedDisplay.split(",")){
					req2ndDisplay.addPrinter(Integer.parseInt(printerId));
				}
				try{
					ServerConnector.instance().ask(req2ndDisplay.build());
				}catch(IOException | BusinessException ignored){
					ignored.printStackTrace();
				}
			}
			
		} catch (BusinessException | SQLException e) {
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
