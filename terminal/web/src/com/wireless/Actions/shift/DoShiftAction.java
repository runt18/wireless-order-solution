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

import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.staffMgr.Staff;

public class DoShiftAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		PrintWriter out = null;
		JObject jobject = new JObject();
		try {
			out = response.getWriter();
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final DutyRange dutyRange = ShiftDao.doShift(staff);
			
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("dutyRange", dutyRange, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
			
			jobject.initTip(true, staff.getName() + "交班成功");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(IOException e){
			e.printStackTrace();
			jobject.initTip(false, "数据库请求发生错误，请确认网络是否连接正常");
			
		}finally{
			out.write(jobject.toString());
		}
		
		return null;
	}
}
