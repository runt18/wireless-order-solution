package com.wireless.Actions.couponMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.coupon.CouponTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.coupon.CouponType;
import com.wireless.pojo.coupon.CouponType.InsertBuilder;

public class OperateCouponTypeAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String typeName = request.getParameter("typeName");
		String price = request.getParameter("price");
		String date = request.getParameter("date");
		String desc = request.getParameter("desc");
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			CouponTypeDao.insert(StaffDao.verify(Integer.parseInt(pin)), new InsertBuilder(typeName, Float.parseFloat(price))
																			.setExpired(Long.parseLong(date))
																			.setComment(desc));
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
		String typeId = request.getParameter("typeId");
		String typeName = request.getParameter("typeName");
		String date = request.getParameter("date");
		String desc = request.getParameter("desc");
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			CouponTypeDao.update(StaffDao.verify(Integer.parseInt(pin)), new CouponType.UpdateBuilder(Integer.parseInt(typeId), typeName)
																			.setExpired(Long.parseLong(date))
																			.setComment(desc));
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
		String typeId = request.getParameter("typeId");
		
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			CouponTypeDao.delete(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(typeId));
			jobject.initTip(true, "删除成功");
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

}
