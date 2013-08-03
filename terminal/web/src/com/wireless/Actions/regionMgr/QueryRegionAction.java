package com.wireless.Actions.regionMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.json.JObject;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.protocol.Terminal;

public class QueryRegionAction extends DispatchAction{
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			List<Region> list = RegionDao.getRegions(term, " AND REGION.restaurant_id = " + term.restaurantID, null);
			jobject.setRoot(list);
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		StringBuilder tsb = new StringBuilder();
		try{
			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(Long.valueOf(pin), Terminal.MODEL_STAFF);
			List<Region> list = RegionDao.getRegions(term, " AND REGION.restaurant_id = " + term.restaurantID, null);
			if(!list.isEmpty()){
				tsb.append("[");
				Region temp;
				for(int i = 0; i < list.size(); i++){
					temp = list.get(i);
					tsb.append(i > 0 ? "," : "")
					   .append("{")
					   .append("leaf:" + true)
					   .append(",")
					   .append("regionId:" + temp.getRegionId())
					   .append(",")
					   .append("regionName:'" + temp.getName() + "'")
					   .append(",")
					   .append("restaurantId:" + temp.getRestaurantId())
					   .append(",")
					   .append("text:'" + temp.getName() + "'")
					   .append("}");
					temp = null;
				}
				tsb.append("]");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tsb.toString());
		}
		return null;
	}

}
