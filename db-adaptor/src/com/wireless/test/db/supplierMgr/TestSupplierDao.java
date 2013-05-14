package com.wireless.test.db.supplierMgr;
import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestSupplierDao {

	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDBParam(){
		TestInit.init();
		try{
			mTerminal = VerifyPin.exec(217, Terminal.MODEL_STAFF);
		}catch(SQLException e){
			e.printStackTrace();
		}catch(BusinessException e){
			e.printStackTrace();
		}
	}
	//期望值与真实值比较
	private void compare(Supplier expected, Supplier actual){
		Assert.assertEquals("supplierid", expected.getSupplierId(), actual.getSupplierId());
		System.out.println("restaurantid"+expected.getRestaurantId()+actual.getRestaurantId());
		Assert.assertEquals("restaurantid", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("name", expected.getName(), actual.getName());
		Assert.assertEquals("tele", expected.getTele(), actual.getTele());
		Assert.assertEquals("addr", expected.getAddr(), actual.getAddr());
	}
	
	@Test
	public void testDeleteById() throws Exception{

		
		Supplier actual = SupplierDao.getSupplierById(mTerminal, 132);
		
		SupplierDao.deleteById(mTerminal, actual.getSupplierId());
		
		
	}
	
	@Test
	public void testUpdate() throws Exception{
		Supplier upsupplier = SupplierDao.getSuppliers(mTerminal, null, null).get(0);		
		upsupplier.setName("鸣人3");
		
		SupplierDao.update(mTerminal, upsupplier);
		
		Supplier actual = SupplierDao.getSupplierById(mTerminal, upsupplier.getSupplierId());
		
		compare(upsupplier,actual);
		
	}
	
	@Test
	public void testInsert() throws Exception{
		Supplier insupplier = new Supplier(37, "xiaoli", "12334", "内环路二号", "wang", "bedly");
		
		int supplierId = SupplierDao.insert(insupplier);
		insupplier.setSupplierid(supplierId);
		
		Supplier actual = SupplierDao.getSupplierById(mTerminal, supplierId);
		
		compare(insupplier, actual);
		
	}
	
	
	
	

	
}
