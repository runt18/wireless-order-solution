package com.wireless.db.system;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.system.BillBoard;
import com.wireless.pojo.util.DateUtil;

public class BillBoardDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param extra
	 * @return
	 * @throws SQLException
	 */
	public static int getCount(DBCon dbCon, String extra) throws SQLException{
		int count = 0;
		String querySQL = "SELECT COUNT(*) "
				+ " FROM billboard BB LEFT JOIN restaurant R ON BB.restaurant_id = R.id "
				+ " WHERE 1=1 ";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next())
			count = dbCon.rs.getInt(1);
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param extra
	 * @return
	 * @throws SQLException
	 */
	public static List<BillBoard> get(DBCon dbCon, String extra) throws SQLException{
		List<BillBoard> list = new ArrayList<BillBoard>();
		BillBoard item = null;
		String querySQL = "SELECT BB.billboard_id, BB.restaurant_id, BB.title, BB.desc, BB.created, BB.expired, BB.type "
				+ " FROM billboard BB LEFT JOIN restaurant R ON BB.restaurant_id = R.id "
				+ " WHERE 1=1 ";
		querySQL += (extra != null && !extra.trim().isEmpty() ? extra : "");
		Statement stmt = dbCon.conn.createStatement();
		ResultSet res = stmt.executeQuery(querySQL);
		while(res != null && res.next()){
			item = new BillBoard();
			item.setId(res.getInt("billboard_id"));
			item.setTitle(res.getString("title"));
			item.setDesc(res.getString("desc"));
			item.setCreated(res.getTimestamp("created").getTime());
			item.setExpired(res.getTimestamp("expired").getTime());
			item.setType(res.getInt("type"));
			if(item.getType() == BillBoard.Type.RESTAURANT){
				try {
					item.setRestaurant(RestaurantDao.getById(dbCon, res.getInt("restaurant_id")));
				} catch (BusinessException e) {
//					e.printStackTrace();
				}
			}
			
			list.add(item);
		}
		
		res.close();
		res = null;
		stmt.close();
		stmt = null;
		item = null;
		return list;
	}
	public static List<BillBoard> get(String extra) throws SQLException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			return get(dbCon, extra);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	/**
	 * 
	 * @param dbCon
	 * @param insert
	 * @return
	 * @throws SQLException
	 */
	public static BillBoard insert(DBCon dbCon, BillBoard.InsertBuilder insert) throws SQLException{
		BillBoard bb = insert.build();
		String insertSQl = "INSERT INTO billboard (title, `desc`, created, expired, type, restaurant_id) "
				+ "VALUES("
				+ "'" + bb.getTitle() + "',"
				+ "'" + bb.getDesc() + "',"
				+ "NOW(),"
				+ "'" + DateUtil.format(bb.getExpired()) + "',"
				+ bb.getType().getVal() + ","
				+ (bb.getRestaurant() != null && bb.getRestaurant().getId() > 0 ? bb.getRestaurant().getId() : "null")
				+ ")";
		dbCon.stmt.executeUpdate(insertSQl, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()) bb.setId(dbCon.rs.getInt(1));
		return bb;
	}
	public static BillBoard insert(BillBoard.InsertBuilder insert) throws SQLException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			return insert(dbCon, insert);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	/**
	 * 
	 * @param dbCon
	 * @param update
	 * @return
	 * @throws SQLException
	 */
	public static BillBoard update(DBCon dbCon, BillBoard.UpdateBuilder update) throws SQLException{
		BillBoard bb = update.build();
		String insertSQl = "UPDATE billboard SET "
				+ " title='" + bb.getTitle() + "',"
				+ " `desc`='" + bb.getDesc() + "',"
				+ " expired='" + DateUtil.format(bb.getExpired()) + "',"
				+ " type=" + bb.getType().getVal() + ","
				+ " restaurant_id=" + (bb.getRestaurant() != null && bb.getRestaurant().getId() > 0 ? bb.getRestaurant().getId() : "null")
				+ " WHERE billboard_id = " + bb.getId();
		dbCon.stmt.executeUpdate(insertSQl, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()) bb.setId(dbCon.rs.getInt(1));
		return bb;
	}
	public static BillBoard update(BillBoard.UpdateBuilder update) throws SQLException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			return update(dbCon, update);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	
}
