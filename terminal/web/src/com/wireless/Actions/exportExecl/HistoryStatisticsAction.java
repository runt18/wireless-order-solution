package com.wireless.Actions.exportExecl;

import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

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
import com.wireless.db.billStatistics.BusinessStatisticsDao;
import com.wireless.db.billStatistics.QuerySaleDetails;
import com.wireless.pojo.billStatistics.BusinessStatistics;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.protocol.Terminal;
import com.wireless.util.DataPaging;
import com.wireless.util.DateUtil;
import com.wireless.util.WebParams;

@SuppressWarnings("deprecation")
public class HistoryStatisticsAction extends DispatchAction{
	
	/**
	 * 
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
		response.addHeader("Content-Disposition","attachment;filename=" + new String("菜品销售明细(历史).xls".getBytes("GBK"), "ISO8859_1"));
		
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
				1);
		
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("菜品销售明细(历史)");
		HSSFRow row = null;
		HSSFCell cell = null;
		HSSFCellStyle style = null;
		HSSFFont font = null;
		HSSFDataFormat fromat = wb.createDataFormat();
		
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
		cell.setCellValue("菜品销售明细(历史)");
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
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 11));
		
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +terminal.owner);
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 11));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 11));
		
		// 列表头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		style = wb.createCellStyle();
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
//		style.setFillPattern(HSSFCellStyle.FINE_DOTS);
//		style.setFillForegroundColor((short)0xCCC);
//		style.setFillBackgroundColor((short)0xCCC);
		style.setFont(font);
		
		cell = row.createCell(0);
		cell.setCellValue("编号");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("名称");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("销量");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("营业额");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("折扣额");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("赠送额");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("成本");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("成本率");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("毛利");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("毛利率");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("均价");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("单位成本");
		cell.setCellStyle(style);
		
		if(saleDetails != null && saleDetails.length > 0){
			for(SalesDetail item : saleDetails){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				// ***
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				
				cell = row.createCell(0);
				cell.setCellValue(item.getFood().getAliasID());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getFood().getFoodName());
				cell.setCellStyle(style);
				
				// ***
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getSalesAmount());
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				cell.setCellStyle(style);
				
				// ***
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				style.setDataFormat(fromat.getFormat("0.00"));
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getIncome());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getDiscount());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getGifted());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getCost());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getCostRate());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getProfit());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getProfitRate());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getAvgCost());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getAvgPrice());
				cell.setCellStyle(style);
			}
		}
		
        OutputStream os=response.getOutputStream();
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("收款明细(历史).xls".getBytes("GBK"), "ISO8859_1"));
		
		String pin = request.getParameter("pin");
		String restaurantID = request.getParameter("restaurantID");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("pin", pin);
		params.put("restaurantID", restaurantID);
		params.put("onDuty", onDuty);
		params.put("offDuty", offDuty);
		
		Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
		List<BusinessStatistics> root = BusinessStatisticsDao.getBusinessReceiptsStatisticsByHistory(params);
		BusinessStatistics sum = new BusinessStatistics();
		
		// 创建execl主页
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("收款明细(历史)");
		HSSFRow row = null;
		HSSFCell cell = null;
		HSSFCellStyle style = null;
		HSSFFont font = null;
		HSSFDataFormat fromat = wb.createDataFormat();
		
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
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));
		// 冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("收款明细(历史)");
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
		
		style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty);
		cell.setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 12));
		
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +terminal.owner);
		cell.setCellStyle(style);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 12));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 12));
		
		// 列表头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		style = wb.createCellStyle();
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setFont(font);
		
		cell = row.createCell(0);
		cell.setCellValue("日期");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("应收");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("实收");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账单数");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("现金");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("刷卡");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("挂账");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("签单");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("折扣");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("赠送");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("退菜");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("抹数");
		cell.setCellStyle(style);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("反结账");
		cell.setCellStyle(style);
		
		if(root != null && root.size() > 0){
			for(BusinessStatistics item : root){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				// ***
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				
				cell = row.createCell(0);
				cell.setCellValue(item.getOnDutyToDate());
				cell.setCellStyle(style);
				
				// ***
				style = wb.createCellStyle();
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				style.setDataFormat(fromat.getFormat("0.00"));
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getTotalPrice());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getTotalPrice2());
				cell.setCellStyle(style);
				
				HSSFCellStyle ts = wb.createCellStyle();
				ts.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				ts.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				ts.setDataFormat(wb.createDataFormat().getFormat("0"));
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getOrderAmount());
				cell.setCellStyle(ts);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getCashIncome2());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getCreditCardIncome2());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getHangIncome2());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getSignIncome2());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getDiscountIncome());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getGiftIncome());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getCancelIncome());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getEraseIncome());
				cell.setCellStyle(style);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getPaidIncome());
				cell.setCellStyle(style);
				
				sum.setCashAmount(sum.getCashAmount() + item.getCashAmount());
				sum.setCashIncome2(sum.getCashIncome2() + item.getCashIncome2());
				sum.setCreditCardAmount(sum.getCreditCardAmount() + item.getCreditCardAmount());
				sum.setCreditCardIncome2(sum.getCreditCardIncome2() + item.getCreditCardIncome2());
				sum.setSignAmount(sum.getSignAmount() + item.getSignAmount());
				sum.setSignIncome2(sum.getSignIncome2() + item.getSignIncome2());
				sum.setHangAmount(sum.getHangAmount() + item.getHangAmount());
				sum.setHangIncome2(sum.getHangIncome2() + item.getHangIncome2());
				
				sum.setEraseAmount(sum.getEraseAmount() + item.getEraseAmount());
				sum.setEraseIncome(sum.getEraseIncome() + item.getEraseIncome());
				sum.setDiscountAmount(sum.getDiscountAmount() + item.getDiscountAmount());
				sum.setDiscountIncome(sum.getDiscountIncome() + item.getDiscountIncome());
				sum.setGiftAmount(sum.getGiftAmount() + item.getGiftAmount());
				sum.setGiftIncome(sum.getGiftIncome() + item.getGiftIncome());
				sum.setCancelAmount(sum.getCancelAmount() + item.getCancelAmount());
				sum.setCancelIncome(sum.getCancelIncome() + item.getCancelIncome());
				sum.setPaidIncome(sum.getPaidIncome() + item.getPaidIncome());
				
				sum.setTotalPrice(sum.getTotalPrice() + item.getTotalPrice());
				sum.setTotalPrice2(sum.getTotalPrice2() + item.getTotalPrice2());
				
				sum.setOrderAmount(sum.getOrderAmount() + item.getOrderAmount());
			}
		}
		
		OutputStream os=response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close(); 
				
		return null;
	}
	
	
	
}
