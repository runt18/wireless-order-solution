package com.wireless.test.db.printScheme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.printScheme.PrintFuncDao;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.printScheme.PrintFunc.Builder;
import com.wireless.pojo.printScheme.PrintFunc.DetailBuilder;
import com.wireless.pojo.printScheme.PrintFunc.SummaryBuilder;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.SortedList;
import com.wireless.test.db.TestInit;

public class TestPrinterScheme {
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(37).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void compare(Printer expected, Printer actual){
		assertEquals("printer id", expected.getId(), actual.getId());
		assertEquals("printer name", expected.getName(), actual.getName());
		assertEquals("printer alias", expected.getAlias(), actual.getAlias());
		assertEquals("printer style", expected.getStyle().getVal(), actual.getStyle().getVal());
		assertEquals("printer enabled", expected.isEnabled(), actual.isEnabled());
		
		//Compare the associated print functions
		List<PrintFunc> expectedFuncs = SortedList.newInstance(expected.getPrintFuncs());
		List<PrintFunc> actualFuncs = SortedList.newInstance(actual.getPrintFuncs());
		assertEquals(expectedFuncs.size(), actualFuncs.size());
		
		for(int i = 0; i < expectedFuncs.size(); i++){
			assertEquals("print function type", expectedFuncs.get(i).getType().getVal(), actualFuncs.get(i).getType().getVal());
			assertEquals("print function repeat", expectedFuncs.get(i).getRepeat(), actualFuncs.get(i).getRepeat());
			
			if(expectedFuncs.get(i).getType().isSummary()){
				//Compare the department if the print type is summary
				assertEquals("department to summary", expectedFuncs.get(i).getDepartment(), actualFuncs.get(i).getDepartment());
			}
			
			if(expectedFuncs.get(i).getType().isDetail()){
				//Compare the kitchens if the print type is detail
				assertEquals("kitchens to detail", expectedFuncs.get(i).getKitchens(), actualFuncs.get(i).getKitchens());
			}
			
			//Compare the regions
			assertEquals("regions to print type", expectedFuncs.get(i).getRegions(), actualFuncs.get(i).getRegions());
		}
	}
	
