package com.wireless.Actions.printScheme;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.staffMgr.Staff;

public class OperatePrinterAction extends DispatchAction{

	public ActionForward printerTree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String pin = (String)request.getAttribute("pin");
		final StringBuilder jsonSB = new StringBuilder();
		try{
			final Staff staff = StaffDao.getById(Integer.parseInt(pin));
			final List<Printer> result = PrinterDao.getByCond(staff, null);
			if(!result.isEmpty()){
				int index = 0;
				for (Printer printer : result) {
					jsonSB.append(index > 0 ? "," : "");
					jsonSB.append("{");
					jsonSB.append("printerId : '" + printer.getId() + "'");
					jsonSB.append(", id : '" + printer.getId() + "'");
					jsonSB.append(", restaurantId : '" + printer.getRestaurantId() + "'");
					jsonSB.append(", name :'" + printer.getName() + "'");
					jsonSB.append(", alias : '" + printer.getAlias() + "'");
					jsonSB.append(", styleValue : '" + printer.getStyle().getVal() + "'");
					jsonSB.append(", styleText : '" + printer.getStyle().getDesc() + "'");
					jsonSB.append(", isEnabled : " + printer.isEnabled());
					jsonSB.append(", orientedValue : '" + printer.getOriented().getVal() + "'");
					jsonSB.append(", orientedText : '" + printer.getOriented().toString() + "'");
					if(printer.getAlias().equals("")){
						jsonSB.append(", text : '" + printer.getName() + "(" + printer.getStyle().getDesc() + ")'");
					}else{
						jsonSB.append(", text : '" + printer.getName() + "(" + printer.getAlias() + " " + printer.getStyle().getDesc() + ")'");
					}
					jsonSB.append(", leaf : true" );
					if(!printer.isEnabled()){
						jsonSB.append(", iconCls : 'btn_error'");
					}else if(printer.getOriented() == Printer.Oriented.SPECIFIC){
						jsonSB.append(", icon : '../../images/printShift.png'");
					}else{
						jsonSB.append(", icon : '../../images/printer.png'");
					}
					jsonSB.append("}");
					index++;
					
				}
			}
		}catch(SQLException e){
			JObject jObj = new JObject();
			jObj.initTip(e);
			response.getWriter().print(jObj.toString());
		}finally{
			response.getWriter().print("[" + jsonSB.toString() + "]");
		}
		
		return null;
	}
	
	public ActionForward getByCond(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		final JObject jObject = new JObject();
		
		try{
			
			final String pin = (String)request.getAttribute("pin");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final String printerName = request.getParameter("printerName");
			final String isEnabled = request.getParameter("isEnabled");
			final String oriented = request.getParameter("oriented");
			
			PrinterDao.ExtraCond extraCond = new PrinterDao.ExtraCond();
			if(printerName != null && !printerName.isEmpty()){
				extraCond.setName(printerName);
			}
			if(isEnabled != null && !isEnabled.isEmpty()){
				extraCond.setEnabled(Boolean.parseBoolean(isEnabled));
			}
			if(oriented != null && !oriented.isEmpty()){
				extraCond.setOriented(Printer.Oriented.valueOf(Integer.parseInt(oriented)));
			}
			
			jObject.setRoot(PrinterDao.getByCond(staff, extraCond));
			
		}catch(BusinessException e){
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
	
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final JObject jObject = new JObject();
		final DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			final String pin = (String)request.getAttribute("pin");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final String printerId = request.getParameter("printerId");
			PrinterDao.deleteById(dbCon, staff, Integer.parseInt(printerId));
			jObject.initTip(true, "操作成功, 已删除打印机");
			
		}catch(BusinessException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jObject.toString());
		}
		
		return null;
		
		
	}
	
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		final JObject jObject = new JObject();
		
		final DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			final String pin = (String)request.getAttribute("pin");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final String printerName = request.getParameter("printerName");
			final String printerAlias = request.getParameter("printerAlias");
			final int style =Integer.parseInt(request.getParameter("style"));
			final String isEnabled = request.getParameter("isEnabled");
			final String oriented = request.getParameter("oriented");
			
			Printer.InsertBuilder builder = new Printer.InsertBuilder(printerName, PStyle.valueOf(style));
			builder.setAlias(printerAlias);
			builder.setEnabled(Boolean.parseBoolean(isEnabled));
			builder.setOriented(Printer.Oriented.valueOf(Integer.parseInt(oriented)));
			PrinterDao.insert(dbCon, staff, builder);
			jObject.initTip(true, "操作成功, 已添加打印机");
			
		}catch(BusinessException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jObject.toString());
		}
		
		return null;
		
	}
	
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final JObject jObject = new JObject();
		final DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			final String pin = (String)request.getAttribute("pin");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final String printerName = request.getParameter("printerName");
			final String printerAlias = request.getParameter("printerAlias");
			final String style = request.getParameter("style");
			final String printerId = request.getParameter("printerId");
			final String isEnabled = request.getParameter("isEnabled");
			final String oriented = request.getParameter("oriented");
			
			Printer.UpdateBuilder builder = new Printer.UpdateBuilder(Integer.parseInt(printerId));
			if(printerName != null && !printerName.isEmpty()){
				builder.setName(printerName);
			}
			if(style != null && !style.isEmpty()){
				builder.setStyle(PStyle.valueOf(Integer.parseInt(style)));
			}
			if(printerAlias != null && !printerAlias.isEmpty()){
				builder.setAlias(printerAlias);
			}
			if(isEnabled != null && !isEnabled.isEmpty()){
				builder.setEnabled(Boolean.parseBoolean(isEnabled));
			}
			if(oriented != null && !oriented.isEmpty()){
				builder.setOriented(Printer.Oriented.valueOf(Integer.parseInt(oriented)));
			}
			
			PrinterDao.update(dbCon, staff, builder);
			
			jObject.initTip(true, "操作成功,已修改打印机");
		}catch(BusinessException e){
			jObject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jObject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
}
