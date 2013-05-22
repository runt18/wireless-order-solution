package com.wireless.Actions.inventoryMgr.material;

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
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.util.DataPaging;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMaterialAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Material> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
//			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			String name = request.getParameter("name");
			String cateId = request.getParameter("cateId");
			
			String extra = "";
			extra += (" AND M.restaurant_id = " + restaurantID + " AND MC.type = " + MaterialCate.Type.MATERIAL.getValue());
			
			if(name != null && !name.trim().isEmpty()){
				extra += (" AND M.name like '%" + name + "%' ");
			}
			if(cateId != null && !cateId.trim().isEmpty()){
				extra += (" AND MC.cate_id = " + cateId);
			}
			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			
			root = MaterialDao.getContent(params);
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
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
}
