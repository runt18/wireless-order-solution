package com.wireless.Actions.client.memberType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMemberTypeAction extends DispatchAction {
	
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
		List<MemberType> list = new ArrayList<MemberType>();
		
		try{
			
			String pin = (String) request.getSession().getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin), Privilege.Code.MEMBER);
			
			String restaurantID = request.getParameter("restaurantID");
			String name = request.getParameter("name");
			String discountType = request.getParameter("discountType");
			String attr = request.getParameter("attr");
			
			String extraCond = (" AND A.restaurant_id = " + restaurantID);
			if(name != null && !name.trim().isEmpty()){
				extraCond += (" AND A.name like '%" + name.trim() + "%' ");
			}
			if(discountType != null && !discountType.trim().isEmpty() && !discountType.equals("-1")){
				extraCond += (" AND A.discount_type = " + discountType);
			}
			if(attr != null && !attr.trim().isEmpty()){
				extraCond += (" AND A.attribute = " + attr);
			}
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>();
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.member_type_id ");
			list = MemberTypeDao.getMemberType(paramsSet);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_EXCEPTION, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			jobject.setTotalProperty(list.size());
			jobject.setRoot(list);
			
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 
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
		StringBuffer tsb = new StringBuffer();
		try{
			String restaurantId = request.getParameter("restaurantId");
			Map<Object, Object> paramsSet = new HashMap<Object, Object>();
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.restaurant_id = " + restaurantId);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.member_type_id ");
			List<MemberType> list = MemberTypeDao.getMemberType(paramsSet);
			MemberType item = null;
			
			StringBuilder change = new StringBuilder(), point = new StringBuilder();
			change.append("{")
				  .append("text:'充值属性'")
				  .append(",attr:"+MemberType.Attribute.CHARGE.getVal())
				  .append(",expanded:true")
				  .append(",children:[");
			point.append("{")
				 .append("text:'积分属性'")
				 .append(",attr:"+MemberType.Attribute.POINT.getVal())
				 .append(",expanded:true")
				 .append(",children:[");
			boolean hc = false, hp = false;
			for(int i = 0; i < list.size(); i++){
				item = list.get(i);
				if(item.getAttribute() == MemberType.Attribute.CHARGE){
					change.append(hc ? "," : "");
					children(item, change);
					if(!hc){hc = true;}
				}else if(item.getAttribute() == MemberType.Attribute.POINT){
					point.append(hp ? "," : "");
					children(item, point);
					if(!hp){hp = true;}
				}
			}
			change.append("]}");
			point.append("]}");
			tsb.append("[").append(change).append(",").append(point).append("]");
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tsb.toString());
		}
		return null;
	}
	
	private StringBuilder children(MemberType item, StringBuilder sb){
		sb.append("{")
			.append("text:'" + item.getName() + "'")
			.append(",leaf:true")
			.append(",memberTypeId:" + item.getTypeId())
			.append(",memberTypeName:'" + item.getName() + "'")
			.append("}");
		return sb;
	}
	
}
