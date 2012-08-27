package com.wireless.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Table;

public class SetTableFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_tab1,container,false);
		
		final AutoCompleteTextView mTableNumEditText;
		
		mTableNumEditText = (AutoCompleteTextView)view.findViewById(R.id.editText_table_num);
		mTableNumEditText.setThreshold(0);
		Table[] tableSources = WirelessOrder.tables;
		List<String> tables = new ArrayList<String>();
		for(Table t:tableSources)
		{
			tables.add(String.valueOf(t.aliasID));
		}
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(view.getContext(),R.layout.table_item,tables);
		mTableNumEditText.setAdapter(arrayAdapter);
		
		return view;
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
//
//		
//		EditText mPeopCntEditText = (EditText) activity.findViewById(R.id.editText_people_cnt);

//		((Button)activity.findViewById(R.id.button_tab1_confirm)).setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				final String gotTableNum = mTableNumEditText.getText().toString();
//				final TextView tableNumTextView = (TextView)activity.findViewById(R.id.txtView_table_count);
//
//				if(gotTableNum.isEmpty())
//					tableNumTextView.setText("点击设置");
//				else{
//					new QueryTableStatusTask(Integer.parseInt(gotTableNum)){
//						@Override
//						void OnQueryTblStatus(int status) {
//							tableNumTextView.setText(gotTableNum);
//							if(status == Table.TABLE_IDLE){
//								mSelectedFoodTextView.setText("0");
//								Toast.makeText(activity.getApplicationContext(), "该餐台尚未点菜", Toast.LENGTH_SHORT).show();
//							}else if(status == Table.TABLE_BUSY){
//								 new QueryOrderTask(Integer.parseInt(gotTableNum)){
//									@Override
//									void onOrderChanged(Order order) {
//										mSelectedFoodTextView.setText(""+order.foods.length);
//									}
//								 }.execute(WirelessOrder.foodMenu);
//							}
//						}								
//					}.execute();
//				}
//			}
//		});
	}
}
