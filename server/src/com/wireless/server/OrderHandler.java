package com.wireless.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.wireless.db.CancelOrder;
import com.wireless.db.InsertOrder;
import com.wireless.db.PayOrder;
import com.wireless.db.QueryMenu;
import com.wireless.db.QueryOrder;
import com.wireless.db.QueryRestaurant;
import com.wireless.db.QueryTable;
import com.wireless.db.UpdateOrder;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Mode;
import com.wireless.protocol.Order;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqParser;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.RespACK;
import com.wireless.protocol.RespNAK;
import com.wireless.protocol.RespOTAUpdate;
import com.wireless.protocol.RespPackage;
import com.wireless.protocol.RespQueryMenu;
import com.wireless.protocol.RespQueryOrder;
import com.wireless.protocol.RespQueryRestaurant;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
/**
 * @author yzhang
 *
 */
class OrderHandler extends Handler implements Runnable{
	
    private Connection _dbCon = null;
    private Statement _stmt = null;
    private ResultSet _rs = null;
    private int _restaurantID = -1;
    private String _owner = null;
    private short _model = Terminal.MODEL_BB;
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


			
//			String sql = "SELECT restaurant_id, expire_date, owner_name FROM "
//					+ WirelessSocketServer.database + ".terminal WHERE pin="
//					+ _pin;
//			_rs = _stmt.executeQuery(sql);
//
//			while (_rs.next()) {
//				_restaurantID = _rs.getInt("restaurant_id");
//				Date expiredDate = _rs.getDate("expire_date");
//				_expiredTimeMillis = expiredDate != null ? expiredDate.getTime() : 0;
//				_owner = _rs.getString("owner_name");
//			}
//			// in the case the terminal is not associated with any valid
//			// restaurant,
//			// the valid restaurant id is more than 10, id ranges from 1 through
//			// 10 is reserved for system,
//			// then throw an OrderBusinessException with "TERMINAL_NOT_ATTACHED"
//			// error code
//			if (_restaurantID <= 10) {
//				throw new OrderBusinessException(
//						"The terminal hasn't been associated with a restaurant.",
//						ErrorCode.TERMINAL_NOT_ATTACHED);
//			}
//			// in the case the terminal is expired
//			// throw an OrderBusinessException with "TERMINAL_EXPIRED" error
//			// code
//			if (System.currentTimeMillis() > _expiredTimeMillis) {
//				throw new OrderBusinessException("The terminal is expired.",
//						ErrorCode.TERMINAL_EXPIRED);
//			}

			Terminal term = VerifyPin.exec(_pin, _model);
			_restaurantID = term.restaurant_id;
			_owner = term.owner;
			
