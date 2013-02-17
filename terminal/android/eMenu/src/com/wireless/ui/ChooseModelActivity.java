package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.lib.task.QueryFoodGroupTask;
import com.wireless.ordermenu.R;
import com.wireless.panorama.PanoramaActivity;
import com.wireless.panorama.util.FoodGroupProvider;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Pager;

public class ChooseModelActivity extends Activity {
	
	public static final String KEY_DEPT_ID = "key_deptId";
	public static final String KEY_KITCHEN_ID = "key_kitchenId";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_choose_model);
		 
		final ArrayList<Department> mValidDepts = new ArrayList<Department>();
		final ArrayList<Kitchen> mSortKitchens = new ArrayList<Kitchen>();
		
		if(WirelessOrder.foods != null && WirelessOrder.foods.length != 0){ 
		
			//让菜品按编号排序
			Comparator<Food> mFoodCompByNumber = new Comparator<Food>() {
				@Override
				public int compare(Food food1, Food food2) {
					if (food1.getKitchen().getAliasId() > food2.getKitchen().getAliasId()) {
						return 1;
					} else if (food1.getKitchen().getAliasId() < food2.getKitchen().getAliasId()) {
						return -1;
					} else {
						if(food1.isHot() && !food2.isHot()){
							return -1;
						}else if(!food1.isHot() && food2.isHot()){
							return 1;
						}else{
							return 0;
						}
					}
				}
			};
			
			/*
			 * 将所有菜品进行按厨房编号进行排序，方便筛选厨房
			 */
			Arrays.sort(WirelessOrder.foods, mFoodCompByNumber);
			Arrays.sort(WirelessOrder.foodMenu.foods, mFoodCompByNumber);
			/*
			 * 使用二分查找算法筛选出有菜品的厨房
			 */
			ArrayList<Kitchen> mValidKitchens = new ArrayList<Kitchen>();
			for(Kitchen kitchen : WirelessOrder.foodMenu.kitchens) {
				Food keyFood = new Food();
				keyFood.setKitchen(kitchen);
				int index = Arrays.binarySearch(WirelessOrder.foods, keyFood, new Comparator<Food>() {
							@Override
							public int compare(Food food1, Food food2) {
								if (food1.getKitchen().getAliasId() > food2.getKitchen().getAliasId()) {
									return 1;
								} else if (food1.getKitchen().getAliasId() < food2.getKitchen().getAliasId()) {
									return -1;
								} else {
									return 0;
								}
							}
						});
				if (index >= 0 ) {
					mValidKitchens.add(kitchen);
				}
			}		
			
			/*
			 * 筛选出有菜品的部门
			 */
			mValidDepts.clear();
			for (Department dept : WirelessOrder.foodMenu.depts) {
				for (Kitchen kitchen : mValidKitchens) {
					if(dept.getId() == kitchen.getDept().getId()) {
						mValidDepts.add(dept);
						break;
					}
				}
			}
			
			//根据部门对厨房排序 
			mSortKitchens.clear();
			for(Department d:mValidDepts)
			{
				for(Kitchen k:mValidKitchens)
				{
					if(k.getDept().equals(d))
						mSortKitchens.add(k);
				}
			}
			
			//根据排序了的厨房对食品排序
			ArrayList<Food> mSortFoods = new ArrayList<Food>();
			
			for(Kitchen k:mSortKitchens)
			{
				for(Food f:WirelessOrder.foods)
				{
					if(f.getKitchen().equals(k))
						mSortFoods.add(f);
				}	
			}
			
			WirelessOrder.foods = mSortFoods.toArray(new Food[mSortFoods.size()]);
		}
		
		findViewById(R.id.button_chooseModel_panorama).setOnClickListener(new View.OnClickListener() {
			private AsyncTask<FoodMenu, Void, Pager[]> mQueryFoodGroupTask;

			@Override
			public void onClick(View v) {
				if(FoodGroupProvider.getInstance().hasGroup()){
					startActivity();
				} else if(mQueryFoodGroupTask != null){
					//do noting
				} else {
					mQueryFoodGroupTask = new QueryFoodGroupTask(){

						private ProgressDialog mProgressDialog;

						@Override
						protected void onPreExecute() {
							super.onPreExecute();
							mProgressDialog = ProgressDialog.show(ChooseModelActivity.this, "请稍后", "正在读取菜品信息");
						}
						
						@Override
						protected Pager[] doInBackground(FoodMenu... foodMenu) {
							return super.doInBackground(foodMenu);
						}

						@Override
						protected void onPostExecute(Pager[] result) {
							super.onPostExecute(result);
							mProgressDialog.dismiss();
							mQueryFoodGroupTask = null;
							
							if(result != null){
								FoodGroupProvider.getInstance().setGroups(result);
								startActivity();
							} else Toast.makeText(ChooseModelActivity.this, "没有适合该模式的菜品信息，无法进入该模式", Toast.LENGTH_SHORT).show();
						}
						
					}.execute(WirelessOrder.foodMenu);
				}
			}
			
			private void startActivity(){
				ArrayList<Integer> deptIds = new ArrayList<Integer>();
				for(Department d:mValidDepts){
					deptIds.add(Integer.valueOf(d.getId()));
				}
				
				ArrayList<Integer> kitchenIds = new ArrayList<Integer>();
				for(Kitchen k : mSortKitchens){
					kitchenIds.add((int)k.getAliasId());
				}
				
				Intent intent = new Intent(ChooseModelActivity.this, PanoramaActivity.class);
				intent.putIntegerArrayListExtra(KEY_DEPT_ID, deptIds);
				intent.putIntegerArrayListExtra(KEY_KITCHEN_ID, kitchenIds);
				ChooseModelActivity.this.startActivity(intent);
			}
		});
		
		findViewById(R.id.button_chooseModel_traditional).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ChooseModelActivity.this,MainActivity.class));	
			}
		});
	}
}
