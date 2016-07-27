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
import java.util.List;

import org.marker.weixin.api.BaseAPI;

import com.wireless.db.foodAssociation.QueryFoodAssociationDao;
import com.wireless.db.frontBusiness.DailySettleDao;
import com.wireless.db.frontBusiness.DailySettleDao.ManualResult;
import com.wireless.db.frontBusiness.QueryMenu;
import com.wireless.db.member.MemberDao;
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
import com.wireless.parcel.wrapper.IntParcel;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
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
/**
 * @author yzhang
 *
 */
class OrderHandler implements Runnable{
	
	private Socket _connection = null;
	private int _timeout = 10000;	//default timeout is 10s
	 
	OrderHandler(Socket connection){
		_connection = connection;
	}	
	
	OrderHandler(Socket connection, int timeout){
		_connection = connection;
		_timeout = timeout;
	}
	
	public void run(){
		
		ProtocolPackage request = new ProtocolPackage();
		InputStream in = null;
		OutputStream out = null;
		try{
			in = new BufferedInputStream(new DataInputStream(_connection.getInputStream()));
			out = new BufferedOutputStream(new DataOutputStream(_connection.getOutputStream()));
			
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
				response = new RespPackage(request.header).fillBody(StaffDao.getByDevice(device), Staff.ST_PARCELABLE_COMPLEX);
				
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
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.DAILY_SETTLE){
					//handle the daily settle
					response = doDailySettle(staff, request);
					
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
					
				}else if(request.header.mode == Mode.MEMBER && request.header.type == Type.QUERY_MEMBER_DETAIL){
					//handle the request to query member detail
					response = new RespPackage(request.header).fillBody(MemberDao.getById(staff, new Parcel(request.body).readParcel(Member.CREATOR).getId()), Member.MEMBER_PARCELABLE_COMPLEX);
					
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
				if(_connection != null){
					_connection.close();
					_connection = null;
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
	
	private ProtocolPackage doInsertOrder(final Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		//handle insert order request 
		final Order.InsertBuilder builder = new Parcel(request.body).readParcel(Order.InsertBuilder.CREATOR);
		
		final List<Printer> printers = getAvailPrinters(staff, builder.getPrinters());
		
		final Order orderToInsert = InsertOrder.exec(staff, builder);
		
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
		
		//新下单时打印【微信店小二】
		printWxWaiter(staff, orderToInsert, printers);
		
		if(orderToInsert.getCategory().isJoin()){
			return new RespPackage(request.header).fillBody(orderToInsert.getDestTbl(), Table.TABLE_PARCELABLE_4_QUERY);
		}else{
			return new RespACK(request.header).fillBody(new IntParcel(orderToInsert.getId()), 0);
		}
	}
	
	private void printWxWaiter(final Staff staff, final Order orderToInsert, List<Printer> printers){
		//新下单时打印【微信店小二】
		for(Printer printer : printers){
			for(PrintFunc func : printer.getPrintFuncs()){
				if(func.isTypeMatched(PType.PRINT_WX_WAITER) && func.isRegionMatched(orderToInsert.getRegion())){
					if(WirelessSocketServer.wxServer != null){
						WirelessSocketServer.threadPool.execute(new Runnable(){
							@Override
							public void run() {
								try {
									BaseAPI.doGet("http://" + WirelessSocketServer.wxServer + "/wx-term/WxOperateWaiter.do?dataSource=print&restaurantId=" + staff.getRestaurantId() + "&orderId=" + orderToInsert.getId());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						});
					}
					break;
				}
			}
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
				return new RespACK(request.header).fillBody(new IntParcel(oriOrder.getId()), 0);
			}
			
		}else{
			throw new BusinessException("Unknown table status");
		}
	}
	
	private ProtocolPackage doUpdateOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		//handle update order request
		final Order.UpdateBuilder builder = new Parcel(request.body).readParcel(Order.UpdateBuilder.CREATOR);
		
		final List<Printer> printers = getAvailPrinters(staff, builder.getPrinters());
		
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
		return new RespACK(request.header).fillBody(new IntParcel(diffResult.oriOrder.getId()), 0);
	}
	
	private RespPackage doGiftOrderFood(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		OrderDao.gift(staff, new Parcel(request.body).readParcel(Order.GiftBuilder.CREATOR));
		return new RespACK(request.header);
	}
	
	private RespPackage doTransOrderFood(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		final Order.TransferBuilder builder = new Parcel(request.body).readParcel(Order.TransferBuilder.CREATOR);
		final OrderDao.TransResult result = OrderDao.transfer(staff, builder);
		//Print the transfer food.
		new PrintHandler(staff).process(JobContentFactory.instance().createTransFoodContent(PType.PRINT_TRANSFER_FOOD, 
				staff, getAvailPrinters(staff, builder.getPrinters()), 
				builder.getSourceOrderId(), result.srcTbl, result.destTbl, result.transFoods));

		return new RespACK(request.header);
	}
	
	private RespPackage doTransTable(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		Table.TransferBuilder builder = new Parcel(request.body).readParcel(Table.TransferBuilder.CREATOR);
		int orderId = TableDao.transfer(staff, builder);
		ServerConnector.instance().ask(ReqPrintContent.buildTransTbl(staff, orderId, new Table.Builder(builder.getSrcTbl().getId()), new Table.Builder(builder.getDestTbl().getId())).setPrinters(builder.getPrinters()).build());
		return new RespACK(request.header);
	}
	
	private RespPackage doDiscountOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		OrderDao.discount(staff, new Parcel(request.body).readParcel(Order.DiscountBuilder.CREATOR));
		return new RespACK(request.header);
	}
	
	private RespPackage doRepayOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		Order.RepaidBuilder builder = new Parcel(request.body).readParcel(Order.RepaidBuilder.CREATOR);
		UpdateOrder.DiffResult diffResult = OrderDao.repaid(staff, builder);
		
		//Only print the detail to cancel order food.
		if(!diffResult.cancelledFoods.isEmpty()){
			PrintHandler printHandler = new PrintHandler(staff);
			Order cancelledOrder = OrderDao.getById(staff, diffResult.newOrder.getId(), DateType.TODAY);
			cancelledOrder.setOrderFoods(diffResult.cancelledFoods);
			//print the detail to canceled foods
			printHandler.process(JobContentFactory.instance().createDetailContent(PType.PRINT_CANCELLED_FOOD_DETAIL,
																				  staff,
																				  getAvailPrinters(staff, builder.getPrinters()),
																				  cancelledOrder,
																				  FoodDetailContent.DetailType.DELTA));

		}
		//Print the member receipt.
		if(request.header.reserved == PrintOption.DO_PRINT.getVal()){
			ServerConnector.instance().ask(ReqPrintContent.buildReceipt(staff, builder.getUpdateBuilder().build().getId()).setPrinters(builder.getPrinters()).build());
			Order order = OrderDao.getById(staff, builder.getUpdateBuilder().build().getId(), DateType.TODAY);
			if(order.isSettledByMember()){
				ServerConnector.instance().ask(ReqPrintContent.buildMemberReceipt(staff, MemberOperationDao.getLastConsumptionByOrder(staff, order).getId()).setPrinters(builder.getPrinters()).build());
			}
		}
		return new RespACK(request.header);
	}
	
	private RespPackage doDailySettle(final Staff staff, ProtocolPackage request)  throws SQLException, BusinessException, IOException{
		final ManualResult result = DailySettleDao.manual(staff);
		final ReqPrintContent dailyContent = ReqPrintContent.buildShiftReceipt(staff, result.getRange(), PType.PRINT_DAILY_SETTLE_RECEIPT);
		for(Printer printer : new Parcel(request.body).readParcelList(Printer.CREATOR)){
			dailyContent.addPrinter(printer);
		}
		ServerConnector.instance().ask(dailyContent.build());
		return new RespACK(request.header);
	}
	
	
	private RespPackage doPayOrder(final Staff staff, ProtocolPackage request)  throws SQLException, BusinessException{
		final Order.PayBuilder payBuilder = new Parcel(request.body).readParcel(Order.PayBuilder.CREATOR);
		
		final List<Printer> printers = getAvailPrinters(staff, payBuilder.getPrinters());
		
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
				
				//Perform SMS notification to member coupon dispatch & member upgrade in another thread so that not affect the order payment.
				WirelessSocketServer.threadPool.execute(new Runnable(){
					@Override
					public void run() {
						//Perform to send the weixin msg to member.
						try {
							BaseAPI.doPost("http://" + WirelessSocketServer.wxServer + "/wx-term/WxNotifyMember.do?dataSource=bill&orderId=" + order.getId() + "&staffId=" + staff.getId(), "");
						} catch (Exception ignored) {
							ignored.printStackTrace();
						}
						
						//Send SMS if perform member consumption.
						if(payBuilder.isSendSMS()){
							try{
								MemberOperation mo = MemberOperationDao.getLastConsumptionByOrder(staff, order);
								SMS.send(staff, mo.getMemberMobile(), new Msg4Consume(mo));
							}catch(BusinessException | SQLException | IOException ignored){
								ignored.printStackTrace();
							}
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
		
		final List<Printer> oriented = parcel.readParcelList(Printer.CREATOR);
		final List<Printer> printers = getAvailPrinters(staff, oriented);
		
		if(printType.isSummary()){
			//点菜总单
			int orderId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createSummaryContent(printType, staff, printers, orderId, FoodDetailContent.DetailType.TOTAL));
			
		}else if(printType.isDetail()){
			//点菜分单
			int orderId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createDetailContent(printType, staff, printers, orderId, FoodDetailContent.DetailType.TOTAL));
			
		}else if(printType.isReceipt()){
			//结账单
			int orderId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createReceiptContent(printType, staff, printers, orderId));
			
		}else if(printType.isWxReceipt()){
			//打印微信支付单
			final Order.PayBuilder payBuilder = parcel.readParcel(Order.PayBuilder.CREATOR);
			final Order order = PayOrder.calc(staff, payBuilder);
			final String codeUrl = parcel.readString();
			new PrintHandler(staff).process(JobContentFactory.instance().createReceiptContent(printType, staff, printers, order, codeUrl));
			
		}else if(printType.isTransTbl()){
			//转台
			int orderId = parcel.readInt();
			Table srcTbl = TableDao.getById(staff, parcel.readParcel(Table.CREATOR).getId());
			Table destTbl = TableDao.getById(staff, parcel.readParcel(Table.CREATOR).getId());
			new PrintHandler(staff).process(JobContentFactory.instance().createTransContent(printType, staff, printers, orderId, srcTbl, destTbl));
			
		}else if(printType.isShift()){
			//交班
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
			//会员小票
			int moId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createMemberReceiptContent(printType, staff, printers, MemberOperationDao.getById(staff, DateType.TODAY, moId)));
			
		}else if(printType.is2ndDisplay()){
			//客显
			float display = parcel.readFloat();
			new PrintHandler(staff).process(JobContentFactory.instance().create2ndDisplayContent(staff, printers, display));
			
		}else if(printType.isWxOrder()){
			//微信订单
			int wxOrderId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createWxOrderContent(staff, printers, wxOrderId));
			
		}else if(printType.isBook()){
			//预订
			int bookId = parcel.readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createBookContent(staff, printers, bookId));
			
		}else if(printType.isWxWaiter()){
			//微信小二
			int orderId = parcel.readInt();
			String qrCodeContent = parcel.readString();
			new PrintHandler(staff).process(JobContentFactory.instance().createWxWaiterContent(staff, printers, orderId, qrCodeContent));
			
		}else if(printType.isWxCallPay()){
			//微信呼叫结账
			int orderId = parcel.readInt();
			int memberId = parcel.readInt();
			final String payType = parcel.readString();
			new PrintHandler(staff).process(JobContentFactory.instance().createWxCallPayContent(staff, printers, orderId, memberId, payType));
		}
		
		return new RespACK(request.header);
	}
	
	private List<Printer> getAvailPrinters(Staff staff, List<Printer> orientedPrinters) throws SQLException{
		final List<Printer> printers = PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setEnabled(true).setOriented(Printer.Oriented.ALL));
		
		//Add the specific printers.
		if(orientedPrinters != null){
			for(Printer orientedPrinter : orientedPrinters){
				printers.addAll(PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setEnabled(true).setId(orientedPrinter.getId()).setOriented(Printer.Oriented.SPECIFIC)));
			}
		}		
		return printers;
	}
}



