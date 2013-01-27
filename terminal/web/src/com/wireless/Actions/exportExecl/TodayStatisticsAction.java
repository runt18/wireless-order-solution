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

import com.wireless.db.VerifyPin;
import com.wireless.db.billStatistics.QuerySaleDetails;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.protocol.Terminal;
import com.wireless.util.DateUtil;

public class TodayStatisticsAction extends DispatchAction{
	
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
		response.addHeader("Content-Disposition","attachment;filename=" + new String("菜品销售明细(当日).xls".getBytes("GBK"), "ISO8859_1"));
		
		String pin = request.getParameter("pin");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		
		Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
		SalesDetail[] saleDetails = QuerySaleDetails.execByFood(
				terminal, 
				onDuty, 
				offDuty,
				new int[0],
				QuerySaleDetails.ORDER_BY_SALES,
				0);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("菜品销售明细(当日)");
		HSSFRow row = null;
		HSSFCell cell = null;
		HSSFCellStyle style = null;
		HSSFFont font = null;
		HSSFDataFormat fromat = wb.createDataFormat();
		
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
		cell.setCellValue("菜品销售明细(当日)");
		style = wb.createCellStyle();
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		font = wb.createFont();
		font.setFontHeight((short) 350);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		style.setFont(font);
		cell.setCellStyle(style);
		
		font = wb.createFont();
		font.setFontHeight((short) 220);
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty);
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
//		cell.getCellStyle().setFont(font);
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
		style = wb.createCellStyle();
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setFont(font);
		
		cell = row.createCell(0);
		cell.setCellValue("编号");
		cell.setCellStyle(style);
		
		cell = row.createCell(1);
		cell.setCellValue("名称");
		cell.setCellStyle(style);
		
		cell = row.createCell(2);
		cell.setCellValue("销量");
		cell.setCellStyle(style);
		
		cell = row.createCell(3);
		cell.setCellValue("营业额");
		cell.setCellStyle(style);
		
		cell = row.createCell(4);
		cell.setCellValue("折扣额");
		cell.setCellStyle(style);
		
		cell = row.createCell(5);
		cell.setCellValue("赠送额");
		cell.setCellStyle(style);
		
		if(saleDetails != null && saleDetails.length > 0){
			for(int i = 0; i < saleDetails.length; i++){
				SalesDetail item = saleDetails[i];
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				cell = row.createCell(0);
				cell.setCellValue(item.getFood().getAliasID());
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				cell.setCellStyle(style);
				
				cell = row.createCell(1);
				cell.setCellValue(item.getFood().getFoodName());
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				cell.setCellStyle(style);
				
				cell = row.createCell(2);
				cell.setCellValue(item.getSalesAmount());
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				cell.setCellStyle(style);

				cell = row.createCell(3);
				cell.setCellValue(item.getIncome());
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				style.setDataFormat(fromat.getFormat("0.00"));
				cell.setCellStyle(style);
				
				cell = row.createCell(4);
				cell.setCellValue(item.getDiscount());
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				style.setDataFormat(fromat.getFormat("0.00"));
				cell.setCellStyle(style);
				
				cell = row.createCell(5);
				cell.setCellValue(item.getGifted());
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				style.setDataFormat(fromat.getFormat("0.00"));
				cell.setCellStyle(style);
			}
		}
		
        OutputStream os=response.getOutputStream();
        wb.write(os);
        os.flush();
        os.close();
        
		return null;
	}
}
