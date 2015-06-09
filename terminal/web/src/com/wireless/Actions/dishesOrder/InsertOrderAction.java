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


public class InsertOrderAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		JObject jobject = new JObject();
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			
			String jsonText = request.getParameter("commitOrderData");
			
			int type = Integer.parseInt(request.getParameter("type"));

			String notPrint = request.getParameter("notPrint");
			final PrintOption printOption;
			if(notPrint != null && Boolean.valueOf(notPrint)){
				printOption = PrintOption.DO_NOT_PRINT;
			}else{
				printOption = PrintOption.DO_PRINT;
			}
			
//			ProtocolPackage resp = ServerConnector.instance().ask(
//										new ReqInsertOrder(staff,
//											orderToInsert,
//											(type == 1) ? Type.INSERT_ORDER : (type == 7) ? Type.UPDATE_ORDER : Type.INSERT_ORDER_FORCE,
//											notPrint != null && Boolean.valueOf(notPrint) ? PrintOption.DO_NOT_PRINT : PrintOption.DO_PRINT
//										));
			
			final ProtocolPackage resp;
			if(type == 1){
				Order.InsertBuilder builder = JObject.parse(Order.InsertBuilder.JSON_CREATOR, 0, jsonText);
				resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, builder, printOption));
				
			}else if(type == 7){
				Order.UpdateBuilder builder = JObject.parse(Order.UpdateBuilder.JSON_CREATOR, 0, jsonText);
				resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, builder, printOption));
				
			}else{
				Order.InsertBuilder builder = JObject.parse(Order.InsertBuilder.JSON_CREATOR, 0, jsonText);
				builder.setForce(true);
				resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, builder, printOption));
			}
			
			if(resp.header.type == Type.ACK){
				jobject.initTip(true, ("下单成功."));
				
			}else if(resp.header.type == Type.NAK){
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				jobject.initTip(false, errCode.getCode(), errCode.getDesc());
				
			}else{
				jobject.initTip(false, ("下单不成功，请重新确认."));
			}
			
		}catch(BusinessException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
		}catch(IOException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9997, "服务器请求不成功，请重新检查网络是否连通.");
			e.printStackTrace();
		}catch(NumberFormatException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9998, "菜品提交的数量不正确，请检查后重新提交.");
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作错误.");
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	

}
