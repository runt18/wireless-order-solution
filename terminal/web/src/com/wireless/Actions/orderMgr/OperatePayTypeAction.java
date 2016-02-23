package com.wireless.Actions.orderMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.orderMgr.PayTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.staffMgr.Staff;

public class OperatePayTypeAction extends DispatchAction{

	/**
	 * 新增付款方式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String name = request.getParameter("name");
		try{
			PayTypeDao.insert(StaffDao.verify(Integer.parseInt(pin)), new PayType.InsertBuilder(name));
			
			jObject.initTip(true, "添加成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}

	/**
	 * 修改付款方式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String name = request.getParameter("name");
		try{
			PayTypeDao.update(StaffDao.verify(Integer.parseInt(pin)), new PayType.UpdateBuilder(Integer.parseInt(id)).setName(name));
			
			jObject.initTip(true, "修改成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}	
	
	/**
	 * 删除付款方式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String id = request.getParameter("id");
		try{
			PayTypeDao.deleteById(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(id));
			
			jObject.initTip(true, "删除成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}		
	
	/**
	 * 获取付款方式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String id = request.getParameter("id");
		final String designed = request.getParameter("designed");
		final String extra = request.getParameter("extra");
		final String member = request.getParameter("member");
		final String mixed = request.getParameter("mixed");
		try{
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final PayTypeDao.ExtraCond extraCond = new PayTypeDao.ExtraCond();
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			
			if(designed != null && !designed.isEmpty() && Boolean.parseBoolean(designed)){
				extraCond.addType(PayType.Type.DESIGNED);
			}
			
			if(extra != null && !extra.isEmpty() && Boolean.parseBoolean(extra)){
				extraCond.addType(PayType.Type.EXTRA);
			}
			
			if(member != null && !member.isEmpty() && Boolean.parseBoolean(member)){
				extraCond.addType(PayType.Type.MEMBER);
			}
			
			if(mixed != null && !mixed.isEmpty() && Boolean.parseBoolean(mixed)){
				extraCond.addType(PayType.Type.MIXED);
			}
			
			jObject.setRoot(PayTypeDao.getByCond(staff, extraCond));
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}	
	
}
