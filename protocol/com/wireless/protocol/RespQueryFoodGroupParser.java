package com.wireless.protocol;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;


public class RespQueryFoodGroupParser {
	
	/******************************************************
	 * In the case query food group successfully, 
	 * design the query order response looks like below
	 * mode : type : seq : reserved : pin[6] : len[2] : <Body>
	 * <Header>
	 * mode - ORDER_BUSSINESS
	 * type - ACK
	 * seq - same as request
	 * reserved : 0x00
	 * pin[6] : same as request
	 * len[2] -  length of the <Body>
	 * <Body>
	 * amount_to_pager[2] : <Pager> ... <Pager>
	 * <Pager>
	 * amount_to_large_food : large_food_alias[2] ... large_food_alias_x[2]
	 * amount_to_medium_food : medium_food_alias[2] ... medium_food_alias[2]
	 * amount_to_small_food : small_food_alias[2] ... small_food_alias[2]
	 * amount_to_text_food : text_food_alias[2] ... text_food_alias[2]
	 * captain_food_alias[2]
	 *******************************************************/
	public static Pager[] parse(ProtocolPackage response, FoodMenu foodMenu){
		//make sure the response is ACK
		if(response.header.type == Type.ACK){
			int offset = 0;
			
			//get the amount of pagers
			int amount = (response.body[offset] & 0x000000FF) |
						 ((response.body[offset + 1] & 0x000000FF) << 8);
			offset += 2;
			
			Pager[] pagers = new Pager[amount];
			
			for(int i = 0; i < pagers.length; i++){
				
				//get the amount of large foods to this pager
				int foodAmount = response.body[offset];
				offset += 1;
				
				Food[] largeFoods = new Food[foodAmount];
				for(int j = 0; j < largeFoods.length; j++){
					//get the alias to each large food
					int foodAlias = (response.body[offset] & 0x000000FF) |
									((response.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					Food food = new Food();
					food.setAliasId(foodAlias);
					
					//search the detail from food menu
					for(Food real : foodMenu.foods){
						if(real.equals(food)){
							largeFoods[j] = real;
							break;
						}
					}
				}
				
				//get the amount of medium foods to this pager
				foodAmount = response.body[offset];
				offset += 1;
				
				Food[] mediumFoods = new Food[foodAmount];
				for(int j = 0; j < mediumFoods.length; j++){
					//get the alias to each medium food
					int foodAlias = (response.body[offset] & 0x000000FF) |
									((response.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					Food food = new Food();
					food.setAliasId(foodAlias);
					
					//search the detail from food menu
					for(Food real : foodMenu.foods){
						if(real.equals(food)){
							mediumFoods[j] = real;
							break;
						}
					}
				}
				
				//get the amount of small foods to this pager
				foodAmount = response.body[offset];
				offset += 1;
				
				Food[] smallFoods = new Food[foodAmount];
				for(int j = 0; j < smallFoods.length; j++){
					//get the alias to each small food
					int foodAlias = (response.body[offset] & 0x000000FF) |
									((response.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					Food food = new Food();
					food.setAliasId(foodAlias);
					
					//search the detail from food menu
					for(Food real : foodMenu.foods){
						if(real.equals(food)){
							smallFoods[j] = real;
							break;
						}
					}
				}
				
				//get the amount of text foods to this pager
				foodAmount = response.body[offset];
				offset += 1;
				
				Food[] textFoods = new Food[foodAmount];
				for(int j = 0; j < textFoods.length; j++){
					//get the alias to each text food
					int foodAlias = (response.body[offset] & 0x000000FF) |
									((response.body[offset + 1] & 0x000000FF) << 8);
					offset += 2;
					
					Food food = new Food();
					food.setAliasId(foodAlias);
					
					//search the detail from food menu
					for(Food real : foodMenu.foods){
						if(real.equals(food)){
							textFoods[j] = real;
							break;
						}
					}
				}
				
				//get the food to captain food
				int foodAlias = (response.body[offset] & 0x000000FF) |
								((response.body[offset + 1] & 0x000000FF) << 8);
				offset += 2;
				Food captainFood = new Food();
				captainFood.setAliasId(foodAlias);
				
				//search the detail from food menu
				for(Food real : foodMenu.foods){
					if(real.equals(captainFood)){
						captainFood = real;
						break;
					}
				}
				
				pagers[i] = new Pager(largeFoods, mediumFoods, smallFoods, textFoods, captainFood);
			}
			
			return pagers;
			
		}else{
			return new Pager[0];
		}
	}
	
//	public static void main(String[] args){
//		ServerConnector.instance().setNetAddr("localhost");
//		ServerConnector.instance().setNetPort(55555);
//		ReqPackage.setGen(new PinGen(){
//		
//			@Override
//			public long getDeviceId() {
//				return 0x20000002;
//			}
//		
//			@Override
//			public short getDeviceType() {
//				return Terminal.MODEL_ANDROID;
//			}
//			
//		});
//		try{
//			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu());
//			FoodMenu foodMenu = RespQueryMenuParser.parse(resp);
//			resp = ServerConnector.instance().ask(new ReqQueryFoodGroup());
//			Pager[] pagers = RespQueryFoodGroupParser.parse(resp, foodMenu);
//			for(Pager eachPage : pagers){
//				System.out.println(eachPage);
//			}
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//	}
}
