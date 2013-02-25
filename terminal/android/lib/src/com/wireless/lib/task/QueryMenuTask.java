package com.wireless.lib.task;

import java.io.IOException;
import java.util.Arrays;

import android.os.AsyncTask;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqQueryMenu;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Taste;
import com.wireless.protocol.comp.FoodComp;
import com.wireless.protocol.comp.TasteComp;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.sccon.ServerConnector;
import com.wireless.util.PinyinUtil;

public class QueryMenuTask extends AsyncTask<Void, Void, FoodMenu>{

	protected String mErrMsg;
	
	/**
	 * 在新的线程中执行请求菜谱信息的操作
	 */
	@Override
	protected FoodMenu doInBackground(Void... arg0) {
		
		FoodMenu foodMenu = null;
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqQueryMenu());
			if(resp.header.type == Type.ACK){
				//result = RespQueryMenuParserEx.parse(resp);
				foodMenu = new FoodMenu();
				foodMenu.createFromParcel(new Parcel(resp.body));
				
				Arrays.sort(foodMenu.tastes, TasteComp.instance());
				Arrays.sort(foodMenu.styles, TasteComp.instance());
				Arrays.sort(foodMenu.specs, TasteComp.instance());
				
				Arrays.sort(foodMenu.foods, FoodComp.instance());
				for(int i = 0; i < foodMenu.foods.length; i++){
			
					//Generate the pinyin to each food
					foodMenu.foods[i].setPinyin(PinyinUtil.cn2Spell(foodMenu.foods[i].getName()));
					foodMenu.foods[i].setPinyinShortcut(PinyinUtil.cn2FirstSpell(foodMenu.foods[i].getName()));
					
					if(foodMenu.foods[i].isCombo()){
	
						Food[] childFoods = foodMenu.foods[i].getChildFoods();
						for(int j = 0; j < childFoods.length; j++){
							int index = Arrays.binarySearch(foodMenu.foods, childFoods[j], FoodComp.instance());
							if(index >= 0){
								childFoods[j] = foodMenu.foods[index];
							}
						}
					}
					
					if(foodMenu.foods[i].hasPopTastes()){
						
						Taste[] popTastes = foodMenu.foods[i].getPopTastes();
						for(int j = 0; j < popTastes.length; j++){
							int index;
							index = Arrays.binarySearch(foodMenu.tastes, popTastes[j], TasteComp.instance());
							if(index >= 0){
								popTastes[j] = foodMenu.tastes[index];
								continue;
							}
							
							index = Arrays.binarySearch(foodMenu.styles, popTastes[j], TasteComp.instance());
							if(index >= 0){
								popTastes[j] = foodMenu.styles[index];
								continue;
							}
							
							index = Arrays.binarySearch(foodMenu.specs, popTastes[j], TasteComp.instance());
							if(index >= 0){
								popTastes[j] = foodMenu.specs[index];
								continue;
							}
						}
					}
				}	
				
			}else{
				if(resp.header.reserved == ErrorCode.TERMINAL_NOT_ATTACHED) {
					mErrMsg = "终端没有登记到餐厅，请联系管理人员。";
				}else if(resp.header.reserved == ErrorCode.TERMINAL_EXPIRED) {
					mErrMsg = "终端已过期，请联系管理人员。";
				}else{
					mErrMsg = "菜谱下载失败，请检查网络信号或重新连接。";
				}
				throw new IOException(mErrMsg);
			}
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return foodMenu;
	}

}
