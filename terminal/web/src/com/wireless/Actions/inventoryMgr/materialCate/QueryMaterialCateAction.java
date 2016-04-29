package com.wireless.Actions.inventoryMgr.materialCate;

import java.sql.SQLException;
import java.util.List;

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

public class QueryMaterialCateAction extends DispatchAction{


	
	/**
	 * 获取原料类型
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		final JObject jObject = new JObject();
		final String isPaging = request.getParameter("isPaging");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String pin = (String)request.getAttribute("pin");
		final String type = request.getParameter("type");
		try{
			MaterialCateDao.ExtraCond extraCond = new MaterialCateDao.ExtraCond();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(type != null && !type.isEmpty()){
				extraCond.setType(MaterialCate.Type.valueOf(Integer.valueOf(type)));
			}

			final List<MaterialCate> result = MaterialCateDao.getByCond(staff, extraCond);
			jObject.setRoot(DataPaging.getPagingData(result, Boolean.parseBoolean(isPaging), start, limit));
			jObject.setTotalProperty(result.size());
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}finally{
			response.getWriter().print(jObject.toString());
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
	public ActionForward tree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String) request.getAttribute("pin");
		final StringBuilder tree = new StringBuilder();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final String good = children(staff, MaterialCate.Type.GOOD);
			if(!good.isEmpty()){
				tree.append("{")
				.append("text:'商品'")
				.append(",expanded:true")
				.append(",cate:" + MaterialCate.Type.GOOD.getValue())
				.append(",type:" + MaterialCate.Type.GOOD.getValue())
				.append(",children:[" + good + "]") 
				.append("}");						
			}
			
			final String material = children(staff, MaterialCate.Type.MATERIAL);
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

	
	/**
	 * 获取字树项
	 * @param staff
	 * @param type
	 * @return
	 * @throws SQLException
	 */
	private String children(Staff staff, MaterialCate.Type type) throws SQLException{
		final StringBuilder tree = new StringBuilder();
		final List<MaterialCate> list = MaterialCateDao.getByCond(staff, new MaterialCateDao.ExtraCond().setType(type));
		for(int i = 0; i < list.size(); i++){
			MaterialCate item = list.get(i);
			if(i > 0){
				tree.append(",");
			}
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
}
