package com.wireless.Actions.inventoryMgr.material;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.inventoryMgr.FoodMaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.FoodMaterial;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryFoodMaterialAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String foodId = request.getParameter("foodId");
			
			String extra = " AND MC.type = " + MaterialCate.Type.MATERIAL.getValue();
			extra += (" AND FM.food_id = " + foodId);
			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			
			List<FoodMaterial> root = FoodMaterialDao.getList(params);
			jobject.setRoot(root);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
