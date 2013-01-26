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
import java.util.List;

import com.wireless.db.CancelOrder;
import com.wireless.db.DBCon;
import com.wireless.db.InsertOrder;
import com.wireless.db.QueryMenu;
import com.wireless.db.QueryRegion;
import com.wireless.db.QueryRestaurant;
import com.wireless.db.QueryStaffTerminal;
import com.wireless.db.QueryTable;
import com.wireless.db.TransTblDao;
import com.wireless.db.UpdateOrder;
import com.wireless.db.VerifyPin;
import com.wireless.db.foodAssociation.QueryFoodAssociationDao;
import com.wireless.db.foodGroup.CalcFoodGroupDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.payment.ConsumeMaterial;
import com.wireless.db.payment.PayOrder;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Department;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Mode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderDiff.DiffResult;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Pager;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqInsertOrderParser;
import com.wireless.protocol.ReqParser;
import com.wireless.protocol.ReqPayOrderParser;
import com.wireless.protocol.ReqPrintOrder2;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.RespACK;
import com.wireless.protocol.RespNAK;
import com.wireless.protocol.RespOTAUpdate;
import com.wireless.protocol.RespPackage;
import com.wireless.protocol.RespQueryFoodAssociation;
import com.wireless.protocol.RespQueryFoodGroup;
import com.wireless.protocol.RespQueryMenu;
import com.wireless.protocol.RespQueryOrder;
import com.wireless.protocol.RespQueryRegion;
import com.wireless.protocol.RespQueryRestaurant;
import com.wireless.protocol.RespQuerySellOut;
import com.wireless.protocol.RespQueryStaff;
import com.wireless.protocol.RespQueryTable;
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
			int bodyLen = (request.header.length[0] & 0x000000FF) | ((request.header.length[1] & 0x000000FF) << 8);

			//check if request header's 2-byte length field equals the body's length				
			if(bodyLen != request.body.length){
				throw new Exception("The request's header length field doesn't match the its body length.");
			}

		    short model = Terminal.MODEL_BB;
		    long pin = 0;			
		    /**
		     * Extract the pin and model from the header of request package
		     */
			pin = (((long)request.header.pin[0] & 0x00000000000000FF) |
			   	   (((long)request.header.pin[1] & 0x00000000000000FF) << 8) |
			       (((long)request.header.pin[2] & 0x00000000000000FF) << 16) |
			       (((long)request.header.pin[3] & 0x00000000000000FF) << 24));
			
			model = (short)((request.header.pin[4] & 0x000000FF) | 
							((request.header.pin[5] & 0x000000FF) << 8)); 
			
			/**
			 * Verify to check if the terminal with this pin and model is valid or not.
			 */
			_term = VerifyPin.exec(pin, model);
			
				//handle query menu request
			if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_MENU){
				response = new RespQueryMenu(request.header, QueryMenu.exec(_term));

				//handle query restaurant request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_RESTAURANT){
				response = new RespQueryRestaurant(request.header, QueryRestaurant.exec(_term));
				
				//handle query staff request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_STAFF){
				response = new RespQueryStaff(request.header, QueryStaffTerminal.exec(_term));
				
				//handle query region request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_REGION){
				response = new RespQueryRegion(request.header, QueryRegion.exec(_term));	
				
				//handle query the associated food
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_ASSOCIATION){
				Food foodToAssociated = ReqParser.parseQueryFoodAssociation(request);
				response = new RespQueryFoodAssociation(request.header, QueryFoodAssociationDao.exec(_term, foodToAssociated));
				
				//handle query sell out foods request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_SELL_OUT){
				response = new RespQuerySellOut(request.header, QueryMenu.queryPureFoods(" AND FOOD.restaurant_id=" + _term.restaurantID + 
																					     " AND FOOD.status & 0x04", null));
					
				//handle query table request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE){
				response = new RespQueryTable(request.header, QueryTable.exec(_term));
			
				//handle query order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER){
				int tableToQuery = ReqParser.parseQueryOrder(request);
				try{
					response = new RespQueryOrder(request.header, QueryOrderDao.execByTableDync(_term, tableToQuery));
				}catch(BusinessException e){
					if(e.errCode == ErrorCode.TABLE_IDLE || e.errCode == ErrorCode.TABLE_NOT_EXIST){
						response = new RespNAK(request.header, e.errCode);
					}else{
						throw e;
					}
				}

				//handle query order 2 request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_2){
				int tableToQuery = ReqParser.parseQueryOrder(request);
				try{
					Table table = QueryTable.exec(_term, tableToQuery);
					if(table.isBusy()){
						response = new RespACK(request.header);
						
					}else if(table.isIdle()){
						response = new RespNAK(request.header, ErrorCode.TABLE_IDLE);
						
					}else{
						response = new RespNAK(request.header, ErrorCode.UNKNOWN);
					}
				}catch(BusinessException e){
					if(e.errCode == ErrorCode.TABLE_NOT_EXIST){
						response = new RespNAK(request.header, e.errCode);
					}else{
						throw e;
					}
				}
				
				//handle query table status
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE_STATUS){
				
				try{
					Table table = QueryTable.exec(_term, ReqParser.parseQueryTblStatus(request));
					response = new RespACK(request.header, (byte)table.getStatus());
						
				}catch(BusinessException e){
					response = new RespNAK(request.header, e.errCode);
				}
				
				//handle insert order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER){

				Order orderToInsert = ReqInsertOrderParser.parse(request);	
				PrintHandler.PrintParam printParam = new PrintHandler.PrintParam();
				printParam.orderToPrint = InsertOrder.exec(_term, orderToInsert);
				printOrder(Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2, printParam);
				response = new RespACK(request.header);

				//handle update order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.UPDATE_ORDER){
				
				DiffResult result = UpdateOrder.exec(_term, ReqInsertOrderParser.parse(request));
				
				PrintHandler.PrintParam printParam = new PrintHandler.PrintParam();
				
				short printConf = Reserved.DEFAULT_CONF;				
				
				//perform to print if hurried foods exist
				if(!result.hurriedFoods.isEmpty()){
					printConf = Reserved.PRINT_SYNC | Reserved.PRINT_ALL_HURRIED_FOOD_2 | Reserved.PRINT_HURRIED_FOOD_2;
					printParam.orderToPrint = result.newOrder;
					printParam.orderToPrint.foods = result.hurriedFoods.toArray(new OrderFood[result.hurriedFoods.size()]);
					printOrder(printConf, printParam);					
				}
				
				//perform to print if extra foods exist
				if(!result.extraFoods.isEmpty()){
					printConf = Reserved.PRINT_SYNC | Reserved.PRINT_EXTRA_FOOD_2 | Reserved.PRINT_ALL_EXTRA_FOOD_2;
					printParam.orderToPrint = result.newOrder;
					printParam.orderToPrint.foods = result.extraFoods.toArray(new OrderFood[result.extraFoods.size()]);
					printOrder(printConf, printParam);
				}
					
				//perform to print if canceled foods exist
				if(!result.cancelledFoods.isEmpty()){
					printConf = Reserved.PRINT_SYNC | Reserved.PRINT_CANCELLED_FOOD_2 | Reserved.PRINT_ALL_CANCELLED_FOOD_2;
					printParam.orderToPrint = result.newOrder;
					printParam.orderToPrint.foods = result.cancelledFoods.toArray(new OrderFood[result.cancelledFoods.size()]);
					printOrder(printConf, printParam);
				}
				
				//print the table transfer
				if(!result.newOrder.srcTbl.equals(result.newOrder.destTbl)){
					printConf = Reserved.PRINT_SYNC | Reserved.PRINT_TRANSFER_TABLE_2;
					printParam.orderToPrint = result.newOrder;
					printOrder(printConf, printParam);
				}
				
				response = new RespACK(request.header);

				//handle the table transfer request 
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.TRANS_TABLE){
				Table[] tbl = ReqParser.parseTransTbl(request);
				TransTblDao.exec(_term, tbl[0], tbl[1]);
				response = new RespACK(request.header);
				
				PrintHandler.PrintParam printParam = new PrintHandler.PrintParam();
				printParam.orderToPrint.getSrcTbl().setAliasId(tbl[0].getAliasId());
				printParam.orderToPrint.getDestTbl().setAliasId(tbl[1].getAliasId());
				printOrder(Reserved.PRINT_TRANSFER_TABLE_2, printParam);
				
				//handle the cancel order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.CANCEL_ORDER){
				int tableToCancel = ReqParser.parseCancelOrder(request);
				CancelOrder.exec(_term, tableToCancel);
				response = new RespACK(request.header);

				//handle the pay order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.PAY_ORDER){
				Order orderToPay = ReqPayOrderParser.parse(request);
				/**
				 * If pay order temporary, just only print the temporary receipt.
				 * Otherwise perform the pay action and print receipt 
				 */
				final PrintHandler.PrintParam printParam = new PrintHandler.PrintParam();
				int printConf = orderToPay.printType;
				if((printConf & Reserved.PRINT_TEMP_RECEIPT_2) != 0){
					printParam.orderToPrint = PayOrder.calcByID(_term, orderToPay);
					printOrder(printConf, printParam);
					
				}else{
					
					printParam.orderToPrint = PayOrder.execByID(_term, orderToPay, false);
					/**
					 * Perform to consume the corresponding material in another thread,
					 * so as to prevent the action to pay order from taking too long.
					 * Since the action to consume material would become slower as the amount of 
					 * material detail records grow up. 
					 */
					WirelessSocketServer.threadPool.execute(new Runnable(){
						@Override
						public void run() {
							try{
								ConsumeMaterial.execByOrderID(_term, printParam.orderToPrint.getId());
							}catch(Exception e){
								e.printStackTrace();
							}
						}						
					});
					printOrder(printConf, printParam);
				}
				response = new RespACK(request.header);

				//handle the print request
			}else if(request.header.mode == Mode.PRINT && request.header.type == Type.PRINT_BILL_2){
				
				ReqPrintOrder2.ReqParam reqParam = ReqParser.parsePrintReq(request);
				
				int printConf = reqParam.printConf;

				PrintHandler.PrintParam printParam = new PrintHandler.PrintParam();
				/**
				 * In the case below,
				 * 1 - print shift, 
				 * 2 - temporary shift 
				 * 3 - daily settle
				 * 4 - history shift
				 * 5 - history daily settle
				 * just assign the on & off duty.
				 * Otherwise query to associated detail to this order.
				 */
				if((printConf & (Reserved.PRINT_SHIFT_RECEIPT_2 | Reserved.PRINT_TEMP_SHIFT_RECEIPT_2 |
								 Reserved.PRINT_DAILY_SETTLE_RECEIPT_2 | Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT_2 |
								 Reserved.PRINT_HISTORY_SHIFT_RECEIPT_2)) == 0){
					printParam.orderToPrint = QueryOrderDao.execByID(reqParam.orderID, QueryOrderDao.QUERY_TODAY);
				}else{				
					printParam.onDuty = reqParam.onDuty;
					printParam.offDuty = reqParam.offDuty;
				}
				/**
				 * If print table transfer, need to assign the original and new table id to order.
				 */
				if((printConf & Reserved.PRINT_TRANSFER_TABLE_2) != 0){
					printParam.orderToPrint.getDestTbl().setAliasId(reqParam.destTblID);
					printParam.orderToPrint.getSrcTbl().setAliasId(reqParam.srcTblID);
				}
				
				printOrder(printConf, printParam);
				response = new RespACK(request.header);
			
				//handle the query food group
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_GROUP){
				List<Pager> pagers = CalcFoodGroupDao.calc(_term);
				response = new RespQueryFoodGroup(request.header, pagers.toArray(new Pager[pagers.size()]));
				
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
	 *            the raw data to the request package, We use the "Reserved"
	 *            field since the print parameters stored in this field
	 * @param orderToPrint
	 *            the order to print
	 * @throws PrintLogicException
	 *             throws if any logic exception occurred while performing print
	 *             action
	 */
	private void printOrder(int printConf, PrintHandler.PrintParam param) throws PrintLogicException{
		if(param != null){
			//find the printer connection socket to the restaurant for this terminal
			List<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(_term.restaurantID));
			Socket[] connections = null;
			if(printerConn != null){
				connections = printerConn.toArray(new Socket[printerConn.size()]);			
			}else{
				connections = new Socket[0];
			}
			
			/**
			 * Get the corresponding restaurant information
			 */
			DBCon dbCon = new DBCon();
			try{
				dbCon.connect();
				param.restaurant = QueryRestaurant.exec(dbCon, _term.restaurantID);
				param.depts = QueryMenu.queryDepartments(dbCon, "AND DEPT.restaurant_id=" + _term.restaurantID, null);
			}catch(Exception e){
				param.restaurant = new Restaurant();
				param.depts = new Department[0];
			}finally{
				dbCon.disconnect();
			}
			
			param.term = _term;
			
			//check whether the print request is synchronized or asynchronous
			if((printConf & Reserved.PRINT_SYNC) != 0){
				/**
				 * if the print request is synchronized, then the insert order request must wait until
				 * the print request is done, and send the ACK or NAK to let the terminal know whether 
				 * the print actions is successfully or not
				 */	
				new PrintHandler(connections, printConf, param).run();						
				
			}else{
				/**
				 * if the print request is asynchronous, then the insert order request return an ACK immediately,
				 * regardless of the print request. In the mean time, the print request would be put to a 
				 * new thread to run.
				 */	
				WirelessSocketServer.threadPool.execute(new PrintHandler(connections, printConf, param));
			}
		}
	}
}



