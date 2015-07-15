package com.wireless.Actions.inventoryMgr.material;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.db.stockMgr.StockInitDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.MonthlyBalance;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.util.DateUtil;

public class OperateMaterialInitlAction extends DispatchAction{

	public ActionForward getInitMaterial(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String deptId = request.getParameter("deptId");
		String cateType = request.getParameter("cateType");
		String cateId = request.getParameter("cateId");
		String name = request.getParameter("name");
		
		try{
			StockInitDao.ExtraCond extra = new StockInitDao.ExtraCond();
			
			if(cateType != null && !cateType.isEmpty()){
				extra.setCateType(Integer.parseInt(cateType));
			}
			
			if(cateId != null && !cateId.isEmpty()){
				extra.setCateId(Integer.parseInt(cateId));
			}
			
			if(name != null && !name.isEmpty()){
				extra.setName(name);
			}
			
			List<Material> ms = StockInitDao.getMaterialStockByDeptId(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(deptId), extra, null);
/*			if(deptId.equals("-1")){
				ms = StockInitDao.getMaterialStock(StaffDao.verify(Integer.parseInt(pin)), extra, null);
			}else{
				ms = StockInitDao.getMaterialStockByDeptId(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(deptId), extra, null);
//			}			
 */
			
			jobject.setRoot(ms);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
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

	public ActionForward updateDeptStock(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		String deptId = request.getParameter("deptId");
		String editData = request.getParameter("editData");
		String cateType = request.getParameter("cateType");
		DBCon dbCon = new DBCon();
		dbCon.connect();
		try{
			if(!editData.isEmpty()){
				Staff staff = StaffDao.verify(Integer.parseInt(pin));
				//保存初始化数据之前先初始化一次
				//删除月结明细记录
				String sql = "DELETE MD FROM " + Params.dbName + ".monthly_balance_detail MD " + 
						" JOIN " + Params.dbName + ".monthly_balance M ON M.id = MD.monthly_balance_id " +
						" WHERE M.restaurant_id = " + staff.getRestaurantId();
				dbCon.stmt.executeUpdate(sql);	
				
				//删除月结记录
				sql = "DELETE FROM " + Params.dbName + ".monthly_balance WHERE restaurant_id = " + staff.getRestaurantId();
				dbCon.stmt.executeUpdate(sql);
				
				Calendar c = Calendar.getInstance();
				c.add(Calendar.MONTH, -1);
				//初始化库存账单为上个月31
				String initDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.getActualMaximum(Calendar.DAY_OF_MONTH);
				
				InsertBuilder builder = StockAction.InsertBuilder.stockInit(staff.getRestaurantId(), DateUtil.parseDate(initDate))
						.setOriStockId("")
						.setOperatorId(staff.getId()).setOperator(staff.getName())
						.setComment("")
						.setCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)))
						.setDeptIn(Short.valueOf(deptId));
				
				
				
				String[] mds = editData.split("<li>");
				for (String md : mds) {
					String[] m = md.split(",");
					
					List<MaterialDept> list = MaterialDeptDao.getMaterialDepts(staff, " AND MD.material_id = " + m[0] + " AND MD.dept_id = " + deptId, null);
					Material material = MaterialDao.getById(Integer.parseInt(m[0]));
					if(list.isEmpty()){
						material.setStock(material.getStock() + Float.parseFloat(m[1]));
						
						MaterialDept mDept = new MaterialDept();
						mDept.setMaterial(material);
						mDept.setStock(Float.parseFloat(m[1]));
						mDept.setDeptId(Integer.parseInt(deptId));
						mDept.setRestaurantId(staff.getRestaurantId());
						MaterialDeptDao.insertMaterialDept(staff, mDept);	
						
						MaterialDao.update(material);
					}else{
						MaterialDept mDept = list.get(0);
						float delta = Float.parseFloat(m[1]) - mDept.getStock();
						material.setStock(material.getStock() + delta);
						MaterialDao.update(material);
						
						mDept.setStock(Float.parseFloat(m[1]));
						MaterialDeptDao.updateMaterialDept(staff, mDept);
					}
					
					builder.addDetail(new StockActionDetail(material.getId(), Float.parseFloat(m[2]), Float.valueOf(m[1])));

				}
				//设置总额
				builder.setInitActualPrice(builder.getTotalPrice());
				
				//添加并审核
				int stockActionId = StockActionDao.insertStockAction(dbCon, staff, builder);
				StockActionDao.auditStockAction(dbCon, staff, StockAction.AuditBuilder.newStockActionAudit(stockActionId).setStockInitApproverDate());
				
				MonthlyBalance.InsertBuilder monthBuild = new MonthlyBalance.InsertBuilder(staff.getRestaurantId(), staff.getName());

				MonthlyBalanceDao.insert(monthBuild, staff);
				jobject.initTip(true, "保存成功");				
			}

		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}

		return null;
	}
	
	public ActionForward init(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		JObject jobject = new JObject();
		String pin = (String) request.getAttribute("pin");
		try{
			StockInitDao.initStock(StaffDao.verify(Integer.parseInt(pin)));
			
			jobject.initTip(true, "初始化成功");			
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
	
	public ActionForward isInit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
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
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		
		return null;
	}		
	
}
