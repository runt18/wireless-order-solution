package com.wireless.Actions.menuMgr.pricePlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.util.SQLUtil;

public class QueryPricePlanTreeAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		
		
		List<PricePlan> list = null;
		PricePlan item = null;
		StringBuffer sbt = new StringBuffer();
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String extra = "", orderBy = null;
			String restaurantID = (String)request.getAttribute("restaurantID");
			extra += (" AND A.restaurant_id = " + restaurantID);
			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			params.put(SQLUtil.SQL_PARAMS_ORDERBY, orderBy);
			list = MenuDao.getPricePlan(params);
			if(list != null){
				sbt.append("[");
				for(int i = 0; i < list.size(); i++){
					item = list.get(i);
					sbt.append(i > 0 ? "," : "");
					sbt.append("{");
					sbt.append("leaf:true");
					sbt.append(",");
					sbt.append("text:'" + item.getName() + "'");
					sbt.append(",");
					sbt.append("pricePlanName:'" + item.getName() + "'");
					sbt.append(",");
					sbt.append("pricePlanID:" + item.getId());
					sbt.append(",");
					sbt.append("statusValue:" + item.getStatus().getVal());
					sbt.append(",");
					sbt.append("statusText:'" + item.getStatus().getDesc() + "'");
					sbt.append("}");
					item = null;
				}
				sbt.append("]");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(sbt.toString());
		}
		return null;
	}

}