			//check the header's mode and type to determine which action is performed
			//handle query menu request
			if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_MENU){
				//FoodMenu foodMenu = execQueryMenu(request);
				//response = new RespQueryMenu(request.header, foodMenu);
				response = new RespQueryMenu(request.header, QueryMenu.exec(term.pin, term.modelID));

				//handle query restaurant request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_RESTAURANT){
				//response = new RespQueryRestaurant(request.header, execQueryRestaurant(request));
				response = new RespQueryRestaurant(request.header, QueryRestaurant.exec(term.pin, term.modelID));

				//handle query order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER){
				//response = new RespQueryOrder(request.header, execQueryOrder(request));
				short tableToQuery = ReqParser.parseQueryOrder(request);
				try{
					response = new RespQueryOrder(request.header, QueryOrder.exec(term.pin, term.modelID, tableToQuery));
				}catch(BusinessException e){
					if(e.errCode == ErrorCode.TABLE_IDLE){
						response = new RespNAK(request.header, ErrorCode.TABLE_IDLE);
					}else{
						throw e;
					}
				}

				//handle query order 2 request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_2){
				//execQueryOrder2(request);
				//response = new RespACK(request.header);
				short tableToQuery = ReqParser.parseQueryOrder(request);
				Table table = QueryTable.exec(term.pin, term.modelID, tableToQuery);
				if(table.status == Table.TABLE_BUSY){
					response = new RespACK(request.header);
					
				}else if(table.status == Table.TABLE_IDLE){
					response = new RespNAK(request.header, ErrorCode.TABLE_IDLE);
					
				}else{
					response = new RespNAK(request.header, ErrorCode.UNKNOWN);
				}
				
				//handle insert order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER){

				Order orderToInsert = ReqParser.parseInsertOrder(request);				
				printNewOrder(request, InsertOrder.exec(term.pin, term.modelID, orderToInsert));
				response = new RespACK(request.header);

				//handle update order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.UPDATE_ORDER){
				Order orderToUpdate = ReqParser.parseInsertOrder(request);
				UpdateOrder.Result result = UpdateOrder.exec(term.pin, term.modelID, orderToUpdate);
				printUpdateOrder(request, result);
				response = new RespACK(request.header);

				//handle the cancel order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.CANCEL_ORDER){
				//execCancelOrder(request);
				short tableToCancel = ReqParser.parseCancelOrder(request);
				CancelOrder.exec(term.pin, term.modelID, tableToCancel);
				response = new RespACK(request.header);

				//handle the pay order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.PAY_ORDER){
				Order orderToPay = ReqParser.parsePayOrder(request);
				printPaidOrder(request, PayOrder.exec(term.pin, term.modelID, orderToPay));
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
			
			
		}catch(BusinessException e){
			if(request != null){
				try{
					send(out, new RespNAK(request.header, e.errCode));
				}catch(IOException ex){}
			}
			e.printStackTrace();
			
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
/*	private FoodMenu execQueryMenu(ProtocolPackage req) throws SQLException{
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
		sql = "SELECT alias_id, name, discount, discount_2, discount_3, member_discount_1, member_discount_2, member_discount_3 FROM " + 
			  WirelessSocketServer.database + ".kitchen WHERE restaurant_id=" + _restaurantID;
		_rs = _stmt.executeQuery(sql);
		while(_rs.next()){
			kitchens.add(new Kitchen(_rs.getString("name"),
									  _rs.getShort("alias_id"),
									  (byte)(_rs.getFloat("discount") * 100),
									  (byte)(_rs.getFloat("discount_2") * 100),
									  (byte)(_rs.getFloat("discount_3") * 100),
									  (byte)(_rs.getFloat("member_discount_1") * 100),
									  (byte)(_rs.getFloat("member_discount_2") * 100),
									  (byte)(_rs.getFloat("member_discount_3") * 100)));
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
	}*/

	/**
	 * Access the db to get the restaurant information
	 * @param req the restaurant request package
	 * @return the restaurant information
	 * @throws SQLException if execute the SQL statements fail
	 */
/*	private Restaurant execQueryRestaurant(ProtocolPackage req) throws SQLException{
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
		*//**
		 * if the corresponding info not be found, then get the root's info as common
		 *//*
		if(restaurant.info.isEmpty()){
			sql = "SELECT restaurant_info FROM " + WirelessSocketServer.database + "." +
						"restaurant WHERE id=" + Restaurant.ADMIN;
			_rs = _stmt.executeQuery(sql);
			if(_rs.next()){
				restaurant.info = _rs.getString(1);
			}
		}			
		return restaurant;
	}*/
	
	/**
	 * Access the db to query order according the table, 
	 * which is contained in the query order request
	 * @param req the query order request
	 * @return the order object containing all the information if the order exist
	 * @throws SQLException if execute the SQL statements fail
	 * @throws OrderBusinessException if the table to query or the order to query doesn't exist.
	 */
/*	private Order execQueryOrder(ProtocolPackage req) throws SQLException, OrderBusinessException{
		short tableToQuery = ReqParser.parseQueryOrder(req);
		
		int orderID = getUnPaidOrderID(tableToQuery);

		return getOrderByID(orderID);
	}*/
	
	/**
	 * Access the db to check if the table has been order or not, 
	 * the table to request is contained in the query order request
	 * @param req the query order request
	 * @throws SQLException if execute the SQL statements fail
	 * @throws OrderBusinessException if the table to query or the order to query doesn't exist.
	 */
/*	private void execQueryOrder2(ProtocolPackage req) throws SQLException, OrderBusinessException{
		short tableToQuery = ReqParser.parseQueryOrder(req);
		getUnPaidOrderID(tableToQuery);
	}*/
	
	/**
	 * Access the db to insert the order, and then connect the print server to print the order.
	 * @param req the insert order request which contains all information of order
	 * @throws SQLException if execute the insert order to db not successfully
	 * 		   PrintException if fail to send print request 
	 * 		   OrderBusinessException if any ordered doesn't exist in db or disabled by user 
	 */
	private void printNewOrder(ProtocolPackage req, Order orderToInsert) throws SQLException, PrintLogicException, OrderBusinessException{
		
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
	
	/**
	 * Print the update order
	 * @param req the raw data to request
	 * @param result the update result containing two orders below.<br>
	 * 				 - The extra order.<br>
	 * 				 - The canceled order.
	 * @throws PrintLogicException 
	 */
	private void printUpdateOrder(ProtocolPackage req, UpdateOrder.Result result) throws PrintLogicException{
			
		//find the printer connection socket to the restaurant for this terminal
		ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_restaurantID));
		Socket[] connections = null;
		if(printerConn != null){
			connections = printerConn.toArray(new Socket[printerConn.size()]);			
		}
		if(connections != null){		
			for(int i = 0; i < connections.length; i++){
				//check whether the print request is synchronized or asynchronous
				if((req.header.reserved & Reserved.PRINT_SYNC) != 0){					
					//perform print in synchronized mode					
					try {
						if(result.extraOrder != null){
							new PrintHandler(result.extraOrder, connections[i], Reserved.PRINT_EXTRA_FOOD_2, _restaurantID, _owner).run2();								
						}
						if(result.canceledOrder != null){
							new PrintHandler(result.canceledOrder, connections[i], Reserved.PRINT_CANCELLED_FOOD_2, _restaurantID, _owner).run2();								
						}
					} catch (PrintSocketException e) {}

				}else{
					//perform print in asynchronous mode					 
					if(result.extraOrder != null){
						WirelessSocketServer.threadPool.execute(new PrintHandler(result.extraOrder, connections[i], Reserved.PRINT_EXTRA_FOOD_2, _restaurantID, _owner));							
					}
					if(result.canceledOrder != null){
						WirelessSocketServer.threadPool.execute(new PrintHandler(result.canceledOrder, connections[i], Reserved.PRINT_CANCELLED_FOOD_2, _restaurantID, _owner));							
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
/*	private void execCancelOrder(ProtocolPackage req) throws SQLException, OrderBusinessException{
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
	}*/
	
	/**
	 * Access the db to pay the order according to the table containing in the request
	 * @param req the cancel order request which contains the table to be paid
	 * @throws SQLException if execute SQL statements fails
	 * @throws OrderBusinessException if the order or table to be paid doesn't exist
	 */
	private void printPaidOrder(ProtocolPackage req, Order orderToPay) throws SQLException, OrderBusinessException, PrintLogicException{

		
/*		reqOrderInfo.id = getUnPaidOrderID(reqOrderInfo.tableID);
		
		//get the order along with discount to each food according to the payment and discount type
		Order orderToPay = getOrderByID(reqOrderInfo.id, 
									    reqOrderInfo.tableID,
									    reqOrderInfo.payType,
									    reqOrderInfo.discountType,
									    reqOrderInfo.memberID);
		
		orderToPay.actualPrice = reqOrderInfo.actualPrice;
		orderToPay.payManner = reqOrderInfo.payManner;
		
		*//**
		 * Calculate the total price of this order as below.
		 * total = food_price_1 + food_price_2 + ...
		 * food_price_n = (unit * discount + taste) * count
		 *//*
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
		*//**
		 * Update the values below to "order" table
		 * - total price
		 * - actual price
		 * - payment manner
		 * - terminal pin
		 * - pay order date
		 *//*
		String sql = "UPDATE `" + WirelessSocketServer.database + "`.`order` SET terminal_pin=" + _pin +
					", total_price=" + totalPrice + 
					", total_price_2=" + Util.price2Float(orderToPay.actualPrice, Util.INT_MASK_3) +
					", type=" + orderToPay.payManner + 
					", order_date=NOW()" + 
					" WHERE id=" + orderToPay.id;
		_stmt.addBatch(sql);
		

		*//**
		 * Two tasks below.
		 * 1 - update each food's discount to "order_food" table
		 * 2 - update the order count to "food" table
		 *//*

		for(int i = 0; i < orderToPay.foods.length; i++){
			float discount = (float)orderToPay.foods[i].discount / 100;
			sql = "UPDATE " + WirelessSocketServer.database + ".order_food SET discount=" + discount +
				  " WHERE order_id=" + orderToPay.id + 
				  " AND food_id=" + orderToPay.foods[i].alias_id;
			_stmt.addBatch(sql);
			
//			sql = "UPDATE " + WirelessSocketServer.database + 
//				  ".food SET order_count=order_count+1 WHERE alias_id=" + orderToPay.foods[i].alias_id +
//				  " AND restaurant_id=" + _restaurantID;
		}
		_stmt.executeBatch();*/
		
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
/*	private int getUnPaidOrderID(short tableToQuery) throws SQLException, OrderBusinessException{
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
				throw new OrderBusinessException("The table(alias_id=" + tableToQuery + ")to query is idle.", ErrorCode.TABLE_IDLE);
			}
			
		}else{
			throw new OrderBusinessException("The table(alias_id=" + tableToQuery + ") to query doesn't exist.", ErrorCode.TABLE_NOT_EXIST);
		}

	}*/
	
	/**
	 * Access db to get the order detail information for a specific order id.
	 * @param orderID the order id to be queried
	 * @param tableID the alias table id used to construct the order detail information
	 * @return the order detail information
	 * @throws SQLException if execute the SQL statement fail
	 * @throws OrderBusinessException if the table to query or the order to query doesn't exist.
	 */
/*	private Order getOrderByID(int orderID) throws SQLException, OrderBusinessException{
		
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
			food.kitchen = _rs.getByte("kitchen");
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

	}*/
	
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
/*	private Order getOrderByID(int orderID, short tableID, int payType, 
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
	}*/
}



