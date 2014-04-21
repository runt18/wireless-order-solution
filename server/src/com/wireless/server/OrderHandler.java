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

import com.wireless.db.client.member.MemberCommentDao;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.foodAssociation.QueryFoodAssociationDao;
import com.wireless.db.foodGroup.CalcFoodGroupDao;
import com.wireless.db.frontBusiness.CancelOrder;
import com.wireless.db.frontBusiness.InsertOrder;
import com.wireless.db.frontBusiness.PayOrder;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.frontBusiness.TransTblDao;
import com.wireless.db.frontBusiness.UpdateOrder;
import com.wireless.db.frontBusiness.UpdateOrder.DiffResult;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.DeviceDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.resp.RespACK;
import com.wireless.pack.resp.RespNAK;
import com.wireless.pack.resp.RespOTAUpdate;
import com.wireless.pack.resp.RespPackage;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberComment;
import com.wireless.pojo.client.MemberComment.CommitBuilder;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.foodGroup.Pager;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.print.scheme.JobContentFactory;
import com.wireless.sccon.ServerConnector;
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
			
			RespPackage response = null;
			
			if(request.header.mode == Mode.TEST && request.header.type == Type.PING){
				//handle the ping test request
				response = new RespACK(request.header);
				
			}else if(request.header.mode == Mode.OTA && request.header.type == Type.GET_HOST){
				//handle the OTA update request
				if(WirelessSocketServer.OTA_IP.length() == 0 || WirelessSocketServer.OTA_Port.length() == 0){
					response = new RespNAK(request.header);
				}else{
					response = new RespOTAUpdate(request.header, WirelessSocketServer.OTA_IP, WirelessSocketServer.OTA_Port);
				}
				
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_STAFF){
				//handle the query staff request
				Device device = DeviceDao.getWorkingDeviceById(new Parcel(request.body).readParcel(Device.CREATOR).getDeviceId());
				response = new RespPackage(request.header, StaffDao.getStaffs(device.getRestaurantId()), Staff.ST_PARCELABLE_COMPLEX);
				
			}else{
				
			    // Extract the staff and restaurant id from header of request package
				int staffId = ((request.header.staffId[0] & 0x000000FF) |
				   	   		  ((request.header.staffId[1] & 0x000000FF) << 8) |
				   	   		  ((request.header.staffId[2] & 0x000000FF) << 16) |
				   	   		  ((request.header.staffId[3] & 0x000000FF) << 24));
				
//				short restaurantId = (short)((request.header.restaurantId[0] & 0x000000FF) | 
//											((request.header.restaurantId[1] & 0x000000FF) << 8)); 
				
				// Verify to check the staff
				final Staff staff = StaffDao.verify(staffId);
				
				if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_MENU){
					//handle query menu request
					response = new RespPackage(request.header, QueryMenu.exec(staff), 0);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_RESTAURANT){
					//handle query restaurant request
					response = new RespPackage(request.header, RestaurantDao.getById(staff.getRestaurantId()), 0);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_REGION){
					//handle query region request
					response = new RespPackage(request.header, RegionDao.getRegions(staff, null, null), Region.REGION_PARCELABLE_COMPLEX);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_ASSOCIATION){
					//handle query the associated food
					Food foodToAssociated = new Parcel(request.body).readParcel(Food.CREATOR);
					response = new RespPackage(request.header, QueryFoodAssociationDao.exec(staff, foodToAssociated), Food.FOOD_PARCELABLE_SIMPLE);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_SELL_OUT){
					//handle query sell out foods request
					response = new RespPackage(request.header, 
											   FoodDao.getPureByCond(staff, " AND FOOD.status & " + Food.SELL_OUT + " <> 0 ", null), 
											   Food.FOOD_PARCELABLE_SIMPLE);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.MAKE_FOOD_SELL_OUT){
					//handle update the food to be sell out
					List<Food> toSellOut = new Parcel(request.body).readParcelList(Food.CREATOR);
					for(Food f : toSellOut){
						FoodDao.update(staff, new Food.UpdateBuilder(f.getFoodId()).setSellOut(true));
					}
					response = new RespACK(request.header);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.MAKE_FOOD_ON_SALE){
					//handle update the food to be on sale 
					List<Food> toOnSale = new Parcel(request.body).readParcelList(Food.CREATOR);
					for(Food f : toOnSale){
						FoodDao.update(staff, new Food.UpdateBuilder(f.getFoodId()).setSellOut(false));
					}
					response = new RespACK(request.header);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE){
					//handle query table request
					response = new RespPackage(request.header, TableDao.getTables(staff, null, null), Table.TABLE_PARCELABLE_COMPLEX);
				
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_BY_TBL){
					//handle query order request
					Table tableToQuery = new Parcel(request.body).readParcel(Table.CREATOR);
					response = new RespPackage(request.header, OrderDao.getByTableAliasDync(staff, tableToQuery.getAliasId()), Order.ORDER_PARCELABLE_4_QUERY);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE_STATUS){
					//handle query table status
					Table tblToQuery = TableDao.getTableByAlias(staff, new Parcel(request.body).readParcel(Table.CREATOR).getAliasId());
					response = new RespACK(request.header, (byte)tblToQuery.getStatus().getVal());
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER){
					//handle insert order request
					response = doInsertOrder(staff, request);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER_FORCE){
					//handle insert order request force
					response = doInsertOrderForce(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.UPDATE_ORDER){
					//handle update order request
					response = doUpdateOrder(staff, request);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.TRANS_TABLE){
					//handle the table transfer request 
					response = doTransTable(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.CANCEL_ORDER){
					//handle the cancel order request
					Table tblToCancel = new Parcel(request.body).readParcel(Table.CREATOR);
					CancelOrder.execByTable(staff, tblToCancel.getAliasId());
					response = new RespACK(request.header);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.PAY_ORDER || request.header.type == Type.PAY_TEMP_ORDER){
					//handle the pay order request
					response = doPayOrder(staff, request);
					
				}else if(request.header.mode == Mode.PRINT && request.header.type == Type.PRINT_CONTENT){
					//handle the print request
					response = doPrintContent(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_GROUP){
					//handle the query food group
					List<Pager> pagers = CalcFoodGroupDao.calc(staff);
					response = new RespPackage(request.header, pagers, 0);
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.QUERY_MEMBER){
					//handle the request to query member
					response = new RespPackage(request.header, MemberDao.getByCond(staff, null, null), Member.MEMBER_PARCELABLE_SIMPLE);
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.QUERY_INTERESTED_MEMBER){
					//handle the request to query interested member
					response = new RespPackage(request.header, MemberDao.getInterestedMember(staff, null), Member.MEMBER_PARCELABLE_SIMPLE);
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.INTERESTED_IN_MEMBER){
					//handle the request to be interested in specific member
					MemberDao.interestedIn(staff, new Parcel(request.body).readParcel(Member.CREATOR).getId());
					response = new RespACK(request.header);
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.CANCEL_INTERESTED_IN_MEMBER){
					//handle the request to cancel interested in specific member
					MemberDao.cancelInterestedIn(staff, new Parcel(request.body).readParcel(Member.CREATOR).getId());
					response = new RespACK(request.header);
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.QUERY_MEMBER_DETAIL){
					//handle the request to query member detail
					response = new RespPackage(request.header, MemberDao.getById(staff, new Parcel(request.body).readParcel(Member.CREATOR).getId()), Member.MEMBER_PARCELABLE_COMPLEX);
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.COMMIT_MEMBER_COMMENT){
					//handle the request to commit member comment
					MemberComment comment = new Parcel(request.body).readParcel(MemberComment.CREATOR);
					if(comment.isPublic()){
						MemberCommentDao.commit(staff, CommitBuilder.newPublicBuilder(staff.getId(), comment.getMember().getId(), comment.getComment()));
					}else{
						MemberCommentDao.commit(staff, CommitBuilder.newPrivateBuilder(staff.getId(), comment.getMember().getId(), comment.getComment()));
					}
					response = new RespACK(request.header);
				}
			}

			//send the response to terminal
			response.writeToStream(out);
			
		}catch(BusinessException e){
			try{
				new RespNAK(request.header, e.getErrCode()).writeToStream(out);
			}catch(IOException ignored){}
			
			e.printStackTrace();
			
		}catch(IOException | SQLException e){
			try{
				new RespNAK(request.header, new BusinessException(e.getMessage()).getErrCode()).writeToStream(out);
			}catch(IOException ignored){}
			
			e.printStackTrace();
			
		}catch(Exception e){
			try{
				new RespNAK(request.header, new BusinessException(e.getMessage()).getErrCode()).writeToStream(out);
			}catch(IOException ignored){}
			
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
	
	private RespPackage doInsertOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		//handle insert order request 
		List<Printer> printers = PrinterDao.getPrinters(staff);
		Order orderToInsert = new Parcel(request.body).readParcel(Order.CREATOR);
		
		orderToInsert = InsertOrder.exec(staff, orderToInsert);
		
		if(request.header.reserved == PrintOption.DO_PRINT.getVal()){
			new PrintHandler(staff)
				.addContent(JobContentFactory.instance().createSummaryContent(PType.PRINT_ORDER, 
				 															  staff, 
				 															  printers,
									 										  orderToInsert))
				.addContent(JobContentFactory.instance().createDetailContent(PType.PRINT_ORDER_DETAIL, 
																		     staff, 
																		     printers,
																		     orderToInsert))
				.fireAsync();
		}
		
		return new RespACK(request.header);
	}
	
	private RespPackage doInsertOrderForce(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		//handle insert order request force
		Order orderToInsert = new Parcel(request.body).readParcel(Order.CREATOR);
		
		Table tblToOrder = TableDao.getTableByAlias(staff, orderToInsert.getDestTbl().getAliasId());
		
		if(tblToOrder.isIdle()){
			return doInsertOrder(staff, request);
			
		}else if(tblToOrder.isBusy()){
			Order orderToUpdate = OrderDao.getByTableAlias(staff, tblToOrder.getAliasId());
			orderToUpdate.addFoods(orderToInsert.getOrderFoods(), staff);
			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, orderToUpdate, Type.UPDATE_ORDER, PrintOption.valueOf(request.header.reserved)));
			if(resp.header.type == Type.NAK){
				throw new BusinessException(new Parcel(resp.body).readParcel(ErrorCode.CREATOR));
			}else{
				return new RespACK(request.header);
			}
			
		}else{
			throw new BusinessException("Unknown table status");
		}
	}
	
	private RespPackage doUpdateOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		//handle update order request
		Order orderToUpdate = new Parcel(request.body).readParcel(Order.CREATOR);
		DiffResult diffResult = UpdateOrder.execById(staff, orderToUpdate);
		List<Printer> printers = PrinterDao.getPrinters(staff);
		
		if(request.header.reserved == PrintOption.DO_PRINT.getVal()){
			
			PrintHandler printHandler = new PrintHandler(staff);
			
			if(!diffResult.hurriedFoods.isEmpty()){
				diffResult.newOrder.setOrderFoods(diffResult.hurriedFoods);
				//print the summary to hurried foods
				printHandler.addContent(JobContentFactory.instance().createSummaryContent(PType.PRINT_ALL_HURRIED_FOOD, 
																							  staff, 
																							  printers,
																							  diffResult.newOrder));
				//print the detail to hurried foods
				printHandler.addContent(JobContentFactory.instance().createDetailContent(PType.PRINT_HURRIED_FOOD, 
																							 staff,
																							 printers,
																							 diffResult.newOrder));
			}
			
			if(!diffResult.extraFoods.isEmpty()){
				diffResult.newOrder.setOrderFoods(diffResult.extraFoods);
				//print the summary to extra foods
				printHandler.addContent(JobContentFactory.instance().createSummaryContent(PType.PRINT_ALL_EXTRA_FOOD, 
																							  staff,
																							  printers,
																							  diffResult.newOrder));
				//print the detail to extra foods
				printHandler.addContent(JobContentFactory.instance().createDetailContent(PType.PRINT_EXTRA_FOOD, 
																							 staff,
																							 printers,
																							 diffResult.newOrder));
			}

			if(!diffResult.cancelledFoods.isEmpty()){
				diffResult.newOrder.setOrderFoods(diffResult.cancelledFoods);
				//print the summary to canceled foods
				printHandler.addContent(JobContentFactory.instance().createSummaryContent(PType.PRINT_ALL_CANCELLED_FOOD,
																							  staff,
																							  printers,
																							  diffResult.newOrder));
				//print the detail to canceled foods
				printHandler.addContent(JobContentFactory.instance().createDetailContent(PType.PRINT_CANCELLED_FOOD,
																							 staff,
																							 printers,
																							 diffResult.newOrder));

			}

			//print the transfer
			printHandler.addContent(JobContentFactory.instance().createTransContent(PType.PRINT_TRANSFER_TABLE, 
																						 staff,
																						 printers,
																						 diffResult.newOrder.getId(), 
																						 diffResult.oriOrder.getDestTbl(),
																						 diffResult.newOrder.getDestTbl()));

			//Fire to execute print action.
			printHandler.fireAsync();
		}
		return new RespACK(request.header);
	}
	
	private RespPackage doTransTable(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		Table[] tables = new Parcel(request.body).readParcelArray(Table.CREATOR);
		if(tables != null){
			Table srcTbl = tables[0];
			Table destTbl = tables[1];
			
			int orderId = TransTblDao.exec(staff, srcTbl, destTbl);
			List<Printer> printers = PrinterDao.getPrinters(staff);
			
			new PrintHandler(staff)
				.addContent(JobContentFactory.instance().createTransContent(PType.PRINT_TRANSFER_TABLE, staff, printers, orderId, srcTbl, destTbl))
				.fireAsync();
			
		}
		
		return new RespACK(request.header);
	}
	
	private RespPackage doPayOrder(Staff staff, ProtocolPackage request)  throws SQLException, BusinessException{
		Order.PayBuilder payParam = new Parcel(request.body).readParcel(Order.PayBuilder.CREATOR);
		
		List<Printer> printers = PrinterDao.getPrinters(staff);
		
		/**
		 * If pay order temporary, just only print the temporary receipt.
		 * Otherwise perform the pay action and print receipt 
		 */
		if(request.header.type == Type.PAY_TEMP_ORDER){
			if(payParam.getPrintOption() == PrintOption.DO_PRINT){
				new PrintHandler(staff)
					.addContent(JobContentFactory.instance().createReceiptContent(PType.PRINT_TEMP_RECEIPT, 
																				  staff,
																				  printers,
																				  PayOrder.payTemp(staff, payParam)))
					.fireAsync();
			}else{
				PayOrder.payTemp(staff, payParam);
			}
			
		}else{
			
			final Order order = PayOrder.pay(staff, payParam);
			
			PrintHandler printHandler = new PrintHandler(staff);
			
			printHandler.addContent(JobContentFactory.instance().createReceiptContent(PType.PRINT_RECEIPT, staff, printers, order));
			
			//Perform to print the member receipt if settled by member.
			if(order.isSettledByMember()){
				printHandler.addContent(JobContentFactory.instance().createMemberReceiptContent(PType.PRINT_MEMBER_RECEIPT, 
																								staff, 
																								printers,
																								order.getMemberOperationId()));
			}
			
			printHandler.fireAsync();
			
		}
		
		return new RespACK(request.header);
	}
	
	private RespPackage doPrintContent(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		PType printType = PType.valueOf(request.header.reserved);
		List<Printer> printers = PrinterDao.getPrinters(staff);
		
		if(printType.isSummary()){
			int orderId = new Parcel(request.body).readInt();
			new PrintHandler(staff)
				.addContent(JobContentFactory.instance().createSummaryContent(printType, staff, printers, orderId))
				.fireAsync();
			
		}else if(printType.isDetail()){
			int orderId = new Parcel(request.body).readInt();
			new PrintHandler(staff)
				.addContent(JobContentFactory.instance().createDetailContent(printType, staff, printers, orderId))
				.fireAsync();
			
		}else if(printType.isReceipt()){
			int orderId = new Parcel(request.body).readInt();
			new PrintHandler(staff)
				.addContent(JobContentFactory.instance().createReceiptContent(printType, staff, printers, orderId))
				.fireAsync();
			
		}else if(printType.isTransTbl()){
			Parcel p = new Parcel(request.body);
			int orderId = p.readInt();
			Table srcTbl = p.readParcel(Table.CREATOR);
			Table destTbl = p.readParcel(Table.CREATOR);
			new PrintHandler(staff)
				.addContent(JobContentFactory.instance().createTransContent(printType, staff, printers, orderId, srcTbl, destTbl))
				.fireSync();
			
		}else if(printType.isShift()){
			Parcel p = new Parcel(request.body);
			long onDuty = p.readLong();
			long offDuty = p.readLong();
			new PrintHandler(staff)
				.addContent(JobContentFactory.instance().createShiftContent(printType, staff, printers, onDuty, offDuty))
				.fireAsync();
			
		}else if(printType.isMember()){
			int moId = new Parcel(request.body).readInt();
			new PrintHandler(staff)
				.addContent(JobContentFactory.instance().createMemberReceiptContent(printType, staff, printers, moId))
				.fireAsync();
		}
		
		return new RespACK(request.header);
	}
}



