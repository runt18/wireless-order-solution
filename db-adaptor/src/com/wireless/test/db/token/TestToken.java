package com.wireless.test.db.token;

import java.beans.PropertyVetoException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.token.TokenDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TokenError;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.token.Token;
import com.wireless.test.db.TestInit;

public class TestToken {
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testTokenDao() throws SQLException, BusinessException, NoSuchAlgorithmException, InterruptedException{
		int tokenId = 0;
		try{
			long lastModified = System.currentTimeMillis();
			Restaurant r1 = RestaurantDao.getByAccount("liyy");
			if(!r1.hasRSA()){
				RestaurantDao.update(new Restaurant.UpdateBuilder(r1.getId()).resetRSA());
			}
			Token r1Token = new Token(0);
			r1Token.setRestaurant(r1);
			
			
			Restaurant r2 = RestaurantDao.getByAccount("demo");
			if(!r2.hasRSA()){
				RestaurantDao.update(new Restaurant.UpdateBuilder(r2.getId()).resetRSA());
			}
			Token r2Token = new Token(0);
			r2Token.setRestaurant(r2);
			
			//Test the public key is NOT matched the private.
			r2Token.setLastModified(lastModified);
			try{			
				TokenDao.insert(new Token.InsertBuilder(r1.getAccount(), r2Token.encrypt(), lastModified));
			}catch(BusinessException e){
				Assert.assertEquals("fail to test the public & private key NOT matched", TokenError.TOKEN_DECRYPT_FAIL, e.getErrCode());
			}
			
			//Test the encrypted token is NOT matched the last modified.
			r1Token.setLastModified(lastModified + 1);
			try{			
				TokenDao.insert(new Token.InsertBuilder(r1.getAccount(), r1Token.encrypt(), lastModified));
			}catch(BusinessException e){
				Assert.assertEquals("fail to test the last modified NOT matched", TokenError.LAST_MODIFIED_NOT_MATCH, e.getErrCode());
			}
			
			//Test to insert a new token successfully.
			r1Token.setLastModified(lastModified);
			byte[] encryptedToken = r1Token.encrypt();
			
			tokenId = TokenDao.insert(new Token.InsertBuilder(r1.getAccount(), encryptedToken, lastModified));
			
			Token actual = TokenDao.getById(tokenId);
			r1Token.setId(tokenId);
			r1Token.setLastModified(actual.getLastModified());
			compareToken(r1Token, actual);
	
			//Test to failed insert a new token using wrong failed token.
			byte[] failedEncryptedToken = r2Token.encrypt();
			lastModified = System.currentTimeMillis();
			r1Token.setLastModified(lastModified);
			try{
				TokenDao.failedInsert(new Token.FailedInsertBuilder(failedEncryptedToken, r1.getAccount(), r1Token.encrypt(), lastModified));
				Assert.assertTrue("failed to test failed insert a new token using wrong failed token", false);
			}catch(BusinessException e){
				Assert.assertEquals("failed to test failed insert a new token using wrong failed token", TokenError.TOKEN_DECRYPT_FAIL, e.getErrCode());
			}
			
			//Test to failed insert a new token successfully.
			failedEncryptedToken = r1Token.encrypt();
			int failedTokenId = tokenId;
			tokenId = TokenDao.failedInsert(new Token.FailedInsertBuilder(failedEncryptedToken, r1.getAccount(), r1Token.encrypt(), lastModified));
			
			try{
				TokenDao.getById(failedTokenId);
				Assert.assertTrue("failed to delete the failed token", false);
			}catch(BusinessException e){
				Assert.assertEquals("failed to delete the failed token", TokenError.TOKEN_NOT_EXIST, e.getErrCode());
			}
			
			actual = TokenDao.getById(tokenId);
			r1Token.setId(tokenId);
			long actualLastModified = actual.getLastModified();
			r1Token.setLastModified(actual.getLastModified());
			compareToken(r1Token, actual);
			
			//Test to verify the token using the wrong public key.
			try{
				TokenDao.verify(new Token.VerifyBuilder(r1.getAccount(), r2Token.encrypt()));
				Assert.assertTrue("failed to test verify token using the wrong public key", false);
			}catch(BusinessException e){
				Assert.assertEquals("failed to test verify token using the wrong public key", TokenError.TOKEN_DECRYPT_FAIL, e.getErrCode());
			}
			
			//Test to verify the token using the wrong last modified.
			try{
				r1Token.setLastModified(lastModified + 1);
				TokenDao.verify(new Token.VerifyBuilder(r1.getAccount(), r1Token.encrypt()));
				Assert.assertTrue("failed to test verify token using the wrong last modified", false);
			}catch(BusinessException e){
				Assert.assertEquals("failed to test verify token using the wrong last modified", TokenError.LAST_MODIFIED_NOT_MATCH, e.getErrCode());
			}
			
			//Test to verify the token successfully.
			Thread.sleep(500);
			try{
				r1Token.setLastModified(actualLastModified);
				tokenId = TokenDao.verify(new Token.VerifyBuilder(r1.getAccount(), r1Token.encrypt()));
			}catch(BusinessException e){
				Assert.assertTrue("failed to test verify token", false);
			}
			
			//Test to verify the token become invalid after verification.
			try{
				TokenDao.verify(new Token.VerifyBuilder(r1.getAccount(), r1Token.encrypt()));
				Assert.assertTrue("the token should become invalid due to last modified changed", false);
			}catch(BusinessException e){
				Assert.assertEquals("the token should become invalid due to last modified changed", TokenError.LAST_MODIFIED_NOT_MATCH, e.getErrCode());
			}
			
			//Test to verify the next token after verification  
			try{
				TokenDao.verify(new Token.VerifyBuilder(r1.getAccount(), TokenDao.getById(tokenId).encrypt()));
			}catch(BusinessException e){
				Assert.assertTrue("failed to test verify the next token after verification", false);
			}
			
			
		}finally{
			if(tokenId != 0){
				TokenDao.deleteById(tokenId);
				try{
					TokenDao.getById(tokenId);
				}catch(BusinessException e){
					Assert.assertEquals("token failed to delete", TokenError.TOKEN_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compareToken(Token expected, Token actual){
		Assert.assertEquals("token id", expected.getId(), actual.getId());
		Assert.assertEquals("token restaurant", expected.getRestaurant(), actual.getRestaurant());
		Assert.assertEquals("token last modified", expected.getLastModified(), actual.getLastModified());
	}
}
