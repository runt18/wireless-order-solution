package com.wireless.Actions.ota;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.OSSParams;
import com.wireless.util.OSSUtil;

public class QueryOTAction extends Action {


	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		PrintWriter out = null;
		
		String result = null;
		
		try {

			// 解决后台中文传到前台乱码
			
			out = response.getWriter();			
			result = "$(result)</br>$(value)";

			/**
			 * The parameters looks like below.
			 * 
			 * 1st - Get the version info
			 * funCode(1) : otaType
			 * otaType - '1' means phone for android 
			 * 			 '2' means pad for android
			 * 			 '3' means eMenu for android			  			 
			 * 
			 * 2nd - Get the root url to pictures
			 * funCode(2) : pin : model 
			 */
			int funCode = Integer.parseInt(request.getParameter("funCode"));
			if(funCode == 1){
				
			}else if(funCode == 2){
				int pin = Integer.parseInt(request.getParameter("pin"));
				Staff staff = StaffDao.getById(pin);
				// 获取图片操作路径(物理路径)
				String url = "http://" + OSSUtil.BUCKET_IMAGE + "." + OSSParams.instance().OSS_OUTER_POINT + "/" + staff.getRestaurantId() + "/";
				result = result.replace("$(result)", "true").replace("$(value)", url);
			}			
			
		}catch(BusinessException e){
			result = result.replace("$(result)", "false").replace("$(value)", e.getDesc());
		}catch(SQLException e){
			result = result.replace("$(result)", "false").replace("$(value)", e.getMessage());
		}finally{
			out.write(result);
			out.close();
		}
		
		return null;
	}
}
