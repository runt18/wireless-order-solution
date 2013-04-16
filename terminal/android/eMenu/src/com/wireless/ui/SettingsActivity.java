package com.wireless.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.ListView;

import com.wireless.common.ShoppingCart.OnTableChangedListener;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodList;
import com.wireless.protocol.FoodMenuEx;
import com.wireless.protocol.PTable;
import com.wireless.protocol.comp.FoodComp;

/**
 * This activity will display some setting headers, all header are define 
 * by {@code R.xml.setting_preference_simple_header}
 * or {@code R.xml.setting_preference_header}. 
 * when click the header, it will jump to the target fragment which define in {@code <header></header>}
 * 
 * <br/>
 * This activity also contain food menu refresh logic and it may return the bounded table as a result.
 * @author ggdsn1
 *
 */
public class SettingsActivity extends PreferenceActivity  implements OnTableChangedListener{

	public static final String SETTINGS_IP = "settingIP";
	public static final int SETTING_RES_CODE = 131;
	public static final String FOODS_REFRESHED = "food_refreshed";
	
	private boolean isFoodChanged;

	private PTable mTable;

	@Override
	public void onBuildHeaders(List<Header> target) {
		//TODO 删除旧版设置
		if(getIntent().hasExtra(SETTINGS_IP)){
			loadHeadersFromResource(R.xml.setting_preference_simple_header, target);
		}else{
			loadHeadersFromResource(R.xml.setting_preference_header, target);
		}
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
			bundle.putBoolean(FOODS_REFRESHED, true);
		}
		
		intent.putExtras(bundle);
		setResult(SettingsActivity.SETTING_RES_CODE, intent);
		super.onBackPressed();
	}
	
	@Override
	public void onTableChanged(PTable table) {
		mTable = table;
	}

	/**
	 * the task to refresh the food menu, all new foods will be sort by it's id
	 * @author ggdsn1
	 *
	 */
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{
		private ProgressDialog mToast;
		
		/**
		 * 执行菜谱请求操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mToast = ProgressDialog.show(SettingsActivity.this, "","正在下载菜谱...请稍候");
		}
		
		/**
		 * 根据返回的error message判断，如果发错异常则提示用户，
		 * 如果菜谱请求成功，则继续进行请求餐厅信息的操作。
		 */
		@Override
		protected void onPostExecute(FoodMenuEx foodMenu){
			mToast.cancel();
			WirelessOrder.foodMenu = foodMenu;
			
			//Filter the food without image and sort the food by alias id.
			List<Food> foods = new ArrayList<Food>(foodMenu.foods);
			Iterator<Food> iter = foods.iterator();
			while(iter.hasNext()){
				Food f = iter.next();
				if(f.image == null || f.isSellOut()){
					iter.remove();
				}
			}
			WirelessOrder.foods = new FoodList(foods, FoodComp.DEFAULT);
			
			isFoodChanged  = true;
			/**
			 * Prompt user message if any error occurred,
			 * otherwise continue to query restaurant info.
			 */
			if(mProtocolException != null){
				new AlertDialog.Builder(SettingsActivity.this)
				.setTitle("提示")
				.setMessage(mProtocolException.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				}).show();
			}
		}
	}
}
