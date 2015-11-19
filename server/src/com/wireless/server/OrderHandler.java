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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.marker.weixin.api.BaseAPI;

import com.alibaba.fastjson.JSON;
import com.wireless.db.foodAssociation.QueryFoodAssociationDao;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.member.MemberCommentDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberLevelDao;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.orderMgr.InsertOrder;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.orderMgr.PayOrder;
import com.wireless.db.orderMgr.UpdateOrder;
import com.wireless.db.orderMgr.UpdateOrder.DiffResult;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.DeviceDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.IOError;
import com.wireless.exception.RestaurantError;
import com.wireless.exception.WxOrderError;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.req.ReqPrintContent;
import com.wireless.pack.req.ReqQueryMember;
import com.wireless.pack.resp.RespACK;
import com.wireless.pack.resp.RespNAK;
import com.wireless.pack.resp.RespOTAUpdate;
import com.wireless.pack.resp.RespPackage;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberComment;
import com.wireless.pojo.member.MemberComment.CommitBuilder;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.print.content.ContentParcel;
import com.wireless.print.content.concrete.FoodDetailContent;
import com.wireless.print.scheme.JobContentFactory;
import com.wireless.sccon.ServerConnector;
import com.wireless.sms.SMS;
import com.wireless.sms.msg.Msg4Consume;
import com.wireless.sms.msg.Msg4Upgrade;

