package com.wireless.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class OrderDiff {
	
	public static class DiffResult{
		public Order oriOrder;
		public Order newOrder;
		public List<OrderFood> extraFoods;
		public List<OrderFood> cancelledFoods;
		public List<OrderFood> hurriedFoods;
		public List<OrderFood> immediateFoods;
	}
	
	/**
	 * Compare the order foods of new order with the ones of original order,
	 * and the compared results would be handled according to the three cases below. 
	 * <1> Ori = New
	 * Means the order food is contained in both original and new order,
	 * Check to see if the difference count is greater or less than zero.
	 * Add to the extra if the difference is greater than zero, add to cancelled if the difference is less than zero.
	 * <2> Ori - New
	 * Means the order foods are only contained in the original order.
	 * Add these order foods to the cancelled.
	 * <3> New - Ori
	 * Means the order foods are only contained in the new order
	 * Add these order foods to the extra. 
	 * @param oriOrder	the original order
	 * @param newOrder	the new order
	 * @return the difference result
	 */
	public static DiffResult diff(Order oriOrder, Order newOrder){
		DiffResult result = new DiffResult();

		List<OrderFood> oriFoods = new ArrayList<OrderFood>(Arrays.asList(oriOrder.foods));
		List<OrderFood> newFoods = new ArrayList<OrderFood>(Arrays.asList(newOrder.foods));
		
		result.oriOrder = oriOrder;
		result.newOrder = newOrder;		
		result.extraFoods = new ArrayList<OrderFood>();
		result.cancelledFoods = new ArrayList<OrderFood>();
		result.hurriedFoods = new ArrayList<OrderFood>();
		result.immediateFoods = new ArrayList<OrderFood>();
		
		/**
		 * Compare the order foods of new order with the ones of original order,
		 * and the compared results would be handled according to the three cases below. 
		 * <1> Ori = New
		 * Means the order food is contained in both original and new order,
		 * Check to see if the difference count is greater or less than zero.
		 * Add to the extra if the difference is greater than zero, add to be cancelled if the difference is less than zero.
		 * <2> Ori - New
		 * Means the order foods are only contained in the original order.
		 * Add these order foods would the cancelled.
		 * <3> New - Ori
		 * Means the order foods are only contained in the new order
		 * Add these order foods would the extra.
		 */
		Iterator<OrderFood> iterNew = newFoods.iterator();
		while(iterNew.hasNext()){
			OrderFood newFood = iterNew.next();
			
			if(newFood.isHurried){
				result.hurriedFoods.add(newFood);
			}
			
			if(newFood.hangStatus == OrderFood.FOOD_IMMEDIATE){
				result.immediateFoods.add(newFood);
			}
			
			Iterator<OrderFood> iterOri = oriFoods.iterator();
			while(iterOri.hasNext()){
				OrderFood oriFood = iterOri.next();
				if(newFood.equals(oriFood)){
					float diff = newFood.getCount() - oriFood.getCount();
					if(diff > 0){
						oriFood.setCount((float)Math.round(Math.abs(diff) * 100) / 100);
						result.extraFoods.add(oriFood);
						
					}else if(diff < 0){
						oriFood.setCount((float)Math.round(Math.abs(diff) * 100) / 100);
						oriFood.setCancelReason(newFood.getCancelReason());
						result.cancelledFoods.add(oriFood);
					}
					
					iterOri.remove();
					iterNew.remove();
					break;
				}
			}
			
		}
		
		for(OrderFood newExtraFood : newFoods){
			if(newExtraFood.hasTaste()){
				newExtraFood.getTasteGroup().setGroupId(TasteGroup.NEW_TASTE_GROUP_ID);
			}
			result.extraFoods.add(newExtraFood);
		}
		//result.extraFoods.addAll(newFoods);		
		result.cancelledFoods.addAll(oriFoods);
		
		return result;
	}
}
