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
import com.wireless.pack.ErrorCode;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Reserved;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.pack.resp.RespACK;
import com.wireless.pack.resp.RespNAK;
import com.wireless.pack.resp.RespPackage;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderDiff.DiffResult;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Pager;
import com.wireless.protocol.Region;
import com.wireless.protocol.ReqParser;
import com.wireless.protocol.ReqPrintOrder2;
import com.wireless.protocol.RespOTAUpdate;
import com.wireless.protocol.RespQueryFoodGroup;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
/**
 * @author yzhang
 *
 */
class OrderHandler implements Runnable{
	
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
		
		ProtocolPackage request = new ProtocolPackage();
		InputStream in = null;
		OutputStream out = null;
		try{
			in = new BufferedInputStream(new DataInputStream(_conn.getInputStream()));
			out = new BufferedOutputStream(new DataOutputStream(_conn.getOutputStream()));
			
			// Get the request from socket stream.
			request.readFromStream(in, _timeout);

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
			
			RespPackage response = null;
			
				//handle query menu request
			if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_MENU){
				//response = new RespQueryMenu(request.header, QueryMenu.exec(_term));
				response = new RespPackage(request.header, QueryMenu.exec(_term), 0);

				//handle query restaurant request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_RESTAURANT){
				//response = new RespQueryRestaurant(request.header, QueryRestaurant.exec(_term));
				response = new RespPackage(request.header, QueryRestaurant.exec(_term), 0);
				
				//handle query staff request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_STAFF){
				//response = new RespQueryStaff(request.header, QueryStaffTerminal.exec(_term));
				response = new RespPackage(request.header, QueryStaffTerminal.exec(_term), StaffTerminal.ST_PARCELABLE_COMPLEX);
				
				//handle query region request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_REGION){
				//response = new RespQueryRegion(request.header, QueryRegion.exec(_term));
				response = new RespPackage(request.header, QueryRegion.exec(_term), Region.REGION_PARCELABLE_COMPLEX);
				
				//handle query the associated food
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_ASSOCIATION){
				//Food foodToAssociated = ReqParser.parseQueryFoodAssociation(request);
				Food foodToAssociated = new Food(); 
				foodToAssociated.createFromParcel(new Parcel(request.body));
				//response = new RespQueryFoodAssociation(request.header, QueryFoodAssociationDao.exec(_term, foodToAssociated));
				response = new RespPackage(request.header, QueryFoodAssociationDao.exec(_term, foodToAssociated), Food.FOOD_PARCELABLE_SIMPLE);
				
				//handle query sell out foods request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_SELL_OUT){
//				response = new RespQuerySellOut(request.header, QueryMenu.queryPureFoods(" AND FOOD.restaurant_id=" + _term.restaurantID + 
//																					     " AND FOOD.status & 0x04", null));
				response = new RespPackage(request.header, 
										   QueryMenu.queryPureFoods(" AND FOOD.restaurant_id=" + _term.restaurantID + 
																	" AND FOOD.status & " + Food.SELL_OUT + " <> 0 ", null), 
										   Food.FOOD_PARCELABLE_SIMPLE);
					
				//handle query table request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE){
				//response = new RespQueryTable(request.header, QueryTable.exec(_term));
				response = new RespPackage(request.header, QueryTable.exec(_term), Table.TABLE_PARCELABLE_COMPLEX);
			
				//handle query order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_BY_TBL){
				//int tableToQuery = ReqParser.parseQueryOrder(request);
				Table tableToQuery = new Table();
				tableToQuery.createFromParcel(new Parcel(request.body));
				try{
					//response = new RespQueryOrder(request.header, QueryOrderDao.execByTableDync(_term, tableToQuery));
					response = new RespPackage(request.header, QueryOrderDao.execByTableDync(_term, tableToQuery.getAliasId()), Order.ORDER_PARCELABLE_4_QUERY);
				}catch(BusinessException e){
					if(e.errCode == ErrorCode.ORDER_NOT_EXIST || e.errCode == ErrorCode.TABLE_IDLE || e.errCode == ErrorCode.TABLE_NOT_EXIST){
						response = new RespNAK(request.header, e.errCode);
					}else{
						throw e;
					}
				}

				//handle query order 2 request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_2){
				//int tableToQuery = ReqParser.parseQueryOrder(request);
				Table tblToQuery = new Table();
				tblToQuery.createFromParcel(new Parcel(request.body));
				try{
					Table table = QueryTable.exec(_term, tblToQuery.getAliasId());
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
					Table tblToQuery = new Table();
					tblToQuery.createFromParcel(new Parcel(request.body));
					tblToQuery = QueryTable.exec(_term, tblToQuery.getAliasId());
					response = new RespACK(request.header, (byte)tblToQuery.getStatus());
						
				}catch(BusinessException e){
					response = new RespNAK(request.header, e.errCode);
				}
				
				//handle insert order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER){

				//Order orderToInsert = ReqInsertOrderParser.parse(request);	
				Order orderToInsert = new Order();
				orderToInsert.createFromParcel(new Parcel(request.body));
				
				PrintHandler.PrintParam printParam = new PrintHandler.PrintParam();
				printParam.orderToPrint = InsertOrder.exec(_term, orderToInsert);
				printOrder(Reserved.PRINT_ORDER_2 | Reserved.PRINT_ORDER_DETAIL_2, printParam);
				response = new RespACK(request.header);

				//handle update order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.UPDATE_ORDER){
				
				Order orderToUpdate = new Order();
				orderToUpdate.createFromParcel(new Parcel(request.body));
				DiffResult result = UpdateOrder.exec(_term, orderToUpdate);
				
				PrintHandler.PrintParam printParam = new PrintHandler.PrintParam();
				
				short printConf = Reserved.DEFAULT_CONF;				
				
				//perform to print if hurried foods exist
				if(!result.hurriedFoods.isEmpty()){
					printConf = Reserved.PRINT_SYNC | Reserved.PRINT_ALL_HURRIED_FOOD_2 | Reserved.PRINT_HURRIED_FOOD_2;
					printParam.orderToPrint = result.newOrder;
					printParam.orderToPrint.setOrderFoods(result.hurriedFoods.toArray(new OrderFood[result.hurriedFoods.size()]));
					printOrder(printConf, printParam);					
				}
				
				//perform to print if extra foods exist
				if(!result.extraFoods.isEmpty()){
					printConf = Reserved.PRINT_SYNC | Reserved.PRINT_EXTRA_FOOD_2 | Reserved.PRINT_ALL_EXTRA_FOOD_2;
					printParam.orderToPrint = result.newOrder;
					printParam.orderToPrint.setOrderFoods(result.extraFoods.toArray(new OrderFood[result.extraFoods.size()]));
					printOrder(printConf, printParam);
				}
					
				//perform to print if canceled foods exist
				if(!result.cancelledFoods.isEmpty()){
					printConf = Reserved.PRINT_SYNC | Reserved.PRINT_CANCELLED_FOOD_2 | Reserved.PRINT_ALL_CANCELLED_FOOD_2;
					printParam.orderToPrint = result.newOrder;
					printParam.orderToPrint.setOrderFoods(result.cancelledFoods.toArray(new OrderFood[result.cancelledFoods.size()]));
					printOrder(printConf, printParam);
				}
				
				//print the table transfer
				if(!result.newOrder.getSrcTbl().equals(result.newOrder.getDestTbl())){
					printConf = Reserved.PRINT_SYNC | Reserved.PRINT_TRANSFER_TABLE_2;
					printParam.orderToPrint = result.newOrder;
					printOrder(printConf, printParam);
				}
				
				response = new RespACK(request.header);

				//handle the table transfer request 
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.TRANS_TABLE){
				Parcelable[] parcelables = new Parcel(request.body).readParcelArray(Table.TABLE_CREATOR);
				if(parcelables != null){
					Table[] tblPairToTrans = new Table[parcelables.length];
					for(int i = 0; i < tblPairToTrans.length; i++){
						tblPairToTrans[i] = (Table)parcelables[i];
					}
					
					TransTblDao.exec(_term, tblPairToTrans[0], tblPairToTrans[1]);
					response = new RespACK(request.header);
					
					PrintHandler.PrintParam printParam = new PrintHandler.PrintParam();
					printParam.orderToPrint.getSrcTbl().setAliasId(tblPairToTrans[0].getAliasId());
					printParam.orderToPrint.getDestTbl().setAliasId(tblPairToTrans[1].getAliasId());
					printOrder(Reserved.PRINT_TRANSFER_TABLE_2, printParam);
					
				}
				
				//handle the cancel order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.CANCEL_ORDER){
				int tableToCancel = ReqParser.parseCancelOrder(request);
				CancelOrder.exec(_term, tableToCancel);
				response = new RespACK(request.header);

				//handle the pay order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.PAY_ORDER){
				//Order orderToPay = ReqPayOrderParser.parse(request);
				Order orderToPay = new Order();
				orderToPay.createFromParcel(new Parcel(request.body));
				/**
				 * If pay order temporary, just only print the temporary receipt.
				 * Otherwise perform the pay action and print receipt 
				 */
				final PrintHandler.PrintParam printParam = new PrintHandler.PrintParam();
				//int printConf = orderToPay.printType;
				//if((printConf & Reserved.PRINT_TEMP_RECEIPT_2) != 0){
				if(request.header.reserved == ReqPayOrder.PAY_CATE_TEMP){
					printParam.orderToPrint = PayOrder.calcByID(_term, orderToPay);
					printOrder(Reserved.PRINT_TEMP_RECEIPT_2, printParam);
					
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
					printOrder(Reserved.PRINT_RECEIPT_2, printParam);
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
			response.writeToStream(out);
			
			
		}catch(BusinessException e){
			try{
				new RespNAK(request.header, e.errCode).writeToStream(out);
			}catch(IOException ex){}
			e.printStackTrace();
			
		}catch(IOException e){
			try{
				new RespNAK(request.header).writeToStream(out);
			}catch(IOException ex){}
			e.printStackTrace();
			
		}catch(SQLException e){
			try{
				new RespNAK(request.header).writeToStream(out);
			}catch(IOException ex){}
			e.printStackTrace();			
			
		}catch(PrintLogicException e){
			try{
				new RespNAK(request.header, ErrorCode.PRINT_FAIL).writeToStream(out);
			}catch(IOException ex){}
			e.printStackTrace();
		}
		catch(Exception e){
			try{
				new RespNAK(request.header).writeToStream(out);
			}catch(IOException ex){}
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



