package com.wireless.print.scheme;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.billStatistics.CalcCouponStatisticsDao;
import com.wireless.db.book.BookDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.PaymentDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.system.SystemDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.CouponUsage;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.book.Book;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberType.Attribute;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.print.content.Content;
import com.wireless.print.content.ContentCombinator;
import com.wireless.print.content.concrete.BookContent;
import com.wireless.print.content.concrete.FoodDetailContent;
import com.wireless.print.content.concrete.MemberReceiptContent;
import com.wireless.print.content.concrete.OrderDetailContent;
import com.wireless.print.content.concrete.ReceiptContent;
import com.wireless.print.content.concrete.SecondDisplayContent;
import com.wireless.print.content.concrete.ShiftContent;
import com.wireless.print.content.concrete.SummaryContent;
import com.wireless.print.content.concrete.TransFoodContent;
import com.wireless.print.content.concrete.TransTableContent;
import com.wireless.print.content.concrete.WxCallPayContent;
import com.wireless.print.content.concrete.WxOrderContent;
import com.wireless.print.content.concrete.WxWaiterContent;

public class JobContentFactory {

	private static class JobCombinationContent implements Content{

		private final ContentCombinator mCombinator = new ContentCombinator();
		
		JobCombinationContent(List<JobContent> jobContents){
			
			final int jobAmount = jobContents.size();
			
			//Add the job amount to the head of contents.
			mCombinator.append(new Content(){

				@Override
				public byte[] toBytes() {
					byte[] bytesToJobAmount = new byte[2];
					bytesToJobAmount[0] = (byte)(jobAmount & 0x000000FF);
					bytesToJobAmount[1] = (byte)((jobAmount & 0x0000FF00) >> 8);
					return bytesToJobAmount;
				}

			});
			
			//Append each job contents
			mCombinator.append(jobContents);
		}
		
		@Override
		public byte[] toBytes() {
			return mCombinator.toBytes();
		}
	}
	
	private final static JobContentFactory mInstance = new JobContentFactory();
	
	private JobContentFactory(){
		
	}
	
	public static JobContentFactory instance(){
		return mInstance;
	}
	
	public Content createSummaryContent(PType printType, Staff staff, List<Printer> printers, int orderId, FoodDetailContent.DetailType detailType) throws SQLException, BusinessException{
		return createSummaryContent(printType, staff, printers, OrderDao.getById(staff, orderId, DateType.TODAY), detailType);
	}
	
