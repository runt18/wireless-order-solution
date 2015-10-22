package com.wireless.Actions.payment;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.alibaba.fastjson.JSONObject;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class BeeCloudHookAction extends Action{
	
	static enum ChannelType{
		WX("WX", "微信支付"),
		ALI("ALI", "支付宝支付"),
		UN("UN", "银联支付");
		private final String val;
		private final String desc;
		
		ChannelType(String val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static ChannelType valueOf(String val, int flag){
			for(ChannelType type : values()){
				if(type.val.equals(val)){
					return type;
				}
			}
			throw new IllegalArgumentException("The channel type(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		String line = null;
		BufferedReader reader = request.getReader();
		while ((line = reader.readLine()) != null){
			sb.append(line);
		}
		
		JSONObject jObj = JSONObject.parseObject(sb.toString());
		
		ChannelType channelType = ChannelType.valueOf(jObj.getString("channelType"));
		if(jObj.getBooleanValue("tradeSuccess")){
			
			JSONObject jObj4optional = jObj.getJSONObject("optional");
			Order.PayBuilder payBuilder = JObject.parse(Order.PayBuilder.JSON_CREATOR, 0, jObj4optional.getJSONObject("payBuilder").toJSONString());
			Staff staff = StaffDao.verify(jObj4optional.getIntValue("staffId"));
			
			if(OrderDao.getStatusById(staff, payBuilder.getOrderId()) == Order.Status.UNPAID){
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqPayOrder(staff, payBuilder));
				
				if(resp.header.type == Type.ACK){
					System.out.println(channelType.desc + "结账成功");
				}else{
					System.out.println(channelType.desc + "结账失败" + "," + new Parcel(resp.body).readParcel(ErrorCode.CREATOR).getDesc());
				}
			}			
		}else{
			System.out.println(channelType.desc + "失败");
		}
		response.getWriter().print("success");
		return null;
	}

}
