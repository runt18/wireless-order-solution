package com.wireless.Actions.payment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

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
//		try{
//			StringBuilder sb = new StringBuilder();
//			String line = null;
//			BufferedReader reader = request.getReader();
//			while ((line = reader.readLine()) != null){
//				sb.append(line);
//			}
//			WebHook hookResponse = JObject.parse(WebHook.JSON_CREATOR, 0, sb.toString());
//			//System.out.println(hookResponse);
//			if(hookResponse.isTradeSuccess()){
//				JSONObject jObj4optional = JSONObject.parseObject(hookResponse.getOptional());
//				Order.PayBuilder payBuilder = JObject.parse(Order.PayBuilder.JSON_CREATOR, 0, jObj4optional.getJSONObject("payBuilder").toJSONString());
//				Staff staff = StaffDao.verify(jObj4optional.getIntValue("staffId"));
//				
//				if(OrderDao.getStatusById(staff, payBuilder.getOrderId()) == Order.Status.UNPAID){
//					ProtocolPackage resp = ServerConnector.instance().ask(new ReqPayOrder(staff, payBuilder));
//					
//					if(resp.header.type == Type.ACK){
//						System.out.println("结账成功");
//					}else{
//						System.out.println("结账失败" + "," + new Parcel(resp.body).readParcel(ErrorCode.CREATOR).getDesc());
//					}
//				}
//			}
//		}finally{
//			response.getWriter().print("success");
//		}
		response.getWriter().print("success");
		return null;
	}

}
