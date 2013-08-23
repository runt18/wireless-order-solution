package com.wireless.Actions.printScheme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.printScheme.PrintFuncDao;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.printScheme.PrintFunc.Builder;
import com.wireless.pojo.printScheme.PrintFunc.DetailBuilder;
import com.wireless.pojo.printScheme.PrintFunc.SummaryBuilder;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class OperatePrintFuncAction extends DispatchAction{

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String printFuncId = request.getParameter("printFuncId");
			PrintFuncDao.removeFunc(staff, Integer.parseInt(printFuncId));
			
			jobject.initTip(true, "操作成功, 已删除方案");
			
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String pin = (String)request.getAttribute("pin");
			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String repeat = request.getParameter("repeat");
			String kitchens = request.getParameter("kitchens");
			String dept = request.getParameter("dept");
			String regions = request.getParameter("regions");
			int printerId = -1;
			if(request.getParameter("printerId") == null){
				String printerName = request.getParameter("printerName");
				printerId = PrinterDao.getPrinterIdByName(staff, printerName);
			}else{
				printerId = Integer.parseInt(request.getParameter("printerId"));
			}
			 
			int pType = Integer.parseInt(request.getParameter("pType"));
			
			Department de = null;
			if(!dept.trim().isEmpty()){
				de = DepartmentDao.getDepartmentById(staff, Integer.parseInt(dept));
			}
			
			
			String[] kitchen = null ,region = null;
			if(!kitchens.trim().isEmpty()){
				kitchen = kitchens.split(",");
			}else{
				kitchen = new String[0];
			}
			
			if(!regions.trim().isEmpty()){
				region = regions.split(",");
			}else{
				region = new String[0];
			}
			
			if(PType.valueOf(pType) == PType.PRINT_ORDER){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newPrintOrder();
				for (String r : region) {
					summaryBuilder.addRegion(new Region(Short.parseShort(r)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				summaryBuilder.setDepartment(de);
				PrintFuncDao.addFunc(dbCon, staff, printerId, summaryBuilder);
			}else if(PType.valueOf(pType) == PType.PRINT_ALL_CANCELLED_FOOD){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newAllCancelledFood();
				for (String r : region) {
					summaryBuilder.addRegion(new Region(Short.parseShort(r)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				summaryBuilder.setDepartment(de);
				PrintFuncDao.addFunc(dbCon, staff, printerId, summaryBuilder);
			}else if(PType.valueOf(pType) == PType.PRINT_ORDER_DETAIL){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newPrintFoodDetail();
				for (String k : kitchen) {
					Kitchen ki = new Kitchen();
					ki.setAliasId(Short.parseShort(k));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, detailBuilder);
			}else if(PType.valueOf(pType) == PType.PRINT_CANCELLED_FOOD){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newCancelledFood();
				for (String k : kitchen) {
					Kitchen ki = new Kitchen();
					ki.setAliasId(Short.parseShort(k));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, detailBuilder);
			}else if(PType.valueOf(pType) == PType.PRINT_RECEIPT){
				PrintFunc.Builder builder = Builder.newReceipt();
				for (String r : region) {
					builder.addRegion(new Region(Short.parseShort(r)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
			}else if(PType.valueOf(pType) ==PType.PRINT_TEMP_RECEIPT){
				PrintFunc.Builder builder = Builder.newTempReceipt();
				for (String r : region) {
					builder.addRegion(new Region(Short.parseShort(r)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
			}else if(PType.valueOf(pType) ==PType.PRINT_TRANSFER_TABLE){
				PrintFunc.Builder builder = Builder.newTransferTable();
				for (String r : region) {
					builder.addRegion(new Region(Short.parseShort(r)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
			}else if(PType.valueOf(pType) ==PType.PRINT_ALL_HURRIED_FOOD){
				PrintFunc.Builder builder = Builder.newAllHurriedFood();
				for (String r : region) {
					builder.addRegion(new Region(Short.parseShort(r)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
			}
		
			jobject.initTip(true, "操作成功, 已添加方案");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String pin = (String)request.getAttribute("pin");
			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String repeat = request.getParameter("repeat");
			String kitchens = request.getParameter("kitchens");
			String dept = request.getParameter("dept");
			String regions = request.getParameter("regions");
			int printerId = Integer.parseInt(request.getParameter("printerId"));
			int funcId = Integer.parseInt(request.getParameter("funcId"));
			int pType = Integer.parseInt(request.getParameter("pType"));
			
			Department de = null;
			if(!dept.trim().isEmpty()){
				de = DepartmentDao.getDepartmentById(staff, Integer.parseInt(dept));
			}
			
			
			String[] kitchen = null,region = null;
			if(!kitchens.trim().isEmpty()){
				kitchen = kitchens.split(",");
			}else{
				kitchen = new String[0];
			}
			
			if(!regions.trim().isEmpty()){
				region = regions.split(",");
			}else{
				region = new String[0];
			}
			
			if(PType.valueOf(pType) == PType.PRINT_ORDER){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newPrintOrder();
				for (String r : region) {
					summaryBuilder.addRegion(new Region(Short.parseShort(r)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				summaryBuilder.setDepartment(de);
				PrintFuncDao.updateFunc(dbCon, staff, printerId, summaryBuilder.build(), funcId);
			}else if(PType.valueOf(pType) == PType.PRINT_ALL_CANCELLED_FOOD){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newAllCancelledFood();
				for (String r : region) {
					summaryBuilder.addRegion(new Region(Short.parseShort(r)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				summaryBuilder.setDepartment(de);
				PrintFuncDao.updateFunc(dbCon, staff, printerId, summaryBuilder.build(), funcId);
			}else if(PType.valueOf(pType) == PType.PRINT_ORDER_DETAIL){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newPrintFoodDetail();
				for (String k : kitchen) {
					Kitchen ki = new Kitchen();
					ki.setAliasId(Short.parseShort(k));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, printerId, detailBuilder.build(), funcId);
			}else if(PType.valueOf(pType) == PType.PRINT_CANCELLED_FOOD){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newCancelledFood();
				for (String k : kitchen) {
					Kitchen ki = new Kitchen();
					ki.setAliasId(Short.parseShort(k));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, printerId, detailBuilder.build(), funcId);
			}else if(PType.valueOf(pType) == PType.PRINT_RECEIPT){
				PrintFunc.Builder builder = Builder.newReceipt();
				for (String r : region) {
					builder.addRegion(new Region(Short.parseShort(r)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, printerId, builder.build(), funcId);
			}else if(PType.valueOf(pType) ==PType.PRINT_TEMP_RECEIPT){
				PrintFunc.Builder builder = Builder.newTempReceipt();
				for (String r : region) {
					builder.addRegion(new Region(Short.parseShort(r)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, printerId, builder.build(), funcId);
			}else if(PType.valueOf(pType) ==PType.PRINT_TRANSFER_TABLE){
				PrintFunc.Builder builder = Builder.newTransferTable();
				for (String r : region) {
					builder.addRegion(new Region(Short.parseShort(r)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, printerId, builder.build(), funcId);
			}else if(PType.valueOf(pType) ==PType.PRINT_ALL_HURRIED_FOOD){
				PrintFunc.Builder builder = Builder.newAllHurriedFood();
				for (String r : region) {
					builder.addRegion(new Region(Short.parseShort(r)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, printerId, builder.build(), funcId);
			}
		
			jobject.initTip(true, "操作成功, 已修改方案");
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrCode().getCode(), e.getDesc());
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	
}
