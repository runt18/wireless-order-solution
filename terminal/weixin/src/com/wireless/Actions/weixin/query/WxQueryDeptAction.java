package com.wireless.Actions.weixin.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.finance.WeixinFinanceDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class WxQueryDeptAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		try{
			
			String fid = request.getParameter("fid");
			int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			
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
			
			final String openId = request.getParameter("oid");
			final int rid = WeixinFinanceDao.getRestaurantIdByWeixin(openId);
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
		final JObject jobject = new JObject();
		
		try{
			
			final String fid = request.getParameter("fid");
			final int rid = WxRestaurantDao.getRestaurantIdByWeixin(fid);
			final Staff staff = StaffDao.getAdminByRestaurant(rid);
			
			final List<Kitchen> list = new ArrayList<>(); 
			
			if(!FoodDao.getPureByCond(staff, new FoodDao.ExtraCond().setSellout(false).setRecomment(true).setContainsImage(true), null).isEmpty()){
				Kitchen star = new Kitchen(-10);
				star.setName("明星菜");
				list.add(star);
			}
			
			list.addAll(KitchenDao.getByCond(staff, new KitchenDao.ExtraCond().setContainsImage(true), null));
			
			jobject.setRoot(list);
			
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
		
}
