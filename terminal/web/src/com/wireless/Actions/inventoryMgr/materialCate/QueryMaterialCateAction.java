package com.wireless.Actions.inventoryMgr.materialCate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMaterialCateAction extends DispatchAction{

	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<MaterialCate> list = null;
		MaterialCate item = null;
		StringBuffer tree = new StringBuffer();
		try{
//			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			String type = request.getParameter("type");
			
			String extra = "";
			extra = " AND MC.restaurant_id = " + restaurantID;
			extra += (" AND MC.type = " + (type != null && !type.isEmpty() ? type : "2"));
			
			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			list = MaterialCateDao.getContent(params);
			tree.append("[");
			for(int i = 0; i < list.size(); i++){
				item = list.get(i);
				if(i>0)
					tree.append(",");	
				tree.append("{");
				tree.append("leaf:true");
				tree.append(",text:'" + item.getName() + "'");
				tree.append(",cateId:" + item.getId());
				tree.append(",name:'" + item.getName() + "'");
				tree.append(",type:" + item.getType().getValue());
				tree.append("}");
			}
			tree.append("]");
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree.toString());
		}
		
		return null;
	}

}
