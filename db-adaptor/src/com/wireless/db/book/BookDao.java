package com.wireless.db.book;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BookError;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class BookDao {

	public static class ExtraCond{
		private int id;
		private String tele;
		private String member;
		private Book.Status status;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setTele(String tele){
			this.tele = tele;
			return this;
		}
		
		public ExtraCond setMember(String member){
			this.member = member;
			return this;
		}
		
		public ExtraCond setStatus(Book.Status status){
			this.status = status;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND book_id = " + id);
			}
			if(tele != null){
				extraCond.append(" AND book_tele LIKE %" + tele + "%");
			}
			if(member != null){
				extraCond.append(" AND book_member LIKE %" + member + "%");
			}
			if(status != null){
				extraCond.append(" AND book_status = " + status.getVal());
			}
			return extraCond.toString();
		}
	}

	/**
	 * Confirm the book record.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the confirm builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the book record to confirm does NOT exist
	 */
	public static void confirm(Staff staff, Book.ConfirmBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			confirm(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Confirm the book record.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the confirm builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the book record to confirm does NOT exist
	 */
	public static void confirm(DBCon dbCon, Staff staff, Book.ConfirmBuilder builder) throws SQLException, BusinessException{
		if(getById(dbCon, staff, builder.build().getId()).getStatus() == Book.Status.CREATED){
			update(dbCon, staff, builder.getBuilder());
		}else{
			throw new BusinessException("【已创建】状态的预订信息才可确认", BookError.BOOK_RECORD_CONFIRM_FAIL);
		}
	}
	
	/**
	 * Insert the book record for weixin according to specific builder {@link Book#InsertBuilder4Weixin}.
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert weixin book record
	 * @return the book id 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if failed to insert the book record
	 */
	public static int insert(Staff staff, Book.InsertBuilder4Weixin builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int bookId = insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return bookId;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the book record for weixin according to specific builder {@link Book#InsertBuilder4Weixin}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert weixin book record
	 * @return the book id 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if failed to insert the book record
	 */
	public static int insert(DBCon dbCon, Staff staff, Book.InsertBuilder4Weixin builder) throws SQLException, BusinessException{
		int bookId = insert(dbCon, staff);
		update(dbCon, staff, builder.setId(bookId).getBuilder());
		return bookId;
	}
	
	private static int insert(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".book " + 
			  " (restaurant_id) VALUES( " +
			  staff.getRestaurantId() +
			  ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int bookId = 0;
		if(dbCon.rs.next()){
			bookId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The book id is NOT generated successfully.");
		}
		dbCon.rs.close();
		
		return bookId;
	}
	
	private static void update(DBCon dbCon, Staff staff, Book.UpdateBuilder builder) throws SQLException, BusinessException{
		Book book = builder.build();
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".book SET " +
			  " book_id = " + book.getId() +
			  (builder.isBookDateChanged() ? " ,book_date = '" + DateUtil.format(book.getBookDate()) + "'" : "") +
			  (builder.isReservedChanged() ? " ,book_reserved = " + book.getReserved() : "") +
			  (builder.isRegionChanged() ? " ,book_region = '" + book.getRegion() + "'" : "") +
			  (builder.isMemberChanged() ? " ,book_member = '" + book.getMember() + "'" : "") +
			  (builder.isMemberIdChanged() ? " ,book_member_id = " + book.getMemberId() : "") +
			  (builder.isTeleChanged() ? " ,book_tele = '" + book.getTele() + "'" : "") +
			  (builder.isAmountChanged() ? " ,book_amount = " + book.getAmount() : "") +
			  (builder.isStaffChanged() ? " ,book_staff = '" + book.getStaff().getName() + "'" : "") +
			  (builder.isStaffChanged() ? " ,book_staff_id = " + book.getStaff().getId() : "") +
			  (builder.isCategoryChanged() ? " ,book_cate = '" + book.getCategory() + "'" : "") +
			  (builder.isSourceChanged() ? " ,book_source = " + book.getSource().getVal() : "") +
			  (builder.isStatusChanged() ? " ,book_status = " + book.getStatus().getVal() : "") +
			  (builder.isMoneyChanged() ? " ,book_money = " + book.getMoney() : "") +
			  (builder.isCommentChanged() ? " ,comment = '" + book.getComment() + "'" : "") +
			  " WHERE book_id = " + book.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(BookError.BOOK_RECORD_NOT_EXIST);
		}
		
		//Update the associated book tables.
		if(builder.isTableChanged()){
			sql = " DELETE FROM " + Params.dbName + ".book_table WHERE book_id = " + book.getId();
			dbCon.stmt.executeUpdate(sql);
			
			for(Table bookTbl : book.getTables()){
				sql = " INSERT INTO " + Params.dbName + ".book_table " +
					  " (book_id, table_id, table_name) VALUES ( " +
					  book.getId() + "," +
					  bookTbl.getId() + "," +
					  "'" + bookTbl.getName() + "'" +
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}
		}
		
		//TODO Update the associated book order.
	}

	/**
	 * Get the book record to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param bookId
	 * 			the book id
	 * @return the book record to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the book record to this id does NOT exist
	 */
	public static Book getById(Staff staff, int bookId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, bookId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the book record to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param bookId
	 * 			the book id
	 * @return the book record to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the book record to this id does NOT exist
	 */
	public static Book getById(DBCon dbCon, Staff staff, int bookId) throws SQLException, BusinessException{
		List<Book> result = getByCond(dbCon, staff, new ExtraCond().setId(bookId));
		if(result.isEmpty()){
			throw new BusinessException(BookError.BOOK_RECORD_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the book according to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the book records
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Book> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".book WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		final List<Book> result = new ArrayList<Book>();
		while(dbCon.rs.next()){
			Book book = new Book(dbCon.rs.getInt("book_id"));
			book.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			book.setBookDate(dbCon.rs.getTimestamp("book_date").getTime());
			book.setReserved(dbCon.rs.getInt("book_reserved"));
			book.setRegion(dbCon.rs.getString("book_region"));
			book.setMember(dbCon.rs.getString("book_member"));
			book.setMemberId(dbCon.rs.getInt("book_member_id"));
			book.setTele(dbCon.rs.getString("book_tele"));
			book.setAmount(dbCon.rs.getInt("book_amount"));
			if(dbCon.rs.getInt("book_staff_id") != 0){
				Staff bookStaff = new Staff(dbCon.rs.getInt("book_staff_id"));
				bookStaff.setName(dbCon.rs.getString("book_staff"));
				book.setStaff(bookStaff);
			}
			book.setCategory(dbCon.rs.getString("book_cate"));
			book.setSource(Book.Source.valueOf(dbCon.rs.getInt("book_source")));
			book.setStatus(Book.Status.valueOf(dbCon.rs.getInt("book_status")));
			book.setMoney(dbCon.rs.getFloat("book_money"));
			book.setComment(dbCon.rs.getString("comment"));
			result.add(book);
		}
		dbCon.rs.close();
		
		//Get the associated book tables.
		for(Book book : result){
			sql = " SELECT table_id, table_name FROM " + Params.dbName + ".book_table WHERE book_id = " + book.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			final List<Table> bookTbls = new ArrayList<Table>();
			while(dbCon.rs.next()){
				Table tbl = new Table(dbCon.rs.getInt("table_id"));
				tbl.setTableName(dbCon.rs.getString("table_name"));
				bookTbls.add(tbl);
			}
			dbCon.rs.close();
		}
		
		//TODO Get the associated book order.
		return result;
	}

	/**
	 * Delete the book record to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param bookId
	 * 			the book id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the book to delete does NOT exist 
	 */
	public static void deleteById(Staff staff, int bookId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			deleteById(dbCon, staff, bookId);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the book record to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param bookId
	 * 			the book id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the book to delete does NOT exist 
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int bookId) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(bookId)) == 0){
			throw new BusinessException(BookError.BOOK_RECORD_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the book record according to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition 
	 * @return the amount to book records deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(Book book : getByCond(dbCon, staff, extraCond)){
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".book WHERE book_id = " + book.getId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " DELETE FROM " + Params.dbName + ".book_table WHERE book_id = " + book.getId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " DELETE FROM " + Params.dbName + ".book_order WHERE book_id = " + book.getId();
			dbCon.stmt.executeUpdate(sql);
			
			amount++;
		}
		return amount;
	}
}
