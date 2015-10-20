package com.wireless.print.scheme;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.billStatistics.CalcBillStatisticsDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.PaymentDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.system.SystemDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.ShiftDetail;
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
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.print.content.Content;
import com.wireless.print.content.ContentCombinator;
import com.wireless.print.content.concrete.FoodDetailContent;
import com.wireless.print.content.concrete.MemberReceiptContent;
import com.wireless.print.content.concrete.OrderDetailContent;
import com.wireless.print.content.concrete.ReceiptContent;
import com.wireless.print.content.concrete.ShiftContent;
import com.wireless.print.content.concrete.SummaryContent;
import com.wireless.print.content.concrete.TransFoodContent;
import com.wireless.print.content.concrete.TransTableContent;

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
										   						   			  printer.getStyle(), detailType).setEnding(func.getComment())));
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
							   						   			  printer.getStyle(), detailType).setEnding(func.getComment())));
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
	
	public Content createDetailContent(PType printType, Staff staff, List<Printer> printers, Order order, FoodDetailContent.DetailType detailType) throws BusinessException, SQLException{
		final List<JobContent> jobContents = new ArrayList<JobContent>();
		
		if(order.hasOrderFood() && !printers.isEmpty()){
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(printType)){
						for(OrderFood of : order.getOrderFoods()){
							if(of.asFood().isCombo()){
								for(ComboFood childFood : of.asFood().getChildFoods()){
									if(func.isKitchenAll()){
										//Add the detail content of this child order food to the job contents.
										jobContents.add(new JobContent(printer, func.getRepeat(), printType,
																	   new OrderDetailContent(of, 
																			   				  childFood, 
																			   				  order, 
																			   				  staff.getName(), 
																			   				  printType, 
																			   				  printer.getStyle(), detailType)));
									}else{
										for(Kitchen kitchen : func.getKitchens()){
											if(kitchen.equals(childFood.asFood().getKitchen())){
												//Add the detail content of this child order food matched the kitchen to the job contents.
												jobContents.add(new JobContent(printer, func.getRepeat(), printType,
																			   new OrderDetailContent(of, 
																					   				  childFood, 
																					   				  order, 
																					   				  staff.getName(), 
																					   				  printType, 
																					   				  printer.getStyle(), detailType)));
												break;
											}
										}
									}
								}
							}else{
								if(func.isKitchenAll()){
									//Add the detail content of this order food to the job contents.
									jobContents.add(new JobContent(printer, func.getRepeat(), printType,
																   new OrderDetailContent(of, 
																		   				  order, 
																		   				  staff.getName(), 
																		   				  printType, 
																		   				  printer.getStyle(), detailType)));
								}else{
									for(Kitchen kitchen : func.getKitchens()){
										if(of.getKitchen().equals(kitchen)){
											//Add the detail content of this order food matched the kitchen to the job contents.
											jobContents.add(new JobContent(printer, func.getRepeat(), printType,
																		   new OrderDetailContent(of, 
																				   				  order, 
																				   				  staff.getName(), 
																				   				  printType, 
																				   				  printer.getStyle(), detailType)));
											break;
										}
									}
								}

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
	
	public Content createDetailContent(PType printType, Staff staff, List<Printer> printers, int orderId, FoodDetailContent.DetailType detailType) throws BusinessException, SQLException{
		return createDetailContent(printType, staff, printers, OrderDao.getById(staff, orderId, DateType.TODAY), detailType);
	}

	/**
	 * Create the receipt content
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
			Restaurant restaurant = RestaurantDao.getById(dbCon, staff.getRestaurantId());
			WxRestaurant wxRestaurant = WxRestaurantDao.get(dbCon, staff);
			final int receiptStyle = SystemDao.getSetting(dbCon, staff.getRestaurantId()).getReceiptStyle();
			
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
																					  .setWxPayUrl(wxPayUrl);
						jobContents.add(new JobContent(printer, func.getRepeat(), printType, content));
					}
				}
			}
			
			return jobContents.isEmpty() ? null : new JobCombinationContent(jobContents);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	public Content createReceiptContent(PType printType, Staff staff, List<Printer> printers, Order order) throws BusinessException, SQLException{
		return createReceiptContent(printType, staff, printers, order, null);
	}
	
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
	 */
	public Content createShiftContent(PType printType, Staff staff, List<Printer> printers, DutyRange range, Region.RegionId regionId) throws SQLException{
		
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
						shiftDetail = ShiftDao.getByRange(staff, range, new CalcBillStatisticsDao.ExtraCond(DateType.HISTORY));
						
					}else{
						shiftDetail = ShiftDao.getByRange(staff, range, new CalcBillStatisticsDao.ExtraCond(DateType.TODAY));
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
