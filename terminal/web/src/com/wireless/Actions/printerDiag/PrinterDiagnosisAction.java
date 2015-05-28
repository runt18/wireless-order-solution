package com.wireless.Actions.printerDiag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.RequestPackage;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.diagnose.DiagnoseResult;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class PrinterDiagnosisAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
//		final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getParameter("pin")));

		ProtocolPackage resp = ServerConnector.instance().ask(new RequestPackage(staff, Mode.DIAGNOSIS, Type.PRINTER));
		
		JObject jObj = new JObject();
		if(resp.header.type == Type.ACK){
			jObj.setRoot(new Parcel(resp.body).readParcel(DiagnoseResult.CREATOR));
		}else{
			ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
			jObj.initTip(false, errCode.getCode(), errCode.getDesc());
		}
		response.getWriter().print(jObj.toString());
		return null;
	}
}
