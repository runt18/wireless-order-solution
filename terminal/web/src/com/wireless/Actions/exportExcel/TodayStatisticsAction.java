package com.wireless.Actions.exportExcel;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;

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
import com.wireless.db.billStatistics.CalcBillStatisticsDao.ExtraCond;
import com.wireless.db.billStatistics.SaleDetailsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Region.RegionId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

@SuppressWarnings("deprecation")
public class TodayStatisticsAction extends DispatchAction{
	
	private HSSFCellStyle headerStyle = null, titleStyle = null, strStyle = null, numStyle = null, normalNumStyle= null;
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
		String region = request.getParameter("region");
		String orderType = request.getParameter("orderType");
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		Integer ot = (orderType != null && !orderType.isEmpty()) ? Integer.parseInt(orderType) : SaleDetailsDao.ORDER_BY_SALES;
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		DutyRange dutyRange = new DutyRange(onDuty, offDuty);
		
		CalcBillStatisticsDao.ExtraCond extraConds = new ExtraCond(DateType.TODAY);
		
		if(opening != null && !opening.isEmpty()){
			HourRange hr = new HourRange(opening, ending, DateUtil.Pattern.HOUR);
			extraConds.setHourRange(hr);
		}
		
		if(foodName != null && !foodName.isEmpty()){
			extraConds.setFoodName(foodName);
		}
		
		if(deptID != null && !deptID.equals("-1")){
			extraConds.setDept(Department.DeptId.valueOf(Integer.parseInt(deptID)));
		}
		
		if(region != null && !region.equals("-1")){
			extraConds.setRegion(RegionId.valueOf(Integer.parseInt(region)));
			
		}
		List<SalesDetail> saleDetails = SaleDetailsDao.getByFood(staff, dutyRange, extraConds, ot);
		
		
		
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("菜品销售统计(历史)");
		HSSFRow row = null;
		HSSFCell cell = null;
		// ******
		initParams(wb);
		
		sheet.setColumnWidth(0, 8000);
		sheet.setColumnWidth(1, 2000);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 3500);
		sheet.setColumnWidth(4, 3500);
		sheet.setColumnWidth(5, 3500);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		sheet.setColumnWidth(8, 3000);
		sheet.setColumnWidth(9, 3000);
		sheet.setColumnWidth(10, 3000);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
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
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty + "         共: " + saleDetails.size() + " 条");
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +staff.getName());
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		
		// 列表头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
//		style.setFillForegroundColor((short)0xCCC);
//		style.setFillBackgroundColor((short)0xCCC);
		
		cell = row.createCell(0);
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
		
		if(saleDetails != null && saleDetails.size() > 0){
			for(SalesDetail item : saleDetails){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				// ***
				cell = row.createCell(0);
				cell.setCellValue(item.getFood().getName());
				cell.setCellStyle(strStyle);
				
				// ***
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(item.getSalesAmount());
				cell.setCellStyle(normalNumStyle);
				
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
		response.addHeader("Content-Disposition","attachment;filename=" + new String(("分厨銷售统计(" + DateType.TODAY.getDesc() + ").xls").getBytes("GBK"), "ISO8859_1"));
		
		String pin = (String)request.getAttribute("pin");
		String onDuty = request.getParameter("onDuty");
		String offDuty = request.getParameter("offDuty");
		
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String region = request.getParameter("region");
		
		DutyRange dutyRange = new DutyRange(onDuty, offDuty);
		
		CalcBillStatisticsDao.ExtraCond extraCond = new ExtraCond(DateType.TODAY);
		
		if(opening != null && !opening.isEmpty()){
			HourRange hr = new HourRange(opening, ending, DateUtil.Pattern.HOUR);
			extraCond.setHourRange(hr);
		}
		
		if(region != null && !region.equals("-1")){
			extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
		}
		List<SalesDetail> list = SaleDetailsDao.getByKitchen(
				staff, 
				dutyRange,
				extraCond);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("分厨销售统计(" + DateType.TODAY.getDesc() + ")");
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
		cell.setCellValue("分厨销售统计(" + DateType.TODAY.getDesc() + ")");
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
		
		if(list != null && list.size() > 0){
			SalesDetail temp = null, sum = new SalesDetail();
			Kitchen kitchen = new Kitchen(-1);
			kitchen.setName("汇总");
			sum.setKitchen(kitchen);
			for(int i = 0; i <= list.size(); i++){
				if(i == list.size()){
					temp = sum;
				}else{
					temp = list.get(i);					
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
		
		String opening = request.getParameter("opening");
		String ending = request.getParameter("ending");
		
		String region = request.getParameter("region");
		
		CalcBillStatisticsDao.ExtraCond extraCond = new ExtraCond(DateType.TODAY);
		
		if(region != null && !region.equals("-1")){
			extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
		}
		
		if(opening != null && !opening.isEmpty()){
			HourRange hr = new HourRange(opening, ending, DateUtil.Pattern.HOUR);
			extraCond.setHourRange(hr);
		}
		
		DutyRange dutyRange = new DutyRange(onDuty, offDuty);
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		List<SalesDetail> list = SaleDetailsDao.execByDept(
				staff, 
				dutyRange,
				extraCond);
		
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
//		cell.setCellStyle(strStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
				
		// *****
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +staff.getName());
//		cell.setCellStyle(strStyle);
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
		
		if(list != null && list.size() > 0){
			SalesDetail temp = null, sum = new SalesDetail();
			sum.setDept(new Department(0, (short)0, "汇总"));
			for(int i = 0; i <= list.size(); i++){
				if(i == list.size()){
					temp = sum;
				}else{
					temp = list.get(i);					
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
}
