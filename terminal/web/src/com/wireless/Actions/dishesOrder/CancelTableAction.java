package com.wireless.Actions.dishesOrder;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.CancelTable;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

public class CancelTableAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		PrintWriter out = null;
		String jsonResp = "{success:$(result), data:'$(value)'}";
		short tableID = 0;
		
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			/**
			 * The parameters looks like below.
			 * example: pin=0x1 & tableID=201
			 * pin : The pin the this terminal.
			 * tableID : The table id to be deleted. 
			 */			
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			tableID = Short.parseShort(request.getParameter("tableID"));
			
			CancelTable.exec(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF, tableID);
			
			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", tableID + "号餐台删除成功");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED){
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
				
			}else if(e.errCode == ErrorCode.TABLE_NOT_EXIST){
				jsonResp = jsonResp.replace("$(value)", tableID + "号餐台不存在，请选择其他餐台号");
				
			}else{
				jsonResp = jsonResp.replace("$(value)", "添加新餐台不成功，请重新尝试");
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}finally{
			//Just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}
}
