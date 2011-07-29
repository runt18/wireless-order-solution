package com.wireless.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLException;
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
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;
/**
 * @author yzhang
 *
 */
class OrderHandler extends Handler implements Runnable{
	
    private Terminal _term = null;
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
			int bodyLen = (request.header.length[0] & 0x000000FF) + ((request.header.length[1] & 0x0000FF00) << 8);

			//check if request header's 2-byte length field equals the body's length				
			if(bodyLen != request.body.length){
				throw new Exception("The request's header length field doesn't match the its body length.");
			}

		    short model = Terminal.MODEL_BB;
		    int pin = 0;			
		    /**
		     * Extract the pin and model from the header of request package
		     */
			pin = (request.header.pin[0] & 0x000000FF) |
			   	   ((request.header.pin[1] & 0x000000FF) << 8) |
			       ((request.header.pin[2] & 0x000000FF) << 16) |
			       ((request.header.pin[3] & 0x000000FF) << 24); 
			
			model = (short)((request.header.pin[4] & 0x000000FF) | 
							((request.header.pin[5] & 0x000000FF) << 8)); 
			
			/**
			 * Verify to check if the terminal with this pin and model is valid or not.
			 */
			_term = VerifyPin.exec(pin, model);
			
				//handle query menu request
			if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_MENU){
				response = new RespQueryMenu(request.header, QueryMenu.exec(_term.pin, _term.modelID));

				//handle query restaurant request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_RESTAURANT){
				response = new RespQueryRestaurant(request.header, QueryRestaurant.exec(_term.pin, _term.modelID));

				//handle query order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER){
				short tableToQuery = ReqParser.parseQueryOrder(request);
				try{
					response = new RespQueryOrder(request.header, QueryOrder.exec(_term.pin, _term.modelID, tableToQuery));
				}catch(BusinessException e){
					if(e.errCode == ErrorCode.TABLE_IDLE){
						response = new RespNAK(request.header, ErrorCode.TABLE_IDLE);
					}else{
						throw e;
					}
				}

				//handle query order 2 request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_2){
				short tableToQuery = ReqParser.parseQueryOrder(request);
				Table table = QueryTable.exec(_term.pin, _term.modelID, tableToQuery);
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
				printOrder(request.header.reserved, InsertOrder.exec(_term.pin, _term.modelID, orderToInsert));
				response = new RespACK(request.header);

				//handle update order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.UPDATE_ORDER){
				Order orderToUpdate = ReqParser.parseInsertOrder(request);
				UpdateOrder.Result result = UpdateOrder.exec(_term.pin, _term.modelID, orderToUpdate);
				
				//print the extra food 
				byte printConf = Reserved.DEFAULT_CONF;				
				if((request.header.reserved & Reserved.PRINT_SYNC) != 0){
					printConf |= Reserved.PRINT_SYNC;
				}
				if((request.header.reserved & Reserved.PRINT_EXTRA_FOOD_2) != 0){
					printConf |= Reserved.PRINT_EXTRA_FOOD_2;
				}
				printOrder(printConf, result.extraOrder);
				
				//print canceled food
				printConf = Reserved.DEFAULT_CONF;
				if((request.header.reserved & Reserved.PRINT_SYNC) != 0){
					printConf |= Reserved.PRINT_SYNC;
				}
				if((request.header.reserved & Reserved.PRINT_CANCELLED_FOOD_2) != 0){
					printConf |= Reserved.PRINT_CANCELLED_FOOD_2;
				}
				printOrder(printConf, result.canceledOrder);
				
				//print the table transfer
				printConf = Reserved.DEFAULT_CONF;
				if((request.header.reserved & Reserved.PRINT_SYNC) != 0){
					printConf |= Reserved.PRINT_SYNC;
				}
				if((request.header.reserved & Reserved.PRINT_TRANSFER_TABLE_2) != 0){
					printConf |= Reserved.PRINT_TRANSFER_TABLE_2;
				}
				printOrder(printConf, orderToUpdate);
				
				response = new RespACK(request.header);

				//handle the cancel order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.CANCEL_ORDER){
				short tableToCancel = ReqParser.parseCancelOrder(request);
				CancelOrder.exec(_term.pin, _term.modelID, tableToCancel);
				response = new RespACK(request.header);

				//handle the pay order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.PAY_ORDER){
				Order orderToPay = ReqParser.parsePayOrder(request);
				orderToPay.restaurant_id = _term.restaurant_id;
				/**
				 * If pay order temporary, just only print the temporary receipt.
				 * Otherwise perform the pay action and print receipt 
				 */
				if((request.header.reserved & Reserved.PRINT_TEMP_RECEIPT_2) != 0){
					printOrder(request.header.reserved, PayOrder.queryOrder(_term.pin, _term.modelID, orderToPay));
				}else{
					printOrder(request.header.reserved, PayOrder.exec(_term.pin, _term.modelID, orderToPay));
				}
				response = new RespACK(request.header);

				//handle the print request
			}else if(request.header.mode == Mode.PRINT && request.header.type == Type.PRINT_BILL_2){
				
				Order reqToPrint = ReqParser.parsePrintReq(request);	
				
				Order orderToPrint = QueryOrder.execByID(_term.pin, _term.modelID, reqToPrint.id);
				
				if((request.header.reserved & Reserved.PRINT_TRANSFER_TABLE_2) != 0){
					orderToPrint.table_id = reqToPrint.table_id;
					orderToPrint.originalTableID = reqToPrint.originalTableID;
				}
				
				printOrder(request.header.reserved, orderToPrint);
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
	 * Print the order according the parameters
	 * 
	 * @param req
	 *            the raw data to the request package, We use the "Resvered"
	 *            field since the print parameters stored in this field
	 * @param orderToPrint
	 *            the order to print
	 * @throws PrintLogicException
	 *             throws if any logic exception occurred while performing print
	 *             action
	 */
	private void printOrder(byte printConf, Order orderToPrint) throws PrintLogicException{
		//find the printer connection socket to the restaurant for this terminal
		ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_term.restaurant_id));
		Socket[] connections = null;
		if(printerConn != null){
			connections = printerConn.toArray(new Socket[printerConn.size()]);			
		}else{
			connections = new Socket[0];
		}
		if(orderToPrint != null){
			/**
			 * Get the corresponding restaurant information
			 */
			Restaurant restaurant = null;
			try{
				restaurant = QueryRestaurant.exec(_term.restaurant_id);
			}catch(Exception e){
				restaurant = new Restaurant();
			}
			
			//check whether the print request is synchronized or asynchronous
			if((printConf & Reserved.PRINT_SYNC) != 0){
				/**
				 * if the print request is synchronized, then the insert order request must wait until
				 * the print request is done, and send the ACK or NAK to let the terminal know whether 
				 * the print actions is successfully or not
				 */	
				new PrintHandler(orderToPrint, connections, printConf, restaurant, _term.owner).run();						
				
			}else{
				/**
				 * if the print request is asynchronous, then the insert order request return an ACK immediately,
				 * regardless of the print request. In the mean time, the print request would be put to a 
				 * new thread to run.
				 */	
				WirelessSocketServer.threadPool.execute(new PrintHandler(orderToPrint, connections, printConf, restaurant, _term.owner));
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
/*	private void printUpdateOrder(ProtocolPackage req, UpdateOrder.Result result) throws PrintLogicException{
			
		//find the printer connection socket to the restaurant for this terminal
		ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_term.restaurant_id));
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
							new PrintHandler(result.extraOrder, connections[i], Reserved.PRINT_EXTRA_FOOD_2, _term.restaurant_id, _term.owner).run2();								
						}
						if(result.canceledOrder != null){
							new PrintHandler(result.canceledOrder, connections[i], Reserved.PRINT_CANCELLED_FOOD_2, _term.restaurant_id, _term.owner).run2();								
						}
					} catch (PrintSocketException e) {}

				}else{
					//perform print in asynchronous mode					 
					if(result.extraOrder != null){
						WirelessSocketServer.threadPool.execute(new PrintHandler(result.extraOrder, connections[i], Reserved.PRINT_EXTRA_FOOD_2, _term.restaurant_id, _term.owner));							
					}
					if(result.canceledOrder != null){
						WirelessSocketServer.threadPool.execute(new PrintHandler(result.canceledOrder, connections[i], Reserved.PRINT_CANCELLED_FOOD_2, _term.restaurant_id, _term.owner));							
					}
				}				
			}
		}				
	}*/

	

}