	public Content createSummaryContent(PType printType, Staff staff, List<Printer> printers, Order order, FoodDetailContent.DetailType detailType) throws SQLException{
		if(order.hasOrderFood() && !printers.isEmpty()){
			
			final List<JobContent> jobContents = new ArrayList<JobContent>();
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(printType) && func.isRegionMatched(order.getRegion())){
						if(func.isDeptAll()){
							//Generate the the summary to all departments.
							jobContents.add(new JobContent(printer, func.getRepeat(), printType, 
										   				   new SummaryContent(order,
										   						   			  staff.getName(),
										   						   			  printType, 
										   						   			  printer.getStyle(), detailType).setEnding(func.getComment())
										   				   												     .setOptions(func.getSummaryOptions())));
						}else{
							//Generate the summary to specific departments.
							final List<OrderFood> ofToDept = new ArrayList<OrderFood>();
							for(OrderFood of : order.getOrderFoods()){
								if(func.getDepartment().contains(of.asFood().getKitchen().getDept())){
									ofToDept.add(of);
								}
							}
							
							if(!ofToDept.isEmpty()){
								Order orderToDept = new Order(0);
								orderToDept.copyFrom(order);
								orderToDept.setOrderFoods(ofToDept);
								jobContents.add(new JobContent(printer, func.getRepeat(), printType,
							   				   new SummaryContent(func.getDepartment(), 
							   						   			  orderToDept,
							   						   			  staff.getName(),
							   						   			  printType, 
							   						   			  printer.getStyle(), detailType).setEnding(func.getComment())
							   				   													 .setOptions(func.getSummaryOptions())));
							}
						}
						
					}
				}
			}
			
			return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
			
		}else{
			return null;
		}
	}
	
	private JobContent createDetail(PType printType, Staff staff, Printer printer, PrintFunc func, Order order, FoodDetailContent.DetailType detailType, OrderFood of){
		if(of.asFood().isCombo()){
			for(ComboFood childFood : of.asFood().getChildFoods()){
				if(func.isKitchenAll()){
					//Add the detail content of this child order food to the job contents.
					return (new JobContent(printer, func.getRepeat(), printType,
												   new OrderDetailContent(of, 
														   				  childFood, 
														   				  order, 
														   				  staff.getName(), 
														   				  printType, 
														   				  printer.getStyle(), detailType)));
				}else{
					for(Kitchen kitchen : func.getKitchens()){
						final int printKitchenId;
						if(childFood.asFood().hasPrintKitchen()){
							printKitchenId = childFood.asFood().getPrintKitchenId();
						}else{
							printKitchenId = childFood.asFood().getKitchen().getId();
						}
						if(kitchen.getId() == printKitchenId){
							//Add the detail content of this child order food matched the kitchen to the job contents.
							return (new JobContent(printer, func.getRepeat(), printType,
														   new OrderDetailContent(of, 
																   				  childFood, 
																   				  order, 
																   				  staff.getName(), 
																   				  printType, 
																   				  printer.getStyle(), detailType)));
						}
					}
				}
			}
		}else{
			if(func.isKitchenAll()){
				//Add the detail content of this order food to the job contents.
				return (new JobContent(printer, func.getRepeat(), printType,
											   new OrderDetailContent(of, 
													   				  order, 
													   				  staff.getName(), 
													   				  printType, 
													   				  printer.getStyle(), detailType)));
			}else{
				for(Kitchen kitchen : func.getKitchens()){
					final int printKitchenId;
					if(of.asFood().hasPrintKitchen()){
						printKitchenId = of.asFood().getPrintKitchenId();
					}else{
						printKitchenId = of.asFood().getKitchen().getId();
					}
					if(kitchen.getId() == printKitchenId){
						//Add the detail content of this order food matched the kitchen to the job contents.
						return (new JobContent(printer, func.getRepeat(), printType,
													   new OrderDetailContent(of, 
															   				  order, 
															   				  staff.getName(), 
															   				  printType, 
															   				  printer.getStyle(), detailType)));
					}
				}
			}

		}
		
		return null;
	}
	
	/**
	 * 生成点餐分单的内容
	 * @param printType
	 * @param staff
	 * @param printers
	 * @param order
	 * @param detailType
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public Content createDetailContent(PType printType, Staff staff, List<Printer> printers, Order order, FoodDetailContent.DetailType detailType) throws BusinessException, SQLException{
		final List<JobContent> jobContents = new ArrayList<JobContent>();
		
		if(order.hasOrderFood() && !printers.isEmpty()){
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(printType) && func.isRegionMatched(order.getRegion())){
						for(OrderFood of : order.getOrderFoods()){
							JobContent content = null;
							//加菜并且设置了数量不累加时, 菜品分开打印
							if(of.asFood().isSplit()){
								final int amount;
								final float count;
								//delta不为0时表示是加菜或者退菜，为0时表示是补打
								if(of.getDelta() != 0){
									count = Math.abs(of.getDelta());
									amount = Float.valueOf(count).intValue();
								}else{
									count = Math.abs(of.getCount());
									amount = Float.valueOf(count).intValue();
								}
								//只有是小数时才分开打印
								if(count % amount == 0){
									OrderFood single = (OrderFood)of.clone();
									for(int i = 0; i < amount; i++){
										single.setCount(0);
										single.addCount(1);
										content = createDetail(printType, staff, printer, func, order, detailType, single);
									}
								}else{
									content = createDetail(printType, staff, printer, func, order, detailType, of);
								}
							}else{
								content = createDetail(printType, staff, printer, func, order, detailType, of);
							}
							if(content != null){
								jobContents.add(content);
							}
						}
					}
				}
			}
			
			return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
			
		}else{
			return null;
		}
	}
	
	/**
	 * 生成点菜分单
	 * @param printType
	 * @param staff
	 * @param printers
	 * @param orderId
	 * @param detailType
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public Content createDetailContent(PType printType, Staff staff, List<Printer> printers, int orderId, FoodDetailContent.DetailType detailType) throws BusinessException, SQLException{
		return createDetailContent(printType, staff, printers, OrderDao.getById(staff, orderId, DateType.TODAY), detailType);
	}

	/**
	 * 生成客显内容
	 * @param staff
	 * @param printers
	 * @param display
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public Content create2ndDisplayContent(Staff staff, List<Printer> printers, float display) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			final List<JobContent> jobContents = new ArrayList<JobContent>();
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(PType.PRINT_2ND_DISPLAY)){
						jobContents.add(new JobContent(printer, func.getRepeat(), PType.PRINT_2ND_DISPLAY, new SecondDisplayContent(display)));
					}
				}
			}
			
			return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	public Content createWxOrderContent(Staff staff, List<Printer> printers, int wxOrderId) throws SQLException, BusinessException{
		final List<JobContent> jobContents = new ArrayList<JobContent>();
		final WxOrder wxOrder = WxOrderDao.getById(staff, wxOrderId);
		for(Printer printer : printers){
			for(PrintFunc func : printer.getPrintFuncs()){
				if(func.isTypeMatched(PType.PRINT_WX_ORDER)){
					if(wxOrder.hasTable() && !func.isRegionMatched(wxOrder.getTable().getRegion())){
						continue;
					}
					jobContents.add(new JobContent(printer, func.getRepeat(), PType.PRINT_WX_ORDER, new WxOrderContent(wxOrder, printer.getStyle())));
				}
			}
		}
		
		return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
	}

	/**
	 * 生成微信小二
	 * @param staff
	 * @param printers
	 * @param bookId
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public Content createWxWaiterContent(Staff staff, List<Printer> printers, int orderId, String qrCodeContent) throws SQLException, BusinessException{
		final List<JobContent> jobContents = new ArrayList<JobContent>();
		final Order order = OrderDao.getById(staff, orderId, DateType.TODAY);
		
		for(Printer printer : printers){
			for(PrintFunc func : printer.getPrintFuncs()){
				if(func.isTypeMatched(PType.PRINT_WX_WAITER) && func.isRegionMatched(order.getRegion())){
					final Restaurant restaurant = RestaurantDao.getById(staff.getRestaurantId());
					jobContents.add(new JobContent(printer, func.getRepeat(), PType.PRINT_WX_WAITER, new WxWaiterContent(printer.getStyle(), order, restaurant, qrCodeContent)));
				}
			}
		}
		
		return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
	}
	
	/**
	 * 生成微信呼叫结账单
	 * @param staff
	 * @param printers
	 * @param orderId
	 * @param memberId
	 * @param payType
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public Content createWxCallPayContent(Staff staff, List<Printer> printers, int orderId, int memberId, String payType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			final List<JobContent> jobContents = new ArrayList<JobContent>();
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(PType.PRINT_WX_CALL_PAY)){
						final Order order = OrderDao.getById(staff, orderId, DateType.TODAY);
						final Member member = MemberDao.getById(staff, memberId);
						jobContents.add(new JobContent(printer, func.getRepeat(), PType.PRINT_WX_CALL_PAY, new WxCallPayContent(printer.getStyle(), order, member, payType)));
					}
				}
			}
			
			return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 生成微信预订
	 * @param staff
	 * @param printers
	 * @param bookId
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public Content createBookContent(Staff staff, List<Printer> printers, int bookId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			final List<JobContent> jobContents = new ArrayList<JobContent>();
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(PType.PRINT_BOOK)){
						Book book = BookDao.getById(dbCon, staff, bookId);
						jobContents.add(new JobContent(printer, func.getRepeat(), PType.PRINT_BOOK, new BookContent(book, printer.getStyle())));
					}
				}
			}
			
			return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 生成结账单
	 * @param printType
	 * @param staff
	 * @param printers
	 * @param order
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public Content createReceiptContent(PType printType, Staff staff, List<Printer> printers, Order order, String wxPayUrl) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			final Restaurant restaurant = RestaurantDao.getById(dbCon, staff.getRestaurantId());
			final WxRestaurant wxRestaurant = WxRestaurantDao.get(dbCon, staff);
			final CouponUsage couponUsage = CalcCouponStatisticsDao.calcUsage(dbCon, staff, new CalcCouponStatisticsDao.ExtraCond().setAssociateId(order.getId()));
			final int receiptStyle = SystemDao.getByCond(dbCon, staff, null).get(0).getSetting().getReceiptStyle();
			
			final List<JobContent> jobContents = new ArrayList<JobContent>();
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(printType) && func.isRegionMatched(order.getRegion())){
						final Member member;
						if(order.hasMember()){
							member = MemberDao.getById(dbCon, staff, order.getMemberId());
						}else{
							member = null;
						}
						ReceiptContent content = new ReceiptContent(receiptStyle,
													  			   restaurant, 
													  			   wxRestaurant,
													  			   order,
													  			   staff.getName(),
													  			   printType, 
													  			   printer.getStyle()).setEnding(func.getComment())
																					  .setMember(member)
																					  .setWxPayUrl(wxPayUrl)
																					  .setCouponUsage(couponUsage);
						jobContents.add(new JobContent(printer, func.getRepeat(), printType, content));
					}
				}
			}
			
			return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 生成结账单
	 * @param printType
	 * @param staff
	 * @param printers
	 * @param order
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public Content createReceiptContent(PType printType, Staff staff, List<Printer> printers, Order order) throws BusinessException, SQLException{
		return createReceiptContent(printType, staff, printers, order, null);
	}
	
	/**
	 * 生成结账单
	 * @param printType
	 * @param staff
	 * @param printers
	 * @param orderId
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public Content createReceiptContent(PType printType, Staff staff, List<Printer> printers, int orderId) throws BusinessException, SQLException{
		return createReceiptContent(printType, staff, printers, OrderDao.getById(staff, orderId, DateType.TODAY), null);
	}
	
	/**
	 * Create the shift content
	 * @param printType
	 * 			the print type {@link PType}
	 * @param staff
	 * 			the staff to perform this action
	 * @param printers
	 * 			the printers
	 * @param range
	 * 			the range to shift
	 * @param regionId
	 * @return the shift content
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public Content createShiftContent(PType printType, Staff staff, List<Printer> printers, DutyRange range, Region.RegionId regionId) throws SQLException, BusinessException{
		
		List<JobContent> jobContents = new ArrayList<JobContent>();
		
		Region regionToCompare = new Region(regionId.getId(), null, staff.getRestaurantId());
		
		for(Printer printer : printers){
			for(PrintFunc func : printer.getPrintFuncs()){
				if(func.isTypeMatched(printType) && func.isRegionMatched(regionToCompare)){
					ShiftDetail shiftDetail;
					if(printType == PType.PRINT_PAYMENT_RECEIPT){
						shiftDetail = PaymentDao.getDetail(staff, range, new PaymentDao.ExtraCond(DateType.TODAY).setStaffId(staff.getId()));
						
					}else if(printType == PType.PRINT_HISTORY_PAYMENT_RECEIPT){
						shiftDetail = PaymentDao.getDetail(staff, range, new PaymentDao.ExtraCond(DateType.HISTORY).setStaffId(staff.getId()));
						
					}else if(printType == PType.PRINT_DAILY_SETTLE_RECEIPT || 
					   printType == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT ||
					   printType == PType.PRINT_HISTORY_SHIFT_RECEIPT){
						 //Get the details to daily settlement from history ,
						 //since records to today has been moved to history before printing daily settlement receipt. 
						shiftDetail = ShiftDao.getByRange(staff, new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY).setDutyRange(range));
						
					}else{
						shiftDetail = ShiftDao.getByRange(staff, new CalcBillStatisticsDao.ExtraCond(DateType.TODAY).setDutyRange(range));
					}
					jobContents.add(new JobContent(printer, func.getRepeat(), printType,
												   new ShiftContent(shiftDetail, 
														   		    staff.getName(),
														   		    printType,
														   		    printer.getStyle())));
				}
			}
		}
		
		return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
	}
	
	/**
	 * 生成会员对账单
	 * @param printType
	 * @param staff
	 * @param printers
	 * @param mo
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public Content createMemberReceiptContent(PType printType, Staff staff, List<Printer> printers, MemberOperation mo) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			List<JobContent> jobContents = new ArrayList<JobContent>();
			
			Region regionToCompare = new Region(Region.RegionId.REGION_1.getId(), "", staff.getRestaurantId());
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(printType) && func.isRegionMatched(regionToCompare)){
						
						Member member = MemberDao.getById(dbCon, staff, mo.getMemberId());
						//Print the member receipt only if member type belongs to charge.
						if(member.getMemberType().getAttribute() == Attribute.CHARGE){
							
							mo.setMember(MemberDao.getById(dbCon, staff, mo.getMemberId()));
							Restaurant restaurant = RestaurantDao.getById(dbCon, staff.getRestaurantId());
							jobContents.add(new JobContent(printer, func.getRepeat(), printType,
														   new MemberReceiptContent(restaurant, staff.getName(), mo, 
																   					mo.getOrderId() != 0 ? OrderDao.getById(dbCon, staff, mo.getOrderId(), DateType.TODAY) : null, 
																   					printType, printer.getStyle())));
	
						}					
					}
				}
			}
				
			return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 生成转台单
	 * @param printType
	 * @param staff
	 * @param printers
	 * @param orderId
	 * @param srcTbl
	 * @param destTbl
	 * @return
	 */
	public Content createTransContent(PType printType, Staff staff, List<Printer> printers, int orderId, Table srcTbl, Table destTbl){
		List<JobContent> jobContents = new ArrayList<JobContent>();
		
		Region regionToCompare = new Region(Region.RegionId.REGION_1.getId(), "", staff.getRestaurantId());
		
		for(Printer printer : printers){
			for(PrintFunc func : printer.getPrintFuncs()){
				if(func.isTypeMatched(printType) && func.isRegionMatched(regionToCompare)){
					if(!srcTbl.equals(destTbl)){
						jobContents.add(new JobContent(printer, func.getRepeat(), printType,
														new TransTableContent(orderId,
																   srcTbl,
																   destTbl,
																   staff.getName(),
																   printType,
																   printer.getStyle())));
					}
				}
			}
		}

		return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
	}
	
	/**
	 * 生成转菜单
	 * @param printType
	 * @param staff
	 * @param printers
	 * @param orderId
	 * @param srcTbl
	 * @param destTbl
	 * @param transferFoods
	 * @return
	 */
	public Content createTransFoodContent(PType printType, Staff staff, List<Printer> printers, int orderId, Table srcTbl, Table destTbl, List<OrderFood> transferFoods){
		List<JobContent> jobContents = new ArrayList<JobContent>();
		
		Region regionToCompare = new Region(Region.RegionId.REGION_1.getId(), "", staff.getRestaurantId());
		
		for(Printer printer : printers){
			for(PrintFunc func : printer.getPrintFuncs()){
				if(func.isTypeMatched(printType) && func.isRegionMatched(regionToCompare)){
					if(!srcTbl.equals(destTbl)){
						jobContents.add(new JobContent(printer, func.getRepeat(), printType,
														new TransFoodContent(transferFoods,
																   orderId,
																   destTbl,
																   srcTbl,
																   staff.getName(),
																   printType,
																   printer.getStyle())));
					}
				}
			}
		}

		return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
	}
	
}