import cn.beecloud.BCEumeration.PAY_CHANNEL;
import cn.beecloud.BCPay;
import cn.beecloud.BCPayResult;
import cn.beecloud.BeeCloud;
import cn.beecloud.bean.BCPayParameter;
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
			
			final ProtocolPackage response;
			
			if(request.header.mode == Mode.DIAGNOSIS && request.header.type == Type.PING){
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
				Device device = DeviceDao.getWorkingDevices(new Parcel(request.body).readParcel(Device.CREATOR).getDeviceId());
				response = new RespPackage(request.header).fillBody(StaffDao.getByRestaurant(device.getRestaurantId()), Staff.ST_PARCELABLE_COMPLEX);
				
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_BACKUP_SERVER){
				//handle the query backup connectors
				response = new RespPackage(request.header).fillBody(WirelessSocketServer.backups, 0);
			
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
					response = new RespPackage(request.header).fillBody(QueryMenu.exec(staff), 0);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_RESTAURANT){
					//handle query restaurant request
					response = new RespPackage(request.header).fillBody(RestaurantDao.getById(staff.getRestaurantId()), 0);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_REGION){
					//handle query region request
					response = new RespPackage(request.header).fillBody(RegionDao.getByStatus(staff, Region.Status.BUSY), Region.REGION_PARCELABLE_COMPLEX);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_ASSOCIATION){
					//handle query the associated food
					Food foodToAssociated = new Parcel(request.body).readParcel(Food.CREATOR);
					response = new RespPackage(request.header).fillBody(QueryFoodAssociationDao.exec(staff, foodToAssociated), Food.FOOD_PARCELABLE_SIMPLE);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_SELL_OUT){
					//handle query sell out foods request
					response = new RespPackage(request.header).fillBody( 
											   FoodDao.getPureByCond(staff, new FoodDao.ExtraCond().addStatus(Food.SELL_OUT).addStatus(Food.LIMIT), null), 
											   Food.FOOD_PARCELABLE_SELL_OUT);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.MAKE_FOOD_SELL_OUT){
					//handle update the food to be sell out
					for(Food f : new Parcel(request.body).readParcelList(Food.CREATOR)){
						FoodDao.update(staff, new Food.UpdateBuilder(f.getFoodId()).setSellOut(true));
					}
					response = new RespACK(request.header);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.MAKE_FOOD_ON_SALE){
					//handle update the food to be on sale 
					for(Food f : new Parcel(request.body).readParcelList(Food.CREATOR)){
						FoodDao.update(staff, new Food.UpdateBuilder(f.getFoodId()).setSellOut(false));
					}
					response = new RespACK(request.header);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.MAKE_LIMIT_REMAINING){
					//handle update the food limit remaining
					FoodDao.update(staff, new Parcel(request.body).readParcel(Food.LimitRemainingBuilder.CREATOR));
					response = new RespACK(request.header);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE){
					//handle query table request
					response = new RespPackage(request.header).fillBody(TableDao.getByCond(staff, null, null), Table.TABLE_PARCELABLE_COMPLEX);
				
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_WX_ORDER){
					//handle the query wx order
					response = doQueryWxOrder(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_BY_TBL){
					//handle query order request
					Table tableToQuery = new Parcel(request.body).readParcel(Table.CREATOR);
					response = new RespPackage(request.header).fillBody(OrderDao.getByTableId(staff, tableToQuery.getId()), Order.ORDER_PARCELABLE_4_QUERY);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE_STATUS){
					//handle query table status
					Table tblToQuery = TableDao.getById(staff, new Parcel(request.body).readParcel(Table.CREATOR).getId());
					response = new RespACK(request.header, (byte)tblToQuery.getStatus().getVal());
					
//				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_FEAST_ORDER){
//					//handle feast order request
//					response = doFeastOrder(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER){
					//handle insert order request
					response = doInsertOrder(staff, request);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.INSERT_ORDER_FORCE){
					//handle insert order request force
					response = doInsertOrderForce(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.UPDATE_ORDER){
					//handle update order request
					response = doUpdateOrder(staff, request);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.TRANSFER_ORDER_FOOD){
					//handle the transfer order food
					response = doTransOrderFood(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.GIFT_ORDER_FOOD){
					//handle the gift order food
					response = doGiftOrderFood(staff, request);
						
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.TRANS_TABLE){
					//handle the table transfer request 
					response = doTransTable(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.DISCOUNT_ORDER){
					//handle the discount order request
					response = doDiscountOrder(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.PAY_ORDER || request.header.type == Type.PAY_TEMP_ORDER){
					//handle the pay order request
					response = doPayOrder(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.RE_PAY_ORDER){
					//handle the repaid order request
					response = doRepayOrder(staff, request);
					
				}else if(request.header.mode == Mode.PRINT && request.header.type == Type.PRINT_DISPATCH_CONTENT){
					//handle the print dispatch
					response = new PrintHandler(staff).processDispatch(request, new Parcel(request.body).readParcel(ContentParcel.CREATOR));
					
				}else if(request.header.mode == Mode.PRINT && request.header.type == Type.PRINT_CONTENT){
					//handle the print request
					response = doPrintContent(staff, request);
					
				}else if(request.header.mode == Mode.DIAGNOSIS && request.header.type == Type.PRINTER){
					//handler the printer diagnosis
					response = new PrinterDiagnoseHandler(staff).process(request);
					
				}else if(request.header.mode == Mode.DIAGNOSIS && request.header.type == Type.PRINTER_DISPATCH){
					response = new PrinterDiagnoseHandler(staff).processDispatch(request);
					
//				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_FOOD_GROUP){
//					//handle the query food group
//					List<Pager> pagers = CalcFoodGroupDao.calc(staff);
//					response = new RespPackage(request.header, pagers, 0);
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.QUERY_MEMBER){
					//handle the request to query member
					response = new RespPackage(request.header).fillBody(MemberDao.getByCond(staff, new MemberDao.ExtraCond(new Parcel(request.body).readParcel(ReqQueryMember.ExtraCond.CREATOR)), null), Member.MEMBER_PARCELABLE_SIMPLE);
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.QUERY_INTERESTED_MEMBER){
					//handle the request to query interested member
					response = new RespPackage(request.header).fillBody(MemberDao.getInterestedMember(staff, null), Member.MEMBER_PARCELABLE_SIMPLE);
					
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
					response = new RespPackage(request.header).fillBody(MemberDao.getById(staff, new Parcel(request.body).readParcel(Member.CREATOR).getId()), Member.MEMBER_PARCELABLE_COMPLEX);
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.COMMIT_MEMBER_COMMENT){
					//handle the request to commit member comment
					MemberComment comment = new Parcel(request.body).readParcel(MemberComment.CREATOR);
					if(comment.isPublic()){
						MemberCommentDao.commit(staff, CommitBuilder.newPublicBuilder(staff.getId(), comment.getMember().getId(), comment.getComment()));
					}else{
						MemberCommentDao.commit(staff, CommitBuilder.newPrivateBuilder(staff.getId(), comment.getMember().getId(), comment.getComment()));
					}
					response = new RespACK(request.header);
					
				}else{
					response = new RespNAK(request.header);
				}
			}

			//send the response to terminal
			response.writeToStream(out);
			
		}catch(BusinessException e){
			try{
				new RespNAK(request.header, e.getErrCode()).writeToStream(out);
			}catch(IOException ignored){}
			
			e.printStackTrace();
			
		}catch(SQLException e){
			try{
				new RespNAK(request.header, new BusinessException(e.getMessage()).getErrCode()).writeToStream(out);
			}catch(IOException ignored){}
			
			e.printStackTrace();
			
		}catch(IOException e){
			try{
				new RespNAK(request.header, IOError.IO_ERROR).writeToStream(out);
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
	
//	private RespPackage doFeastOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
//		int orderId = OrderDao.feast(staff, new Parcel(request.body).readParcel(Order.FeastBuilder.CREATOR));
//		//Perform to print receipt to this feast order.
//		new PrintHandler(staff).process(JobContentFactory.instance().createReceiptContent(PType.PRINT_RECEIPT, staff, 
//																						  PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setEnabled(true)), 
//																						  OrderDao.getById(staff, orderId, DateType.TODAY)));
//		return new RespACK(request.header);
//	}
	
	private ProtocolPackage doQueryWxOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		List<WxOrder> result = new ArrayList<WxOrder>();
		for(WxOrder wxOrder : new Parcel(request.body).readParcelList(WxOrder.CREATOR)){
			wxOrder = WxOrderDao.getByCode(staff, wxOrder.getCode());
			if(wxOrder.getStatus() == WxOrder.Status.COMMITTED){
				result.add(wxOrder);
			}
		}
		if(result.isEmpty()){
			throw new BusinessException(WxOrderError.WX_ORDER_NOT_EXIST);
		}else{
			return new RespPackage(request.header).fillBody(result, WxOrder.WX_ORDER_PARCELABLE_COMPLEX);
		}
	}
	
	private ProtocolPackage doInsertOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		//handle insert order request 
		final List<Printer> printers = PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setEnabled(true).setOriented(Printer.Oriented.ALL));
		
		final Order.InsertBuilder builder = new Parcel(request.body).readParcel(Order.InsertBuilder.CREATOR);
		
		//Add the specific printers.
		for(Printer orientedPrinter : builder.getPrinters()){
			printers.addAll(PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setEnabled(true).setId(orientedPrinter.getId()).setOriented(Printer.Oriented.SPECIFIC)));
		}
		
		Order orderToInsert = InsertOrder.exec(staff, builder);
		
		if(request.header.reserved == PrintOption.DO_PRINT.getVal()){
			PrintHandler printHandler = new PrintHandler(staff);
			printHandler.process(JobContentFactory.instance().createSummaryContent(PType.PRINT_ORDER, 
				 															  	   staff, 
				 															  	   printers,
				 															  	   orderToInsert,
				 															  	   FoodDetailContent.DetailType.DELTA));
			printHandler.process(JobContentFactory.instance().createDetailContent(PType.PRINT_ORDER_DETAIL, 
																		     	  staff, 
																		     	  printers,
																		     	  orderToInsert,
																		     	  FoodDetailContent.DetailType.DELTA));
		}
		if(orderToInsert.getCategory().isJoin()){
			return new RespPackage(request.header).fillBody(orderToInsert.getDestTbl(), Table.TABLE_PARCELABLE_4_QUERY);
		}else{
			return new RespACK(request.header);
		}
	}
	
	private ProtocolPackage doInsertOrderForce(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		//handle insert order request force
		final Order.InsertBuilder builder = new Parcel(request.body).readParcel(Order.InsertBuilder.CREATOR);
		
		final Order newOrder = builder.build();
		
		Table tblToOrder = TableDao.getById(staff, newOrder.getDestTbl().getId());
		
		if(tblToOrder.isIdle()){
			return doInsertOrder(staff, request);
			
		}else if(tblToOrder.isBusy()){
			Order oriOrder = OrderDao.getByTableId(staff, tblToOrder.getId());
			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, 
																					 new Order.UpdateBuilder(oriOrder)
																							  .addOri(oriOrder.getOrderFoods())
																							  .addNew(newOrder.getOrderFoods(), staff)
																							  .setWxOrders(newOrder.getWxOrders())
																							  .addPrinters(builder.getPrinters()), 
																					 PrintOption.valueOf(request.header.reserved)));
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
		final Order.UpdateBuilder builder = new Parcel(request.body).readParcel(Order.UpdateBuilder.CREATOR);
		
		final List<Printer> printers = PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setEnabled(true).setOriented(Printer.Oriented.ALL));
		
		//Add the specific printers.
		for(Printer orientedPrinter : builder.getPrinters()){
			printers.addAll(PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setId(orientedPrinter.getId()).setEnabled(true).setOriented(Printer.Oriented.SPECIFIC)));
		}
		
		final DiffResult diffResult = UpdateOrder.exec(staff, builder);

		
		if(request.header.reserved == PrintOption.DO_PRINT.getVal()){
			
			PrintHandler printHandler = new PrintHandler(staff);
			
			if(!diffResult.hurriedFoods.isEmpty()){
				Order hurriedOrder = new Order();
				hurriedOrder.copyFrom(diffResult.newOrder);
				hurriedOrder.setOrderFoods(diffResult.hurriedFoods);
				//print the summary to hurried foods
				printHandler.process(JobContentFactory.instance().createSummaryContent(PType.PRINT_ALL_HURRIED_FOOD, 
																					   staff, 
																					   printers,
																					   hurriedOrder,
																					   FoodDetailContent.DetailType.TOTAL));
				//print the detail to hurried foods
				printHandler.process(JobContentFactory.instance().createDetailContent(PType.PRINT_HURRIED_FOOD, 
																					  staff,
																					  printers,
																					  hurriedOrder,
																					  FoodDetailContent.DetailType.TOTAL));
			}
			
			if(!diffResult.extraFoods.isEmpty()){
				Order extraOrder = new Order();
				extraOrder.copyFrom(diffResult.newOrder);
				extraOrder.setOrderFoods(diffResult.extraFoods);
				//print the summary to extra foods
				printHandler.process(JobContentFactory.instance().createSummaryContent(PType.PRINT_ALL_EXTRA_FOOD, 
																					   staff,
																					   printers,
																					   extraOrder,
																					   FoodDetailContent.DetailType.DELTA));
				//print the detail to extra foods
				printHandler.process(JobContentFactory.instance().createDetailContent(PType.PRINT_EXTRA_FOOD_DETAIL, 
																					  staff,
																					  printers,
																					  extraOrder,
																					  FoodDetailContent.DetailType.DELTA));
			}

			if(!diffResult.cancelledFoods.isEmpty()){
				Order cancelledOrder = new Order();
				cancelledOrder.copyFrom(diffResult.newOrder);
				cancelledOrder.setOrderFoods(diffResult.cancelledFoods);
				//print the summary to canceled foods
				printHandler.process(JobContentFactory.instance().createSummaryContent(PType.PRINT_ALL_CANCELLED_FOOD,
																							  staff,
																							  printers,
																							  cancelledOrder,
																							  FoodDetailContent.DetailType.DELTA));
				//print the detail to canceled foods
				printHandler.process(JobContentFactory.instance().createDetailContent(PType.PRINT_CANCELLED_FOOD_DETAIL,
																							 staff,
																							 printers,
																							 cancelledOrder,
																							 FoodDetailContent.DetailType.DELTA));

			}

		}
		return new RespACK(request.header);
	}
	
	private RespPackage doGiftOrderFood(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		OrderDao.gift(staff, new Parcel(request.body).readParcel(Order.GiftBuilder.CREATOR));
		return new RespACK(request.header);
	}
	
	private RespPackage doTransOrderFood(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		Order.TransferBuilder builder = new Parcel(request.body).readParcel(Order.TransferBuilder.CREATOR);
		OrderDao.transfer(staff, builder);
		ServerConnector.instance().ask(ReqPrintContent.buildTransFood(staff, builder).build());
		return new RespACK(request.header);
	}
	
	private RespPackage doTransTable(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		Table.TransferBuilder builder = new Parcel(request.body).readParcel(Table.TransferBuilder.CREATOR);
		int orderId = TableDao.transfer(staff, builder);
		ServerConnector.instance().ask(ReqPrintContent.buildTransTbl(staff, orderId, new Table.Builder(builder.getSrcTbl().getId()), new Table.Builder(builder.getDestTbl().getId())).build());
		return new RespACK(request.header);
	}
	
	private RespPackage doDiscountOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		OrderDao.discount(staff, new Parcel(request.body).readParcel(Order.DiscountBuilder.CREATOR));
		return new RespACK(request.header);
	}
	
	private RespPackage doRepayOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		Order.RepaidBuilder builder = new Parcel(request.body).readParcel(Order.RepaidBuilder.CREATOR);
		OrderDao.repaid(staff, builder);
		if(request.header.reserved == PrintOption.DO_PRINT.getVal()){
			ServerConnector.instance().ask(ReqPrintContent.buildReceipt(staff, builder.getUpdateBuilder().build().getId()).setPrinters(builder.getPrinters()).build());
			Order order = OrderDao.getById(staff, builder.getUpdateBuilder().build().getId(), DateType.TODAY);
			if(order.isSettledByMember()){
				ServerConnector.instance().ask(ReqPrintContent.buildMemberReceipt(staff, MemberOperationDao.getLastConsumptionByOrder(staff, order).getId()).setPrinters(builder.getPrinters()).build());
			}
		}
		return new RespACK(request.header);
	}
	
	private RespPackage doPayOrder(final Staff staff, ProtocolPackage request)  throws SQLException, BusinessException{
		final Order.PayBuilder payBuilder = new Parcel(request.body).readParcel(Order.PayBuilder.CREATOR);
		
		final List<Printer> printers = PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setEnabled(true).setOriented(Printer.Oriented.ALL));
		//Add the specific printers.
		for(Printer orientedPrinter : payBuilder.getPrinters()){
			printers.addAll(PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setId(orientedPrinter.getId()).setEnabled(true).setOriented(Printer.Oriented.SPECIFIC)));
		}
		
		final PrintHandler printHandler = new PrintHandler(staff);

		/**
		 * If pay order temporary, just only print the temporary receipt.
		 * Otherwise perform the pay action and print receipt 
		 */
		if(request.header.type == Type.PAY_TEMP_ORDER){
			if(payBuilder.getPrintOption() == PrintOption.DO_PRINT){
				printHandler.process(JobContentFactory.instance().createReceiptContent(PType.PRINT_TEMP_RECEIPT, staff,  printers, PayOrder.payTemp(staff, payBuilder)));

			}else{
				PayOrder.payTemp(staff, payBuilder);
			}
			
		}else{
			
			final Order order = PayOrder.pay(staff, payBuilder);
			
			//Perform to print receipt to this order.
			printHandler.process(JobContentFactory.instance().createReceiptContent(PType.PRINT_RECEIPT, staff, printers, order));

			if(order.isSettledByMember()){
				
				final MemberOperation lastOperation = MemberOperationDao.getLastConsumptionByOrder(staff, order);
				
				//Perform to print the member receipt if settled by member.
				printHandler.process(JobContentFactory.instance().createMemberReceiptContent(PType.PRINT_MEMBER_RECEIPT, staff, printers, lastOperation));
				
				//Perform to send the weixin msg to member.
				try {
					BaseAPI.doPost("http://ts.e-tones.net/wx-term/WxNotifyMember.do?dataSource=bill&orderId=" + order.getId() + "&staffId=" + staff.getId(), "");
				} catch (Exception ignored) {
					ignored.printStackTrace();
				}
				
				//Perform SMS notification to member coupon dispatch & member upgrade in another thread so that not affect the order payment.
				WirelessSocketServer.threadPool.execute(new Runnable(){
					@Override
					public void run() {
						
						try{
							//Perform the member upgrade
							Msg4Upgrade msg4Upgrade = MemberLevelDao.upgrade(staff, order.getMemberId());

							if(payBuilder.isSendSMS()){
								MemberOperation mo = MemberOperationDao.getLastConsumptionByOrder(staff, order);
								//Send SMS if perform member consumption
								SMS.send(staff, mo.getMemberMobile(), new Msg4Consume(mo));
								//Send SMS if member upgrade
								if(msg4Upgrade != null){
									SMS.send(staff, mo.getMemberMobile(), msg4Upgrade);
								}

							}
						}catch(BusinessException | SQLException | IOException e){
							e.printStackTrace();
						}
					}
				});

			}
			
		}
		
		return new RespACK(request.header);
	}
	
	private RespPackage doPrintContent(final Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		final PType printType = PType.valueOf(request.header.reserved);
		final Parcel parcel = new Parcel(request.body);
		
		final List<Printer> printers = PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setEnabled(true).setOriented(Printer.Oriented.ALL));
		//Get the specific printers
		final List<Printer> oriented = parcel.readParcelList(Printer.CREATOR);
		for(Printer orientedPrinter : oriented){
			printers.addAll(PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setId(orientedPrinter.getId()).setOriented(Printer.Oriented.SPECIFIC)));
		}
		
		if(printType.isSummary()){
			int orderId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createSummaryContent(printType, staff, printers, orderId, FoodDetailContent.DetailType.TOTAL));
			
		}else if(printType.isDetail()){
			int orderId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createDetailContent(printType, staff, printers, orderId, FoodDetailContent.DetailType.TOTAL));
			
		}else if(printType.isReceipt()){
			int orderId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createReceiptContent(printType, staff, printers, orderId));
			
		}else if(printType.isWxReceipt()){
			final Order.PayBuilder payBuilder = parcel.readParcel(Order.PayBuilder.CREATOR);
			//打印微信支付单
			Restaurant restaurant = RestaurantDao.getById(staff.getRestaurantId());
			if(restaurant.hasBeeCloud()){
				BeeCloud.registerApp(restaurant.getBeeCloudAppId(), restaurant.getBeeCloudAppSecret());
				Map<String, Object> optional = new HashMap<String, Object>(){
					private static final long serialVersionUID = 1L;
					{ 
						put("payBuilder", JSON.toJSONString(payBuilder.toJsonMap(0)));
						put("staffId", Integer.toString(staff.getId()));	
					}
				};
				final Order order = PayOrder.calc(staff, payBuilder);
				BCPayParameter param = new BCPayParameter(PAY_CHANNEL.WX_NATIVE,
														  //1,
														  Float.valueOf(order.getActualPrice() * 100).intValue(), 
														  System.currentTimeMillis() + Integer.toString(payBuilder.getOrderId()),	//billNo 
														  restaurant.getName() + "(账单号：" + payBuilder.getOrderId() + ")"			//title
														  );
				param.setOptional(optional);
				BCPayResult bcPayResult = BCPay.startBCPay(param);
				
				if(bcPayResult.getType().ordinal() == 0){
					new PrintHandler(staff).process(JobContentFactory.instance().createReceiptContent(printType, staff, printers, order, bcPayResult.getCodeUrl()));
				}else{
					throw new BusinessException(bcPayResult.getErrMsg() + "," + bcPayResult.getErrDetail());
				}
			}else{
				throw new BusinessException(RestaurantError.BEE_CLOUD_NOT_BOUND);
			}
			
		}else if(printType.isTransTbl()){
			int orderId = parcel.readInt();
			Table srcTbl = TableDao.getById(staff, parcel.readParcel(Table.CREATOR).getId());
			Table destTbl = TableDao.getById(staff, parcel.readParcel(Table.CREATOR).getId());
			new PrintHandler(staff).process(JobContentFactory.instance().createTransContent(printType, staff, printers, orderId, srcTbl, destTbl));
			
		}else if(printType.isTransFood()){
			Order.TransferBuilder builder = parcel.readParcel(Order.TransferBuilder.CREATOR);
			Order srcOrder = OrderDao.getById(staff, builder.getSourceOrderId(), DateType.TODAY);
			Table destTbl = TableDao.getById(staff, builder.getDestTbl().getId());
			for(OrderFood foodOut : builder.getTransferFoods()){
				foodOut.asFood().copyFrom(FoodDao.getById(staff, foodOut.asFood().getFoodId()));
			}
			new PrintHandler(staff).process(JobContentFactory.instance().createTransFoodContent(printType, staff, printers, srcOrder.getId(), srcOrder.getDestTbl(), destTbl, builder.getTransferFoods()));
			
		}else if(printType.isShift()){
			long onDuty = parcel.readLong();
			long offDuty = parcel.readLong();
			Region.RegionId regionId = Region.RegionId.valueOf(parcel.readShort());
			if(regionId == Region.RegionId.REGION_NULL){
				List<Region> regions = RegionDao.getByStatus(staff, Region.Status.BUSY);
				if(!regions.isEmpty()){
					regionId = Region.RegionId.valueOf(regions.get(0).getId());
				}
			}
			new PrintHandler(staff).process(JobContentFactory.instance().createShiftContent(printType, staff, printers, new DutyRange(onDuty, offDuty), regionId));
			
		}else if(printType.isMember()){
			int moId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createMemberReceiptContent(printType, staff, printers, MemberOperationDao.getById(staff, DateType.TODAY, moId)));
			
		}else if(printType.is2ndDisplay()){
			float display = parcel.readFloat();
			new PrintHandler(staff).process(JobContentFactory.instance().create2ndDisplayContent(staff, printers, display));
		}
		
		return new RespACK(request.header);
	}
}



