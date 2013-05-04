package com.wireless.Actions.inventoryMgr.supplier;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.inventoryMgr.SupplierDAO;
import com.wireless.pojo.inventoryMgr.Supplier;
import com.wireless.protocol.Terminal;

public class QuerySupplierAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// TODO Auto-generated method stub
		DBCon dbCon = new DBCon();
		JSONObject jsonObject = new JSONObject();
		try{
			response.setContentType("text/json; charset=utf-8");
			String restaurntID = request.getParameter("restaurantID");
			String pin = request.getParameter("pin");
			String tele = request.getParameter("tele");
			String name = request.getParameter("name");
			String addr = request.getParameter("addr");
			String start = request.getParameter("start");
			String limit = request.getParameter("limit");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			dbCon.disconnect();
			String whereCondition = " WHERE 0 = 0 AND "+Supplier.TableFields.RESTAURANT_ID+" = "+restaurntID+(name.equals("")?"":" AND "+Supplier.TableFields.NAME+" LIKE '%"+name+"%' ")+(tele.equals("")?"":" AND "+Supplier.TableFields.TELE+" LIKE '%"+tele+"%' ")+(addr.equals("")?"":(" AND "+Supplier.TableFields.ADDR+" LIKE '%"+addr+"%' "))+" LIMIT "+start+","+limit+";";
			List<Supplier> suppliers = SupplierDAO.query(terminal, whereCondition);
			jsonObject.put("allCount", true);
			jsonObject.put("all", JSONArray.fromObject(suppliers).toString());
		}
		catch(Exception e){
			jsonObject.put("allCount", 0);
			jsonObject.put("all","[]");
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(jsonObject.toString());
		}
		return null;
	}
}
