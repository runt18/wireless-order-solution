package com.wireless.Actions.printScheme;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.DBCon;
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
import com.wireless.pojo.util.WebParams;

public class OperatePrintFuncAction extends DispatchAction{

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
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
	
	//FIXME
	public ActionForward port(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		DBCon dbCon = new DBCon();
		
		int pType = PType.PRINT_UNKNOWN.getVal();
		String printerName = "";
		
		try{
			dbCon.connect();
			
			String account = request.getParameter("account");
			printerName = request.getParameter("printerName");
			String repeat = request.getParameter("repeat");
			String kitchenIds = request.getParameter("kitchens");
			String deptId = request.getParameter("dept");
			String regionIds = request.getParameter("regions");
			pType = Integer.parseInt(request.getParameter("pType"));

			int staffId = 0;
			int restaurantId = 0;
			String sql;
			sql = " SELECT STAFF.restaurant_id, STAFF.staff_id FROM " + 
				  " restaurant REST JOIN staff STAFF ON REST.id = STAFF.restaurant_id " +
				  " WHERE REST.account = '" + account + "'";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				restaurantId = dbCon.rs.getInt("restaurant_id");
				staffId = dbCon.rs.getInt("staff_id");
			}
			dbCon.rs.close();
			
			Staff staff = StaffDao.verify(staffId);
			
			int printerId = 0;
			sql = " SELECT printer_id FROM printer WHERE " +
				  " name = '" + printerName + "'" +
				  " AND " +
				  " restaurant_id = " + restaurantId;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				printerId = dbCon.rs.getInt("printer_id");
			}
			dbCon.rs.close();
			
			
			String[] depts = null, kitchens = null ,regions = null;
			
			if(!deptId.trim().isEmpty()){
				depts = deptId.split(",");
			}else{
				depts = new String[0];
			}

			if(!kitchenIds.trim().isEmpty()){
				kitchens = kitchenIds.split(",");
			}else{
				kitchens = new String[0];
			}
			
			if(!regionIds.trim().isEmpty()){
				regions = regionIds.split(",");
			}else{
				regions = new String[0];
			}
			
