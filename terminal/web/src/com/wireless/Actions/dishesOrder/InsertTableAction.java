package com.wireless.Actions.dishesOrder;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.InsertTable;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class InsertTableAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		PrintWriter out = null;
		String jsonResp = "{success:$(result), data:'$(value)'}";
		Table table = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			/**
			 * The parameters looks like below.
			 * 1st example: pin=0x1 & autoGenID=true & tableName="水仙厅"
			 * 2nd example: pin=0x01 & tableID=205
			 * pin : The pin the this terminal.
			 * autoGenID : Indicating whether auto to generate the table id. 
			 * tableID : The table id to be inserted. 
			 * 			 This parameter would be ignored if "autoGenID" is set to true.
			 * tableName : The alias name to this table.
			 * 			   This parameter is optional.
			 */			
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
						
			boolean autoGenID = Boolean.parseBoolean(request.getParameter("autoGenID"));
			table = new Table();
			if(!autoGenID){
				table.alias_id = Short.parseShort(request.getParameter("tableID"));
			}
			table.name = request.getParameter("tableName");
			
			int tableID = InsertTable.exec(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF, table, autoGenID);
			
			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", Integer.toString(tableID));
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED){
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
				
			}else if(e.errCode == ErrorCode.TABLE_EXIST){
				jsonResp = jsonResp.replace("$(value)", table.alias_id + "号餐台已经存在，请选择其他餐台号录入");
				
			}else{
				jsonResp = jsonResp.replace("$(value)", "添加新餐台不成功，请重新尝试");
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}finally{
			//Just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}
}
