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
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.util.DataPaging;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMaterialCateAction extends DispatchAction{

	/**
	 * 
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
		List<MaterialCate> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin), Privilege.Code.INVENTORY);
			
			String restaurantID = request.getParameter("restaurantID");
			String type = request.getParameter("type");
			String extra = "";
			extra += " AND MC.restaurant_id = " + restaurantID;
			extra += " AND MC.type = " + type;
			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			root = MaterialCateDao.getContent(params);
			
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
			
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
	
	/**
	 * tree
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
		JObject jobject = new JObject();
		List<MaterialCate> list = null;
		MaterialCate item = null;
		StringBuffer tree = new StringBuffer();
		try{
//			String pin = (String) request.getSession().getAttribute("pin");
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
