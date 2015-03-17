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
			
			//Test to create a new token.
			tokenId = TokenDao.insert(new Token.InsertBuilder(r1));
			int code = TokenDao.getById(tokenId).getCode();
			
			//Test the dynamic code is incorrect.
			try{			
				TokenDao.generate(new Token.GenerateBuilder(r1.getAccount(), code + 1));
			}catch(BusinessException e){
				Assert.assertEquals("fail to test dynamic code is incorrect", TokenError.DYN_CODE_INCORRECT, e.getErrCode());
			}
			
			//Test the dynamic code is expired.
//			r1Token.setLastModified(lastModified + 1);
//			try{			
//				TokenDao.generate(new Token.GenerateBuilder(r1.getAccount(), r1Token.encrypt(), lastModified));
//			}catch(BusinessException e){
//				Assert.assertEquals("fail to test the last modified NOT matched", TokenError.LAST_MODIFIED_NOT_MATCH, e.getErrCode());
//			}
			
			//Test to generate a token successfully.
			TokenDao.generate(new Token.GenerateBuilder(r1.getAccount(), code));
			
			Token actual = TokenDao.getById(tokenId);
			r1Token.setId(tokenId);
			r1Token.setLastModified(actual.getLastModified());
			r1Token.setStatus(Token.Status.TOKEN);
			compareToken(r1Token, actual);
	
			int failedTokenId = tokenId;
			//Test to failed generate a new token using wrong failed encrypted token.
			tokenId = TokenDao.insert(new Token.InsertBuilder(r1));
			code = TokenDao.getById(tokenId).getCode();
			String failedEncryptedToken = r2Token.encrypt();
			try{
				TokenDao.failedGenerate(new Token.FailedGenerateBuilder(failedEncryptedToken, r1.getAccount(), code));
				Assert.assertTrue("failed to test failed insert a new token using wrong failed encrypted token", false);
			}catch(BusinessException e){
				Assert.assertEquals("failed to test failed insert a new token using wrong failed encrypted token", TokenError.TOKEN_DECRYPT_FAIL, e.getErrCode());
			}
			
			//Test to failed generate a new token using wrong code.
			failedEncryptedToken = r1Token.encrypt();
			try{
				TokenDao.failedGenerate(new Token.FailedGenerateBuilder(failedEncryptedToken, r1.getAccount(), code + 1));
				Assert.assertTrue("failed to test failed insert a new token using wrong code", false);
			}catch(BusinessException e){
				Assert.assertEquals("failed to test failed insert a new token using wrong code", TokenError.DYN_CODE_INCORRECT, e.getErrCode());
			}
			
			//Test to failed generate a new token successfully.
			failedEncryptedToken = r1Token.encrypt();
			TokenDao.failedGenerate(new Token.FailedGenerateBuilder(failedEncryptedToken, r1.getAccount(), code));
			
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
				Thread.sleep(11 * 1000);
				r1Token.setLastModified(actualLastModified + 1);
				TokenDao.verify(new Token.VerifyBuilder(r1.getAccount(), r1Token.encrypt()));
				Assert.assertTrue("failed to test verify token using the wrong last modified", false);
			}catch(BusinessException e){
				Assert.assertEquals("failed to test verify token using the wrong last modified", TokenError.LAST_MODIFIED_NOT_MATCH, e.getErrCode());
			}
			
			//Test to verify the token successfully.
			Thread.sleep(500);
			try{
				r1Token.setLastModified(actualLastModified);
				TokenDao.verify(new Token.VerifyBuilder(r1.getAccount(), r1Token.encrypt()));
			}catch(BusinessException e){
				Assert.assertTrue("failed to test verify token", false);
			}
			
			//Test to verify the token become invalid after verification.
			try{
				Thread.sleep(11 * 1000);
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
		Assert.assertEquals("token status", expected.getStatus(), actual.getStatus());
	}
	
	@Test
	public void testCleanup() throws SQLException{
		System.out.println(TokenDao.cleanup());
	}
}
