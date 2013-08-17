package com.wireless.Actions.deptMgr.kitchenMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryKitchenAction extends DispatchAction {
	
	/**
	 * 树形数据格式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		StringBuffer jsb = new StringBuffer();
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String extraCond = "", orderClause = "";
			extraCond += (" AND KITCHEN.restaurant_id = " + staff.getRestaurantId());
			extraCond += (" AND KITCHEN.kitchen_alias <> 253 AND KITCHEN.kitchen_alias <> 255 ");
			List<Kitchen> list = KitchenDao.getKitchens(staff, extraCond, orderClause);
			for(int i = 0; i < list.size(); i++){
				if(i > 0){
					jsb.append(",");
				}
				jsb.append("{");
				jsb.append("leaf:true");
				jsb.append(",text:'" + list.get(i).getName() + "'");
				jsb.append(",alias:" + list.get(i).getAliasId());
				jsb.append(",name:'" + list.get(i).getName() + "'");
				jsb.append(",kid:" + list.get(i).getId());
				jsb.append("}");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + jsb.toString() + "]");
		}
		return null;
	}
	
	/**
	 * 普通数据格式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Kitchen> root = null;
		
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			String deptID = request.getParameter("deptID");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String extraCond = "", orderClause = "";
			
			extraCond += (" AND KITCHEN.restaurant_id = " + staff.getRestaurantId());
			extraCond += (" AND KITCHEN.kitchen_alias <> 253 AND KITCHEN.kitchen_alias <> 255 ");
			if(deptID != null && !deptID.trim().isEmpty() && !deptID.equals("-1")){
				extraCond += (" AND DEPT.dept_id = " + deptID);
			}
			
			orderClause = " ORDER BY KITCHEN.kitchen_alias ";
			
			root = KitchenDao.getKitchens(staff, extraCond, orderClause);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
