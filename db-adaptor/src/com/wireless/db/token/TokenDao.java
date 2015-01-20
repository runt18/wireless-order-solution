package com.wireless.db.token;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TokenError;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.token.Token;

public class TokenDao {

	public static class ExtraCond{
		private int id;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND token_id = " + id);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the token.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the insert builder {@link Token#InsertBuilder}
	 * @return the token id just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the account does NOT exist
	 * 			<li>the create token is NOT qualified to be decrypted by private key
	 * 			<li>the create token is NOT matched both account and code
	 */
	public static int insert(Token.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the token.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the insert builder {@link Token#InsertBuilder}
	 * @return the token id just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the account does NOT exist
	 * 			<li>the create token is NOT qualified to be decrypted by private key
	 * 			<li>the create token is NOT matched the last modified
	 */
	public static int insert(DBCon dbCon, Token.InsertBuilder builder) throws SQLException, BusinessException{
		
		Restaurant restaurant = RestaurantDao.getByAccount(dbCon, builder.getAccount());
		
		Token createToken = new Token(0);
		createToken.setRestaurant(restaurant);
		createToken.decrypt(builder.getEncryptedCreateToken());

		if(createToken.getLastModified() == builder.getLastModified()){
			return insert(dbCon, restaurant);
		}else{
			throw new BusinessException("新建Token的时间戳跟系统验证的不一致", TokenError.LAST_MODIFIED_NOT_MATCH);
		}

	}
	
	/**
	 * Failed insert a new token.
	 * @param builder
	 * 			the failed insert builder
	 * @return the id to new token
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statment
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the account does NOT exist
	 * 			<li>the failed token is NOT qualified to be decrypted by private key
	 * 			<li>the create token is NOT qualified to be decrypted by private key
	 * 			<li>the create token is NOT matched the last modified	 
	 **/
	public static int failedInsert(Token.FailedInsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int tokenId = failedInsert(dbCon, builder);
			dbCon.conn.commit();
			return tokenId;
			
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Failed insert a new token.
	 * @param builder
	 * 			the failed insert builder
	 * @param dbCon
	 * 			the database connection
	 * @return the id to new token
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statment
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the account does NOT exist
	 * 			<li>the failed token is NOT qualified to be decrypted by private key
	 * 			<li>the create token is NOT qualified to be decrypted by private key
	 * 			<li>the create token is NOT matched the last modified	 
	 **/
	private static int failedInsert(DBCon dbCon, Token.FailedInsertBuilder builder) throws SQLException, BusinessException{
		Restaurant restaurant = RestaurantDao.getByAccount(dbCon, builder.getAccount());
		
		//Delete the failed token if it can be decrypted by the related private key.
		Token failedToken = new Token(0);
		failedToken.setRestaurant(restaurant);
		failedToken.decrypt(builder.getFailedEncryptedToken());
		
		deleteById(dbCon, failedToken.getId());
		
		//Insert the new encrypted token.
		return insert(dbCon, builder.getTokenBuilder());
				
	}
	
	private static int insert(DBCon dbCon, Restaurant restaurant) throws SQLException{
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".token " +
			  " (restaurant_id, birth_date, last_modified ) VALUES (" +
			  restaurant.getId() + "," +
			  " NOW(), " +
			  " NOW() " + 
			  ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int tokenId = 0;
		if(dbCon.rs.next()){
			tokenId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of token is not generated successfully.");
		}
		dbCon.rs.close();
		
		return tokenId;
	}
	
	/**
	 * Verify the token.
	 * @param builder
	 * 			the builder to verify token
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the token to verify does NOT exist
	 * 			<li>the last modified to token is NOT matched
	 */
	public static int verify(Token.VerifyBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return verify(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Verify the token.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the builder to verify token
	 * @return the id to next valid token 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the token to verify does NOT exist
	 * 			<li>the last modified to token is NOT matched
	 */
	public static int verify(DBCon dbCon, Token.VerifyBuilder builder) throws SQLException, BusinessException{
		Restaurant restaurant = RestaurantDao.getByAccount(dbCon, builder.getAccount());
		
		Token verifyToken = new Token(0);
		verifyToken.setRestaurant(restaurant);
		verifyToken.decrypt(builder.getEncryptedToken());
		
		List<Token> result = getByCond(dbCon, new ExtraCond().setId(verifyToken.getId()));
		
		if(result.isEmpty()){
			throw new BusinessException("验证Token不存在", TokenError.TOKEN_NOT_EXIST);
		}else{
			Token token = result.get(0);
			if(token.getLastModified() != verifyToken.getLastModified()){
				throw new BusinessException("验证Token的时间戳不正确", TokenError.LAST_MODIFIED_NOT_MATCH);
			}else{
				update(dbCon, new Token.UpdateBuilder(token.getId()));
				return token.getId();
			}
		}
	}
	
	/**
	 * Update the token to specific builder {@link Token#UpdateBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the token to update does NOT exist
	 */
	private static void update(DBCon dbCon, Token.UpdateBuilder builder) throws SQLException, BusinessException{
		Token token = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".token SET " +
			  " token_id = token_id " +
			  (builder.isRestaurantChanged() ? " ,restaurant_id = " + token.getRestaurant().getId() : "") +
			  " ,last_modified = NOW() " +
			  " WHERE token_id = " + token.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(TokenError.TOKEN_NOT_EXIST);
		}
	}
	
	/**
	 * Get the token to specific id.
	 * @param tokenId
	 * 			the token id
	 * @return the token to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the token to this id does NOT exist
	 */
	public static Token getById(int tokenId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, tokenId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the token to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param tokenId
	 * 			the token id
	 * @return the token to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the token to this id does NOT exist
	 */
	public static Token getById(DBCon dbCon, int tokenId) throws SQLException, BusinessException{
		List<Token> result = getByCond(dbCon, new ExtraCond().setId(tokenId));
		if(result.isEmpty()){
			throw new BusinessException(TokenError.TOKEN_NOT_EXIST);
		}else{
			Token token = result.get(0);
			token.setRestaurant(RestaurantDao.getById(token.getRestaurant().getId()));
			return token;
		}
	}
	
	/**
	 * Get the token according to extra condition {@link ExtraCond}
	 * @param dbCon
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 */
	public static List<Token> getByCond(DBCon dbCon, TokenDao.ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".token" +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<Token> result = new ArrayList<Token>();
		while(dbCon.rs.next()){
			Token token = new Token(dbCon.rs.getInt("token_id"));
			token.setRestaurant(new Restaurant(dbCon.rs.getInt("restaurant_id")));
			token.setBirthDate(dbCon.rs.getTimestamp("birth_date").getTime());
			token.setLastModified(dbCon.rs.getTimestamp("last_modified").getTime());
			result.add(token);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Delete the token to specific id.
	 * @param tokenId
	 * 			the token id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the token to this id does NOT exist
	 */
	public static void deleteById(int tokenId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, tokenId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the token to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param tokenId
	 * 			the token id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the token to this id does NOT exist
	 */
	public static void deleteById(DBCon dbCon, int tokenId) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, new ExtraCond().setId(tokenId)) == 0){
			throw new BusinessException(TokenError.TOKEN_NOT_EXIST);
		}
	}
	
	public static int deleteByCond(DBCon dbCon, TokenDao.ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(Token token : getByCond(dbCon, extraCond)){
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".token WHERE token_id = " + token.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
	
}
