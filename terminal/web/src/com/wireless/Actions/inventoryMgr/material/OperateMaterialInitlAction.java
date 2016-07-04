package com.wireless.Actions.inventoryMgr.material;

import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockInitDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.util.DateUtil;

public class OperateMaterialInitlAction extends DispatchAction{
	
	/**
	 * 获取所有商品
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getInitMaterial(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jobject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String deptId = request.getParameter("deptId");
		final String cateType = request.getParameter("cateType");
		final String cateId = request.getParameter("cateId");
		final String name = request.getParameter("name");
		
		try{
			final StockInitDao.ExtraCond extra = new StockInitDao.ExtraCond();
			
			
			if(cateType != null && !cateType.isEmpty()){
				extra.setCateType(Integer.parseInt(cateType));
			}
			
			if(cateId != null && !cateId.isEmpty()){
				extra.setCateId(Integer.parseInt(cateId));
			}
			
			if(name != null && !name.isEmpty()){
				extra.setName(name);
			}
			
			jobject.setRoot(StockInitDao.getMaterialStockByDeptId(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(deptId), extra, null));
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}

	
	/**
	 * 修改部门仓库库存量
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateDeptStock(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final JObject jObject = new JObject();
		final String pin = (String) request.getAttribute("pin");
		final String deptId = request.getParameter("deptId");
		final String editData = request.getParameter("editData");
		final String cateType = request.getParameter("cateType");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		try{
//			if(!editData.isEmpty()){

				Calendar c = Calendar.getInstance();
				c.add(Calendar.MONTH, -1);
				//初始化库存账单为上个月31
				String initDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.getActualMaximum(Calendar.DAY_OF_MONTH);
				
				InsertBuilder builder = StockAction.InsertBuilder.stockInit(staff.getRestaurantId(), DateUtil.parseDate(initDate))
																 .setOriStockId("")
																 .setOperatorId(staff.getId()).setOperator(staff.getName())
																 .setComment("")
																 .setCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)))
															 	 .setDeptIn(Short.parseShort(deptId));
				
				if(!editData.isEmpty()){
					for (String md : editData.split("<li>")) {
						String[] m = md.split(",");
						
						builder.addDetail(new StockActionDetail(Integer.parseInt(m[0]), Float.parseFloat(m[2]), Float.parseFloat(m[1])));
	
					}
				}
			
				StockAction stockAction = builder.build();
				//设置总额
				builder.setInitActualPrice(stockAction.getTotalPrice());
				
				StockInitDao.insert(staff, builder);
				jObject.initTip(true, "保存成功");				
//			}

		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}

		return null;
	}
	
	
	
	
	/**
	 * 清空数据
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward init(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		try{
			StockInitDao.initStock(StaffDao.verify(Integer.parseInt(pin)));
			
			jobject.initTip(true, "初始化成功");			
		}catch(SQLException | BusinessException	e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}	
	
	
	
	/**
	 * 判定初始化后是否有出入库操作
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward isInit(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		try{
			if(StockInitDao.isInit(StaffDao.verify(Integer.parseInt(pin)))){
				jobject.initTip(true, "");	
			}else{
				jobject.initTip(false, "");
			}
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		
		return null;
	}		
	
}
