package com.wireless.Actions.dailySettle;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.DailySettleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.staffMgr.Staff;

public class DailySettleExecAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {


		PrintWriter out = null;

		JObject jObj = new JObject();		
		
		try {
			// 解决后台中文传到前台乱码
			
			out = response.getWriter();

			String pin = (String)request.getAttribute("pin");

			Staff staff = StaffDao.verify(Integer.parseInt(pin));

			final DutyRange dutyRange = DailySettleDao.exec(staff).getRange();
			
			jObj.setExtra(new Jsonable(){

				@Override
				public Map<String, Object> toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("dutyRange", dutyRange, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});

			jObj.initTip(true, staff.getName() + "日结成功");

		} catch (BusinessException e) {
			e.printStackTrace();
			jObj.initTip(false, e.getDesc());
			
		} catch (SQLException e) {
			e.printStackTrace();
			jObj.initTip(false, "数据库请求发生错误，请确认网络是否连接正常");

		} catch (IOException e) {
			e.printStackTrace();
			jObj.initTip(false, "数据库请求发生错误，请确认网络是否连接正常");

		} finally {
			out.write(jObj.toString());
		}

		return null;
	}
}
