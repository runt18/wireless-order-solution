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
	List<Food> _Foodes;
	short _kichencode;
	private EditText searchpin;
	private AppContext _appContext;
	private TextView Spinner01;
	RelativeLayout sp;
	Kitchen ken;
	boolean tag=false;
	List<Food> seachfoods;;
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
		Spinner01.setText("����");
		searchpin=(EditText)findViewById(R.id.searchpin);
		sp=(RelativeLayout)findViewById(R.id.sp);
		
		searchpin.addTextChangedListener(watcher);
		ketback = (ImageView) findViewById(R.id.ketback);
		_Foodes=new ArrayList<Food>();
		seachfoods=new ArrayList<Food>();
		// ����ѡ������ArrayAdapter��������
//		_adapter = new ArrayAdapter<String>(this, R.layout.spinner, getKichens());
//		myspinner.setAdapter(_adapter);
		adapter = new FoodAdapter(KitchenActivity.this, appcontext.getFoods());
		myListView.setAdapter(adapter);
		myListView.setOnItemClickListener(new item());
		// ���������б�ķ��
		//_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ketback.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		/*
		 
		 ������ĵ���¼�,�ڲ���ʵ�ֹ���
		 * */
		//myspinner.setOnItemSelectedListener(new spinnerListen());
		
		sp.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				searchpin.setText("");
				
				//�����
				List<SKitchen> skitchen=Arrays.asList(appcontext.getFoodMenu().sKitchens);
				//С����
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
			 Common.getCommon().showkichent(KitchenActivity.this, skitchen, kes);
			}
		});
		
	}
	
	 /*
	    * ���list��item���¼�
	    * 
	    * */
		public class item implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {	
	        if(tag){
	        	Common.getCommon().getorderFoods(KitchenActivity.this, _Foodes,position);

	        }else{
	         	Common.getCommon().getorderFoods(KitchenActivity.this, appcontext.getFoods(),position);

	        }  
			
			
		  }
			
		}
	// �����ʵʱ����
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
					if(tag){
						seachfoods=_Foodes;
						_Foodes.clear();
						for (int i = 0; i <seachfoods.size(); i++) {
							if (String.valueOf(seachfoods.get(i).alias_id).startsWith(s.toString().trim())) {
								_Foodes.add(seachfoods.get(i));
							}
						}
						adapter = new FoodAdapter(KitchenActivity.this, _Foodes);
						myListView.setAdapter(adapter);
						
						
						
					}else{
						if(s.toString().trim().equals("")){
							_Foodes.clear();
							_Foodes=appcontext.getFoods();
							adapter = new FoodAdapter(KitchenActivity.this, _Foodes);
							myListView.setAdapter(adapter);
						}else{
							seachfoods=_Foodes;
							_Foodes.clear();
						for (int i = 0; i < seachfoods.size(); i++) {
							if (String.valueOf(seachfoods.get(i).alias_id).startsWith(s.toString().trim())) {
								_Foodes.add(seachfoods.get(i));
							}
						}
						adapter = new FoodAdapter(KitchenActivity.this, _Foodes);
						myListView.setAdapter(adapter);
						}
					}
					
				
			

		}

	};
	
	/*
	 * ����ѡ����������ж�
	 * */
	public  void getslect(Kitchen k){
		this.ken=k;
		_Foodes.clear();
		if(k!=null){
			Spinner01.setText(k.name);
			for(int i=0;i<appcontext.getFoods().size();i++){
				if(appcontext.getFoods().get(i).kitchen==k.alias_id){
					_Foodes.add(appcontext.getFoods().get(i));
				}
			}
			adapter = new FoodAdapter(KitchenActivity.this, _Foodes);
			myListView.setAdapter(adapter);
			tag=true;
		}
		
		
		
	}
	
    
   
		
	 
}
