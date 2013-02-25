package com.wireless.lib.task;

import java.io.IOException;
import java.util.Arrays;

import android.os.AsyncTask;

import com.wireless.excep.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryOrderByTable;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.comp.FoodComp;
import com.wireless.protocol.comp.TasteComp;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.sccon.ServerConnector;

public class QueryOrderTask extends AsyncTask<FoodMenu, Void, Order>{

	protected BusinessException mBusinessException;
	
	protected int mTblAlias;

	public QueryOrderTask(int tableAlias){
		mTblAlias = tableAlias;
	}	
	
	@Override
	protected Order doInBackground(FoodMenu... foodMenu) {
		Order order = null;
		try{
			//根据tableID请求数据
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrderByTable(mTblAlias));
			if(resp.header.type == Type.ACK){
				order = new Order();
				order.createFromParcel(new Parcel(resp.body));
				
				OrderFood[] foods = order.getOrderFoods();
				for(int i = 0; i < foods.length; i++){
					//Get the food detail from menu.
					int index = Arrays.binarySearch(foodMenu[0].foods, foods[i], FoodComp.instance());
					if(index >= 0){
						foods[i].copyFrom(foodMenu[0].foods[index]);
					}
					
					if(foods[i].hasNormalTaste()){
						Taste[] normal = foods[i].getTasteGroup().getNormalTastes();
						//Get the taste detail from menu.
						for(int j = 0; j < normal.length; j++){
							index = Arrays.binarySearch(foodMenu[0].tastes, normal[j], TasteComp.instance());
							if(index >= 0){
								normal[j].copyFrom(foodMenu[0].tastes[index]);
								continue;
							}
							
							index = Arrays.binarySearch(foodMenu[0].specs, normal[j], TasteComp.instance());
							if(index >= 0){
								normal[j].copyFrom(foodMenu[0].specs[index]);
								continue;
							}

							index = Arrays.binarySearch(foodMenu[0].styles, normal[j], TasteComp.instance());
							if(index >= 0){
								normal[j].copyFrom(foodMenu[0].styles[index]);
								continue;
							}
						}
					}
				}
				
			}else{
				mBusinessException = new BusinessException(resp.header.reserved);
				
				if(resp.header.reserved == ErrorCode.ORDER_NOT_EXIST){
					mBusinessException = new BusinessException(mTblAlias + "号台还未下单", ErrorCode.ORDER_NOT_EXIST);
					
				}else if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
					mBusinessException = new BusinessException(mTblAlias + "号台还未下单", ErrorCode.TABLE_IDLE);
					
				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
					mBusinessException = new BusinessException(mTblAlias + "号台信息不存在", ErrorCode.TABLE_NOT_EXIST);

				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					mBusinessException = new BusinessException("终端没有登记到餐厅，请联系管理人员。", ErrorCode.TERMINAL_NOT_ATTACHED);

				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					mBusinessException = new BusinessException("终端已过期，请联系管理人员。", ErrorCode.TERMINAL_EXPIRED);

				}else{
					mBusinessException = new BusinessException("未确定的异常错误(" + resp.header.reserved + ")", ErrorCode.UNKNOWN);
				}
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return order;
	}

}
