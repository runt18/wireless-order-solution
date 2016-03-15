package com.wireless.Actions.dishesOrder;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.marker.weixin.api.BaseAPI;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.wrapper.IntParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;


public class InsertOrderAction extends Action{
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String jsonText = request.getParameter("commitOrderData");
		
		final int type = Integer.parseInt(request.getParameter("type"));

		final String notPrint = request.getParameter("notPrint");
		
		final String wxCode = request.getParameter("wxCode");
		
		final String orientedPrinter = request.getParameter("orientedPrinter");		
		
		final String wxWaiter = request.getParameter("wxWaiter");
		
		final JObject jObject = new JObject();
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			

			
			final PrintOption printOption;
			if(notPrint != null && !notPrint.isEmpty()){
				printOption = Boolean.valueOf(notPrint) ? PrintOption.DO_NOT_PRINT : PrintOption.DO_PRINT;
			}else{
				printOption = PrintOption.DO_PRINT;
			}
			
			final ProtocolPackage resp;
			if(type == 1){
				Order.InsertBuilder builder = JObject.parse(Order.InsertBuilder.JSON_CREATOR, 0, jsonText);
				//加载特定打印机
				if(orientedPrinter != null && !orientedPrinter.isEmpty()){
					for(String printerId : orientedPrinter.split(",")){
						builder.addPrinter(Integer.parseInt(printerId));
					}
				}
				//加载微信账单
				if(wxCode != null && !wxCode.isEmpty()){
					for(String code : wxCode.split(",")){
						builder.addWxOrder(WxOrderDao.getByCode(staff, Integer.parseInt(code)));
					}
				}
				resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, builder, printOption));
				
				
			}else if(type == 7){
				Order.UpdateBuilder builder = JObject.parse(Order.UpdateBuilder.JSON_CREATOR, 0, jsonText);
				//加载特定打印机
				if(orientedPrinter != null && !orientedPrinter.isEmpty()){
					for(String printerId : orientedPrinter.split(",")){
						builder.addPrinter(Integer.parseInt(printerId));
					}
				}
				//加载微信账单
				if(wxCode != null && !wxCode.isEmpty()){
					for(String code : wxCode.split(",")){
						builder.addWxOrder(WxOrderDao.getByCode(staff, Integer.parseInt(code)));
					}
				}
				resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, builder, printOption));
				
			}else{
				Order.InsertBuilder builder = JObject.parse(Order.InsertBuilder.JSON_CREATOR, 0, jsonText);
				builder.setForce(true);
				//加载特定打印机
				if(orientedPrinter != null && !orientedPrinter.isEmpty()){
					for(String printerId : orientedPrinter.split(",")){
						builder.addPrinter(Integer.parseInt(printerId));
					}
				}
				//加载微信账单
				if(wxCode != null && !wxCode.isEmpty()){
					for(String code : wxCode.split(",")){
						builder.addWxOrder(WxOrderDao.getByCode(staff, Integer.parseInt(code)));
					}
				}
				resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, builder, printOption));
				
			}
			
			if(resp.header.type == Type.ACK){
				jObject.initTip(true, ("下单成功."));
				//FIXME 新下单时打印【微信店小二】
				//if(type == 1 && wxWaiter != null && !wxWaiter.isEmpty() && Boolean.parseBoolean(wxWaiter)){
					final String serverName;
					if(request.getServerName().equals("e-tones.net")){
						serverName = "wx.e-tones.net";
					}else{
						serverName = request.getServerName(); 
					}
					final int serverPoint = request.getServerPort();
					final int orderId = new Parcel(resp.body).readParcel(IntParcel.CREATOR).intValue();
					new Thread(){
						@Override
						public void run(){
							try {
								BaseAPI.doGet("http://" + serverName + ":" + serverPoint + "/wx-term/WxOperateWaiter.do?dataSource=print&restaurantId=" + staff.getRestaurantId() + "&orderId=" + orderId);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}.start();
				//}
				
			}else if(resp.header.type == Type.NAK){
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				jObject.initTip(false, errCode.getCode(), errCode.getDesc());
				
			}else{
				jObject.initTip(false, ("下单不成功，请重新确认."));
			}
			
		}catch(BusinessException e){
			jObject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
		}catch(IOException e){
			jObject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9997, "服务器请求不成功，请重新检查网络是否连通.");
			e.printStackTrace();
		}catch(NumberFormatException e){
			jObject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9998, "菜品提交的数量不正确，请检查后重新提交.");
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作错误.");
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	

}
