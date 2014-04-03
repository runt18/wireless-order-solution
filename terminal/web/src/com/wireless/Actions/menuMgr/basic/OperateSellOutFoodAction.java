package com.wireless.Actions.menuMgr.basic;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;

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
			jobject.initTip(e);
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
			jobject.initTip(true, "取消沽清成功");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}
}
