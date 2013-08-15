package com.wireless.Actions.tableSelect;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.TransTblDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class TransTableAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		
		String srcTblAlias = "", destTblAlias = "";
		Table srcTbl = null, destTbl = null;
		
		try {
			/**
			 * The parameters looks like below. 
			 * e.g. pin=0x1 & newTableID=201 & oldTableID=101" 
			 * pin : the pin the this terminal 
			 * newTableID : the table id to transfer 
			 * oldTableID : the table id to be transferred
			 */
			final Staff staff = StaffDao.verify(Integer.parseInt((String) request.getSession().getAttribute("pin")));
			
			srcTblAlias = request.getParameter("oldTableAlias");
			destTblAlias = request.getParameter("newTableAlias");

			srcTbl = new Table();
			srcTbl.setTableAlias(Integer.parseInt(srcTblAlias));
			
			destTbl = new Table();
			destTbl.setTableAlias(Integer.parseInt(destTblAlias));
				
			int orderId = TransTblDao.exec(staff, srcTbl, destTbl);
			
			jobject.initTip(true, "操作成功, 原 " + srcTbl.getAliasId() + " 号台转至新 " + destTbl.getAliasId() + " 号台成功.");
			
			// print the transfer table receipt
			ServerConnector.instance().ask(ReqPrintContent.buildReqPrintTransTbl(staff,	orderId, srcTbl, destTbl));			
			
		}catch(IOException e){
			jobject.initTip(jobject.getMsg() + "但打印操作请求异常, 请联系管理员.");
			System.out.println(WebParams.TIP_TITLE_WARNING + ":" + jobject.getMsg());
		}catch(NumberFormatException e){
			jobject.initTip(false, "操作失败, 餐台号输入不正确，请重新输入");
			System.out.println(WebParams.TIP_TITLE_ERROE + ":" + jobject.getMsg());
		}catch (BusinessException e) {
			if(e.getErrCode() == ProtocolError.TABLE_NOT_EXIST){
				jobject.initTip(false, "操作失败, " + srcTblAlias + "或" + destTblAlias + "号台信息不存在, 请重新确认.");
			}else if(e.getErrCode() == ProtocolError.TABLE_IDLE){
				jobject.initTip(false, "操作失败, " + "原" + srcTbl.getAliasId() + "号台是空闲状态，可能已经结帐，请重新确认.");
			}else if(e.getErrCode() == ProtocolError.TABLE_BUSY){
				jobject.initTip(false, "操作失败, " + "新" + destTbl.getAliasId()	+ "号台是就餐状态，请重新确认.");
			}else{
				jobject.initTip(false, "操作失败, 原 " + srcTbl.getAliasId() + "号台转至新 " + destTbl.getAliasId() + "号台失败, 未知错误.");
			}
			System.out.println(WebParams.TIP_TITLE_ERROE + ":" + jobject.getMsg());
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally {
			response.getWriter().print(JSONObject.fromObject(jobject).toString());
		}
		return null;
	}

}
