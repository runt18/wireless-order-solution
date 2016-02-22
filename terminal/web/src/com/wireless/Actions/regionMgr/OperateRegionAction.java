package com.wireless.Actions.regionMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;

public class OperateRegionAction extends DispatchAction{
	
	/**
	 * 更新区域
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		final String name = request.getParameter("name");
		
		final JObject jObject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			RegionDao.update(staff, new Region.UpdateBuilder(Short.valueOf(id), name.trim()));
			jObject.initTip(true, "操作成功, 已修改区域信息.");
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
	 * 新建区域信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insert(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String name = request.getParameter("name");
		final JObject jObject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			RegionDao.add(staff, new Region.AddBuilder(name));
			jObject.initTip(true, "操作成功, 已添加区域信息.");
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
	 * 删除区域
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		
		final JObject jObject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			RegionDao.remove(staff, Integer.parseInt(id));
			jObject.initTip(true, "删除成功");
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
	 * 获取区域
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String id = request.getParameter("id");
		final String status = request.getParameter("status");
		
		final JObject jObject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			final RegionDao.ExtraCond extraCond = new RegionDao.ExtraCond();
			
			if(id != null && !id.isEmpty()){
				extraCond.setId(Integer.parseInt(id));
			}

			if(status != null && !status.isEmpty()){
				extraCond.setStatus(Region.Status.valueOf(Integer.parseInt(status)));
			}else{
				extraCond.setStatus(Region.Status.BUSY);
			}
			
			jObject.setRoot(RegionDao.getByCond(staff, extraCond, null));
			
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
	 * 互换区域
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward swap(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String regionA = request.getParameter("regionA");
		final String regionB = request.getParameter("regionB");
		
		final JObject jObject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			RegionDao.move(staff, new Region.MoveBuilder(Integer.parseInt(regionA), Integer.parseInt(regionB)));
			//jobject.initTip(true, "删除成功");
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
	 * 生成区域树状
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		
		final StringBuilder tsb = new StringBuilder();
		try{
			Staff staff = StaffDao.getById(Integer.parseInt(pin));
			List<Region> list = RegionDao.getByStatus(staff, Region.Status.BUSY);
			if(!list.isEmpty()){
				tsb.append("[");
				for(int i = 0; i < list.size(); i++){
					Region temp = list.get(i);
					tsb.append(i > 0 ? "," : "")
					   .append("{")
					   .append("leaf:" + true)
					   .append(",")
					   .append("id:" + temp.getId())
					   .append(",")
					   .append("regionId:" + temp.getId())
					   .append(",")
					   .append("regionName:'" + temp.getName() + "'")
					   .append(",")
					   .append("restaurantId:" + temp.getRestaurantId())
					   .append(",")
					   .append("text:'" + temp.getName() + "'")
					   .append("}");
				}
				tsb.append("]");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tsb.toString());
		}
		return null;
	}
}
