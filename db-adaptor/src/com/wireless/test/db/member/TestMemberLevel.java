package com.wireless.test.db.member;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.member.MemberLevelDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.member.MemberLevel;
import com.wireless.pojo.member.MemberLevel.InsertBuilder;
import com.wireless.pojo.member.MemberLevel.UpdateBuilder;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestMemberLevel {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getByRestaurant(63).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void compare(MemberLevel expected, MemberLevel actual){
		assertEquals("id", expected.getId(), actual.getId());
		assertEquals("levelId", expected.getLevelId(), actual.getLevelId());
		assertEquals("pointThreshold", expected.getPointThreshold(), actual.getPointThreshold());
		assertEquals("memberTypeId", expected.getMemberType().getId(), actual.getMemberType().getId());
	}
	
	@Test
	public void testMemberLevelDao() throws SQLException, BusinessException{
		List<MemberType> list = MemberTypeDao.getByCond(mStaff, null, null);
		MemberType memberType = null;
		if(list.isEmpty()){
			throw new BusinessException("You don't add any member type!!!");
		}else{
			memberType = list.get(0);
		}
		
		int memberLevelId = 0;
		try{
			//添加
			MemberLevel.InsertBuilder insertBuilder = new InsertBuilder(1000, memberType.getId());
			insertBuilder.setRestaurantId(mStaff.getRestaurantId());
			
			memberLevelId = MemberLevelDao.insert(mStaff, insertBuilder);
			
			MemberLevel expected = insertBuilder.build();
			
			MemberLevel actual = MemberLevelDao.getById(mStaff, memberLevelId);
			
			expected.setId(memberLevelId);
			expected.setLevelId(actual.getLevelId());
			compare(expected, actual);
			
			//修改
			MemberLevel.UpdateBuilder updateBuilder = new UpdateBuilder(memberLevelId);
			updateBuilder.setPointThreshold(1500);
			updateBuilder.setMemberTypeId(list.get(1).getId());
			
			MemberLevelDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			actual = MemberLevelDao.getById(mStaff, memberLevelId);
			
			expected.setLevelId(actual.getLevelId());
			
			compare(expected, actual);
			
		}finally{
			MemberLevelDao.delete(memberLevelId);
			try{
				MemberLevelDao.getById(mStaff, memberLevelId);
				assertEquals("failed to delete memberLevel", false);
			}catch(Exception e){}
		}
	}

}
