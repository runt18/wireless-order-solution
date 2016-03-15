package com.wireless.Actions.weixin.query;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxRestaurantError;
import com.wireless.json.JObject;
import com.wireless.listener.SessionListener;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class WxQueryDeptAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try{
			final String branchId = request.getParameter("branchId");
			String fid = request.getParameter("fid");
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			}
			
			Staff staff = StaffDao.getAdminByRestaurant(rid);
			List<Department> depts = DepartmentDao.getByType(staff, Department.Type.NORMAL);
			List<Kitchen> kitchens = KitchenDao.getByType(staff, Kitchen.Type.NORMAL);
			
			List<Map<String, Object>> deptList = new ArrayList<Map<String,Object>>();
			List<Kitchen> tempKitchenList = null;
			for(Department td : depts){
				tempKitchenList = new ArrayList<Kitchen>();
				for(Kitchen tk : kitchens){
					if(tk.getDept().getId() == td.getId()){
						tempKitchenList.add(tk);
					}
				}
				if(tempKitchenList != null && !tempKitchenList.isEmpty()){
					LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>(td.toJsonMap(0));
					map.put("kitchen", tempKitchenList);
					deptList.add(map);					
				}
			}
			depts = null;
			kitchens = null;
			tempKitchenList = null;
//			jobject.getExtra().put("dept", deptList);
		}catch(BusinessException e){
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
	
	/**
	 * 只获取普通部门列表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward queryDepts(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jobject = new JObject();
		try{
			final String branchId = request.getParameter("branchId");
			final String openId = request.getParameter("oid");
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(openId);
			}
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			jobject.setRoot(DepartmentDao.getByType(staff, Department.Type.NORMAL));
			
		}catch(BusinessException e){
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
	
	public ActionForward kitchen(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String branchId = request.getParameter("branchId");
		String fid = request.getParameter("fid");
		String oid = request.getParameter("oid");
		final String sessionId = request.getParameter("sessionId");
		final JObject jobject = new JObject();
		
		try{

			if(sessionId != null && !sessionId.isEmpty()){
				HttpSession session = SessionListener.sessions.get(sessionId);
				if(session != null){
					fid = (String)session.getAttribute("rid");
					oid = (String)session.getAttribute("oid");
				}else{
					throw new BusinessException(WxRestaurantError.WEIXIN_SESSION_TIMEOUT);
				}
			}
			
			final int rid;
			if(branchId != null && !branchId.isEmpty()){
				rid = Integer.parseInt(branchId);
			}else{
				rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			}
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			final List<Kitchen> list = new ArrayList<>(); 
			
			if(!FoodDao.getPureByCond(staff, new FoodDao.ExtraCond().setSellout(false).setRecomment(true).setContainsImage(true), null).isEmpty()){
				Kitchen star = new Kitchen(-10);
				star.setName("明星菜");
				list.add(star);
			}
			
			Member member = MemberDao.getById(staff, WxMemberDao.getBySerial(staff, oid).getMemberId());
			if(!member.getFavorFoods().isEmpty()){
				Kitchen favor = new Kitchen(-9);
				favor.setName("我的最爱");
				list.add(favor);
			}
			
			if(!member.getRecommendFoods().isEmpty()){
				Kitchen recommend = new Kitchen(-8);
				recommend.setName("为你推荐");
				list.add(recommend);
			}
			
			list.addAll(KitchenDao.getByCond(staff, new KitchenDao.ExtraCond().setContainsImage(true), null));
			
			jobject.setRoot(list);
			
		}catch(BusinessException | SQLException e){
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
