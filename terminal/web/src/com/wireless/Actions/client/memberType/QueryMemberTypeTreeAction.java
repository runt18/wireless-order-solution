package com.wireless.Actions.client.memberType;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.pojo.client.MemberType;

public class QueryMemberTypeTreeAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		StringBuilder tsb = new StringBuilder();
		try{
			String pin = (String)request.getAttribute("pin");
			List<MemberType> list = MemberTypeDao.getMemberType(StaffDao.verify(Integer.parseInt(pin)), null, " ORDER BY A.member_type_id ");
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
				tsb.append("memberTypeID:" + item.getId());
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
