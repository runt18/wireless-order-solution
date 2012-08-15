package com.wireless.ui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
import com.wireless.protocol.Food;
import com.wireless.ui.ItemFragment.OnItemChangeListener;

public class MainActivity extends Activity implements OnItemChangeListener{
	ContentFragment mContentFragment;
	public static final String KEY_POSITION = "position";
	
	public static enum TabId{
		TAB1,TAB2
	}
	
	Food[] mTempFoods = new Food[WirelessOrder.foodMenu.foods.length];

	
	@Override
	public void onItemChange(int value) {
		// TODO Auto-generated method stub
		
		ArrayList<String> selectedFoodImgs = new ArrayList<String>();
		for(Food f:mTempFoods)
		{
			if(f.kitchen.aliasID == value)
				selectedFoodImgs.add(f.image);
		}

		if(mContentFragment == null){
			Intent intent = new Intent(MainActivity.this,ContentActivity.class);
			Bundle bundle = new Bundle();
			bundle.putInt(KEY_POSITION, value);
			intent.putExtras(bundle);
			startActivity(intent);
		}else{
			mContentFragment.onUpdateContent(selectedFoodImgs);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		System.arraycopy(WirelessOrder.foodMenu.foods, 0, mTempFoods, 0,
				WirelessOrder.foodMenu.foods.length);
		
		mContentFragment = (ContentFragment)getFragmentManager().findFragmentById(R.id.content);
		
		ImageView amplifyImgView = (ImageView)findViewById(R.id.amplify_btn_imgView);
		amplifyImgView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("btn clicked");
			}
		});
		
		ImageView addDishImgView = (ImageView)findViewById(R.id.add_dish_imgView);
		addDishImgView.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
		
		ArrayList<String> allImgs = new ArrayList<String>();
		for(Food f : mTempFoods){
			if(f.image != null)
				allImgs.add(f.image);
		}
		//FIXME 去掉那个add
		if(!allImgs.isEmpty())
		{
			for(String s:allImgs)
				Log.i(s,"$$$$$$$$$");
			allImgs.add("Hydrangeas.jpg");
			mContentFragment.onUpdateContent(allImgs);
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
			// TODO Auto-generated method stub
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
							// TODO Auto-generated method stub
						}
					})
					.setNegativeButton("取消",new DialogInterface.OnClickListener(){

						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
							// TODO Auto-generated method stub
							
						}
					});
			dialogBuilder.show();
		}
		
	}
}
