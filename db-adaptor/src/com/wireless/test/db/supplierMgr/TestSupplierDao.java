package com.wireless.test.db.supplierMgr;
import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.test.db.TestInit;

public class TestSupplierDao {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try{
			mStaff = StaffDao.getAdminByRestaurant(37);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	//期望值与真实值比较
	private void compare(Supplier expected, Supplier actual){
		assertEquals("supplierid", expected.getId(), actual.getId());
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
		
		Supplier actual = SupplierDao.getSupplierById(mStaff, supplierId);
		
		compare(insupplier, actual);
		
	}
	
	
	@Test
	public void testSupplierDao() throws Exception{
		Supplier insupplier = new Supplier(mStaff.getRestaurantId(), "eozu", "12334", "内环路二号", "wang", "bedly");
		final int supplierId = SupplierDao.insert(insupplier);
		
		insupplier.setSupplierid(supplierId);
		
		Supplier actual = SupplierDao.getSupplierById(mStaff, supplierId);
		
		compare(insupplier, actual);
		
		insupplier = actual;
	
		insupplier.setName("鸣人");
		insupplier.setTele("888888888");
		insupplier.setAddr("番禺区星光大道");
		insupplier.setContact("佐助");
		insupplier.setComment("good.");
		
		SupplierDao.update(mStaff, insupplier);
	    actual = SupplierDao.getSupplierById(mStaff, insupplier.getId());
		
		compare(insupplier,actual);
		
		SupplierDao.deleteById(mStaff, supplierId);

		try{
			SupplierDao.getSupplierById(mStaff, supplierId);
		}catch(Exception e){}
		
	}
	
	@Test
	public void testDeleteById() throws Exception{

		SupplierDao.deleteById(mStaff, 1);

		try{
			SupplierDao.getSupplierById(mStaff, 1);
		}catch(Exception e){}
	
	}
	

	

	
	
	
	

	
}
