package com.wireless.db.book;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.FoodUnitDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BookError;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.DateUtil;
import com.wireless.sccon.ServerConnector;

public class BookDao {

	public static class ExtraCond{
		private int id;
		private String tele;
		private String member;
		private final List<Book.Status> statuses = new ArrayList<Book.Status>();
		private DutyRange bookRange;
		private DutyRange confirmRange;
		private int tableId;
		
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
		
		public ExtraCond addStatus(Book.Status status){
			this.statuses.add(status);
			return this;
		}
		
		public ExtraCond setBookRange(DutyRange range){
			this.bookRange = range;
			return this;
		}
		
		public ExtraCond setConfirmRange(DutyRange range){
			this.confirmRange = range;
			return this;
		}
		
		public ExtraCond setTable(int tableId){
			this.tableId = tableId;
			return this;
		}
		
		public ExtraCond setTable(Table table){
			this.tableId = table.getId();
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
			StringBuilder status = new StringBuilder();
			for(Book.Status s : statuses){
				if(status.length() != 0){
					status.append(",");
				}
				status.append(s.getVal());
			}
			if(status.length() != 0){
				extraCond.append(" AND book_status IN ( " + status.toString() + ")");
			}
			if(bookRange != null){
				if(bookRange.getOnDuty() != 0){
					extraCond.append(" AND book_date >= '" + bookRange.getOnDutyFormat() + "'");
				}
				if(bookRange.getOffDuty() != 0){
					extraCond.append(" AND book_date <= '" + bookRange.getOffDutyFormat() + "'");
				}
			}
			if(confirmRange != null){
				if(confirmRange.getOnDuty() != 0){
					extraCond.append(" AND book_confirm_date >= '" + confirmRange.getOnDutyFormat() + "'");
				}
				if(confirmRange.getOffDuty() != 0){
					extraCond.append(" AND book_confirm_date <= '" + confirmRange.getOffDutyFormat() + "'");
				}
			}
			if(tableId != 0){
				String sql = " SELECT book_id FROM " + Params.dbName + ".book_table WHERE table_id = " + tableId;
				extraCond.append(" AND book_id IN (" + sql + ")");
			}
			return extraCond.toString();
		}
	}

