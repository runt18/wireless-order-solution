package com.wireless.Actions.inventoryMgr.materialCate;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.protocol.Terminal;

public class QueryMaterialCateTreeAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// TODO Auto-generated method stub
		DBCon dbCon = new DBCon();
		StringBuilder stringBuilder = new StringBuilder();
		try{
			response.setContentType("text/json; charset=utf-8");
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			dbCon.disconnect();
		    List<MaterialCate> materialCates = MaterialCateDao.select(terminal, "");
		    stringBuilder.append("[");
		    for(int i = 0;i < materialCates.size();i ++){
		    	String node = "{id:${id},text:'${text}',leaf:true}";
		    	if(i != materialCates.size()-1){
			    	node = node.replace("${id}", materialCates.get(i).getCateId()+"");
			    	node = node.replace("${text}", materialCates.get(i).getName());
			    	node += ",";
		    	}
		    	else{
		    		node = node.replace("${id}", materialCates.get(i).getCateId()+"");
			    	node = node.replace("${text}", materialCates.get(i).getName());
		    	}
		    	stringBuilder.append(node);
		    }
		    stringBuilder.append("]");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(stringBuilder.toString());
		}
		return null;
	}
}
