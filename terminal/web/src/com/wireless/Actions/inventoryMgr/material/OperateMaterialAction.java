package com.wireless.Actions.inventoryMgr.material;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.Material.MonthlyChangeTypeUpdateBuilder;
import com.wireless.pojo.staffMgr.Staff;

public class OperateMaterialAction extends DispatchAction {
	
	/**
	 * 添加原料
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String name = request.getParameter("name");
		final String cateId = request.getParameter("cateId");
		final String price = request.getParameter("price");
		final String alarmAmount = request.getParameter("alarmAmount");
		try{
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			MaterialDao.insert(staff, new Material.InsertBuilder()
											      .setName(name)
											      .setMaterialCate(MaterialCateDao.getById(staff, Integer.valueOf(cateId)))
											      .setPrice(Float.valueOf(price))
											      .setLastModStaff(staff.getName())
											      .setAlarmAmount(Integer.valueOf(alarmAmount)));
			
			jObject.initTip(true, "操作成功, 已添加新原料信息.");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 修改原料
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jobject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String name = request.getParameter("name");
		final String cateId = request.getParameter("cateId");
		final String price = request.getParameter("price");
		final String alarmAmount = request.getParameter("alarmAmount");
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final Material.UpdateBuilder builder = new Material.UpdateBuilder(Integer.valueOf(id)).setLastModStaff(staff.getName());
			
			if(cateId != null && !cateId.isEmpty()){
				builder.setMaterialCate(MaterialCateDao.getById(staff, Integer.valueOf(cateId)));
			}
			
			if(name != null && !name.isEmpty()){
				builder.setName(name);
			}
			
			if(price != null && !price.isEmpty()){
				builder.setPrice(Float.valueOf(price));
			}
			
			if(alarmAmount != null && !alarmAmount.isEmpty() && Integer.valueOf(alarmAmount) > 0){
				builder.setAlarmAmount(Integer.valueOf(alarmAmount));
			}
			
			MaterialDao.update(staff, builder);
			jobject.initTip(true, "操作成功, 已修改物品信息.");
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 删除原料
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String pin = (String)request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		JObject jobject = new JObject();
		try{
			MaterialDao.deleteById(staff, Integer.valueOf(request.getParameter("id")));
			jobject.initTip(true, "操作成功, 已删除物品信息.");
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
	
	public ActionForward monthSettleChangeType(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String editData = request.getParameter("editData");
		final String pin = (String)request.getAttribute("pin");
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String[] materialRecords = editData.split("<li>");
			for (String record : materialRecords) {
				
				String material[] = record.split(",");
				Material.MonthlyChangeTypeUpdateBuilder build = new MonthlyChangeTypeUpdateBuilder(Integer.parseInt(material[0]));
				build.setDelta(Float.parseFloat(material[1]));
				build.setLastModStaff(staff.getName());
				MaterialDao.updateDelta(staff, build);
			}
		}catch(SQLException e){	
			e.printStackTrace();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	
	public ActionForward cancelMonthSettle(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		try{
			final Staff staff = StaffDao.verify(Integer.valueOf(pin));
			MaterialDao.canelMonthly(staff);
			
		}catch(SQLException e){	
			e.printStackTrace();
		}
		return null;
	}
	
	
	public ActionForward monthSettleMaterial(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		try{
			final Staff staff = StaffDao.verify(Integer.valueOf(pin));
			MaterialDao.updateMonthly(staff);
		}catch(SQLException e){	
			e.printStackTrace();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 把菜品设置为商品
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward setToBeGood(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jobject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String material_goodList = request.getParameter("material_goodList");
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(!material_goodList.isEmpty()){
				String[] foods = material_goodList.split(",");
				DBCon dbCon = new DBCon();
				dbCon.connect();
				for (String food : foods) {
					MaterialDao.insertGoods(staff, FoodDao.getById(staff, Integer.parseInt(food)));
				}
				
				dbCon.disconnect();
			}
			jobject.initTip(true, "设置成功");
		}catch(SQLException e){	
			e.printStackTrace();
			jobject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}	
	
	
}
