package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;

import com.wireless.adapter.FoodAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.Food;
import com.wireless.ui.KitchenActivity.item;
import com.wireless.util.DragListView;

public class PingyinActivity extends Activity {
	private DragListView myListView;
	private FoodAdapter adapter;
	private AppContext appcontext;
	private ImageView pinback;
	private EditText searchpin;
	private List<Food> foodes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pinyin);
		
		appcontext = (AppContext) getApplication();
		appcontext.activityList.add(PingyinActivity.this);
		myListView = (DragListView) findViewById(R.id.myListView);
		searchpin = (EditText) findViewById(R.id.searchpin);
		

		foodes = new ArrayList<Food>();
		foodes=appcontext.getFoods();
		
		adapter = new FoodAdapter(PingyinActivity.this, foodes);
		myListView.setAdapter(adapter);
		
		pinback = (ImageView) findViewById(R.id.pinback);
		myListView.setOnItemClickListener(new item());
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(searchpin.getWindowToken(), 0);
		searchpin.addTextChangedListener(watcher);
		pinback.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	@Override
	protected void onStart(){
		super.onStart();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}
	
	@Override
	protected void onStop(){
		super.onStop();
	}
	
	 /*
	    * 点解list的item的事件
	    * 
	    * */
		public class item implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Common.getCommon().getorderFoods(PingyinActivity.this,foodes,position);
		  }
			
		}
	// 输入框实时监听

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
			
			if (s.toString() == null || s.toString().equals("")) {
				adapter = new FoodAdapter(PingyinActivity.this,appcontext.getFoods());
				myListView.setAdapter(adapter);
			} else {
				foodes.clear();
				for (int i = 0; i < appcontext.getFoods().size(); i++) {
					if (appcontext.getFoods().get(i).pinyin.contains(s.toString().trim())) {		
						foodes.add(appcontext.getFoods().get(i));
					}
				}
				adapter = new FoodAdapter(PingyinActivity.this, foodes);
				myListView.setAdapter(adapter);
				
			}

		}

	};
}
