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
    private long _pin = 0;
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
			_pin = ((request.header.pin[0] & 0x00000000000000FF) |
					(request.header.pin[1] & 0x00000000000000FF) << 8 |
					(request.header.pin[2] & 0x00000000000000FF) << 16 |
					(request.header.pin[3] & 0x00000000000000FF) << 24 |
					(request.header.pin[4] & 0x00000000000000FF) << 32 |
					(request.header.pin[5] & 0x00000000000000FF) << 40) & 0x0000FFFFFFFFFFFFL; 
			String sql = "SELECT restaurant_id, expire_date, owner_name FROM " + WirelessSocketServer.database + ".terminal WHERE pin=" + _pin;
			_rs = _stmt.executeQuery(sql);   

			while(_rs.next()){
				_restaurantID = _rs.getInt("restaurant_id");
				Date expiredDate = _rs.getDate("expire_date");
				_expiredTimeMillis = expiredDate != null ? expiredDate.getTime() : 0;
				_owner = _rs.getString("owner_name");
			}   
			//in the case the terminal is not associated with any valid restaurant,
			//the valid restaurant id is more than 10, id ranges from 1 through 10 is reserved for system,
			//then throw an OrderBusinessException with "TERMINAL_NOT_ATTACHED" error code 
			if(_restaurantID <= 10){
				throw new OrderBusinessException("The terminal hasn't been associated with a restaurant.", 
						ErrorCode.TERMINAL_NOT_ATTACHED);
			}
			//in the case the terminal is expired
			//throw an OrderBusinessException with "TERMINAL_EXPIRED" error code 
			if(System.currentTimeMillis() > _expiredTimeMillis){
				throw new OrderBusinessException("The terminal is expired.", ErrorCode.TERMINAL_EXPIRED);		        	
			}

			//check the header's mode and type to determine which action is performed
			//handle query menu request
			if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_MENU){
				Food[] foodMenu = execQueryMenu(request);
				if(foodMenu != null){
					response = new RespQueryMenu(request.header, foodMenu);
				}else{
					response = new RespNAK(request.header);
				}

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
	 * @return the array containing the food's information if query menu successfully
	 * 		   the null array object if query menu not successfully
	 * @throws SQLException if execute the SQL statements fail		   
	 */
	private Food[] execQueryMenu(ProtocolPackage req) throws SQLException{
		ArrayList<Food> foodMenu = new ArrayList<Food>();
        //in the case the corresponding restaurant exist, and is not expired
        //then get the food menu to this restaurant
		String sql = "SELECT alias_id, name, unit_price FROM " + WirelessSocketServer.database + ".food WHERE restaurant_id=" + _restaurantID +
					 " AND enabled=1";
		_rs = _stmt.executeQuery(sql);
		while(_rs.next()){
			Food food = new Food(_rs.getShort("alias_id"),
					_rs.getString("name"),
					new Float(_rs.getFloat("unit_price")));
			foodMenu.add(food);
		}
		return foodMenu.toArray(new Food[0]);
	
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
		short tableToQuery = OrderReqParser.parseQueryOrder(req);
		
		int orderID = getUnPaidOrderID(tableToQuery);

		return getOrderByID(orderID, tableToQuery);
	}
	
	/**
	 * Access the db to check if the table has been order or not, 
	 * the table to request is contained in the query order request
	 * @param req the query order request
	 * @throws SQLException if execute the SQL statements fail
	 * @throws OrderBusinessException if the table to query or the order to query doesn't exist.
	 */
	private void execQueryOrder2(ProtocolPackage req) throws SQLException, OrderBusinessException{
		short tableToQuery = OrderReqParser.parseQueryOrder(req);
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
		Order orderToInsert = OrderReqParser.parseInsertOrder(req);
		
		String sql = null;	
		//calculate the real table id
		//the real id is in the form as "restaurant.id << 32 | table.alias_id"
		long realTableID = ((long)orderToInsert.tableID & 0x00000000FFFFFFFFL) | 
							(((long)_restaurantID << 32) & 0xFFFFFFFF00000000L);
		
		/**
		 * Here invoke "getUnPaidOrderID" is to check two status.
		 * One is check to see the table is exist or not.
		 * The other is check to see the table has been paid or not. 
		 */
		try{
			getUnPaidOrderID(orderToInsert.tableID);
			//throw the exception if the table is paid
			throw new OrderBusinessException("The table(id=" + realTableID +") has been paid.", ErrorCode.TABLE_HAS_PAID);
			
		}catch(OrderBusinessException e){ 			
			if(e.errCode == ErrorCode.TABLE_NOT_EXIST){
				//re-throw the exception if table doesn't exist
				throw e;			
			}else if(e.errCode == ErrorCode.TABLE_HAS_PAID){
				//re-throw the exception if the table is paid
				throw e;
			}else if(e.errCode == ErrorCode.ORDER_NOT_EXIST){
				//proceed to insert the new order if the table isn't paid 
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
			//the food's real id is in the form as "restaurant.id" << 32 | "food.alias_id"
			long realFoodID = ((long)orderToInsert.foods[i].alias_id & 0x00000000FFFFFFFFL) |
							(((long)_restaurantID << 32) & 0xFFFFFFFF00000000L);
			//get the associated food's unit price and name
			sql = "SELECT unit_price, name, kitchen FROM " +  WirelessSocketServer.database + 
				".food WHERE id=" + realFoodID + " AND enabled=1";
			_rs = _stmt.executeQuery(sql);
			//check if the food exist in db 
			if(_rs.next()){
				orderToInsert.foods[i].real_id = realFoodID;
				orderToInsert.foods[i].name = _rs.getString("name");
				int val = (int)(_rs.getFloat("unit_price") * 100);
				int unitPrice = ((val / 100) << 8) | (val % 100);
				orderToInsert.foods[i].setPrice(unitPrice);
				orderToInsert.foods[i].kitchen = _rs.getShort("kitchen");
			}else{
				throw new OrderBusinessException("The food(id=" + realFoodID + ") to query doesn't exit.", ErrorCode.MENU_EXPIRED);
			}
		}

		//insert to order table
		sql = "INSERT INTO `" + WirelessSocketServer.database + 
				"`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `custom_num`, `waiter`) VALUES (NULL, " + 
				_restaurantID + ", " + realTableID + ", " + _pin + ", NOW(), " + 
				orderToInsert.customNum + ", '" + _owner + "')";
		_stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
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
			sql = "INSERT INTO `" + WirelessSocketServer.database +
				"`.`order_food` (`order_id`, `food_id`, `order_count`, `unit_price`, `name`) VALUES (" +	
				orderToInsert.id + ", " + orderToInsert.foods[i].real_id + ", " + orderToInsert.foods[i].count2Float().toString() + 
				", " + orderToInsert.foods[i].price2Float().toString() + ", '" + orderToInsert.foods[i].name + "')";
			_stmt.addBatch(sql);
		}		
		_stmt.executeBatch();

		//find the printer connection socket to the restaurant for this terminal
		ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_restaurantID));
		//check whether the print request is synchronized or asynchronous
		if((req.header.reserved & Reserved.PRINT_SYNC) != 0){
			/**
			 * if the print request is synchronized, then the insert order request must wait until
			 * the print request is done, and send the ACK or NAK to let the terminal know whether 
			 * the print actions is successfully or not
			 */
			if(printerConn != null){
				Iterator<Socket> iter = printerConn.iterator();
				while(iter.hasNext()){
					byte[] printFuncs = {Reserved.PRINT_ORDER, Reserved.PRINT_ORDER_DETAIL};
					new PrintHandler(orderToInsert, iter.next(), printFuncs, _restaurantID, _owner).run2();
				}
			}else{
				throw new PrintLogicException("The printer server hasn't been registered to wireless order server.");
			}
		}else{
			/**
			 * if the print request is asynchronous, then the insert order request return an ACK immediately,
			 * regardless of the print request. In the mean time, the print request would be put to a 
			 * new thread to run.
			 */
			if(printerConn != null){
				Iterator<Socket> iter = printerConn.iterator();
				while(iter.hasNext()){
					byte[] printFuncs = {Reserved.PRINT_ORDER, Reserved.PRINT_ORDER_DETAIL};
					WirelessSocketServer.threadPool.execute(new PrintHandler(orderToInsert, iter.next(), printFuncs, _restaurantID, _owner));
				}
			}
		}
	}
	
	enum Action{
		Skip, Insert, Update;
	};
	/**
	 * Access the database to update the order specified in the request
	 * @param req the update order request with all the order information
	 * @throws SQLException if execute SQL statements fail
	 * @throws OrderBusinessException if the table or the order to be updated doesn't exist
	 */
	private void execUpdateOrder(ProtocolPackage req) throws SQLException, OrderBusinessException{
		Order orderToUpdate = OrderReqParser.parseInsertOrder(req);
		int orderID = getUnPaidOrderID(orderToUpdate.tableID);
		
		//calculate how many foods has been ordered 
		String sql = "SELECT COUNT(food_id) FROM `" + WirelessSocketServer.database + "`.`order_food` WHERE order_id=" + orderID;
		_rs = _stmt.executeQuery(sql);
		int nFoods = 0;
		if(_rs.next()){
			nFoods = _rs.getInt(1);
		}

		//query all the food's id and order count of this order
		long[] oldFoodIDs = new long[nFoods];
		float[] oldOrderCounts = new float[nFoods];
		sql = "SELECT food_id, order_count FROM `" + WirelessSocketServer.database + "`.`order_food` WHERE order_id=" + orderID;
		_rs = _stmt.executeQuery(sql);
		int index = 0;
		while(_rs.next()){
			oldFoodIDs[index] = _rs.getLong("food_id");
			oldOrderCounts[index] = _rs.getFloat("order_count");
			index++;
		}
		
		ArrayList<Long> insertFoodIDs = new ArrayList<Long>();
		ArrayList<String> insertFoodNames = new ArrayList<String>();
		ArrayList<Float> insertFoodCounts = new ArrayList<Float>();
		ArrayList<Float> insertFoodUnitPrices = new ArrayList<Float>();		
		ArrayList<Long> updateFoodIDs = new ArrayList<Long>();
		ArrayList<Float> updateFoodCounts = new ArrayList<Float>();
		
		for(int i = 0; i < orderToUpdate.foods.length; i++){
			/**
			 * Assume both food'id and order amount is not matched,
			 * and need to perform the insert action.
			 */
			Action action = Action.Insert;
			
			for(int j = 0; j < nFoods; j++){
				/**
				 * in the case the both food alias id and order count is matched
				 * means no change to this record
				 */
				if(orderToUpdate.foods[i].alias_id == (int)(oldFoodIDs[j] & 0x00000000FFFFFFFF) &&
					orderToUpdate.foods[i].count2Float().floatValue() == oldOrderCounts[j]){
					action = Action.Skip;
					break;
					
				/**
				 * in the case the food alias id is matched, but the order count not,
				 * means to update the order count to this record
				 */
				}else if(orderToUpdate.foods[i].alias_id == (int)(oldFoodIDs[j] & 0x00000000FFFFFFFF) &&
						orderToUpdate.foods[i].count2Float().floatValue() != oldOrderCounts[j]){
					action = Action.Update;
					break;					
				}
			}
			long realFoodID = ((long)orderToUpdate.foods[i].alias_id & 0x00000000FFFFFFFFL) |
								(((long)_restaurantID << 32) & 0xFFFFFFFF00000000L);
			/**
			 * If performing insert action, check to see whether the new food submitted by terminal exist in db or is 
			 * disabled by user. 
			 * If the food can't be found in db or has been disabled by user, means the menu in terminal has been expired,
			 * and then sent back an error to tell the terminal to update the menu.
			 */
			if(action == Action.Insert){
				sql = "SELECT name, unit_price, enabled FROM " + WirelessSocketServer.database + ".food WHERE id=" + realFoodID + " AND enabled=1";
				_rs = _stmt.executeQuery(sql);
				//check if the food to be inserted exist in db or not
				if(_rs.next()){
					insertFoodIDs.add(new Long(realFoodID));
					insertFoodNames.add(_rs.getString("name"));
					insertFoodUnitPrices.add(new Float(_rs.getFloat("unit_price")));
					insertFoodCounts.add(orderToUpdate.foods[i].count2Float());
				}else{
					throw new OrderBusinessException("The food(id=" + realFoodID + ") to query doesn't exist.", ErrorCode.MENU_EXPIRED);
				}

			/**
			 * If performing update action, we just update the order amount without checking its other info.
			 * So we might have a potential bug, 
			 * if the user updates the food using original alias id (means only modify the name or unit price),
			 * then the db's record would be different from the terminal.
			 * So we suggest the user had better update the menu in the case that no updated order exist.    
			 */
			}else if(action == Action.Update){
				updateFoodIDs.add(new Long(realFoodID));
				updateFoodCounts.add(orderToUpdate.foods[i].count2Float());
			}
		}
		
		_stmt.clearBatch();
		//insert the new ordered food
		for(int i = 0; i < insertFoodIDs.size(); i++){
			sql = "INSERT INTO `" + WirelessSocketServer.database + "`.`order_food` (`order_id`, `food_id`, `order_count`, `unit_price`, `name`) VALUES (" +
			orderID + ", " + insertFoodIDs.get(i).toString() + ", " + insertFoodCounts.get(i).toString() + 
			", " + insertFoodUnitPrices.get(i).toString() + ", '" + insertFoodNames.get(i) + "')";
			_stmt.addBatch(sql);
		}
		//update the ordered food
		for(int i = 0; i < updateFoodIDs.size(); i++){
			sql = "UPDATE `" + WirelessSocketServer.database + "`.`order_food` SET order_count=" + 
			updateFoodCounts.get(i).toString() + " WHERE order_id=" + orderID +
			" AND food_id=" + updateFoodIDs.get(i).toString();
			_stmt.addBatch(sql);			
		}
		//delete the original's food'id from "order_food" table if it's excluded in the updated's
		for(int i = 0; i < oldFoodIDs.length; i++){
			boolean isCancelledFood = true;
			for(int j = 0; j < orderToUpdate.foods.length; j++){
				if(((int)oldFoodIDs[i] & 0x00000000FFFFFFFF) == orderToUpdate.foods[j].alias_id){
					isCancelledFood = false;
					break;
				}
			}
			if(isCancelledFood){
				sql = "DELETE FROM `" + WirelessSocketServer.database + "`.`order_food` WHERE order_id=" + orderID +
						" AND food_id=" + oldFoodIDs[i];
				_stmt.addBatch(sql);
			}
		}

		//update the custom number depending on the order id to "order" table
		 sql = "UPDATE `" + WirelessSocketServer.database + "`.`order` SET custom_num=" + orderToUpdate.customNum +
				", terminal_pin=" + _pin + ", waiter='" + _owner + "' WHERE id=" + orderID;
		_stmt.addBatch(sql);

		_stmt.executeBatch();
	}
	
	/**
	 * Access the db to cancel the order according to the table containing in the request
	 * @param req the cancel order request which contains the table to be canceled
	 * @throws SQLException if execute SQL statements fail
	 * @throws OrderBusinessException if the order or the table to be canceled doesn't exist
	 */
	private void execCancelOrder(ProtocolPackage req) throws SQLException, OrderBusinessException{
		short tableToCancel = OrderReqParser.parseCancelOrder(req);
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
		Order payOrderInfo = OrderReqParser.parsePayOrder(req);
		payOrderInfo.id = getUnPaidOrderID(payOrderInfo.tableID);
		
		float totalPrice = payOrderInfo.price2Float().floatValue();
		_stmt.clearBatch();
		//update the total price and terminal pin in "order" table
		String sql = "UPDATE `" + WirelessSocketServer.database + "`.`order` SET terminal_pin=" + _pin +
					", total_price=" + totalPrice + " WHERE id=" + payOrderInfo.id;
		_stmt.addBatch(sql);
		//accumulate order price to total income in "restaurant" table
		sql = "UPDATE `" + WirelessSocketServer.database + "`.`restaurant` SET total_income=total_income+" + totalPrice +
				"WHERE id=" + _restaurantID;
		_stmt.addBatch(sql);
		_stmt.executeBatch();
		
		//get all the food's id of this order
		sql = "SELECT food_id FROM `" + WirelessSocketServer.database + "`.`order_food` WHERE order_id=" + payOrderInfo.id;
		_rs = _stmt.executeQuery(sql);
		ArrayList<String> execSql = new ArrayList<String>();
		//accumulate the order count to this food
		while(_rs.next()){
			execSql.add("UPDATE `" + WirelessSocketServer.database + "`.`food` SET order_count=order_count+1 WHERE id=" + _rs.getLong(1));
		}
		_stmt.clearBatch();
		for(int i = 0; i < execSql.size(); i++){
			_stmt.addBatch(execSql.get(i));
		}
		_stmt.executeBatch();
		
		//find the printer connection socket to the restaurant for this terminal
		ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_restaurantID));
		//check whether the print request is synchronized or asynchronous
		if((req.header.reserved & Reserved.PRINT_SYNC) != 0){
			/**
			 * if the print request is synchronized, then the pay order request must wait until
			 * the print request is done, and send the ACK or NAK to let the terminal know whether 
			 * the print actions is successfully or not
			 */
			if(printerConn != null){
				Iterator<Socket> iter = printerConn.iterator();
				while(iter.hasNext()){
					Order orderToPay = getOrderByID(payOrderInfo.id, payOrderInfo.tableID);
					orderToPay.setTotalPrice(payOrderInfo.getTotalPrice());
					new PrintHandler(orderToPay, iter.next(), Reserved.PRINT_RECEIPT, _restaurantID, _owner).run2();
				}
			}else{
				throw new PrintLogicException("The printer server hasn't been registered to wireless order server.");
			}
		}else{
			/**
			 * if the print request is asynchronous, then the pay order request return an ACK immediately,
			 * regardless of the print request. In the mean time, the print request would be put to the 
			 * thread pool to run.
			 */
			if(printerConn != null){
				Iterator<Socket> iter = printerConn.iterator();
				while(iter.hasNext()){
					Order orderToPay = getOrderByID(payOrderInfo.id, payOrderInfo.tableID);
					orderToPay.setTotalPrice(payOrderInfo.getTotalPrice());
					WirelessSocketServer.threadPool.execute(new PrintHandler(orderToPay, iter.next(), Reserved.PRINT_RECEIPT, _restaurantID, _owner));
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
		long realTableID = -1;
		_rs = _stmt.executeQuery(sql);
		if(_rs.next()){
			realTableID = _rs.getLong("id");
		}else{
			throw new OrderBusinessException("The table(id=" + realTableID + ") to query doesn't exist.", ErrorCode.TABLE_NOT_EXIST);
		}
		//query the order table to check if the order exist
		//in the case the record whose total_price equals -1.00,
		//means the order exist, then return the order id, 
		//otherwise throw an OrderBusinessExcpetion.
		 sql = "SELECT id FROM `" + WirelessSocketServer.database + 
					"`.`order` WHERE table_id = " + realTableID +
					" AND restaurant_id = " + _restaurantID +
					" AND total_price = -1.00";
		_rs = _stmt.executeQuery(sql);
		if(_rs.next()){
			return _rs.getInt(1);
		}else{
			throw new OrderBusinessException("The order to query doesn't exist.", ErrorCode.ORDER_NOT_EXIST);
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
	private Order getOrderByID(int orderID, short tableID) throws SQLException, OrderBusinessException{
		
		int nCustom = 0;
		//query the custom number from "order" table according to the order id
		String sql = "SELECT custom_num FROM `" + WirelessSocketServer.database + 
		"`.`order` WHERE id=" + orderID;
		_rs = _stmt.executeQuery(sql);
		if(_rs.next()){
			nCustom = _rs.getByte(1);
		}
		//query the food's id and order count associate with the order id for "order_food" table
		sql = "SELECT name, food_id, order_count, unit_price FROM `" + WirelessSocketServer.database + 
		"`.`order_food` WHERE order_id=" + orderID;
		_rs = _stmt.executeQuery(sql);
		ArrayList<Food> foods = new ArrayList<Food>();
		while(_rs.next()){
			Food food = new Food();
			food.name = _rs.getString("name");
			//note that the food id store in "order_food" table is the real food's id
			//means ("restaurant.id" << 32 | "food.alias_id") 
			//so we only get food's alias id represented by the lowest 4-byte value
			food.alias_id = (int)(_rs.getLong("food_id") & 0x00000000FFFFFFFFL);
			food.setCount(new Float(_rs.getFloat("order_count")));
			int val = (int)(_rs.getFloat("unit_price") * 100);
			int unitPrice = ((val / 100) << 8) | (val % 100);
			food.setPrice(unitPrice);
			foods.add(food);
		}
		
		Order orderInfo = new Order();
		orderInfo.id = orderID;
		orderInfo.tableID = tableID;
		orderInfo.customNum = nCustom;
		orderInfo.foods = foods.toArray(new Food[0]);
		return orderInfo;
		
		//query each food's unit price to calculate the total price 
		/*
		float totalPrice = 0;
		for(int i = 0; i < foods.size(); i++){
			//the food's real id is in the form as "restaurant.id" << 32 | "food.alias_id"
			long realFoodID = ((long)foods.get(i).alias_id & 0x00000000FFFFFFFFL) |
							(((long)_restaurantID << 32) & 0xFFFFFFFF00000000L);
			sql = "SELECT unit_price FROM `" + WirelessSocketServer.database + 
			"`.`food` WHERE id=" + new Long(realFoodID).toString();
			_rs = _stmt.executeQuery(sql);
			if(_rs.next()){
				totalPrice += _rs.getFloat("unit_price") * foods.get(i).count2Float().floatValue();
			}
		}	
		return new Order(foods.toArray(new Food[0]), tableID, 
						nCustom, new Float((float)Math.round(totalPrice * 100) / 100));*/
	}

}

