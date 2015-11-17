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

public class QueryDeviceAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws SQLException, Exception{
		
		final JObject jobject = new JObject();
		final String rId = request.getParameter("rId");
		final String rName = request.getParameter("rName");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		try{
			String extraCond = "";
			if(rId != null && !rId.trim().isEmpty()){
				extraCond += " AND DEV.restaurant_id = " + rId;
			}else if(rName != null && !rName.trim().isEmpty()){
				extraCond += " AND RES.restaurant_name LIKE '%" + rName + "%' ";
			}
			
			final String orderClause;
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				orderClause = " LIMIT " + start + "," + limit;
			}else{
				orderClause = null;
			}
			final List<Device> result = DeviceDao.getDevices(extraCond, orderClause);
			
			if(!result.isEmpty()){
				jobject.setRoot(result);
			}
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
		
	}
	
}
