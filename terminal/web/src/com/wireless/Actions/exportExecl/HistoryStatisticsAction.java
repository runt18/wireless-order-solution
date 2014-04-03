package com.wireless.Actions.exportExecl;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.billStatistics.CancelledFoodDao;
import com.wireless.db.billStatistics.SaleDetailsDao;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.CommissionStatistics;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.RepaidStatistics;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.dishesOrder.CancelledFood;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.Status;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.pojo.system.DailySettle;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.util.DateType;
import com.wireless.util.SQLUtil;

@SuppressWarnings("deprecation")
public class HistoryStatisticsAction extends DispatchAction{
	
	private HSSFCellStyle headerStyle = null, headerDetailStyle = null, titleStyle = null, strStyle = null, numStyle = null, normalNumStyle = null;
	private HSSFFont headerFont = null, titleFont = null;
	private HSSFDataFormat doubleForamt = null;
	
	/**
	 * 初始化execl工作区参数
	 * @param wb
	 */
	private void initParams(HSSFWorkbook wb){
		headerStyle = wb.createCellStyle();
		headerStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		headerFont = wb.createFont();
		headerFont.setFontHeight((short) 220);
		headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		
		headerStyle.setBorderTop((short)1);
		headerStyle.setBorderBottom((short)1);
		headerStyle.setBorderLeft((short)1);
		headerStyle.setBorderRight((short)1);
		
		titleStyle = wb.createCellStyle();
		titleStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		titleStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		titleFont = wb.createFont();
		titleFont.setFontHeight((short) 350);	
		titleFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		titleStyle.setFont(titleFont);
		
		strStyle = wb.createCellStyle();
		strStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		strStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		strStyle.setBorderTop((short)1);
		strStyle.setBorderBottom((short)1);
		strStyle.setBorderLeft((short)1);
		strStyle.setBorderRight((short)1);
		

		headerDetailStyle = wb.createCellStyle();
		headerDetailStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		headerDetailStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);

		
		doubleForamt = wb.createDataFormat();
		numStyle = wb.createCellStyle();
		numStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		numStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		numStyle.setDataFormat(doubleForamt.getFormat("0.00"));
		numStyle.setBorderTop((short)1);
		numStyle.setBorderBottom((short)1);
		numStyle.setBorderLeft((short)1);
		numStyle.setBorderRight((short)1);
		
