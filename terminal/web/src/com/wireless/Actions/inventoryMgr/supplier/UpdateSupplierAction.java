package com.wireless.Actions.inventoryMgr.supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class UpdateSupplierAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		// TODO Auto-generated method stub
		DBCon dbCon = new DBCon();
		JSONObject jsonObject = new JSONObject();
		try{
			response.setContentType("text/json; charset=utf-8");
			String pin = request.getParameter("pin");
			String restaurantID = request.getParameter("restaurantID");
			String supplierId = request.getParameter("supplierId");
			String name = request.getParameter("name");
			String tele = request.getParameter("tele");
			String addr = request.getParameter("addr");
			String comment = request.getParameter("comment");
			String contact = request.getParameter("contact");
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			dbCon.disconnect();
			String whereConditionQueryByID = " WHERE "+Supplier.TableFields.SUPPLIER_ID+" = "+supplierId+" AND "+Supplier.TableFields.RESTAURANT_ID+" = "+terminal.restaurantID+";";
			Supplier supplier = SupplierDAO.query(terminal, whereConditionQueryByID).get(0);
			supplier.setAddr(addr);
			supplier.setComment(comment);
			supplier.setContact(contact);
			supplier.setName(name);
			supplier.setTele(tele);
			String whereConditionUpdateByID = " WHERE "+Supplier.TableFields.SUPPLIER_ID+" = "+supplierId+" AND "+Supplier.TableFields.RESTAURANT_ID+" = "+terminal.restaurantID+";";
			SupplierDAO.update(terminal, supplier, whereConditionUpdateByID);
			jsonObject.put("success", true);
			jsonObject.put("msg", "成功保存一条记录!");
		}
		catch(Exception e){
			jsonObject.put("success", true);
			jsonObject.put("msg",e.getMessage());
			e.printStackTrace();
		}
		finally{
			response.getWriter().write(jsonObject.toString());
		}
		return null;
	}
}
