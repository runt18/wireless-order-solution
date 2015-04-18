package com.wireless.Actions.dishesOrder;

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
import com.wireless.pack.req.ReqFeastOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class FeastOrderAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String deptFeasts = request.getParameter("deptFeasts");
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String[] feasts = deptFeasts.split("&");
			Order.FeastBuilder builder = new Order.FeastBuilder();
			for (int i = 0; i < feasts.length; i++) {
				String[] feast = feasts[i].split(",");
				builder.add(Integer.parseInt(feast[0]), Float.parseFloat(feast[1]));
			}
			final ProtocolPackage resp = ServerConnector.instance().ask(new ReqFeastOrder(staff, builder));
			
			if(resp.header.type == Type.ACK){
				jobject.initTip(true, ("录入成功."));
				
			}else if(resp.header.type == Type.NAK){
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				jobject.initTip(false, errCode.getCode(), "酒席分账失败，请重新确认.");
				
			}else{
				jobject.initTip(false, ("酒席分账失败，请重新确认."));
			}
			
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}

}
