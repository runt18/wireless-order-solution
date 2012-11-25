package com.wireless.protocol;


public class RespQueryFoodAssociationParser {
	
	public static Food[] parse(ProtocolPackage response, FoodMenu foodMenu){
		/******************************************************
		 * In the case query associated food successfully, 
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
		 * amount : associated_food_alias[2] ... associated_food_alias_x[2]
		 * amount - 1-byte indicates the amount to associated food
		 * associated_food_alias_x[2] - 2-byte indicates alias id to each associated food
		 *******************************************************/
		//make sure the response is ACK
		if(response.header.type == Type.ACK){
			
			int offset = 0;
			
			//get the amount of associated foods
			int foodAmount = response.body[offset];
			offset += 1;
			
			//get alias id to each associated food
			Food[] associatedFoods = new Food[foodAmount];
			for(int i = 0; i < associatedFoods.length; i++){
				//get the food's id
				int foodAlias = (response.body[offset] & 0x000000FF) |
								((response.body[offset + 1] & 0x000000FF) << 8);
				offset += 2;
				
				Food food = new Food();
				food.setAliasId(foodAlias);
				
				//get the details to this associated food
				for(int j = 0; j < foodMenu.foods.length; j++){
					if(food.equals(foodMenu.foods[j])){
						associatedFoods[i] = foodMenu.foods[j];
						break;
					}
				}				
			}
			
			return associatedFoods;
			
		}else{
			return new Food[0];
		}
	}
	
//	public static void main(String[] args){
//		ServerConnector.instance().setNetAddr("localhost");
//		ServerConnector.instance().setNetPort(55555);
//		ReqPackage.setGen(new PinGen(){
//
//			@Override
//			public long getDeviceId() {
//				return 0x20000001;
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
//			resp = ServerConnector.instance().ask(new ReqQueryFoodAssociation(new Food(0, 202, 0)));
//			Food[] associateFoods = RespQueryFoodAssociationParser.parse(resp, foodMenu);
//			for(Food f : associateFoods){
//				System.out.println(f.name);
//			}
//		}catch(IOException e){
//			e.printStackTrace();
//		}
//	}
}
