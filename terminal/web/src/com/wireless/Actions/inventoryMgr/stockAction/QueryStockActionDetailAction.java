package com.wireless.Actions.inventoryMgr.stockAction;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDetailDao;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.util.WebParams;

public class QueryStockActionDetailAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<StockActionDetail> root = null;
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			String id = request.getParameter("id");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String  orderClause = "";
			if(id != null){
				root = StockActionDetailDao.getStockActionDetails(staff, " AND stock_action_id = " + Integer.parseInt(id), orderClause);
			}

			
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(root);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
