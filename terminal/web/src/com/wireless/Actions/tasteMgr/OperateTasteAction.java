package com.wireless.Actions.tasteMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteCategoryDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteCategory;

public class OperateTasteAction extends DispatchAction{

	public ActionForward insert(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String name = request.getParameter("name");
			String price = request.getParameter("price");
			String rate = request.getParameter("rate");
			String cate = request.getParameter("cate");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			TasteCategory category = new TasteCategory(Integer.parseInt(cate));
			Taste.InsertBuilder builder = new Taste.InsertBuilder(staff.getRestaurantId(), name, category);
			
			if(price != null && !price.isEmpty()){
				builder.setPrice(Float.valueOf(price));
			}
			if(rate != null && !rate.isEmpty()){
				builder.setRate(Float.valueOf(rate));
				
			}
			TasteDao.insert(staff, builder);
			jobject.initTip(true, "操作成功, 已添加新口味信息.");
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(IllegalArgumentException e){
			jobject.initTip4Exception(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		JObject jobject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String id = request.getParameter("id");
			String name = request.getParameter("name");
			String price = request.getParameter("price");
			String rate = request.getParameter("rate");
			String cate = request.getParameter("cate");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			TasteCategory category = TasteCategoryDao.getById(staff, Integer.parseInt(cate));
			
			
			Taste.UpdateBuilder builder = new Taste.UpdateBuilder(Integer.valueOf(id))
												   .setPrefence(name)
												   .setCategory(category);
			if(price != null && !price.isEmpty()){
				builder.setPrice(Float.valueOf(price));
			}
			if(rate != null && !rate.isEmpty()){
				builder.setRate(Float.valueOf(rate));
			}
			
			TasteDao.update(staff, builder);
			
			jobject.initTip(true, "操作成功, 已修改口味信息.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	public ActionForward delete(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		
		final JObject jObject = new JObject();
		try{
			TasteDao.delete(StaffDao.verify(Integer.parseInt(pin)), Integer.valueOf(id));
			jObject.initTip(true, "操作成功, 已删除口味信息.");
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
	 * 获取口味信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final JObject jObject = new JObject();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final TasteDao.ExtraCond extraCond = new TasteDao.ExtraCond();
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}
			
			jObject.setRoot(TasteDao.getByCond(staff, extraCond, null));
			
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
