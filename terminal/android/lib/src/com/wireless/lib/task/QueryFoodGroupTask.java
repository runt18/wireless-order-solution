package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Pager;
import com.wireless.protocol.ReqQueryFoodGroup;
import com.wireless.protocol.RespQueryFoodGroupParser;
import com.wireless.sccon.ServerConnector;

public class QueryFoodGroupTask extends AsyncTask<FoodMenu, Void, Pager[]>{
	
	protected BusinessException mBusinessException;
	
	@Override
	protected Pager[] doInBackground(FoodMenu... foodMenu) {
		
		Pager[] pagers = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryFoodGroup());
			if(resp.header.type == Type.ACK){
				pagers = RespQueryFoodGroupParser.parse(resp, foodMenu[0]);
			}else{
				throw new BusinessException("查找菜品分组信息不成功");
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
			
		}catch(BusinessException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return pagers;
	}
}
