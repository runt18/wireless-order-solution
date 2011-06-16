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
import com.wireless.db.Params;
import com.wireless.db.QueryTable;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class TransTableAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
								HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		PrintWriter out = null;

		String jsonResp = "{success:$(result), data:'$(value)'}";
		DBCon dbCon = new DBCon();
		String oldTableID = "", newTableID = "";
		Table oldTable = null; 
		Table newTable = null; 
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
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			
			oldTableID = request.getParameter("oldTableID");
			newTableID = request.getParameter("newTableID");
			
			dbCon.connect();
			oldTable = QueryTable.exec(dbCon, Integer.parseInt(pin, 16), 
						 			   Terminal.MODEL_STAFF, Short.parseShort(oldTableID));
			
			newTable = QueryTable.exec(dbCon, Integer.parseInt(pin, 16), 
						  			   Terminal.MODEL_STAFF, Short.parseShort(newTableID));
			
			/**
			 * Need to assure two conditions before table transfer
			 * 1 - the old table remains in busy
			 * 2 - the new table is idle now
			 */
			if(oldTable.status == Table.TABLE_IDLE){
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", oldTable.alias_id + "号台是空闲状态，可能已经结帐，请跟餐厅经理确认");
				
			}else if(newTable.status == Table.TABLE_BUSY){
				jsonResp = jsonResp.replace("$(result)", "false");
				jsonResp = jsonResp.replace("$(value)", newTable.alias_id + "号台是就餐状态，请跟餐厅经理确认");
				
			}else{
				String sql = "UPDATE " + Params.dbName + ".order SET table_id=" +
							 newTable.alias_id +
							 " WHERE restaurant_id=" + newTable.restaurant_id +
							 " AND table_id=" + oldTable.alias_id +
							 " AND total_price IS NULL";
				dbCon.stmt.execute(sql);
				
				jsonResp = jsonResp.replace("$(result)", "true");
				jsonResp = jsonResp.replace("$(value)", oldTable.alias_id + "号台转至" + newTable.alias_id + "号台成功");
			}
			
		}catch(BusinessException e){
			jsonResp = jsonResp.replace("$(result)", "false");
			if(oldTable == null){
				jsonResp = jsonResp.replace("$(value)", oldTableID + "号台信息不存在");
			}else if(newTable == null){
				jsonResp = jsonResp.replace("$(value)", newTableID + "号台信息不存在");
			}else{
				jsonResp = jsonResp.replace("$(value)", oldTable.alias_id + "号台转至" + newTable.alias_id + "号台不成功");				
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}finally{
			
			dbCon.disconnect();
			//Just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}
}
