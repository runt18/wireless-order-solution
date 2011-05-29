package com.wireless.Actions.payment;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.QueryMember;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Member;
import com.wireless.protocol.Terminal;

public class QueryMemberAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		String memberID = "";
		
		DBCon dbCon = new DBCon();
		
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();		
			
			/**
			 * The parameter to pass looks like below.
			 * pin=0x01 & memberID=13214323132
			 * pin : the pin to this terminal
			 * memberID : the member id to query
			 */
			String pin = request.getParameter("pin");
			if(pin.startsWith("0x") || pin.startsWith("0X")){
				pin = pin.substring(2);
			}
			memberID = request.getParameter("memberID");
			
			Member member = QueryMember.exec(Integer.parseInt(pin, 16), Terminal.MODEL_STAFF, memberID);
			jsonResp = jsonResp.replace("$(result)", "true");
			/**
			 * The format to member looks like below.
			 * 会员姓名,会员电话
			 */
			String jsonMember = "\"$(name)\",\"$(phone)\"";
			jsonMember = jsonMember.replace("$(name)", member.name);
			jsonMember = jsonMember.replace("$(phone)", member.tele);
			jsonResp = jsonResp.replace("$(value)", jsonMember);
			
		}catch(BusinessException e){

			jsonResp = jsonResp.replace("$(result)", "false");
			
			if(e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED){
				e.printStackTrace();
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");	
				
			}else if(e.errCode == ErrorCode.MEMBER_NOT_EXIST){
				jsonResp = jsonResp.replace("$(value)", "会员(" + memberID + ")的信息不存在，请重新确认");	
				
			}else{
				e.printStackTrace();
				jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅的餐台信息，请重新确认");
			}
		
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}finally{
			
			dbCon.disconnect();
			
			//just for debug
			System.out.println(jsonResp);
			out.write(jsonResp);
		}
		
		return null;
	}
}
