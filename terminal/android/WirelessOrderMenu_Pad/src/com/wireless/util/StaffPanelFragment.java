package com.wireless.util;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.StaffTerminal;

public class StaffPanelFragment extends Fragment {
	private StaffTerminal mStaff;
	private TextView mServerIdTextView;
	private EditText mServerPswdEditText;

	private OnStaffChangedListener mOnStaffChangedListener;
	
	public interface OnStaffChangedListener{
		void onStaffChanged(StaffTerminal staff, String id, String pwd);
	}
	
	public void setOnServerChangeListener(OnStaffChangedListener l){
		mOnStaffChangedListener = l;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.dialog_tab2,container,false);
		
		mServerIdTextView  = (TextView) view.findViewById(R.id.TextView_serverId);
		mServerPswdEditText = (EditText) view.findViewById(R.id.editText_serverPswd);

		/*
		 * 设置服务员列表
		 */
		final ListView staffLstView = (ListView) view.findViewById(R.id.listView_server_tab2);
		List<String> staffNames = new ArrayList<String>();
		for(StaffTerminal s : WirelessOrder.staffs){
			staffNames.add(s.name);
		}
		staffLstView.setAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_checked, staffNames));
		/*
		 * 从列表框中选择员工信息的操作
		 */
		staffLstView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mStaff = WirelessOrder.staffs[position];
				mServerIdTextView.setText(mStaff.name);
				staffLstView.setItemChecked(position, true);
			}
		});
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		((Button) getView().findViewById(R.id.button_tab2_confirm)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(mOnStaffChangedListener != null){
					mOnStaffChangedListener.onStaffChanged(mStaff, mServerIdTextView.getText().toString(), mServerPswdEditText.getText().toString());
				}
				mServerPswdEditText.setText("");
			}
		});
	}
	
}
