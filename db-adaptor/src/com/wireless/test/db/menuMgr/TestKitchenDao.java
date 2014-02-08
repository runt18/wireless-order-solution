package com.wireless.test.db.menuMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.SortedList;
import com.wireless.test.db.TestInit;

public class TestKitchenDao {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(26).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testKitchenDao() throws SQLException, BusinessException{
		int kitchenId = 0;
		try{
			//Add a kitchen.
			Kitchen.AddBuilder addBuilder = new Kitchen.AddBuilder("测试厨房", Department.DeptId.DEPT_1).setAllowTmp(true);
			kitchenId = KitchenDao.add(mStaff, addBuilder);
			
			Kitchen expected = addBuilder.build();
			Kitchen actual = KitchenDao.getById(mStaff, kitchenId);
			
			Assert.assertEquals("id : add kitchen", kitchenId, actual.getId());
			Assert.assertEquals("name : add kitchen", expected.getName(), actual.getName());
			Assert.assertEquals("dept : add kitchen", expected.getDept().getId(), actual.getDept().getId());
			Assert.assertEquals("type : add kitchen", Kitchen.Type.NORMAL, actual.getType());
			Assert.assertEquals("restaurant : add kitchen", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("is allow tmp : add kitchen", expected.isAllowTemp(), actual.isAllowTemp());
			
			//Update a kitchen.
			Kitchen.UpdateBuilder updateBuilder = new Kitchen.UpdateBuilder(kitchenId).setName("测试厨房2").setDeptId(Department.DeptId.DEPT_2).setAllowTmp(false);
			KitchenDao.update(mStaff, updateBuilder);
			expected = updateBuilder.build();
			actual = KitchenDao.getById(mStaff, kitchenId);
			
			Assert.assertEquals("id : update kitchen", kitchenId, actual.getId());
			Assert.assertEquals("name : update kitchen", expected.getName(), actual.getName());
			Assert.assertEquals("dept : update kitchen", expected.getDept().getId(), actual.getDept().getId());
			Assert.assertEquals("type : update kitchen", Kitchen.Type.NORMAL, actual.getType());
			Assert.assertEquals("restaurant : update kitchen", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("is allow tmp : update kitchen", expected.isAllowTemp(), actual.isAllowTemp());
			
		}finally{
			if(kitchenId != 0){
				KitchenDao.remove(mStaff, kitchenId);
				Kitchen kitchenRemoved = KitchenDao.getById(mStaff, kitchenId);
				Assert.assertEquals("type : failed to remove kitchen", kitchenRemoved.getType(), Kitchen.Type.IDLE);
				Assert.assertEquals("dept : failed to remove kitchen", kitchenRemoved.getDept().getId(), Department.DeptId.DEPT_NULL.getVal());
				Assert.assertEquals("is allow temp : failed to remove kitchen", kitchenRemoved.isAllowTemp(), false);
			}
		}
	}
	
	@Test
	public void testKitchenMove() throws SQLException, BusinessException{
		List<Kitchen> kitchens = KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL);
		testKitchenMove(kitchens.get(3),
						kitchens.get(0));
		
		kitchens = KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL);
		testKitchenMove(kitchens.get(0),
						kitchens.get(3));
		
		kitchens = KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL);
		testKitchenMove(kitchens.get(3),
						kitchens.get(kitchens.size() - 1));
		
		kitchens = KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL);
		testKitchenMove(kitchens.get(kitchens.size() - 1),
						kitchens.get(3));
		
		kitchens = KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL);
		testKitchenMove(kitchens.get(0),
						kitchens.get(kitchens.size() - 1));
		
		kitchens = KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL);
		testKitchenMove(kitchens.get(kitchens.size() - 1),
						kitchens.get(0));
		
		kitchens = KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL);
		testKitchenMove(kitchens.get(3),
						kitchens.get(5));
		
		kitchens = KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL);
		testKitchenMove(kitchens.get(5),
						kitchens.get(3));
	}
	
	private void testKitchenMove(Kitchen fromK, Kitchen toK) throws SQLException, BusinessException{
		List<Kitchen> expected = SortedList.newInstance(new Comparator<Kitchen>(){
			@Override
			public int compare(Kitchen k0, Kitchen k1) {
				if(k0.getDisplayId() < k1.getDisplayId()){
					return -1;
				}else if(k0.getDisplayId() > k1.getDisplayId()){
					return 1;
				}else{
					return 0;
				}
			}
		});
		expected.addAll(KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL));
		expected.addAll(KitchenDao.getByType(mStaff, Kitchen.Type.IDLE));
		
		int to = expected.indexOf(toK);
		int from = expected.indexOf(fromK);		
		expected.add(to, fromK);
		if(from  > to){
			expected.remove(from + 1);
		}else{
			expected.remove(from);
		}
		for(int i = 0; i < expected.size(); i++){
			expected.get(i).setDisplayId(i + 1);
		}
		
		//Test after moving kitchen.
		KitchenDao.move(mStaff, new Kitchen.MoveBuilder(fromK.getId(), toK.getId()));
		List<Kitchen> actual = SortedList.newInstance(new Comparator<Kitchen>(){
			@Override
			public int compare(Kitchen k0, Kitchen k1) {
				if(k0.getDisplayId() < k1.getDisplayId()){
					return -1;
				}else if(k0.getDisplayId() > k1.getDisplayId()){
					return 1;
				}else{
					return 0;
				}
			}
		});
		actual.addAll(KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL));
		actual.addAll(KitchenDao.getByType(mStaff, Kitchen.Type.IDLE));
		
		for(int i = 0; i < expected.size(); i++){
			Assert.assertEquals(expected.get(i).getDisplayId(), actual.get(i).getDisplayId());
			Assert.assertEquals(expected.get(i).getId(), actual.get(i).getId());
		}
	}
}
