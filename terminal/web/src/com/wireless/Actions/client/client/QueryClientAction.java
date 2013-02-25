package com.wireless.Actions.client.client;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.ClientDao;
import com.wireless.pojo.client.Client;
import com.wireless.pojo.client.ClientType;
import com.wireless.util.DataPaging;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class QueryClientAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject= new JObject();
		List<Client> list = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String searchType = request.getParameter("searchType");
			String searchValue = request.getParameter("searchValue");
			String searchClientType = request.getParameter("searchClientType");
			
			String cond = " AND A.restaurant_id = " + restaurantID;
			
			if(searchClientType != null && !searchClientType.equals("-1")){
				List<ClientType> ct = ClientDao.getClientType(" AND restaurant_id = " + restaurantID, null);
				String childType = findChildType(ct, new ClientType(Integer.valueOf(searchClientType), "", 0, 0));
				cond += (" AND A.client_type_id in ( " + searchClientType + childType + ")");
			}
			
			if(searchType != null && searchValue != null){
				if(searchType.equals("1")){
					cond += (" AND A.name like '%" + searchValue + "%' ");
				}else if(searchType.equals("2")){
					cond += (" AND A.company like '%" + searchValue + "%' ");
				}else if(searchType.equals("3")){
					cond += (" AND A.mobile like '%" + searchValue + "%' ");
				}else if(searchType.equals("4")){
					cond += (" AND A.sex = " + searchValue);
				}else if(searchType.equals("5")){
					cond += (" AND A.client_id = " + searchValue);
				}
			}
			
			list = ClientDao.getClient(cond, null);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(DataPaging.getPagingData(list, isPaging, start, limit));
			}
			
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
	/**
	 * 递归查找某大类下的所有小类
	 * @param list
	 * @param item
	 * @return
	 */
	private String findChildType(List<ClientType> list, ClientType item){
		String childType = "";
		for(int i = 0; i < list.size(); i++){
			ClientType temp =  list.get(i);
			if(temp.getParentID() == item.getTypeID()){
				childType += ("," + temp.getTypeID());
				childType += findChildType(list, temp);
			}
		}
		return childType;
	}
	
}
