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
			PrintFuncDao.deleteById(staff, Integer.parseInt(printFuncId));
			
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
			String isNeedToCancel = request.getParameter("isNeedToCancel");
			int printerId = -1;
			if(request.getParameter("printerId") == null){
				printerId = PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setName(request.getParameter("printerName"))).get(0).getId();
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
				PrintFunc.SummaryBuilder summaryBuilder = SummaryBuilder.newExtra(printerId, Boolean.parseBoolean(isNeedToCancel));
				for (String region : regions) {
					summaryBuilder.addRegion(new Region(Short.parseShort(region)));
				}
				for (String department : depts) {
					summaryBuilder.addDepartment(new Department(Short.parseShort(department)));
				}
				
				//单尾结束语
				String comment = request.getParameter("comment");
				if(comment != null && !comment.isEmpty()){
					summaryBuilder.setComment(comment);
				}
				
				summaryBuilder.setRepeat(Integer.parseInt(repeat));
				
				PrintFuncDao.addFunc(dbCon, staff, summaryBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_ORDER_DETAIL){
				PrintFunc.DetailBuilder detailBuilder = DetailBuilder.newExtra(printerId, Boolean.parseBoolean(isNeedToCancel));
				for (String kitchenAlias : kitchens) {
					Kitchen ki = new Kitchen(Integer.parseInt(kitchenAlias));
					detailBuilder.addKitchen(ki);
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, detailBuilder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_RECEIPT){
				PrintFunc.Builder builder = Builder.newReceipt(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				
				//单尾结束语
				String comment = request.getParameter("comment");
				if(comment != null && !comment.isEmpty()){
					builder.setComment(comment);
				}				
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(pType) ==PType.PRINT_TEMP_RECEIPT){
				PrintFunc.Builder builder = Builder.newTempReceipt(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				
				//单尾结束语
				String comment = request.getParameter("comment");
				if(comment != null && !comment.isEmpty()){
					builder.setComment(comment);
				}
				
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(pType) ==PType.PRINT_TRANSFER_TABLE){
				PrintFunc.Builder builder = Builder.newTransferTable(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(pType) ==PType.PRINT_ALL_HURRIED_FOOD){
				PrintFunc.Builder builder = Builder.newAllHurriedFood(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, builder);
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
			int funcId = Integer.parseInt(request.getParameter("funcId"));
			int pType = Integer.parseInt(request.getParameter("pType"));
			String isNeedToCancel = request.getParameter("isNeedToCancel");
			
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
				PrintFunc.UpdateBuilder builder = new PrintFunc.UpdateBuilder(funcId);
				if(region.length == 0){
					builder.setRegionAll();
				}else{
					for (String r : region) {
						builder.addRegion(new Region(Short.parseShort(r)));
					}
				}
				
				if(dept.length == 0){
					builder.setDepartmentAll();
				}else{
					for (String department : dept) {
						builder.addDepartment(new Department(Short.parseShort(department)));
					}
				}
				
				//单尾结束语
				String comment = request.getParameter("comment");
				if(comment != null){
					builder.setComment(comment);
				}
				
				builder.setRepeat(Integer.parseInt(repeat));
				builder.setIncludeCancel(Boolean.parseBoolean(isNeedToCancel));
				PrintFuncDao.updateFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(pType) == PType.PRINT_ORDER_DETAIL){
				PrintFunc.UpdateBuilder builder = new PrintFunc.UpdateBuilder(funcId);
				if(kitchen.length == 0){
					builder.setKitchenAll();
				}else{
					for (String k : kitchen) {
						Kitchen ki = new Kitchen(Integer.parseInt(k));
						builder.addKitchen(ki);
					}
				}
				builder.setRepeat(Integer.parseInt(repeat));
				builder.setIncludeCancel(Boolean.parseBoolean(isNeedToCancel));
				PrintFuncDao.updateFunc(dbCon, staff, builder);
			}else if(PType.valueOf(pType) == PType.PRINT_RECEIPT){
				PrintFunc.UpdateBuilder builder = new PrintFunc.UpdateBuilder(funcId);
				if(region.length == 0){
					builder.setRegionAll();
				}else{
					for (String r : region) {
						builder.addRegion(new Region(Short.parseShort(r)));
					}
				}
				builder.setRepeat(Integer.parseInt(repeat));
				//单尾结束语
				String comment = request.getParameter("comment");
				if(comment != null){
					builder.setComment(comment);
				}
				PrintFuncDao.updateFunc(dbCon, staff, builder);
			}else if(PType.valueOf(pType) ==PType.PRINT_TEMP_RECEIPT){
				PrintFunc.UpdateBuilder builder = new PrintFunc.UpdateBuilder(funcId);
				if(region.length == 0){
					builder.setRegionAll();
				}else{
					for (String r : region) {
						builder.addRegion(new Region(Short.parseShort(r)));
					}
				}
				//单尾结束语
				String comment = request.getParameter("comment");
				if(comment != null){
					builder.setComment(comment);
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, builder);
			}else if(PType.valueOf(pType) ==PType.PRINT_TRANSFER_TABLE){
				PrintFunc.UpdateBuilder builder = new PrintFunc.UpdateBuilder(funcId);
				if(region.length == 0){
					builder.setRegionAll();
				}else{
					for (String r : region) {
						builder.addRegion(new Region(Short.parseShort(r)));
					}
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, builder);
			}else if(PType.valueOf(pType) ==PType.PRINT_ALL_HURRIED_FOOD){
				PrintFunc.UpdateBuilder builder = new PrintFunc.UpdateBuilder(funcId);
				if(region.length == 0){
					builder.setRegionAll();
				}else{
					for (String r : region) {
						builder.addRegion(new Region(Short.parseShort(r)));
					}
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, builder);
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
