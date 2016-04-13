package com.wireless.Actions.inventoryMgr.materialCate;

import java.sql.SQLException;
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
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;
import com.wireless.util.SQLUtil;

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
		
		
		JObject jobject = new JObject();
		List<MaterialCate> root = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = (String) request.getAttribute("restaurantID");
			String type = request.getParameter("type");
			String extra = "";
			extra += " AND MC.restaurant_id = " + restaurantID;
			
			if(type != null && !type.isEmpty() && !type.equals("-1")){
				extra += " AND MC.type = " + type;
			}
			
			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			root = MaterialCateDao.getContent(params);
			
		}catch(BusinessException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, Boolean.parseBoolean(isPaging), start, limit));
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
		
		String pin = (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		StringBuilder tree = new StringBuilder();
		
		try{
			String good = children(staff, MaterialCate.Type.GOOD);
			if(!good.isEmpty()){
				tree.append("{")
				.append("text:'商品'")
				.append(",expanded:true")
				.append(",cate:" + MaterialCate.Type.GOOD.getValue())
				.append(",type:" + MaterialCate.Type.GOOD.getValue())
				.append(",children:[" + good + "]") 
				.append("}");						
			}
			
			String material = children(staff, MaterialCate.Type.MATERIAL);
			if(!material.isEmpty()){
				if(!tree.toString().isEmpty()){
					tree.append(",");
				}
				tree.append("{")
				.append("text:'原料'")
				.append(",expanded:true")
				.append(",cate:" + MaterialCate.Type.MATERIAL.getValue())
				.append(",type:" + MaterialCate.Type.MATERIAL.getValue())
				.append(",children:[" + material + "]") 
				.append("}");						
			}			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + tree.toString() + "]");
		}
		
		return null;
	}
	
	private String children(Staff staff, MaterialCate.Type type) throws SQLException{
		List<MaterialCate> list;
		StringBuilder tree = new StringBuilder();
		String extra = "";
		extra = " AND MC.restaurant_id = " + staff.getRestaurantId();
		extra += (" AND MC.type = " + type.getValue());
		
		Map<Object, Object> params = new LinkedHashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
		list = MaterialCateDao.getContent(params);
		for(int i = 0; i < list.size(); i++){
			MaterialCate item = list.get(i);
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
		return tree.toString();	

	}		
	
	/**
	 * MonthSettle tree
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
/*	public ActionForward monthSettleTree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		List<MaterialCate> list = null;
		MaterialCate item = null;
		List<Kitchen> kitchens = null;
		Kitchen kitchen = null;
		
		StringBuilder tree = new StringBuilder();
		StringBuilder inventory = new StringBuilder();
		StringBuilder goods = new StringBuilder();
		try{
			String restaurantID = (String) request.getAttribute("restaurantID");
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String extra = "";
			extra = " AND MC.restaurant_id = " + restaurantID;
			extra += (" AND MC.type = 2");
			
			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			list = MaterialCateDao.getContent(params);
			
			kitchens = KitchenDao.getMonthSettleKitchen(staff, null, " GROUP BY K.kitchen_id");
			
			tree.append("[");
			
			tree.append("{");
			tree.append("leaf:false");
			tree.append(",expanded:true");
			tree.append(",mType:" + Food.StockStatus.MATERIAL.getVal());
			tree.append(",type:" + MaterialCate.Type.MATERIAL.getValue());
			tree.append(",text:'原料'");
			for(int i = 0; i < list.size(); i++){
				item = list.get(i);
				if(i>0)
					inventory.append(",");	
				inventory.append("{");
				inventory.append("leaf:true");
				inventory.append(",text:'" + item.getName() + "'");
				inventory.append(",mText:'" + item.getName() + "'");
				inventory.append(",cateId:" + item.getId());
				inventory.append(",name:'" + item.getName() + "'");
				inventory.append(",mType:" + Food.StockStatus.MATERIAL.getVal());
				inventory.append(",changes: 0 ");
				inventory.append("}");
			}
			tree.append(",children : [" + inventory.toString() + "]");
			tree.append("},");
			
			tree.append("{");
			tree.append("leaf:false");
			tree.append(",expanded:true");
			tree.append(",type: " + + MaterialCate.Type.GOOD.getValue());
			tree.append(",text:'商品'");
			tree.append(",mType:" + Food.StockStatus.GOOD.getVal());
			for(int i = 0; i < kitchens.size(); i++){
				kitchen = kitchens.get(i);
				if(i>0)
					goods.append(",");	
				goods.append("{");
				goods.append("leaf:true");
				goods.append(",text:'" + kitchen.getName() + "'");
				goods.append(",mText:'" + kitchen.getName() + "'");
				goods.append(",cateId:" + kitchen.getId());
				goods.append(",name:'" + kitchen.getName() + "'");
				goods.append(",mType:" + Food.StockStatus.GOOD.getVal());
				goods.append(",changes: 0 ");
				goods.append("}");
			}
			tree.append(",children : [" + goods.toString() + "]");
			tree.append("}");
			
			tree.append("]");
			
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree.toString());
		}
		return null;
	}*/
}
