package com.wireless.Actions.shift;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

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
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.staffMgr.Staff;

public class DoShiftAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		PrintWriter out = null;
		JObject jobject = new JObject();
		DutyRange dutyRange = null;
		try {
			out = response.getWriter();
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			dutyRange = ShiftDao.doShift(staff);
			
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
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("dutyRange", dutyRange);
			jobject.setOther(map);
			out.write(jobject.toString());
		}
		
		return null;
	}
}
