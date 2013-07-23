package com.wireless.print.scheme;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.print.PFormat;
import com.wireless.print.content.Content;
import com.wireless.print.content.ContentCombinator;
import com.wireless.print.content.SummaryContent;
import com.wireless.protocol.Terminal;

public class JobContentFactory {

	private final static JobContentFactory mInstance = new JobContentFactory();
	
	private JobContentFactory(){
		
	}
	
	public static JobContentFactory instance(){
		return mInstance;
	}
	
	public Content createSummaryContent(PType printType, Terminal term, List<Printer> printers, Order order) throws SQLException{
		if(order.hasOrderFood() && !printers.isEmpty()){
			
			final List<Content> jobContents = new ArrayList<Content>();
			
			for(Printer printer : printers){
				for(PrintFunc func : printer.getPrintFuncs()){
					if(func.getType().isSummary()){
						if(func.isDeptAll()){
							//Generate the the summary to all departments.
							jobContents.add(new JobContent(printer, printType,
										   				   new SummaryContent(new Department(null, Department.DEPT_ALL, term.restaurantID, Department.Type.RESERVED), 
										   						   			  PFormat.RECEIPT_FORMAT_DEF, 
										   						   			  order,
										   						   			  term.owner,
										   						   			  printType, 
										   						   			  printer.getStyle())));
						}else{
							//Generate the summary to specific department.
							List<OrderFood> orderFoods = new ArrayList<OrderFood>();
							for(OrderFood of : order.getOrderFoods()){
								if(of.asFood().getKitchen().getDept().equals(func.getDepartment())){
									orderFoods.add(of);
								}
							}
							order.setOrderFoods(orderFoods);
							jobContents.add(new JobContent(printer, printType,
					   				   new SummaryContent(func.getDepartment(), 
					   						   			  PFormat.RECEIPT_FORMAT_DEF, 
					   						   			  order,
					   						   			  term.owner,
					   						   			  printType, 
					   						   			  printer.getStyle())));
						}
						
					}
				}
			}
			
			//Add the job amount to the head of contents.
			jobContents.add(0, new Content(){

				@Override
				public byte[] toBytes() {
					byte[] bytesToJobAmount = new byte[2];
					bytesToJobAmount[0] = (byte)(jobContents.size() & 0x000000FF);
					bytesToJobAmount[1] = (byte)((jobContents.size() & 0x0000FF00) >> 8);
					return bytesToJobAmount;
				}
				
			});
			
			return new ContentCombinator(jobContents);
			
		}else{
			return null;
		}
	}
}
