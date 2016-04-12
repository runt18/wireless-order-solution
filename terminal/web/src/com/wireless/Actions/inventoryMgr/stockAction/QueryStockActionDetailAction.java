package com.wireless.Actions.inventoryMgr.stockAction;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDetailDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockActionDetail;

public class QueryStockActionDetailAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final String id = request.getParameter("id");
		
		final JObject jObject = new JObject();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final StockActionDetailDao.ExtraCond extraCond = new StockActionDetailDao.ExtraCond();
			if(id != null){
				extraCond.setStockAction(Integer.parseInt(id));
			}

			List<StockActionDetail> root = StockActionDetailDao.getByCond(staff, new StockActionDetailDao.ExtraCond().setStockAction(Integer.parseInt(id)), null);

			jObject.setTotalProperty(root.size());
			jObject.setRoot(root);
			
		}catch(BusinessException  | SQLException e){
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
