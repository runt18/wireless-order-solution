package com.wireless.print.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.wireless.print.PFormat;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.content.ContentCombinator;
import com.wireless.print.content.SummaryContent;
import com.wireless.protocol.PDepartment;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Terminal;

public class SummaryTypeContent extends TypeContent {

	private final int mOrderId;
	private final short mRegionId;
	
	private final ContentCombinator m58;
	
	private final ContentCombinator m80;
	
	SummaryTypeContent(PType printType, Terminal term, Order order, PDepartment[] depts) {
		super(printType);
		
		if(!printType.isSummary()){
			throw new IllegalArgumentException("The print type(" + printType + ") is invalid");
		}
		
		Order summaryOrder = new Order();
		summaryOrder.copyFrom(order);
		
		this.mRegionId = summaryOrder.getRegion().getRegionId();
		if(summaryOrder.hasChildOrder()){
			this.mOrderId = summaryOrder.getChildOrder()[0].getRegion().getRegionId();
		}else{
			this.mOrderId = summaryOrder.getId();
		}
		
		HashMap<PDepartment, List<OrderFood>> foodsByDept = new HashMap<PDepartment, List<OrderFood>>();
		
		//Group order foods by department.
		for(OrderFood orderFood : summaryOrder.getOrderFoods()){
			List<OrderFood> foods = foodsByDept.get(orderFood.getKitchen().getDept());
			if(foods != null){
				foods.add(orderFood);
				foodsByDept.put(orderFood.getKitchen().getDept(), foods);
			}else{
				foods = new ArrayList<OrderFood>();
				foods.add(orderFood);
				foodsByDept.put(orderFood.getKitchen().getDept(), foods);
			}
		}
		//Add a record with all order foods.
		foodsByDept.put(new PDepartment(null, PDepartment.DEPT_ALL, term.restaurantID, PDepartment.TYPE_RESERVED), 
					    Arrays.asList(summaryOrder.getOrderFoods()));					
		
		m58 = new ContentCombinator();
		m80 = new ContentCombinator();
		
		for(final Entry<PDepartment, List<OrderFood>> entry : foodsByDept.entrySet()){

			//Get the detail to department
			PDepartment deptToSummary = entry.getKey();
			for(PDepartment dept : depts){
				if(entry.getKey().equals(dept)){
					deptToSummary = dept;
					break;
				}
			}
			
			summaryOrder.setOrderFoods(entry.getValue().toArray(new OrderFood[entry.getValue().size()]));
			
			m58.append(new SummaryContent(deptToSummary, 
									  	  PFormat.RECEIPT_FORMAT_DEF, 
									  	  summaryOrder,
									  	  term.owner,
									  	  printType, 
									  	  PStyle.PRINT_STYLE_58MM));
			
			m80.append(new SummaryContent(deptToSummary, 
									  	  PFormat.RECEIPT_FORMAT_DEF, 
									  	  summaryOrder,
									  	  term.owner,
									  	  printType, 
									  	  PStyle.PRINT_STYLE_80MM));
		}
	}

	@Override
	protected StyleContent createItem(PStyle style) {
		
		if(style == PStyle.PRINT_STYLE_58MM){
			return new StyleContent(mRegionId, mOrderId, m58);
			
		}else if(style == PStyle.PRINT_STYLE_80MM){
			return new StyleContent(mRegionId, mOrderId, m80);
			
		}else{
			return null;
		}
	}

}