	/**
	 * Perform seat action according to builder {@link Book.SeatBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the seat builder {@link Book.SeatBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the book record has expired
	 * 			<li>the book status is NOT confirmed
	 * 			<li>the book order failed to insert
	 * 			<li>lack of book order
	 **/
	public static void seat(Staff staff, Book.SeatBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			seat(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform seat action according to builder {@link Book.SeatBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the seat builder {@link Book.SeatBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the book record has expired
	 * 			<li>the book status is NOT confirmed
	 * 			<li>the book status is NOT confirmed
	 * 			<li>the book order failed to insert
	 * 			<li>lack of book order
	 */
	public static void seat(DBCon dbCon, Staff staff, Book.SeatBuilder builder) throws SQLException, BusinessException{
		Book book = getById(dbCon, staff, builder.getBookId());
		if(book.getStatus() != Book.Status.CONFIRMED){
			throw new BusinessException("当前餐台状态是【" + book.getStatus().toString() + "】，不能入座", BookError.BOOK_RECORD_SEAT_FAIL);
		}
		if(book.isExpired()){
			throw new BusinessException("已超过预订时间，不能入座", BookError.BOOK_RECORD_EXPIRED);
		}
		if(book.getStatus() != Book.Status.CONFIRMED){
			throw new BusinessException("预订信息不是【" + Book.Status.CONFIRMED.toString() + "】，不能入座", BookError.BOOK_RECORD_SEAT_FAIL);
		}
		for(Order.InsertBuilder orderBuilder : builder.getBookOrders()){
			Table tbl = TableDao.getById(dbCon, staff, orderBuilder.build().getDestTbl().getId());
			if(tbl.isBusy()){
				throw new BusinessException("餐台【" + tbl.getName() + "】是就餐状态，不能入座", BookError.BOOK_RECORD_SEAT_FAIL);
			}
		}
		
		String sql;
		//Update the status to seated.
		sql = " UPDATE " + Params.dbName + ".book SET book_status = " + Book.Status.SEAT.getVal() + " WHERE book_id = " + book.getId();
		dbCon.stmt.executeUpdate(sql);
		
		final int nBookOrder = builder.getBookOrders().size();
		if(nBookOrder == 0){
			throw new BusinessException("缺少账单信息，不能预订入座");
		}
		for(Order.InsertBuilder orderBuilder : builder.getBookOrders()){
			try{
				ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, orderBuilder.setForce(true)
																											.setCustomNum(book.getAmount() / nBookOrder)
																											.setComment(book.getMember() + "预订"), 
																						 PrintOption.DO_PRINT));
				if(resp.header.type == Type.ACK){
					
				}else if(resp.header.type == Type.NAK){
					throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
				}
			}catch(IOException e){
				throw new BusinessException(e.getMessage());
			}
		}
	}
	
	/**
	 * Confirm the book record according to builder {@link Book.ConfirmBuilder}.
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
	 * Confirm the book record according to builder {@link Book.ConfirmBuilder}.
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
		Book book = getById(dbCon, staff, builder.build().getId());
		if(book.getStatus() == Book.Status.CREATED || book.getStatus() == Book.Status.CONFIRMED){
			update(dbCon, staff, builder.getBuilder());
		}else{
			throw new BusinessException("【" + book.getStatus().toString() + "】状态的预订信息不能进行确认操作", BookError.BOOK_RECORD_CONFIRM_FAIL);
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
	
	/**
	 * Insert the book record for manual according to specific builder {@link Book#InsertBuilder4Weixin}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert manual book record
	 * @return the book id 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if failed to insert the manual book record
	 */
	public static int insert(Staff staff, Book.InsertBuilder4Manual builder) throws SQLException, BusinessException{
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
	 * Insert the book record for manual according to specific builder {@link Book#InsertBuilder4Weixin}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert manual book record
	 * @return the book id 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if failed to insert the manual book record
	 */
	public static int insert(DBCon dbCon, Staff staff, Book.InsertBuilder4Manual builder) throws SQLException, BusinessException{
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
			  (builder.isStatusChanged() && book.getStatus() == Book.Status.CONFIRMED ? " ,book_confirm_date = NOW() " : "") +
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
		
		//Update the associated book order.
		if(book.hasOrder()){
			for(OrderFood of : book.getOrder().getOrderFoods()){
				sql = " INSERT INTO " + Params.dbName+ ".book_order" +
					  " (book_id, food_id, food_unit_id, amount, tmp_taste, tmp_taste_price) VALUES ( " +
					  book.getId() + "," +
					  of.asFood().getFoodId() + "," +
					  (of.hasFoodUnit() ? of.getFoodUnit().getId() : "NULL") + "," +
					  of.getCount() + "," +
					  (of.hasTmpTaste() ? "'" + of.getTasteGroup().getTmpTastePref() + "'" : "NULL") + "," +
					  (of.hasTmpTaste() ? of.getTasteGroup().getTmpTastePrice() : "NULL") +
					  ")";
				dbCon.stmt.executeUpdate(sql);

				if(of.getTasteGroup().hasNormalTaste()){
					for(Taste ntg : of.getTasteGroup().getNormalTastes()){
						sql = " INSERT INTO " + Params.dbName + ".book_order_taste" +
							  " (book_id, food_id, taste_id) VALUES( " +
							  book.getId() + "," +
							  of.asFood().getFoodId() + "," +
							  ntg.getTasteId() + 
							  ")";
						dbCon.stmt.executeUpdate(sql);
					}
				}
			}
		}
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
	 * 			throws if cases below
	 * 			<li>any booked table does NOT exist
	 * 			<li>any food to booked does NOT exist
	 * 			<li>any food unit to booked order food does NOT exist
	 * 			<li>any taste to booked order food does NOT exist
	 */
	public static Book getById(DBCon dbCon, Staff staff, int bookId) throws SQLException, BusinessException{
		List<Book> result = getByCond(dbCon, staff, new ExtraCond().setId(bookId));
		if(result.isEmpty()){
			throw new BusinessException(BookError.BOOK_RECORD_NOT_EXIST);
		}else{
			Book book = result.get(0);
			
			if(book.getOrder() != null){
				for(OrderFood of : book.getOrder().getOrderFoods()){
					//Get the detail to food.
					of.asFood().copyFrom(FoodDao.getById(dbCon, staff, of.asFood().getFoodId()));
					//Get the detail to food unit.
					if(of.hasFoodUnit()){
						of.setFoodUnit(FoodUnitDao.getById(dbCon, staff, of.getFoodUnit().getId()));
					}
					//Get the normal tastes.
					String sql = " SELECT taste_id FROM " + Params.dbName + ".book_order_taste WHERE book_id = " + result.get(0).getId() + " AND food_id = " + of.asFood().getFoodId();
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					while(dbCon.rs.next()){
						of.addTaste(TasteDao.getById(staff, dbCon.rs.getInt("taste_id")));
					}
					dbCon.rs.close();
				}
			}
			return book;
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
			  (extraCond != null ? extraCond.toString() : "") +
			  " ORDER BY book_date ASC ";
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
			if(dbCon.rs.getTimestamp("book_confirm_date") != null){
				book.setConfirmDate(dbCon.rs.getTimestamp("book_confirm_date").getTime());
			}
			book.setCategory(dbCon.rs.getString("book_cate"));
			book.setSource(Book.Source.valueOf(dbCon.rs.getInt("book_source")));
			book.setStatus(Book.Status.valueOf(dbCon.rs.getInt("book_status")));
			book.setMoney(dbCon.rs.getFloat("book_money"));
			book.setComment(dbCon.rs.getString("comment"));
			result.add(book);
		}
		dbCon.rs.close();
		
		for(Book book : result){
			//Get the associated book tables.
			sql = " SELECT table_id, table_name FROM " + Params.dbName + ".book_table WHERE book_id = " + book.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			final List<Table> bookTbls = new ArrayList<Table>();
			while(dbCon.rs.next()){
				Table tbl;
				try {
					tbl = TableDao.getById(staff, dbCon.rs.getInt("table_id"));
				} catch (BusinessException e) {
					tbl = new Table(dbCon.rs.getInt("table_id"));
					tbl.setTableName(dbCon.rs.getString("table_name"));
				}

				bookTbls.add(tbl);
				book.setTables(bookTbls);
			}
			dbCon.rs.close();
			
			Staff admin = null;
			try{
				admin = StaffDao.getAdminByRestaurant(dbCon, staff.getRestaurantId());
			}catch(BusinessException ignored){
				ignored.printStackTrace();
			}
			//Get the associated book order.
			Order bookOrder = null;
			sql = " SELECT food_id, food_unit_id, amount, tmp_taste, tmp_taste_price FROM " + Params.dbName + ".book_order WHERE book_id = " + book.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				if(bookOrder == null){
					bookOrder = new Order();
				}
				OrderFood of = new OrderFood();
				//Get the food.
				of.asFood().setFoodId(dbCon.rs.getInt("food_id"));
				//Get the food unit.
				if(dbCon.rs.getInt("food_unit_id") != 0){
					of.setFoodUnit(new FoodUnit(dbCon.rs.getInt("food_unit_id")));
				}
				//Get the amount.
				of.setCount(dbCon.rs.getFloat("amount"));
				//Get the temporary taste.
				if(dbCon.rs.getString("tmp_taste") != null){
					of.setTmpTaste(Taste.newTmpTaste(dbCon.rs.getString("tmp_taste"), dbCon.rs.getFloat("tmp_taste_price")));
				}
				try{
					bookOrder.addFood(of, admin);
				}catch(BusinessException ignored){
					ignored.printStackTrace();
				}
			}
			dbCon.rs.close();
			
			book.setOrder(bookOrder);
			
		}
		
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
			
			sql = " DELETE FROM " + Params.dbName + ".book_order_taste WHERE book_id = " + book.getId();
			dbCon.stmt.executeUpdate(sql);
			
			amount++;
		}
		return amount;
	}
}
