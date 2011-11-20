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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.wireless.adapter.FoodAdapter;
import com.wireless.common.Common;
import com.wireless.protocol.Food;
import com.wireless.ui.view.DragListView;

public class NumberActivity extends Activity {
	private DragListView myListView;
	private FoodAdapter adapter;
	private AppContext appcontext;
	private ImageView numback;
	private EditText search;
	private List<Food> foodes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		int i = getIntent().getExtras().getInt("a");
		
		setContentView(R.layout.number);
		
		appcontext = (AppContext) getApplication();
		appcontext.activityList.add(NumberActivity.this);
		foodes = new ArrayList<Food>();
		myListView = (DragListView) findViewById(R.id.myListView);
		search = (EditText) findViewById(R.id.search);
		foodes=appcontext.getFoods();

		adapter = new FoodAdapter(NumberActivity.this,foodes);
		myListView.setAdapter(adapter);
		numback = (ImageView) findViewById(R.id.numback);
		myListView.setOnItemClickListener(new item());

		search.addTextChangedListener(watcher);
		// InputMethodManager imm =
		// (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		// imm.hideSoftInputFromWindow(search.getWindowToken(), 0);

		numback.setOnClickListener(new View.OnClickListener() {

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
		Common.getCommon().getorderFoods(NumberActivity.this,foodes,position);
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

			if (s.toString() == null || s.toString().equals("")) {
				adapter = new FoodAdapter(NumberActivity.this,appcontext.getFoods());
				myListView.setAdapter(adapter);
			} else {
				foodes.clear();
				for (int i = 0; i < appcontext.getFoods().size(); i++) {
					if (String.valueOf(appcontext.getFoods().get(i).alias_id).startsWith(s.toString().trim())) {
						foodes.add(appcontext.getFoods().get(i));
					}
				}
				Log.e("",foodes.size()+"" );
				adapter = new FoodAdapter(NumberActivity.this, foodes);
				myListView.setAdapter(adapter);

			}

		}

	};
}
