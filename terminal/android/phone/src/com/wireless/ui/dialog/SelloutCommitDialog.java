package com.wireless.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.lib.task.UpdateSelloutStatusTask;
import com.wireless.parcel.FoodParcel;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.ui.R;

public class SelloutCommitDialog extends DialogFragment {

	public final static String TAG = "SellOutCommitDialog";
	
	private final static String KEY_SELL_OUT_PARAM = "key_sell_out";
	private final static String KEY_ON_SALE_PARAM = "key_on_sale";
	
	public static SelloutCommitDialog newInstance(List<Food> toSellout, List<Food> toOnSale){
		SelloutCommitDialog dlg = new SelloutCommitDialog();
		Bundle bundle = new Bundle();
		
		ArrayList<FoodParcel> toSelloutParcels = new ArrayList<FoodParcel>();
		for(Food f : toSellout){
			toSelloutParcels.add(new FoodParcel(f));
		}
		bundle.putParcelableArrayList(KEY_SELL_OUT_PARAM, toSelloutParcels);
		
		ArrayList<FoodParcel> toOnSaleParcels = new ArrayList<FoodParcel>();
		for(Food f : toOnSale){
			toOnSaleParcels.add(new FoodParcel(f));
		}
		bundle.putParcelableArrayList(KEY_ON_SALE_PARAM, toOnSaleParcels);
		
		dlg.setArguments(bundle);
		
		return dlg;
	}
	
	public SelloutCommitDialog(){
		
	}
	
	@Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		getDialog().setTitle("请确认沽清列表");
		
		View view = inflater.inflate(R.layout.sell_out_commit_dialog, null);
		
		//取得待沽清菜品
		final List<Food> toSellout = new ArrayList<Food>();
		ArrayList<FoodParcel> sellOutParcels = getArguments().getParcelableArrayList(KEY_SELL_OUT_PARAM);
		for(FoodParcel fp : sellOutParcels){
			toSellout.add(fp.asFood());
		}
		
		//取得待售菜品
		final List<Food> toOnSale = new ArrayList<Food>();
		ArrayList<FoodParcel> onSaleParcels = getArguments().getParcelableArrayList(KEY_ON_SALE_PARAM);
		for(FoodParcel fp : onSaleParcels){
			toOnSale.add(fp.asFood());
		}
		
		final List<Food> foodsToCommit = new ArrayList<Food>();
		foodsToCommit.addAll(toOnSale);
		foodsToCommit.addAll(toSellout);
		
		//待沽清和待售菜品ListView
		ListView listView = (ListView)view.findViewById(R.id.listView_sellOut_commitDialog);
		listView.setAdapter(new BaseAdapter(){

			@Override
			public int getCount() {
				return foodsToCommit.size();
			}

			@Override
			public Object getItem(int position) {
				return foodsToCommit.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view;
				if(convertView == null){
					view = LayoutInflater.from(getActivity()).inflate(R.layout.sell_out_activity_confirm_dialog_list_item, null);
				}else{
					view = convertView;
				}
				
				Food food = foodsToCommit.get(position);
				if(food.getName().length() >= 8){
					((TextView)view.findViewById(R.id.textView_foodName_sellOut_commitDialog)).setText(food.getName().substring(0, 8));
				}else{
					((TextView)view.findViewById(R.id.textView_foodName_sellOut_commitDialog)).setText(food.getName());
				}

				if(toSellout.indexOf(food) >= 0){
					((TextView)view.findViewById(R.id.textView_status_sellOut_commitDialog)).setTextColor(Color.RED);
					((TextView)view.findViewById(R.id.textView_status_sellOut_commitDialog)).setText("沽清");
				}else if(toOnSale.indexOf(food) >= 0){
					((TextView)view.findViewById(R.id.textView_status_sellOut_commitDialog)).setTextColor(Color.GREEN);
					((TextView)view.findViewById(R.id.textView_status_sellOut_commitDialog)).setText("开售");
				}
				
				return view;
			}
       	});
		
		//"确定"Button
		((Button)view.findViewById(R.id.button_ok_sellOut_commitDialog)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new UpdateSelloutStatusTask(WirelessOrder.loginStaff, toSellout, toOnSale){
					
					ProgressDialog mProgDialog;
		
					@Override
					protected void onPreExecute(){
						mProgDialog = ProgressDialog.show(getActivity(), "提示", "正在提交菜品沽清信息...请稍后", true);
					}
					
					@Override
					protected void onPostExecute(Void result){
						mProgDialog.dismiss();
						dismiss();
						if(mBusinessException != null){
							new AlertDialog.Builder(getActivity())
											.setTitle("提示")
											.setMessage(mBusinessException.getMessage())
											.setPositiveButton("确定", null)
											.show();							
						}else{
							Toast.makeText(getActivity(), "更新沽清菜品成功", Toast.LENGTH_SHORT).show();
							getActivity().finish();
						}
					}
				}.execute();
			}
		});
		
		//"取消"Button
		((Button)view.findViewById(R.id.button_cancel_sellOut_commitDialog)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		return view;
	}
}
