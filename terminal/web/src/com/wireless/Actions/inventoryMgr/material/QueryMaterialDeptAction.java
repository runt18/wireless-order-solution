package com.wireless.Actions.inventoryMgr.material;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
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
		try{
			String pin = (String)request.getAttribute("pin");
			String deptId = request.getParameter("deptId");
			String cateType = request.getParameter("cateType");
			String cateId = request.getParameter("cateId");
			String materialId = request.getParameter("materialId");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String extraCond = "";
			if(deptId != null && !deptId.trim().isEmpty()){
				extraCond += " AND MD.dept_id = " + deptId;
			}
			if(materialId != null && !materialId.trim().isEmpty()){
				extraCond += " AND M.material_id = " + materialId;
			}else{
				if(cateId != null && !cateId.trim().isEmpty()){
					extraCond += " AND MC.cate_id = " + cateId;
				}
				if (cateType != null && !cateType.trim().isEmpty()){
					extraCond += " AND MC.type = " + cateType;
				}
			}
			List<MaterialDept> root = MaterialDeptDao.getMaterialDeptState(staff, extraCond, null);
			
			if(!root.isEmpty()){
				for (MaterialDept m : root) {
					if(m.getStock() > 0){
						mds.add(m);
					}
				}
			}
			
			
		}catch(BusinessException e){
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			e.printStackTrace();
			
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			if(mds != null){
				jobject.setTotalProperty(mds.size());
				jobject.setRoot(DataPaging.getPagingData(mds, "true", start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
