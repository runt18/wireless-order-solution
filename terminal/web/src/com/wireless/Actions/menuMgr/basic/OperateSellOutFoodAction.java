package com.wireless.Actions.menuMgr.basic;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;

public class OperateSellOutFoodAction extends DispatchAction{

	public ActionForward sellOut(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String foodId = request.getParameter("foodIds");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try{
			String[] foodIds = foodId.split(",") ;
			for (String id : foodIds) {
				Food.UpdateBuilder builder = new Food.UpdateBuilder(Integer.parseInt(id)).setSellOut(true);
				FoodDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
			}
			jobject.initTip(true, "沽清成功");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

	
	public ActionForward deSellOut(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String foodId = request.getParameter("foodIds");
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try{
			String[] foodIds = foodId.split(",") ;
			for (String id : foodIds) {
				Food.UpdateBuilder builder = new Food.UpdateBuilder(Integer.parseInt(id)).setSellOut(false);
				FoodDao.update(StaffDao.verify(Integer.parseInt(pin)), builder);
			}
			jobject.initTip(true, "开售成功");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}
	
	/**
	 * 限量沽清重置
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward resetFoodLimit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		DBCon con = null;
		try{
			con = new DBCon();
			con.connect();
			FoodDao.restoreLimit(con, StaffDao.verify(Integer.parseInt(pin)));
			jobject.initTip(true, "重置成功");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			con.disconnect();
			response.getWriter().print(jobject.toString());
		}

		return null;
	}	

	/**
	 * 剩余数量设置
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward setFoodLimit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final String pin = (String)request.getAttribute("pin");
		final String foodId = request.getParameter("foodId");
		final String remain = request.getParameter("remain");
		
		JObject jobject = new JObject();
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			Food.LimitRemainingBuilder builder = new Food.LimitRemainingBuilder(Integer.parseInt(foodId), Integer.parseInt(remain));
			
			FoodDao.update(staff, builder);
			jobject.initTip(true, "设定成功");
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		
		return null;
	}
}

