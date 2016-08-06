package com.wireless.Actions.inventoryMgr.material;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.util.DataPaging;

public class QueryMaterialDeptAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String pin = (String)request.getAttribute("pin");
		final String deptId = request.getParameter("deptId");
		final String cateType = request.getParameter("cateType");
		final String cateId = request.getParameter("cateId");
		final String materialId = request.getParameter("materialId");
		final String checkAlarm = request.getParameter("checkAlarm");
		final JObject jObject = new JObject();
		try{

			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final MaterialDeptDao.ExtraCond extraCond = new MaterialDeptDao.ExtraCond();
			if(deptId != null && !deptId.trim().isEmpty()){
				extraCond.setDeptId(Integer.parseInt(deptId));
			}
			if(materialId != null && !materialId.trim().isEmpty() && !materialId.equals("-1")){
				extraCond.setMaterial(Integer.parseInt(materialId));
			}
			
			if(cateId != null && !cateId.trim().isEmpty()){
				extraCond.setMaterialCate(Integer.parseInt(cateId));
			}
			
			if (cateType != null && !cateType.trim().isEmpty() && !cateType.equals("-1")){
				extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
			}
			
			if(checkAlarm != null && !checkAlarm.isEmpty() && Integer.parseInt(checkAlarm) > 0){
				extraCond.setIsAlarm(Integer.parseInt(checkAlarm));
			}
			
			final List<MaterialDept> root = MaterialDeptDao.getByCond(staff, extraCond, null);
			
			final Map<Material, Map<Department, Float>> result1 = new HashMap<>();
			
			for(MaterialDept materialDept : root){
				Map<Department, Float> deptStocks = result1.get(materialDept.getMaterial());
				if(deptStocks != null){
					deptStocks.put(materialDept.getDept(), materialDept.getStock());
				}else{
					deptStocks = new HashMap<>();
					deptStocks.put(materialDept.getDept(), materialDept.getStock());
					result1.put(materialDept.getMaterial(), deptStocks);
				}
			}
			
			//获取所有库存分布的部门
			final List<Department> dept4Inventory = DepartmentDao.getDepartments4Inventory(staff);
			//库存分布中没有库存的数据用0补齐
			for(Map<Department, Float> deptStocks : result1.values()){
				for(Department dept : dept4Inventory){
					if(!deptStocks.containsKey(dept)){
						deptStocks.put(dept, 0f);
					}
				}
			}
			
			List<Jsonable> result = new ArrayList<>();
			float summaryStock = 0;
			float summaryCost = 0;
			for(final Map.Entry<Material, Map<Department, Float>> entry : result1.entrySet()){
				for (Map.Entry<Department, Float> entry4EachDept : entry.getValue().entrySet()) {
					summaryStock += entry4EachDept.getValue();
					summaryCost += entry.getKey().getPrice() * entry4EachDept.getValue();
				}
				
				result.add(new Jsonable(){

					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putString("materialName", entry.getKey().getName());
						jm.putFloat("minAlarmAmount", entry.getKey().getMinAlarmAmount());
						jm.putFloat("maxAlarmAmount", entry.getKey().getMaxAlarmAmount());
						//数量合计
						float totalStock = 0;
						//成本合计
						float totalCost = 0;
						for (Map.Entry<Department, Float> entry4EachDept : entry.getValue().entrySet()) {
							jm.putFloat("dept" + entry4EachDept.getKey().getId(), entry4EachDept.getValue());
							totalStock += entry4EachDept.getValue();
							totalCost += entry.getKey().getPrice() * entry4EachDept.getValue();
						}
						jm.putFloat("price", entry.getKey().getPrice());
						jm.putFloat("stock", totalStock);
						jm.putFloat("cost", totalCost);
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jm, int flag) {
						
					}
					
				});
			}

			jObject.setTotalProperty(result.size());
			
			result = DataPaging.getPagingData(result, true, start, limit);
			
			//增加最后一条汇总
			final float totalStock = summaryStock;
			final float totalCost = summaryCost;
			result.add(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("materialName", "汇总");
					jm.putFloat("stock", totalStock);
					jm.putFloat("cost", totalCost);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					
				}
				
			});
			
			jObject.setRoot(result);
			
			
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
			
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{

			response.getWriter().print(jObject.toString());
		}
		return null;
	}
}
