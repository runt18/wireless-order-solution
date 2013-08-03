package com.wireless.Actions.tableSelect;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.CancelOrder;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;

public class CancelOrderAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		PrintWriter out = null;
		int tableAlias = 0;
		String jsonResp = "{success:$(result), data:'$(value)'}";
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			String pin = request.getParameter("pin");
			
			tableAlias = Integer.parseInt(request.getParameter("tableAlias"));
			
			CancelOrder.exec(StaffDao.verify(Integer.parseInt(pin)), tableAlias);
			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", tableAlias + "号餐台删单成功");
			
		}catch(NumberFormatException e){
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "餐台号输入不正确，请重新输入");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			if(e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED){
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
				
			}else if(e.getErrCode() == ProtocolError.TABLE_NOT_EXIST){
				jsonResp = jsonResp.replace("$(value)", tableAlias + "号餐台信息不存在，请重新确认");
				
			}else if(e.getErrCode() == ProtocolError.TABLE_IDLE){
				jsonResp = jsonResp.replace("$(value)", tableAlias + "号餐台是空闲状态，可能已结帐，请与餐厅管理人员确认");
				
			}else{
				jsonResp = jsonResp.replace("$(value)", "删除" + tableAlias + "号餐台信息不成功，请确认网络连接是否正常");
			}
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			
		}finally{
			//Just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}
		return null;
	}
}
