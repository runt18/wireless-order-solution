package com.wireless.Actions.inventoryMgr.material;

import java.sql.SQLException;
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

import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.util.DataPaging;
import com.wireless.util.SQLUtil;

public class QueryMaterialAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		List<Material> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = (String) request.getAttribute("restaurantID");
			String name = request.getParameter("name");
			String cateId = request.getParameter("cateId");
			String cateType = request.getParameter("cateType");
			String materialId = request.getParameter("materialId");
			String extra = "";
			extra += (" AND M.restaurant_id = " + restaurantID);
			
			if(cateType != null && !cateType.trim().isEmpty()){
				extra += (" AND MC.type = " + cateType);
			}
			
			if(name != null && !name.trim().isEmpty()){
				extra += (" AND M.name like '%" + name + "%' ");
			}
			if(cateId != null && !cateId.trim().isEmpty()){
				extra += (" AND MC.cate_id = " + cateId);
			}
			if(materialId != null && !materialId.trim().isEmpty()){
				extra += (" AND M.material_id = " + materialId);
			}
			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			
			root = MaterialDao.getContent(params);
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
	/**
	 * Get monthSettle materials
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward monthSettleMaterial(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		String mType = request.getParameter("mType");
		String type = request.getParameter("type");
		String restaurantID = (String) request.getAttribute("restaurantID");
		String cateId = request.getParameter("cateId");
		
		List<Material> root = null;
		String extra = "AND M.restaurant_id = " + restaurantID;
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(Food.StockStatus.MATERIAL.getVal() == Integer.parseInt(mType)){
				if(type != null && !type.trim().isEmpty()){
					extra += (" AND MC.type = " + type);
				}
				
				if(cateId != null && !cateId.trim().isEmpty()){
					extra += (" AND MC.cate_id = " + cateId);
				}
				
				Map<Object, Object> params = new LinkedHashMap<Object, Object>();
				params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
				params.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY ABS(delta) DESC");
				
				root = MaterialDao.getContent(params);
			}else if(Food.StockStatus.GOOD.getVal() == Integer.parseInt(mType)){
				if(type != null && !type.trim().isEmpty()){
					extra = null;
				}else{
					if(cateId != null && !cateId.trim().isEmpty()){
						extra = " AND F.kitchen_id = " + cateId;
					}
				}
				root = MaterialDao.getMonthSettleMaterial(staff, extra, " ORDER BY ABS(delta) DESC");
			}else{
				root = MaterialDao.getAllMonthSettleMaterial(staff.getRestaurantId());
			}
			jobject.setRoot(root);
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward stockTakeDetail(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		List<StockTakeDetail> root = new ArrayList<StockTakeDetail>();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String cateId = request.getParameter("cateId");
			String deptId = request.getParameter("deptId");
			if(cateId != null && !cateId.trim().isEmpty() && deptId != null){
				root = MaterialDeptDao.getStockTakeDetails(staff, Integer.parseInt(deptId), Integer.parseInt(cateId), " ORDER BY MD.stock DESC");
			}
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(root);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

	
	
}
