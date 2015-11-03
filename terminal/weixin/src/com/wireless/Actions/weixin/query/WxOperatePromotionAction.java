package com.wireless.Actions.weixin.query;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.json.JObject;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.staffMgr.Staff;

public class WxOperatePromotionAction extends DispatchAction{
	
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String fromId = request.getParameter("fid");
		//final String oid = request.getParameter("oid");

		final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fromId);
		final Staff staff = StaffDao.getAdminByRestaurant(rid);
		
		JObject jObj = new JObject();
		try{
			
			PromotionDao.ExtraCond extraCond = new PromotionDao.ExtraCond();
			if(request.getParameter("pid") != null && !request.getParameter("pid").isEmpty()){
				extraCond.setPromotionId(Integer.parseInt(request.getParameter("pid")));
			}
			
			List<Promotion> result = PromotionDao.getByCond(staff, extraCond);
			
			if(result.isEmpty()){
				jObj.initTip(false, "无相关活动");
			}else{
				jObj.setRoot(result);
			}
			
			
		}catch(SQLException e){
			e.printStackTrace();
			jObj.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObj.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObj.toString());
		}
		
		return null;		
		
	}	
	
}