		normalNumStyle = wb.createCellStyle();
		normalNumStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		normalNumStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		normalNumStyle.setBorderTop((short)1);
		normalNumStyle.setBorderBottom((short)1);
		normalNumStyle.setBorderLeft((short)1);
		normalNumStyle.setBorderRight((short)1);
		
	}
	
	/**
	 * 菜品销售统计
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward salesFoodDetail(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("菜品销售统计(历史).xls".getBytes("GBK"), "ISO8859_1"));
		
		String pin = (String)request.getAttribute("pin");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		String deptID = request.getParameter("deptID");
		String foodName = new String(request.getParameter("foodName").getBytes("ISO8859_1"), "UTF-8");
		
		int[] did = null;
		if(deptID != null && deptID.length() > 0){
			String[] splitDeptID = deptID.split(",");
			did = new int[splitDeptID.length];
			for(int i = 0; i < splitDeptID.length; i++){
				did[i] = Integer.parseInt(splitDeptID[i]);
			}
			if(did.length == 1 && did[0] == -1){
				did = new int[0];
			}
		}
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		SalesDetail[] saleDetails = SaleDetailsDao.execByFood(
				staff, 
				onDuty, 
				offDuty,
				did,
				SaleDetailsDao.ORDER_BY_SALES,
				DateType.HISTORY,
				foodName);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("菜品销售统计(历史)");
		HSSFRow row = null;
		HSSFCell cell = null;
		// ******
		initParams(wb);
		
		sheet.setColumnWidth(0, 2000);
		sheet.setColumnWidth(1, 8000);
		sheet.setColumnWidth(2, 2000);
		sheet.setColumnWidth(3, 3500);
		sheet.setColumnWidth(4, 3500);
		sheet.setColumnWidth(5, 3500);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		sheet.setColumnWidth(8, 3000);
		sheet.setColumnWidth(9, 3000);
		sheet.setColumnWidth(10, 3000);
		sheet.setColumnWidth(11, 3000);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 11));
		// 冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("菜品销售统计(历史)");
		cell.setCellStyle(titleStyle);
		
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty);
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 11));
		
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +staff.getName());
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 11));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 11));
		
		// 列表头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
//		style.setFillForegroundColor((short)0xCCC);
//		style.setFillBackgroundColor((short)0xCCC);
		
		cell = row.createCell(0);
		cell.setCellValue("编号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("销量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("营业额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("折扣额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("赠送额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("成本");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("成本率");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("毛利");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("毛利率");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("均价");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("单位成本");
		cell.setCellStyle(headerStyle);
		
		if(saleDetails != null && saleDetails.length > 0){
			for(SalesDetail item : saleDetails){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				// ***
				cell = row.createCell(0);
				cell.setCellValue(item.getFood().getAliasId());
				cell.setCellStyle(strStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getFood().getName());
				cell.setCellStyle(strStyle);
				
				// ***
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getSalesAmount());
				
				// ***
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncome());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getDiscount());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getGifted());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getCost());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getCostRate());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getProfit());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getProfitRate());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getAvgPrice());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getAvgCost());
				cell.setCellStyle(numStyle);
			}
		}
		
        OutputStream os = response.getOutputStream();
        wb.write(os);
        os.flush();
        os.close();
        
		return null;
	}
	
	/**
	 * 分厨销售统计
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward salesByKitchen(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String(("分厨銷售统计(" + DateType.HISTORY.getDesc() + ").xls").getBytes("GBK"), "ISO8859_1"));
		
		String pin = (String)request.getAttribute("pin");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		SalesDetail[] list = SaleDetailsDao.execByKitchen(staff, onDuty, offDuty, DateType.HISTORY);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("分厨销售统计(" + DateType.HISTORY.getDesc() + ")");
		HSSFRow row = null;
		HSSFCell cell = null;
		// 初始化参数,重要
		initParams(wb);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 3500);
		sheet.setColumnWidth(3, 3500);
		sheet.setColumnWidth(4, 3500);
		sheet.setColumnWidth(5, 3500);
		sheet.setColumnWidth(6, 3500);
		sheet.setColumnWidth(7, 3500);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("分厨销售统计(" + DateType.HISTORY.getDesc() + ")");
		cell.setCellStyle(titleStyle);
				
		// *****
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty);
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
				
		// *****
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +staff.getName());
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		// *****
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("分厨");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("营业额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("折扣额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("赠送额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("成本");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("成本率");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("毛利");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("毛利率");
		cell.setCellStyle(headerStyle);
		
		if(list != null && list.length > 0){
			SalesDetail temp = null, sum = new SalesDetail();
			Kitchen kitchen = new Kitchen(-1);
			kitchen.setName("汇总");
			sum.setKitchen(kitchen);
			for(int i = 0; i <= list.length; i++){
				if(i == list.length){
					temp = sum;
				}else{
					temp = list[i];					
					sum.setIncome(sum.getIncome() + temp.getIncome());
					sum.setDiscount(sum.getDiscount() + temp.getDiscount());
					sum.setGifted(sum.getGifted() + temp.getGifted());
					sum.setCost(sum.getCost() + temp.getCost());
					sum.setProfit(sum.getProfit() + temp.getProfit());
				}
				
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				cell = row.createCell(0);
				cell.setCellValue(temp.getKitchen().getName());
				cell.setCellStyle(strStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getIncome());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getDiscount());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getGifted());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getCost());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getCostRate());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getProfit());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getProfitRate());
				cell.setCellStyle(numStyle);
			}
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close(); 
		
		return null;
	}
	
	/**
	 * 部门销售统计
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward salesByDept(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("部门销售统计(历史).xls".getBytes("GBK"), "ISO8859_1"));
		
		String pin = (String)request.getAttribute("pin");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		SalesDetail[] list = SaleDetailsDao.execByDept(staff, onDuty, offDuty, DateType.HISTORY);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("部门销售统计(历史)");
		HSSFRow row = null;
		HSSFCell cell = null;
		// 初始化参数,重要
		initParams(wb);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 3500);
		sheet.setColumnWidth(3, 3500);
		sheet.setColumnWidth(4, 3500);
		sheet.setColumnWidth(5, 3500);
		sheet.setColumnWidth(6, 3500);
		sheet.setColumnWidth(7, 3500);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("部门销售统计(历史)");
		cell.setCellStyle(titleStyle);
				
		// *****
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty);
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
				
		// *****
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +staff.getName());
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		// *****
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("部门");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("营业额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("折扣额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("赠送额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("成本");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("成本率");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("毛利");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("毛利率");
		cell.setCellStyle(headerStyle);
		
		if(list != null && list.length > 0){
			SalesDetail temp = null, sum = new SalesDetail();
			sum.setDept(new Department(0, (short)0, "汇总"));
			for(int i = 0; i <= list.length; i++){
				if(i == list.length){
					temp = sum;
				}else{
					temp = list[i];					
					sum.setIncome(sum.getIncome() + temp.getIncome());
					sum.setDiscount(sum.getDiscount() + temp.getDiscount());
					sum.setGifted(sum.getGifted() + temp.getGifted());
					sum.setCost(sum.getCost() + temp.getCost());
					sum.setProfit(sum.getProfit() + temp.getProfit());
				}
				
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				cell = row.createCell(0);
				cell.setCellValue(temp.getDept().getName());
				cell.setCellStyle(strStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getIncome());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getDiscount());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getGifted());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getCost());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getCostRate());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getProfit());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(temp.getProfitRate());
				cell.setCellStyle(numStyle);
			}
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close(); 
		
		return null;
	}
	
	/**
	 * 收款明细
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward businessReceips(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("收款明细(历史).xls".getBytes("GBK"), "ISO8859_1"));
		
		String pin = (String)request.getAttribute("pin");
		String restaurantID = request.getParameter("restaurantID");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pin", pin);
		params.put("restaurantID", restaurantID);
		params.put("onDuty", onDuty);
		params.put("offDuty", offDuty);
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		List<IncomeByEachDay> incomesByEachDay = new ArrayList<IncomeByEachDay>();
		incomesByEachDay.addAll(CalcBillStatisticsDao.calcIncomeByEachDay(StaffDao.verify(Integer.parseInt(pin)), onDuty, offDuty));
		
		
//		BusinessStatistics sum = new BusinessStatistics();
		
		// 创建execl主页
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("收款明细(历史)");
		HSSFRow row = null;
		HSSFCell cell = null;
		
		// *****
		initParams(wb);
		
		sheet.setColumnWidth(0, 3500);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 3500);
		sheet.setColumnWidth(3, 3500);
		sheet.setColumnWidth(4, 3500);
		sheet.setColumnWidth(5, 3500);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		sheet.setColumnWidth(8, 3000);
		sheet.setColumnWidth(9, 3000);
		sheet.setColumnWidth(10, 3000);
		sheet.setColumnWidth(11, 3000);
		sheet.setColumnWidth(12, 3000);
		sheet.setColumnWidth(13, 3000);
		sheet.setColumnWidth(14, 3000);
		sheet.setColumnWidth(15, 3000);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 16));
		// 冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("收款明细(历史)");
		cell.setCellStyle(titleStyle);
		
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 16));
		
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +staff.getName());
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 16));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 16));
		
		// 列表头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		
		cell = row.createCell(0);
		cell.setCellValue("日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("应收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("实收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账单数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("现金");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("刷卡");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("会员");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("挂账");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("签单");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("折扣");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("赠送");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("退菜");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("抹数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("反结账");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("优惠劵");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("会员充值");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("会员退款");
		cell.setCellStyle(headerStyle);
		
		if(incomesByEachDay != null && incomesByEachDay.size() > 0){
			for(IncomeByEachDay item : incomesByEachDay){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				// ***
				cell = row.createCell(0);
				cell.setCellValue(item.getDate());
				cell.setCellStyle(strStyle);
				
				// ***
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByPay().getTotalIncome());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByPay().getTotalActual());
				cell.setCellStyle(numStyle);
				
				HSSFCellStyle ts = wb.createCellStyle();
				ts.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				ts.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				ts.setDataFormat(wb.createDataFormat().getFormat("0"));
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getTotalAmount());
				cell.setCellStyle(ts);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByPay().getCashActual());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByPay().getCreditCardActual());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByPay().getMemberCardActual());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByPay().getHangActual());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByPay().getSignActual());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByDiscount().getTotalDiscount());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByGift().getTotalGift());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByCancel().getTotalCancel());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByErase().getTotalErase());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByRepaid().getTotalRepaid());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByCoupon().getTotalCoupon());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByCharge().getTotalActualCharge());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncomeByCharge().getTotalActualRefund());
				cell.setCellStyle(numStyle);
				
//				sum.setCashAmount(sum.getCashAmount() + item.getCashAmount());
//				sum.setCashIncome2(sum.getCashIncome2() + item.getCashIncome2());
//				sum.setCreditCardAmount(sum.getCreditCardAmount() + item.getCreditCardAmount());
//				sum.setCreditCardIncome2(sum.getCreditCardIncome2() + item.getCreditCardIncome2());
//				sum.setSignAmount(sum.getSignAmount() + item.getSignAmount());
//				sum.setSignIncome2(sum.getSignIncome2() + item.getSignIncome2());
//				sum.setHangAmount(sum.getHangAmount() + item.getHangAmount());
//				sum.setHangIncome2(sum.getHangIncome2() + item.getHangIncome2());
//				
//				sum.setEraseAmount(sum.getEraseAmount() + item.getEraseAmount());
//				sum.setEraseIncome(sum.getEraseIncome() + item.getEraseIncome());
//				sum.setDiscountAmount(sum.getDiscountAmount() + item.getDiscountAmount());
//				sum.setDiscountIncome(sum.getDiscountIncome() + item.getDiscountIncome());
//				sum.setGiftAmount(sum.getGiftAmount() + item.getGiftAmount());
//				sum.setGiftIncome(sum.getGiftIncome() + item.getGiftIncome());
//				sum.setCancelAmount(sum.getCancelAmount() + item.getCancelAmount());
//				sum.setCancelIncome(sum.getCancelIncome() + item.getCancelIncome());
//				sum.setPaidIncome(sum.getPaidIncome() + item.getPaidIncome());
//				
//				sum.setTotalPrice(sum.getTotalPrice() + item.getTotalPrice());
//				sum.setTotalPrice2(sum.getTotalPrice2() + item.getTotalPrice2());
//				
//				sum.setOrderAmount(sum.getOrderAmount() + item.getOrderAmount());
			}
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close(); 
				
		return null;
	}
	
	/**
	 * 营业汇总
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward business(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		//String restaurantID = request.getParameter("restaurantID");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		//String queryPattern = request.getParameter("queryPattern");
		String dataType = request.getParameter("dataType");
		
		DateType dt = DateType.HISTORY;
		if(dataType == null || dt == null){
			return null;
		}
		
		response.addHeader("Content-Disposition","attachment;filename=" + new String(("营业汇总(" + dt.getDesc() + ").xls").getBytes("GBK"), "ISO8859_1"));
		
/*		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(dt, dt.getValue());
		params.put("pin", pin);
		params.put("restaurantID", restaurantID);
		params.put("onDuty", onDuty);
		params.put("offDuty", offDuty);
		params.put("queryPattern", queryPattern);*/
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		ShiftDetail business = ShiftDao.getByRange(staff, new DutyRange(onDuty, offDuty), DateType.HISTORY);
		
		// 创建excel主页
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("营业汇总(" + dt.getDesc() + ")");
		HSSFRow row = null;
		HSSFCell cell = null;
		// 初始化参数,重要
		initParams(wb);
		
		//
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 3500);
		sheet.setColumnWidth(3, 4000);
		sheet.setColumnWidth(4, 1000);
		sheet.setColumnWidth(5, 3500);
		sheet.setColumnWidth(6, 3500);
		sheet.setColumnWidth(7, 3500);
		sheet.setColumnWidth(8, 3500);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("营业汇总(" + dt.getDesc() + ")");
		cell.setCellStyle(titleStyle);
		// *****
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty + "     账单总数: " + business.getOrderAmount());
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
				
		// *****
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +staff.getName());
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		
		// ***** 收款方式
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("收款方式");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账单数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("应收总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("实收总额");
		cell.setCellStyle(headerStyle);
		
		// 现金
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("现金");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCancelAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCashTotalIncome());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCashActualIncome());
		cell.setCellStyle(numStyle);
		
		// 刷卡
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("刷卡");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCreditCardAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCreditTotalIncome());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCreditActualIncome());
		cell.setCellStyle(numStyle);
		
		// 会员卡
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("会员卡");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getMemberCardAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getMemberTotalIncome());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getMemberActualIncome());
		cell.setCellStyle(numStyle);
		
		// 签单
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("签单");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getSignAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getSignTotalIncome());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getSignActualIncome());
		cell.setCellStyle(numStyle);
		
		// 挂账
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("挂账");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getHangAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getHangTotalIncome());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getHangActualIncome());
		cell.setCellStyle(numStyle);
		
		// 合计
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("合计");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getOrderAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getTotalIncome());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getTotalActual());
		cell.setCellStyle(numStyle);
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
		// ***** 操作类型
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("操作类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账单数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("金额");
		cell.setCellStyle(headerStyle);
		
		// 抹数
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("抹数");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getEraseAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getEraseIncome());
		cell.setCellStyle(numStyle);
		
		// 折扣
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("折扣");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getDiscountAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getDiscountIncome());
		cell.setCellStyle(numStyle);
		
		// 赠送
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("赠送");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getGiftAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getGiftIncome());
		cell.setCellStyle(numStyle);
		
		// 退菜
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("退菜");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCancelAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCancelIncome());
		cell.setCellStyle(numStyle);
		
		// 反结帐
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("反结帐");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getPaidAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getPaidIncome());
		cell.setCellStyle(numStyle);
		
		// 优惠劵
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("优惠劵");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCouponAmount());
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getCouponIncome());
		cell.setCellStyle(numStyle);
		
		// 服务费收入
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("服务费收入");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("--");
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getServiceIncome());
		cell.setCellStyle(numStyle);
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
		// ***** 会员充值退款
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("会员操作");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("现金");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("刷卡");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账户实充/扣额");
		cell.setCellStyle(headerStyle);
		
		
		// 会员充值
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("会员充值");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getActualCashCharge());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getActualCreditCardCharge());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getTotalAccountCharge());
		cell.setCellStyle(numStyle);
		
		// 会员退款
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("会员退款");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getTotalActualRefund());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(0.00);
		cell.setCellStyle(numStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getTotalAccountRefund());
		cell.setCellStyle(numStyle);
		
		// *****
		row = sheet.getRow(4);
		
		cell = row.createCell(5);
		cell.setCellValue("部门汇总");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("折扣总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("赠送总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("应收总额");
		cell.setCellStyle(headerStyle);
		
		if(business != null && business.getDeptIncome() != null){
			sheet.addMergedRegion(new CellRangeAddress(4, business.getDeptIncome().size() + 4, 4, 4));
			IncomeByDept item = null;
			for(int i = 0; i < business.getDeptIncome().size(); i++){
				 item = business.getDeptIncome().get(i);
				 row = sheet.getRow(i + 5);
				 
				 cell = row.createCell(5);
				 cell.setCellValue(item.getDept().getName());
				 cell.setCellStyle(strStyle);
				 
				 cell = row.createCell(row.getLastCellNum());
				 cell.setCellValue(item.getDiscount());
				 cell.setCellStyle(numStyle);
				 
				 cell = row.createCell(row.getLastCellNum());
				 cell.setCellValue(item.getGift());
				 cell.setCellStyle(numStyle);
				 
				 cell = row.createCell(row.getLastCellNum());
				 cell.setCellValue(item.getIncome());
				 cell.setCellStyle(numStyle);
				 
				 item = null;
			}
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close(); 
		
		return null;
	}
	
	/**
	 * 库单导出
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward stockAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		String id = request.getParameter("id");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		StockAction stockAction = StockActionDao.getStockAndDetailById(staff, Integer.parseInt(id));
		
		String title = stockAction.getType().getDesc() + " -- " + stockAction.getCateType().getText() +  stockAction.getSubType().getText() + "单";
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( (stockAction.getType().getDesc() + "(" + stockAction.getCateType().getText() + stockAction.getSubType().getText() + "单).xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 6000);
		sheet.setColumnWidth(2, 6000);
		sheet.setColumnWidth(3, 6000);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
		
		//冻结行
		sheet.createFreezePane(0, 7, 0, 7);
		
		//报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title + "(" + stockAction.getId() + ")");
		cell.setCellStyle(titleStyle);
		
		//摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		String dept = null;
		if(stockAction.getSubType() == SubType.STOCK_IN){
			dept = "收货仓: " + stockAction.getDeptIn().getName() + "	         供货商: " + stockAction.getSupplier().getName();
		}else if(stockAction.getSubType() == SubType.STOCK_IN_TRANSFER || stockAction.getSubType() == SubType.STOCK_OUT_TRANSFER){
			dept = "收货仓: " + stockAction.getDeptIn().getName() + "	         出货仓: " + stockAction.getDeptOut().getName();
		}else if(stockAction.getSubType() == SubType.STOCK_OUT){
			dept = "供应商: " + stockAction.getSupplier().getName() + "         出货仓: " + stockAction.getDeptOut().getName();
		}else if(stockAction.getSubType() == SubType.SPILL || stockAction.getSubType() == SubType.MORE){
			dept = "收货仓: " + stockAction.getDeptIn().getName();
		}else{
			dept = "出货仓: " + stockAction.getDeptOut().getName();
		}
		cell.setCellValue(dept + "         原始单号: " + stockAction.getOriStockId() + "         货单日期: " + DateUtil.formatToDate(stockAction.getOriStockDate())
				);

		cell.setCellStyle(headerDetailStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
		//审核人
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		if(stockAction.getStatus() == Status.UNAUDIT){
			cell.setCellValue("制单人: " + stockAction.getOperator() + "         制单时间: " + DateUtil.format(stockAction.getBirthDate()));
		}else{
			cell.setCellValue("审核人: " + stockAction.getApprover() + "         审核时间: " + DateUtil.format(stockAction.getApproverDate()) + "         制单人: " + stockAction.getOperator() + "         制单时间: " + DateUtil.format(stockAction.getBirthDate()));
		}
		cell.setCellStyle(headerDetailStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
		//数量, 金额
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("总数量: " + stockAction.getAmount() + "         总金额: " + stockAction.getTotalPrice() + "         实际金额: " + stockAction.getActualPrice());
		cell.setCellStyle(headerDetailStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
		//备注
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("备注: " + stockAction.getComment());
		cell.setCellStyle(headerDetailStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
		//空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
		//列头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("货品名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("单价");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("总价");
		cell.setCellStyle(headerStyle);
		
		if(stockAction.getStockDetails() != null && stockAction.getStockDetails().size() >0){
			for (StockActionDetail item : stockAction.getStockDetails()) {
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				cell = row.createCell(0);
				cell.setCellValue(item.getName());
				cell.setCellStyle(strStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getAmount());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getPrice());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getAmount() * item.getPrice());
				cell.setCellStyle(numStyle);
			}
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
		
	}
	/**
	 * 充值明细
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public ActionForward rechargeDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		//String restaurantID = request.getParameter("restaurantID");
		String dataSource = request.getParameter("dataSources");
		String memberMobile = request.getParameter("memberMobile");
		String memberCard = request.getParameter("memberCard");
		String memberName = request.getParameter("memberName");
		String memberType = request.getParameter("memberType");
		String operateType = request.getParameter("operateType");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		String detailOperate = request.getParameter("detailOperate");
		//String total = request.getParameter("total");
		
		List<MemberOperation> list = null;
		String extraCond = null, orderClause = null;
		extraCond = " AND MO.restaurant_id = " + staff.getRestaurantId();
		
		if(memberMobile != null && !memberMobile.trim().isEmpty()){
			extraCond += (" AND MO.member_mobile like '%" + memberMobile.trim() + "%'");
		}
		if(memberCard != null && !memberCard.trim().isEmpty()){
			extraCond += (" AND MO.member_card like '%" + memberCard.trim() + "%'");
		}
		if(memberName != null && !memberName.trim().isEmpty()){
			extraCond += (" AND MO.member_name like '%" + memberName.trim() + "%'");
		}
		if(memberType != null && !memberType.trim().isEmpty()){
			extraCond += (" AND M.member_type_id = " + memberType);
		}
		if(detailOperate != null && !detailOperate.trim().isEmpty() && Integer.valueOf(detailOperate) > 0){
			extraCond += (" AND MO.operate_type = " + detailOperate);
		}else{
			if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
				List<OperationType> types = OperationType.typeOf(Integer.parseInt(operateType));
				String extra = "";
				for (int i = 0; i < types.size(); i++) {
					if(i == 0){
						extra += " MO.operate_type = " + types.get(i).getValue();
					}else{
						extra += " OR MO.operate_type = " + types.get(i).getValue();
					}
				}
				if(Integer.parseInt(operateType) == OperationType.POINT_ADJUST.getType()){
					extra += " OR MO.operate_type = " + OperationType.CONSUME.getValue();
				}
				extraCond += " AND(" + extra + ")";
			}
		}
		orderClause = " ORDER BY MO.operate_date ";
		
		Map<Object, Object> paramsSet = new HashMap<Object, Object>(), countSet = null;
		countSet = new HashMap<Object, Object>();
		if(dataSource.equalsIgnoreCase("today")){
			countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
		}else if(dataSource.equalsIgnoreCase("history")){
			if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
				extraCond += (" AND MO.operate_date >= '" + onDuty + "'");
				extraCond += (" AND MO.operate_date <= '" + offDuty + "'");
			}
			countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
		}
		paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
		paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
		
		if(dataSource.equalsIgnoreCase("today")){
			list = MemberOperationDao.getToday(staff, paramsSet);
		}else if(dataSource.equalsIgnoreCase("history")){
			list = MemberOperationDao.getHistory(staff, paramsSet);
		}
		MemberOperation sum = MemberOperation.newMO(-10, "", "", "");
		if(list != null && !list.isEmpty()){
			sum.setChargeType(list.get(0).getChargeType());
			sum.setComment(list.get(0).getComment());
			sum.setOperationType(list.get(0).getOperationType());
			sum.setPayType(list.get(0).getPayType());
			sum.setOperateSeq(list.get(0).getOperateSeq());
			sum.setStaffName(list.get(0).getStaffName());
			for(MemberOperation temp : list){
				temp.setMember(MemberDao.getMemberById(staff, temp.getMemberId()));
				
				sum.setDeltaBaseMoney(temp.getDeltaBaseMoney() + sum.getDeltaBaseMoney());
				sum.setDeltaExtraMoney(temp.getDeltaExtraMoney() + sum.getDeltaExtraMoney());
				sum.setChargeMoney(temp.getChargeMoney() + sum.getChargeMoney());
				sum.setPayMoney(temp.getPayMoney() + sum.getPayMoney());
				sum.setDeltaPoint(temp.getDeltaPoint() + sum.getDeltaPoint());
			}
		}
		
		DecimalFormat df = new DecimalFormat("#.00");
		String title = "会员充值明细表";
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("会员充值明细表.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 3800);
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 3300);
		sheet.setColumnWidth(3, 4000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 6000);
		sheet.setColumnWidth(7, 4000);
		
		//冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		//报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		String date;
		if(dataSource.equals("today")){
			date = "当日";
		}else{
			date = onDuty + "  至  " + offDuty;
		}
		cell.setCellValue("统计时间: " + date );
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("总收款金额: " + df.format(sum.getChargeMoney()) + "         总充值额 :" + df.format(sum.getDeltaTotalMoney()));
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		//空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		//列头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("会员名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("会员类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("实收/实退");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("充值/退款");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("收款方式");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("操作时间");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("操作类型");
		cell.setCellStyle(headerStyle);
		
		for (MemberOperation mo : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(mo.getMemberName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(mo.getMember().getMemberType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(df.format(mo.getChargeMoney()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(df.format(mo.getDeltaTotalMoney()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(mo.getChargeType() == null ? "现金" : mo.getChargeType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(mo.getStaffName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(DateUtil.format(mo.getOperateDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(mo.getOperationType().getName());
			cell.setCellStyle(strStyle);
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	/**
	 * 消费明细
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws Exception
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public ActionForward consumeDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		//String restaurantID = request.getParameter("restaurantID");
		String dataSource = request.getParameter("dataSources");
		String memberMobile = request.getParameter("memberMobile");
		String memberCard = request.getParameter("memberCard");
		String memberName = request.getParameter("memberName");
		String memberType = request.getParameter("memberType");
		String operateType = request.getParameter("operateType");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		//String total = request.getParameter("total");
		
		List<MemberOperation> list = null;
		String extraCond = null, orderClause = null;
		extraCond = " AND MO.restaurant_id = " + staff.getRestaurantId();
		
		if(memberMobile != null && !memberMobile.trim().isEmpty()){
			extraCond += (" AND MO.member_mobile like '%" + memberMobile.trim() + "%'");
		}
		if(memberCard != null && !memberCard.trim().isEmpty()){
			extraCond += (" AND MO.member_card like '%" + memberCard.trim() + "%'");
		}
		if(memberName != null && !memberName.trim().isEmpty()){
			extraCond += (" AND MO.member_name like '%" + memberName.trim() + "%'");
		}
		if(memberType != null && !memberType.trim().isEmpty()){
			extraCond += (" AND M.member_type_id = " + memberType);
		}
		if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
			List<OperationType> types = OperationType.typeOf(Integer.parseInt(operateType));
			String extra = "";
			for (int i = 0; i < types.size(); i++) {
				if(i == 0){
					extra += " MO.operate_type = " + types.get(i).getValue();
				}else{
					extra += " OR MO.operate_type = " + types.get(i).getValue();
				}
			}
			if(Integer.parseInt(operateType) == OperationType.POINT_ADJUST.getType()){
				extra += " OR MO.operate_type = " + OperationType.CONSUME.getValue();
			}
			extraCond += " AND(" + extra + ")";
		}
		
		orderClause = " ORDER BY MO.operate_date ";
		
		Map<Object, Object> paramsSet = new HashMap<Object, Object>(), countSet = null;
		countSet = new HashMap<Object, Object>();
		if(dataSource.equalsIgnoreCase("today")){
			countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
		}else if(dataSource.equalsIgnoreCase("history")){
			if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
				extraCond += (" AND MO.operate_date >= '" + onDuty + "'");
				extraCond += (" AND MO.operate_date <= '" + offDuty + "'");
			}
			countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
		}
		paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
		paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
		
		if(dataSource.equalsIgnoreCase("today")){
			list = MemberOperationDao.getToday(staff, paramsSet);
		}else if(dataSource.equalsIgnoreCase("history")){
			list = MemberOperationDao.getHistory(staff, paramsSet);
		}
		MemberOperation sum = MemberOperation.newMO(-10, "", "", "");
		if(list != null && !list.isEmpty()){
			sum.setChargeType(list.get(0).getChargeType());
			sum.setComment(list.get(0).getComment());
			sum.setOperationType(list.get(0).getOperationType());
			sum.setPayType(list.get(0).getPayType());
			sum.setOperateSeq(list.get(0).getOperateSeq());
			sum.setStaffName(list.get(0).getStaffName());
			for(MemberOperation temp : list){
				temp.setMember(MemberDao.getMemberById(staff, temp.getMemberId()));
				
				sum.setDeltaBaseMoney(temp.getDeltaBaseMoney() + sum.getDeltaBaseMoney());
				sum.setDeltaExtraMoney(temp.getDeltaExtraMoney() + sum.getDeltaExtraMoney());
				sum.setChargeMoney(temp.getChargeMoney() + sum.getChargeMoney());
				sum.setPayMoney(temp.getPayMoney() + sum.getPayMoney());
				sum.setDeltaPoint(temp.getDeltaPoint() + sum.getDeltaPoint());
			}
		}
		
		DecimalFormat df = new DecimalFormat("#.00");
		String title = "会员消费明细表";
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("会员消费表明细.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 3800);
		sheet.setColumnWidth(1, 6000);
		sheet.setColumnWidth(2, 3300);
		sheet.setColumnWidth(3, 4000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 6000);
		
		//冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		//报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		String date;
		if(dataSource.equals("today")){
			date = "当日";
		}else{
			date = onDuty + "  至  " + offDuty;
		}
		cell.setCellValue("统计时间: " + date );
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("总金额: " + df.format(sum.getPayMoney()) + "         总积分 :" + sum.getDeltaPoint());
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		//空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		//列头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("单据号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("消费时间");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("会员名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("会员类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("消费金额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("所得积分");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("备注");
		cell.setCellStyle(headerStyle);
		
		for (MemberOperation mo : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(mo.getOrderId());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(DateUtil.format(mo.getOperateDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(mo.getMemberName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(mo.getMember().getMemberType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(df.format(mo.getPayMoney()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(df.format(mo.getDeltaPoint()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(mo.getStaffName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(mo.getComment());
			cell.setCellStyle(strStyle);
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	
	public ActionForward cancelledFood(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		
		
		response.setContentType("application/vnd.ms-excel;");
		List<CancelledFood> list = new ArrayList<CancelledFood>();
		//Object sum = null;
		
		String pin = (String)request.getAttribute("pin");
		//String isPaging = request.getParameter("isPaging");
/*		String limit = request.getParameter("limit");
		String start = request.getParameter("start");*/
		
		//String qtype = request.getParameter("qtype");
		String otype = request.getParameter("otype");
		String dtype = request.getParameter("dtype");
		String dateBeg = request.getParameter("dateBeg");
		String dateEnd = request.getParameter("dateEnd");
		String deptID = request.getParameter("deptID");
		String reasonID = request.getParameter("reasonID");
		
		if(otype == null || otype.trim().isEmpty()){
			otype = CancelledFoodDao.ORDER_BY_COUNT + "";
		}
		if(deptID == null || deptID.trim().isEmpty()){
			deptID = "-1";
		}
		if(reasonID == null || reasonID.trim().isEmpty()){
			reasonID = "-1";
		}
		DateType dt = DateType.valueOf(Integer.valueOf(dtype));
		Integer did = Integer.valueOf(deptID), rid = Integer.valueOf(reasonID);
		
		DutyRange queryDate = new DutyRange(dateBeg, dateEnd);
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		
		list = CancelledFoodDao.getCancelledFoodDetail(staff, queryDate, dt, did, rid);
		CancelledFood tempSum = new CancelledFood();
		if(list != null && list.size() > 0){
			CancelledFood tempItem = null;
			for(int i = 0; i < list.size(); i++){
				tempItem = list.get(i);
				tempSum.setCount(tempSum.getCount() + tempItem.getCount());
				tempSum.setTotalPrice(tempSum.getTotalPrice() + tempItem.getTotalPrice());
			}
		}
		
		String title = "退菜明细表";
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("退菜明细表.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 7000);
		sheet.setColumnWidth(2, 3300);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 2300);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3200);
		sheet.setColumnWidth(8, 6000);
		
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("总数量: " + tempSum.getCount() + "         总金额 :" + tempSum.getTotalPrice());
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));

