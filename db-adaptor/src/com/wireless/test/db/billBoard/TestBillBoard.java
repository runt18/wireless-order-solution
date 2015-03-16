package com.wireless.test.db.billBoard;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.system.BillBoardDao;
import com.wireless.exception.BillBoardError;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.system.BillBoard;
import com.wireless.pojo.system.BillBoard.Status;
import com.wireless.pojo.util.DateUtil;
import com.wireless.test.db.TestInit;

public class TestBillBoard {
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException, ParseException{
		TestInit.init();
	}

	@Test
	public void testSystemBillBoard() throws SQLException, BusinessException, ParseException{
		int id = 0;
		try{
			BillBoard.InsertBuilder insertBuilder = BillBoard.InsertBuilder.build4System("测试系统公告", "2016-09-11 00:00:00").setBody("系统公告内容");
			id = BillBoardDao.insert(insertBuilder);
			
			BillBoard expected = insertBuilder.build();
			expected.setId(id);
			BillBoard actual = BillBoardDao.getById(id);
			//Compare the system billboard
			compare(expected, actual);
			//Compare the associated system billboards
			for(Restaurant eachRestaurant : RestaurantDao.getByCond(null, null)){
				BillBoard actualAssociated = BillBoardDao.getByCond(new BillBoardDao.ExtraCond().setSystem(id).setRestaurant(eachRestaurant)).get(0);
				
				BillBoard expectedAssociated = insertBuilder.build();
				expectedAssociated.setId(actualAssociated.getId());
				expectedAssociated.setType(BillBoard.Type.SYSTEM_ASSOCIATED);
				expectedAssociated.setRestaurantId(eachRestaurant.getId());
				
				compare(expectedAssociated, actualAssociated);
			}
			
			BillBoard.UpdateBuilder updateBuilder = new BillBoard.UpdateBuilder(id).setBody("修改系统公告内容")
																				   .setTitle("修改系统公告标题")
																				   .setExpired("2016-09-12 00:00:00");
			BillBoardDao.update(updateBuilder);
			if(updateBuilder.isTitleChanged()){
				expected.setTitle(updateBuilder.build().getTitle());
			}
			if(updateBuilder.isBodyChanged()){
				expected.setBody(updateBuilder.build().getBody());
			}
			if(updateBuilder.isExpiredChanged()){
				expected.setExpired(updateBuilder.build().getExpired());
			}
			if(updateBuilder.isStatusChanged()){
				expected.setStatus(updateBuilder.build().getStatus());
			}
			actual = BillBoardDao.getById(id);
			compare(expected, actual);
			//Compare the associated system billboards
			for(Restaurant eachRestaurant : RestaurantDao.getByCond(null, null)){
				BillBoard actualAssociated = BillBoardDao.getByCond(new BillBoardDao.ExtraCond().setSystem(id).setRestaurant(eachRestaurant)).get(0);
				
				BillBoard expectedAssociated = expected;
				expectedAssociated.setId(actualAssociated.getId());
				expectedAssociated.setType(BillBoard.Type.SYSTEM_ASSOCIATED);
				expectedAssociated.setRestaurantId(eachRestaurant.getId());
				
				compare(expectedAssociated, actualAssociated);
			}
			
		}finally{
			if(id != 0){
				BillBoardDao.deleteById(id);
				try{
					BillBoardDao.getById(id);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the bill board", BillBoardError.BILL_BOARD_NOT_EXIST, e.getErrCode());
				}
				Assert.assertTrue("failed to delete the associated system bill boards", BillBoardDao.getByCond(new BillBoardDao.ExtraCond().setSystem(id)).isEmpty());
			}
		}
	}
	
	@Test
	public void testBillBoard() throws SQLException, BusinessException, ParseException{
		int id = 0;
		try{
			BillBoard.InsertBuilder insertBuilder = BillBoard.InsertBuilder.build4Restaurant("测试公告", 40, "2016-09-11 00:00:00").setBody("公告内容");
			id = BillBoardDao.insert(insertBuilder);
			
			BillBoard expected = insertBuilder.build();
			expected.setId(id);
			BillBoard actual = BillBoardDao.getById(id);
			
			compare(expected, actual);
			
			BillBoard.UpdateBuilder updateBuilder = new BillBoard.UpdateBuilder(id).setBody("修改公告内容")
																				   .setTitle("修改公告标题")
																				   .setExpired("2016-09-12 00:00:00")
																				   .setStatus(Status.READ);
			BillBoardDao.update(updateBuilder);
			if(updateBuilder.isTitleChanged()){
				expected.setTitle(updateBuilder.build().getTitle());
			}
			if(updateBuilder.isBodyChanged()){
				expected.setBody(updateBuilder.build().getBody());
			}
			if(updateBuilder.isExpiredChanged()){
				expected.setExpired(updateBuilder.build().getExpired());
			}
			if(updateBuilder.isStatusChanged()){
				expected.setStatus(updateBuilder.build().getStatus());
			}
			actual = BillBoardDao.getById(id);
			compare(expected, actual);
			
		}finally{
			if(id != 0){
				BillBoardDao.deleteById(id);
				try{
					BillBoardDao.getById(id);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the bill board", BillBoardError.BILL_BOARD_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(BillBoard expected, BillBoard actual){
		Assert.assertEquals("id to bill board", expected.getId(), actual.getId());
		Assert.assertEquals("restaurant to bill board", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("title to bill board", expected.getTitle(), actual.getTitle());
		Assert.assertEquals("body to bill board", expected.getBody(), actual.getBody());
		Assert.assertEquals("created date to bill board", DateUtil.format(expected.getCreated(), DateUtil.Pattern.DATE), DateUtil.format(actual.getCreated(), DateUtil.Pattern.DATE));
		Assert.assertEquals("expired date to bill board", DateUtil.format(expected.getExpired(), DateUtil.Pattern.DATE), DateUtil.format(actual.getExpired(), DateUtil.Pattern.DATE));
		Assert.assertEquals("status to bill board", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("type to bill board", expected.getType(), actual.getType());
	}
	
	@Test
	public void testCleanup() throws SQLException{
		System.out.println(BillBoardDao.cleanup());
	}
}