			if(PType.valueOf(pType) == PType.PRINT_ORDER){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newPrintOrder();
				for (String region : regions) {
					summaryBuilder.addRegion(new Region(Short.parseShort(region)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				for(String dept : depts){
					summaryBuilder.addDepartment(new Department(restaurantId, Short.parseShort(dept), null));
				}
				PrintFuncDao.addFunc(dbCon, staff, printerId, summaryBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_ALL_CANCELLED_FOOD){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newAllCancelledFood();
				for (String region : regions) {
					summaryBuilder.addRegion(new Region(Short.parseShort(region)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				for(String dept : depts){
					summaryBuilder.addDepartment(new Department(restaurantId, Short.parseShort(dept), null));
				}
				PrintFuncDao.addFunc(dbCon, staff, printerId, summaryBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_ORDER_DETAIL){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newPrintFoodDetail();
				for (String kitchenAlias : kitchens) {
					Kitchen ki = new Kitchen(Integer.parseInt(kitchenAlias));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, detailBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_CANCELLED_FOOD_DETAIL){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newCancelledFood();
				for (String kitchenAlias : kitchens) {
					Kitchen ki = new Kitchen(Integer.parseInt(kitchenAlias));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, detailBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_RECEIPT){
				PrintFunc.Builder builder = Builder.newReceipt();
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
				
			}else if(PType.valueOf(pType) ==PType.PRINT_TEMP_RECEIPT){
				PrintFunc.Builder builder = Builder.newTempReceipt();
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
				
			}else if(PType.valueOf(pType) ==PType.PRINT_TRANSFER_TABLE){
				PrintFunc.Builder builder = Builder.newTransferTable();
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
				
			}else if(PType.valueOf(pType) ==PType.PRINT_ALL_HURRIED_FOOD){
				PrintFunc.Builder builder = Builder.newAllHurriedFood();
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
			}
		
			response.getWriter().print("Port print scheme '" + printerName + " - " + PType.valueOf(pType).getVal() + "' successfully...");
			
		}catch(BusinessException e){
			response.getWriter().print("Port print scheme '" + printerName + " - " + PType.valueOf(pType).getVal() + "' fail...");
			e.printStackTrace();
		}catch(Exception e){
			response.getWriter().print("Port print scheme '" + printerName + " - " + PType.valueOf(pType).getVal() + "' fail...");
			e.printStackTrace();
		}finally{
			dbCon.disconnect();
		}
		
		return null;
	}
	
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String pin = (String)request.getAttribute("pin");
			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String repeat = request.getParameter("repeat");
			String kitchenIds = request.getParameter("kitchens");
			String deptId = request.getParameter("dept");
			String regionIds = request.getParameter("regions");
			int printerId = -1;
			if(request.getParameter("printerId") == null){
				String printerName = request.getParameter("printerName");
				printerId = PrinterDao.getPrinterIdByName(staff, printerName);
			}else{
				printerId = Integer.parseInt(request.getParameter("printerId"));
			}
			 
			int pType = Integer.parseInt(request.getParameter("pType"));
			
			
			String[] kitchens = null, regions = null, depts = null;
			if(!kitchenIds.trim().isEmpty()){
				kitchens = kitchenIds.split(",");
			}else{
				kitchens = new String[0];
			}
			
			if(!regionIds.trim().isEmpty()){
				regions = regionIds.split(",");
			}else{
				regions = new String[0];
			}
			
			if(!deptId.trim().isEmpty()){
				depts = deptId.split(",");
			}else{
				depts = new String[0];
			}
			
			if(PType.valueOf(pType) == PType.PRINT_ORDER){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newPrintOrder();
				for (String region : regions) {
					summaryBuilder.addRegion(new Region(Short.parseShort(region)));
				}
				for (String department : depts) {
					summaryBuilder.addDepartment(new Department(Short.parseShort(department)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				
				PrintFuncDao.addFunc(dbCon, staff, printerId, summaryBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_ALL_CANCELLED_FOOD){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newAllCancelledFood();
				for (String region : regions) {
					summaryBuilder.addRegion(new Region(Short.parseShort(region)));
				}
				for (String department : depts) {
					summaryBuilder.addDepartment(new Department(Short.parseShort(department)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, summaryBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_ORDER_DETAIL){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newPrintFoodDetail();
				for (String kitchenAlias : kitchens) {
					Kitchen ki = new Kitchen(Integer.parseInt(kitchenAlias));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, detailBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_CANCELLED_FOOD_DETAIL){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newCancelledFood();
				for (String kitchenAlias : kitchens) {
					Kitchen ki = new Kitchen(Integer.parseInt(kitchenAlias));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, detailBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_RECEIPT){
				PrintFunc.Builder builder = Builder.newReceipt();
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
				
			}else if(PType.valueOf(pType) ==PType.PRINT_TEMP_RECEIPT){
				PrintFunc.Builder builder = Builder.newTempReceipt();
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
				
			}else if(PType.valueOf(pType) ==PType.PRINT_TRANSFER_TABLE){
				PrintFunc.Builder builder = Builder.newTransferTable();
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, printerId, builder);
				
			}else if(PType.valueOf(pType) ==PType.PRINT_ALL_HURRIED_FOOD){
				PrintFunc.Builder builder = Builder.newAllHurriedFood();
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
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
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			String pin = (String)request.getAttribute("pin");
			dbCon.connect();
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String repeat = request.getParameter("repeat");
			String kitchens = request.getParameter("kitchens");
			String depts = request.getParameter("dept");
			String regions = request.getParameter("regions");
			int printerId = Integer.parseInt(request.getParameter("printerId"));
			int funcId = Integer.parseInt(request.getParameter("funcId"));
			int pType = Integer.parseInt(request.getParameter("pType"));
			
			String[] kitchen = null, region = null, dept = null;
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
			
			if(!depts.trim().isEmpty()){
				dept = depts.split(",");
			}else{
				dept = new String[0];
			}
			
			if(PType.valueOf(pType) == PType.PRINT_ORDER){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newPrintOrder();
				for (String r : region) {
					summaryBuilder.addRegion(new Region(Short.parseShort(r)));
				}
				for (String department : dept) {
					summaryBuilder.addDepartment(new Department(Short.parseShort(department)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, printerId, summaryBuilder.build(), funcId);
			}else if(PType.valueOf(pType) == PType.PRINT_ALL_CANCELLED_FOOD){
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newAllCancelledFood();
				for (String r : region) {
					summaryBuilder.addRegion(new Region(Short.parseShort(r)));
				}
				for (String department : dept) {
					summaryBuilder.addDepartment(new Department(Short.parseShort(department)));
				}
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, printerId, summaryBuilder.build(), funcId);
			}else if(PType.valueOf(pType) == PType.PRINT_ORDER_DETAIL){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newPrintFoodDetail();
				for (String k : kitchen) {
					Kitchen ki = new Kitchen(Integer.parseInt(k));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, printerId, detailBuilder.build(), funcId);
			}else if(PType.valueOf(pType) == PType.PRINT_CANCELLED_FOOD_DETAIL){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newCancelledFood();
				for (String k : kitchen) {
					Kitchen ki = new Kitchen(Integer.parseInt(k));
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
			dbCon.disconnect();
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	
}
