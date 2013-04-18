package com.wireless.Actions.regionMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.protocol.Terminal;

public class QueryRegionTreeAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		StringBuffer tree = new StringBuffer();
		
		try{
			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			
			List<Region> list = RegionDao.getRegions(term, " AND REGION.restaurant_id = " + term.restaurantID, null);
			
			if(!list.isEmpty()){
				tree.append("[");
				for(int i = 0; i < list.size(); i++){
					Region temp = list.get(i);
					tree.append(i > 0 ? "," : "");
					tree.append("{");
					tree.append("leaf:" + true);
					tree.append(",");
					tree.append("regionID:" + temp.getId());
					tree.append(",");
					tree.append("regionName:'" + temp.getName() + "'");
					tree.append(",");
					tree.append("restaurantID:" + temp.getRestaurantId());
					tree.append(",");
					tree.append("text:'" + temp.getName() + "'");
					tree.append("}");
				}
				tree.append("]");
			}
			
		}catch(Exception e){
			e.printStackTrace();
			
		}finally{
			response.getWriter().print(tree.toString());
		}
		return null;
	}
}
