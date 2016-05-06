package com.wireless.Actions.exportExcel;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.wireless.db.billStatistics.CalcCommissionStatisticsDao;
import com.wireless.db.billStatistics.CalcDiscountStatisticsDao;
import com.wireless.db.billStatistics.CalcEraseStatisticsDao;
import com.wireless.db.billStatistics.CalcRepaidStatisticsDao;
import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.billStatistics.SaleDetailsDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.member.MemberCondDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.OrderFoodDao;
import com.wireless.db.orderMgr.PayTypeDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponEffectDao;
import com.wireless.db.promotion.CouponOperationDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.db.stockMgr.StockActionDetailDao;
import com.wireless.db.stockMgr.StockDetailReportDao;
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.IncomeByPay.PaymentIncome;
import com.wireless.pojo.billStatistics.SalesDetail;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.billStatistics.commission.CommissionStatistics;
import com.wireless.pojo.billStatistics.repaid.RepaidStatistics;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberCond;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.OperationCate;
import com.wireless.pojo.member.MemberOperation.OperationType;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponEffect;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Region.RegionId;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.Status;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.stockMgr.StockDetailReport;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;

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
	
	
	public ActionForward couponDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("优惠券统计.xls".getBytes("GBK"), "ISO8859_1"));
		
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String staffId = request.getParameter("staffId");
		final String begin = request.getParameter("beginDate");
		final String end = request.getParameter("endDate");
		final String isDuty = request.getParameter("isDuty");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		final String operate = request.getParameter("operate");
		final String operateType = request.getParameter("operateType");
		final String memberFuzzy = request.getParameter("memberFuzzy");
		final String couponId = request.getParameter("couponId");
		final String couponTypeId = request.getParameter("couponTypeId");
			
		Staff staff = StaffDao.verify(Integer.parseInt(pin));

		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}

		final CouponOperationDao.ExtraCond extraCond = new CouponOperationDao.ExtraCond();
		
		if(staffId != null && !staffId.isEmpty()){
			extraCond.setStaff(Integer.parseInt(staffId));
		}
		
		if(begin != null && !begin.isEmpty() && end != null && !end.isEmpty()){
			if(isDuty != null && !isDuty.isEmpty() && Boolean.parseBoolean(isDuty)){
				DutyRange range = DutyRangeDao.exec(staff, begin, end);
				if(range != null){
					extraCond.setRange(range);
				}else{
					extraCond.setRange(begin, end);
				}
			}else{
				extraCond.setRange(begin, end);
			}
		}
		
		if(opening != null && !opening.isEmpty() && ending != null && !ending.isEmpty()){
			extraCond.setHourRange(opening, ending);
		}
		
		if(operate != null && !operate.isEmpty()){
			extraCond.setOperate(CouponOperation.Operate.valueOf(Integer.parseInt(operate)));
		}
		
		if(operateType != null && !operateType.isEmpty()){
			if(operateType.equalsIgnoreCase("issue")){
				extraCond.setOperateType(CouponOperation.OperateType.ISSUE);
			}else if(operateType.equalsIgnoreCase("use")){
				extraCond.setOperateType(CouponOperation.OperateType.USE);
			}
		}
		
		if(memberFuzzy != null && !memberFuzzy.isEmpty()){
			extraCond.setMemberFuzzy(memberFuzzy);
		}
		
		if(couponId != null && !couponId.isEmpty()){
			extraCond.setCoupon(Integer.parseInt(couponId));
		}
		
		if(couponTypeId != null && !couponTypeId.isEmpty()){
			extraCond.setCouponType(Integer.parseInt(couponTypeId));
		}
		
		//获取优惠券的操作记录
		final List<CouponOperation> result = CouponOperationDao.getByCond(staff, extraCond);
		
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("优惠券统计");
		HSSFRow row = null;
		HSSFCell cell = null;
		
		initParams(wb);
		
		sheet.setColumnWidth(0, 8000);
		sheet.setColumnWidth(1, 4000);
		sheet.setColumnWidth(2, 3500);
		sheet.setColumnWidth(3, 4500);
		sheet.setColumnWidth(4, 4000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(4, 7000);
		sheet.setColumnWidth(5, 8500);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
		
		
		//冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("优惠券统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")");
		cell.setCellStyle(titleStyle);
		
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + begin + " 至 " + end + "         共: " + result.size() + " 条");
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
				
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " + staff.getName());
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
				
		cell = row.createCell(0);
		cell.setCellValue("操作日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("优惠券");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("面额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("关联信息");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("会员");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("备注");
		cell.setCellStyle(headerStyle);
		
		if(result != null && result.size() > 0){
			for(CouponOperation item : result){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				cell = row.createCell(0);
				cell.setCellValue(DateUtil.format(item.getOperateDate()));
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getCouponName());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getCouponPrice());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getOperate().getVal());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getAssociateId());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getMemberId());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getOperateStaff());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getComment());
				cell.setCellStyle(headerStyle);
				
			}
		}
		
		OutputStream os = response.getOutputStream();
        wb.write(os);
        os.flush();
        os.close();
		return null;
	}
	
	public ActionForward couponEffectDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("优惠活动效果统计.xls".getBytes("GBK"), "ISO8859_1"));
		
		final String pin = (String)request.getAttribute("pin");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String couponId = request.getParameter("couponId");
		final String branchId = request.getParameter("branchId");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final CouponEffectDao.ExtraCond extraCond = new CouponEffectDao.ExtraCond();
		
		if(beginDate != null && !beginDate.isEmpty() && endDate != null && !endDate.isEmpty()){
			extraCond.setRange(beginDate, endDate);
		}
		
		if(couponId != null && !couponId.isEmpty()){
			extraCond.setCouponType(Integer.parseInt(couponId));
		}
		
		if(branchId != null && !branchId.isEmpty()){
			extraCond.setBranchId(Integer.parseInt(branchId));
		}
		
		//获取优惠活动的记录
		List<CouponEffect> result = CouponEffectDao.calcByCond(staff, extraCond);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("优惠活动效果统计");
		HSSFRow row = null;
		HSSFCell cell = null;
		
		initParams(wb);
		
		sheet.setColumnWidth(0, 8000);
		sheet.setColumnWidth(1, 4000);
		sheet.setColumnWidth(2, 3500);
		sheet.setColumnWidth(3, 4500);
		sheet.setColumnWidth(4, 4000);
		sheet.setColumnWidth(5, 5000);
		sheet.setColumnWidth(4, 7000);
		sheet.setColumnWidth(5, 8500);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
		
		//冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("优惠活动效果统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")");
		cell.setCellStyle(titleStyle);
		
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + beginDate + " 至 " + endDate + "         共: " + result.size() + " 条");
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " + staff.getName());
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		
		cell = row.createCell(0);
		cell.setCellValue("优惠活动名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("优惠券面额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("共发送张数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("优惠券成本");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("共使用张数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("使用的优惠券总面额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("拉动消费次数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("拉动消费额");
		cell.setCellStyle(headerStyle);
		
		if(result != null && result.size() > 0){
			for(CouponEffect item : result){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				cell = row.createCell(0);
				cell.setCellValue(item.getCouponName());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getCouponPrice());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIssuedAmount());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIssuedPrice());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getUsedAmount());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getUsedPrice());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getSalesAmount());
				cell.setCellStyle(headerStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getEffectSales());
				cell.setCellStyle(headerStyle);
				
			}
		}
		
		OutputStream os = response.getOutputStream();
        wb.write(os);
        os.flush();
        os.close();
		return null;
		
		
	}
	
	
	/**
	 * 销售统计（菜品）导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward salesFoodDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("菜品销售统计.xls".getBytes("GBK"), "ISO8859_1"));
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String deptId = request.getParameter("deptID");
		final String foodName = new String(request.getParameter("foodName").getBytes("ISO8859_1"), "UTF-8");
		final String region = request.getParameter("region");
		final String orderType = request.getParameter("orderType");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		final String kitchenId = request.getParameter("kitchenId");
		
		final int ot = (orderType != null && !orderType.isEmpty()) ? Integer.parseInt(orderType) : SaleDetailsDao.ORDER_BY_SALES;
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && !branchId.isEmpty() && Integer.valueOf(branchId) >= 0){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final CalcBillStatisticsDao.ExtraCond extraCond = new ExtraCond(DateType.HISTORY).setCalcByDuty(true);
		
		if(opening != null && !opening.isEmpty()){
			extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
		}
		
		if(foodName != null && !foodName.isEmpty()){
			extraCond.setFoodName(foodName);
		}
		
		if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
			extraCond.setDept(Department.DeptId.valueOf(Integer.parseInt(deptId)));
		}
		
		if(kitchenId != null && !kitchenId.isEmpty() && !kitchenId.equals("-1")){
			extraCond.setKitchen(Integer.parseInt(kitchenId));
		}
		
		if(region != null && !region.isEmpty() && !region.equals("-1")){
			extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
			
		}
		
		extraCond.setDutyRange(new DutyRange(onDuty, offDuty));
		
		final List<SalesDetail> saleDetails = SaleDetailsDao.getByFood(staff, extraCond, ot);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("菜品销售统计");
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
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
		// 冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("菜品销售统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")");
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("销量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("营业额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("折扣额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("赠送数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("赠送额");
		cell.setCellStyle(headerStyle);
		
		
		if(saleDetails != null && saleDetails.size() > 0){
			for(SalesDetail item : saleDetails){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				// 名称***
				cell = row.createCell(0);
				cell.setCellValue(item.getFood().getName());
				cell.setCellStyle(strStyle);
				
				// 销量***
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getSalesAmount());
				cell.setCellStyle(normalNumStyle);
				
				// 营业额***
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncome());
				cell.setCellStyle(numStyle);
				
				//折扣额
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getDiscount());
				cell.setCellStyle(numStyle);
				
				//赠送数量
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getGiftAmount());
				cell.setCellStyle(numStyle);
				
				//赠送额
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getGifted());
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
	 * 进销存明细导出
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward stockActionDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("进销存明细.xls".getBytes("GBK"), "ISO8859_1"));
		
		final String pin = (String)request.getAttribute("pin");
		final String beginDate = request.getParameter("beginDate");
		final String materialId = request.getParameter("materialId");
		final String materialCateId = request.getParameter("materialCateId");
		final String cateType = request.getParameter("cateType");
		final String deptOut = request.getParameter("deptOut");
		final String deptIn = request.getParameter("deptIn");
		final String supplier = request.getParameter("supplier");
		//String stockType = request.getParameter("stockType");
		final String subType = request.getParameter("subType");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		final StockActionDetailDao.ExtraCond extraCond = new StockActionDetailDao.ExtraCond();
		
		extraCond.setOriDate(beginDate + "-01", beginDate + "-31");
		
		if(materialId != null && !materialId.isEmpty()){
			extraCond.setMaterial(Integer.parseInt(materialId));
		}
		
		if(materialCateId != null && !materialCateId.isEmpty()){
			extraCond.setMaterialCate(Integer.parseInt(materialCateId));
		}
		
		if(cateType != null && !cateType.isEmpty() && !cateType.equals("-1")){
			extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.parseInt(cateType)));
		}
		
		if(subType != null && !subType.isEmpty()){
			extraCond.addSubType(StockAction.SubType.valueOf(Integer.parseInt(subType)));
		}
			
		if(supplier != null && !supplier.isEmpty() && !supplier.equals("-1")){
			extraCond.setSupplier(Integer.parseInt(supplier));
		}
		
		if(deptIn != null && !deptIn.isEmpty()){
			extraCond.setDeptIn(Integer.parseInt(deptIn));
		}

		if(deptOut != null && !deptOut.isEmpty()){
			extraCond.setDeptOut(Integer.parseInt(deptOut));
		}
		
		List<StockDetailReport> stockDetailReports = StockDetailReportDao.getByCond(staff, extraCond);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("进销存明细");
		HSSFRow row = null;
		HSSFCell cell = null;
		// ******
		initParams(wb);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 4000);
		sheet.setColumnWidth(2, 5500);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
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
		cell.setCellValue("进销存明细");
		cell.setCellStyle(titleStyle);
		
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + beginDate + "         共: " + stockDetailReports.size() + " 条");
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 11));
		
		// 导出操作相关信息
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);

		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 11));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 11));
		
		// 列表头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		
		cell = row.createCell(0);
		cell.setCellValue("日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("货品名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("供应商");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库部门");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库部门");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库金额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库金额");
		cell.setCellStyle(headerStyle);

		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("结存数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);		
		
		for(StockDetailReport item : stockDetailReports){
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			// 日期
			cell = row.createCell(0);
			cell.setCellValue(DateUtil.format(item.getStockAction().getOriStockDate(), DateUtil.Pattern.DATE));
			cell.setCellStyle(strStyle);
			
			// 原始单号
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(item.getStockAction().getOriStockId());
			cell.setCellStyle(strStyle);
			
			// 货品名称
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(item.getStockActionDetail().getName());
			cell.setCellStyle(strStyle);
			
			//供应商
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(!item.getStockAction().getSupplier().getName().isEmpty() ? item.getStockAction().getSupplier().getName() : "----");
			cell.setCellStyle(strStyle);
			
			if(item.getStockAction().getType() == StockAction.Type.STOCK_IN){
				//出库部门
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(!item.getStockAction().getDeptOut().getName().isEmpty() ? item.getStockAction().getDeptOut().getName() : "----");
				cell.setCellStyle(strStyle);
				
				//入库部门
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockAction().getDeptIn().getName());
				cell.setCellStyle(strStyle);
				
				//入库类型
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockAction().getSubType().getText());
				cell.setCellStyle(strStyle);
				
				//入库数量
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockActionDetail().getAmount());
				cell.setCellStyle(numStyle);
				
				//入库金额 
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockActionDetail().getAmount() * item.getStockActionDetail().getPrice());
				cell.setCellStyle(numStyle);
				
				//出库类型
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue("----");
				cell.setCellStyle(strStyle);
				
				//出库数量
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue("----");
				cell.setCellStyle(strStyle);
				
				//出库金额
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue("----");
				cell.setCellStyle(strStyle);
				
			}else if(item.getStockAction().getType() == StockAction.Type.STOCK_OUT){
				
				//出库部门
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockAction().getDeptOut().getName());
				cell.setCellStyle(strStyle);
				
				//入库部门
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(!item.getStockAction().getDeptIn().getName().isEmpty() ? item.getStockAction().getDeptIn().getName() : "----");
				cell.setCellStyle(strStyle);
				
				//入库类型
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue("----");
				cell.setCellStyle(strStyle);
				
				//入库数量
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue("----");
				cell.setCellStyle(strStyle);
				
				//入库金额 
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue("----");
				cell.setCellStyle(strStyle);
				
				//出库类型
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockAction().getSubType().getText());
				cell.setCellStyle(strStyle);
				
				//出库数量
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockActionDetail().getAmount());
				cell.setCellStyle(numStyle);
				
				//出库金额
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockActionDetail().getAmount() * item.getStockActionDetail().getPrice());
				cell.setCellStyle(numStyle);
				
			}				
			
			//结存数量
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(item.getStockActionDetail().getRemaining());
			cell.setCellStyle(numStyle);
			
			//操作人
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(item.getStockAction().getApprover());
			cell.setCellStyle(strStyle);
		}
		
        OutputStream os = response.getOutputStream();
        wb.write(os);
        os.flush();
        os.close();
        
		return null;
	}	
	
	
	/**
	 * 消耗差异表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward detailReport(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("消耗差异表.xls".getBytes("GBK"), "ISO8859_1"));
		
		final String pin = (String)request.getAttribute("pin");
		final String beginDate = request.getParameter("beginDate");
		//表头统计时间
		final String date = beginDate;
		final String materialId = request.getParameter("materialId");
		final String cateType = request.getParameter("cateType");
		final String cateId = request.getParameter("cateId");
		final String deptId = request.getParameter("deptId");
		List<StockReport> deltaReports = new ArrayList<StockReport>();
		StockReportDao.ExtraCond extraCond = new StockReportDao.ExtraCond();
		Staff staff = StaffDao.verify(Integer.parseInt(pin));

		if(beginDate != null && !beginDate.isEmpty()){
			extraCond.setRange(beginDate);
		}
		
		if(materialId != null && !materialId.isEmpty()){
			extraCond.setMaterialCate(Integer.valueOf(materialId));
		}
		
		if(cateType != null && !cateType.isEmpty()){
			extraCond.setMaterialCateType(MaterialCate.Type.valueOf(Integer.valueOf(cateType)));
		}
		
		if(cateId != null && !cateId.isEmpty()){
			extraCond.setMaterialCate(Integer.valueOf(cateId));
		}
		
		if(Integer.valueOf(deptId) >= 0){
			extraCond.setDept(Integer.valueOf(deptId));
		}

		deltaReports = StockReportDao.getByCond(staff, extraCond);
		
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("消耗差异表");
		HSSFRow row = null;
		HSSFCell cell = null;
		// ******
		initParams(wb);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 3000);
		sheet.setColumnWidth(3, 3000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
		// 冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("消耗差异表");
		cell.setCellStyle(titleStyle);
		
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("日期: " + date +"         共: " + deltaReports.size() + " 条");
		cell.getCellStyle().setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 7));
		
		// 列表头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		
		cell = row.createCell(0);
		cell.setCellValue("品项名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("期初数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库总数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库总数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("期末数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("实际消耗");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("理论消耗");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("差异数");
		cell.setCellStyle(headerStyle);
		
		if(deltaReports != null && deltaReports.size() > 0){
			for(StockReport item : deltaReports){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				// 品项名称
				cell = row.createCell(0);
				cell.setCellValue(item.getMaterial().getName());
				cell.setCellStyle(strStyle);
				
				// 期初数量
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getPrimeAmount());
				cell.setCellStyle(normalNumStyle);
				
				// 入库总量
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockInTotal());
				cell.setCellStyle(normalNumStyle);
				
				//出库总量
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getStockOutTotal());
				cell.setCellStyle(normalNumStyle);
				
				//期末数量
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getFinalAmount());
				cell.setCellStyle(normalNumStyle);
				
				//实际消耗
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getActualConsumption());
				cell.setCellStyle(normalNumStyle);
				
				//理论消耗
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getExpectConsumption());
				cell.setCellStyle(normalNumStyle);
				
				//差异数
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getDeltaAmount());
				cell.setCellStyle(normalNumStyle);
				
			}
		}
		
        OutputStream os = response.getOutputStream();
        wb.write(os);
        os.flush();
        os.close();
        
		return null;
	}		
	
	
	/**
	 * 销售统计（厨房）导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward salesByKitchen(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String(("分厨銷售统计.xls").getBytes("GBK"), "ISO8859_1"));
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String region = request.getParameter("region");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && !branchId.isEmpty() && Integer.valueOf(branchId) >= 0){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final CalcBillStatisticsDao.ExtraCond extraCond = new ExtraCond(DateType.HISTORY).setCalcByDuty(true);
		
		if(opening != null && !opening.isEmpty()){
			HourRange hr = new HourRange(opening, ending, DateUtil.Pattern.HOUR);
			extraCond.setHourRange(hr);
		}
		
		if(region != null && !region.isEmpty() && !region.equals("-1")){
			extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
		}
		
		extraCond.setDutyRange(new DutyRange(onDuty, offDuty));
		
		List<SalesDetail> list = SaleDetailsDao.getByKitchen(staff,	extraCond);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("分厨销售统计");
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
		cell.setCellValue("分厨销售统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")");
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("营业额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("折扣额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("赠送额");
		cell.setCellStyle(headerStyle);
		
//		cell = row.createCell((int)row.getLastCellNum());
//		cell.setCellValue("成本");
//		cell.setCellStyle(headerStyle);
		
//		cell = row.createCell((int)row.getLastCellNum());
//		cell.setCellValue("成本率");
//		cell.setCellStyle(headerStyle);
		
//		cell = row.createCell((int)row.getLastCellNum());
//		cell.setCellValue("毛利");
//		cell.setCellStyle(headerStyle);
		
//		cell = row.createCell((int)row.getLastCellNum());
//		cell.setCellValue("毛利率");
//		cell.setCellStyle(headerStyle);
		
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
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(temp.getIncome());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(temp.getDiscount());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(temp.getGifted());
				cell.setCellStyle(numStyle);
				
//				cell = row.createCell((int)row.getLastCellNum());
//				cell.setCellValue(temp.getCost());
//				cell.setCellStyle(numStyle);
//				
//				cell = row.createCell((int)row.getLastCellNum());
//				cell.setCellValue(temp.getCostRate());
//				cell.setCellStyle(numStyle);
//				
//				cell = row.createCell((int)row.getLastCellNum());
//				cell.setCellValue(temp.getProfit());
//				cell.setCellStyle(numStyle);
//				
//				cell = row.createCell((int)row.getLastCellNum());
//				cell.setCellValue(temp.getProfitRate());
//				cell.setCellStyle(numStyle);
			}
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close(); 
		
		return null;
	}
	
	/**
	 * 销售统计（部门）导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward salesByDept(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("部门销售统计.xls".getBytes("GBK"), "ISO8859_1"));
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		final String region = request.getParameter("region");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && !branchId.isEmpty() && Integer.valueOf(branchId) >= 0){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final CalcBillStatisticsDao.ExtraCond extraCond = new ExtraCond(DateType.HISTORY).setCalcByDuty(true);
		
		if(region != null && !region.equals("-1") && !region.isEmpty()){
			extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
		}
		
		if(opening != null && !opening.isEmpty()){
			HourRange hr = new HourRange(opening, ending, DateUtil.Pattern.HOUR);
			extraCond.setHourRange(hr);
		}
		
		extraCond.setDutyRange(new DutyRange(onDuty, offDuty));
		
		List<SalesDetail> list = SaleDetailsDao.getByDept(staff, extraCond);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("部门销售统计");
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
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("部门销售统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")");
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("营业额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("折扣额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("赠送额");
		cell.setCellStyle(headerStyle);
		
//		cell = row.createCell((int)row.getLastCellNum());
//		cell.setCellValue("成本");
//		cell.setCellStyle(headerStyle);
//		
//		cell = row.createCell((int)row.getLastCellNum());
//		cell.setCellValue("成本率");
//		cell.setCellStyle(headerStyle);
//		
//		cell = row.createCell((int)row.getLastCellNum());
//		cell.setCellValue("毛利");
//		cell.setCellStyle(headerStyle);
//		
//		cell = row.createCell((int)row.getLastCellNum());
//		cell.setCellValue("毛利率");
//		cell.setCellStyle(headerStyle);
		
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
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(temp.getIncome());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(temp.getDiscount());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(temp.getGifted());
				cell.setCellStyle(numStyle);
				
//				cell = row.createCell((int)row.getLastCellNum());
//				cell.setCellValue(temp.getCost());
//				cell.setCellStyle(numStyle);
//				
//				cell = row.createCell((int)row.getLastCellNum());
//				cell.setCellValue(temp.getCostRate());
//				cell.setCellStyle(numStyle);
//				
//				cell = row.createCell((int)row.getLastCellNum());
//				cell.setCellValue(temp.getProfit());
//				cell.setCellStyle(numStyle);
//				
//				cell = row.createCell((int)row.getLastCellNum());
//				cell.setCellValue(temp.getProfitRate());
//				cell.setCellStyle(numStyle);
			}
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close(); 
		
		return null;
	}
	
	
	/**
	 * 收款统计导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward businessReceips(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		response.setContentType("application/vnd.ms-excel;");
		response.addHeader("Content-Disposition","attachment;filename=" + new String("收款统计.xls".getBytes("GBK"), "ISO8859_1"));
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String regionId = request.getParameter("region");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY)
																				.setDutyRange(new DutyRange(onDuty, offDuty));
		
		if(regionId != null && !regionId.isEmpty()){
			extraCond.setRegion(Region.RegionId.valueOf(Integer.parseInt(regionId)));
		}
		
		final List<IncomeByEachDay> incomesByEachDay = CalcBillStatisticsDao.calcIncomeByEachDay(staff, extraCond);
		
		// 创建excel主页
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("收款统计");
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
		sheet.setColumnWidth(16, 3000);
		
		List<PayType> payTypeList = PayTypeDao.getByCond(staff, new PayTypeDao.ExtraCond().addType(PayType.Type.DESIGNED).addType(PayType.Type.EXTRA).addType(PayType.Type.MEMBER));		
		
		// 报表头
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 16));
		// 冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue("收款统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")");
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("应收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("实收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账单数");
		cell.setCellStyle(headerStyle);
		
		for (PayType payType : payTypeList) {
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(payType.getName());
			cell.setCellStyle(headerStyle);			
		}
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("折扣");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("赠送");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("退菜");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("抹数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("反结账");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("优惠劵");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("会员充值");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("会员退款");
		cell.setCellStyle(headerStyle);
		
		if(incomesByEachDay != null && incomesByEachDay.size() > 0){
			for(IncomeByEachDay item : incomesByEachDay){
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				//日期
				cell = row.createCell(0);
				cell.setCellValue(item.getDate());
				cell.setCellStyle(strStyle);
				
				//应收
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByPay().getTotalIncome());
				cell.setCellStyle(numStyle);
				
				//实收
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByPay().getTotalActual());
				cell.setCellStyle(numStyle);
				
				HSSFCellStyle ts = wb.createCellStyle();
				ts.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				ts.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				ts.setDataFormat(wb.createDataFormat().getFormat("0"));
				
				//账单数
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getTotalAmount());
				cell.setCellStyle(normalNumStyle);
				
				for(PayType payType : payTypeList) {
					String value = "0.00";
					for (PaymentIncome p : item.getIncomeByPay().getPaymentIncomes()) {
						if(p.getPayType().equals(payType)){
							value = NumericUtil.float2String2(p.getActual());
						}
					}	
					cell = row.createCell((int)row.getLastCellNum());
					cell.setCellValue(value);
					cell.setCellStyle(numStyle);							
				}
				
				//折扣额
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByDiscount().getTotalDiscount());
				cell.setCellStyle(numStyle);
				
				//赠送额
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByGift().getTotalGift());
				cell.setCellStyle(numStyle);
				
				//退菜额
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByCancel().getTotalCancel());
				cell.setCellStyle(numStyle);
				
				//抹数额
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByErase().getTotalErase());
				cell.setCellStyle(numStyle);
				
				//反结账
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByRepaid().getTotalRepaid());
				cell.setCellStyle(numStyle);
				
				//优惠券
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByCoupon().getTotalCoupon());
				cell.setCellStyle(numStyle);
				
				//会员充值
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByCharge().getTotalActualCharge());
				cell.setCellStyle(numStyle);
				
				//会员退款
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncomeByCharge().getTotalActualRefund());
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
	 * 营业统计导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward business(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		final String region = request.getParameter("region");
		
		final String dataType = request.getParameter("dataType");
		
		DateType dt = DateType.HISTORY;
		if(dataType == null || dt == null){
			return null;
		}
		
		response.addHeader("Content-Disposition","attachment;filename=" + new String(("营业汇总.xls").getBytes("GBK"), "ISO8859_1"));
		
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final CalcBillStatisticsDao.ExtraCond extraCond = new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY); 
		
		if(opening != null && !opening.isEmpty()){
			extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
		}
		
		if(region != null && !region.equals("-1")){
			extraCond.setRegion(RegionId.valueOf(Integer.parseInt(region)));
		}		
		
		DutyRange range = DutyRangeDao.exec(staff, onDuty, offDuty);
		if(range == null){
			range = new DutyRange(onDuty, offDuty);
		}
		
		final ShiftDetail	business = ShiftDao.getByRange(staff, range, extraCond);
		
		// 创建excel主页
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("营业汇总");
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
		cell.setCellValue("营业汇总(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")");
		cell.setCellStyle(titleStyle);
		// *****
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("统计时间: " + onDuty + " 至 " + offDuty + "     账单总数: " + business.getOrderAmount());
//		cell.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
				
		// *****
		// 导出操作相关信息 
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("导出时间: " + DateUtil.format(new Date()) + "     操作人:  " +staff.getName());
//		cell.setCellStyle(headerStyle);
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账单数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("应收总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("实收总额");
		cell.setCellStyle(headerStyle);
		
		List<PaymentIncome> paymentIncomes = business.getIncomeByPay().getPaymentIncomes();
		
		int totalAmount = 0;
		float totalShouldPay = 0;
		float totalActual = 0;
		
		for (PaymentIncome p : paymentIncomes) {
			totalAmount += p.getAmount();
			totalShouldPay += p.getTotal();
			totalActual += p.getActual();
			
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);		
			
			cell = row.createCell(0);
			cell.setCellValue(p.getPayType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(p.getAmount());
			cell.setCellStyle(normalNumStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(p.getTotal());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(p.getActual());
			cell.setCellStyle(numStyle);
		}
		
		// 合计
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("合计");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(totalAmount);
		cell.setCellStyle(normalNumStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(totalShouldPay);
		cell.setCellStyle(numStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(totalActual);
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账单数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("金额");
		cell.setCellStyle(headerStyle);

		// 尾数
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("尾数");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getRoundAmount());
		cell.setCellStyle(normalNumStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getRoundIncome());
		cell.setCellStyle(numStyle);
		
		// 抹数
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("抹数");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getEraseAmount());
		cell.setCellStyle(normalNumStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getEraseIncome());
		cell.setCellStyle(numStyle);
		
		// 折扣
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("折扣");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getDiscountAmount());
		cell.setCellStyle(normalNumStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getDiscountIncome());
		cell.setCellStyle(numStyle);
		
		// 赠送
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("赠送");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getGiftAmount());
		cell.setCellStyle(normalNumStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getGiftIncome());
		cell.setCellStyle(numStyle);
		
		// 退菜
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("退菜");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getCancelAmount());
		cell.setCellStyle(normalNumStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getCancelIncome());
		cell.setCellStyle(numStyle);
		
		// 反结帐
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("反结帐");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getPaidAmount());
		cell.setCellStyle(normalNumStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getPaidIncome());
		cell.setCellStyle(numStyle);
		
		// 优惠劵
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("优惠劵");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getCouponAmount());
		cell.setCellStyle(normalNumStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getCouponIncome());
		cell.setCellStyle(numStyle);
		
		// 服务费收入
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("服务费收入");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("--");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("现金");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("刷卡");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账户实充/扣额");
		cell.setCellStyle(headerStyle);
		
		
		// 会员充值
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("会员充值");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getActualCashCharge());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getActualCreditCardCharge());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getTotalAccountCharge());
		cell.setCellStyle(numStyle);
		
		// 会员退款
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("会员退款");
		cell.setCellStyle(strStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getTotalActualRefund());
		cell.setCellStyle(numStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(0.00);
		cell.setCellStyle(numStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue(business.getIncomeByCharge().getTotalAccountRefund());
		cell.setCellStyle(numStyle);
		
		// *****
		row = sheet.getRow(4);
		
		cell = row.createCell(5);
		cell.setCellValue("部门汇总");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("折扣总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("赠送总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("应收总额");
		cell.setCellStyle(headerStyle);
		
		if(business != null && business.getDeptIncome() != null){
			sheet.addMergedRegion(new CellRangeAddress(4, business.getDeptIncome().size() + 4, 4, 4));
			for(int i = 0; i < business.getDeptIncome().size(); i++){
				IncomeByDept item = business.getDeptIncome().get(i);
				row = sheet.getRow(i + 5);
				 
				cell = row.createCell(5);
				cell.setCellValue(item.getDept().getName());
				cell.setCellStyle(strStyle);
				 
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getDiscount());
				cell.setCellStyle(numStyle);
				 
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getGift());
				cell.setCellStyle(numStyle);
				 
				cell = row.createCell((int)row.getLastCellNum());
				cell.setCellValue(item.getIncome());
				cell.setCellStyle(numStyle);
				 
			}
		}
		
		if(business.getIncomeByBook().getIncome() > 0){
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 3));		
			
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue("预订总金额");
			cell.setCellStyle(headerStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(business.getIncomeByBook().getIncome());
			cell.setCellStyle(normalNumStyle);
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
		StockAction stockAction = StockActionDao.getById(staff, Integer.parseInt(id), true);
		
		String title = stockAction.getType().getDesc() + " -- " + stockAction.getCateType().getText() +  stockAction.getSubType().getText() + "单";
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( (stockAction.getType().getDesc() + "(" + stockAction.getCateType().getText() + stockAction.getSubType().getText() + "单).xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 5000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 5000);
		
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
	 * 导出盘点明细
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward stockTakeDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		String cateId = request.getParameter("cateId");
		String deptId = request.getParameter("deptId");
		String cateType = request.getParameter("cateType");
		
		String deptName = DepartmentDao.getById(staff, Integer.parseInt(deptId)).getName();
		
		List<StockTakeDetail> root = new ArrayList<StockTakeDetail>();
		if(cateId != null && !cateId.trim().isEmpty() && deptId != null){
			root = MaterialDeptDao.getStockTakeDetails(staff, Integer.parseInt(deptId), Integer.parseInt(cateType), Integer.parseInt(cateId), " ORDER BY MD.stock DESC");
		}
		
		String title = "盘点单明细";
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("盘点单明细.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 6000);
		sheet.setColumnWidth(1, 6000);
		sheet.setColumnWidth(2, 6000);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
		//报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("部门: " + deptName
				);

		cell.setCellStyle(headerDetailStyle);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 2));
		
		//空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 2));
		
		//列头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("货品名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("盘点数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("账面数");
		cell.setCellStyle(headerStyle);
		
		for (StockTakeDetail stockTakeDetail : root) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(stockTakeDetail.getMaterial().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue("");
			cell.setCellStyle(numStyle);
			
			cell = row.createCell(row.getLastCellNum());
			cell.setCellValue(stockTakeDetail.getExpectAmount());
			cell.setCellStyle(numStyle);			
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
		
	}	
	
	/**
	 * 会员充值/取款明细导出excel
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
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		
		final String fuzzy = request.getParameter("fuzzy");
		final String dataSource = request.getParameter("dataSources");
		final String memberType = request.getParameter("memberType");
		final String operateType = request.getParameter("operateType");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String detailOperate = request.getParameter("detailOperate");
		final String payType = request.getParameter("payType");
		final String isRefund = request.getParameter("isRefund");

		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final MemberOperationDao.ExtraCond extraCond;
		if(dataSource.equalsIgnoreCase("today")){
			extraCond = new MemberOperationDao.ExtraCond(DateType.TODAY);
		}else{
			extraCond = new MemberOperationDao.ExtraCond(DateType.HISTORY);
		}
		
		if(fuzzy != null && !fuzzy.trim().isEmpty()){
			List<Member> members = MemberDao.getByCond(staff, new MemberDao.ExtraCond().setFuzzyName(fuzzy), null);
			for (Member member : members) {
				extraCond.addMember(member);
			}
		}
		
		if(memberType != null && !memberType.trim().isEmpty()){
			extraCond.setMemberType(Integer.parseInt(memberType));
		}
		if(detailOperate != null && !detailOperate.trim().isEmpty() && Integer.valueOf(detailOperate) > 0){
			extraCond.addOperationType(OperationType.valueOf(Integer.parseInt(detailOperate)));
		}else{
			if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
				for(OperationType type : OperationType.typeOf(OperationCate.valueOf(Integer.parseInt(operateType)))){
					extraCond.addOperationType(type);
				}
			}
		}
		
		if(!payType.equals("-1")){
			extraCond.setChargeType(Integer.parseInt(payType));
		}
		
		if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
			extraCond.setOperateDate(new DutyRange(onDuty, offDuty));
		}
		
		String orderClause = " ORDER BY MO.id DESC ";

		final List<MemberOperation> list = MemberOperationDao.getByCond(staff, extraCond, orderClause);
		MemberOperation sum = MemberOperation.newMO(-10, "", "", "");
		if(!list.isEmpty()){
			sum.setChargeType(list.get(0).getChargeType());
			sum.setComment(list.get(0).getComment());
			sum.setOperationType(list.get(0).getOperationType());
			sum.setPayType(list.get(0).getPayType());
			sum.setOperateSeq(list.get(0).getOperateSeq());
			sum.setStaffName(list.get(0).getStaffName());
			for(MemberOperation temp : list){
				List<Member> members = MemberDao.getByCond(staff, new MemberDao.ExtraCond().setId(temp.getMemberId()), null);
				
				if(members.isEmpty()){
					MemberType delteMT = new MemberType(0);
					delteMT.setName("已删除会员");
					temp.getMember().setMemberType(delteMT);
				}else{
					temp.setMember(members.get(0));
				}
				
				sum.setDeltaBaseMoney(temp.getDeltaBaseMoney() + sum.getDeltaBaseMoney());
				sum.setDeltaExtraMoney(temp.getDeltaExtraMoney() + sum.getDeltaExtraMoney());
				sum.setChargeMoney(temp.getChargeMoney() + sum.getChargeMoney());
				sum.setPayMoney(temp.getPayMoney() + sum.getPayMoney());
				sum.setDeltaPoint(temp.getDeltaPoint() + sum.getDeltaPoint());
			}
		}
		
		final DecimalFormat df = new DecimalFormat("0.00");
		String title;
		if(isRefund != null){
			title = "会员取款明细表";
			response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("会员取款明细表.xls").getBytes("GBK"),  "ISO8859_1"));
		}else{
			title = "会员充值明细表";
			response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("会员充值明细表.xls").getBytes("GBK"),  "ISO8859_1"));
		}
		title += "(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")";
		
		//标题
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 3800);
		sheet.setColumnWidth(1, 3000);
		sheet.setColumnWidth(2, 3800);
		sheet.setColumnWidth(3, 3300);
		sheet.setColumnWidth(4, 4000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 6000);
		sheet.setColumnWidth(8, 4000);
		
		//冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		//报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
		
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
//		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		
		if(isRefund != null){
			cell.setCellValue("共 "+ list.size() +" 条充值记录" + "         总取款金额: " + df.format(sum.getChargeMoney()) + "         账户取款额 :" + df.format(sum.getDeltaTotalMoney()));
		}else{
			cell.setCellValue("共 "+ list.size() +" 条充值记录" + "         总收款金额: " + df.format(sum.getChargeMoney()) + "         账户充值额 :" + df.format(sum.getDeltaTotalMoney()));
		}
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		
		//空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		
		//列头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("会员名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("会员类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("手机号码");
		cell.setCellStyle(headerStyle);		
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("实收/实退");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("充值/退款");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("收款方式");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作时间");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作类型");
		cell.setCellStyle(headerStyle);
		
		for (MemberOperation mo : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(mo.getMemberName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getMember().getMemberType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getMemberMobile().isEmpty()? "----" : mo.getMemberMobile());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(df.format(mo.getChargeMoney()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(df.format(mo.getDeltaTotalMoney()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getChargeType() == null ? "现金" : mo.getChargeType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getStaffName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(DateUtil.format(mo.getOperateDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
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
	public ActionForward consumeDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dataSource = request.getParameter("dataSources");
		final String memberType = request.getParameter("memberType");
		final String operateType = request.getParameter("operateType");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String fuzzy = request.getParameter("fuzzy");
		final String payType = request.getParameter("payType");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		DateType dy;
		if(dataSource.equalsIgnoreCase("today")){
			dy = DateType.TODAY;
		}else{
			dy = DateType.HISTORY;
		}

		final MemberOperationDao.ExtraCond extraCond;
		if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
			if(OperationCate.valueOf(Integer.valueOf(operateType)) == OperationCate.CONSUME_TYPE){
				extraCond = new MemberOperationDao.ExtraCond4Consume(dy);
			}else{
				extraCond = new MemberOperationDao.ExtraCond(dy);
			}
		}else{
			extraCond = new MemberOperationDao.ExtraCond(dy);
		}
		
		if(fuzzy != null && !fuzzy.trim().isEmpty()){
			List<Member> members = MemberDao.getByCond(staff, new MemberDao.ExtraCond().setFuzzyName(fuzzy), null);
			for (Member member : members) {
				extraCond.addMember(member);
			}
		}
		
		if(memberType != null && !memberType.trim().isEmpty()){
			extraCond.setMemberType(Integer.parseInt(memberType));
		}
		
		if(!payType.equals("-1")){
			extraCond.setPayType(Integer.parseInt(payType));
		}
		
		if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
			extraCond.setOperateDate(new DutyRange(onDuty, offDuty));
		}
		
		String orderClause = " ORDER BY MO.id DESC ";
		final List<MemberOperation> list = MemberOperationDao.getByCond(staff, extraCond, orderClause);
		
		MemberOperation sum = MemberOperation.newMO(-10, "", "", "");
		if(list != null && !list.isEmpty()){
			sum.setChargeType(list.get(0).getChargeType());
			sum.setComment(list.get(0).getComment());
			sum.setOperationType(list.get(0).getOperationType());
			sum.setPayType(list.get(0).getPayType());
			sum.setOperateSeq(list.get(0).getOperateSeq());
			sum.setStaffName(list.get(0).getStaffName());
			for(MemberOperation temp : list){
				List<Member> members = MemberDao.getByCond(staff, new MemberDao.ExtraCond().setId(temp.getMemberId()), null);
				
				if(members.isEmpty()){
					MemberType delteMT = new MemberType(0);
					delteMT.setName("已删除会员");
					temp.getMember().setMemberType(delteMT);
				}else{
					temp.setMember(members.get(0));
				}
				
				sum.setDeltaBaseMoney(temp.getDeltaBaseMoney() + sum.getDeltaBaseMoney());
				sum.setDeltaExtraMoney(temp.getDeltaExtraMoney() + sum.getDeltaExtraMoney());
				sum.setChargeMoney(temp.getChargeMoney() + sum.getChargeMoney());
				sum.setPayMoney(temp.getPayMoney() + sum.getPayMoney());
				sum.setDeltaPoint(temp.getDeltaPoint() + sum.getDeltaPoint());
			}
		}
		
		DecimalFormat df = new DecimalFormat("0.00");
		String title = "会员消费明细表(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")";
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
		sheet.setColumnWidth(3, 5000);
		sheet.setColumnWidth(4, 4000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		sheet.setColumnWidth(8, 3000);
		sheet.setColumnWidth(9, 6000);
		
		//冻结行
		sheet.createFreezePane(0, 5, 0, 5);
		
		//报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));
		
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
//		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 9));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("共 " + list.size() + " 条消费记录" + "         总金额: " + df.format(sum.getPayMoney()) + "         总积分 :" + sum.getDeltaPoint());
//		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 9));
		
		//空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 9));
		
		//列头
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("单据号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("消费时间");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("会员名称");
		cell.setCellStyle(headerStyle);
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("手机号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("会员类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("付款方式");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("消费金额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("所得积分");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("备注");
		cell.setCellStyle(headerStyle);
		
		for (MemberOperation mo : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(mo.getOrderId());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(DateUtil.format(mo.getOperateDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getMemberName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getMemberMobile());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getMember().getMemberType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getPayType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(df.format(mo.getPayMoney()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(df.format(mo.getDeltaPoint()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getStaffName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(mo.getComment());
			cell.setCellStyle(strStyle);
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	/**
	 * 退菜统计导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward cancelledFood(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String dateBeg = request.getParameter("dateBeg");
		final String dateEnd = request.getParameter("dateEnd");
		final String deptId = request.getParameter("deptID");
		final String reasonId = request.getParameter("reasonID");
		final String staffId = request.getParameter("staffID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final OrderFoodDao.ExtraCond4CancelFood extraCond = new OrderFoodDao.ExtraCond4CancelFood(DateType.HISTORY);
		
		DutyRange range = DutyRangeDao.exec(staff, dateEnd, dateEnd);
		if(range == null){
			range = new DutyRange(dateBeg, dateEnd);
		}
		
		extraCond.setDutyRange(range);
		
		if(reasonId != null && !reasonId.isEmpty() && !reasonId.equals("-1")){
			extraCond.setReasonId(Integer.valueOf(reasonId));
		}
		
		if(deptId != null && deptId.isEmpty() && !deptId.equals("-1")){
			extraCond.setDeptId(Department.DeptId.valueOf(Integer.parseInt(deptId)));
		}
		
		if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
			extraCond.setStaffId(Integer.valueOf(staffId));
		}
		
		if(opening != null && !opening.isEmpty()){
			extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
		}
		
		final List<OrderFood> cancelList = OrderFoodDao.getSingleDetail(staff, extraCond, null);
		
		float totalAmount = 0, totalPrice = 0;
		
		for (OrderFood of : cancelList) {
			totalAmount += of.getCount();
			totalPrice += of.calcPrice();
		}
		
		String title = "退菜统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")" ;
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("退菜统计.xls").getBytes("GBK"),  "ISO8859_1"));
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
		
		cell.setCellValue("退菜数量: " + Math.abs(totalAmount) + "         退菜金额 :" + Math.abs(totalPrice));
//		cell.setCellStyle(strStyle);
		
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("菜名");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("部门");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("单价");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("退菜数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("退菜金额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("退菜原因");
		cell.setCellStyle(headerStyle);
		
		
		for (OrderFood orderFood : cancelList) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(DateUtil.format(orderFood.getOrderDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(orderFood.getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(orderFood.getKitchen().getDept().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(orderFood.getOrderId());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(orderFood.asFood().getPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(orderFood.getCount());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(orderFood.calcPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(orderFood.getWaiter());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(orderFood.getCancelReason().getReason());
			cell.setCellStyle(strStyle);
			
		}
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	/**
	 * 会员开卡统计导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward memberList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
		response.setContentType("application/vnd.ms-excel;");
		
		final String pin = (String) request.getAttribute("pin");
		final String memberCondId = request.getParameter("memberCondId");
		final String memberType = request.getParameter("memberType");
		final String memberCondMinConsume = request.getParameter("memberCondMinConsume");
		final String memberCondMaxConsume = request.getParameter("memberCondMaxConsume");
		final String memberCondMinAmount = request.getParameter("memberCondMinAmount");
		final String memberCondMaxAmount = request.getParameter("memberCondMaxAmount");
		final String memberCondMinBalance = request.getParameter("memberCondMinBalance");
		final String memberCondMaxBalance = request.getParameter("memberCondMaxBalance");
		final String memberCondBeginDate = request.getParameter("memberCondBeginDate");
		final String memberCondEndDate = request.getParameter("memberCondEndDate");
		final String memberCondMinFansAmount =request.getParameter("memberCondMinFansAmount");
		final String memberCondMaxFansAmount =request.getParameter("memberCondMaxFansAmount");
		final String memberCondMinCommission = request.getParameter("memberCondMinCommission");
		final String memebrCondMaxCommission = request.getParameter("memberCondMaxCommission");
		
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		final MemberCond memberCond;
		if(memberCondId != null && !memberCondId.isEmpty()){
			memberCond = MemberCondDao.getById(staff, Integer.parseInt(memberCondId));
		}else{
			memberCond = new MemberCond(-1);
			int minAmount = 0, maxAmount = 0;
			float minConsume = 0, maxConsume = 0, minBalance = 0, maxBalance = 0;
			
			//设置时间段
			memberCond.setRange(new DutyRange(memberCondBeginDate, memberCondEndDate));
			
			if(memberType != null && !memberType.isEmpty() && !memberType.equals("-1")){
				memberCond.setMemberType(new MemberType(Integer.parseInt(memberType)));
			}
			if(memberCondMinConsume != null && !memberCondMinConsume.isEmpty()){
				minConsume = Float.parseFloat(memberCondMinConsume);
			}
			if(memberCondMaxConsume != null && !memberCondMaxConsume.isEmpty()){
				maxConsume = Float.parseFloat(memberCondMaxConsume);
			}
			if(memberCondMinAmount != null && !memberCondMinAmount.isEmpty()){
				minAmount = Integer.parseInt(memberCondMinAmount);
			}
			if(memberCondMaxAmount != null && !memberCondMaxAmount.isEmpty()){
				maxAmount = Integer.parseInt(memberCondMaxAmount);
			}
			if(memberCondMinBalance != null && !memberCondMinBalance.isEmpty()){
				minBalance = Float.parseFloat(memberCondMinBalance);
			}
			if(memberCondMaxBalance != null && !memberCondMaxBalance.isEmpty()){
				maxBalance = Float.parseFloat(memberCondMaxBalance);
			}
			
			memberCond.setMaxBalance(maxBalance);
			memberCond.setMaxConsumeAmount(maxAmount);
			memberCond.setMaxConsumeMoney(maxConsume);
			memberCond.setMinBalance(minBalance);
			memberCond.setMinConsumeAmount(minAmount);
			memberCond.setMinConsumeMoney(minConsume);
			
			//粉丝数
			if(memberCondMinFansAmount != null && !memberCondMinFansAmount.isEmpty()){
				memberCond.setMinFansAmount(Integer.valueOf(memberCondMinFansAmount));
			}
			
			if(memberCondMaxFansAmount != null && !memberCondMaxFansAmount.isEmpty()){
				memberCond.setMaxFansAmount(Integer.valueOf(memberCondMaxFansAmount));
			}
			
			//佣金总额
			if(memberCondMinCommission != null && !memberCondMinCommission.isEmpty()){
				memberCond.setMinCommissionAmount(Float.valueOf(memberCondMinCommission));
			}
			
			if(memebrCondMaxCommission != null && !memebrCondMaxCommission.isEmpty()){
				memberCond.setMaxCommissionAmount(Float.valueOf(memebrCondMaxCommission));
			}
		}
			
			
		final List<Member> result =	MemberDao.getByCond(staff, new MemberDao.ExtraCond(memberCond), null);  
		
		final String title = "会员列表(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")";
		
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
		sheet.setColumnWidth(6, 4000);
		sheet.setColumnWidth(7, 3200);
		sheet.setColumnWidth(8, 4000);
		sheet.setColumnWidth(9, 4000);
		sheet.setColumnWidth(10, 3000);
		sheet.setColumnWidth(11, 3200);
		sheet.setColumnWidth(12, 4000);
		sheet.setColumnWidth(13, 4000);
		
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
		
		cell.setCellValue("会员数量: " + result.size());
//		cell.setCellStyle(strStyle);
		
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("年龄段");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("性别");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("粉丝数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("创建时间");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("消费次数");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("消费总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("累计积分");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("当前积分");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("总充值额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账户余额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("手机号码");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("会员卡号");
		cell.setCellStyle(headerStyle);
		
		for (Member member : result) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			//名称
			cell = row.createCell(0);
			cell.setCellValue(member.getName());
			cell.setCellStyle(strStyle);
			
			//类型
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getMemberType().getName());
			cell.setCellStyle(strStyle);
			
			//年龄段
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getAge().getVal());
			cell.setCellStyle(normalNumStyle);
			
			//性别
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getSex().getDesc());
			cell.setCellStyle(normalNumStyle);
			
			//粉丝数
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getFansAmount());
			cell.setCellStyle(normalNumStyle);
			
			//创建时间
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(DateUtil.format(member.getCreateDate()));
			cell.setCellStyle(normalNumStyle);
			
			//消费次数
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getConsumptionAmount());
			cell.setCellStyle(normalNumStyle);
			
			//消费总额
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getTotalConsumption());
			cell.setCellStyle(numStyle);
			
			//累计积分
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getTotalPoint());
			cell.setCellStyle(normalNumStyle);
			
			//当前积分
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getPoint());
			cell.setCellStyle(normalNumStyle);
			
			//总充值额
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getTotalCharge());
			cell.setCellStyle(numStyle);
			
			//账户余额
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getTotalBalance());
			cell.setCellStyle(numStyle);
			
			//手机号码
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getMobile());
			cell.setCellStyle(strStyle);
			
			//会员卡号
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(member.getMemberCard());
			cell.setCellStyle(strStyle);
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	/**
	 * 提成统计导出excel
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
	public ActionForward commissionStatisticsList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		response.setContentType("application/vnd.ms-excel;");
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String staffId = request.getParameter("staffId");
		final String deptId = request.getParameter("deptId");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final CalcCommissionStatisticsDao.ExtraCond extraCond = new CalcCommissionStatisticsDao.ExtraCond(DateType.HISTORY);
		
		if(staffId != null && !staffId.equals("-1") && !staffId.isEmpty()){
			extraCond.setStaffId(Integer.valueOf(staffId));
		}
		
		if(deptId != null && !deptId.equals("-1")){
			extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
		}
		final List<CommissionStatistics> list = CalcCommissionStatisticsDao.getCommissionStatisticsDetail(staff, new DutyRange(beginDate, endDate), extraCond);
		
		CommissionStatistics total = new CommissionStatistics();
		total.setCommission(0);
		if(!list.isEmpty() && list.size() != 0){
			for (CommissionStatistics item : list) {
//				total.setTotalPrice(item.getTotalPrice() + total.getTotalPrice());
				total.setCommission(item.getCommission() + total.getCommission());
			}
		}
		final String title = "提成统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")";
		
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
//		cell.setCellStyle(strStyle);
		
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
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("菜名");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("部门");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("单价");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("总额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("提成");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("人员");
		cell.setCellStyle(headerStyle);
		
		for (CommissionStatistics commission : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(DateUtil.format(commission.getOrderDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(commission.getFoodName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(commission.getDept().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(commission.getOrderId());
			cell.setCellStyle(normalNumStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(commission.getUnitPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(commission.getAmount());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(commission.getTotalPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(commission.getCommission());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(commission.getWaiter());
			cell.setCellStyle(strStyle);
			
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	/**
	 * 抹数统计导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward eraseStatisticsList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("application/vnd.ms-excel;");
		
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String staffId = request.getParameter("staffID");
		final String deptId = request.getParameter("deptID");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final CalcEraseStatisticsDao.ExtraCond extraCond = new CalcEraseStatisticsDao.ExtraCond(DateType.HISTORY)
																				    .setDutyRange(new DutyRange(beginDate, endDate))
																				    .setCalcByDuty(true);
		
		if(staffId != null && !staffId.equals("-1") && !staffId.isEmpty()){
			extraCond.setStaffId(Integer.valueOf(staffId));
		}
		
		if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
			extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
		}
		
		if(opening != null && !opening.isEmpty()){
			extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
		}
		
		List<Order> result = CalcEraseStatisticsDao.getDetail(staff, extraCond);
		
		String title = "抹数统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")"; 
		
		int totalErasePrice = 0;
		for (Order item : result) {
			totalErasePrice += item.getErasePrice();
		}
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("抹数统计.xls").getBytes("GBK"),  "ISO8859_1"));
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
		sheet.setColumnWidth(5, 7000);
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("日期: " + beginDate + "  至  " + endDate +  "         抹数总额: " + totalErasePrice);
//		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 5));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 5));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("抹数额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("实收金额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("备注");
		cell.setCellStyle(headerStyle);
		
		for (Order order : result) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			//日期
			cell = row.createCell(0);
			cell.setCellValue(DateUtil.format(order.getOrderDate()));
			cell.setCellStyle(strStyle);
			
			//账单号
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getId());
			cell.setCellStyle(strStyle);
			
			//抹数额
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getErasePrice());
			cell.setCellStyle(numStyle);
			
			//实收金额
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getActualPrice());
			cell.setCellStyle(numStyle);
			
			//操作人
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getWaiter());
			cell.setCellStyle(strStyle);
			
			//备注
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getComment());
			cell.setCellStyle(strStyle);
			
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}	
	
	/**
	 * 赠送统计导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward giftStatisticsList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("application/vnd.ms-excel;");
		
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String region = request.getParameter("region");
		final String foodName = request.getParameter("foodName");
		final String giftStaffId = request.getParameter("staffId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final OrderFoodDao.ExtraCond extraCond = new OrderFoodDao.ExtraCond(DateType.HISTORY);
		
		extraCond.setGift(true);
		
		if(region != null && !region.equals("-1")){
			extraCond.setRegionId(RegionId.valueOf(Integer.parseInt(region)));
		}
		
		if(foodName != null && !foodName.trim().isEmpty()){
			extraCond.setFoodName(foodName);
		}
		
		if(giftStaffId != null && !giftStaffId.isEmpty() && !giftStaffId.equals("-1")){
			extraCond.setStaffId(Integer.parseInt(giftStaffId));
		}
		if(opening != null && !opening.isEmpty()){
			extraCond.setHourRange(new HourRange(opening, ending, DateUtil.Pattern.HOUR));
		}
		
		DutyRange range = DutyRangeDao.exec(staff, beginDate, endDate);
		if(range != null){
			extraCond.setDutyRange(range);
		}else{
			extraCond.setDutyRange(new DutyRange(beginDate, endDate));
		}
		
		final List<OrderFood> list = OrderFoodDao.getSingleDetail(staff, extraCond, null);
		
		float totalGiftPrice = 0;
		if(!list.isEmpty()){
			for (OrderFood item : list) {
				totalGiftPrice += item.getFoodPrice();
			}
		}
		
		String title = "赠送统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")"; 
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("赠送统计.xls").getBytes("GBK"),  "ISO8859_1"));
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
		sheet.setColumnWidth(5, 7000);
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("日期: " + beginDate + "  至  " + endDate +  "         赠送总额: " + totalGiftPrice);
//		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 5));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 5));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("菜品名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("单价");
		cell.setCellStyle(headerStyle);

		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("总价");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("赠送人");
		cell.setCellStyle(headerStyle);
		
		for (OrderFood of : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			//账单号
			cell = row.createCell(0);
			cell.setCellValue(of.getOrderId());
			cell.setCellStyle(strStyle);
			
			//日期
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(DateUtil.format(of.getOrderDate()));
			cell.setCellStyle(strStyle);
			
			//菜品名称
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(of.asFood().getName());
			cell.setCellStyle(strStyle);
			
			//数量
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(of.getCount());
			cell.setCellStyle(numStyle);
			
			//单价
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(of.getPrice());
			cell.setCellStyle(numStyle);
			
			//总价
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(of.getPrice() * of.getCount());
			cell.setCellStyle(numStyle);
			
			//赠送人
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(of.getWaiter());
			cell.setCellStyle(strStyle);
			
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}	
	
	/**
	 * 折扣统计导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward discountStatisticsList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("application/vnd.ms-excel;");
		
		final String pin = (String) request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String staffId = request.getParameter("staffID");
		final String deptId = request.getParameter("deptID");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final CalcDiscountStatisticsDao.ExtraCond extraCond = new CalcDiscountStatisticsDao.ExtraCond(DateType.HISTORY)
																					.setDutyRange(new DutyRange(beginDate, endDate))
																					;
		
		if(staffId != null && !staffId.equals("-1") && !staffId.isEmpty()){
			extraCond.setStaffId(Integer.valueOf(staffId));
		}
		
		if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
			extraCond.setDeptId(DeptId.valueOf(Integer.parseInt(deptId)));
		}
		
		List<Order> list = CalcDiscountStatisticsDao.getDetail(staff, extraCond);
		
		float totalDiscountPrice = 0;
		if(!list.isEmpty()){
			for (Order item : list) {
				totalDiscountPrice += item.getDiscountPrice();
			}
		}
		
		String title = "折扣统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")"; 
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("折扣统计.xls").getBytes("GBK"),  "ISO8859_1"));
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
		sheet.setColumnWidth(5, 7000);
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("日期: " + beginDate + "  至  " + endDate +  "         折扣总额: " + totalDiscountPrice);
//		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 5));
//----------------		
//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 5));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("折扣额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("实收金额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("操作人");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("备注");
		cell.setCellStyle(headerStyle);
		
		for (Order order : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(DateUtil.format(order.getOrderDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getId());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getDiscountPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.calcTotalPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getActualPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getWaiter());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(order.getComment());
			cell.setCellStyle(strStyle);
			
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}	
	
	

	/**
	 * 反结账明细导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward repaidStatisticsList(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("application/vnd.ms-excel;");
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		
		final String beginDate = request.getParameter("beginDate");
		final String endDate = request.getParameter("endDate");
		final String staffId = request.getParameter("staffId");
		final String opening = request.getParameter("opening");
		final String ending = request.getParameter("ending");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final CalcRepaidStatisticsDao.ExtraCond extraCond = new CalcRepaidStatisticsDao.ExtraCond(DateType.HISTORY)
																					   .setDutyRange(new DutyRange(beginDate, endDate))
																					   .setCalcByCond(true);
		
		if(opening != null && !opening.isEmpty()){
			extraCond.setHourRange(new HourRange(opening, ending));
		}
		
		if(staffId != null && !staffId.isEmpty() && !staffId.equals("-1")){
			extraCond.setStaffId(Integer.valueOf(staffId));
		}
		
		final List<RepaidStatistics> list = CalcRepaidStatisticsDao.getRepaidIncomeDetail(staff, extraCond);
		
		final String title = "反结账统计(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")";
		
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
//		cell.setCellStyle(strStyle);
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 8));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("反结账时间");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("人员");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("原应收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("原实收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("反结账金额");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("现应收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("现实收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("付款方式");
		cell.setCellStyle(headerStyle);
		
		for (RepaidStatistics repaid : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(DateUtil.format(repaid.getmOrderDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(repaid.getStaff().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(repaid.getmId());
			cell.setCellStyle(normalNumStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(NumericUtil.roundFloat(repaid.getTotalPrice() - repaid.getRepaidPrice()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(NumericUtil.roundFloat(repaid.getActualPrice() - repaid.getRepaidPrice()));
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(repaid.getRepaidPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(repaid.getTotalPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(repaid.getActualPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(repaid.getPaymentType().getName());
			cell.setCellStyle(strStyle);
			
		}
		
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		
		return null;
	}
	
	/**
	 * 历史账单导出excel
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward historyOrder(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		response.setContentType("application/vnd.ms-excel;");
		
		final String dateType = request.getParameter("dataType");
		final String dateBeg = request.getParameter("dateBeg");
		final String dateEnd = request.getParameter("dateEnd");
		
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");

		final String businessHourBeg = request.getParameter("opening");
		final String businessHourEnd = request.getParameter("ending");
		
		final String comboType = request.getParameter("havingCond");
		final String orderId = request.getParameter("orderId");
		final String seqId = request.getParameter("seqId");
		final String type = request.getParameter("type");
		final String tableAlias = request.getParameter("tableAlias");
		final String tableName = request.getParameter("tableName");
		final String region = request.getParameter("region");
		String comment = request.getParameter("common");
		// 中文乱码
		if(comment != null && !comment.isEmpty()){
			comment = new String(request.getParameter("common").getBytes("ISO8859_1"), "UTF-8");
		}

		Staff staff = StaffDao.verify(Integer.parseInt(pin));

		if(branchId != null && !branchId.isEmpty()){
			staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
		}
		
		final OrderDao.ExtraCond extraCond = new OrderDao.ExtraCond(DateType.valueOf(Integer.parseInt(dateType)));
		
		if(comboType != null && !comboType.trim().isEmpty()){
			int comboVal = Integer.valueOf(comboType);
			if(comboVal == 1){
				//是否有反结帐
				extraCond.isRepaid(true);
			}else if(comboVal == 2){
				//是否有折扣
				extraCond.isDiscount(true);
			}else if(comboVal == 3){
				//是否有赠送
				extraCond.isGift(true);
			}else if(comboVal == 4){
				//是否有退菜
				extraCond.isCancelled(true);
			}else if(comboVal == 5){
				//是否有抹数
				extraCond.isErased(true);
			}else if(comboVal == 6){
				//是否有优惠劵
				extraCond.isCoupon(true);
			}
		}
		
		if(orderId != null && !orderId.isEmpty()){
			extraCond.setOrderId(Integer.parseInt(orderId));
		}
		if(seqId != null && !seqId.isEmpty()){
			extraCond.setSeqId(Integer.parseInt(seqId));
		}
		
		if(Boolean.parseBoolean(type)){
			String comboPayType = request.getParameter("comboPayType");
			
			if(comboPayType != null && !comboPayType.equals("-1")){
				//按结帐方式
				extraCond.setPayType(new com.wireless.pojo.dishesOrder.PayType(Integer.parseInt(comboPayType)));
			}
		}
		if(comment != null && !comment.isEmpty()){
			extraCond.setComment(comment);
		}
		if(tableAlias != null && !tableAlias.isEmpty()){
			extraCond.setTableAlias(Integer.parseInt(tableAlias));
		}
		if(tableName != null && !tableName.isEmpty()){
			extraCond.setTableName(tableName);
		}
		if(region != null && !region.equals("-1")){
			extraCond.setRegionId(Region.RegionId.valueOf(Short.parseShort(region)));
		}
		if(dateBeg != null && !dateBeg.isEmpty()){
			DutyRange orderRange = DutyRangeDao.exec(staff, dateBeg, dateEnd);
			if(orderRange == null){
				orderRange = new DutyRange(dateBeg, dateEnd);
			}
			extraCond.setOrderRange(orderRange);
		}
		
		if(businessHourBeg != null && !businessHourBeg.isEmpty()){
			HourRange hr = new HourRange(businessHourBeg, businessHourEnd, DateUtil.Pattern.HOUR);
			extraCond.setHourRange(hr);
		}
		
		String orderClause = " ORDER BY "+ extraCond.orderTblAlias +".order_date ASC ";
		
		final List<Order> list = OrderDao.getByCond(staff, extraCond, orderClause);
		
		final String title = "历史账单(" + RestaurantDao.getById(staff.getRestaurantId()).getName() + ")";
		
		//标题
		response.addHeader("Content-Disposition", "attachment;filename=" + new String( ("历史账单.xls").getBytes("GBK"),  "ISO8859_1"));
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		initParams(wb);
		
		sheet.setColumnWidth(0, 3000);
		sheet.setColumnWidth(1, 4000);
		sheet.setColumnWidth(2, 5000);
		sheet.setColumnWidth(3, 6000);
		sheet.setColumnWidth(4, 3000);
		sheet.setColumnWidth(5, 3000);
		sheet.setColumnWidth(6, 3000);
		sheet.setColumnWidth(7, 3000);
		sheet.setColumnWidth(8, 3000);
		sheet.setColumnWidth(9, 3000);
		sheet.setColumnWidth(10, 6000);
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
		//------------------报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
		//---------------摘要------------------		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		
		cell.setCellValue("账单数量: " + list.size());
		
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		//----------------空白
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 10));
		
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("账单号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("台号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("区域");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("日期");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("账单类型");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("结账方式");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("收款方式");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("应收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("实收");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("状态");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("备注");
		cell.setCellStyle(headerStyle);
		
		for (Order o : list) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			cell = row.createCell(0);
			cell.setCellValue(o.getId());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			if(!o.getDestTbl().getName().isEmpty()){
				cell.setCellValue(o.getDestTbl().getAliasId() + "(" + o.getDestTbl().getName() + ")");
			}else{
				cell.setCellValue(o.getDestTbl().getAliasId());
			}
			
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(o.getDestTbl().getRegion().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(DateUtil.format(o.getOrderDate()));
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(o.getCategory().getDesc());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(o.getSettleType().getDesc());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(o.getPaymentType().getName());
			cell.setCellStyle(strStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(o.getTotalPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(o.getActualPrice());
			cell.setCellStyle(numStyle);
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(o.getStatus().getDesc());
			cell.setCellStyle(strStyle);
			
			
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(o.getComment().length() > 8 ? o.getComment().substring(0, 8) + "..." : o.getComment());
			cell.setCellStyle(strStyle);
		}
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		return null;
	}
	
	/**
	 * 进销存汇总导出excel
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
	public ActionForward stockCollect(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, Exception, SQLException, BusinessException{
		
		response.setContentType("application/vnd.ms-excel;");
		
		String pin = (String)request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		String beginDate = request.getParameter("beginDate");
		String materialId = request.getParameter("materialId");
		String deptId = request.getParameter("deptId");
		
		final StockReportDao.ExtraCond extraCond = new StockReportDao.ExtraCond();
		
		if(beginDate != null){
			extraCond.setRange(beginDate);
		}else{
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.MONTH, -1);
			extraCond.setRange(new SimpleDateFormat("yyyy-MM").format(c.getTime()));
		}
		
		if(materialId != null && !materialId.equals("-1") && !materialId.trim().isEmpty()){
			extraCond.setMaterial(Integer.parseInt(materialId));
		}
		
		if(deptId != null && !deptId.isEmpty() && !deptId.equals("-1")){
			extraCond.setDept(Integer.parseInt(deptId));
		}
		
		List<StockReport> stockReports = StockReportDao.getByCond(staff, extraCond);
		
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
		
		cell.setCellValue("日期: " + beginDate + "         物料个数: " + stockReports.size());
//		cell.setCellStyle(strStyle);
		
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
		cell.setCellValue("品行编号");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("品行名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("期初数量");
		cell.setCellStyle(headerStyle);

		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库采购");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库调拨");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库报溢");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库盘盈");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("入库小计");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库退货");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库调拨");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库报损");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库盘亏");
		cell.setCellStyle(headerStyle);

		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库消耗");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("出库小计");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("期末数量");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("期末单价");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell((int)row.getLastCellNum());
		cell.setCellValue("期末金额");
		cell.setCellStyle(headerStyle);
		
		for (StockReport s : stockReports) {
			row = sheet.createRow(sheet.getLastRowNum() + 1);
			row.setHeight((short) 350);
			
			//品行编号
			cell = row.createCell(0);
			cell.setCellValue(s.getMaterial().getId());
			cell.setCellStyle(strStyle);
			
			//品行名称
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getMaterial().getName());
			cell.setCellStyle(strStyle);
			
			//期初数量
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getPrimeAmount());
			cell.setCellStyle(numStyle);
			
			//入库采购
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockIn());
			cell.setCellStyle(numStyle);
			
			//入库调拨
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockInTransfer());
			cell.setCellStyle(numStyle);
			
			//入库报溢
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockSpill());
			cell.setCellStyle(numStyle);
			
			//入库盘盈
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockTakeMore());
			cell.setCellStyle(numStyle);
			
			//入库小计
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockInAmount());
			cell.setCellStyle(numStyle);
			
			//出库退货
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockOut());
			cell.setCellStyle(numStyle);
			
			//出库调拨
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockOutTransfer());
			cell.setCellStyle(numStyle);
			
			//出库报损
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockDamage());
			cell.setCellStyle(numStyle);
			
			//出库盘亏
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockTakeLess());
			cell.setCellStyle(numStyle);

			//出库消耗
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getUseUp());
			cell.setCellStyle(numStyle);
			
			//出库小计
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getStockOutAmount());
			cell.setCellStyle(numStyle);
			
			//期末数量
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getFinalAmount());
			cell.setCellStyle(numStyle);
			
			//期末单价
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getFinalPrice());
			cell.setCellStyle(numStyle);
			
			//期末金额
			cell = row.createCell((int)row.getLastCellNum());
			cell.setCellValue(s.getFinalMoney());
			cell.setCellStyle(numStyle);
		}
		OutputStream os = response.getOutputStream();
		wb.write(os);
		os.flush();
		os.close();
		return null;
	}
	
	
	/**
	 * 参与活动会员导出
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward promotionMember(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("application/vnd.ms-excel;");
		
		String pId = request.getParameter("pId");
		String status = request.getParameter("status");
		Staff staff = StaffDao.verify(Integer.parseInt((String)request.getAttribute("pin")));
		
		CouponDao.ExtraCond extra = new CouponDao.ExtraCond();
		extra.setPromotion(Integer.parseInt(pId));
		
		if(status != null && !status.isEmpty()){
			extra.setStatus(Coupon.Status.valueOf(Integer.parseInt(status)));
		}
		
		List<Coupon> list = CouponDao.getByCond(staff, extra.setOnlyAmount(false), null);
		
		
		//标题
		String title = "参与活动会员统计";
		response.addHeader("Content-Disposition","attachment;filename=" + new String((title+".xls").getBytes("GBK"), "ISO8859_1"));

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(title);
		HSSFRow row = null;
		HSSFCell cell = null;
		// 初始化参数,重要
		initParams(wb);
		
		sheet.setColumnWidth(0, 5000);
		sheet.setColumnWidth(1, 3500);
		sheet.setColumnWidth(2, 3500);
		
		//冻结行
		sheet.createFreezePane(0, 4, 0, 4);
		
		// 报表头
		row = sheet.createRow(0);
		row.setHeight((short) 550);
		cell = row.createCell(0);
		cell.setCellValue(title);
		cell.setCellStyle(titleStyle);
		//合并单元格
		sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
				
		// 摘要
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		cell = row.createCell(0);
		cell.setCellValue("总数: " + list.size());
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 2));
				
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		sheet.addMergedRegion(new CellRangeAddress(sheet.getLastRowNum(), sheet.getLastRowNum(), 0, 2));
		
		// *****
		row = sheet.createRow(sheet.getLastRowNum() + 1);
		row.setHeight((short) 350);
		
		cell = row.createCell(0);
		cell.setCellValue("会员名称");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("手机号码");
		cell.setCellStyle(headerStyle);
		
		cell = row.createCell(row.getLastCellNum());
		cell.setCellValue("状态");
		cell.setCellStyle(headerStyle);
		
		if(list != null && list.size() > 0){
			for(int i = 0; i < list.size(); i++){
				
				row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.setHeight((short) 350);
				
				cell = row.createCell(0);
				cell.setCellValue(list.get(i).getMember().getName());
				cell.setCellStyle(strStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(list.get(i).getMember().getMobile());
				cell.setCellStyle(numStyle);
				
				cell = row.createCell(row.getLastCellNum());
				cell.setCellValue(list.get(i).getStatus().getDesc());
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
