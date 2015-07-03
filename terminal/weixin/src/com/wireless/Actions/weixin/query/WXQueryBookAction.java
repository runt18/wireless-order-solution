package com.wireless.Actions.weixin.query;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.json.JObject;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;

public class WXQueryBookAction extends DispatchAction{
	
	public ActionForward region(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			
			String fid = request.getParameter("fid");
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(dbCon, fid);
			Staff staff = StaffDao.getAdminByRestaurant(dbCon, rid);
			jobject.setRoot(RegionDao.getByStatus(staff, Region.Status.BUSY));
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(dbCon != null) dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}		
		return null;
	}		
}
