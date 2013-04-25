package com.wireless.ui;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.lib.task.QueryFoodGroupTask;
import com.wireless.ordermenu.R;
import com.wireless.panorama.PanoramaActivity;
import com.wireless.protocol.Pager;

public class ChooseModelActivity extends Activity {
	
	public static final String KEY_DEPT_ID = "key_deptId";
	public static final String KEY_KITCHEN_ID = "key_kitchenId";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_choose_model);
		 
		findViewById(R.id.button_chooseModel_panorama).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				new QueryFoodGroupTask(WirelessOrder.pinGen, WirelessOrder.foodMenu.foods){

					private ProgressDialog mProgressDialog;

					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						mProgressDialog = ProgressDialog.show(ChooseModelActivity.this, "请稍后", "正在读取菜品信息");
					}
					
					@Override
					protected void onPostExecute(List<Pager> pagers) {
						mProgressDialog.dismiss();
						
						if(pagers != null){
							WirelessOrder.pagers = pagers;
							ChooseModelActivity.this.startActivity(new Intent(ChooseModelActivity.this, PanoramaActivity.class));
						} else {
							Toast.makeText(ChooseModelActivity.this, "没有适合该模式的菜品信息，无法进入该模式", Toast.LENGTH_SHORT).show();
						}
					}
					
				}.execute();
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
