package com.wireless.Actions.client.clientType;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.ClientDao;
import com.wireless.pojo.client.ClientType;

public class QueryClientTypeTreeAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String tree = null;
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			
			List<ClientType> list = ClientDao.getClientType(" AND restaurant_id = " + restaurantID, null);
			
//			System.out.println(list.size());
			
			tree = createTree(list);
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tree);
		}
		
		return null;
	}
	
	private String createTree(List<ClientType> list){
		StringBuffer tsb = new StringBuffer();
		tsb.append("[");
		int index = 0;
		
		for(int i = 0; i < list.size(); i++){
			ClientType item = list.get(i);
			if(item.getParentID() == -1){
				tsb.append(index > 0 ? "," : "");
				tsb.append("{");
				tsb.append("text:'" + item.getName() + "'");
				tsb.append(",");
				tsb.append("expanded:true");
				tsb.append(",");
				tsb.append("clientTypeID:" + item.getTypeID());
				tsb.append(",");
				tsb.append("clientTypeName:'" + item.getName() + "'");
				tsb.append(",");
				tsb.append("clientParentTypeID:" + item.getParentID());
				tsb.append("");
				String children = checkChildren(list, item);
				if(children != null && children.length() > 0){
					tsb.append(",");
					tsb.append("leaf:false");
					tsb.append(",");
					tsb.append("children:[" + children + "]");
				}else{
					tsb.append(",");
					tsb.append("leaf:true");
				}
				tsb.append("}");
				index++;
			}
		}
		tsb.append("]");
		
		return tsb.toString();
	}
	
	private String checkChildren(List<ClientType> list, ClientType root){
		StringBuffer trc = new StringBuffer();
		int index = 0;
		
		for(int i = 0; i < list.size(); i++){
			ClientType item = list.get(i);
			if(item.getParentID() == root.getTypeID()){
				trc.append(index > 0 ? "," : "");
				trc.append("{");
				trc.append("text:'" + item.getName() + "'");
				trc.append(",");
				trc.append("expanded:true");
				trc.append(",");
				trc.append("clientTypeID:" + item.getTypeID());
				trc.append(",");
				trc.append("clientTypeName:'" + item.getName() + "'");
				trc.append(",");
				trc.append("clientParentTypeID:" + item.getParentID());
				trc.append("");
				String children = checkChildren(list, item);
				if(children != null && children.length() > 0){
					trc.append(",");
					trc.append("leaf:false");
					trc.append(",");
					trc.append("children:[" + children + "]");
				}else{
					trc.append(",");
					trc.append("leaf:true");
				}
				trc.append("}");
				index++;
			}
		}
		
		return trc.toString();
	}
	
	
}
