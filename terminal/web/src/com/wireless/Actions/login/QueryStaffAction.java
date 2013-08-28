package com.wireless.Actions.login;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryStaffAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PrintWriter out = null;

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		JObject jobject = new JObject();
		List<Staff> staffList = new ArrayList<Staff>();
		int index = 0;
		int pageSize = 0;
		if (!(start == null)) {
			index = Integer.parseInt(start);
			pageSize = Integer.parseInt(limit);
		}


		// 是否分頁
		String isPaging = request.getParameter("isPaging");
		// 是否combo
		String isCombo = request.getParameter("isCombo");
		Map<Object, Object> other = new HashMap<Object, Object>();
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			
			out = response.getWriter();
			String restaurantID ;
				
			restaurantID = request.getParameter("restaurantID");
			
			// get the type to filter
/*			int type = Integer.parseInt(request.getParameter("type"));

			// get the operator to filter
			String ope = request.getParameter("ope");
			if (ope != null) {
				int opeType = Integer.parseInt(ope);

				if (opeType == 1) {
					ope = "=";
				} else if (opeType == 2) {
					ope = ">=";
				} else if (opeType == 3) {
					ope = "<=";
				} else {
					// 不可能到这里
					ope = "=";
				}
			} else {
				// 不可能到这里
				ope = "";
			}*/

			// get the value to filter
/*			String filterVal = request.getParameter("value");

			// combine the operator and filter value
			String filterCondition = null;
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "姓名" ]
			if (type == 1) {
				// 按编号
				filterCondition = " AND staff_alias " + ope + filterVal;
			} else if (type == 2) {
				// 按姓名
				filterCondition = " AND name like '%" + filterVal + "%'";
			} else {
				// 全部
				filterCondition = "";
			}*/
			staffList = StaffDao.getStaffs(Integer.parseInt(restaurantID));
			
			jobject.setMsg("normal");
			

		} catch (SQLException e) {
			e.printStackTrace();
			jobject.initTip(false, "数据库请求发生错误，请确认网络是否连接正常");

		} catch (IOException e) {
			e.printStackTrace();
			jobject.initTip(false, "未处理异常");
		}catch (BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, e.getMessage());
		} finally {
			if (isCombo.equals("true")) {
				
			} else {
				staffList = DataPaging.getPagingData(staffList, isPaging, index, pageSize);
			}	
			if(request.getAttribute("pin") != null){
				other.put("pin", request.getAttribute("pin"));
			}
			jobject.setTotalProperty(staffList.size());
			jobject.setRoot(staffList);
			jobject.setOther(other);
			out.write(jobject.toString());
		}

		return null;
	}
}
