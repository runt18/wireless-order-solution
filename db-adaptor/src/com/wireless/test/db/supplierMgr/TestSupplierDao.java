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
		Supplier.InsertBuilder insupplier = new Supplier.InsertBuilder();
		
		int supplierId = SupplierDao.insert(insupplier);
		
		
		Supplier actual = SupplierDao.getById(mStaff, supplierId);
		
		compare(insupplier.build(), actual);
		
	}
	
	
	@Test
	public void testSupplierDao() throws Exception{
		Supplier.InsertBuilder insupplier = new Supplier.InsertBuilder();
		final int supplierId = SupplierDao.insert(insupplier);
		
		Supplier actual = SupplierDao.getById(mStaff, supplierId);
		
		compare(insupplier.build(), actual);
		
	
		Supplier.UpdateBuilder updateBuilder = new Supplier.UpdateBuilder(supplierId);
		updateBuilder.setName("鸣人");
		updateBuilder.setTele("888888888");
		updateBuilder.setAddr("番禺区星光大道");
		updateBuilder.setContact("佐助");
		updateBuilder.setComment("good.");
		
		SupplierDao.update(mStaff, updateBuilder);
	    actual = SupplierDao.getById(mStaff, updateBuilder.getId());
		
		compare(updateBuilder.build(),actual);
		
		SupplierDao.deleteById(mStaff, supplierId);

		try{
			SupplierDao.getById(mStaff, supplierId);
		}catch(Exception e){}
		
	}
	
	@Test
	public void testDeleteById() throws Exception{

		SupplierDao.deleteById(mStaff, 1);

		try{
			SupplierDao.getById(mStaff, 1);
		}catch(Exception e){}
	
	}
	

	

	
	
	
	

	
}
