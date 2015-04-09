package com.wireless.Actions.weixin.query;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.json.JObject;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.staffMgr.Staff;

public class WXQueryTakeoutAddressAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		try{
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(request.getParameter("fid"));
			Staff mStaff = StaffDao.getAdminByRestaurant(rid);
			final Member member = MemberDao.getByWxSerial(mStaff, request.getParameter("oid"));			
			
			List<TakeoutAddress> list = TakeoutAddressDao.getByCond(StaffDao.getAdminByRestaurant(rid), new TakeoutAddressDao.ExtraCond().setMember(member));
			jobject.setRoot(list);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
