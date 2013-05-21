package com.wireless.print.type;

import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.print.content.ContentCombinator;
import com.wireless.print.content.OrderDetailContent;
import com.wireless.protocol.Terminal;

public class DetailTypeContent extends TypeContent {

	private final Order mOrder;
	
	private final ContentCombinator m58;
	
	private final ContentCombinator m80;
	
	DetailTypeContent(PType printType, Terminal term, Order order) {
		super(printType);
		
		if(!printType.isDetail()){
			throw new IllegalArgumentException("The print type(" + printType + ") is invalid");
		}
		
		this.mOrder = order;
		
		
		m58 = new ContentCombinator();
		m80 = new ContentCombinator();
		
		for(OrderFood food : order.getOrderFoods()){
			
			//Append the food to order detail in case of belonging to a specific kitchen
			if(food.getKitchen().getAliasId() != Kitchen.KITCHEN_NULL){
				
				if(food.asFood().isCombo()){
					for(Food childFood : food.asFood().getChildFoods()){
						//Append the detail of child order food to combinator.
						m58.append(new OrderDetailContent(food, 
														  childFood,
														  order,
													      term.owner,
														  printType, 
														  PStyle.PRINT_STYLE_58MM));
						
						m80.append(new OrderDetailContent(food, 
								  						  childFood,
								  						  order,
								  						  term.owner,
								  						  printType, 
								  						  PStyle.PRINT_STYLE_80MM));
					}
					
				}else{
					
					//Append the detail of parent to combinator
					m58.append(new OrderDetailContent(food,
												   	  order,
												   	  term.owner,
												   	  printType, 
												   	  PStyle.PRINT_STYLE_58MM));
					
					m80.append(new OrderDetailContent(food,
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
