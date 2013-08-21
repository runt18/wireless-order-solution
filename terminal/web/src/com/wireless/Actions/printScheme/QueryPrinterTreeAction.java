package com.wireless.Actions.printScheme;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.staffMgr.Staff;

public class QueryPrinterTreeAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		StringBuilder jsonSB = new StringBuilder();
		String pin = (String)request.getAttribute("pin");
		
		try{
			Staff staff = StaffDao.getStaffById(Integer.parseInt(pin));
			List<Printer> result = PrinterDao.getPrinters(staff);
			if(!result.isEmpty()){
				int index = 0;
				for (Printer printer : result) {
					jsonSB.append(index > 0 ? "," : "");
					jsonSB.append("{");
					jsonSB.append("printerId : '" + printer.getId() + "'");
					jsonSB.append(", restaurantId : '" + printer.getRestaurantId() + "'");
					jsonSB.append(", name :'" + printer.getName() + "'");
					jsonSB.append(", alias : '" + printer.getAlias() + "'");
					jsonSB.append(", styleValue : '" + printer.getStyle().getVal() + "'");
					jsonSB.append(", styleText : '" + printer.getStyle().getDesc() + "'");
					jsonSB.append(", isEnabled : " + printer.isEnabled());
					if(printer.getAlias().equals("")){
						jsonSB.append(", text : '" + printer.getName() + "(" + printer.getStyle().getDesc() + ")'");
					}else{
						jsonSB.append(", text : '" + printer.getName() + "(" + printer.getAlias() + " " + printer.getStyle().getDesc() + ")'");
					}
					jsonSB.append(", leaf : true" );
					if(!printer.isEnabled()){
						jsonSB.append(", iconCls : 'btn_error'");
					}else{
						jsonSB.append(", icon : '../../images/printer.png'");
					}
					jsonSB.append("}");
					index++;
					
				}
			}
		}finally{
			response.getWriter().print("[" + jsonSB.toString() + "]");
		}
		
		return null;
	}
}
