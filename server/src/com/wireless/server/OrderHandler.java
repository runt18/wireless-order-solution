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

import com.wireless.db.foodAssociation.QueryFoodAssociationDao;
import com.wireless.db.foodGroup.CalcFoodGroupDao;
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
import com.wireless.db.promotion.CouponDao;
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
import com.wireless.pack.resp.RespACK;
import com.wireless.pack.resp.RespNAK;
import com.wireless.pack.resp.RespOTAUpdate;
import com.wireless.pack.resp.RespPackage;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.foodGroup.Pager;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberComment;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberComment.CommitBuilder;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Device;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.print.content.ContentParcel;
import com.wireless.print.scheme.JobContentFactory;
import com.wireless.sccon.ServerConnector;
import com.wireless.sms.SMS;
import com.wireless.sms.msg.Msg4Consume;
import com.wireless.sms.msg.Msg4Upgrade;
import com.wireless.util.DateType;
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
			
			final RespPackage response;
			
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
				response = new RespPackage(request.header, StaffDao.getByRestaurant(device.getRestaurantId()), Staff.ST_PARCELABLE_COMPLEX);
				
			}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_BACKUP_SERVER){
				//handle the query backup connectors
				response = new RespPackage(request.header, WirelessSocketServer.backups, 0);
			
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
					response = new RespPackage(request.header, RegionDao.getByStatus(staff, Region.Status.BUSY), Region.REGION_PARCELABLE_COMPLEX);
					
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
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE){
					//handle query table request
					response = new RespPackage(request.header, TableDao.getByCond(staff, null, null), Table.TABLE_PARCELABLE_COMPLEX);
				
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_WX_ORDER){
					//handle the query wx order
					response = doQueryWxOrder(staff, request);
					
				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_ORDER_BY_TBL){
					//handle query order request
					Table tableToQuery = new Parcel(request.body).readParcel(Table.CREATOR);
					response = new RespPackage(request.header, OrderDao.getByTableAlias(staff, tableToQuery.getAliasId()), Order.ORDER_PARCELABLE_4_QUERY);

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.QUERY_TABLE_STATUS){
					//handle query table status
					Table tblToQuery = TableDao.getByAlias(staff, new Parcel(request.body).readParcel(Table.CREATOR).getAliasId());
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

				}else if(request.header.mode == Mode.ORDER_BUSSINESS && request.header.type == Type.TRANSFER_ORDER_FOOD){
					//handle the transfer order food
					response = doTransOrderFood(staff, request);
					
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
	
	private RespPackage doQueryWxOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
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
			return new RespPackage(request.header, result, WxOrder.WX_ORDER_PARCELABLE_COMPLEX);
		}
	}
	
	private RespPackage doInsertOrder(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		//handle insert order request 
		final List<Printer> printers = PrinterDao.getPrinters(staff);
		
		Order orderToInsert = InsertOrder.exec(staff, new Parcel(request.body).readParcel(Order.InsertBuilder.CREATOR));
		
		if(request.header.reserved == PrintOption.DO_PRINT.getVal()){
			PrintHandler printHandler = new PrintHandler(staff);
			printHandler.process(JobContentFactory.instance().createSummaryContent(PType.PRINT_ORDER, 
				 															  staff, 
				 															  printers,
									 										  orderToInsert));
			printHandler.process(JobContentFactory.instance().createDetailContent(PType.PRINT_ORDER_DETAIL, 
																		     staff, 
																		     printers,
																		     orderToInsert));
		}
		
		return new RespACK(request.header);
	}
	
	private RespPackage doInsertOrderForce(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		//handle insert order request force
		Order newOrder = new Parcel(request.body).readParcel(Order.InsertBuilder.CREATOR).build();
		
		Table tblToOrder = TableDao.getByAlias(staff, newOrder.getDestTbl().getAliasId());
		
		if(tblToOrder.isIdle()){
			return doInsertOrder(staff, request);
			
		}else if(tblToOrder.isBusy()){
			Order oriOrder = OrderDao.getByTableAlias(staff, tblToOrder.getAliasId());
			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqInsertOrder(staff, 
																					 new Order.UpdateBuilder(oriOrder.getId(), oriOrder.getOrderDate())
																							  .addAll(oriOrder.getOrderFoods(), staff)
																							  .addAll(newOrder.getOrderFoods(), staff)
																							  .setWxOrders(newOrder.getWxOrders()), 
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
		DiffResult diffResult = UpdateOrder.exec(staff, new Parcel(request.body).readParcel(Order.UpdateBuilder.CREATOR));
		List<Printer> printers = PrinterDao.getPrinters(staff);
		
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
																							  hurriedOrder));
				//print the detail to hurried foods
				printHandler.process(JobContentFactory.instance().createDetailContent(PType.PRINT_HURRIED_FOOD, 
																							 staff,
																							 printers,
																							 hurriedOrder));
			}
			
			if(!diffResult.extraFoods.isEmpty()){
				Order extraOrder = new Order();
				extraOrder.copyFrom(diffResult.newOrder);
				extraOrder.setOrderFoods(diffResult.extraFoods);
				//print the summary to extra foods
				printHandler.process(JobContentFactory.instance().createSummaryContent(PType.PRINT_ALL_EXTRA_FOOD, 
																							  staff,
																							  printers,
																							  extraOrder));
				//print the detail to extra foods
				printHandler.process(JobContentFactory.instance().createDetailContent(PType.PRINT_EXTRA_FOOD_DETAIL, 
																							 staff,
																							 printers,
																							 extraOrder));
			}

			if(!diffResult.cancelledFoods.isEmpty()){
				Order cancelledOrder = new Order();
				cancelledOrder.copyFrom(diffResult.newOrder);
				cancelledOrder.setOrderFoods(diffResult.cancelledFoods);
				//print the summary to canceled foods
				printHandler.process(JobContentFactory.instance().createSummaryContent(PType.PRINT_ALL_CANCELLED_FOOD,
																							  staff,
																							  printers,
																							  cancelledOrder));
				//print the detail to canceled foods
				printHandler.process(JobContentFactory.instance().createDetailContent(PType.PRINT_CANCELLED_FOOD_DETAIL,
																							 staff,
																							 printers,
																							 cancelledOrder));

			}

		}
		return new RespACK(request.header);
	}
	
	private RespPackage doTransOrderFood(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		OrderDao.transfer(staff, new Parcel(request.body).readParcel(Order.TransferBuilder.CREATOR));
		return new RespACK(request.header);
	}
	
	private RespPackage doTransTable(Staff staff, ProtocolPackage request) throws SQLException, BusinessException, IOException{
		Table.TransferBuilder builder = new Parcel(request.body).readParcel(Table.TransferBuilder.CREATOR);
		int orderId = TableDao.transfer(staff, builder);
		ServerConnector.instance().ask(ReqPrintContent.buildReqPrintTransTbl(staff, orderId, builder.getSrcTbl(), builder.getDestTbl()));
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
			ServerConnector.instance().ask(ReqPrintContent.buildReqPrintReceipt(staff, builder.getUpdateBuilder().build().getId()));
			if(builder.getPayBuilder().getSettleType() == Order.SettleType.MEMBER){
				ServerConnector.instance().ask(ReqPrintContent.buildReqPrintMemberReceipt(staff, MemberOperationDao.getByOrder(staff, builder.getUpdateBuilder().build().getId()).getId()));
			}
		}
		return new RespACK(request.header);
	}
	
	private RespPackage doPayOrder(final Staff staff, ProtocolPackage request)  throws SQLException, BusinessException{
		final Order.PayBuilder payBuilder = new Parcel(request.body).readParcel(Order.PayBuilder.CREATOR);
		
		List<Printer> printers = PrinterDao.getPrinters(staff);
		
		PrintHandler printHandler = new PrintHandler(staff);

		/**
		 * If pay order temporary, just only print the temporary receipt.
		 * Otherwise perform the pay action and print receipt 
		 */
		if(request.header.type == Type.PAY_TEMP_ORDER){
			if(payBuilder.getPrintOption() == PrintOption.DO_PRINT){
				printHandler.process(JobContentFactory.instance().createReceiptContent(PType.PRINT_TEMP_RECEIPT, 
																				  staff,
																				  printers,
																				  PayOrder.payTemp(staff, payBuilder)));
			}else{
				PayOrder.payTemp(staff, payBuilder);
			}
			
		}else{
			
			final Order order = PayOrder.pay(staff, payBuilder);
			
			//Perform SMS notification to member coupon dispatch & member upgrade in another thread
			//so that not affect the order payment.
			if(payBuilder.getSettleType() == Order.SettleType.MEMBER){
				WirelessSocketServer.threadPool.execute(new Runnable(){
					@Override
					public void run() {
						try{
							//Perform this coupon draw.
							List<Coupon> coupons = CouponDao.getByCond(staff, new CouponDao.ExtraCond().setMember(payBuilder.getMemberId()).setStatus(Coupon.Status.PUBLISHED)
																									   .addPromotionStatus(Promotion.Status.PROGRESS), null);
							//Check to see whether or not any coupons associated with this member is qualified to take.
							for(Coupon coupon : coupons){
								coupon = CouponDao.getById(staff, coupon.getId());
								if(coupon.getPromotion().getRule() == Promotion.Rule.ONCE || coupon.getPromotion().getRule() == Promotion.Rule.TOTAL){
									if(coupon.getDrawProgress().isOk()){
										CouponDao.draw(staff, coupon.getId());
									}
								}
							}
						}catch(SQLException | BusinessException e){
							e.printStackTrace();
						} 
						
						try{
							//Perform the member upgrade
							Msg4Upgrade msg4Upgrade = MemberLevelDao.upgrade(staff, payBuilder.getMemberId());

							if(payBuilder.isSendSMS()){
								MemberOperation mo = MemberOperationDao.getByOrder(staff, order.getId());
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
			
			printHandler.process(JobContentFactory.instance().createReceiptContent(PType.PRINT_RECEIPT, staff, printers, order));
			
			//Perform to print the member receipt if settled by member.
			if(order.isSettledByMember()){
				printHandler.process(JobContentFactory.instance().createMemberReceiptContent(PType.PRINT_MEMBER_RECEIPT, 
																								staff, 
																								printers,
																								MemberOperationDao.getByOrder(staff, order.getId())));
			}
			
		}
		
		return new RespACK(request.header);
	}
	
	private RespPackage doPrintContent(Staff staff, ProtocolPackage request) throws SQLException, BusinessException{
		PType printType = PType.valueOf(request.header.reserved);
		List<Printer> printers = PrinterDao.getPrinters(staff);
		
		if(printType.isSummary()){
			int orderId = new Parcel(request.body).readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createSummaryContent(printType, staff, printers, orderId));
			
		}else if(printType.isDetail()){
			int orderId = new Parcel(request.body).readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createDetailContent(printType, staff, printers, orderId));
			
		}else if(printType.isReceipt()){
			int orderId = new Parcel(request.body).readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createReceiptContent(printType, staff, printers, orderId));
			
		}else if(printType.isTransTbl()){
			Parcel p = new Parcel(request.body);
			int orderId = p.readInt();
			Table srcTbl = TableDao.getByAlias(staff, p.readParcel(Table.CREATOR).getAliasId());
			Table destTbl = TableDao.getByAlias(staff, p.readParcel(Table.CREATOR).getAliasId());
			new PrintHandler(staff).process(JobContentFactory.instance().createTransContent(printType, staff, printers, orderId, srcTbl, destTbl));
			
		}else if(printType.isShift()){
			Parcel p = new Parcel(request.body);
			long onDuty = p.readLong();
			long offDuty = p.readLong();
			Region.RegionId regionId = Region.RegionId.valueOf(p.readShort());
			if(regionId == Region.RegionId.REGION_NULL){
				List<Region> regions = RegionDao.getByStatus(staff, Region.Status.BUSY);
				if(!regions.isEmpty()){
					regionId = Region.RegionId.valueOf(regions.get(0).getId());
				}
			}
			new PrintHandler(staff).process(JobContentFactory.instance().createShiftContent(printType, staff, printers, new DutyRange(onDuty, offDuty), regionId));
			
		}else if(printType.isMember()){
			int moId = new Parcel(request.body).readInt();
			new PrintHandler(staff).process(JobContentFactory.instance().createMemberReceiptContent(printType, staff, printers, MemberOperationDao.getById(staff, DateType.TODAY, moId)));
		}
		
		return new RespACK(request.header);
	}
}



