package com.wireless.Actions.client.memberType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.pojo.client.MemberType;
import com.wireless.util.SQLUtil;

public class QueryMemberTypeTreeAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		StringBuffer tsb = new StringBuffer();
		try{
			String restaurantID = request.getParameter("restaurantID");
			Map<Object, Object> paramsSet = new HashMap<Object, Object>();
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.restaurant_id = " + restaurantID);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.member_type_id ");
			List<MemberType> list = MemberTypeDao.getMemberType(paramsSet);
			MemberType item = null;
			
			tsb.append("[");
			for(int i = 0; i < list.size(); i++){
				item = list.get(i);
				tsb.append(i > 0 ? "," : "");
				tsb.append("{");
				tsb.append("text:'" + item.getName() + "'");
				tsb.append(",");
				tsb.append("leaf:true");
				tsb.append(",");
				tsb.append("memberTypeID:" + item.getTypeId());
				tsb.append(",");
				tsb.append("memberTypeName:'" + item.getName() + "'");
				tsb.append("}");
			}
			tsb.append("]");
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tsb.toString());
		}
		return null;
	}
}
