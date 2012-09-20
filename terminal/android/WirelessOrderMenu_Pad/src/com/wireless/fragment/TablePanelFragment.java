package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Region;
import com.wireless.protocol.Table;
import com.wireless.util.ScrollLayout;
import com.wireless.util.ScrollLayout.OnViewChangedListner;

public class TablePanelFragment extends Fragment {
	// 每页要显示餐台数量
	private static final int TABLE_AMOUNT_PER_PAGE = 18;
	private ScrollLayout mTblScrolledArea;
	private List<Table> mTables = new ArrayList<Table>();
	
	private int mTableCond = FILTER_TABLE_ALL;			//the current table filter condition
	
	private final static int FILTER_TABLE_ALL = 0;		//table filter condition to all
	private final static int FILTER_TABLE_IDLE = 1;		//table filter condition to idle
	private final static int FILTER_TABLE_BUSY = 2;		//table filter condition to busy
	
	private short mRegionCond = FILTER_REGION_ALL;		//the current region filter condition
	private final static short FILTER_REGION_ALL = Short.MIN_VALUE;		//region filter condition to all
	private static final String REGION_ALL_STR = "全部区域";
	
	private String mFilterCond = "";					//the current filter string
	
	private DataRefreshHandler mTableRefreshHandler;
	private RegionRefreshHandler mRegionRefreshHandler;
	
	private OnTableChangedListener mOnTableChangedListener;
	
	public void setOnTableChangedListener(OnTableChangedListener l){
		mOnTableChangedListener = l;
	}
	
	public interface OnTableChangedListener{
		void onTableChanged(Table table);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mTableRefreshHandler = new DataRefreshHandler(this);
		mRegionRefreshHandler = new RegionRefreshHandler(this);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater , ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.dialog_tab1,container,false);
		
		final AutoCompleteTextView tableNumEditText = (AutoCompleteTextView)view.findViewById(R.id.editText_table_num);
		
