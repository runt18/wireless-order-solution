package com.wireless.test.db.printScheme;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

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
		Assert.assertEquals("printer id", expected.getId(), actual.getId());
		Assert.assertEquals("printer name", expected.getName(), actual.getName());
		Assert.assertEquals("printer alias", expected.getAlias(), actual.getAlias());
		Assert.assertEquals("printer style", expected.getStyle().getVal(), actual.getStyle().getVal());
		
		//Compare the associated print functions
		List<PrintFunc> expectedFuncs = SortedList.newInstance(expected.getPrintFuncs());
		List<PrintFunc> actualFuncs = SortedList.newInstance(actual.getPrintFuncs());
		Assert.assertEquals(expectedFuncs.size(), actualFuncs.size());
		
		for(int i = 0; i < expectedFuncs.size(); i++){
			Assert.assertEquals("print function type", expectedFuncs.get(i).getType().getVal(), actualFuncs.get(i).getType().getVal());
			Assert.assertEquals("print function repeat", expectedFuncs.get(i).getRepeat(), actualFuncs.get(i).getRepeat());
			
			if(expectedFuncs.get(i).getType().isSummary()){
				//Compare the department if the print type is summary
				Assert.assertEquals("department to summary", expectedFuncs.get(i).getDepartment(), actualFuncs.get(i).getDepartment());
			}
			
			if(expectedFuncs.get(i).getType().isDetail()){
				//Compare the kitchens if the print type is detail
				Assert.assertEquals("kitchens to detail", expectedFuncs.get(i).getKitchens(), actualFuncs.get(i).getKitchens());
			}
			
			//Compare the regions
			Assert.assertEquals("regions to print type", expectedFuncs.get(i).getRegions(), actualFuncs.get(i).getRegions());
		}
	}
	
	@Test
	public void testPrintScheme() throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			List<Department> depts = DepartmentDao.getDepartments(dbCon, mTerminal, null, null);
			List<Kitchen> kitchens = KitchenDao.getKitchens(dbCon, mTerminal, null, null);
			List<Region> regions = RegionDao.getRegions(dbCon, mTerminal, null, null);
			
			Printer.InsertBuilder builder = new Printer.InsertBuilder("GP-80250-200", PStyle.PRINT_STYLE_58MM, mTerminal.restaurantID);
			
			//Add a new printer
			int printerId = PrinterDao.insert(dbCon, mTerminal, builder);

			PrintFunc.SummaryBuilder summaryFuncBuilder = new PrintFunc.SummaryBuilder()
																	   .setRepeat(2)
																	   .addRegion(regions.get(0))
																	   .addRegion(regions.get(1))
																	   .setDepartment(depts.get(0));
			
			//Add a summary function to this printer
			int summaryFuncId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, summaryFuncBuilder);
			
			//Add a detail function to this printer
			PrintFunc.DetailBuilder detailFuncBuilder = new PrintFunc.DetailBuilder()
																	 .setRepeat(2)
																	 .addKitchen(kitchens.get(0))
																	 .addKitchen(kitchens.get(1));
			int detailFuncId = PrintFuncDao.addFunc(dbCon, mTerminal, printerId, detailFuncBuilder);
			
			Printer expected = builder.build();
			expected.setId(printerId);
			
			PrintFunc summaryFunc = summaryFuncBuilder.build();
			summaryFunc.setId(summaryFuncId);
			expected.addFunc(summaryFunc);
			
			PrintFunc detailFunc = detailFuncBuilder.build();
			detailFunc.setId(detailFuncId);
			expected.addFunc(detailFunc);
			
			//Compare after insertion
			compare(expected, PrinterDao.getPrinters(dbCon, mTerminal).get(0));
			
			//Remove the summary function
			PrintFuncDao.removeFunc(dbCon, mTerminal, summaryFuncId);
			
			expected.removeFunc(summaryFunc);
			
			//Compare after deletion
			compare(expected, PrinterDao.getPrinters(dbCon, mTerminal).get(0));
			
			//delete the printer just created
			PrinterDao.deleteById(dbCon, printerId);
			Assert.assertTrue("fail to delete printer", PrinterDao.getPrinters(dbCon, mTerminal).isEmpty());
			
		}finally{
			dbCon.disconnect();
		}
	}
}
