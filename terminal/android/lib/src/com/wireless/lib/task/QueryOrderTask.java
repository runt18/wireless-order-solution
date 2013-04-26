package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.excep.ProtocolException;
import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryOrderByTable;
import com.wireless.protocol.FoodMenuEx;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.sccon.ServerConnector;

public class QueryOrderTask extends AsyncTask<Void, Void, Order>{

	protected ProtocolException mBusinessException;
	
	protected int mTblAlias;

	private final FoodMenuEx mFoodMenu;
	
	private final PinGen mPinGen;
	
	public QueryOrderTask(PinGen gen, int tableAlias, FoodMenuEx foodMenu){
		mTblAlias = tableAlias;
		mFoodMenu = foodMenu;
		mPinGen = gen;
	}	
	
	@Override
	protected Order doInBackground(Void... args) {
		Order order = null;
		try{
			//根据tableID请求数据
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryOrderByTable(mPinGen, mTblAlias));
			if(resp.header.type == Type.ACK){
				order = new Order();
				order.createFromParcel(new Parcel(resp.body));
				
				//Get the detail to each order food
				for(OrderFood eachOrderFood : order.getOrderFoods()){
					eachOrderFood.copyFrom(mFoodMenu.foods.find(eachOrderFood));
					
					if(eachOrderFood.hasNormalTaste()){
						//Get the normal taste detail to each order food
						for(Taste eachNormalTaste : eachOrderFood.getTasteGroup().getNormalTastes()){
							eachNormalTaste.copyFrom(mFoodMenu.tastes.find(eachNormalTaste));
							eachNormalTaste.copyFrom(mFoodMenu.styles.find(eachNormalTaste));
							eachNormalTaste.copyFrom(mFoodMenu.specs.find(eachNormalTaste));
						}
					}
				}
				
			}else{
				mBusinessException = new ProtocolException(resp.header.reserved);
				
				if(resp.header.reserved == ErrorCode.ORDER_NOT_EXIST){
					mBusinessException = new ProtocolException(mTblAlias + "号台还未下单", ErrorCode.ORDER_NOT_EXIST);
					
				}else if(resp.header.reserved == ErrorCode.TABLE_IDLE) {
					mBusinessException = new ProtocolException(mTblAlias + "号台还未下单", ErrorCode.TABLE_IDLE);
					
				}else if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST) {
					mBusinessException = new ProtocolException(mTblAlias + "号台信息不存在", ErrorCode.TABLE_NOT_EXIST);

				}else if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					mBusinessException = new ProtocolException("终端没有登记到餐厅，请联系管理人员。", ErrorCode.TERMINAL_NOT_ATTACHED);

				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					mBusinessException = new ProtocolException("终端已过期，请联系管理人员。", ErrorCode.TERMINAL_EXPIRED);

				}else{
					mBusinessException = new ProtocolException("未确定的异常错误(" + resp.header.reserved + ")", ErrorCode.UNKNOWN);
				}
			}
		}catch(IOException e){
			mBusinessException = new ProtocolException(e.getMessage());
		}
		
		return order;
	}

}
