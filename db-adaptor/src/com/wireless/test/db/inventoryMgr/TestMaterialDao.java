package com.wireless.test.db.inventoryMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Date;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;


public class TestMaterialDao {
	
	private static Staff staff;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			staff = StaffDao.getAdminByRestaurant(26);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void insert() throws SQLException{
		int id = 0;
		try{
			//test insert
			Material.InsertBuilder insertBuilder = new Material.InsertBuilder();
			id = MaterialDao.insert(staff, insertBuilder);
			
			Material expected = insertBuilder.build();
			expected.setId(id);
			Material actual = MaterialDao.getById(staff, id);
			compare(expected, actual);
			
			//test update
			final MaterialCate cate = new MaterialCate(1);
			final float price = 10;
			final float delta = 10;
			final float stock = 10;
			final String name = "测试";
			final String lastModStaff = "管理员";
			final long lastModDate = new Date().getTime();
			final String pinyin = "test";
			final boolean isGood = false;
//			/final int alarmAmount = 10;
			
			Material.UpdateBuilder updateBuilder = new Material.UpdateBuilder(id)
															   .setMaterialCate(cate)
															   .setPrice(price)
															   .setDelta(delta)
															   .setStock(stock)
															   .setName(name)
															   .setLastModStaff(lastModStaff)
															   .setLastModDate(lastModDate)
															   .setPinYin(pinyin)
															   .setIsGood(isGood);
			MaterialDao.update(staff, updateBuilder);
			MaterialDao.update(staff, updateBuilder);
			expected = updateBuilder.build();
			actual = MaterialDao.getById(staff, id);
			compare(expected, actual);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			MaterialDao.deleteByCond(staff, new MaterialDao.ExtraCond().setId(id));
			Assert.assertTrue("failed to delete the material", MaterialDao.getByCond(staff, new MaterialDao.ExtraCond().setId(id)).isEmpty());
		}
	}
	
	private void compare(Material expect, Material actual){
		Assert.assertEquals("id", expect.getId(), actual.getId());
		Assert.assertEquals("price", expect.getPrice(), actual.getPrice(), 0.01);
		Assert.assertEquals("delta", expect.getDelta(), actual.getPrice(), 0.01);
		Assert.assertEquals("stock", expect.getStock(), actual.getStock(), 0.01);
		Assert.assertEquals("name", expect.getName(), actual.getName());
		Assert.assertEquals("lastModStaff", expect.getLastModStaff(), actual.getLastModStaff());
		Assert.assertEquals("lastModDate", expect.getLastModDate(), actual.getLastModDate());
		Assert.assertEquals("pinyin", expect.getPinyin(), actual.getPinyin());
		Assert.assertEquals("isGood", expect.isGood(), actual.isGood());
//		Assert.assertEquals("alarmAmount", expect.getAlarmAmount(), actual.getAlarmAmount());
		Assert.assertEquals("cate", expect.getCate(), actual.getCate());
	}
	
}
