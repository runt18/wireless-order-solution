package com.wireless.Actions.dailySettle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.wireless.pack.req.ReqDailySettle;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class DailySettleExecAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		final String orientedPrinters = request.getParameter("orientedPrinter");

		final JObject jObject = new JObject();		
		
		try {

			final String pin = (String)request.getAttribute("pin");

			final Staff staff = StaffDao.verify(Integer.parseInt(pin));

			final List<Printer> printers = new ArrayList<>();
			
			if(orientedPrinters != null && !orientedPrinters.isEmpty()){
				for(String printerId : orientedPrinters.split(",")){
					printers.add(new Printer(Integer.parseInt(printerId)));
				}
			}
			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqDailySettle(staff, printers));
			
			if(resp.header.type == Type.ACK){
				jObject.initTip(true, staff.getName() + "日结成功");
			}else{
				jObject.initTip(false, new Parcel(resp.body).readParcel(ErrorCode.CREATOR).getDesc());
			}
			

		} catch (BusinessException e) {
			e.printStackTrace();
			jObject.initTip(false, e.getDesc());
			
		} catch (IOException e) {
			e.printStackTrace();
			jObject.initTip(false, "数据库请求发生错误，请确认网络是否连接正常");

		} finally {
			response.getWriter().write(jObject.toString());
		}

		return null;
	}
}
