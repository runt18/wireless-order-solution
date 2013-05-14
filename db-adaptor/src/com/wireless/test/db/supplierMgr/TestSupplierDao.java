package com.wireless.test.db.supplierMgr;
import java.sql.SQLException;
import java.util.List;

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
		Assert.assertEquals("restaurantid", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("name", expected.getName(), actual.getName());
		Assert.assertEquals("tele", expected.getTele(), actual.getTele());
		Assert.assertEquals("addr", expected.getAddr(), actual.getAddr());
	}
	@Test
	public void testSupplierDao() throws Exception{
		int supplierId = testInsert();
		
		testUpdate(supplierId);
		
		testDeleteById(supplierId);
		
	}
	
	
	
	
	private int testInsert() throws Exception{
		Supplier insupplier = new Supplier(37, "zuozu", "12334", "内环路二号", "wang", "bedly");
		
		int supplierId = SupplierDao.insert(insupplier);
		
		insupplier.setSupplierid(supplierId);
		
		Supplier actual = SupplierDao.getSupplierById(mTerminal, supplierId);
		
		compare(insupplier, actual);
		
		return supplierId;
		
	}
	
	private  void testUpdate(int supplierId) throws Exception{
		Supplier upsupplier = SupplierDao.getSuppliers(mTerminal, " AND supplier_id = " + supplierId, null).get(0);
		 		
		upsupplier.setName("鸣人2");
		upsupplier.setTele("888888888");
		upsupplier.setAddr("番禺区星光大道");
		upsupplier.setContact("佐助");
		upsupplier.setComment("good");
		
		SupplierDao.update(mTerminal, upsupplier);
		
		Supplier actual = SupplierDao.getSupplierById(mTerminal, upsupplier.getSupplierId());
		
		compare(upsupplier,actual);
		
	}
	
	private void testDeleteById(int supplierId) throws Exception{

		SupplierDao.deleteById(mTerminal, supplierId);

		try{
			SupplierDao.getSupplierById(mTerminal, supplierId);
		}catch(Exception e){}
		
		
	}
	

	

	
	
	
	

	
}
