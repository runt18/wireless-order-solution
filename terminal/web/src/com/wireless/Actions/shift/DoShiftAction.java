package com.wireless.Actions.shift;

import java.io.IOException;
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
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;

public class DoShiftAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
	
		DBCon dbCon = new DBCon();
		
		String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();
			
			/**
			 * The parameters looks like below.
			 * e.g. pin=0x01 & onDuty='2011-08-04 08:24:32' & offDuty='2011-08-04 12:43:21'
			 * 
			 * pin : the pin the this terminal
			 * 
			 * onDuty : the date time to be on duty
			 * 
			 * OffDuty : the date time to be off duty
			 */
			String pin = (String)request.getAttribute("pin");
						
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			dbCon.connect();
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String sql = "INSERT INTO " + Params.dbName + ".shift (restaurant_id, name, on_duty, off_duty) VALUES(" +
						 staff.getRestaurantId() + "," +
						 "'" + staff.getName() + "'," +
						 "DATE_FORMAT('" + onDuty + "', '%Y%m%d%H%i%s')" + "," +
						 "DATE_FORMAT('" + offDuty + "', '%Y%m%d%H%i%s')" + 
						 ")";
			
			dbCon.stmt.execute(sql);
			
			jsonResp = jsonResp.replace("$(result)", "true");
			jsonResp = jsonResp.replace("$(value)", staff.getName() + "交班成功");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", e.getDesc());
			
		}catch(SQLException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}catch(IOException e){
			e.printStackTrace();
			jsonResp = jsonResp.replace("$(result)", "false");
			jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			
		}finally{
			dbCon.disconnect();
			//just for debug
			//System.out.println(jsonResp);
			out.write(jsonResp);
		}
		
		return null;
	}
}
