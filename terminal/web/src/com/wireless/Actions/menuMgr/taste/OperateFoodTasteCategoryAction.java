package com.wireless.Actions.menuMgr.taste;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteCategoryDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.TasteCategory;

public class OperateFoodTasteCategoryAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, SQLException, BusinessException{
		
		
		
		String pin =   (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String name = request.getParameter("tasteCateName");
		JObject jobject = new JObject();
		try{
			TasteCategory.InsertBuilder builder = new TasteCategory.InsertBuilder(staff.getRestaurantId(), name);
			TasteCategoryDao.insert(staff, builder);
			jobject.initTip(true, "添加成功");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, SQLException, BusinessException{
		
		
		String pin =   (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String categoryId = request.getParameter("categoryId");
		String name = request.getParameter("tasteCateName");
		JObject jobject = new JObject();
		try{
			TasteCategory.UpdateBuilder updateBuilder = new TasteCategory.UpdateBuilder(Integer.parseInt(categoryId), name);
			TasteCategoryDao.update(staff, updateBuilder);
			jobject.initTip(true, "修改成功");
		}catch(Exception e){
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, SQLException, BusinessException{
		
		
		String pin =   (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String categoryId = request.getParameter("categoryId");
		JObject jobject = new JObject();
		try{
			TasteCategoryDao.delete(staff, Integer.parseInt(categoryId));
			jobject.initTip(true, "删除成功");
		}catch(Exception e){
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward swap(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception, SQLException, BusinessException{
		String pin =   (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String cateA = request.getParameter("cateA");
		String cateB = request.getParameter("cateB");
		JObject jobject = new JObject();
		try{
			TasteCategory.SwapDisplayBuilder builder = new TasteCategory.SwapDisplayBuilder(Integer.parseInt(cateA), Integer.parseInt(cateB));
			TasteCategoryDao.swap(staff, builder);
		}catch(Exception e){
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
