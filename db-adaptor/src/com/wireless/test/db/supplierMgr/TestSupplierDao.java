package com.wireless.test.db.supplierMgr;
import java.beans.PropertyVetoException;
import java.sql.SQLException;

import static org.junit.Assert.*;

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
	public static void initDBParam() throws PropertyVetoException{
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
		assertEquals("supplierid", expected.getSupplierId(), actual.getSupplierId());
		assertEquals("restaurantid", expected.getRestaurantId(), actual.getRestaurantId());
		assertEquals("name", expected.getName(), actual.getName());
		assertEquals("tele", expected.getTele(), actual.getTele());
		assertEquals("addr", expected.getAddr(), actual.getAddr());
	}
	
	@Test
	public void testInsert() throws Exception{
		Supplier insupplier = new Supplier(26, "美宜佳", "12334", "内环路二号", "wang", "bedly");
		
		int supplierId = SupplierDao.insert(insupplier);
		
		insupplier.setSupplierid(supplierId);
		
		Supplier actual = SupplierDao.getSupplierById(mTerminal, supplierId);
		
		compare(insupplier, actual);
		
	}
	
	
	@Test
	public void testSupplierDao() throws Exception{
		Supplier insupplier = new Supplier(mTerminal.restaurantID, "eozu", "12334", "内环路二号", "wang", "bedly");
		final int supplierId = SupplierDao.insert(insupplier);
		
		insupplier.setSupplierid(supplierId);
		
		Supplier actual = SupplierDao.getSupplierById(mTerminal, supplierId);
		
		compare(insupplier, actual);
		
		insupplier = actual;
	
		insupplier.setName("鸣人");
		insupplier.setTele("888888888");
		insupplier.setAddr("番禺区星光大道");
		insupplier.setContact("佐助");
		insupplier.setComment("good.");
		
		SupplierDao.update(mTerminal, insupplier);
	    actual = SupplierDao.getSupplierById(mTerminal, insupplier.getSupplierId());
		
		compare(insupplier,actual);
		
		SupplierDao.deleteById(mTerminal, supplierId);

		try{
			SupplierDao.getSupplierById(mTerminal, supplierId);
		}catch(Exception e){}
		
	}
	
	@Test
	public void testDeleteById() throws Exception{

		SupplierDao.deleteById(mTerminal, 1);

		try{
			SupplierDao.getSupplierById(mTerminal, 1);
		}catch(Exception e){}
	
	}
	

	

	
	
	
	

	
}
