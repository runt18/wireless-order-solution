package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.ErrorEnum;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqQueryOrderByTable;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.sccon.ServerConnector;

public class QueryOrderTask extends AsyncTask<Void, Void, Order>{

	protected BusinessException mBusinessException;
	
	protected int mTblAlias;

	private final FoodMenu mFoodMenu;
	
	private final PinGen mPinGen;
	
	public QueryOrderTask(PinGen gen, int tableAlias, FoodMenu foodMenu){
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
				order = new Parcel(resp.body).readParcel(Order.CREATOR);
				
				//Get the detail to each order food
				for(OrderFood eachOrderFood : order.getOrderFoods()){
					eachOrderFood.asFood().copyFrom(mFoodMenu.foods.find(eachOrderFood.asFood()));
					
					if(eachOrderFood.hasNormalTaste()){
						//Get the normal taste detail to each order food
						for(Taste eachNormalTaste : eachOrderFood.getTasteGroup().getNormalTastes()){
							eachNormalTaste.copyFrom(mFoodMenu.tastes.get(mFoodMenu.tastes.indexOf(eachNormalTaste)));
							eachNormalTaste.copyFrom(mFoodMenu.styles.get(mFoodMenu.styles.indexOf(eachNormalTaste)));
							eachNormalTaste.copyFrom(mFoodMenu.specs.get(mFoodMenu.specs.indexOf(eachNormalTaste)));
						}
					}
				}
				
			}else{
				
				ErrorCode errCode = new Parcel(resp.body).readParcel(ErrorCode.CREATOR);
				
				if(errCode.equals(ProtocolError.ORDER_NOT_EXIST)){
					mBusinessException = new BusinessException(mTblAlias + "号台还未下单", errCode);
					
				}else if(errCode.equals(ProtocolError.TABLE_IDLE)) {
					mBusinessException = new BusinessException(mTblAlias + "号台还未下单", errCode);
					
				}else if(errCode.equals(ProtocolError.TABLE_NOT_EXIST)) {
					mBusinessException = new BusinessException(mTblAlias + "号台信息不存在", errCode);

				}else if(errCode.equals(ProtocolError.TERMINAL_NOT_ATTACHED)) {
					mBusinessException = new BusinessException("终端没有登记到餐厅，请联系管理人员。", errCode);

				}else if(errCode.equals(ProtocolError.TERMINAL_EXPIRED)) {
					mBusinessException = new BusinessException("终端已过期，请联系管理人员。", errCode);

				}else{
					mBusinessException = new BusinessException("未确定的异常错误(" + resp.header.reserved + ")", ErrorEnum.UNKNOWN);
				}
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		}
		
		return order;
	}

}
