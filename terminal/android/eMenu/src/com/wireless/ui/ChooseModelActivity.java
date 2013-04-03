package com.wireless.ui;

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
import com.wireless.protocol.FoodMenuEx;
import com.wireless.protocol.Pager;

public class ChooseModelActivity extends Activity {
	
	public static final String KEY_DEPT_ID = "key_deptId";
	public static final String KEY_KITCHEN_ID = "key_kitchenId";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_choose_model);
		 
		findViewById(R.id.button_chooseModel_panorama).setOnClickListener(new View.OnClickListener() {
			private AsyncTask<FoodMenuEx, Void, Pager[]> mQueryFoodGroupTask;

			@Override
			public void onClick(View v) {
				if(FoodGroupProvider.getInstance().hasGroup()){
					Intent intent = new Intent(ChooseModelActivity.this, PanoramaActivity.class);
					ChooseModelActivity.this.startActivity(intent);
					
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
						protected void onPostExecute(Pager[] result) {
							super.onPostExecute(result);
							mProgressDialog.dismiss();
							mQueryFoodGroupTask = null;
							
							if(result != null){
								FoodGroupProvider.getInstance().setGroups(result);
								Intent intent = new Intent(ChooseModelActivity.this, PanoramaActivity.class);
								ChooseModelActivity.this.startActivity(intent);
							} else {
								Toast.makeText(ChooseModelActivity.this, "没有适合该模式的菜品信息，无法进入该模式", Toast.LENGTH_SHORT).show();
							}
						}
						
					}.execute(WirelessOrder.foodMenu);
				}
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
