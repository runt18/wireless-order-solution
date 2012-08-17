package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.ui.ItemFragment.OnItemChangeListener;

public class MainActivity extends Activity implements OnItemChangeListener{
	ContentFragment mContentFragment;
	ItemFragment mItemFragment;
	KitchenData mKcData;
	public static final String KEY_POSITION = "position";
	
	public static enum TabId{
		TAB1,TAB2
	}
	
	Food[] mTempFoods ;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mContentFragment = (ContentFragment)getFragmentManager().findFragmentById(R.id.content);
		mItemFragment = (ItemFragment)getFragmentManager().findFragmentById(R.id.item);
		
		ImageView amplifyImgView = (ImageView)findViewById(R.id.amplify_btn_imgView);
		amplifyImgView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				System.out.println("btn clicked");
			}
		});
		
		ImageView addDishImgView = (ImageView)findViewById(R.id.add_dish_imgView);
		addDishImgView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				System.out.println("add dish btn clicked");
			}
		});
		
		ImageView setTableImgView = (ImageView)findViewById(R.id.imgView_set_table);
		setTableImgView.setOnClickListener(new BottomClickListener(TabId.TAB1));

		ImageView peopleNumImgView = (ImageView)findViewById(R.id.imageView_num_people);
		peopleNumImgView.setOnClickListener(new BottomClickListener(TabId.TAB2));
		
		ImageView serverImgView = (ImageView)findViewById(R.id.imageView_server);
		serverImgView.setOnClickListener(new BottomClickListener(TabId.TAB2));
		
		ImageView vipImgView = (ImageView)findViewById(R.id.imageView_vip);
		vipImgView.setOnClickListener(new BottomClickListener(TabId.TAB2));
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		mKcData = new KitchenData();
		mItemFragment.onUpdateContent(mKcData.getmValidDepts(),mKcData.getmValidKitchens());
		mContentFragment.setContent(mKcData.getValidFoodImgs());
	}
	
	@Override
	public void onItemChange(int value) {
		if(mContentFragment == null){
			Intent intent = new Intent(MainActivity.this,ContentActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt(KEY_POSITION, value);
			intent.putExtras(bundle);
			startActivity(intent);
		}else{
			mContentFragment.onUpdateContent(mKcData.getPosition((short) value));
		}
	}
	
	class BottomClickListener implements OnClickListener{
		int id = 0;
		BottomClickListener(TabId tabId){
			switch(tabId)
			{
			case TAB1:
				id = 0;
				break;
			case TAB2:
				id = 1;
				break;
			}
		}
		@Override
		public void onClick(View v) {
			View dialogLayout = getLayoutInflater().inflate(R.layout.setup_dialog,(ViewGroup)findViewById(R.id.tab_dialog));
			TabHost tabHost = (TabHost) dialogLayout.findViewById(R.id.tabhost);
			tabHost.setup();
			
			tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("餐台设置").setContent(R.id.tab1));
			tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("其它设置").setContent(R.id.tab2));
			
			tabHost.setCurrentTab(id);
			
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this).setView(dialogLayout)
					.setPositiveButton("确定",new DialogInterface.OnClickListener(){
						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
						}
					})
					.setNegativeButton("取消",new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog,int which) {
							
						}
					});
			dialogBuilder.show();
		}
		
	}
}

class KitchenData {
	private Food[] mTempFoods;
	private ArrayList<Department> mValidDepts;
	private ArrayList<List<Kitchen>> mValidKitchens;
	private HashMap<Short,Integer> kcPositions;
	public Food[] getmTempFoods() {
		return mTempFoods;
	}

	public int getPosition(short kitchenID)
	{
		return kcPositions.get(kitchenID);
	}
	
	public ArrayList<String> getValidFoodImgs() {
		ArrayList<String> validImgs = new ArrayList<String>();
		//FIXME 下面代码未测试
		for(Food f : mTempFoods){
			validImgs.add(f.image);
		}
		validImgs.add("Hydrangeas.jpg");
		validImgs.add("Desert.jpg");
		validImgs.add("Lighthouse.jpg");
		return validImgs;
	}

	public ArrayList<Department> getmValidDepts() {
		return mValidDepts;
	}

	public ArrayList<List<Kitchen>> getmValidKitchens() {
		return mValidKitchens;
	}

	KitchenData(){
	
		mTempFoods = new Food[WirelessOrder.foodMenu.foods.length];
		/**
		 * 将所有菜品进行按厨房编号进行排序
		 */
		System.arraycopy(WirelessOrder.foodMenu.foods, 0, mTempFoods, 0,
				WirelessOrder.foodMenu.foods.length);
		Arrays.sort(mTempFoods, new Comparator<Food>() {
			@Override
			public int compare(Food food1, Food food2) {
				if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
					return 1;
				} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		
		kcPositions = new HashMap<Short,Integer>();
		short firstKc = mTempFoods[0].kitchen.aliasID;
		kcPositions.put(firstKc, 0);
		for(int i =0;i<mTempFoods.length;i++)
		{
			if(firstKc != mTempFoods[i].kitchen.aliasID)
			{
				kcPositions.put(mTempFoods[i].kitchen.aliasID, i);
				Log.i(""+mTempFoods[i].kitchen.aliasID,""+i);
				firstKc = mTempFoods[i].kitchen.aliasID;
			}
		}
		
		/**
		 * 使用二分查找算法筛选出有菜品的厨房
		 */
		ArrayList<Kitchen> mAllKitchens = new ArrayList<Kitchen>();
		for (int i = 0; i < WirelessOrder.foodMenu.kitchens.length; i++) {
			Food keyFood = new Food();
			keyFood.kitchen.aliasID = WirelessOrder.foodMenu.kitchens[i].aliasID;
			int index = Arrays.binarySearch(mTempFoods, keyFood,
					new Comparator<Food>() {
	
						public int compare(Food food1, Food food2) {
							if (food1.kitchen.aliasID > food2.kitchen.aliasID) {
								return 1;
							} else if (food1.kitchen.aliasID < food2.kitchen.aliasID) {
								return -1;
							} else {
								return 0;
							}
						}
					});
			if (index >= 0 ) {
				mAllKitchens.add(WirelessOrder.foodMenu.kitchens[i]);
			}
		}
		
		/**
		 * 筛选出有菜品的部门
		 */
	   
		mValidDepts = new ArrayList<Department>();
		for (int i = 0; i < WirelessOrder.foodMenu.depts.length; i++) {
			for (int j = 0; j < mAllKitchens.size(); j++) {
				if (WirelessOrder.foodMenu.depts[i].deptID == mAllKitchens.get(j).dept.deptID) {
					mValidDepts.add(WirelessOrder.foodMenu.depts[i]);
					break;
				}
			}
		}
		
		/**
		 * 筛选出每个部门中有菜品的厨房
		 */
		mValidKitchens = new ArrayList<List<Kitchen>>();
		for (int i = 0; i < mValidDepts.size(); i++) {
			List<Kitchen> kitchens = new ArrayList<Kitchen>();
			for (int j = 0; j < mAllKitchens.size(); j++) {
				if (mAllKitchens.get(j).dept.deptID == mValidDepts.get(i).deptID) {
					kitchens.add(mAllKitchens.get(j));
				}
			}
			mValidKitchens.add(kitchens);
		}
	}
}
