package com.wireless.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wireless.adapter.FoodAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.SKitchen;
import com.wireless.util.DragListView;

public class KitchenActivity extends Activity {
	private DragListView myListView;
	private FoodAdapter adapter;
	private AppContext appcontext;
	private ImageView ketback;
	private ArrayAdapter<String> _adapter;
	List<String> kichens;
	List<Food> Foodes;
	short _kichencode;
	private EditText searchpin;
	private AppContext _appContext;
	private TextView Spinner01;
	RelativeLayout sp;
	Kitchen ken;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ketchen);
		
		Common.getCommon().setKient(KitchenActivity.this);
		appcontext = (AppContext) getApplication();
		appcontext.activityList.add(KitchenActivity.this);
		myListView = (DragListView) findViewById(R.id.myListView);
		Spinner01=(TextView)findViewById(R.id.Spinner01);
		Spinner01.setText("厨房");
		searchpin=(EditText)findViewById(R.id.searchpin);
		sp=(RelativeLayout)findViewById(R.id.sp);
		
		searchpin.addTextChangedListener(watcher);
		ketback = (ImageView) findViewById(R.id.ketback);
		Foodes=new ArrayList<Food>();
		
		Foodes=appcontext.getFoods();
		
		adapter = new FoodAdapter(KitchenActivity.this, Foodes);
		myListView.setAdapter(adapter);
		myListView.setOnItemClickListener(new item());

		ketback.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		/*
		 
		 下拉框的点击事件,内部类实现功能
		 * */
		
		sp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				searchpin.setText("");
				//大厨房
				List<SKitchen> skitchen=Arrays.asList(appcontext.getFoodMenu().sKitchens);
				//小厨房
				List<Kitchen> kitchens=Arrays.asList(appcontext.getFoodMenu().kitchens);
				List<List<Kitchen>> kes=new ArrayList<List<Kitchen>>();
				for(int i=0;i<skitchen.size();i++){
					List<Kitchen> kitchenes=new ArrayList<Kitchen>();
					for(int j=0;j<kitchens.size();j++){
					  if(kitchens.get(j).skitchen_id==skitchen.get(i).alias_id){
						  kitchenes.add(kitchens.get(j));
					  }
					}
					 kes.add(kitchenes);
				}
//			   for(int j=0;j<kes.size();j++){
//				  if(kes.get(j).get(0)==null){
//					  kes.remove(kes.get(j));
//				  }
//			   }	
			 Common.getCommon().showkichent(KitchenActivity.this, skitchen, kes);
			}
		});
		
	}
	
	 /*
	    * 点解list的item的事件
	    * 
	    * */
		public class item implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {	
	       
	        	Common.getCommon().getorderFoods(KitchenActivity.this, Foodes,position);
			
		  }
			
		}
	// 输入框实时监听
	private TextWatcher watcher = new TextWatcher() {
		List<Food> _SeachFoods;
		@Override
		public void afterTextChanged(Editable s) {
			// TODO Auto-generated method stub

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {	
		            _SeachFoods=new ArrayList<Food>();
					if(s.toString().trim().equals("")){
						adapter = new FoodAdapter(KitchenActivity.this, Foodes);
						myListView.setAdapter(adapter);
					}else{
						_SeachFoods=Foodes;
						Foodes.clear();
					for (int i = 0; i < _SeachFoods.size(); i++) {
						if (String.valueOf(_SeachFoods.get(i).alias_id).startsWith(s.toString().trim())) {
							Foodes.add(_SeachFoods.get(i));
						}
					}
					adapter = new FoodAdapter(KitchenActivity.this, Foodes);
					myListView.setAdapter(adapter);
					}
				}
	

	};
	
	/*
	 * 根据选择厨房进行判断
	 * */
	public  void getslect(Kitchen k){
		Foodes.clear();
		if(k!=null){
			Spinner01.setText(k.name);
			for(int i=0;i<appcontext.getFoods().size();i++){
				if(appcontext.getFoods().get(i).kitchen==k.alias_id){
					Foodes.add(appcontext.getFoods().get(i));
				}
			}
			adapter = new FoodAdapter(KitchenActivity.this, Foodes);
			myListView.setAdapter(adapter);
			
		}
		
		
		
	}
	
    
   
		
	 
}
