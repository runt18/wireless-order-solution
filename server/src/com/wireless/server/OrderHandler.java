package com.wireless.server;

import java.net.*;
import java.io.*;
import java.util.*;
import com.wireless.protocol.*;

import java.sql.Connection;   
import java.sql.DriverManager;   
import java.sql.ResultSet;   
import java.sql.SQLException;   
import java.sql.Statement;  
/**
 * @author yzhang
 *
 */
class OrderHandler extends Handler implements Runnable{
	
    private Connection _dbCon = null;
    private Statement _stmt = null;
    private ResultSet _rs = null;
    private int _restaurantID = -1;
    private long _expiredTimeMillis = 0;
    private String _owner = null;
    private short _model = PinGen.BLACK_BERRY;
    private int _pin = 0;
	private Socket _conn = null;
	private int _timeout = 10000;	//default timeout is 10s
	 
	OrderHandler(Socket connection){
		_conn = connection;
	}	
	
	OrderHandler(Socket connection, int timeout){
		_conn = connection;
		_timeout = timeout;
	}
	
	public void run(){
		
		ProtocolPackage request = null;
		InputStream in = null;
		OutputStream out = null;
		try{
			in = new BufferedInputStream(new DataInputStream(_conn.getInputStream()));
			out = new BufferedOutputStream(new DataOutputStream(_conn.getOutputStream()));
			
			request = recv(in, _timeout);				

			RespPackage response = null;
			int bodyLen = (request.header.length[0] & 0x000000FF) + 
			((request.header.length[1] & 0x0000FF00) << 8);

			//check if request header's 2-byte length field equals the body's length				
			if(bodyLen != request.body.length){
				throw new Exception("The request's header length field doesn't match the its body length.");
			}

			//open the database
			try {   
				Class.forName("com.mysql.jdbc.Driver");   
			} catch (ClassNotFoundException e) { 
				e.printStackTrace();   
			}   
			_dbCon = DriverManager.getConnection(WirelessSocketServer.url, 
					WirelessSocketServer.user, 
					WirelessSocketServer.password);   
			_stmt = _dbCon.createStatement();   
			
			//set names to UTF-8
			_stmt.execute("SET NAMES utf8");
			
			//access the terminal table from db according the terminal's pin to check if 
			//the terminal is associated with the specific restaurant 
			//and if the terminal is expired			
			_pin = (request.header.pin[0] & 0x000000FF) |
			   	   ((request.header.pin[1] & 0x000000FF) << 8) |
			       ((request.header.pin[2] & 0x000000FF) << 16) |
			       ((request.header.pin[3] & 0x000000FF) << 24); 
			
			_model = (short)((request.header.pin[4] & 0x000000FF) | 
							((request.header.pin[5] & 0x000000FF) << 8)); 


			
			String sql = "SELECT restaurant_id, expire_date, owner_name FROM "
					+ WirelessSocketServer.database + ".terminal WHERE pin="
					+ _pin;
			_rs = _stmt.executeQuery(sql);

			while (_rs.next()) {
				_restaurantID = _rs.getInt("restaurant_id");
				Date expiredDate = _rs.getDate("expire_date");
				_expiredTimeMillis = expiredDate != null ? expiredDate.getTime() : 0;
				_owner = _rs.getString("owner_name");
			}
			// in the case the terminal is not associated with any valid
			// restaurant,
			// the valid restaurant id is more than 10, id ranges from 1 through
			// 10 is reserved for system,
			// then throw an OrderBusinessException with "TERMINAL_NOT_ATTACHED"
			// error code
			if (_restaurantID <= 10) {
				throw new OrderBusinessException(
						"The terminal hasn't been associated with a restaurant.",
						ErrorCode.TERMINAL_NOT_ATTACHED);
			}
			// in the case the terminal is expired
			// throw an OrderBusinessException with "TERMINAL_EXPIRED" error
			// code
			if (System.currentTimeMillis() > _expiredTimeMillis) {
				throw new OrderBusinessException("The terminal is expired.",
						ErrorCode.TERMINAL_EXPIRED);
			}

			//check the header's mode and type to determine which action is performed
			//handle query menu request
			if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_MENU){
				FoodMenu foodMenu = execQueryMenu(request);
				response = new RespQueryMenu(request.header, foodMenu);

				//handle query restaurant request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_RESTAURANT){
				response = new RespQueryRestaurant(request.header, execQueryRestaurant(request));

				//handle query order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER){
				response = new RespQueryOrder(request.header, execQueryOrder(request));

				//handle query order 2 request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_2){
				execQueryOrder2(request);
				response = new RespACK(request.header);

				//handle insert order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER){

				execInsertOrder(request);
				response = new RespACK(request.header);

				//handle update order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.UPDATE_ORDER){
				execUpdateOrder(request);
				response = new RespACK(request.header);

				//handle the cancel order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.CANCEL_ORDER){
				execCancelOrder(request);
				response = new RespACK(request.header);

				//handle the pay order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.PAY_ORDER){
				execPayOrder(request);
				response = new RespACK(request.header);

				//handle the print request
			}else if(request.header.mode == Mode.PRINT && request.header.type == Type.PRINT_BILL_2){
				execPrintReq(request);
				response = new RespACK(request.header);
				
				//handle the ping test request
			}else if(request.header.mode == Mode.TEST && request.header.type == Type.PING){
				response = new RespACK(request.header);
				
				//handle the OTA update request
			}else if(request.header.mode == Mode.OTA && request.header.type == Type.GET_HOST){
				if(WirelessSocketServer.OTA_IP.length() == 0 || WirelessSocketServer.OTA_Port.length() == 0){
					response = new RespNAK(request.header);
				}else{
					response = new RespOTAUpdate(request.header, WirelessSocketServer.OTA_IP, WirelessSocketServer.OTA_Port);
				}
			}

			//send the response to terminal
			send(out, response);
			
			
		}catch(OrderBusinessException e){
			if(request != null){
				try{
					send(out, new RespNAK(request.header, e.errCode));
				}catch(IOException ex){}
			}
			e.printStackTrace();
				
		}catch(IOException e){
			if(request != null){
				try{
					send(out, new RespNAK(request.header));
				}catch(IOException ex){}
			}
			e.printStackTrace();
			
		}catch(SQLException e){
			if(request != null){
				try{
					send(out, new RespNAK(request.header));
				}catch(IOException ex){}
			}
			e.printStackTrace();			
			
		}catch(PrintLogicException e){
			if(request != null){
				try{
					send(out, new RespNAK(request.header, ErrorCode.PRINT_FAIL));
				}catch(IOException ex){}
			}
			e.printStackTrace();
		}
		catch(Exception e){
			if(request != null){
				try{
					send(out, new RespNAK(request.header));
				}catch(IOException ex){}
			}
			e.printStackTrace();
			
		}finally{
			try{
				if(_rs != null){
					_rs.close();
					_rs = null;
				}
				if(_stmt != null){
					_stmt.close();
					_stmt = null;
				}
				if(_dbCon != null){
					_dbCon.close();
					_dbCon = null;
				}
			}catch(SQLException e){
				System.err.println(e.toString());
			}
			
			try{
				if(in != null){ 
					in.close();
					in = null;
				}
				if(out != null){
					out.close();
					out = null;
				}
				if(_conn != null){
					_conn.close();
					_conn = null;
				}
			}catch(IOException e){
				System.err.println(e.toString());
			}
		}		
	}
	

	/**
	 * Access the db to query the menu according to the table,
	 * which contains in the query request.
	 * @param req the query menu request
	 * @return the food menu containing the foods and tastes information if succeed to request
	 * @throws SQLException if execute the SQL statements fail		   
	 */
	private FoodMenu execQueryMenu(ProtocolPackage req) throws SQLException{
		ArrayList<Food> foods = new ArrayList<Food>();
        //get all the food information to this restaurant
		String sql = "SELECT alias_id, name, unit_price, kitchen FROM " + WirelessSocketServer.database + ".food WHERE restaurant_id=" + _restaurantID +
					 " AND enabled=1";
		_rs = _stmt.executeQuery(sql);
		while(_rs.next()){
			Food food = new Food(_rs.getShort("alias_id"),
								 _rs.getString("name"),
								 new Float(_rs.getFloat("unit_price")),
								 _rs.getShort("kitchen"));
			foods.add(food);
		}
	
		_rs.close();
		//get all the kitchen information to this restaurant,
		ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
		sql = "SELECT alias_id, name, discount, member_discount_1, member_discount_2 FROM " + WirelessSocketServer.database + ".kitchen WHERE restaurant_id=" + _restaurantID;
		_rs = _stmt.executeQuery(sql);
		while(_rs.next()){
			kitchens.add(new Kitchen(_rs.getString("name"),
									  _rs.getShort("alias_id"),
									  (byte)(_rs.getFloat("discount") * 100),
									  (byte)(_rs.getFloat("member_discount_1") * 100),
									  (byte)(_rs.getFloat("member_discount_2") * 100)));
		}
		
		//Get the taste preferences to this restaurant sort by alias id in ascend order.
		//The lower alias id, the more commonly this preference used.
		//Put the most commonly used taste preference in first position 
		sql = "SELECT alias_id, preference, price FROM " + WirelessSocketServer.database + ".taste WHERE restaurant_id=" + _restaurantID + 
				" ORDER BY alias_id";
		_rs = _stmt.executeQuery(sql);
		ArrayList<Taste> tastes = new ArrayList<Taste>();
		while(_rs.next()){
			Taste taste = new Taste(_rs.getShort("alias_id"), 
									_rs.getString("preference"),
									new Float(_rs.getFloat("price")));
			tastes.add(taste);
		}
		
		return new FoodMenu(foods.toArray(new Food[foods.size()]), 
						    tastes.toArray(new Taste[tastes.size()]),
						    kitchens.toArray(new Kitchen[kitchens.size()]));
	}

	/**
	 * Access the db to get the restaurant information
	 * @param req the restaurant request package
	 * @return the restaurant information
	 * @throws SQLException if execute the SQL statements fail
	 */
	private Restaurant execQueryRestaurant(ProtocolPackage req) throws SQLException{
		Restaurant restaurant = new Restaurant();
		restaurant.owner = _owner;
		
		String sql = "SELECT restaurant_name, restaurant_info FROM " + WirelessSocketServer.database + "." +
					"restaurant WHERE id=(" + "SELECT restaurant_id FROM " + WirelessSocketServer.database + 
					".terminal WHERE pin=" + _pin + ")";
					
		_rs = _stmt.executeQuery(sql);
		if(_rs.next()){
			restaurant.name = _rs.getString("restaurant_name");
			restaurant.info = _rs.getString("restaurant_info");
		}
		/**
		 * if the corresponding info not be found, then get the root's info as common
		 */
		if(restaurant.info.isEmpty()){
			sql = "SELECT restaurant_info FROM " + WirelessSocketServer.database + "." +
						"restaurant WHERE id=" + Restaurant.ADMIN;
			_rs = _stmt.executeQuery(sql);
			if(_rs.next()){
				restaurant.info = _rs.getString(1);
			}
		}			
		return restaurant;
	}
	
	/**
	 * Access the db to query order according the table, 
	 * which is contained in the query order request
	 * @param req the query order request
	 * @return the order object containing all the information if the order exist
	 * @throws SQLException if execute the SQL statements fail
	 * @throws OrderBusinessException if the table to query or the order to query doesn't exist.
	 */
	private Order execQueryOrder(ProtocolPackage req) throws SQLException, OrderBusinessException{
		short tableToQuery = ReqParser.parseQueryOrder(req);
		
		int orderID = getUnPaidOrderID(tableToQuery);

		return getOrderByID(orderID);
	}
	
	/**
	 * Access the db to check if the table has been order or not, 
	 * the table to request is contained in the query order request
	 * @param req the query order request
	 * @throws SQLException if execute the SQL statements fail
	 * @throws OrderBusinessException if the table to query or the order to query doesn't exist.
	 */
	private void execQueryOrder2(ProtocolPackage req) throws SQLException, OrderBusinessException{
		short tableToQuery = ReqParser.parseQueryOrder(req);
		getUnPaidOrderID(tableToQuery);
	}
	
	/**
	 * Access the db to insert the order, and then connect the print server to print the order.
	 * @param req the insert order request which contains all information of order
	 * @throws SQLException if execute the insert order to db not successfully
	 * 		   PrintException if fail to send print request 
	 * 		   OrderBusinessException if any ordered doesn't exist in db or disabled by user 
	 */
	private void execInsertOrder(ProtocolPackage req) throws SQLException, PrintLogicException, OrderBusinessException{
		Order orderToInsert = ReqParser.parseInsertOrder(req);
		
		String sql = null;	
		
		/**
		 * Here invoke "getUnPaidOrderID" is to check two status.
		 * One is check to see the table is exist or not.
		 * The other is check to see the table has been paid or not. 
		 */
		try{
			getUnPaidOrderID(orderToInsert.tableID);
			//throw the exception if the table is paid
			throw new OrderBusinessException("The table(alias_id=" + orderToInsert.tableID +") has been paid.", ErrorCode.TABLE_BUSY);
			
		}catch(OrderBusinessException e){ 			
			if(e.errCode == ErrorCode.TABLE_NOT_EXIST){
				//re-throw the exception if table doesn't exist
				throw e;			
			}else if(e.errCode == ErrorCode.TABLE_BUSY){
				//re-throw the exception if the table is busy
				throw e;
			}else if(e.errCode == ErrorCode.TABLE_IDLE){
				//proceed to insert the new order if the table is idle
			}else{
				throw e;
			}
		}
		
		/**
		 * Get all the food's detail info submitted by terminal, 
		 * and then check whether the food exist in db or is disabled by user.
		 * If the food doesn't exist in db or is disabled by user,
		 * then notify the terminal that the food menu is expired.
		 */
		for(int i = 0; i < orderToInsert.foods.length; i++){

			//get the associated foods' unit price and name
			sql = "SELECT unit_price, name FROM " +  WirelessSocketServer.database + 
				".food WHERE alias_id=" + orderToInsert.foods[i].alias_id + " AND restaurant_id=" + _restaurantID + " AND enabled=1";
			_rs = _stmt.executeQuery(sql);
			//check if the food exist in db 
			if(_rs.next()){
				orderToInsert.foods[i].name = _rs.getString("name");
				int val = (int)(_rs.getFloat("unit_price") * 100);
				int unitPrice = ((val / 100) << 8) | (val % 100);
				orderToInsert.foods[i].price = unitPrice;
			}else{
				throw new OrderBusinessException("The food(alias_id=" + orderToInsert.foods[i].alias_id + ") to query doesn't exit.", ErrorCode.MENU_EXPIRED);
			}
			_rs.close();
			
			//get the associated foods' discount
			sql = "SELECT discount FROM " + WirelessSocketServer.database + ".kitchen WHERE restaurant_id=" + _restaurantID +
				" AND alias_id=" + orderToInsert.foods[i].kitchen;		
			_rs = _stmt.executeQuery(sql);
			if(_rs.next()){
				orderToInsert.foods[i].discount = (byte)(_rs.getFloat("discount") * 100);
			}
			
			//get the taste preference according to the taste id,
			//only if the food has the taste preference
			if(orderToInsert.foods[i].taste.alias_id != Taste.NO_TASTE){
				sql = "SELECT preference, price FROM " + WirelessSocketServer.database + 
					".taste WHERE restaurant_id=" + _restaurantID + 
					" AND alias_id=" + orderToInsert.foods[i].taste.alias_id;
				_rs = _stmt.executeQuery(sql);
				if(_rs.next()){
					orderToInsert.foods[i].taste.preference = _rs.getString("preference");
					orderToInsert.foods[i].taste.setPrice(_rs.getFloat("price"));
				}				
			}
		}

		//insert to order table
		sql = "INSERT INTO `" + WirelessSocketServer.database + 
				"`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_model`, `terminal_pin`, `order_date`, `custom_num`, `waiter`) VALUES (NULL, " + 
				_restaurantID + ", " + orderToInsert.tableID + ", " + _model + ", "+ _pin + ", NOW(), " + 
				orderToInsert.customNum + ", '" + _owner + "')";
		_stmt.execute(sql, Statement.RETURN_GENERATED_KEYS);
		//get the generated id to order 
		_rs = _stmt.getGeneratedKeys();
		if(_rs.next()){
			orderToInsert.id = _rs.getInt(1);
		}else{
			throw new SQLException("The id of order is not generated successfully.");
		}
		
		_stmt.clearBatch();
		//insert each ordered food
		for(int i = 0; i < orderToInsert.foods.length; i++){
			
			//insert the record to table "order_food"
			sql = "INSERT INTO `" + WirelessSocketServer.database +
				"`.`order_food` (`order_id`, `food_id`, `order_count`, `unit_price`, `name`, `discount`, `taste`, `taste_price`, `taste_id`, `kitchen`, `waiter`, `order_date`) VALUES (" +	
				orderToInsert.id + ", " + 
				orderToInsert.foods[i].alias_id + ", " + 
				orderToInsert.foods[i].count2Float().toString() + ", " + 
				Util.price2Float(orderToInsert.foods[i].price, Util.INT_MASK_2).toString() + ", '" + 
				orderToInsert.foods[i].name + "', " +
				(float)orderToInsert.foods[i].discount / 100 + ", '" +
				orderToInsert.foods[i].taste.preference + "', " + 
				Util.price2Float(orderToInsert.foods[i].taste.price, Util.INT_MASK_2).toString() + ", " +
				orderToInsert.foods[i].taste.alias_id + ", " + 
				orderToInsert.foods[i].kitchen + ", '" + 
				_owner + "', NOW()" + ")";
			
			_stmt.addBatch(sql);
		}		
		_stmt.executeBatch();


		//find the printer connection socket to the restaurant for this terminal
		ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_restaurantID));
		Socket[] connections = null;
		if(printerConn != null){
			connections = printerConn.toArray(new Socket[printerConn.size()]);			
		}
		if(connections != null){
			//check whether the print request is synchronized or asynchronous
			if((req.header.reserved & Reserved.PRINT_SYNC) != 0){
				/**
				 * if the print request is synchronized, then the insert order request must wait until
				 * the print request is done, and send the ACK or NAK to let the terminal know whether 
				 * the print actions is successfully or not
				 */	
				for(int i = 0; i < connections.length; i++){
					try{
						new PrintHandler(orderToInsert, connections[i], req.header.reserved, _restaurantID, _owner).run2();						
					}catch(PrintSocketException e){}
				}	
				
			}else{
				/**
				 * if the print request is asynchronous, then the insert order request return an ACK immediately,
				 * regardless of the print request. In the mean time, the print request would be put to a 
				 * new thread to run.
				 */	
				for(int i = 0; i < connections.length; i++){
					WirelessSocketServer.threadPool.execute(new PrintHandler(orderToInsert, connections[i], req.header.reserved, _restaurantID, _owner));
				}	
			}
		}
	}
	
	enum STATUS{
		NOT_MATCHED,
		FULL_MATCHED,
		FULL_MATCHED_BUT_COUNT
	}
	
	/**
	 * Access the database to update the order specified in the request
	 * @param req the update order request with all the order information
	 * @throws SQLException if execute SQL statements fail
	 * @throws PrintLogicExeption if fail to print extra food detail
	 * @throws OrderBusinessException if the table or the order to be updated doesn't exist
	 */
	private void execUpdateOrder(ProtocolPackage req) throws SQLException, PrintLogicException, OrderBusinessException{
		Order orderToUpdate = ReqParser.parseInsertOrder(req);
		
		/**
		 * There are two update order condition to deal with.
		 * 1 - The table is the same
		 * 2 - The table is different
		 * 
		 * In the 1st case, need to assure the table to update remains in busy.
		 * 
		 * In the 2nd case, need to assure two conditions
		 * 1 - original table remains in busy
		 * 2 - the table to be transferred is idle now
		 */
		
		int orderID = 0;
		
		/**
		 * In the case the table is the same as before,
		 * need to assure the table to update remains in busy.
		 */
		if(orderToUpdate.tableID == orderToUpdate.originalTableID){
			orderID = getUnPaidOrderID(orderToUpdate.tableID);
			
		/**
		 * In the case that the table is different from before,
		 * need to assure two conditions
		 * 1 - original table remains in busy
		 * 2 - the table to be transferred is idle now
		 */
		}else{			
			orderID = getUnPaidOrderID(orderToUpdate.originalTableID);
			try{
				getUnPaidOrderID(orderToUpdate.tableID);
				throw new OrderBusinessException("The table(alias_id=" + orderToUpdate.tableID + ")to be transferred is busy", ErrorCode.TABLE_BUSY);
			}catch(OrderBusinessException e){
				if(e.errCode == ErrorCode.TABLE_IDLE){
					//proceed to update the new order if the table to be transferred is idle
				}else{
					//otherwise re-throw the order business exception
					throw e;
				}
			}
		}
		
		//query all the food's id ,order count and taste preference of this order
		ArrayList<Food> originalRecords = new ArrayList<Food>();
		String sql = "SELECT food_id, unit_price, name, discount, SUM(order_count) AS order_sum, taste, taste_price, taste_id, kitchen FROM `" + 
					WirelessSocketServer.database + "`.`order_food` WHERE order_id=" + orderID + 
					" GROUP BY food_id, taste_id HAVING order_sum > 0";
		_rs = _stmt.executeQuery(sql);
		while(_rs.next()){
			Food food = new Food();
			food.alias_id = _rs.getShort("food_id");
			food.setPrice(new Float(_rs.getFloat("unit_price")));
			food.name = _rs.getString("name");
			food.discount = (byte)(_rs.getFloat("discount") * 100);
			food.setCount(new Float(_rs.getFloat("order_sum")));
			food.kitchen = _rs.getShort("kitchen");
			food.taste.alias_id = _rs.getShort("taste_id");
			food.taste.preference = _rs.getString("taste");
			food.taste.setPrice(new Float(_rs.getFloat("taste_price")));
			originalRecords.add(food);
		}
		
		ArrayList<Food> extraFoods = new ArrayList<Food>();
		ArrayList<Food> cancelledFoods = new ArrayList<Food>();
		
		for(int i = 0; i < orderToUpdate.foods.length; i++){
					
			/**
			 * Assume the order record is new, means not matched any original record.
			 * So the difference is equal to the amount of new order food
			 */
			STATUS status = STATUS.NOT_MATCHED;
			float diff = orderToUpdate.foods[i].count2Float().floatValue();
			
			for(int j = 0; j < originalRecords.size(); j++){
				/**
				 * In the case below,
				 * 1 - both food alias id and taste id is matched
				 * 2 - order count is matched
				 * Skip this record since it is totally the same as original.
				 */
				if(orderToUpdate.foods[i].equals(originalRecords.get(j)) &&
					orderToUpdate.foods[i].count == originalRecords.get(j).count){
					diff = 0;
					status = STATUS.FULL_MATCHED;
					break;
					
				/**
				 * In the case below,
				 * 1 - both food alias id and taste id is matched
				 * 2 - order count isn't matched
				 * Calculate the difference between these two records and insert a new record to keep track of this incremental
				 */
				}else if(orderToUpdate.foods[i].equals(originalRecords.get(j)) &&
						orderToUpdate.foods[i].count != originalRecords.get(j).count){

					//calculate the difference between the submitted and original record
					diff = orderToUpdate.foods[i].count2Float().floatValue() - originalRecords.get(j).count2Float().floatValue();					
					status = STATUS.FULL_MATCHED_BUT_COUNT;
					break;					
				}
			}
			
			if(status == STATUS.NOT_MATCHED || status == STATUS.FULL_MATCHED_BUT_COUNT){
				
				/**
				 * firstly, check to see whether the new food submitted by terminal exist in db or is disabled by user.
				 * If the food can't be found in db or has been disabled by user, means the menu in terminal has been expired,
				 * and then sent back an error to tell the terminal to update the menu.
				 * secondly, check to see whether the taste preference submitted by terminal exist in db or not.
				 * If the taste preference can't be found in db, means the taste in terminal has been expired,
				 * and then sent back an error to tell the terminal to update the menu.
				 */
				
				//get the food name and its unit price
				sql = "SELECT name, unit_price, kitchen FROM " + WirelessSocketServer.database + 
						".food WHERE alias_id=" + orderToUpdate.foods[i].alias_id + 
						" AND restaurant_id=" + _restaurantID +
						" AND enabled=1";
				_rs = _stmt.executeQuery(sql);
				//check if the food to be inserted exist in db or not
				Food food = new Food();
				if(_rs.next()){
					food.alias_id = orderToUpdate.foods[i].alias_id;
					food.name = _rs.getString("name");
					food.setPrice(new Float(_rs.getFloat("unit_price")));
					food.setCount(new Float((float)Math.round(Math.abs(diff) * 100) / 100));
					food.kitchen = _rs.getShort("kitchen");
				}else{
					throw new OrderBusinessException("The food(alias_id=" + orderToUpdate.foods[i].alias_id + ") to query doesn't exist.", ErrorCode.MENU_EXPIRED);
				}
				
				//get the associated foods' discount
				sql = "SELECT discount FROM " + WirelessSocketServer.database + ".kitchen WHERE restaurant_id=" + _restaurantID +
					" AND alias_id=" + orderToUpdate.foods[i].kitchen;		
				_rs = _stmt.executeQuery(sql);
				if(_rs.next()){
					food.discount = (byte)(_rs.getFloat("discount") * 100);
				}
				
				//get the taste preference only if the food has taste preference
				if(orderToUpdate.foods[i].taste.alias_id != Taste.NO_TASTE){
					sql = "SELECT preference, price FROM " + WirelessSocketServer.database + ".taste WHERE restaurant_id=" + _restaurantID +
						" AND alias_id=" + orderToUpdate.foods[i].taste.alias_id;
					_rs = _stmt.executeQuery(sql);
					//check if the taste preference exist in db
					if(_rs.next()){
						food.taste.alias_id = orderToUpdate.foods[i].taste.alias_id;
						food.taste.preference = _rs.getString("preference");
						food.taste.setPrice(_rs.getFloat("price"));
					}else{
						throw new OrderBusinessException("The taste(alias_id=" + orderToUpdate.foods[i].taste.alias_id + ") to query doesn't exist.", ErrorCode.MENU_EXPIRED);
					}
				}
				
				if(diff > 0){
					extraFoods.add(food);
				}else if(diff < 0){
					cancelledFoods.add(food);
				}
			}
		}	
		
		//insert the canceled order records
		for(int i = 0; i < originalRecords.size(); i++){
			/**
			 * If the sum to original record's order count is zero,
			 * means the record to this food has been canceled before.
			 * So we should skip to check this record.
			 */
			if(originalRecords.get(i).count > 0){
				boolean isCancelled = true;
				for(int j = 0; j < orderToUpdate.foods.length; j++){
					if(originalRecords.get(i).equals(orderToUpdate.foods[j])){
						isCancelled = false;
						break;
					}
				}
				/**
				 * If the original records are excluded from the submitted, means the food is to be cancel.
				 * So we insert an record whose order count is negative to original record
				 */
				if(isCancelled){
					cancelledFoods.add(originalRecords.get(i));
				}			
			}
		}
		
		_stmt.clearBatch();
		
		//insert the extra order food records
		for(int i = 0; i < extraFoods.size(); i++){

			sql = "INSERT INTO `" + WirelessSocketServer.database + "`.`order_food` (`order_id`, `food_id`, `order_count`, `unit_price`, `name`, `discount`, `taste_id`, `taste_price`, `taste`, `kitchen`, `waiter`, `order_date`) VALUES (" +
					orderID + ", " + extraFoods.get(i).alias_id + ", " + 
					extraFoods.get(i).count2String() + ", " + 
					Util.price2Float(extraFoods.get(i).price, Util.INT_MASK_2).toString() + ", '" + 
					extraFoods.get(i).name + "', " + 
					(float)extraFoods.get(i).discount / 100 + ", " +
					extraFoods.get(i).taste.alias_id + "," +
					Util.price2Float(extraFoods.get(i).taste.price, Util.INT_MASK_2).toString() + ", '" +
					extraFoods.get(i).taste.preference + "', " + 
					extraFoods.get(i).kitchen + ", '" + 
					_owner + "', NOW()" + ")";
			_stmt.addBatch(sql);			
		}
		
		//insert the canceled order food records 
		for(int i = 0; i < cancelledFoods.size(); i++){

			sql = "INSERT INTO `" + WirelessSocketServer.database + "`.`order_food` (`order_id`, `food_id`, `order_count`, `unit_price`, `name`, `discount`, `taste_id`, `taste_price`, `taste`, `kitchen`, `waiter`, `order_date`) VALUES (" +
					orderID + ", " + cancelledFoods.get(i).alias_id + ", " + 
					"-" + cancelledFoods.get(i).count2String() + ", " + 
					Util.price2Float(cancelledFoods.get(i).price, Util.INT_MASK_2).toString() + ", '" + 
					cancelledFoods.get(i).name + "', " + 
					(float)cancelledFoods.get(i).discount / 100 + ", " +
					cancelledFoods.get(i).taste.alias_id + "," +
					Util.price2Float(cancelledFoods.get(i).taste.price, Util.INT_MASK_2).toString() + ", '" +
					cancelledFoods.get(i).taste.preference + "', " + 
					cancelledFoods.get(i).kitchen + ", '" + 
					_owner + "', NOW()" + ")";
			_stmt.addBatch(sql);			
		}


		//update the custom number depending on the order id to "order" table
		 sql = "UPDATE `" + WirelessSocketServer.database + "`.`order` SET custom_num=" + orderToUpdate.customNum +
				", terminal_pin=" + _pin + ", waiter='" + _owner + "', table_id=" + orderToUpdate.tableID + " WHERE id=" + orderID;
		_stmt.addBatch(sql);

		_stmt.executeBatch();
		
		/**
		 * Notify the print handler to print the extra and canceled foods
		 */
		if(!extraFoods.isEmpty() || !cancelledFoods.isEmpty()){			
			
			ArrayList<Food> tmpFoods = new ArrayList<Food>();
			
			/**
			 * Find the extra foods to print
			 */
			tmpFoods.clear();
			for(int i = 0; i < extraFoods.size(); i++){				
				boolean isExtra = true;	
				/**
				 * In the case below, 
				 * 1 - food alias id is matched 
				 * 2 - taste alias id is NOT matched 
				 * 3 - order count is matched 
				 * Means just change the taste preference to this food. 
				 * We don't print this record.
				 */
				for(int j = 0; j < cancelledFoods.size(); j++){
					if(extraFoods.get(i).alias_id == cancelledFoods.get(j).alias_id &&
					   extraFoods.get(i).count == cancelledFoods.get(j).count &&
					   extraFoods.get(i).taste.alias_id != cancelledFoods.get(j).taste.alias_id){
							isExtra = false;
							break;
					}
				}				
				if(isExtra){
					tmpFoods.add(extraFoods.get(i));
				}
			}
			
			Order extraOrder = null;
			if(!tmpFoods.isEmpty()){
				extraOrder = new Order();
				extraOrder.id = orderID;
				extraOrder.tableID = orderToUpdate.tableID;
				extraOrder.foods = tmpFoods.toArray(new Food[tmpFoods.size()]);
			}
			
			/**
			 * Find the canceled foods to print
			 */
			tmpFoods.clear();
			for(int i = 0; i < cancelledFoods.size(); i++){				
				boolean isCancelled = true;	
				/**
				 * In the case below, 
				 * 1 - food alias id is matched 
				 * 2 - taste alias id is NOT matched 
				 * 3 - order count is matched 
				 * Means just change the taste preference to this food. 
				 * We don't print this record.
				 */
				for(int j = 0; j < extraFoods.size(); j++){
					if(cancelledFoods.get(i).alias_id == extraFoods.get(j).alias_id &&
						cancelledFoods.get(i).count == extraFoods.get(j).count &&
						cancelledFoods.get(i).taste.alias_id != extraFoods.get(j).taste.alias_id){
						
						isCancelled = false;
						break;
					}
				}				
				if(isCancelled){
					tmpFoods.add(cancelledFoods.get(i));
				}
			}
			
			Order cancelledOrder = null;
			if(!tmpFoods.isEmpty()){
				cancelledOrder = new Order();
				cancelledOrder.id = orderID;
				cancelledOrder.tableID = orderToUpdate.tableID;
				cancelledOrder.foods = tmpFoods.toArray(new Food[tmpFoods.size()]);
			}
			
			//find the printer connection socket to the restaurant for this terminal
			ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_restaurantID));
			Socket[] connections = null;
			if(printerConn != null){
				connections = printerConn.toArray(new Socket[printerConn.size()]);			
			}
			if(connections != null){				
				//check whether the print request is synchronized or asynchronous
				if((req.header.reserved & Reserved.PRINT_SYNC) != 0){					
					//perform print in synchronized mode					
					for (int i = 0; i < connections.length; i++) {
						try {
							if(extraOrder != null){
								new PrintHandler(extraOrder, connections[i], Reserved.PRINT_EXTRA_FOOD_2, _restaurantID, _owner).run2();								
							}
							if(cancelledOrder != null){
								new PrintHandler(cancelledOrder, connections[i], Reserved.PRINT_CANCELLED_FOOD_2, _restaurantID, _owner).run2();								
							}
						} catch (PrintSocketException e) {}
					}
	
				}else{
					//perform print in asynchronous mode					 
					for(int i = 0; i < connections.length; i++){
						if(extraOrder != null){
							WirelessSocketServer.threadPool.execute(new PrintHandler(extraOrder, connections[i], Reserved.PRINT_EXTRA_FOOD_2, _restaurantID, _owner));							
						}
						if(cancelledOrder != null){
							WirelessSocketServer.threadPool.execute(new PrintHandler(cancelledOrder, connections[i], Reserved.PRINT_CANCELLED_FOOD_2, _restaurantID, _owner));							
						}
					}
				}
			}
		}		
	}
	
	/**
	 * Access the db to cancel the order according to the table containing in the request
	 * @param req the cancel order request which contains the table to be canceled
	 * @throws SQLException if execute SQL statements fail
	 * @throws OrderBusinessException if the order or the table to be canceled doesn't exist
	 */
	private void execCancelOrder(ProtocolPackage req) throws SQLException, OrderBusinessException{
		short tableToCancel = ReqParser.parseCancelOrder(req);
		int orderID = getUnPaidOrderID(tableToCancel);
		_stmt.clearBatch();
		//delete the records related to the order id and food id in "order_food" table
		String sql = "DELETE FROM `" + WirelessSocketServer.database + "`.`order_food` WHERE order_id=" + orderID;
		_stmt.addBatch(sql);
		//delete the corresponding order record in "order" table
		sql = "DELETE FROM `" + WirelessSocketServer.database + "`.`order` WHERE id=" + orderID;
		_stmt.addBatch(sql);
		_stmt.executeBatch();
	}
	
	/**
	 * Access the db to pay the order according to the table containing in the request
	 * @param req the cancel order request which contains the table to be paid
	 * @throws SQLException if execute SQL statements fails
	 * @throws OrderBusinessException if the order or table to be paid doesn't exist
	 */
	private void execPayOrder(ProtocolPackage req) throws SQLException, OrderBusinessException, PrintLogicException{
		Order reqOrderInfo = ReqParser.parsePayOrder(req);
		
		reqOrderInfo.id = getUnPaidOrderID(reqOrderInfo.tableID);
		
		//get the order along with discount to each food according to the payment and discount type
		Order orderToPay = getOrderByID(reqOrderInfo.id, 
									    reqOrderInfo.tableID,
									    reqOrderInfo.payType,
									    reqOrderInfo.discountType,
									    reqOrderInfo.memberID);
		
		orderToPay.actualPrice = reqOrderInfo.actualPrice;
		orderToPay.payManner = reqOrderInfo.payManner;
		
		/**
		 * Calculate the total price of this order as below.
		 * total = food_price_1 + food_price_2 + ...
		 * food_price_n = (unit * discount + taste) * count
		 */
		float totalPrice = 0;
		for(int i = 0; i < orderToPay.foods.length; i++){
			float discount = (float)Math.round(orderToPay.foods[i].discount) / 100;
			float foodPrice = Util.price2Float(orderToPay.foods[i].price, Util.INT_MASK_2).floatValue();
			float tastePrice = Util.price2Float(orderToPay.foods[i].taste.price, Util.INT_MASK_2).floatValue();
			totalPrice += (foodPrice * discount + tastePrice) * orderToPay.foods[i].count2Float().floatValue();
		}
		totalPrice = (float)Math.round(totalPrice * 100) / 100;
		orderToPay.setTotalPrice(totalPrice);
		
		_stmt.clearBatch();
		/**
		 * Update the values below to "order" table
		 * - total price
		 * - actual price
		 * - payment manner
		 * - terminal pin
		 * - pay order date
		 */
		String sql = "UPDATE `" + WirelessSocketServer.database + "`.`order` SET terminal_pin=" + _pin +
					", total_price=" + totalPrice + 
					", total_price_2=" + Util.price2Float(orderToPay.actualPrice, Util.INT_MASK_3) +
					", type=" + orderToPay.payManner + 
					", order_date=NOW()" + 
					" WHERE id=" + orderToPay.id;
		_stmt.addBatch(sql);
		

		/**
		 * Two tasks below.
		 * 1 - update each food's discount to "order_food" table
		 * 2 - update the order count to "food" table
		 */

		for(int i = 0; i < orderToPay.foods.length; i++){
			float discount = (float)orderToPay.foods[i].discount / 100;
			sql = "UPDATE " + WirelessSocketServer.database + ".order_food SET discount=" + discount +
				  " WHERE order_id=" + orderToPay.id + 
				  " AND food_id=" + orderToPay.foods[i].alias_id;
			_stmt.addBatch(sql);
			
			sql = "UPDATE " + WirelessSocketServer.database + 
				  ".food SET order_count=order_count+1 WHERE alias_id=" + orderToPay.foods[i].alias_id +
				  " AND restaurant_id=" + _restaurantID;
		}
		_stmt.executeBatch();
		
		//find the printer connection socket to the restaurant for this terminal
		ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_restaurantID));
		Socket[] connections = null;
		if(printerConn != null){
			connections = printerConn.toArray(new Socket[printerConn.size()]);
		}
		if(connections != null){
			//check whether the print request is synchronized or asynchronous
			if((req.header.reserved & Reserved.PRINT_SYNC) != 0){
				//perform to print receipt synchronize
				for(int i = 0; i < connections.length; i++){					
					try{
						new PrintHandler(orderToPay, connections[i], req.header.reserved, _restaurantID, _owner).run2();
					}catch(PrintSocketException e){}
				}	
				
			}else{
				//perform to print receipt async
				for(int i = 0; i < connections.length; i++){
					WirelessSocketServer.threadPool.execute(new PrintHandler(orderToPay, connections[i], req.header.reserved, _restaurantID, _owner));					
				}	
			}
		}
	}
	
	/**
	 * Perform to print the receipt according to the request
	 * @param req the request containing the print information
	 * @throws SQLException throws if execute SQL statements fails
	 * @throws PrintLogicException throws if print fails
	 */
	private void execPrintReq(ProtocolPackage req) throws SQLException, PrintLogicException{
		Order orderToPrint = ReqParser.parsePrintReq(req);
		/**
		 * Get all the food's detail info submitted by terminal, 
		 * and then check whether the food exist in db or is disabled by user.
		 * If the food doesn't exist in db or is disabled by user,
		 * then notify the terminal that the food menu is expired.
		 */
		for(int i = 0; i < orderToPrint.foods.length; i++){

			String sql = "SELECT unit_price, name, kitchen FROM " +  WirelessSocketServer.database + 
						".food WHERE alias_id=" + orderToPrint.foods[i].alias_id + 
						" AND restaurant_id=" + _restaurantID + 
						" AND enabled=1";
			_rs = _stmt.executeQuery(sql);
			//check if the food exist in db 
			if(_rs.next()){
				orderToPrint.foods[i].name = _rs.getString("name");
				int val = (int)(_rs.getFloat("unit_price") * 100);
				int unitPrice = ((val / 100) << 8) | (val % 100);
				orderToPrint.foods[i].price = unitPrice;
				orderToPrint.foods[i].kitchen = _rs.getShort("kitchen");
			}else{
				throw new PrintLogicException("The food(alias_id=" + orderToPrint.foods[i].alias_id + ") to query doesn't exit.", ErrorCode.MENU_EXPIRED);
			}
		}
		
		//find the printer connection socket to the restaurant for this terminal
		ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_restaurantID));
		Socket[] connections = null;
		if(printerConn != null){
			connections = printerConn.toArray(new Socket[printerConn.size()]);			
		}
		if(connections != null){
			//check whether the print request is synchronized or asynchronous
			if((req.header.reserved & Reserved.PRINT_SYNC) != 0){
				//perform print in synchronized mode
				for(int i = 0; i < connections.length; i++){
					try{
						new PrintHandler(orderToPrint, connections[i], req.header.reserved, _restaurantID, _owner).run2();						
					}catch(PrintSocketException e){}
				}
				
			}else{	
				//perform print in asynchronous mode 
				for(int i = 0; i < connections.length; i++){
					WirelessSocketServer.threadPool.execute(new PrintHandler(orderToPrint, connections[i], req.header.reserved, _restaurantID, _owner));
				}
			}
		}
	}
	
	/**
	 * Access the database to get the order id hasn't been paid according to the table id and restaurant id.
	 * @param tableToQuery the table alias id to be checked retrieved from the request 
	 * @return return the order id if the corresponding order exist
	 * @throws SQLException if execute the SQL statement fail
	 * @throws OrderBusinessException if the table to query or the order to query doesn't exist.
	 */
	private int getUnPaidOrderID(short tableToQuery) throws SQLException, OrderBusinessException{
		//query the "table" table according to the table alias id 
		//and the restaurant id to get the real table id
		String sql = "SELECT id, enabled FROM `" + WirelessSocketServer.database +
					"`.`table` WHERE alias_id=" + tableToQuery + " AND restaurant_id=" + _restaurantID + " AND enabled=1";
		_rs = _stmt.executeQuery(sql);
		if(_rs.next()){
			//query the order table to check if the order exist
			//in the case the record whose total_price equals NULL,
			//means the order exist, then return the order id, 
			//otherwise throw an OrderBusinessExcpetion.
			 sql = "SELECT id FROM `" + WirelessSocketServer.database + 
						"`.`order` WHERE table_id = " + tableToQuery +
						" AND restaurant_id = " + _restaurantID +
						" AND total_price IS NULL";
			_rs = _stmt.executeQuery(sql);
			if(_rs.next()){
				return _rs.getInt(1);
			}else{
				throw new OrderBusinessException("The table(alias_id=" + tableToQuery + ")to query is busy.", ErrorCode.TABLE_IDLE);
			}
			
		}else{
			throw new OrderBusinessException("The table(alias_id=" + tableToQuery + ") to query doesn't exist.", ErrorCode.TABLE_NOT_EXIST);
		}

	}
	
	/**
	 * Access db to get the order detail information for a specific order id.
	 * @param orderID the order id to be queried
	 * @param tableID the alias table id used to construct the order detail information
	 * @return the order detail information
	 * @throws SQLException if execute the SQL statement fail
	 * @throws OrderBusinessException if the table to query or the order to query doesn't exist.
	 */
	private Order getOrderByID(int orderID) throws SQLException, OrderBusinessException{
		
		int nCustom = 0;
		short tableID = 0;
		//query the custom number from "order" table according to the order id
		String sql = "SELECT custom_num, table_id FROM `" + WirelessSocketServer.database + 
					"`.`order` WHERE id=" + orderID;
		_rs = _stmt.executeQuery(sql);
		if(_rs.next()){
			nCustom = _rs.getByte("custom_num");
			tableID = _rs.getShort("table_id");
		}
		//query the food's id and order count associate with the order id for "order_food" table
		sql = "SELECT name, food_id, SUM(order_count) AS order_sum, unit_price, discount, kitchen, taste, taste_price, taste_id FROM `" + 
				WirelessSocketServer.database + 
				"`.`order_food` WHERE order_id=" + orderID +
				" GROUP BY food_id, taste_id HAVING order_sum > 0";
		_rs = _stmt.executeQuery(sql);
		ArrayList<Food> foods = new ArrayList<Food>();
		while(_rs.next()){
			Food food = new Food();
			food.name = _rs.getString("name");
			food.alias_id = _rs.getInt("food_id");
			food.setCount(new Float(_rs.getFloat("order_sum")));
			food.setPrice(new Float(_rs.getFloat("unit_price")));
			food.discount = (byte)(_rs.getFloat("discount") * 100);
			food.kitchen = _rs.getShort("kitchen");
			food.taste.preference = _rs.getString("taste");
			food.taste.setPrice(_rs.getFloat("taste_price"));
			food.taste.alias_id = _rs.getShort("taste_id");
			foods.add(food);			
		}
		
		Order orderInfo = new Order();
		orderInfo.id = orderID;
		orderInfo.tableID = tableID;
		orderInfo.customNum = nCustom;
		orderInfo.foods = foods.toArray(new Food[foods.size()]);
		return orderInfo;		

	}
	
	/**
	 * Get the order detail information for a specific order id,
	 * and get the discount to each food according the payment and discount type 
	 * @param orderID the id to this order
	 * @param tableID the table id to this order
	 * @param payType the payment type to this order, it is one of the values below.<br>
	 * 		  - PAY_NORMAL<br>
	 * 		  - PAY_MEMBER
	 * @param discountType the discount type to this order, it is one of the values below.<br>
	 * 		  - DISCOUNT_1<br>
	 * 		  - DISCOUNT_2
	 * @param memberID the member id to this order if the payment type is PAY_MEMBER, otherwise is null
	 * @return the order containing all of the information
	 * @throws SQLException if fail to execute any SQL statement 
	 * @throws OrderBusinessException
	 */
	private Order getOrderByID(int orderID, short tableID, int payType, 
								int discountType, String memberID) throws SQLException, OrderBusinessException{
		
		Order orderInfo = getOrderByID(orderID);
		String discount = "discount";
		if(payType == Order.PAY_NORMAL && discountType == Order.DISCOUNT_1){
			discount = "discount";
			
		}else if(payType == Order.PAY_NORMAL && discountType == Order.DISCOUNT_2){
			discount = "discount_2";
			
		}else if(payType == Order.PAY_MEMBER){
			//validate the member id
			String sql = "SELECT id FROM " + WirelessSocketServer.database + 
						 ".member WHERE restaurant_id=" + _restaurantID + 
						 " AND alias_id='" + memberID + "'";
			_rs = _stmt.executeQuery(sql);
			if(_rs.next()){
				if(discountType == Order.DISCOUNT_1){
					discount = "member_discount_1";
				}else if(discountType == Order.DISCOUNT_2){
					discount = "member_discount_2";
				}
			}else{
				throw new OrderBusinessException("The member id(" + memberID + ") is invalid.", ErrorCode.MEMBER_INVALID);
			}
		}
		
		for(int i = 0; i < orderInfo.foods.length; i++){
			//get the discount to each food according to the payment and discount type
			String sql = "SELECT " + discount + " FROM " + WirelessSocketServer.database + 
			".kitchen WHERE restaurant_id=" + _restaurantID + 
			" AND alias_id=" + orderInfo.foods[i].kitchen;
			
			_rs = _stmt.executeQuery(sql);
			if(_rs.next()){
				orderInfo.foods[i].discount = (byte)(_rs.getFloat(discount) * 100);
			}
			_rs.close();
		}
		orderInfo.payType = payType;
		orderInfo.discountType = discountType;
		orderInfo.memberID = memberID; 
		return orderInfo;
	}
}



