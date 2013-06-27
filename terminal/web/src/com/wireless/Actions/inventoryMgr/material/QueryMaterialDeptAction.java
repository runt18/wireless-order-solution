package com.wireless.Actions.inventoryMgr.material;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.json.JObject;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.protocol.Terminal;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryMaterialDeptAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		JObject jobject = new JObject();
		List<MaterialDept> root = null;
		try{
			String pin = request.getParameter("pin");
			String deptId = request.getParameter("deptId");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			String extraCond = "";
			if(deptId != null){
				extraCond = " AND dept_id = " + deptId;
			}
			root = MaterialDeptDao.getMaterialDepts(term, extraCond, null);
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, "true", start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}
