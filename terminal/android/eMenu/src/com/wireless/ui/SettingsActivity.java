package com.wireless.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ListView;

import com.wireless.common.ShoppingCart.OnTableChangeListener;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Table;

public class SettingsActivity extends PreferenceActivity  implements OnTableChangeListener{

	public static final String SETTINGS_IP = "settingIP";
	public static final int SETTING_RES_CODE = 131;
	
	private boolean isFoodChanged;

	private Table mTable;

	@Override
	public void onBuildHeaders(List<Header> target) {
		if(getIntent().hasExtra(SETTINGS_IP)){
			loadHeadersFromResource(R.xml.setting_preference_simple_header, target);
		}
		else loadHeadersFromResource(R.xml.setting_preference_header, target);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//如果是更新菜品的项，则启动更新
		if(position == 2){
			new QueryMenuTask().execute();
		}
	}

	@Override
	public void onBackPressed() {
		
		Intent intent = new Intent();
		Bundle bundle = new Bundle();

		//如果绑定了餐台，则传回餐台
		if(mTable != null){
			bundle.putParcelable(TableParcel.KEY_VALUE, new TableParcel(mTable));
		}

		if(isFoodChanged){
			bundle.putBoolean(SettingActivity.FOODS_REFRESHED, true);
		}
		
		intent.putExtras(bundle);
		setResult(SettingsActivity.SETTING_RES_CODE, intent);
		super.onBackPressed();
	}
	
	@Override
	public void onTableChange(Table table) {
		mTable = table;
	}

	
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{
		private ProgressDialog mToast;

		private Comparator<Food> mFoodComp = new Comparator<Food>() {
			@Override
			public int compare(Food lhs, Food rhs) {
				if(lhs.getAliasId() == rhs.getAliasId()){
					return 0;
				}else if(lhs.getAliasId() > rhs.getAliasId()){
					return 1;
				}else{
					return -1;
				}
			}
		};

		
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mToast = ProgressDialog.show(SettingsActivity.this, "","正在下载菜谱...请稍候");
		}
		
		/**
		 * 执行菜谱请求操作
		 */
		@Override
		protected FoodMenu doInBackground(Void... arg0) {
			FoodMenu foodMenu = super.doInBackground(arg0);
			if(foodMenu != null){
				/**
				 * Filter the food without image and sort the food by alias id
				 */
				List<Food> validFoods = new ArrayList<Food>();
				for(Food food : foodMenu.foods){
					if(food.image != null && !food.isSellOut()){
						validFoods.add(food);
					}
				}
				Collections.sort(validFoods, mFoodComp);
				WirelessOrder.foods = validFoods.toArray(new Food[validFoods.size()]);
			}
			return foodMenu;
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(FoodMenu foodMenu){
			mToast.cancel();
			WirelessOrder.foodMenu = foodMenu;
			isFoodChanged  = true;
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(SettingsActivity.this)
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();
			}
		}
	}
}
