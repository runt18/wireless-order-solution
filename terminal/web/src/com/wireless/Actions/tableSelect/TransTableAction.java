package com.wireless.Actions.tableSelect;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.TransTblDao;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqPrintOrder2;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.sccon.ServerConnector;

public class TransTableAction extends Action implements PinGen {

	private long _pin = 0;

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PrintWriter out = null;

		String jsonResp = "{success:$(result), data:'$(value)'}";
		DBCon dbCon = new DBCon();
		String srcTblAlias = "", destTblAlias = "";
		Table srcTbl = null;
		Table destTbl = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. 
			 * e.g. pin=0x1 & newTableID=201 & oldTableID=101" 
			 * pin : the pin the this terminal 
			 * newTableID : the table id to transfer 
			 * oldTableID : the table id to be transferred
			 */
			String pin = request.getParameter("pin");
			
			_pin = Long.parseLong(pin);

			srcTblAlias = request.getParameter("oldTableAlias");
			destTblAlias = request.getParameter("newTableAlias");

			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, _pin, Terminal.MODEL_STAFF);
			
			srcTbl = new Table();
			srcTbl.aliasID = Integer.parseInt(srcTblAlias);
			
			destTbl = new Table();
			destTbl.aliasID = Integer.parseInt(destTblAlias);
				
			int orderID = TransTblDao.exec(term, srcTbl, destTbl);
			
			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", srcTbl.aliasID + "号台转至" + destTbl.aliasID + "号台成功");

			// print the transfer table receipt
			ReqPackage.setGen(this);
			ReqPrintOrder2.ReqParam printParam = new ReqPrintOrder2.ReqParam();
			printParam.printConf = Reserved.PRINT_TRANSFER_TABLE_2;
			printParam.orderID = orderID;
			printParam.srcTblID = srcTbl.aliasID;
			printParam.destTblID = destTbl.aliasID;
			ServerConnector.instance().ask(new ReqPrintOrder2(printParam));

		}catch(NumberFormatException e){
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "餐台号输入不正确，请重新输入");
			
		}catch (BusinessException e) {
			if(e.errCode == ErrorCode.TABLE_NOT_EXIST){
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", srcTblAlias + "或" + destTblAlias + "号台信息不存在");
				
			}else if(e.errCode == ErrorCode.TABLE_IDLE){
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", srcTbl.aliasID + "号台是空闲状态，可能已经结帐，请跟餐厅经理确认");
				
			}else if(e.errCode == ErrorCode.TABLE_BUSY){
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", destTbl.aliasID	+ "号台是就餐状态，请跟餐厅经理确认");
				
			}else{
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", srcTbl.aliasID + "号台转至" + destTbl.aliasID + "号台不成功");
			}

		} catch (SQLException e) {
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");

		} finally {

			dbCon.disconnect();
			// Just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}

	@Override
	public long getDeviceId() {
		return _pin;
	}

	@Override
	public short getDeviceType() {
		return Terminal.MODEL_STAFF;
	}
}