//------------------
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("菜名");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("部门");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("单价");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("退菜数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("退菜金额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("退菜原因");
		cell.setCellStyle(headerStyle);
		
		
		for (CancelledFood cf : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(cf.getOrderDateFormat());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(cf.getFoodName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(cf.getDeptName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(cf.getOrderID());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(cf.getUnitPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(cf.getCount());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(cf.getTotalPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(cf.getWaiter());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(cf.getReason());
			cell.setCellStyle(strStyle);
			
		}
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	public ActionForward memberList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String extraCond = " ", orderClause = " ";
		String id = request.getParameter("id");
		String memberType = request.getParameter("memberType");
		String memberTypeAttr = request.getParameter("memberTypeAttr");
		String name = request.getParameter("name");
		String memberCard = request.getParameter("memberCard");
		String mobile = request.getParameter("mobile");
		String totalBalance = request.getParameter("totalBalance");
		String usedBalance = request.getParameter("usedBalance");
		String consumptionAmount = request.getParameter("consumptionAmount");
		String point = request.getParameter("point");
		String usedPoint = request.getParameter("usedPoint");
		String so = request.getParameter("so");
		
		if(id != null && !id.trim().isEmpty() && Integer.valueOf(id.trim()) > 0){
			extraCond += (" AND M.member_id = " + id);
		}else{
			if(so != null){
				so = so.trim();
				if(so.equals("0")){
					so = "=";
				}else if(so.equals("1")){
					so = ">=";
				}else if(so.equals("2")){
					so = "<=";
				}else{
					so = "=";
				}
			}else{
				so = "=";
			}
			
			if(memberType != null && !memberType.trim().isEmpty())
				extraCond += (" AND M.member_type_id = " + memberType);
			if(name != null && !name.trim().isEmpty())
				extraCond += (" AND M.name like '%" + name.trim() + "%'");
			
			if(memberCard != null && !memberCard.trim().isEmpty())
				extraCond += (" AND M.member_card like '%" + memberCard.trim() + "%'");
			
			if(mobile != null && !mobile.trim().isEmpty())
				extraCond += (" AND M.mobile like '%" + mobile.trim() + "%'");
				
			if(totalBalance != null && !totalBalance.trim().isEmpty())
				extraCond += (" AND (M.base_balance + M.extra_balance) " + so + totalBalance);
			
			if(usedBalance != null && !usedBalance.trim().isEmpty())
				extraCond += (" AND M.used_balance " + so + usedBalance);
			
			if(consumptionAmount != null && !consumptionAmount.trim().isEmpty())
				extraCond += (" AND M.consumption_amount " + so + consumptionAmount);
			
			if(usedPoint != null && !usedPoint.trim().isEmpty())
				extraCond += (" AND M.total_point " + so + usedPoint);
			
			if(point != null && !point.trim().isEmpty())
				extraCond += (" AND M.point " + so + point);
		}
		
		orderClause = " ORDER BY M.member_id ";
		
		Map<Object, Object> paramsSet = new HashMap<Object, Object>();
		paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
		paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
/*			if(isPaging != null && isPaging.trim().equals("true")){
			countSet = new HashMap<Object, Object>();
			countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			jobject.setTotalProperty(MemberDao.getMemberCount(countSet));
			// 分页
			orderClause += " LIMIT " + start + "," + limit;
		}*/
		List<Member> list = MemberDao.getMember(staff, extraCond, orderClause);
		List<Member> newList = new ArrayList<Member>(list);  
		if(memberTypeAttr != null && !memberTypeAttr.trim().isEmpty()){
			newList.clear();
			if(Integer.parseInt(memberTypeAttr) == MemberType.Attribute.INTERESTED.getVal()){
				newList.addAll(MemberDao.getInterestedMember(staff, extraCond));
			}else{
				List<Member> attrMember = new ArrayList<Member>();  
				for (Member member : list) {
					if(member.getMemberType().getAttribute().getVal() == Integer.parseInt(memberTypeAttr)){
						attrMember.add(member);
					};
				}
				newList.addAll(attrMember);
			}
		}
		
		String title = "会员列表";
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("会员列表.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 3300);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 3500);
		sheet.setColumnWidth(4, 3500);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3200);
		sheet.setColumnWidth(8, 4000);
		sheet.setColumnWidth(9, 4000);
		
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("会员数量: " + newList.size());
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 9));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 9));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("消费次数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("消费总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("累计积分");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("当前积分");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("总充值额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账户余额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("手机号码");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("会员卡号");
		cell.setCellStyle(headerStyle);
		
		for (Member member : newList) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(member.getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(member.getMemberType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(member.getConsumptionAmount());
			cell.setCellStyle(normalNumStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(member.getTotalConsumption());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(member.getTotalPoint());
			cell.setCellStyle(normalNumStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(member.getPoint());
			cell.setCellStyle(normalNumStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(member.getBaseBalance());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(member.getTotalBalance());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(member.getMobile());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(member.getMemberCard());
			cell.setCellStyle(strStyle);
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	public ActionForward commissionStatisticsList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String staffId = request.getParameter("staffId");
		String deptId = request.getParameter("deptId");
		
		DutyRange range = new DutyRange(beginDate, endDate);
		List<CommissionStatistics> list;
		if(staffId != null && !staffId.equals("-1") && !staffId.isEmpty()){
			if(deptId != null && !deptId.equals("-1")){
				list = CalcBillStatisticsDao.calcCommissionStatByDeptAndStaff(staff, range, Integer.parseInt(staffId), Integer.parseInt(deptId), DateType.HISTORY);
			}else{
				list = CalcBillStatisticsDao.calcCommissionStatByStaff(staff, range, Integer.parseInt(staffId), DateType.HISTORY);
			}
			
		}else if(deptId != null && !deptId.equals("-1")){
			list = CalcBillStatisticsDao.calcCommissionStatByDeptId(staff, range, Integer.parseInt(deptId), DateType.HISTORY);
		}else{
			list = CalcBillStatisticsDao.calcCommissionStatistics(staff, range, DateType.HISTORY);
		}
		CommissionStatistics total = null;
		if(!list.isEmpty()){
			total = new CommissionStatistics();
			for (CommissionStatistics item : list) {
//				total.setTotalPrice(item.getTotalPrice() + total.getTotalPrice());
				total.setCommission(item.getCommission() + total.getCommission());
			}
		}
		String title = "提成统计";
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("提成统计.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		sheet.setColumnWidth(8, 3000);
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("日期: " + beginDate + "  至  " + endDate +  "         提成总额: " + total.getCommission());
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("菜名");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("部门");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("单价");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("提成");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("人员");
		cell.setCellStyle(headerStyle);
		
		for (CommissionStatistics commission : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(DateUtil.format(commission.getOrderDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getFoodName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getDept().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getOrderId());
			cell.setCellStyle(normalNumStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getUnitPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getAmount());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getTotalPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getCommission());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getWaiter());
			cell.setCellStyle(strStyle);
			
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	public ActionForward commissionTotalList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String deptId = request.getParameter("deptId");
		
		DutyRange range = new DutyRange(beginDate, endDate);
		List<CommissionStatistics> list;
		if(deptId != null && !deptId.equals("-1")){
			list = CalcBillStatisticsDao.calcCommissionTotalByDept(staff, range, Integer.parseInt(deptId), DateType.HISTORY);
		}else{
			list = CalcBillStatisticsDao.calcCommissionTotal(staff, range, DateType.HISTORY);
		}
		CommissionStatistics total = null;
		if(!list.isEmpty()){
			total = new CommissionStatistics();
			for (CommissionStatistics item : list) {
//				total.setTotalPrice(item.getTotalPrice() + total.getTotalPrice());
				total.setCommission(item.getCommission() + total.getCommission());
			}
		}
		String title = "提成汇总";
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("提成汇总.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("日期: " + beginDate + "  至  " + endDate +  "      提成总额: " + total.getCommission());
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 2));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 2));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("销售总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("提成总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("人员");
		cell.setCellStyle(headerStyle);
		
		for (CommissionStatistics commission : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(commission.getTotalPrice());
			cell.setCellStyle(numStyle);

			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getCommission());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(commission.getWaiter());
			cell.setCellStyle(strStyle);
			
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}

	public ActionForward repaidStatisticsList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String staffId = request.getParameter("staffId");
		
		DutyRange range = new DutyRange(beginDate, endDate);
		List<RepaidStatistics> list = new ArrayList<RepaidStatistics>();
		if(staffId != null && !staffId.equals("-1") && !staffId.isEmpty()){
			list = CalcBillStatisticsDao.calcRepaidStatByStaff(staff, range, Integer.parseInt(staffId), DateType.HISTORY);
		}else{
			list = CalcBillStatisticsDao.calcRepaidStat(staff, range, DateType.HISTORY);
		}
		
		String title = "反结账统计";
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("反结账统计.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		sheet.setColumnWidth(8, 3000);
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("日期: " + beginDate + "  至  " + endDate +  "         账单数: " + list.size());
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("反结账时间");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("人员");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("原应收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("原实收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("反结账金额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("现应收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("现实收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("付款方式");
		cell.setCellStyle(headerStyle);
		
		for (RepaidStatistics repaid : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(DateUtil.format(repaid.getmOrderDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(repaid.getStaff().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(repaid.getmId());
			cell.setCellStyle(normalNumStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(NumericUtil.roundFloat(repaid.getTotalPrice() - repaid.getRepaidPrice()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(NumericUtil.roundFloat(repaid.getActualPrice() - repaid.getRepaidPrice()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(repaid.getRepaidPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(repaid.getTotalPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(repaid.getActualPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(repaid.getPaymentType().getDesc());
			cell.setCellStyle(strStyle);
			
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	public ActionForward historyOrder(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		
		response.setContentType("application/vnd.ms-excel;");
		String restaurantId = (String) request.getAttribute("restaurantID");
		String pin = (String)request.getAttribute("pin");
		String value = request.getParameter("value");
		
		String ope = request.getParameter("ope");
		if(ope != null && !ope.trim().isEmpty()){
			int opeType = Integer.parseInt(ope);
			
			if(opeType == 1){
				ope = "=";
			}else if(opeType == 2){
				ope = ">=";
			}else if(opeType == 3){
				ope = "<=";
			}else{
				ope = "=";
			}
		}else{
			ope = "=";
		}
		
		String comboCond;
		String comboType = request.getParameter("havingCond");
		if(comboType != null && !comboType.trim().isEmpty()){
			int comboVal = Integer.valueOf(comboType);
			if(comboVal == 1){
				//是否有反结帐
				comboCond = " AND OH.status = " + Order.Status.REPAID.getVal();
			}else if(comboVal == 2){
				//是否有折扣
				comboCond = " AND OH.discount_price > 0 ";
			}else if(comboVal == 3){
				//是否有赠送
				comboCond = " AND OH.gift_price > 0 ";
			}else if(comboVal == 4){
				//是否有退菜
				comboCond = " AND OH.cancel_price > 0 ";
			}else if(comboVal == 5){
				//是否有抹数
				comboCond = " AND OH.erase_price > 0 ";				
			}else{
				comboCond = "";
			}
		}else{
			comboCond = "";
		}
		
		String filterCond;
		String type = request.getParameter("type");
		if(type.equals("1")){
			//按账单号
			filterCond = " AND OH.id" + ope + value;
		}else if(type.equals("2")){
			//按流水号
			filterCond = " AND OH.seq_id " + ope + value;
		}else if(type.equals("3")){
			//按台号
			filterCond = " AND OH.table_alias != '' " + " AND OH.table_alias" + ope + value;
		}else if(type.equals("4")){
			//按日期
			String[] dutyParams = request.getParameter("value").split("<split>");
			filterCond = " AND OH.order_date BETWEEN '" + dutyParams[0] + "' AND '" + dutyParams[1] + "'";
		}else if(type.equals("5")){
			//按类型
			filterCond = " AND OH.category " + ope + value;
		}else if(type.equals("6")){
			//按结帐方式
			filterCond = " AND OH.pay_type " + ope + value;
		}else if(type.equals("7")){
			//按金额
			filterCond = " AND OH.total_price " + ope + value;
		}else if(type.equals("8")){
			//按实收
			filterCond = " AND OH.actual_price " + ope + value;
		}else if(type.equals("9")){
			DailySettle ds = SystemDao.getDailySettle(Integer.valueOf(restaurantId), SystemDao.MAX_DAILY_SETTLE);
//			System.out.println("ds: "+ds.getOnDutyFormat()+"  -  "+ds.getOffDutyFormat());
			filterCond = " AND OH.order_date BETWEEN '" + ds.getOnDutyFormat() + "' AND '" + ds.getOffDutyFormat() + "'";
		}else{
			filterCond = "";
		}
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		List<Order> list = OrderDao.getPureOrder(staff, comboCond + filterCond, null, DateType.HISTORY);
		
		String title = "历史账单";
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("历史账单.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 6000);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		sheet.setColumnWidth(8, 3000);
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("账单数量: " + list.size());
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		
//----	
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("台号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账单类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("结账方式");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("收款方式");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("应收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("实收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("状态");
		cell.setCellStyle(headerStyle);
		
		for (Order o : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(o.getId());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(o.getDestTbl().getAliasId());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(DateUtil.format(o.getOrderDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(o.getCategory().getDesc());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(o.getSettleType().getDesc());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(o.getPaymentType().getDesc());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(o.getTotalPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(o.getActualPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(o.getStatus().getDesc());
			cell.setCellStyle(strStyle);
		}
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		return null;
	}
	
	public ActionForward stockCollect(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String cateType = request.getParameter("cateType");
		String cateId = request.getParameter("cateId");
		String materialId = request.getParameter("materialId");
		String deptId = request.getParameter("deptId");
		
		List<StockReport> stockReports = null ;
		String extra = "";
		extra += " AND S.status = " + Status.AUDIT.getVal();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		if(beginDate == null || cateType == null){
				
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.MONTH, -1);
			beginDate = sdf.format(c.getTime());
			endDate = sdf.format(new Date());
			stockReports = StockReportDao.getStockCollectByTime(staff, beginDate, endDate, extra, null);
			
		}else{
			if(!materialId.equals("-1") && !materialId.trim().isEmpty()){
				extra += " AND M.material_id = " + materialId;
			}
			if(cateType.trim().isEmpty() && cateId.trim().isEmpty()){
				
			}else if(!cateType.trim().isEmpty() && cateId.trim().isEmpty()){
				extra += " AND S.cate_type = " + cateType;
			}else{
				extra += " AND M.cate_id = " + cateId; 
			}
			
			if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
				extra += " AND (S.dept_in = " + deptId +" OR S.dept_out = " + deptId + ")";
				stockReports = StockReportDao.getStockCollectByDept(staff, beginDate, endDate, extra, null, Integer.parseInt(deptId));
			}else{
				stockReports = StockReportDao.getStockCollectByTypes(staff, beginDate, endDate, extra, null);
			}
		}

		
		String title = "进销存汇总";
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("进销存汇总.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 4500);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
/*		sheet.setColumnWidth(8, 3000);
		sheet.setColumnWidth(9, 3000);
		sheet.setColumnWidth(10, 3000);
		sheet.setColumnWidth(11, 3000);
		sheet.setColumnWidth(12, 3000);
		sheet.setColumnWidth(13, 3000);
		sheet.setColumnWidth(14, 3000);
		sheet.setColumnWidth(15, 3000);
		sheet.setColumnWidth(16, 3000);*/
		
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("日期: " + beginDate + "  至  " + endDate +  "         物料个数: " + stockReports.size());
		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
//------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("物料编号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("物料名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("期初数量");
		cell.setCellStyle(headerStyle);
		
/*		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("");
		cell.setCellStyle(headerStyle);*/
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("入库小计");
		cell.setCellStyle(headerStyle);
		
/*		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("");
		cell.setCellStyle(headerStyle);*/
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("出库小计");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("期末数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("期末单价");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("期末金额");
		cell.setCellStyle(headerStyle);
		
		for (StockReport s : stockReports) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(s.getMaterial().getId());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getMaterial().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getPrimeAmount());
			cell.setCellStyle(numStyle);
			
/*			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockIn());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockInTransfer());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockSpill());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockTakeMore());
			cell.setCellStyle(numStyle);*/
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockInAmount());
			cell.setCellStyle(numStyle);
			
/*			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockOut());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockOutTransfer());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockDamage());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockTakeLess());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getUseUp());
			cell.setCellStyle(numStyle);*/
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getStockOutAmount());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getFinalAmount());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getFinalPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(s.getFinalMoney());
			cell.setCellStyle(numStyle);
		}
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		return null;
	}
	
}
