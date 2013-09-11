package com.wireless.Actions.deviceMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.DeviceDao;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.util.WebParams;

public class QueryDeviceAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception{
		response.setCharacterEncoding("utf-8");
		JObject jobject = new JObject();
		List<Device> devices ;
		String rId = request.getParameter("rId");
		
		String extraCond = "", orderClause = null;
		try{
			if(rId != null){
				extraCond += " AND restaurant_id = " + rId;
			}
			devices = DeviceDao.getDevices(extraCond, orderClause);
			
			if(!devices.isEmpty()){
				jobject.setRoot(devices);
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(false, "数据库请求发生错误，请确认网络是否连接正常");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
}
