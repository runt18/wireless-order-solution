package com.wireless.Actions.inventoryMgr.material;

import java.util.ArrayList;
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
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.util.DataPaging;

public class QueryMaterialDeptAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		JObject jobject = new JObject();
		List<MaterialDept> mds = new ArrayList<>();
		List<Jsonable> result = new ArrayList<>();
		try{
			String pin = (String)request.getAttribute("pin");
			String deptId = request.getParameter("deptId");
			String cateType = request.getParameter("cateType");
			String cateId = request.getParameter("cateId");
			String materialId = request.getParameter("materialId");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String extraCond = "";
			if(deptId != null && !deptId.trim().isEmpty() && Integer.valueOf(deptId) >= 0){
				extraCond += " AND MD.dept_id = " + deptId;
			}
			if(materialId != null && !materialId.trim().isEmpty() && !materialId.equals("-1")){
				extraCond += " AND M.material_id = " + materialId;
			}else{
				if(cateId != null && !cateId.trim().isEmpty()){
					extraCond += " AND MC.cate_id = " + cateId;
				}
				if (cateType != null && !cateType.trim().isEmpty() && !cateType.equals("-1")){
					extraCond += " AND MC.type = " + cateType;
				}
			}
			final List<MaterialDept> root = MaterialDeptDao.getMaterialDeptState(staff, extraCond, null);
			//系统现用部门
			final List<Department> deptProperty = DepartmentDao.getDepartments4Inventory(staff);
			
			int lastMaterial = -1;
			float sum = 0;
			int index = 0;
			for (int i = 0; i < root.size(); i++) {
				
				if(lastMaterial != root.get(i).getMaterialId()){
					lastMaterial = root.get(i).getMaterialId();
					index = i;
					sum = 0;
					MaterialDept item = root.get(i);
					//动态设置部门属性和库存
					for (int k = 0; k < deptProperty.size(); k++) {
						if(item.getDeptId() == deptProperty.get(k).getId()){
							item.getDeptStock().put(deptProperty.get(k), item.getStock());
							
						}else{
							item.getDeptStock().put(deptProperty.get(k), 0f);
						}
					}
					item.setCost(item.getStock() * item.getMaterial().getPrice());	
					mds.add(item);
				}else{
					MaterialDept item = root.get(index);
					//对应部门库存
					for (Map.Entry<Department, Float> ds : item.getDeptStock().entrySet()) {
						if(root.get(i).getDeptId() == ds.getKey().getId()){
							ds.setValue(root.get(i).getStock());
							sum += item.getStock();
						}
					}
					item.setStock(sum);
					item.setCost(sum * item.getMaterial().getPrice());	
				}

			}
			
			if(!mds.isEmpty()){
				float sumStock = 0, sumCost = 0;
				for (int i = 0; i < mds.size(); i++) {
					final MaterialDept item = mds.get(i);
					sumStock += item.getStock();
					sumCost += item.getCost();
					
					Jsonable j = new Jsonable() {
						@Override
						public JsonMap toJsonMap(int flag) {
							JsonMap jm = new JsonMap();
							jm.putString("materialName", item.getMaterial().getName());
							
							for (Map.Entry<Department, Float> entry : item.getDeptStock().entrySet()) {
								jm.putFloat("dept"+entry.getKey().getId(), entry.getValue());
							}
							jm.putFloat("price", item.getMaterial().getPrice());
							jm.putFloat("stock", item.getStock());
							jm.putFloat("cost", item.getCost());
							return jm;
						}
						
						@Override
						public void fromJsonMap(JsonMap jm, int flag) {
						}
					};		
					result.add(j);
				}	
				
				final MaterialDept sumMT = mds.get(0);
				final float totalStock = sumStock, totalCost = sumCost;
				Jsonable j = new Jsonable() {
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putString("materialName", "汇总");
						
						for (Map.Entry<Department, Float> entry : sumMT.getDeptStock().entrySet()) {
							jm.putFloat("dept"+entry.getKey().getId(), 0);
						}
						jm.putFloat("price", 0);
						jm.putFloat("stock", totalStock);
						jm.putFloat("cost", totalCost);
						return jm;
					}
					
					@Override
					public void fromJsonMap(JsonMap jm, int flag) {
					}
				};
				result.add(j);
				
			}
			

			
/*			if(!root.isEmpty()){
				for (MaterialDept m : root) {
					if(m.getStock() > 0){
						mds.add(m);
					}
				}
			}*/
			
			
		}catch(BusinessException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			jobject.setTotalProperty(result.size());
			jobject.setRoot(DataPaging.getPagingData(result, "true", start, limit));
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
