package com.wireless.Actions.payment;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.FrontBusinessError;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.sccon.ServerConnector;

public class PrintOrderAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jobject = new JObject();
		try {
			final String pin = (String)request.getAttribute("pin");
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final String orderParam = request.getParameter("orderID");
			final String tableParam = request.getParameter("tableID");
			final String printerParam = request.getParameter("orientedPrinter");
			
			final int orderId;
			if(orderParam != null && !orderParam.isEmpty()){
				orderId = Integer.parseInt(orderParam);
			}else if(tableParam != null && !tableParam.isEmpty()){
				orderId = OrderDao.getByTableId(staff, Integer.parseInt(tableParam)).getId();
			}else{
				orderId = 0;
			}

			final long onDuty;
			if(request.getParameter("onDuty") != null){
				onDuty = DateUtil.parseDate(request.getParameter("onDuty"));
			}else{
				onDuty = 0;
			}
			
			final long offDuty;
			if(request.getParameter("offDuty") != null){
				offDuty = DateUtil.parseDate(request.getParameter("offDuty"));
			}else{
				offDuty = 0;
			}
			
			String regionId = request.getParameter("regionId");
			boolean paymentRegion;
			if(regionId != null && !regionId.isEmpty()){
				paymentRegion = true;
			}else{
				paymentRegion = false;
			}
			
			final ReqPrintContent reqPrintContent;
			final int printType = Integer.valueOf(request.getParameter("printType"));
			
			switch(printType){
				case 1:
					reqPrintContent = ReqPrintContent.buildSummary(staff, orderId);
					break;
				case 2:
					reqPrintContent = ReqPrintContent.buildDetail(staff, orderId);
					break;
				case 3:
					reqPrintContent = ReqPrintContent.buildReceipt(staff, orderId);
					break;
				case 4:
					if(paymentRegion){
						reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), Region.RegionId.valueOf(Integer.parseInt(regionId)), PType.PRINT_SHIFT_RECEIPT);
					}else{
						reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), PType.PRINT_SHIFT_RECEIPT);
					}
					break;
				case 5:
					if(paymentRegion){
						reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), Region.RegionId.valueOf(Integer.parseInt(regionId)), PType.PRINT_TEMP_SHIFT_RECEIPT);
					}else{
						reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), PType.PRINT_TEMP_SHIFT_RECEIPT);
					}
					break;
				case 6:
					if(paymentRegion){
						reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), Region.RegionId.valueOf(Integer.parseInt(regionId)), PType.PRINT_DAILY_SETTLE_RECEIPT);
					}else{
						reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), PType.PRINT_DAILY_SETTLE_RECEIPT);
					}
					break;
				case 7:
					reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), PType.PRINT_HISTORY_SHIFT_RECEIPT);
					break;
				case 8:
					reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT);
					break;
				case 12:
					if(paymentRegion){
						reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), Region.RegionId.valueOf(Integer.parseInt(regionId)), PType.PRINT_PAYMENT_RECEIPT);
					}else{
						reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), PType.PRINT_PAYMENT_RECEIPT);
					}
					break;
				case 13:
					reqPrintContent = ReqPrintContent.buildShiftReceipt(staff, new DutyRange(onDuty, offDuty), PType.PRINT_HISTORY_PAYMENT_RECEIPT);
					break;
				case 14:
					reqPrintContent = ReqPrintContent.buildSummaryPatch(staff, orderId);
					break;
				case 15:
					reqPrintContent = ReqPrintContent.buildDetailPatch(staff, orderId);
					break;
				case 16:
					
					reqPrintContent = ReqPrintContent.build2ndDisplay(staff, Float.parseFloat(request.getParameter("display")));
					break;
				default:
					reqPrintContent = null;
					break;
			}
			
			if(reqPrintContent != null){

				if(printerParam != null && !printerParam.isEmpty()){
					for(String printerId : printerParam.split(",")){
						reqPrintContent.addPrinter(Integer.parseInt(printerId));
					}
				}
				
				ProtocolPackage resp = ServerConnector.instance().ask(reqPrintContent.build());
				
				if(resp.header.type == Type.ACK){
					jobject.setSuccess(true);
					switch(printType){
						case 1:
							jobject.initTip("操作成功, " + orderId + " 号账单总单打印成功.");
							break;
						case 2:
							jobject.initTip("操作成功, " + orderId + " 号账单明细单打印成功.");
							break;
						case 3:
							jobject.initTip("操作成功, " + orderId + " 号结账单打印成功.");
							break;
						case 4:
						case 5:
						case 7:
							jobject.initTip("操作成功, 交班对账信息打印成功.");
							break;
						case 6:
						case 8:
							jobject.initTip("操作成功, 日结信息打印成功.");
							break;
						case 12:
							jobject.initTip("操作成功, 交款对账信息打印成功.");
							break;
						case 13:
							jobject.initTip("操作成功, 交款对账信息打印成功.");
							break;
						case 15:
							jobject.initTip("操作成功, 补打成功.");
							break;
						default:
							jobject.initTip("操作成功, 打印成功.");
							break;
					}
				}else if(resp.header.type == Type.NAK){
					ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
					if(errCode.equals(FrontBusinessError.ORDER_NOT_EXIST)){
						jobject.initTip(false, JObject.TIP_TITLE_ERROE, 9999, "操作失败, " + orderId + "账单不存在, 请重新确认.");
					}else{
						jobject.initTip(false, JObject.TIP_TITLE_ERROE, 9999, "操作失败, " + orderId + "号账单打印不成功, 请重新检查网络是否连通.");
					}
				}else{
					jobject.initTip(false, JObject.TIP_TITLE_ERROE, 9999, "操作失败, 服务器请求不成功, 请重新检查网络是否连通.");
				}
			}
		}catch(NumberFormatException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9999, "操作失败, 获取账单编号或餐台编号或打印类型信息失败.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(IOException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9999, "操作失败, 服务器请求不成功, 请重新检查网络是否连通.");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9999, "操作失败, 未知错误, 请联系客服人员.");
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