		tableNumEditText.addTextChangedListener(new TextWatcher(){
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mFilterCond = s.length() == 0 ? "" : s.toString().trim();
				mTableRefreshHandler.sendEmptyMessage(0);
			}
		});
		
		// 显示餐台的scroll view group
		mTblScrolledArea = (ScrollLayout) view.findViewById(R.id.tableFlipper);
		mTblScrolledArea.setOnViewChangedListener(new OnViewChangedListner() {
					@Override
					public void onViewChanged(int curScreen, View parent,View curView) {
						reflashPageIndictor(view);
					}
				});
		
		/**
		 * “清空”按钮
		 */
		((ImageButton) view.findViewById(R.id.deleteBtn_dialog_tab1)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				tableNumEditText.setText("");
			}
		});
		
		/*
		 * 人数增加的按钮
		 */
		final TextView cstmNumTextView = (TextView) view.findViewById(R.id.textView_customNum);
		((ImageButton) view.findViewById(R.id.imageButton_plus_tab1)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int curNum = Integer.parseInt(cstmNumTextView.getText().toString());
				cstmNumTextView.setText("" + ++curNum);
			}
		});
		
		/*
		 * 人数减少的按钮
		 */
		((ImageButton) view.findViewById(R.id.imageButton_minus_tab1)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				int curNum = Integer.parseInt(cstmNumTextView.getText().toString());
				if(--curNum >= 0)
					cstmNumTextView.setText("" + curNum);
			}
		});
		
		/*
		 * 刷新餐台按钮
		 */
		((Button) view.findViewById(R.id.button_tab1_refresh)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				new QueryTableTask().execute();
			}
		});

		return view;
	}
	
	@Override
	public void onStart(){
		super.onStart();
		new QueryTableTask().execute();
	}
	/*
	 * 区域选择的handler
	 * 根据选择的区域显示不同的餐台
	 */
	private static class RegionRefreshHandler extends Handler{
		
		private WeakReference<TablePanelFragment> mFragment;
		
		RegionRefreshHandler(TablePanelFragment fragment)
		{
			mFragment = new WeakReference<TablePanelFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg){

			final TablePanelFragment fragment = mFragment.get();
	
			/**
			 * Filter the region containing data.
			 */
			HashSet<Short> validRegionID = new HashSet<Short>();
			for(Table tbl : WirelessOrder.tables){
				validRegionID.add(tbl.regionID);
			}
			
			/*
			 * 根据条件筛选出所有要显示的区域
			 */
			final List<Region> validRegions = new ArrayList<Region>();
			validRegions.add(new Region(FILTER_REGION_ALL, REGION_ALL_STR));
			for(Region region : WirelessOrder.regions){
				if(validRegionID.contains(region.regionID)){
					validRegions.add(region);
				}
			}
			
			LinearLayout hScrollViewLinearLayout = (LinearLayout) fragment.getView().findViewById(R.id.hScrollView_linearLayout);
			hScrollViewLinearLayout.removeAllViews();
			for(Region r : validRegions)
			{
				RelativeLayout view = (RelativeLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.region_item, null);
				((TextView)view.findViewById(R.id.textView_region)).setText(r.name);
				view.setTag(r);
				
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Region region = (Region) v.getTag();
						fragment.mRegionCond = region.regionID;
						fragment.mTableRefreshHandler.sendEmptyMessage(0);
					}
				});
				
				hScrollViewLinearLayout.addView(view);
			}
		}
	}
	/*
	 * 餐台显示的handler
	 * 根据区域显示不同数量的餐台
	 */
	private static class DataRefreshHandler extends Handler{
		private List<Table> mFilterTable = new ArrayList<Table>();
		private WeakReference<TablePanelFragment> mFragment;
		
		DataRefreshHandler(TablePanelFragment fragment)
		{
			mFragment = new WeakReference<TablePanelFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg)
		{
			TablePanelFragment fragment = mFragment.get();
			mFilterTable.clear();
			mFilterTable.addAll(Arrays.asList(WirelessOrder.tables));
			Iterator<Table> iter = mFilterTable.iterator();

			/**
			 * Filter the table source according to status & region condition
			 */
			while(iter.hasNext()){
				Table t = iter.next();

				if(fragment.mTableCond == FILTER_TABLE_IDLE && t.status != Table.TABLE_IDLE){
					iter.remove();
					
				}else if(fragment.mTableCond == FILTER_TABLE_BUSY && t.status != Table.TABLE_BUSY){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_1 && t.regionID != Region.REGION_1){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_2 && t.regionID != Region.REGION_2){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_3 && t.regionID != Region.REGION_3){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_4 && t.regionID != Region.REGION_4){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_5 && t.regionID != Region.REGION_5){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_6 && t.regionID != Region.REGION_6){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_7 && t.regionID != Region.REGION_7){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_8 && t.regionID != Region.REGION_8){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_9 && t.regionID != Region.REGION_9){
					iter.remove();
					
				}else if(fragment.mRegionCond == Region.REGION_10 && t.regionID != Region.REGION_10){
					iter.remove();
					
				}else if(fragment.mFilterCond.length() != 0){
					if(!(t.name.contains(fragment.mFilterCond) || Integer.toString(t.aliasID).startsWith(fragment.mFilterCond))){
						iter.remove();
					}
				}
			}
			fragment.mTables = mFilterTable;
			// 加载餐台信息
			fragment.reflashTableArea(fragment.getView());
		}
	}
	
	/**
	 * 根据传入的餐台信息，刷新餐台区域
	 */
	private void reflashTableArea(View view) {
		int size = mTables.size();
		// 计算屏幕的页数
		int pageSize = (size / TABLE_AMOUNT_PER_PAGE)
				+ (size	% TABLE_AMOUNT_PER_PAGE == 0 ? 0 : 1);

		// 清空所有Grid View
		mTblScrolledArea.removeAllViews();
		mTblScrolledArea.page = 0;
		mTblScrolledArea.mCurScreen = 0;
		mTblScrolledArea.mDefaultScreen = 0;

		for (int pageNo = 0; pageNo < pageSize; pageNo++) {
			// 每页餐台的Grid View
			GridView grid = new GridView(this.getActivity());

			grid.setSelected(true);

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			lp.gravity = Gravity.CENTER;
			grid.setLayoutParams(lp);
			// 设置显示的列数
			grid.setNumColumns(6);
			grid.setVerticalSpacing(16);

			grid.setSelector(android.R.color.transparent);

			// 获取显示在此page显示的Table对象
			ArrayList<Table> tables4Page = new ArrayList<Table>();
			for (int i = 0; i < TABLE_AMOUNT_PER_PAGE; i++) {
				int index = pageNo * TABLE_AMOUNT_PER_PAGE + i;
				if (index < size) {
					tables4Page.add(mTables.get(index));
				} else {
					break;
				}
			}
			// 设置Grid的Adapter
			grid.setAdapter(new TableAdapter(tables4Page));
			
			grid.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final Table table = (Table) view.getTag();
					
					final short customNum = Short.parseShort(((TextView)getView().findViewById(R.id.textView_customNum)).getText().toString());
					
					new QueryTableStatusTask(table.aliasID){
						@Override
						void OnQueryTblStatus(byte status) {
							if(mOnTableChangedListener != null){
								table.status = status;
								table.customNum = customNum;
								mOnTableChangedListener.onTableChanged(table);
							}
						}								
					}.execute();
				}
			});

			// 添加Grid
			mTblScrolledArea.addView(grid);

		}

		LinearLayout pageIndicator = (LinearLayout) view.findViewById(R.id.page_point);
		pageIndicator.removeAllViews();
		// 初始化页码指示器的每一项
		for (int i = 0; i < pageSize; i++) {
			ImageView point = new ImageView(this.getActivity());
			point.setImageResource(R.drawable.page_point);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(0, 0, 25, 0);
			point.setLayoutParams(lp);
			pageIndicator.addView(point);
		}
		 //刷新页码指示器
		reflashPageIndictor(view);
	}
	
	/**
	 * 刷新页码指示器
	 */
	private void reflashPageIndictor(View view) {
		LinearLayout pageIndicator = (LinearLayout) view.findViewById(R.id.page_point);
		if (mTblScrolledArea.getChildCount() > 0) {
			pageIndicator.setVisibility(View.VISIBLE);
			for (int i = 0; i < pageIndicator.getChildCount(); i++) {
				((ImageView) pageIndicator.getChildAt(i))
						.setImageResource(R.drawable.page_point);
			}
			// highlight the active page point
			((ImageView) pageIndicator.getChildAt(mTblScrolledArea
					.getCurScreen())).setImageResource(R.drawable.page_point_on);

		} else {
			pageIndicator.setVisibility(View.GONE);
		}
	}
	
	
	
	/**
	 * 餐台信息的Adapter
	 * 
	 * @author Ying.Zhang
	 * 
	 */
	private class TableAdapter extends BaseAdapter {

		private ArrayList<Table> _tables;

		TableAdapter(ArrayList<Table> tables) {
			this._tables = tables;
		}

		@Override
		public int getCount() {
			return _tables.size();
		}

		@Override
		public Object getItem(int position) {
			return _tables.get(position);
		}

		@Override
		public long getItemId(int position) {
			return _tables.get(position).tableID;
		}

		@Override
		public View getView(int position, View convertView,	ViewGroup parent) {

			View view;

			if (convertView == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item, null);
			} else {
				view = convertView;
			}

			final Table table = _tables.get(position);
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
	
	/**
	 * 请求餐台信息
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask {
		
		private ProgressDialog mProgDialog;
		
		/**
		 * 在执行请求餐台信息前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(TablePanelFragment.this.getActivity(), "", "正在更新餐台信息...请稍后", true);
		}


		/**
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则执行请求餐厅的操作。
		 */
		@Override
		protected void onPostExecute(Table[] tables) {
			
			mProgDialog.dismiss();
			
			/**
			 * Prompt user message if any error occurred.
			 */
			if(mErrMsg != null) {
				Toast.makeText(TablePanelFragment.this.getActivity(), "刷新餐台数据失败,请检查网络", Toast.LENGTH_SHORT).show();

			} else {
				
				WirelessOrder.tables = tables;
				
				mRegionRefreshHandler.sendEmptyMessage(0);
				mTableRefreshHandler.sendEmptyMessage(0);
				((AutoCompleteTextView) getView().findViewById(R.id.editText_table_num)).setText("");
				Toast.makeText(TablePanelFragment.this.getActivity(), "餐台信息刷新成功",	Toast.LENGTH_SHORT).show();
			} 
		}
	}
	
	/**
	 * 请求获得餐台的状态
	 */
	private abstract class QueryTableStatusTask extends com.wireless.lib.task.QueryTableStatusTask{

		private ProgressDialog _progDialog;

		QueryTableStatusTask(int tableAlias){
			super(tableAlias);
		}
		
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(TablePanelFragment.this.getActivity(), "", "查询" + mTblAlias + "号餐台信息...请稍候", true);
		}
		
		/**
		 * 如果相应的操作不符合条件（比如要改单的餐台还未下单），
		 * 则把相应信息提示给用户，否则根据餐台状态，分别跳转到下单或改单界面。
		 */
		@Override
		protected void onPostExecute(Byte tblStatus){
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 * Otherwise perform the corresponding action.
			 */
			if(mErrMsg != null){
				new AlertDialog.Builder(TablePanelFragment.this.getActivity())
				.setTitle("提示")
				.setMessage(mErrMsg)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
				
			}else{			
				OnQueryTblStatus(tblStatus);
			}
		}	
		
		abstract void OnQueryTblStatus(byte status);
		
	}	

}
