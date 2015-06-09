package com.wireless.Actions.deviceMgr;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.DeviceDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.pojo.staffMgr.Device.InsertBuilder;
import com.wireless.pojo.staffMgr.Device.Model;
import com.wireless.pojo.staffMgr.Device.Status;
import com.wireless.pojo.staffMgr.Device.UpdateBuilder;

public class OperateDeviceAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws BusinessException, Exception{
		
		JObject jobject = new JObject();
		String deviceId = request.getParameter("deviceId");
		String rId = request.getParameter("rId");
		String model = request.getParameter("model");
		
		try{
			Device.InsertBuilder builder = new InsertBuilder(deviceId, Integer.parseInt(rId))
											.setModel(Model.valueOf(Integer.parseInt(model)));
			
			DeviceDao.insert(builder);
			
			jobject.initTip(true, "添加成功");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getMessage());
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
	
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws SQLException, BusinessException, Exception{
		
		JObject jobject = new JObject();
		String id = request.getParameter("id");
		String deviceId = request.getParameter("deviceId");
		String rId = request.getParameter("rId");
		String status = request.getParameter("status");
		String model = request.getParameter("model");
		
		try{
			Device.UpdateBuilder builder = new UpdateBuilder(Integer.parseInt(id), deviceId, Integer.parseInt(rId))
											.setModel(Model.valueOf(Integer.parseInt(model))).setStatus(Status.valueOf(Integer.parseInt(status)));
			DeviceDao.update(builder);
			
			jobject.initTip(true, "修改成功");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
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
	
	
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws IOException{
		
		
		JObject jobject = new JObject();
		
		String id = request.getParameter("id");
		try{
			DeviceDao.deleteById(Integer.parseInt(id));
			
			jobject.initTip(true, "删除成功");
			
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
