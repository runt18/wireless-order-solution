package com.wireless.Actions.exportExecl;

import java.io.OutputStream;
import java.util.Date;

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

import com.wireless.db.billStatistics.QuerySaleDetails;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.util.DateType;

@SuppressWarnings("deprecation")
public class TodayStatisticsAction extends DispatchAction{
	
	private HSSFCellStyle headerStyle = null, titleStyle = null, strStyle = null, numStyle = null;
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
		
		doubleForamt = wb.createDataFormat();
		numStyle = wb.createCellStyle();
		numStyle.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		numStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		numStyle.setDataFormat(doubleForamt.getFormat("0.00"));
	}
	
	/**
	 * 当日菜品销售明细
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
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String(("菜品销售明细(" + DateType.TODAY.getName() + ").xls").getBytes("GBK"), "ISO8859_1"));
		
		String pin = request.getParameter("pin");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		String deptID = request.getParameter("deptID");
		
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
		Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
		SalesDetail[] saleDetails = QuerySaleDetails.execByFood(
				terminal, 
				onDuty, 
				offDuty,
				did,
				QuerySaleDetails.ORDER_BY_SALES,
				DateType.TODAY.getValue()
		);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("菜品销售明细(" + DateType.TODAY.getName() + ")");
		HSSFRow row = null;
		HSSFCell cell = null;
		// ******
		initParams(wb);
		
		sheet.setColumnWidth(0, 2000);
		sheet.setColumnWidth(1, 8000);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
		// 冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("菜品销售明细(" + DateType.TODAY.getName() + ")");
		cell.setCellStyle(titleStyle);
		
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty);
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 5));
		
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +terminal.owner);
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 5));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 5));
		
		// 列表头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		
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
		
		// tempStyle
		HSSFCellStyle tempStyle = wb.createCellStyle();
		tempStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		tempStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		
		if(saleDetails != null && saleDetails.length > 0){
			for(int i = 0; i < saleDetails.length; i++){
				SalesDetail item = saleDetails[i];
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				cell = row.createCell(0);
				cell.setCellValue(item.getFood().getAliasId());
				cell.setCellStyle(tempStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getFood().getName());
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getSalesAmount());

				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncome());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getDiscount());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getGifted());
				cell.setCellStyle(numStyle);
			}
		}
		
        OutputStream os=response.getOutputStream();
        wb.write(os);
        os.flush();
        os.close();
        
		return null;
	}
	
	/**
	 * 分厨銷售统计(当日)
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
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String(("分厨銷售统计(" + DateType.TODAY.getName() + ").xls").getBytes("GBK"), "ISO8859_1"));
		
		String pin = request.getParameter("pin");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		
		Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
		SalesDetail[] list = QuerySaleDetails.execByKitchen(terminal, onDuty, offDuty, DateType.TODAY.getValue());
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("分厨销售统计(" + DateType.HISTORY.getName() + ")");
		HSSFRow row = null;
		HSSFCell cell = null;
		// 初始化参数,重要
		initParams(wb);
		
		sheet.setColumnWidth(0, 3500);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 3500);
		sheet.setColumnWidth(3, 3500);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("分厨销售统计(" + DateType.HISTORY.getName() + ")");
		cell.setCellStyle(titleStyle);
				
		// *****
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty);
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
				
		// *****
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +terminal.owner);
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
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
		
		if(list != null && list.length > 0){
			SalesDetail temp = null, sum = new SalesDetail();
			Kitchen kitchen = new Kitchen();
			kitchen.setId(-1);
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
			}
		}
		
		OutputStream os=response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	/**
	 * 部分销售统计(当日)
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
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String(("部门销售统计(" + DateType.TODAY.getName() + ").xls").getBytes("GBK"), "ISO8859_1"));
		
		String pin = request.getParameter("pin");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		
		Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
		SalesDetail[] list = QuerySaleDetails.execByDept(terminal, onDuty, offDuty, DateType.TODAY.getValue());
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("部门销售统计(" + DateType.HISTORY.getName() + ")");
		HSSFRow row = null;
		HSSFCell cell = null;
		// 初始化参数,重要
		initParams(wb);
		
		sheet.setColumnWidth(0, 3500);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 3500);
		sheet.setColumnWidth(3, 3500);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("部门销售统计(" + DateType.HISTORY.getName() + ")");
		cell.setCellStyle(titleStyle);
				
		// *****
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty);
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
				
		// *****
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +terminal.owner);
		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));
		
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
			}
		}
		
		OutputStream os=response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
}
