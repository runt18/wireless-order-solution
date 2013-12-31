package com.wireless.Actions.client.memberLevel;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberLevelDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.MemberLevel;
import com.wireless.pojo.client.MemberLevel.InsertBuilder;
import com.wireless.pojo.client.MemberLevel.UpdateBuilder;
import com.wireless.pojo.staffMgr.Staff;

public class OperateMemberLevelAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String point = request.getParameter("pointThreshold");
		String typeId = request.getParameter("memberTypeId");
		JObject jobject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MemberLevel.InsertBuilder builder = new InsertBuilder(Integer.parseInt(point), Integer.parseInt(typeId));
			builder.setRestaurantId(staff.getRestaurantId());
			
			MemberLevelDao.insert(staff, builder);
			jobject.initTip(true, "添加成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String id = request.getParameter("id");
		String point = request.getParameter("pointThreshold");
		String typeId = request.getParameter("memberTypeId");
		JObject jobject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			MemberLevel.UpdateBuilder builder = new UpdateBuilder(Integer.parseInt(id));
			if(point != null && !point.isEmpty()){
				builder.setPointThreshold(Integer.parseInt(point));
			}
			if(typeId != null && !typeId.isEmpty()){
				builder.setMemberTypeId(Integer.parseInt(typeId));
			}
			MemberLevelDao.update(staff, builder);
			jobject.initTip(true, "修改成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String id = request.getParameter("id");
		JObject jobject = new JObject();
		try{
			MemberLevelDao.delete(Integer.parseInt(id));
			jobject.initTip(true, "删除成功");
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	

}
