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
import com.wireless.pojo.printScheme.PType;
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
			mStaff = StaffDao.getAdminByRestaurant(40);
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
			assertEquals("print function comment", expectedFuncs.get(i).getComment(), actualFuncs.get(i).getComment());
			
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
			
			final List<Department> depts = DepartmentDao.getByType(dbCon, mStaff, Department.Type.NORMAL);
			final List<Kitchen> kitchens = KitchenDao.getByType(dbCon, mStaff, Kitchen.Type.NORMAL);
			final List<Region> regions = RegionDao.getByStatus(dbCon, mStaff, Region.Status.BUSY);
			
			Printer.InsertBuilder builder = new Printer.InsertBuilder("GP-80250-200-test", PStyle.PRINT_STYLE_58MM)
													   .setAlias("海鲜打印机");
			//Add a new printer
			printerId = PrinterDao.insert(dbCon, mStaff, builder);
			
			//点菜总单
			PrintFunc.SummaryBuilder summaryFuncBuilder = SummaryBuilder.newExtra(printerId, true)
																	   .setRepeat(2)
																	   .addRegion(regions.get(0))
																	   .addRegion(regions.get(1))
																	   .addDepartment(depts.get(0))
																	   .addDepartment(depts.get(1))
																	   .setComment("测试备注");
			
			//Add a summary function to this printer
			int summaryFuncId = PrintFuncDao.addFunc(dbCon, mStaff, summaryFuncBuilder);
			
			//退菜总单
			int cancelSummaryFuncId = PrintFuncDao.getByCond(dbCon, mStaff, new PrintFuncDao.ExtraCond().setPrinter(printerId).setType(PType.PRINT_ALL_CANCELLED_FOOD)).get(0).getId();
			
			//点菜分单
			PrintFunc.DetailBuilder detailFuncBuilder = DetailBuilder.newExtra(printerId, true)
																	 .setRepeat(2)
																	 .addKitchen(kitchens.get(0))
																	 .addKitchen(kitchens.get(1));
			int detailFuncId = PrintFuncDao.addFunc(dbCon, mStaff, detailFuncBuilder);
			
			//退菜分单
			int cancelledDetailFuncId = PrintFuncDao.getByCond(dbCon, mStaff, new PrintFuncDao.ExtraCond().setPrinter(printerId).setType(PType.PRINT_CANCELLED_FOOD_DETAIL)).get(0).getId();
			
			//结账
			PrintFunc.Builder  receiptBuilder = Builder.newReceipt(printerId)
														.setRepeat(3)
														.addRegion(regions.get(0))
														.addRegion(regions.get(4));
			int receiptId = PrintFuncDao.addFunc(dbCon, mStaff, receiptBuilder);
			
			//暂结
			PrintFunc.Builder tempReceiptBuilder = Builder.newTempReceipt(printerId)
															.setRepeat(2)
															.addRegion(regions.get(0))
															.addRegion(regions.get(2));
			int tempReceiptId = PrintFuncDao.addFunc(dbCon, mStaff, tempReceiptBuilder);
			
			//转台
			PrintFunc.Builder transferTableBuilder = Builder.newTransferTable(printerId)
															.setRepeat(3)
															.addRegion(regions.get(2))
															.addRegion(regions.get(3));
			
			int transferTableId = PrintFuncDao.addFunc(dbCon, mStaff, transferTableBuilder);
			
			//催菜
			PrintFunc.Builder allHurriedFoodBuilder = Builder.newAllHurriedFood(printerId)
															.setRepeat(1)
															.addRegion(regions.get(2))
															.addRegion(regions.get(3));
			int allHurriedFoodId = PrintFuncDao.addFunc(dbCon, mStaff, allHurriedFoodBuilder);
			
			
			Printer expected = builder.build();
			expected.setId(printerId);
			
			PrintFunc summaryExtraFunc = summaryFuncBuilder.build();
			summaryExtraFunc.setType(PType.PRINT_ORDER);
			summaryExtraFunc.setId(summaryFuncId);
			expected.addFunc(summaryExtraFunc);
			
			PrintFunc summaryCancelFunc = summaryFuncBuilder.build();
			summaryCancelFunc.setType(PType.PRINT_ALL_CANCELLED_FOOD);
			summaryCancelFunc.setId(cancelSummaryFuncId);
			expected.addFunc(summaryCancelFunc);
			
			PrintFunc detailFunc = detailFuncBuilder.build();
			detailFunc.setType(PType.PRINT_ORDER_DETAIL);
			detailFunc.setId(detailFuncId);
			expected.addFunc(detailFunc);
			
			PrintFunc cancelledFood = detailFuncBuilder.build();
			cancelledFood.setType(PType.PRINT_CANCELLED_FOOD_DETAIL);
			cancelledFood.setId(cancelledDetailFuncId);
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
			compare(expected, PrinterDao.getById(dbCon, mStaff, printerId));
			
			//------------Update the summary function-----------------
			PrintFunc.UpdateBuilder summaryUpdateBuilder = new PrintFunc.UpdateBuilder(summaryFuncId)
																		.setRepeat(3)
																		.addDepartment(depts.get(1))
																		.setRegionAll();
			PrintFuncDao.updateFunc(dbCon, mStaff, summaryUpdateBuilder);
			expected.removeFunc(summaryExtraFunc);
			if(summaryUpdateBuilder.isDeptChanged()){
				summaryExtraFunc.setDepartments(summaryUpdateBuilder.build().getDepartment());
			}
			if(summaryUpdateBuilder.isKitchenChanged()){
				summaryExtraFunc.setKitchens(summaryUpdateBuilder.build().getKitchens());
			}
			if(summaryUpdateBuilder.isRegionChanged()){
				summaryExtraFunc.setRegions(summaryUpdateBuilder.build().getRegions());
			}
			if(summaryUpdateBuilder.isRepeatChanged()){
				summaryExtraFunc.setRepeat(summaryUpdateBuilder.build().getRepeat());
			}
			//Compare after update summary function
			expected.addFunc(summaryExtraFunc);
			compare(expected, PrinterDao.getById(dbCon, mStaff, printerId));
			
			//------------Remove the summary function---------------------------------
			PrintFuncDao.deleteById(dbCon, mStaff, summaryFuncId);
			
			expected.removeFunc(summaryExtraFunc);
			expected.removeFunc(summaryCancelFunc);
			
			PrintFuncDao.deleteById(dbCon, mStaff, receiptId);
			
			expected.removeFunc(receipt);
			
			//Compare after deletion
			compare(expected, PrinterDao.getById(dbCon, mStaff, printerId));

			//Update the printer
			Printer.UpdateBuilder updateBuilder = new Printer.UpdateBuilder(printerId)
															 .setName("GP-80250-201-test")
															 .setStyle(PStyle.PRINT_STYLE_80MM)
															 .setAlias("中厨打印机").setEnabled(false);
			PrinterDao.update(dbCon, mStaff, updateBuilder);
			
			if(updateBuilder.isNameChanged()){
				expected.setName(updateBuilder.build().getName());
			}
			if(updateBuilder.isStyleChanged()){
				expected.setStyle(updateBuilder.build().getStyle());
			}
			if(updateBuilder.isAliasChanged()){
				expected.setAlias(updateBuilder.build().getAlias());
			}
			if(updateBuilder.isEnabledChanged()){
				expected.setEnabled(updateBuilder.build().isEnabled());
			}
			//Compare after update printer
			compare(expected, PrinterDao.getById(dbCon, mStaff, printerId));
			
		}finally{
			
			//delete the printer just created
			if(printerId > 0){
				PrinterDao.deleteById(dbCon, mStaff, printerId);
				
				try{
					PrinterDao.getById(dbCon, mStaff, printerId);
					assertTrue("fail to delete printer", false);
				}catch(BusinessException ignored){}
				
				assertTrue("fail to delete print functions", PrintFuncDao.getByCond(dbCon, mStaff, new PrintFuncDao.ExtraCond().setPrinter(printerId)).isEmpty());
			}
			
			dbCon.disconnect();
		}
	}
}
