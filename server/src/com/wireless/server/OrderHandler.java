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
import com.wireless.db.frontBusiness.InsertOrder;
import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.frontBusiness.QueryStaffTerminal;
import com.wireless.db.frontBusiness.TransTblDao;
import com.wireless.db.frontBusiness.UpdateOrder;
import com.wireless.db.frontBusiness.UpdateOrder.DiffResult;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.pack.resp.RespACK;
import com.wireless.pack.resp.RespNAK;
import com.wireless.pack.resp.RespOTAUpdate;
import com.wireless.pack.resp.RespPackage;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.foodGroup.Pager;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.print.PType;
import com.wireless.print.type.TypeContentFactory;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Terminal;
/**
 * @author yzhang
 *
 */
class OrderHandler implements Runnable{
	
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
			final Terminal term = VerifyPin.exec(pin, model);
			
			RespPackage response = null;
			
				//handle query menu request
			if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_MENU){
				//response = new RespQueryMenu(request.header, QueryMenu.exec(_term));
				response = new RespPackage(request.header, QueryMenu.exec(term), 0);

				//handle query restaurant request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_RESTAURANT){
				response = new RespPackage(request.header, RestaurantDao.queryById(term), 0);
				
				//handle query staff request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_STAFF){
				//response = new RespQueryStaff(request.header, QueryStaffTerminal.exec(_term));
				response = new RespPackage(request.header, QueryStaffTerminal.exec(term), StaffTerminal.ST_PARCELABLE_COMPLEX);
				
				//handle query region request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_REGION){
				//response = new RespQueryRegion(request.header, QueryRegion.exec(_term));
				response = new RespPackage(request.header, RegionDao.getRegions(term, null, null), Region.REGION_PARCELABLE_COMPLEX);
				
				//handle query the associated food
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_ASSOCIATION){
				Food foodToAssociated = new Parcel(request.body).readParcel(Food.CREATOR);
				response = new RespPackage(request.header, QueryFoodAssociationDao.exec(term, foodToAssociated), Food.FOOD_PARCELABLE_SIMPLE);
				
				//handle query sell out foods request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_SELL_OUT){
				response = new RespPackage(request.header, 
										   FoodDao.getPureFoods(term, " AND FOOD.status & " + Food.SELL_OUT + " <> 0 ", null), 
										   Food.FOOD_PARCELABLE_SIMPLE);
					
				//handle query table request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE){
				response = new RespPackage(request.header, TableDao.getTables(term, null, null), Table.TABLE_PARCELABLE_COMPLEX);
			
				//handle query order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_BY_TBL){
				Table tableToQuery = new Parcel(request.body).readParcel(Table.CREATOR);
				try{
					//response = new RespQueryOrder(request.header, QueryOrderDao.execByTableDync(_term, tableToQuery));
					response = new RespPackage(request.header, OrderDao.getByTableAliasDync(term, tableToQuery.getAliasId()), Order.ORDER_PARCELABLE_4_QUERY);
				}catch(BusinessException e){
					if(e.getErrCode() == ProtocolError.ORDER_NOT_EXIST || e.getErrCode() == ProtocolError.TABLE_IDLE || e.getErrCode() == ProtocolError.TABLE_NOT_EXIST){
						response = new RespNAK(request.header, e.getErrCode());
					}else{
						throw e;
					}
				}

				//handle query table status
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE_STATUS){
				
				try{
					Table tblToQuery = new Parcel(request.body).readParcel(Table.CREATOR);
					tblToQuery = TableDao.getTableByAlias(term, tblToQuery.getAliasId());
					response = new RespACK(request.header, (byte)tblToQuery.getStatus().getVal());
						
				}catch(BusinessException e){
					response = new RespNAK(request.header, e.getErrCode());
				}
				
				//handle insert order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER){

				Order insertedOrder = new Parcel(request.body).readParcel(Order.CREATOR);
				
				insertedOrder = InsertOrder.exec(term, insertedOrder);
				
				if(request.header.reserved == ReqInsertOrder.DO_PRINT){
					new PrintHandler(term)
						.addTypeContent(TypeContentFactory.instance().createSummaryContent(PType.PRINT_ORDER, 
						 																   term, 
											 											   insertedOrder))
						.addTypeContent(TypeContentFactory.instance().createDetailContent(PType.PRINT_ORDER_DETAIL, 
																						  term, 
																						  insertedOrder))
						.fireAsync();
				}
				
				response = new RespACK(request.header);

				//handle update order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.UPDATE_ORDER){
				
				Order orderToUpdate = new Parcel(request.body).readParcel(Order.CREATOR);
				DiffResult diffResult = UpdateOrder.execByID(term, orderToUpdate);
				
				if(request.header.reserved == ReqInsertOrder.DO_PRINT){
					
					PrintHandler printHandler = new PrintHandler(term);
					
					if(!diffResult.hurriedFoods.isEmpty()){
						diffResult.newOrder.setOrderFoods(diffResult.hurriedFoods);
						//print the summary to hurried foods
						printHandler.addTypeContent(TypeContentFactory.instance().createSummaryContent(PType.PRINT_ALL_HURRIED_FOOD, 
																									   term, 
																									   diffResult.newOrder));
						//print the detail to hurried foods
						printHandler.addTypeContent(TypeContentFactory.instance().createDetailContent(PType.PRINT_HURRIED_FOOD, 
																									  term,
																									  diffResult.newOrder));
					}
					
					if(!diffResult.extraFoods.isEmpty()){
						diffResult.newOrder.setOrderFoods(diffResult.extraFoods);
						//print the summary to extra foods
						printHandler.addTypeContent(TypeContentFactory.instance().createSummaryContent(PType.PRINT_ALL_EXTRA_FOOD, 
																									   term,
																									   diffResult.newOrder));
						//print the detail to extra foods
						printHandler.addTypeContent(TypeContentFactory.instance().createDetailContent(PType.PRINT_EXTRA_FOOD, 
																									  term,
																									  diffResult.newOrder));
					}
	
					if(!diffResult.cancelledFoods.isEmpty()){
						diffResult.newOrder.setOrderFoods(diffResult.cancelledFoods);
						//print the summary to canceled foods
						printHandler.addTypeContent(TypeContentFactory.instance().createSummaryContent(PType.PRINT_ALL_CANCELLED_FOOD,
																									   term,
																									   diffResult.newOrder));
						//print the detail to canceled foods
						printHandler.addTypeContent(TypeContentFactory.instance().createDetailContent(PType.PRINT_CANCELLED_FOOD,
																									  term,
																									  diffResult.newOrder));
	
					}
	
					//print the transfer
					printHandler.addTypeContent(TypeContentFactory.instance().createTransContent(PType.PRINT_TRANSFER_TABLE, 
																								 term,
																								 diffResult.newOrder.getId(), 
																								 diffResult.oriOrder.getDestTbl(),
																								 diffResult.newOrder.getDestTbl()));
	
					//Fire to execute print action.
					printHandler.fireAsync();
				}
				response = new RespACK(request.header);

				//handle the table transfer request 
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.TRANS_TABLE){
				Table[] tables = new Parcel(request.body).readParcelArray(Table.CREATOR);
				if(tables != null){
					Table srcTbl = tables[0];
					Table destTbl = tables[1];
					
					int orderId = TransTblDao.exec(term, srcTbl, destTbl);
					response = new RespACK(request.header);
					
					new PrintHandler(term)
						.addTypeContent(TypeContentFactory.instance().createTransContent(PType.PRINT_TRANSFER_TABLE, term, orderId, srcTbl, destTbl))
						.fireAsync();
					
				}
				
				//handle the cancel order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.CANCEL_ORDER){
				Table tblToCancel = new Parcel(request.body).readParcel(Table.CREATOR);
				CancelOrder.exec(term, tblToCancel.getAliasId());
				response = new RespACK(request.header);

				//handle the pay order request
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.PAY_ORDER){
				Order orderToPay = new Parcel(request.body).readParcel(Order.CREATOR);
				/**
				 * If pay order temporary, just only print the temporary receipt.
				 * Otherwise perform the pay action and print receipt 
				 */
				if(request.header.reserved == ReqPayOrder.PAY_CATE_TEMP){
					
					new PrintHandler(term)
						.addTypeContent(TypeContentFactory.instance().createReceiptContent(PType.PRINT_TEMP_RECEIPT, 
																						   term, 
																						   PayOrder.calcByID(term, orderToPay)))
						.fireAsync();
					
				}else{
					
					final Order order = PayOrder.execByID(term, orderToPay);
					
					PrintHandler printHandler = new PrintHandler(term);
					
					printHandler.addTypeContent(TypeContentFactory.instance().createReceiptContent(PType.PRINT_RECEIPT, term, order));
					
					//Perform to print the member receipt if settled by member.
					if(order.isSettledByMember()){
						printHandler.addTypeContent(TypeContentFactory.instance().createMemberReceiptContent(PType.PRINT_MEMBER_RECEIPT, 
																											 term, 
																											 order.getMemberOperationId()));
					}
					
					printHandler.fireAsync();
					
				}
				
				response = new RespACK(request.header);

				//handle the print request
			}else if(request.header.mode == Mode.PRINT && request.header.type == Type.PRINT_CONTENT){
				
				PType printType = PType.valueOf(request.header.reserved);
				if(printType.isSummary()){
					int orderId = new Parcel(request.body).readInt();
					new PrintHandler(term)
						.addTypeContent(TypeContentFactory.instance().createSummaryContent(printType, term, orderId))
						.fireAsync();
					
				}else if(printType.isDetail()){
					int orderId = new Parcel(request.body).readInt();
					new PrintHandler(term)
						.addTypeContent(TypeContentFactory.instance().createDetailContent(printType, term, orderId))
						.fireAsync();
					
				}else if(printType.isReceipt()){
					int orderId = new Parcel(request.body).readInt();
					new PrintHandler(term)
						.addTypeContent(TypeContentFactory.instance().createReceiptContent(printType, term, orderId))
						.fireAsync();
					
				}else if(printType.isTransTbl()){
					Parcel p = new Parcel(request.body);
					int orderId = p.readInt();
					Table srcTbl = p.readParcel(Table.CREATOR);
					Table destTbl = p.readParcel(Table.CREATOR);
					new PrintHandler(term)
						.addTypeContent(TypeContentFactory.instance().createTransContent(printType, term, orderId, srcTbl, destTbl))
						.fireSync();
					
				}else if(printType.isShift()){
					Parcel p = new Parcel(request.body);
					long onDuty = p.readLong();
					long offDuty = p.readLong();
					new PrintHandler(term)
						.addTypeContent(TypeContentFactory.instance().createShiftContent(printType, term, onDuty, offDuty))
						.fireAsync();
					
				}else if(printType.isMember()){
					int memberOperationId = new Parcel(request.body).readInt();
					new PrintHandler(term)
						.addTypeContent(TypeContentFactory.instance().createMemberReceiptContent(printType, term, memberOperationId))
						.fireAsync();
				}
				
				response = new RespACK(request.header);
			
				//handle the query food group
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_GROUP){
				List<Pager> pagers = CalcFoodGroupDao.calc(term);
				response = new RespPackage(request.header, pagers, 0);
				
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
				new RespNAK(request.header, e.getErrCode()).writeToStream(out);
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



