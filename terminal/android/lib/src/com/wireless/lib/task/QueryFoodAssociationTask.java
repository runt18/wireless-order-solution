package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqQueryFoodAssociation;
import com.wireless.protocol.RespQueryFoodAssociationParser;
import com.wireless.protocol.Type;
import com.wireless.sccon.ServerConnector;

public class QueryFoodAssociationTask extends AsyncTask<FoodMenu, Void, Food[]>{

	protected BusinessException mBusinessException;
	
	protected Food mFoodToAssociate;
	
	public QueryFoodAssociationTask(Food foodToAssoicate){
		mFoodToAssociate = foodToAssoicate;
	}
	
	@Override
	protected Food[] doInBackground(FoodMenu... foodMenu) {
		
		Food[] associatedFoods = null;
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryFoodAssociation(mFoodToAssociate));
			if(resp.header.type == Type.ACK){
				associatedFoods = RespQueryFoodAssociationParser.parse(resp, foodMenu[0]);
			}else{
				throw new BusinessException("查找菜品关联数据不成功");
			}

		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}catch(BusinessException e){
			mBusinessException = new BusinessException(e.getMessage());			
		}
		
		return associatedFoods;
		
	}

}
