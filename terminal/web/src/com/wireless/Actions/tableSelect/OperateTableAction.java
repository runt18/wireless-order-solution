package com.wireless.Actions.tableSelect;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.json.JObject;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.req.ReqTransTbl;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class OperateTableAction extends DispatchAction{

	public ActionForward transTable(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
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
	
	public ActionForward apartTable(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String tableID = request.getParameter("tableID");
		String suffix = request.getParameter("suffix");
		try {
			List<Table> list = new ArrayList<>();
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, Order.InsertBuilder.newInstance4Join(new Table.Builder(Integer.parseInt(tableID)), Table.InsertBuilder4Join.Suffix.valueOf(suffix, 0)), PrintOption.DO_NOT_PRINT));
			if(resp.header.type == Type.ACK){
				Table joinedTbl = new Parcel(resp.body).readParcel(Table.CREATOR);
				list.add(TableDao.getById(staff, joinedTbl.getId()));
				jobject.setRoot(list);
				jobject.initTip(true, ("下单成功."));
			}else if(resp.header.type == Type.NAK){
				throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
				
			}
		} catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);

		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}	

}
