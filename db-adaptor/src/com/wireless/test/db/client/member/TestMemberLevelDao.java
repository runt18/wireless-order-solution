package com.wireless.test.db.client.member;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberLevelDao;
import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.MemberLevel;
import com.wireless.pojo.client.MemberLevel.InsertBuilder;
import com.wireless.pojo.client.MemberLevel.UpdateBuilder;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestMemberLevelDao {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(63).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void compare(MemberLevel expected, MemberLevel actual){
		assertEquals("id", expected.getId(), actual.getId());
		assertEquals("levelId", expected.getLevelId(), actual.getLevelId());
		assertEquals("pointThreshold", expected.getPointThreshold(), actual.getPointThreshold());
		assertEquals("memberTypeId", expected.getMemberTypeId(), actual.getMemberTypeId());
	}
	
	@Test
	public void testMemberLevelDao() throws SQLException, BusinessException{
		List<MemberType> list = MemberTypeDao.getMemberType(mStaff, null, null);
		MemberType memberType = null;
		if(list.isEmpty()){
			throw new BusinessException("You don't add any member type!!!");
		}else{
			memberType = list.get(0);
		}
		
		int memberLevelId = 0;
		try{
			//添加
			MemberLevel.InsertBuilder insertBuilder = new InsertBuilder(1000, memberType.getTypeId());
			insertBuilder.setRestaurantId(mStaff.getRestaurantId());
			
			memberLevelId = MemberLevelDao.insert(insertBuilder, mStaff.getRestaurantId());
			
			MemberLevel expected = insertBuilder.build();
			
			MemberLevel actual = MemberLevelDao.getMemberLevelById(memberLevelId, mStaff.getRestaurantId());
			
			expected.setId(memberLevelId);
			expected.setLevelId(actual.getLevelId());
			compare(expected, actual);
			
			//修改
			MemberLevel.UpdateBuilder updateBuilder = new UpdateBuilder(memberLevelId);
			updateBuilder.setPointThreshold(1500);
			updateBuilder.setMemberTypeId(list.get(1).getTypeId());
			
			MemberLevelDao.update(updateBuilder, mStaff.getRestaurantId());
			
			expected = updateBuilder.build();
			actual = MemberLevelDao.getMemberLevelById(memberLevelId, mStaff.getRestaurantId());
			
			expected.setLevelId(actual.getLevelId());
			
			compare(expected, actual);
			
		}finally{
			MemberLevelDao.delete(memberLevelId);
			try{
				MemberLevelDao.getMemberLevelById(memberLevelId, mStaff.getRestaurantId());
				assertEquals("failed to delete memberLevel", false);
			}catch(Exception e){}
		}
	}

}
