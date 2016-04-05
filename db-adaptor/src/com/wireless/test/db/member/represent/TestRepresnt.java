package com.wireless.test.db.member.represent;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.represent.RepresentChainDao;
import com.wireless.db.member.represent.RepresentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.represent.Represent;
import com.wireless.pojo.member.represent.RepresentChain;
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
			final int recommendPoint = 100;
			final float recommendMoney = 10;
			final int subscribePoint = 200;
			final float subscribeMoney = 20;
			Represent.UpdateBuilder updateBuilder = new Represent.UpdateBuilder(id)
																 .setTitle("我要代言")
																 .setSlogon("我要代言宣传语")
																 .setFinishDate("2016-12-31")
																 .setRecommendMoney(recommendMoney)
																 .setRecommendPoint(recommendPoint)
																 .setSubscribeMoney(subscribeMoney)
																 .setSubscribePoint(subscribePoint)
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
	
	@Test
	public void testRepresentChain() throws SQLException, BusinessException{
		final int recommendPoint = 100;
		final float recommendMoney = 10;
		final int subscribePoint = 200;
		final float subscribeMoney = 20;
		Member subscriber = MemberDao.getByCond(mStaff, null, null).get(0);
		Member referrer = MemberDao.getByCond(mStaff, null, null).get(1);
		try{
			
			Represent.UpdateBuilder updateBuilder = new Represent.UpdateBuilder(RepresentDao.getByCond(mStaff, null).get(0).getId())
					 										     .setFinishDate("2016-12-31")
					 										     .setRecommendMoney(recommendMoney)
					 										     .setRecommendPoint(recommendPoint)
					 										     .setSubscribeMoney(subscribeMoney)
					 										     .setSubscribePoint(subscribePoint)
					 										     ;
			
			RepresentDao.update(mStaff, updateBuilder);

			
			Map<Member, MemberOperation[]> mos = MemberDao.chain(mStaff, new Member.ChainBuilder(subscriber, referrer)); 
			
			
			//Test the subscriber after represent chain.
			Member actualSubscriber = MemberDao.getById(mStaff, subscriber.getId());
			Assert.assertEquals("base balance to subscriber", subscriber.getBaseBalance(), actualSubscriber.getBaseBalance(), 0.01);
			Assert.assertEquals("extra balance to subscriber", subscriber.getExtraBalance() + subscribeMoney, actualSubscriber.getExtraBalance(), 0.01);
			Assert.assertEquals("total balance to subscriber", subscriber.getTotalBalance() + subscribeMoney, actualSubscriber.getTotalBalance(), 0.01);
			Assert.assertEquals("total charge to subscriber", subscriber.getTotalCharge() + subscribeMoney, actualSubscriber.getTotalCharge(), 0.01);
			Assert.assertEquals("total point to subscriber", subscriber.getPoint() + subscribePoint, actualSubscriber.getPoint(), 0.01);

			//Test the member operations to subscriber after represent chain.
			for(MemberOperation subscriberMo : mos.get(subscriber)){
				Assert.assertEquals("restaurant id to subscriber mo", mStaff.getRestaurantId(), subscriberMo.getRestaurantId());
				
				if(subscriberMo.getOperationType() == MemberOperation.OperationType.CHARGE){
					Assert.assertEquals("charge money to subscriber mo", subscribeMoney, subscriberMo.getChargeMoney(), 0.01);
					Assert.assertEquals("charge type to subscriber mo", MemberOperation.ChargeType.SUBSCRIBE, subscriberMo.getChargeType());
					
					Assert.assertEquals("delta base money to subscriber mo", 0, subscriberMo.getDeltaBaseMoney(), 0.01);
					Assert.assertEquals("delta extra money to subscriber mo", subscribeMoney, subscriberMo.getDeltaExtraMoney(), 0.01);
					Assert.assertEquals("remaining base money to subscriber mo", subscriber.getBaseBalance(), subscriberMo.getRemainingBaseMoney(), 0.01);
					Assert.assertEquals("remaining extra money to subscriber mo", subscriber.getExtraBalance() + subscribeMoney, subscriberMo.getRemainingExtraMoney(), 0.01);
					Assert.assertEquals("remaining total money to subscriber mo", subscriber.getTotalBalance() + subscribeMoney, subscriberMo.getRemainingTotalMoney(), 0.01);
					
				}else if(subscriberMo.getOperationType() == MemberOperation.OperationType.POINT_SUBSCRIBE){
					Assert.assertEquals("delta point to subscriber mo", subscribePoint, subscriberMo.getDeltaPoint(), 0.01);
					Assert.assertEquals("remaining point to subscriber mo", subscriber.getPoint() + subscribePoint, subscriberMo.getRemainingPoint());
				}
			}
			
			//Test the referrer after represent chain.
			Member actualReferrer = MemberDao.getById(mStaff, referrer.getId());
			Assert.assertEquals("base balance to referrer", referrer.getBaseBalance(), actualReferrer.getBaseBalance(), 0.01);
			Assert.assertEquals("extra balance to referrer", referrer.getExtraBalance() + recommendMoney, actualReferrer.getExtraBalance(), 0.01);
			Assert.assertEquals("total balance to referrer", referrer.getTotalBalance() + recommendMoney, actualReferrer.getTotalBalance(), 0.01);
			Assert.assertEquals("total charge to referrer", referrer.getTotalCharge() + recommendMoney, actualReferrer.getTotalCharge(), 0.01);
			Assert.assertEquals("total point to referrer", referrer.getPoint() + recommendPoint, actualReferrer.getPoint(), 0.01);
			
			//Test the member operations to referrer after represent chain.
			for(MemberOperation referrerMo : mos.get(referrer)){
				Assert.assertEquals("restaurant id to referrer mo", mStaff.getRestaurantId(), referrerMo.getRestaurantId());
				
				if(referrerMo.getOperationType() == MemberOperation.OperationType.CHARGE){
					Assert.assertEquals("charge money to referrer mo", recommendMoney, referrerMo.getChargeMoney(), 0.01);
					Assert.assertEquals("charge type to referrer mo", MemberOperation.ChargeType.RECOMMEND, referrerMo.getChargeType());
					
					Assert.assertEquals("delta base money to referrer mo", 0, referrerMo.getDeltaBaseMoney(), 0.01);
					Assert.assertEquals("delta extra money to referrer mo", recommendMoney, referrerMo.getDeltaExtraMoney(), 0.01);
					Assert.assertEquals("remaining base money to referrer mo", referrer.getBaseBalance(), referrerMo.getRemainingBaseMoney(), 0.01);
					Assert.assertEquals("remaining extra money to referrer mo", referrer.getExtraBalance() + recommendMoney, referrerMo.getRemainingExtraMoney(), 0.01);
					Assert.assertEquals("remaining total money to referrer mo", referrer.getTotalBalance() + recommendMoney, referrerMo.getRemainingTotalMoney(), 0.01);
					
				}else if(referrerMo.getOperationType() == MemberOperation.OperationType.POINT_RECOMMEND){
					Assert.assertEquals("delta point to referrer mo", recommendPoint, referrerMo.getDeltaPoint(), 0.01);
					Assert.assertEquals("remaining point to referrer mo", referrer.getPoint() + recommendPoint, referrerMo.getRemainingPoint());
				}
			}

			//Test the represent chain.
			RepresentChain actualChain = RepresentChainDao.getByCond(mStaff, new RepresentChainDao.ExtraCond().setReferrerId(referrer.getId()).setSubscriberId(subscriber.getId())).get(0);
			Assert.assertEquals("restaurant id to represent chain", mStaff.getRestaurantId(), actualChain.getRestaurantId());
			Assert.assertEquals("referrer id to represent chain", referrer.getId(), actualChain.getRecommendMemberId());
			Assert.assertEquals("referrer name to represent chain", referrer.getName(), actualChain.getRecommendMember());
			Assert.assertEquals("recommend money to represent chain", recommendMoney, actualChain.getRecommendMoney(), 0.01);
			Assert.assertEquals("recommend point to represent chain", recommendPoint, actualChain.getRecommendPoint());
			Assert.assertEquals("subscriber id to represent chain", subscriber.getId(), actualChain.getSubscribeMemberId());
			Assert.assertEquals("subscriber name to represent chain", subscriber.getName(), actualChain.getSubscribeMember());
			Assert.assertEquals("subscriber money to represent chain", subscribeMoney, actualChain.getSubscribeMoney(), 0.01);
			Assert.assertEquals("subscriber point to represent chain", subscribePoint, actualChain.getSubscribePoint());
			Assert.assertTrue("subscriber date to represent chain", Math.abs(System.currentTimeMillis() - actualChain.getSubscribeDate()) < 5000);
		}finally{
			//Delete the represent chain.
			RepresentChainDao.deleteByCond(mStaff, new RepresentChainDao.ExtraCond().setReferrerId(referrer.getId()).setSubscriberId(subscriber.getId()));
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
