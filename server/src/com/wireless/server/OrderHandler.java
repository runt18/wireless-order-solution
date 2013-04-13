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

import com.wireless.db.foodAssociation.QueryFoodAssociationDao;
import com.wireless.db.foodGroup.CalcFoodGroupDao;
import com.wireless.db.frontBusiness.CancelOrder;
import com.wireless.db.frontBusiness.ConsumeMaterial;
import com.wireless.db.frontBusiness.InsertOrder;
import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.frontBusiness.QueryRegion;
import com.wireless.db.frontBusiness.QueryStaffTerminal;
import com.wireless.db.frontBusiness.QueryTable;
import com.wireless.db.frontBusiness.TransTblDao;
import com.wireless.db.frontBusiness.UpdateOrder;
import com.wireless.db.frontBusiness.UpdateOrder.DiffResult;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ErrorCode;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.pack.resp.RespACK;
import com.wireless.pack.resp.RespNAK;
import com.wireless.pack.resp.RespOTAUpdate;
import com.wireless.pack.resp.RespPackage;
import com.wireless.print.PType;
import com.wireless.print.type.TypeContentFactory;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.PRegion;
import com.wireless.protocol.Pager;
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
	
    private Terminal mTerm = null;
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
			mTerm = VerifyPin.exec(pin, model);
			
			RespPackage response = null;
			
				//handle query menu request
			if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_MENU){
				//response = new RespQueryMenu(request.header, QueryMenu.exec(_term));
				response = new RespPackage(request.header, QueryMenu.exec(mTerm), 0);

				//handle query restaurant request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_RESTAURANT){
				response = new RespPackage(request.header, RestaurantDao.queryByID(mTerm).toProtocol(), 0);
				
				//handle query staff request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_STAFF){
				//response = new RespQueryStaff(request.header, QueryStaffTerminal.exec(_term));
				response = new RespPackage(request.header, QueryStaffTerminal.exec(mTerm), StaffTerminal.ST_PARCELABLE_COMPLEX);
				
				//handle query region request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_REGION){
				//response = new RespQueryRegion(request.header, QueryRegion.exec(_term));
				response = new RespPackage(request.header, QueryRegion.exec(mTerm), PRegion.REGION_PARCELABLE_COMPLEX);
				
				//handle query the associated food
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_ASSOCIATION){
				Food foodToAssociated = new Food(); 
				foodToAssociated.createFromParcel(new Parcel(request.body));
				//response = new RespQueryFoodAssociation(request.header, QueryFoodAssociationDao.exec(_term, foodToAssociated));
				response = new RespPackage(request.header, QueryFoodAssociationDao.exec(mTerm, foodToAssociated), Food.FOOD_PARCELABLE_SIMPLE);
				
				//handle query sell out foods request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_SELL_OUT){
				response = new RespPackage(request.header, 
										   QueryMenu.queryPureFoods(" AND FOOD.restaurant_id=" + mTerm.restaurantID + 
																	" AND FOOD.status & " + Food.SELL_OUT + " <> 0 ", null), 
										   Food.FOOD_PARCELABLE_SIMPLE);
					
				//handle query table request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE){
				//response = new RespQueryTable(request.header, QueryTable.exec(_term));
				response = new RespPackage(request.header, QueryTable.exec(mTerm), Table.TABLE_PARCELABLE_COMPLEX);
			
				//handle query order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_BY_TBL){
				//int tableToQuery = ReqParser.parseQueryOrder(request);
				Table tableToQuery = new Table();
				tableToQuery.createFromParcel(new Parcel(request.body));
				try{
					//response = new RespQueryOrder(request.header, QueryOrderDao.execByTableDync(_term, tableToQuery));
					response = new RespPackage(request.header, QueryOrderDao.execByTableDync(mTerm, tableToQuery.getAliasId()), Order.ORDER_PARCELABLE_4_QUERY);
				}catch(BusinessException e){
					if(e.getErrCode() == ProtocolError.ORDER_NOT_EXIST || e.getErrCode() == ProtocolError.TABLE_IDLE || e.getErrCode() == ProtocolError.TABLE_NOT_EXIST){
						response = new RespNAK(request.header, e.getCode());
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
					Table table = QueryTable.exec(mTerm, tblToQuery.getAliasId());
					if(table.isBusy()){
						response = new RespACK(request.header);
						
					}else if(table.isIdle()){
						response = new RespNAK(request.header, ErrorCode.TABLE_IDLE);
						
					}else{
						response = new RespNAK(request.header, ErrorCode.UNKNOWN);
					}
				}catch(BusinessException e){
					if(e.getErrCode() == ProtocolError.TABLE_NOT_EXIST){
						response = new RespNAK(request.header, e.getCode());
					}else{
						throw e;
					}
				}
				
				//handle query table status
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE_STATUS){
				
				try{
					Table tblToQuery = new Table();
					tblToQuery.createFromParcel(new Parcel(request.body));
					tblToQuery = QueryTable.exec(mTerm, tblToQuery.getAliasId());
					response = new RespACK(request.header, (byte)tblToQuery.getStatus());
						
				}catch(BusinessException e){
					response = new RespNAK(request.header, e.getCode());
				}
				
				//handle insert order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER){

				//Order orderToInsert = ReqInsertOrderParser.parse(request);	
				Order insertedOrder = new Order();
				insertedOrder.createFromParcel(new Parcel(request.body));
				
				insertedOrder = InsertOrder.exec(mTerm, insertedOrder);
				
				if(request.header.reserved == ReqInsertOrder.DO_PRINT){
					new PrintHandler(mTerm)
						.addTypeContent(TypeContentFactory.instance().createSummaryContent(PType.PRINT_ORDER, 
						 																   mTerm, 
											 											   insertedOrder))
						.addTypeContent(TypeContentFactory.instance().createDetailContent(PType.PRINT_ORDER_DETAIL, 
																						  mTerm, 
																						  insertedOrder))
						.fireAsync();
				}
				
				response = new RespACK(request.header);

				//handle update order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.UPDATE_ORDER){
				
				Order orderToUpdate = new Order();
				orderToUpdate.createFromParcel(new Parcel(request.body));
				DiffResult diffResult = UpdateOrder.execByID(mTerm, orderToUpdate);
				
				if(request.header.reserved == ReqInsertOrder.DO_PRINT){
					
					PrintHandler printHandler = new PrintHandler(mTerm);
					
					if(!diffResult.hurriedFoods.isEmpty()){
						diffResult.newOrder.setOrderFoods(diffResult.hurriedFoods.toArray(new OrderFood[diffResult.hurriedFoods.size()]));
						//print the summary to hurried foods
						printHandler.addTypeContent(TypeContentFactory.instance().createSummaryContent(PType.PRINT_ALL_HURRIED_FOOD, 
																									   mTerm, 
																									   diffResult.newOrder));
						//print the detail to hurried foods
						printHandler.addTypeContent(TypeContentFactory.instance().createDetailContent(PType.PRINT_HURRIED_FOOD, 
																									  mTerm,
																									  diffResult.newOrder));
					}
					
					if(!diffResult.extraFoods.isEmpty()){
						diffResult.newOrder.setOrderFoods(diffResult.extraFoods.toArray(new OrderFood[diffResult.extraFoods.size()]));
						//print the summary to extra foods
						printHandler.addTypeContent(TypeContentFactory.instance().createSummaryContent(PType.PRINT_ALL_EXTRA_FOOD, 
																									   mTerm,
																									   diffResult.newOrder));
						//print the detail to extra foods
						printHandler.addTypeContent(TypeContentFactory.instance().createDetailContent(PType.PRINT_EXTRA_FOOD, 
																									  mTerm,
																									  diffResult.newOrder));
					}
	
					if(!diffResult.cancelledFoods.isEmpty()){
						diffResult.newOrder.setOrderFoods(diffResult.cancelledFoods.toArray(new OrderFood[diffResult.cancelledFoods.size()]));
						//print the summary to canceled foods
						printHandler.addTypeContent(TypeContentFactory.instance().createSummaryContent(PType.PRINT_ALL_CANCELLED_FOOD,
																									   mTerm,
																									   diffResult.newOrder));
						//print the detail to canceled foods
						printHandler.addTypeContent(TypeContentFactory.instance().createDetailContent(PType.PRINT_CANCELLED_FOOD,
																									  mTerm,
																									  diffResult.newOrder));
	
					}
	
					//print the transfer
					printHandler.addTypeContent(TypeContentFactory.instance().createTransContent(PType.PRINT_TRANSFER_TABLE, 
																								 mTerm,
																								 diffResult.newOrder.getId(), 
																								 diffResult.oriOrder.getDestTbl(),
																								 diffResult.newOrder.getDestTbl()));
	
					//Fire to execute print action.
					printHandler.fireAsync();
				}
				response = new RespACK(request.header);

				//handle the table transfer request 
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.TRANS_TABLE){
				Parcelable[] parcelables = new Parcel(request.body).readParcelArray(Table.TABLE_CREATOR);
				if(parcelables != null){
					Table srcTbl = (Table)parcelables[0];
					Table destTbl = (Table)parcelables[1];
					
					int orderId = TransTblDao.exec(mTerm, srcTbl, destTbl);
					response = new RespACK(request.header);
					
					new PrintHandler(mTerm)
						.addTypeContent(TypeContentFactory.instance().createTransContent(PType.PRINT_TRANSFER_TABLE, mTerm, orderId, srcTbl, destTbl))
						.fireAsync();
					
				}
				
				//handle the cancel order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.CANCEL_ORDER){
				Table tblToCancel = new Table();
				tblToCancel.createFromParcel(new Parcel(request.body));
				CancelOrder.exec(mTerm, tblToCancel.getAliasId());
				response = new RespACK(request.header);

				//handle the pay order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.PAY_ORDER){
				Order orderToPay = new Order();
				orderToPay.createFromParcel(new Parcel(request.body));
				/**
				 * If pay order temporary, just only print the temporary receipt.
				 * Otherwise perform the pay action and print receipt 
				 */
				if(request.header.reserved == ReqPayOrder.PAY_CATE_TEMP){
					
					new PrintHandler(mTerm)
						.addTypeContent(TypeContentFactory.instance().createReceiptContent(PType.PRINT_TEMP_RECEIPT, 
																						   mTerm, 
																						   PayOrder.calcByID(mTerm, orderToPay)))
						.fireAsync();
					
				}else{
					
					final Order order = PayOrder.execByID(mTerm, orderToPay);
					
					PrintHandler printHandler = new PrintHandler(mTerm);
					
					printHandler.addTypeContent(TypeContentFactory.instance().createReceiptContent(PType.PRINT_RECEIPT, mTerm, order));
					
					//Perform to print the member receipt if settled by member.
					if(order.isSettledByMember()){
						printHandler.addTypeContent(TypeContentFactory.instance().createMemberReceiptContent(PType.PRINT_MEMBER_RECEIPT, 
																											 mTerm, 
																											 order.getMemberOperationId()));
					}
					
					printHandler.fireAsync();
					
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
								ConsumeMaterial.execByOrderID(mTerm, order.getId());
							}catch(Exception e){
								e.printStackTrace();
							}
						}						
					});
				}
				
				response = new RespACK(request.header);

				//handle the print request
			}else if(request.header.mode == Mode.PRINT && request.header.type == Type.PRINT_CONTENT){
				
				PType printType = PType.valueOf(request.header.reserved);
				if(printType.isSummary()){
					int orderId = new Parcel(request.body).readInt();
					new PrintHandler(mTerm)
						.addTypeContent(TypeContentFactory.instance().createSummaryContent(printType, mTerm, orderId))
						.fireAsync();
					
				}else if(printType.isDetail()){
					int orderId = new Parcel(request.body).readInt();
					new PrintHandler(mTerm)
						.addTypeContent(TypeContentFactory.instance().createDetailContent(printType, mTerm, orderId))
						.fireAsync();
					
				}else if(printType.isReceipt()){
					int orderId = new Parcel(request.body).readInt();
					new PrintHandler(mTerm)
						.addTypeContent(TypeContentFactory.instance().createReceiptContent(printType, mTerm, orderId))
						.fireAsync();
					
				}else if(printType.isTransTbl()){
					Parcel p = new Parcel(request.body);
					int orderId = p.readInt();
					Table srcTbl = (Table)p.readParcel(Table.TABLE_CREATOR);
					Table destTbl = (Table)p.readParcel(Table.TABLE_CREATOR);
					new PrintHandler(mTerm)
						.addTypeContent(TypeContentFactory.instance().createTransContent(printType, mTerm, orderId, srcTbl, destTbl))
						.fireSync();
					
				}else if(printType.isShift()){
					Parcel p = new Parcel(request.body);
					long onDuty = p.readLong();
					long offDuty = p.readLong();
					new PrintHandler(mTerm)
						.addTypeContent(TypeContentFactory.instance().createShiftContent(printType, mTerm, onDuty, offDuty))
						.fireAsync();
					
				}else if(printType.isMember()){
					int memberOperationId = new Parcel(request.body).readInt();
					new PrintHandler(mTerm)
						.addTypeContent(TypeContentFactory.instance().createMemberReceiptContent(printType, mTerm, memberOperationId))
						.fireAsync();
				}
				
				response = new RespACK(request.header);
			
				//handle the query food group
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_GROUP){
				List<Pager> pagers = CalcFoodGroupDao.calc(mTerm);
				response = new RespPackage(request.header, pagers.toArray(new Pager[pagers.size()]), 0);
				
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
				new RespNAK(request.header, e.getCode()).writeToStream(out);
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
			
		}catch(Exception e){
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
//	private void printOrder(int printConf, PrintHandler.PrintParam param) throws PrintLogicException{
//		if(param != null){
//			//find the printer connection socket to the restaurant for this terminal
//			List<Socket> printerConn = WirelessSocketServer.printerConnections.get(new Integer(mTerm.restaurantID));
//			Socket[] connections = null;
//			if(printerConn != null){
//				connections = printerConn.toArray(new Socket[printerConn.size()]);			
//			}else{
//				connections = new Socket[0];
//			}
//			
//			/**
//			 * Get the corresponding restaurant information
//			 */
//			DBCon dbCon = new DBCon();
//			try{
//				dbCon.connect();
//				param.restaurant = QueryRestaurant.exec(dbCon, mTerm.restaurantID);
//				param.depts = QueryMenu.queryDepartments(dbCon, "AND DEPT.restaurant_id=" + mTerm.restaurantID, null);
//			}catch(Exception e){
//				param.restaurant = new Restaurant();
//				param.depts = new Department[0];
//			}finally{
//				dbCon.disconnect();
//			}
//			
//			param.term = mTerm;
//			
//			//check whether the print request is synchronized or asynchronous
//			if((printConf & Reserved.PRINT_SYNC) != 0){
//				/**
//				 * if the print request is synchronized, then the insert order request must wait until
//				 * the print request is done, and send the ACK or NAK to let the terminal know whether 
//				 * the print actions is successfully or not
//				 */	
//				new PrintHandler(connections, printConf, param).run();						
//				
//			}else{
//				/**
//				 * if the print request is asynchronous, then the insert order request return an ACK immediately,
//				 * regardless of the print request. In the mean time, the print request would be put to a 
//				 * new thread to run.
//				 */	
//				WirelessSocketServer.threadPool.execute(new PrintHandler(connections, printConf, param));
//			}
//		}
//	}
}