	@Test
	public void testPrintScheme() throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		int printerId = -1;
		try{
			dbCon.connect();
			
			List<Department> depts = DepartmentDao.getByType(dbCon, mStaff, Department.Type.NORMAL);
			List<Kitchen> kitchens = KitchenDao.getByType(dbCon, mStaff, Kitchen.Type.NORMAL);
			List<Region> regions = RegionDao.getByStatus(dbCon, mStaff, Region.Status.BUSY);
			
			Printer.InsertBuilder builder = new Printer.InsertBuilder("GP-80250-200", PStyle.PRINT_STYLE_58MM, mStaff.getRestaurantId())
													   .setAlias("海鲜打印机");
			//Add a new printer
			printerId = PrinterDao.insert(dbCon, mStaff, builder);
			
			//下单
			PrintFunc.SummaryBuilder summaryFuncBuilder = SummaryBuilder.newPrintOrder()
																	   .setRepeat(2)
																	   .addRegion(regions.get(0))
																	   .addRegion(regions.get(1))
																	   .addDepartment(depts.get(0))
																	   .addDepartment(depts.get(1));
			
			//Add a summary function to this printer
			int summaryFuncId = PrintFuncDao.addFunc(dbCon, mStaff, printerId, summaryFuncBuilder);
			
			//Add a detail function to this printer(下单详细)
			PrintFunc.DetailBuilder detailFuncBuilder = DetailBuilder.newPrintFoodDetail()
																	 .setRepeat(2)
																	 .addKitchen(kitchens.get(0))
																	 .addKitchen(kitchens.get(1));
			int detailFuncId = PrintFuncDao.addFunc(dbCon, mStaff, printerId, detailFuncBuilder);
			
			//退菜
			PrintFunc.SummaryBuilder allCancelledFoodBuilder = SummaryBuilder.newAllCancelledFood()
																			.setRepeat(3)
																			.addRegion(regions.get(1))
																			.addRegion(regions.get(2))
																			.addDepartment(depts.get(1))
																			.addDepartment(depts.get(2));
			
			int allCancelledFoodId = PrintFuncDao.addFunc(dbCon, mStaff, printerId, allCancelledFoodBuilder);
			
			//退菜详细
			PrintFunc.DetailBuilder cancelledFoodBuilder = DetailBuilder.newCancelledFood()
																		.setRepeat(3)
																		.addKitchen(kitchens.get(3))
																		.addKitchen(kitchens.get(4));
			int CancelledFoodId = PrintFuncDao.addFunc(dbCon, mStaff, printerId, cancelledFoodBuilder);
			
			//结账
			PrintFunc.Builder  receiptBuilder = Builder.newReceipt()
														.setRepeat(3)
														.addRegion(regions.get(0))
														.addRegion(regions.get(4));
			int receiptId = PrintFuncDao.addFunc(dbCon, mStaff, printerId, receiptBuilder);
			
			//暂结
			PrintFunc.Builder tempReceiptBuilder = Builder.newTempReceipt()
															.setRepeat(2)
															.addRegion(regions.get(0))
															.addRegion(regions.get(2));
			int tempReceiptId = PrintFuncDao.addFunc(dbCon, mStaff, printerId, tempReceiptBuilder);
			
			//转台
			PrintFunc.Builder transferTableBuilder = Builder.newTransferTable()
															.setRepeat(3)
															.addRegion(regions.get(2))
															.addRegion(regions.get(3));
			
			int transferTableId = PrintFuncDao.addFunc(dbCon, mStaff, printerId, transferTableBuilder);
			
			//催菜
			PrintFunc.Builder allHurriedFoodBuilder = Builder.newAllHurriedFood()
															.setRepeat(1)
															.addRegion(regions.get(2))
															.addRegion(regions.get(3));
			int allHurriedFoodId = PrintFuncDao.addFunc(dbCon, mStaff, printerId, allHurriedFoodBuilder);
			
			
			Printer expected = builder.build();
			expected.setId(printerId);
			
			PrintFunc summaryFunc = summaryFuncBuilder.build();
			summaryFunc.setId(summaryFuncId);
			expected.addFunc(summaryFunc);
			
			PrintFunc detailFunc = detailFuncBuilder.build();
			detailFunc.setId(detailFuncId);
			expected.addFunc(detailFunc);
			
			PrintFunc allCancelledFood = allCancelledFoodBuilder.build();
			allCancelledFood.setId(allCancelledFoodId);
			expected.addFunc(allCancelledFood);
			
			PrintFunc cancelledFood = cancelledFoodBuilder.build();
			cancelledFood.setId(CancelledFoodId);
			expected.addFunc(cancelledFood);
			
			PrintFunc receipt = receiptBuilder.build();
			receipt.setId(receiptId);
			expected.addFunc(receipt);
			
			PrintFunc tempReceipt = tempReceiptBuilder.build();
			tempReceipt.setId(tempReceiptId);
			expected.addFunc(tempReceipt);
			
			PrintFunc transferTable = transferTableBuilder.build();
			tempReceipt.setId(transferTableId);
			expected.addFunc(transferTable);
			
			PrintFunc allHurriedFood = allHurriedFoodBuilder.build();
			tempReceipt.setId(allHurriedFoodId);
			expected.addFunc(allHurriedFood);
			
			//Compare after insertion
			compare(expected, PrinterDao.getPrinterById(dbCon, mStaff, printerId));
			
			//Remove the summary function
			PrintFuncDao.removeFunc(dbCon, mStaff, summaryFuncId);
			
			expected.removeFunc(summaryFunc);
			
			PrintFuncDao.removeFunc(dbCon, mStaff, allCancelledFoodId);
			
			expected.removeFunc(allCancelledFood);
			
			PrintFuncDao.removeFunc(dbCon, mStaff, receiptId);
			
			expected.removeFunc(receipt);
			
			//Compare after deletion
			compare(expected, PrinterDao.getPrinterById(dbCon, mStaff, printerId));

			//Update the printer
			Printer.UpdateBuilder updateBuilder = new Printer.UpdateBuilder(printerId, "GP-80250-201", PStyle.PRINT_STYLE_80MM)
															 .setAlias("中厨打印机").setEnabled(false);
			PrinterDao.update(dbCon, mStaff, updateBuilder);
			
			expected.setName("GP-80250-201");
			expected.setStyle(PStyle.PRINT_STYLE_80MM);
			expected.setAlias("中厨打印机");
			expected.setEnabled(false);
			//Compare after update printer
			compare(expected, PrinterDao.getPrinterById(dbCon, mStaff, printerId));
			
		}finally{
			
			//delete the printer just created
			if(printerId > 0){
				PrinterDao.deleteById(dbCon, mStaff, printerId);
				
				try{
					PrinterDao.getPrinterById(dbCon, mStaff, printerId);
					assertTrue("fail to delete printer", false);
				}catch(BusinessException ignored){}
				
				assertTrue("fail to delete print functions", PrintFuncDao.getFuncByPrinterId(dbCon, printerId).isEmpty());
			}
			
			dbCon.disconnect();
		}
	}
}
