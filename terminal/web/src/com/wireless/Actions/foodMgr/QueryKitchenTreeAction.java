package com.wireless.Actions.foodMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.foodMgr.KitchenDAO;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.pojo.foodMgr.Kitchen;
import com.wireless.protocol.Terminal;

public class QueryKitchenTreeAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// TODO Auto-generated method stub
		DBCon dbCon = new DBCon();
		StringBuilder stringBuilder = new StringBuilder();
		try{
			response.setContentType("text/json; charset=utf-8");
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			dbCon.disconnect();
			String whereCondition = " WHERE "+Kitchen.TableFields.RESTAURANT_ID+" = "+terminal.restaurantID;
			List<Kitchen> kitchens = KitchenDAO.query(terminal, whereCondition);
			 stringBuilder.append("[");
			for(int i = 0;i < kitchens.size();i ++){
				String node = "{id:${id},text:'${text}',leaf:true}";
				node = node.replace("${id}", kitchens.get(i).getKitchenId()+"");
				node = node.replace("${text}", kitchens.get(i).getName());
				if(i != kitchens.size()-1){
					stringBuilder.append(node+",");
				}
				else{
					stringBuilder.append(node);
				}
			}
			 stringBuilder.append("]");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(stringBuilder.toString());
		}
		return null;
	}
}
