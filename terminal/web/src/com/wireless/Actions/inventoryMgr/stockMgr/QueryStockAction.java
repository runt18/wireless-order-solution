package com.wireless.Actions.inventoryMgr.stockMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.json.JObject;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.protocol.Terminal;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryStockAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		List<StockAction> root = null;
		try{
			String pin = request.getParameter("pin");
			String stockType = request.getParameter("stockType");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			String extraCond = "", orderClause = "";
			if(stockType != null && !stockType.trim().isEmpty()){
				extraCond += (" AND type = " + stockType);
			}
			
			root = StockActionDao.getStockIns(term, extraCond, orderClause);
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
