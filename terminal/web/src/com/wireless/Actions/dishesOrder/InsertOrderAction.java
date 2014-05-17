package com.wireless.Actions.dishesOrder;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.WebParams;


public class InsertOrderAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		JObject jobject = new JObject();
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			
//			Order orderToInsert = new Order();
//			int tableAlias = request.getParameter("tableID") != null ? Integer.parseInt(request.getParameter("tableID")) : 0;
//			orderToInsert.getDestTbl().setTableAlias(tableAlias);
//		
//			orderToInsert.setCategory(Short.parseShort(request.getParameter("category")));
//			orderToInsert.setCustomNum(Integer.parseInt(request.getParameter("customNum")));
//			if(request.getParameter("orderDate") != null && !request.getParameter("orderDate").trim().isEmpty()){
//				orderToInsert.setOrderDate(Long.parseLong(request.getParameter("orderDate")));				
//			}
//			int type = Integer.parseInt(request.getParameter("type"));
//			String orderType = null;
//			
//			if(type == 1){
//				orderType = "下单";
//			}else{
//				orderType = "改单";
//				String orderID = request.getParameter("orderID");
//				if(orderID != null && !orderID.trim().isEmpty())
//					orderToInsert.setId(Integer.valueOf(orderID));
//			}
//			orderToInsert.setOrderFoods(Util.toFoodArray(request.getParameter("foods")));

			
			//TODO Get the json text.
			int type = Integer.parseInt(request.getParameter("type"));
			
			String jsonText = request.getParameter("commitOrderData");
			Order orderToInsert = JObject.parse(Order.JSON_CREATOR, type, jsonText);
			
			
			
			String notPrint = request.getParameter("notPrint");
			
			ProtocolPackage resp = ServerConnector.instance().ask(
										new ReqInsertOrder(staff,
											orderToInsert,
											(type == 1) ? Type.INSERT_ORDER : (type == 7) ? Type.UPDATE_ORDER : Type.INSERT_ORDER_FORCE,
											notPrint != null && Boolean.valueOf(notPrint) ? PrintOption.DO_NOT_PRINT : PrintOption.DO_PRINT
										));
			
			if(resp.header.type == Type.ACK){
				jobject.initTip(true, (orderToInsert.getDestTbl().getAliasId() + "号餐台下单成功."));
				
			}else if(resp.header.type == Type.NAK){
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				jobject.initTip(false, errCode.getCode(), errCode.getDesc());
				
			}else{
				jobject.initTip(false, (orderToInsert.getDestTbl().getAliasId() + "号餐台下单不成功，请重新确认."));
			}
			
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
		}catch(IOException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9997, "服务器请求不成功，请重新检查网络是否连通.");
			e.printStackTrace();
		}catch(NumberFormatException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9998, "菜品提交的数量不正确，请检查后重新提交.");
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作错误.");
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	

}
