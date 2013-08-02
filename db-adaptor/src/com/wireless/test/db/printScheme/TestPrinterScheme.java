package com.wireless.test.db.printScheme;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.printScheme.PrintFuncDao;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.regionMgr.RegionDao;
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
import com.wireless.pojo.util.SortedList;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestPrinterScheme {
	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
		try {
			mTerminal = VerifyPin.exec(229, Terminal.MODEL_STAFF);
		} catch (BusinessException e) {
			e.printStackTrace();
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
			
			List<Department> depts = DepartmentDao.getDepartments(dbCon, mTerminal, null, null);
			List<Kitchen> kitchens = KitchenDao.getKitchens(dbCon, mTerminal, null, null);
			List<Region> regions = RegionDao.getRegions(dbCon, mTerminal, null, null);
			
			Printer.InsertBuilder builder = new Printer.InsertBuilder("GP-80250-200", PStyle.PRINT_STYLE_58MM, mTerminal.restaurantID)
													   .setAlias("海鲜打印机");
			//Add a new printer
			printerId = PrinterDao.insert(dbCon, mTerminal, builder);
			
			//下单
			PrintFunc.SummaryBuilder summaryFuncBuilder = SummaryBuilder.newPrintOrder()
																	   .setRepeat(2)
																	   .addRegion(regions.get(0))
																	   .addRegion(regions.get(1))
																	   .setDepartment(depts.get(0));
			
			//Add a summary function to this printer
			int summaryFuncId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, summaryFuncBuilder);
			
			//Add a detail function to this printer(下单详细)
			PrintFunc.DetailBuilder detailFuncBuilder = DetailBuilder.newPrintFoodDetail()
																	 .setRepeat(2)
																	 .addKitchen(kitchens.get(0))
																	 .addKitchen(kitchens.get(1));
			int detailFuncId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, detailFuncBuilder);
			
			//退菜
			PrintFunc.SummaryBuilder allCancelledFoodBuilder = SummaryBuilder.newAllCancelledFood()
																			.setRepeat(3)
																			.addRegion(regions.get(1))
																			.addRegion(regions.get(2))
																			.setDepartment(depts.get(1));
			int allCancelledFoodId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, allCancelledFoodBuilder);
			
			//退菜详细
			PrintFunc.DetailBuilder cancelledFoodBuilder = DetailBuilder.newCancelledFood()
																		.setRepeat(3)
																		.addKitchen(kitchens.get(3))
																		.addKitchen(kitchens.get(4));
			int CancelledFoodId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, cancelledFoodBuilder);
			
			//结账
			PrintFunc.Builder  receiptBuilder = Builder.newReceipt()
														.setRepeat(3)
														.addRegion(regions.get(0))
														.addRegion(regions.get(4));
			int receiptId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, receiptBuilder);
			
			//暂结
			PrintFunc.Builder tempReceiptBuilder = Builder.newTempReceipt()
															.setRepeat(2)
															.addRegion(regions.get(0))
															.addRegion(regions.get(2));
			int tempReceiptId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, tempReceiptBuilder);
			
			//转台
			PrintFunc.Builder transferTableBuilder = Builder.newTransferTable()
															.setRepeat(3)
															.addRegion(regions.get(2))
															.addRegion(regions.get(5));
			
			int transferTableId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, transferTableBuilder);
			
			//催菜
			PrintFunc.Builder allHurriedFoodBuilder = Builder.newAllHurriedFood()
															.setRepeat(1)
															.addRegion(regions.get(4))
															.addRegion(regions.get(6));
			int allHurriedFoodId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, allHurriedFoodBuilder);
			
			
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
			transferTable.setId(transferTableId);
			expected.addFunc(transferTable);
			
			PrintFunc allHurriedFood = allHurriedFoodBuilder.build();
			allHurriedFood.setId(allHurriedFoodId);
			expected.addFunc(allHurriedFood);
			
			//Compare after insertion
			compare(expected, PrinterDao.getPrinterById(dbCon, mTerminal, printerId));
			
			//Remove the summary function
			//PrintFuncDao.removeFunc(dbCon, mTerminal, summaryFuncId);
			
			//expected.removeFunc(summaryFunc);
			
			PrintFuncDao.removeFunc(dbCon, mTerminal, allCancelledFoodId);
			
			expected.removeFunc(allCancelledFood);
			
			PrintFuncDao.removeFunc(dbCon, mTerminal, receiptId);
			
			expected.removeFunc(receipt);
			
			//Compare after deletion
			compare(expected, PrinterDao.getPrinterById(dbCon, mTerminal, printerId));

			//Update the printer
			Printer.UpdateBuilder updateBuilder = new Printer.UpdateBuilder(printerId, "GP-80250-500", PStyle.PRINT_STYLE_58MM)
															 .setAlias("甜品打印机").setEnabled(false);
			PrinterDao.update(dbCon, mTerminal, updateBuilder);
			
			expected.setName("GP-80250-500");
			expected.setStyle(PStyle.PRINT_STYLE_58MM);
			expected.setAlias("甜品打印机");
			//Compare after update printer
			compare(expected, PrinterDao.getPrinterById(dbCon, mTerminal, printerId));
			
		}finally{
			
			//delete the printer just created
			if(printerId < 0){
				PrinterDao.deleteById(dbCon, mTerminal, printerId);
				
				try{
					PrinterDao.getPrinterById(dbCon, mTerminal, printerId);
					assertTrue("fail to delete printer", false);
				}catch(BusinessException ignored){}
				
				assertTrue("fail to delete print functions", PrintFuncDao.getFuncByPrinterId(dbCon, printerId).isEmpty());
			}
			
			dbCon.disconnect();
		}
	}
}
