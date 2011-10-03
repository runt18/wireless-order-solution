package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;

import com.wireless.adapter.FoodAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.ui.NumberActivity.item;
import com.wireless.util.DragListView;

public class KitchenActivity extends Activity {
	private DragListView myListView;
	private FoodAdapter adapter;
	private AppContext appcontext;
	private ImageView ketback;
	private Spinner myspinner;
	private ArrayAdapter<String> _adapter;
	List<String> kichens;
	List<Food> _Foodes;
	short _kichencode;
	private EditText searchpin;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ketchen);
		appcontext = (AppContext) getApplication();
		myListView = (DragListView) findViewById(R.id.myListView);
		myspinner = (Spinner) findViewById(R.id.Spinner01);

		searchpin=(EditText)findViewById(R.id.searchpin);
		
		searchpin.addTextChangedListener(watcher);
		ketback = (ImageView) findViewById(R.id.ketback);
		_Foodes=new ArrayList<Food>();
		// 将可选内容与ArrayAdapter连接起来
		_adapter = new ArrayAdapter<String>(this, R.layout.spinner, getKichens());
		myspinner.setAdapter(_adapter);

		myListView.setOnItemClickListener(new item());
		// 设置下拉列表的风格
		_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
		myspinner.setOnItemSelectedListener(new spinnerListen());
		
		
		
	}
	
	 /*
	    * 点解list的item的事件
	    * 
	    * */
		public class item implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Common.getCommon().getorderFoods(KitchenActivity.this,_Foodes,position);
		  }
			
		}
	// 输入框实时监听
	private TextWatcher watcher = new TextWatcher() {

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
		
				 _Foodes.clear();
				for (int i = 0; i < appcontext.getFoods().size(); i++) {
					if (String.valueOf(appcontext.getFoods().get(i).alias_id).startsWith(s.toString().trim())) {
						_Foodes.add(appcontext.getFoods().get(i));
					}
				}
				
				adapter = new FoodAdapter(KitchenActivity.this, _Foodes);
				myListView.setAdapter(adapter);

		

		}

	};
	
	
	/*
	 
	 获取分厨的类型 
	 * */
	
	public List<String> getKichens(){
		 kichens=new ArrayList<String>();
		for(int i = 0; i<appcontext.getFoodMenu().kitchens.length;i++){
			kichens.add(appcontext.getFoodMenu().kitchens[i].name);
		}
		return kichens;
	}
	
	/*
	 下拉框的点击事件
	 * 
	 * */
	public class spinnerListen implements Spinner.OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			Log.e("", getKichens().get(position));
			_Foodes.clear();
			if(position==0){
				_kichencode=Kitchen.KITCHEN_1;
			}
			if(position==1){
				_kichencode=Kitchen.KITCHEN_2;
			}
			if(position==2){
				_kichencode=Kitchen.KITCHEN_3;
			}
			if(position==3){
				_kichencode=Kitchen.KITCHEN_4;
			}
			if(position==4){
				_kichencode=Kitchen.KITCHEN_5;
			}
			if(position==5){
				_kichencode=Kitchen.KITCHEN_6;
			}
			if(position==6){
				_kichencode=Kitchen.KITCHEN_7;
			}
			if(position==7){
				_kichencode=Kitchen.KITCHEN_8;
			}
			if(position==8){
				_kichencode=Kitchen.KITCHEN_9;
			}
			if(position==9){
				_kichencode=Kitchen.KITCHEN_10;
			}
			for(int i=0;i<appcontext.getFoods().size();i++){
				if(appcontext.getFoods().get(i).kitchen==_kichencode){
					_Foodes.add(appcontext.getFoods().get(i));
					Log.e("", appcontext.getFoods().get(i).alias_id+"");
				}
			}
			Log.e("",_Foodes.size()+"");
			adapter = new FoodAdapter(KitchenActivity.this, _Foodes);
			myListView.setAdapter(adapter);
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
