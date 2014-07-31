package com.wireless.test.db.menuMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.SortedList;
import com.wireless.test.db.TestInit;

public class TestDepartmentDao {
	private static Staff mStaff;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getByRestaurant(37).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDepartmentDao() throws SQLException, BusinessException{
		int deptId = 0;
		try{
			Department.AddBuilder addBuilder = new Department.AddBuilder("测试部门");
			deptId = DepartmentDao.add(mStaff, addBuilder);
			
			Department actual = DepartmentDao.getById(mStaff, deptId);
			Department expected = addBuilder.build();
			
			Assert.assertEquals("id : insert department", deptId, actual.getId());
			Assert.assertEquals("restaurant : insert department", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("name : insert department", expected.getName(), actual.getName());
			Assert.assertEquals("type : insert department", Department.Type.NORMAL, actual.getType());

			Department.UpdateBuilder updateBuilder = new Department.UpdateBuilder(Department.DeptId.valueOf(deptId), "测试部门2");
			DepartmentDao.update(mStaff, updateBuilder);
			actual = DepartmentDao.getById(mStaff, deptId);
			expected = updateBuilder.build();
			
			Assert.assertEquals("id : insert department", deptId, actual.getId());
			Assert.assertEquals("restaurant : insert department", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("name : insert department", expected.getName(), actual.getName());
			Assert.assertEquals("type : insert department", Department.Type.NORMAL, actual.getType());
			
		}finally{
			if(deptId != 0){
				DepartmentDao.remove(mStaff, deptId);
				Department deptToRemove = DepartmentDao.getById(mStaff, deptId);
				Assert.assertEquals("type : failed to remove department", Department.Type.IDLE, deptToRemove.getType());
			}
		}
	}
	
	@Test
	public void testDepartmentMove() throws SQLException, BusinessException{
		List<Department> depts = DepartmentDao.getByType(mStaff, Department.Type.NORMAL);
		testDepartmentMove(depts.get(3),
						depts.get(0));
		
		depts = DepartmentDao.getByType(mStaff, Department.Type.NORMAL);
		testDepartmentMove(depts.get(0),
						depts.get(3));
		
		depts = DepartmentDao.getByType(mStaff, Department.Type.NORMAL);
		testDepartmentMove(depts.get(3),
						depts.get(depts.size() - 1));
		
		depts = DepartmentDao.getByType(mStaff, Department.Type.NORMAL);
		testDepartmentMove(depts.get(depts.size() - 1),
						depts.get(3));
		
		depts = DepartmentDao.getByType(mStaff, Department.Type.NORMAL);
		testDepartmentMove(depts.get(0),
						depts.get(depts.size() - 1));
		
		depts = DepartmentDao.getByType(mStaff, Department.Type.NORMAL);
		testDepartmentMove(depts.get(depts.size() - 1),
						depts.get(0));
		
		depts = DepartmentDao.getByType(mStaff, Department.Type.NORMAL);
		testDepartmentMove(depts.get(3),
						depts.get(5));
		
		depts = DepartmentDao.getByType(mStaff, Department.Type.NORMAL);
		testDepartmentMove(depts.get(5),
						depts.get(3));
	}
	
	private void testDepartmentMove(Department fromD, Department toD) throws SQLException, BusinessException{
		List<Department> expected = SortedList.newInstance(new Comparator<Department>(){
			@Override
			public int compare(Department d0, Department d1) {
				if(d0.getDisplayId() < d1.getDisplayId()){
					return -1;
				}else if(d0.getDisplayId() > d1.getDisplayId()){
					return 1;
				}else{
					return 0;
				}
			}
		});
		expected.addAll(DepartmentDao.getByType(mStaff, Department.Type.NORMAL));
		expected.addAll(DepartmentDao.getByType(mStaff, Department.Type.IDLE));
		
		int to = expected.indexOf(toD);
		int from = expected.indexOf(fromD);		
		expected.add(to, fromD);
		if(from  > to){
			expected.remove(from + 1);
		}else{
			expected.remove(from);
		}
		for(int i = 0; i < expected.size(); i++){
			expected.get(i).setDisplayId(i + 1);
		}
		
		//Test after moving department.
		DepartmentDao.move(mStaff, new Department.MoveBuilder(fromD.getId(), toD.getId()));
		List<Department> actual = SortedList.newInstance(new Comparator<Department>(){
			@Override
			public int compare(Department d0, Department d1) {
				if(d0.getDisplayId() < d1.getDisplayId()){
					return -1;
				}else if(d0.getDisplayId() > d1.getDisplayId()){
					return 1;
				}else{
					return 0;
				}
			}
		});
		actual.addAll(DepartmentDao.getByType(mStaff, Department.Type.NORMAL));
		actual.addAll(DepartmentDao.getByType(mStaff, Department.Type.IDLE));
		
		for(int i = 0; i < expected.size(); i++){
			Assert.assertEquals(expected.get(i).getDisplayId(), actual.get(i).getDisplayId());
			Assert.assertEquals(expected.get(i).getId(), actual.get(i).getId());
		}
	}
}
