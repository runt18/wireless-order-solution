package com.wireless.Actions.tableSelect;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.TransTblDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.WebParams;

public class TransTableAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
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
			final Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
			
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
			jobject.initTip(e);
		} catch (Exception e) {
			e.printStackTrace();
			jobject.initTip(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
