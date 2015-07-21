package com.wireless.db.weixin.menuAction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4ImageText;
import org.xml.sax.SAXException;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.oss.OssImageDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxMenuError;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.staffMgr.Staff;

public class WxMenuActionDao {

	public static class ExtraCond{
		private int id;
		private String key;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setKey(String key){
			this.key = key;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND id = " + id);
			}
			if(key != null){
				extraCond.append(" AND key = '" + key + "'");
			}
			return extraCond.toString();
		}
	}

	/**
	 * Insert the weixin menu action for image text.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder weixin menu text action 
	 * @return the id to weixin menu action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if failed to create the weixin menu action
	 */
	public static int insert(Staff staff, WxMenuAction.InsertBuilder4ImageText builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder.build());
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the weixin menu action for image text.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder weixin menu text action 
	 * @return the id to weixin menu action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if failed to create the weixin menu action
	 */
	public static int insert(DBCon dbCon, Staff staff, WxMenuAction.InsertBuilder4ImageText builder) throws SQLException, BusinessException{
		return insert(dbCon, staff, builder.build());
	}
	
	/**
	 * Insert the weixin menu action for text.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder weixin menu text action 
	 * @return the id to weixin menu action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if failed to create the weixin menu action
	 */
	public static int insert(Staff staff, WxMenuAction.InsertBuilder4Text builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the weixin menu action for text.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder weixin menu text action 
	 * @return the id to weixin menu action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if failed to create the weixin menu action
	 */
	public static int insert(DBCon dbCon, Staff staff, WxMenuAction.InsertBuilder4Text builder) throws SQLException, BusinessException{
		return insert(dbCon, staff, builder.build());
	}
	
	private static int insert(DBCon dbCon, Staff staff, WxMenuAction menuAction) throws SQLException{
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".weixin_menu_action " +
			  " ( restaurant_id, type, action, cate ) VALUES( " +
			  staff.getRestaurantId() + "," +
			  menuAction.getMsgType().getType() + "," +
			  "'" + menuAction.getAction() + "'," +
			  menuAction.getCate().getVal() + 
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int actionId = 0;
		if(dbCon.rs.next()){
			actionId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The weixin menu action id is NOT generated successfully.");
		}
		dbCon.rs.close();
		
		return actionId;
	}
	
	/**
	 * Update the weixin menu text action.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to weixin menu text action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if action to update does NOT exist
	 */
	public static void update(Staff staff, WxMenuAction.UpdateBuilder4Text builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the weixin menu text action.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to weixin menu text action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if action to update does NOT exist
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void update(DBCon dbCon, Staff staff, WxMenuAction.UpdateBuilder4ImageText builder) throws SQLException, BusinessException, SAXException, IOException, ParserConfigurationException{
		update(dbCon, staff, builder.builder());
	}
	
	/**
	 * Update the weixin menu image text action.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to weixin menu text action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if action to update does NOT exist
	 */
	public static void update(Staff staff, WxMenuAction.UpdateBuilder4ImageText builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder.builder());
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the weixin menu image text action.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to weixin menu text action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if action to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, WxMenuAction.UpdateBuilder4Text builder) throws SQLException, BusinessException{
		update(dbCon, staff, builder.builder());
	}
	
	private static void update(DBCon dbCon, Staff staff, WxMenuAction.UpdateBuilder builder) throws SQLException, BusinessException{
		
		WxMenuAction menuAction = builder.build(); 
		//Delete the associated oss image.
		try {
			WxMenuAction oriAction = getById(dbCon, staff, menuAction.getId());
			if(oriAction.getMsgType() == MsgType.MSG_TYPE_IMAGE_TEXT){
				Msg oriMsg = new WxMenuAction.MsgProxy(oriAction).toMsg();
				for(Data4Item item : ((Msg4ImageText)oriMsg).getItems()){
					for(OssImage image4Item : OssImageDao.getByCond(dbCon, staff, new OssImageDao.ExtraCond().setType(OssImage.Type.WX_ACTION_IMAGE))){
						if(image4Item.getObjectUrl().equals(item.getPicUrl())){
							OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setId(image4Item.getId()));
							break;
						}
					}
				}
			}
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new BusinessException(e.getMessage());
		}

		String sql;
		
		if(builder.isCateChanged() && menuAction.getCate() == WxMenuAction.Cate.SUBSCRIBE_REPLY){
			sql = " UPDATE " + Params.dbName + ".weixin_menu_action SET " +
				  " cate = " + WxMenuAction.Cate.NORMAL.getVal() + 
				  " WHERE restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
		}
		
		sql = " UPDATE " + Params.dbName + ".weixin_menu_action SET " +
			  " id = " + menuAction.getId() + 
			  " ,type = " + menuAction.getMsgType().getType() +
			  " ,action = '" + menuAction.getAction() + "'" +
			  (builder.isCateChanged() ? " ,cate = " + menuAction.getCate().getVal() : "") +
			  " WHERE id = " + menuAction.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(WxMenuError.WEIXIN_MENU_ACTION_NOT_EXIST);
		}
	}
	
	/**
	 * Get the weixin menu action to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to weixin menu action
	 * @return the weixin menu action to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin menu action to this id does NOT exist
	 */
	public static WxMenuAction getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the weixin menu action to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to weixin menu action
	 * @return the weixin menu action to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin menu action to this id does NOT exist
	 */
	public static WxMenuAction getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<WxMenuAction> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(WxMenuError.WEIXIN_MENU_ACTION_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the weixin menu action to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to weixin menu action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<WxMenuAction> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".weixin_menu_action " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() + 
			  (extraCond != null ? extraCond.toString() : "");
		
		List<WxMenuAction> result = new ArrayList<WxMenuAction>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			WxMenuAction menuAction = new WxMenuAction(dbCon.rs.getInt("id"));
			menuAction.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			menuAction.setMsgType(MsgType.valueOf(dbCon.rs.getInt("type")));
			menuAction.setAction(dbCon.rs.getString("action"));
			menuAction.setCate(WxMenuAction.Cate.valueOf(dbCon.rs.getInt("cate")));
			result.add(menuAction);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Delete the weixin menu action to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to weixin menu action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throw BusinessException
	 * 			throws if the weixin menu action to this id does NOT exist
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the weixin menu action to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to weixin menu action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throw BusinessException
	 * 			throws if the weixin menu action to this id does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(WxMenuError.WEIXIN_MENU_ACTION_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the weixin menu action to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to weixin menu action deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(WxMenuAction menuAction : getByCond(dbCon, staff, extraCond)){
			//Delete the associated oss image if belong to image text
			try {
				if(menuAction.getMsgType() == MsgType.MSG_TYPE_IMAGE_TEXT){
					for(Data4Item item : ((Msg4ImageText)new WxMenuAction.MsgProxy(menuAction).toMsg()).getItems()){
						for(OssImage image4Item : OssImageDao.getByCond(dbCon, staff, new OssImageDao.ExtraCond().setType(OssImage.Type.WX_ACTION_IMAGE))){
							if(image4Item.getObjectUrl().equals(item.getPicUrl())){
								OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setId(image4Item.getId()));
								break;
							}
						}
					}
				}
			} catch (SAXException | IOException | ParserConfigurationException | BusinessException ignored) {
				ignored.printStackTrace();
			}
			
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".weixin_menu_action WHERE id = " + menuAction.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
	
}
