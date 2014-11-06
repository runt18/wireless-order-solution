package com.wireless.Actions.tableSelect;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqTransTbl;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class TransTableAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try {
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			
			int srcTblAlias = Integer.parseInt(request.getParameter("oldTableAlias"));
			int destTblAlias = Integer.parseInt(request.getParameter("newTableAlias"));

			// print the transfer table receipt
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTransTbl(staff, new Table.TransferBuilder(new Table.AliasBuilder(srcTblAlias), new Table.AliasBuilder(destTblAlias))));
			if(resp.header.type == Type.ACK){
				jobject.initTip(true, "操作成功, 原 " + srcTblAlias + " 号台转至新 " + destTblAlias + " 号台成功.");
			}else{
				jobject.initTip(false, new Parcel(resp.body).readParcel(ErrorCode.CREATOR).getDesc());
			}
			
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
