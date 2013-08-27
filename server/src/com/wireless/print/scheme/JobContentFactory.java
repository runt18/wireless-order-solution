package com.wireless.print.scheme;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberType.Attribute;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.print.PFormat;
import com.wireless.print.content.Content;
import com.wireless.print.content.ContentCombinator;
import com.wireless.print.content.MemberReceiptContent;
import com.wireless.print.content.OrderDetailContent;
import com.wireless.print.content.ReceiptContent;
import com.wireless.print.content.ShiftContent;
import com.wireless.print.content.SummaryContent;
import com.wireless.print.content.TransTableContent;
import com.wireless.util.DateType;

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
	
	public Content createSummaryContent(PType printType, Staff term, List<Printer> printers, int orderId) throws SQLException, BusinessException{
		return createSummaryContent(printType, term, printers, OrderDao.getById(term, orderId, DateType.TODAY));
	}
	
	public Content createSummaryContent(PType printType, Staff term, List<Printer> printers, Order order) throws SQLException{
		if(order.hasOrderFood() && !printers.isEmpty()){
			
			final List<JobContent> jobContents = new ArrayList<JobContent>();
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(printType) && func.isRegionMatched(order.getRegion())){
						if(func.isDeptAll()){
							//Generate the the summary to all departments.
							jobContents.add(new JobContent(printer, func.getRepeat(), printType, 
										   				   new SummaryContent(new Department(null, Department.DEPT_ALL, term.getRestaurantId(), Department.Type.RESERVED), 
										   						   			  PFormat.RECEIPT_FORMAT_DEF, 
										   						   			  order,
										   						   			  term.getName(),
										   						   			  printType, 
										   						   			  printer.getStyle())));
						}else{
							//Generate the summary to specific department.
							for(Department dept : func.getDepartment()){
								
								List<OrderFood> orderFoods = new ArrayList<OrderFood>();
								for(OrderFood of : order.getOrderFoods()){
									if(of.asFood().getKitchen().getDept().equals(dept)){
										orderFoods.add(of);
									}
								}
								
								if(!orderFoods.isEmpty()){
									Order orderToDept = new Order();
									orderToDept.copyFrom(order);
									orderToDept.setOrderFoods(orderFoods);
									jobContents.add(new JobContent(printer, func.getRepeat(), printType,
								   				   new SummaryContent(dept, 
								   						   			  PFormat.RECEIPT_FORMAT_DEF, 
								   						   			  orderToDept,
								   						   			  term.getName(),
								   						   			  printType, 
								   						   			  printer.getStyle())));
									
								}
							}
						}
						
					}
				}
			}
			
			return new JobCombinationContent(jobContents);
			
		}else{
			return null;
		}
	}
	
	public Content createDetailContent(PType printType, Staff term, List<Printer> printers, Order order) throws BusinessException, SQLException{
		List<JobContent> jobContents = new ArrayList<JobContent>();
		
		if(order.hasOrderFood() && !printers.isEmpty()){
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(printType)){
						for(OrderFood of : order.getOrderFoods()){
							if(of.asFood().isCombo()){
								for(Food childFood : of.asFood().getChildFoods()){
									if(func.isKitchenAll()){
										//Add the detail content of this child order food to the job contents.
										jobContents.add(new JobContent(printer, func.getRepeat(), printType,
																	   new OrderDetailContent(of, 
																			   				  childFood, 
																			   				  order, 
																			   				  term.getName(), 
																			   				  printType, 
																			   				  printer.getStyle())));
									}else{
										for(Kitchen kitchen : func.getKitchens()){
											if(kitchen.equals(childFood.getKitchen())){
												//Add the detail content of this child order food matched the kitchen to the job contents.
												jobContents.add(new JobContent(printer, func.getRepeat(), printType,
																			   new OrderDetailContent(of, 
																					   				  childFood, 
																					   				  order, 
																					   				  term.getName(), 
																					   				  printType, 
																					   				  printer.getStyle())));
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
																		   				  term.getName(), 
																		   				  printType, 
																		   				  printer.getStyle())));
								}else{
									for(Kitchen kitchen : func.getKitchens()){
										if(of.getKitchen().equals(kitchen)){
											//Add the detail content of this order food matched the kitchen to the job contents.
											jobContents.add(new JobContent(printer, func.getRepeat(), printType,
																		   new OrderDetailContent(of, 
																				   				  order, 
																				   				  term.getName(), 
																				   				  printType, 
																				   				  printer.getStyle())));
											break;
										}
									}
								}

							}
						}
					}
				}
			}
			
			return new JobCombinationContent(jobContents);
			
		}else{
			return null;
		}
	}
	
	public Content createDetailContent(PType printType, Staff term, List<Printer> printers, int orderId) throws BusinessException, SQLException{
		return createDetailContent(printType, term, printers, OrderDao.getById(term, orderId, DateType.TODAY));
	}

	/**
	 * Create the receipt content
	 * @param printType
	 * @param term
	 * @param printers
	 * @param order
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public Content createReceiptContent(PType printType, Staff term, List<Printer> printers, Order order) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Restaurant restaurant = RestaurantDao.getById(dbCon, term.getRestaurantId());
			int receiptStyle = SystemDao.getSetting(dbCon, term.getRestaurantId()).getReceiptStyle();
			
			List<JobContent> jobContents = new ArrayList<JobContent>();
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.isTypeMatched(printType) && func.isRegionMatched(order.getRegion())){
						jobContents.add(new JobContent(printer, func.getRepeat(), printType,
														new ReceiptContent(receiptStyle,
															  			   restaurant, 
															  			   order,
															  			   term.getName(),
															  			   printType, 
															  			   printer.getStyle())));
					}
				}
			}
			
			return new JobCombinationContent(jobContents);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	public Content createReceiptContent(PType printType, Staff term, List<Printer> printers, int orderId) throws BusinessException, SQLException{
		return createReceiptContent(printType, term, printers, OrderDao.getById(term, orderId, DateType.TODAY));
	}
	
	public Content createShiftContent(PType printType, Staff term, List<Printer> printers, long onDuty, long offDuty) throws SQLException{
		
		List<JobContent> jobContents = new ArrayList<JobContent>();
		
		Region regionToCompare = new Region(Region.REGION_1, "", term.getRestaurantId());
		
		for(Printer printer : printers){
			for(PrintFunc func : printer.getPrintFuncs()){
				if(func.isTypeMatched(printType) && func.isRegionMatched(regionToCompare)){
					ShiftDetail shiftDetail;
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					if(printType == PType.PRINT_DAILY_SETTLE_RECEIPT || 
					   printType == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT ||
					   printType == PType.PRINT_HISTORY_SHIFT_RECEIPT){
						/*
						 * Get the details to daily settlement from history ,
						 * since records to today has been moved to history before printing daily settlement receipt. 
						 */
						shiftDetail = QueryShiftDao.exec(term, sdf.format(onDuty), sdf.format(offDuty), QueryShiftDao.QUERY_HISTORY);
						
					}else{
						shiftDetail = QueryShiftDao.exec(term, sdf.format(onDuty), sdf.format(offDuty), QueryShiftDao.QUERY_TODAY);
					}
					jobContents.add(new JobContent(printer, func.getRepeat(), printType,
												   new ShiftContent(shiftDetail, 
														   		    term.getName(),
														   		    printType,
														   		    printer.getStyle())));
				}
			}
		}
		
		return new JobCombinationContent(jobContents);
	}
	
	public Content createMemberReceiptContent(PType printType, Staff term, List<Printer> printers, int memberOperationId) throws BusinessException, SQLException{
		
		List<JobContent> jobContents = new ArrayList<JobContent>();
		
		Region regionToCompare = new Region(Region.REGION_1, "", term.getRestaurantId());
		
		for(Printer printer : printers){
			for(PrintFunc func : printer.getPrintFuncs()){
				if(func.isTypeMatched(printType) && func.isRegionMatched(regionToCompare)){
					
					MemberOperation mo = MemberOperationDao.getTodayById(memberOperationId);
					
					if(mo != null){
						Member member = MemberDao.getMemberById(mo.getMemberId());
						//Print the member receipt only if member type belongs to charge.
						if(member.getMemberType().getAttribute() == Attribute.CHARGE){
							
							mo.setMember(MemberDao.getMemberById(mo.getMemberId()));
							Restaurant restaurant = RestaurantDao.getById(term.getRestaurantId());
							jobContents.add(new JobContent(printer, func.getRepeat(), printType,
														   new MemberReceiptContent(restaurant, term.getName(), mo, printType, printer.getStyle())));

						}
					}					
				}
			}
		}
		
		return new JobCombinationContent(jobContents);
	}
	
	public Content createTransContent(PType printType, Staff term, List<Printer> printers, int orderId, Table srcTbl, Table destTbl){
		List<JobContent> jobContents = new ArrayList<JobContent>();
		
		Region regionToCompare = new Region(Region.REGION_1, "", term.getRestaurantId());
		
		for(Printer printer : printers){
			for(PrintFunc func : printer.getPrintFuncs()){
				if(func.isTypeMatched(printType) && func.isRegionMatched(regionToCompare)){
					if(!srcTbl.equals(destTbl)){
						jobContents.add(new JobContent(printer, func.getRepeat(), printType,
														new TransTableContent(orderId,
																   srcTbl,
																   destTbl,
																   term.getName(),
																   printType,
																   printer.getStyle())));
					}
				}
			}
		}

		return new JobCombinationContent(jobContents);
	}
}
