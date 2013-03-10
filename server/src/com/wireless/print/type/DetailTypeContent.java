package com.wireless.print.type;

import java.util.HashMap;

import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.content.ContentCombinator;
import com.wireless.print.content.OrderDetailContent;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Terminal;
import com.wireless.server.WirelessSocketServer;

public class DetailTypeContent extends TypeContent {

	private final Order mOrder;
	
	private ContentCombinator m58;
	
	private ContentCombinator m80;
	
	DetailTypeContent(PType printType, Terminal term, Order order) {
		super(printType);
		
		if(!printType.isDetail()){
			throw new IllegalArgumentException("The print type(" + printType + ") is invalid");
		}
		
		this.mOrder = order;
		
		HashMap<PStyle, String> templates = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER_DETAIL);
		
		m58 = new ContentCombinator();
		m80 = new ContentCombinator();
		
		for(OrderFood food : order.getOrderFoods()){
			
			//Append the food to order detail in case of belonging to a specific kitchen
			if(food.getKitchen().getAliasId() != Kitchen.KITCHEN_NULL){
				
				if(food.isCombo()){
					for(Food childFood : food.getChildFoods()){
						//Append the detail of child order food to combinator.
						m58.append(new OrderDetailContent(templates.get(PStyle.PRINT_STYLE_58MM), 
														  food, 
														  childFood,
														  order,
													      term.owner,
														  printType, 
														  PStyle.PRINT_STYLE_58MM));
						
						m80.append(new OrderDetailContent(templates.get(PStyle.PRINT_STYLE_80MM), 
								  						  food, 
								  						  childFood,
								  						  order,
								  						  term.owner,
								  						  printType, 
								  						  PStyle.PRINT_STYLE_80MM));
					}
					
				}else{
					
					//Append the detail of parent to combinator
					m58.append(new OrderDetailContent(templates.get(PStyle.PRINT_STYLE_58MM), 
												   	  food,
												   	  order,
												   	  term.owner,
												   	  printType, 
												   	  PStyle.PRINT_STYLE_58MM));
					
					m80.append(new OrderDetailContent(templates.get(PStyle.PRINT_STYLE_80MM), 
							   						  food,
							   						  order,
							   						  term.owner,
							   						  printType, 
							   						  PStyle.PRINT_STYLE_80MM));
				}
			}
		}
	}

	@Override
	protected StyleContent createItem(PStyle style) {
		if(style == PStyle.PRINT_STYLE_58MM){
			return new StyleContent(mOrder.getRegion().getRegionId(), mOrder.getId(), m58);
			
		}else if(style == PStyle.PRINT_STYLE_80MM){
			return new StyleContent(mOrder.getRegion().getRegionId(), mOrder.getId(), m80);
			
		}else{
			return null;
		}
	}

}
