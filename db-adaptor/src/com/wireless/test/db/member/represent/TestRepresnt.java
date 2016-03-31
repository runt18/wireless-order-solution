package com.wireless.test.db.member.represent;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.member.represent.RepresentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.member.represent.Represent;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.test.db.TestInit;

public class TestRepresnt {
	private static Staff mStaff;
	@BeforeClass
	public static void initDBParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try{
			mStaff = StaffDao.getAdminByRestaurant(40);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRepresent() throws SQLException, BusinessException{
		int id = 0;
		try{
			//Test to insert a new represent
			Represent.InsertBuilder insertBuilder = new Represent.InsertBuilder();
			id = RepresentDao.insert(mStaff, insertBuilder);
			
			Represent expected = insertBuilder.build();
			expected.setId(id);
			Represent actual = RepresentDao.getByCond(mStaff, new RepresentDao.ExtraCond().setId(id)).get(0);
			compare(expected, actual);
			
			//Test to update the represent
			Represent.UpdateBuilder updateBuilder = new Represent.UpdateBuilder(id)
																 .setTitle("我要代言")
																 .setSlogon("我要代言宣传语")
																 .setFinishDate("2016-12-31")
																 .setRecommendMoney(0)
																 .setRecommendPoint(200)
																 .setSubscribeMoney(10)
																 .setSubscribePoint(100)
																 .setBody("我要代言body");
			RepresentDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			actual = RepresentDao.getByCond(mStaff, new RepresentDao.ExtraCond().setId(id)).get(0);
			compare(expected, actual);
			
		}finally{
			if(id != 0){
				RepresentDao.deleteByCond(mStaff, new RepresentDao.ExtraCond().setId(id));
				Assert.assertTrue("failed to delete the represent", RepresentDao.getByCond(mStaff, new RepresentDao.ExtraCond().setId(id)).isEmpty());
			}
		}
	}
	
	private void compare(Represent expected, Represent actual){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("finish date", DateUtil.format(expected.getFinishDate()), DateUtil.format(actual.getFinishDate()));
		Assert.assertEquals("body", expected.getBody(), actual.getBody());
		Assert.assertEquals("title", expected.getTitle(), actual.getTitle());
		Assert.assertEquals("slogon", expected.getSlogon(), actual.getSlogon());
		Assert.assertEquals("recommend money", expected.getRecommentMoney(), actual.getRecommentMoney(), 0.01);
		Assert.assertEquals("recommend point", expected.getRecommendPoint(), actual.getRecommendPoint());
		Assert.assertEquals("subscribe money", expected.getSubscribeMoney(), actual.getSubscribeMoney(), 0.01);
		Assert.assertEquals("subscribe point", expected.getSubscribePoint(), actual.getSubscribePoint());
	}
}
