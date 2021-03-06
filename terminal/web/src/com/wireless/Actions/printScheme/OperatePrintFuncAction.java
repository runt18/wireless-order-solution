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
import com.wireless.db.printScheme.PrintFuncDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.printScheme.PrintFunc.Builder;
import com.wireless.pojo.printScheme.PrintFunc.DetailBuilder;
import com.wireless.pojo.printScheme.PrintFunc.SummaryBuilder;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;

public class OperatePrintFuncAction extends DispatchAction{

	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String pin = (String)request.getAttribute("pin");
		final String printFuncId = request.getParameter("printFuncId");
		
		final JObject jObject = new JObject();
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			PrintFuncDao.deleteById(staff, Integer.parseInt(printFuncId));
			
			jObject.initTip(true, "操作成功, 已删除方案");
			
		}catch(BusinessException | SQLException e){
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
	
	public ActionForward insert(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String pin = (String)request.getAttribute("pin");

		final int printerId = Integer.parseInt(request.getParameter("printerId"));
		final int printType = Integer.parseInt(request.getParameter("pType"));
		
		final String repeat = request.getParameter("repeat");
		final String kitchenParam = request.getParameter("kitchens");
		final String deptParam = request.getParameter("dept");
		final String regionParam = request.getParameter("regions");
		final String comment = request.getParameter("comment");
		final String isNeedToAdd = request.getParameter("isNeedToAdd");
		final String isNeedToCancel = request.getParameter("isNeedToCancel");
		final String displayTotalPrice = request.getParameter("displayTotalPrice");
		final String qrCodeType = request.getParameter("qrCodeType");
		final String qrCodeContent = request.getParameter("qrCodeContent");
		
		final JObject jObject = new JObject();
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final String[] kitchens;
			if(kitchenParam != null && !kitchenParam.trim().isEmpty()){
				kitchens = kitchenParam.split(",");
			}else{
				kitchens = new String[0];
			}
			
			final String[] regions;
			if(regionParam != null && !regionParam.trim().isEmpty()){
				regions = regionParam.split(",");
			}else{
				regions = new String[0];
			}
			
			final String[] depts;
			if(deptParam != null && !deptParam.trim().isEmpty()){
				depts = deptParam.split(",");
			}else{
				depts = new String[0];
			}
			
			if(PType.valueOf(printType) == PType.PRINT_ORDER || PType.valueOf(printType) == PType.PRINT_ALL_CANCELLED_FOOD){
				PrintFunc.SummaryBuilder summaryBuilder = new SummaryBuilder(printerId, PType.valueOf(printType));
				for (String region : regions) {
					summaryBuilder.addRegion(new Region(Short.parseShort(region)));
				}
				for (String department : depts) {
					summaryBuilder.addDepartment(new Department(Short.parseShort(department)));
				}
				
				//单尾结束语
				if(comment != null && !comment.isEmpty()){
					summaryBuilder.setComment(comment);
				}
				
				summaryBuilder.setRepeat(Integer.parseInt(repeat)).setDisplayTotal(Boolean.parseBoolean(displayTotalPrice));
				
				
				PrintFuncDao.addFunc(dbCon, staff, summaryBuilder);
				
			}else if(PType.valueOf(printType) == PType.PRINT_ORDER_DETAIL){
				//点菜分单
				PrintFunc.DetailBuilder detailBuilder = new DetailBuilder(printerId, Boolean.parseBoolean(isNeedToAdd), Boolean.parseBoolean(isNeedToCancel));
				for (String kitchenAlias : kitchens) {
					Kitchen ki = new Kitchen(Integer.parseInt(kitchenAlias));
					detailBuilder.addKitchen(ki);
				}
				for (String regionId : regions) {
					detailBuilder.addRegion(new Region(Short.parseShort(regionId)));
				}
				detailBuilder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, detailBuilder);
				
			}else if(PType.valueOf(printType) == PType.PRINT_RECEIPT){
				//结账单
				PrintFunc.Builder builder = Builder.newReceipt(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				
				//单尾结束语
				if(comment != null && !comment.isEmpty()){
					builder.setComment(comment);
				}				
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(printType) == PType.PRINT_TEMP_RECEIPT){
				//TODO 暂结单
				PrintFunc.InsertBuilder4TempReceipt builder = new PrintFunc.InsertBuilder4TempReceipt(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				
				//单尾结束语
				if(comment != null && !comment.isEmpty()){
					builder.setComment(comment);
				}
				
				//暂结单二维码类型
				if(qrCodeType != null && !qrCodeType.isEmpty()){
					final PrintFunc.QrCodeType qrType = PrintFunc.QrCodeType.valueOf(Integer.parseInt(qrCodeType));
					if(qrType.isManual()){
						if(qrCodeContent != null && !qrCodeContent.isEmpty()){
							builder.setManualQrCode(qrCodeContent);
						}
					}else{
						builder.setQrCode(qrType);
					}
				}
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(printType) == PType.PRINT_TRANSFER_TABLE){
				//转台单
				PrintFunc.Builder builder = Builder.newTransferTable(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(printType) == PType.PRINT_ALL_HURRIED_FOOD){
				//催菜单
				PrintFunc.Builder builder = Builder.newAllHurriedFood(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(printType) == PType.PRINT_TRANSFER_FOOD){
				//转菜单
				PrintFunc.Builder builder = Builder.newTransferFood(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(printType) == PType.PRINT_2ND_DISPLAY){
				//客显
				PrintFuncDao.addFunc(dbCon, staff, Builder.new2ndDisplay(printerId));
				
			}else if(PType.valueOf(printType) == PType.PRINT_WX_ORDER){
				//微信订单
				PrintFunc.Builder builder = Builder.newWxOrder(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(printType) == PType.PRINT_BOOK){
				//微信预订
				PrintFuncDao.addFunc(dbCon, staff, Builder.newWxBook(printerId));
				
			}else if(PType.valueOf(printType) == PType.PRINT_WX_WAITER){
				//微信店小二
				PrintFunc.Builder builder = Builder.newWxWaiter(printerId);
				for (String regionId : regions) {
					builder.addRegion(new Region(Short.parseShort(regionId)));
				}
				PrintFuncDao.addFunc(dbCon, staff, builder);
				
			}else if(PType.valueOf(printType) == PType.PRINT_WX_CALL_PAY){
				//微信呼叫结账
				PrintFuncDao.addFunc(dbCon, staff, Builder.newWxCallPay(printerId));
				
			}else{
				throw new BusinessException(PType.valueOf(printType).toString() + "不支持此打印功能");
			}
		
			jObject.initTip(true, "操作成功, 已添加方案");
		}catch(BusinessException | SQLException e){
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
	
	/**
	 * 修改打印功能
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String pin = (String)request.getAttribute("pin");
		final String repeat = request.getParameter("repeat");
		final String kitchens = request.getParameter("kitchens");
		final String depts = request.getParameter("dept");
		final String regions = request.getParameter("regions");
		final String ending = request.getParameter("comment");
		final String isNeedToAdd = request.getParameter("isNeedToAdd");
		final String isNeedToCancel = request.getParameter("isNeedToCancel");
		final String displayTotalPrice = request.getParameter("displayTotalPrice");
		final String qrCodeType = request.getParameter("qrCodeType");
		final String qrCodeContent = request.getParameter("qrCodeContent");
		final JObject jObject = new JObject();
		final DBCon dbCon = new DBCon();
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			dbCon.connect();
			int printerId = Integer.parseInt(request.getParameter("printerId"));
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
			
			final PType printType = PType.valueOf(pType);
			
			if(printType == PType.PRINT_ORDER || printType == PType.PRINT_ALL_CANCELLED_FOOD){
				//点菜总单
				PrintFunc.SummaryUpdateBuilder builder = new PrintFunc.SummaryUpdateBuilder(printerId, printType);
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
				if(ending != null && !ending.isEmpty()){
					builder.setComment(ending);
				}
				
				builder.setRepeat(Integer.parseInt(repeat)).setDisplayTotal(Boolean.parseBoolean(displayTotalPrice));
				PrintFuncDao.updateFunc(dbCon, staff, builder);
				
			}else if(printType == PType.PRINT_ORDER_DETAIL){
				//点菜分单
				PrintFunc.DetailUpdateBuilder builder = new PrintFunc.DetailUpdateBuilder(printerId, Boolean.parseBoolean(isNeedToAdd), Boolean.parseBoolean(isNeedToCancel));
				if(kitchen.length == 0){
					builder.setKitchenAll();
				}else{
					for(String kitchenId : kitchen) {
						builder.addKitchen(new Kitchen(Integer.parseInt(kitchenId)));
					}
				}
				if(region.length == 0){
					builder.setRegionAll();
				}else{
					for (String r : region) {
						builder.addRegion(new Region(Short.parseShort(r)));
					}
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, builder);
				
			}else if(printType == PType.PRINT_TEMP_RECEIPT){
				//结账单
				PrintFunc.UpdateBuilder4TempReceipt builder = new PrintFunc.UpdateBuilder4TempReceipt(printerId);
				if(region.length == 0){
					builder.setRegionAll();
				}else{
					for (String r : region) {
						builder.addRegion(new Region(Short.parseShort(r)));
					}
				}
				builder.setRepeat(Integer.parseInt(repeat));
				
				//单尾结束语
				if(ending != null && !ending.isEmpty()){
					builder.setComment(ending);
				}
				
				//暂结单二维码类型
				if(qrCodeType != null && !qrCodeType.isEmpty()){
					final PrintFunc.QrCodeType qrType = PrintFunc.QrCodeType.valueOf(Integer.parseInt(qrCodeType));
					if(qrType.isManual() && qrCodeContent != null && !qrCodeContent.isEmpty()){
						builder.setManualQrCode(qrCodeContent);
					}else{
						builder.setQrCode(qrType);
					}
				}
				
				PrintFuncDao.updateFunc(dbCon, staff, builder);
				
			}else{
				PrintFunc.UpdateBuilder builder = new PrintFunc.UpdateBuilder(printerId, printType);
				if(region.length == 0){
					builder.setRegionAll();
				}else{
					for (String r : region) {
						builder.addRegion(new Region(Short.parseShort(r)));
					}
				}
				//单尾结束语
				if(ending != null && !ending.isEmpty()){
					builder.setComment(ending);
				}
				builder.setRepeat(Integer.parseInt(repeat));
				PrintFuncDao.updateFunc(dbCon, staff, builder);
			}
		
			jObject.initTip(true, "操作成功, 已修改方案");
		}catch(BusinessException | SQLException e){
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
	
	public ActionForward isEnable(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		JObject jobject = new JObject();
		String pin = (String)request.getAttribute("pin");
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String printerId = request.getParameter("printerId");
			String pType = request.getParameter("pType");
			
			boolean add = false, cancel = false;
			List<PrintFunc> funs;
			if(pType.equals("summary")){
				funs = PrintFuncDao.getByCond(staff, new PrintFuncDao.ExtraCond().setPrinter(Integer.parseInt(printerId)).setType(PType.PRINT_ORDER));
				if(!funs.isEmpty() && funs.get(0).isEnabled()){
					add = true;
				}
				funs = PrintFuncDao.getByCond(staff, new PrintFuncDao.ExtraCond().setPrinter(Integer.parseInt(printerId)).setType(PType.PRINT_ALL_CANCELLED_FOOD));
				if(!funs.isEmpty() && funs.get(0).isEnabled()){
					cancel = true;
				}				
			}else{
				funs = PrintFuncDao.getByCond(staff, new PrintFuncDao.ExtraCond().setPrinter(Integer.parseInt(printerId)).setType(PType.PRINT_ORDER_DETAIL));
				if(!funs.isEmpty() && funs.get(0).isEnabled()){
					add = true;
				}
				funs = PrintFuncDao.getByCond(staff, new PrintFuncDao.ExtraCond().setPrinter(Integer.parseInt(printerId)).setType(PType.PRINT_CANCELLED_FOOD_DETAIL));
				if(!funs.isEmpty() && funs.get(0).isEnabled()){
					cancel = true;
				}	
			}

			final boolean fadd = add, fcancel = cancel;
			
			jobject.setExtra(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putBoolean("add", fadd);
					jm.putBoolean("cancel", fcancel);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});
			
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip4Exception(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
}
