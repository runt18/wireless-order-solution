package com.wireless.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wireless.ordermenu.R;
import com.wireless.parcel.TableParcel;
import com.wireless.protocol.Table;

public class TableItemFragment extends Fragment{
    private static final String KEY_PARENT_RES_ID = "table_resource_id";

    public static TableItemFragment newInstance(List<Table> rowTables, int parentResId) {
        final TableItemFragment f = new TableItemFragment();

        final Bundle args = new Bundle();
        
        ArrayList<TableParcel> tables = new ArrayList<TableParcel>();
        for(Table t:rowTables){
        	tables.add(new TableParcel(t));
        }
        
        args.putParcelableArrayList(TableParcel.KEY_VALUE, tables);
        args.putInt(KEY_PARENT_RES_ID, parentResId);
        f.setArguments(args);

        return f;
    }

	public TableItemFragment() {
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.table_item_fragment, container, false);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = this.getArguments();
		
		int parentResId = 0;
		ArrayList<TableParcel> tables = new ArrayList<TableParcel>();
		if(args != null)
		{
			tables = args.getParcelableArrayList(TableParcel.KEY_VALUE);
			parentResId = args.getInt(KEY_PARENT_RES_ID);
		}
		
		GridView gridView = (GridView) this.getView();
		TableAdapter adapter = new TableAdapter(tables);
		gridView.setAdapter(adapter);
		
        final ViewPagerTablePanelFragment parentFragment = (ViewPagerTablePanelFragment)getActivity().getFragmentManager().findFragmentById(parentResId);
        
        if(parentFragment != null)
			gridView.setOnItemClickListener(new OnItemClickListener(){
	
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final Table table = (Table) view.getTag();
					
					final short customNum = Short.parseShort(((TextView)getView().findViewById(R.id.textView_customNum)).getText().toString());
					//TODO 增加回调
				}
			});
	}
    
	class TableAdapter extends BaseAdapter{
		private ArrayList<TableParcel> mTables;
		public TableAdapter(ArrayList<TableParcel> tables) {
			this.mTables = tables;
		}

		@Override
		public int getCount() {
			return mTables.size();
		}

		@Override
		public Object getItem(int position) {
			return mTables.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item, null);
			} else {
				view = convertView;
			}
			
			final Table table = mTables.get(position);
			view.setTag(table);
			// 根据餐台的不同状态设置背景
			if (table.status == Table.TABLE_BUSY) {
				((RelativeLayout) view.findViewById(R.id.table_bg)).setBackgroundResource(R.color.red);
				((TextView) view.findViewById(R.id.textView_customNum_gridItem)).setText("" + table.customNum);
			}
			// 设置餐台台号
			((TextView) view.findViewById(R.id.textView_tableNum)).setText(Integer.toString(table.aliasID));
			// 设置餐台名称
			((TextView) view.findViewById(R.id.textView_tableName)).setText(table.name);

			return view;
		}
		
	}
}
