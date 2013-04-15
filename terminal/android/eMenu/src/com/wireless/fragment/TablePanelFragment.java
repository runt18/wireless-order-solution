package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.PRegion;
import com.wireless.protocol.PTable;

/**
 * this fragment will display all legal regions and tables<br/>
 * it use handler to refresh regions or tables respectively<br/><br/>
 * it also use {@link ViewFlipper} and {@link GestureDetector} to handler the scrolling
 * 
 * @author ggdsn1
 * @see RegionRefreshHandler
 * @see TableRefreshHandler
 */
public class TablePanelFragment extends Fragment implements OnGestureListener {
	// 每页要显示餐台数量
	private static final int TABLE_AMOUNT_PER_PAGE = 18;
	private List<PTable> mTables = new ArrayList<PTable>();
	
	private int mTableCond = FILTER_TABLE_ALL;			//the current table filter condition
	
	private final static int FILTER_TABLE_ALL = 0;		//table filter condition to all
	private final static int FILTER_TABLE_IDLE = 1;		//table filter condition to idle
	private final static int FILTER_TABLE_BUSY = 2;		//table filter condition to busy
	
	private short mRegionCond = FILTER_REGION_ALL;		//the current region filter condition
	private final static short FILTER_REGION_ALL = Short.MIN_VALUE;		//region filter condition to all
	private static final String REGION_ALL_STR = "全部区域";
	
	private String mFilterCond = "";					//the current filter string
	
	private TableRefreshHandler mTableRefreshHandler;
	private RegionRefreshHandler mRegionRefreshHandler;
	
	
	private ViewFlipper mFlipper;
	private GestureDetector mGestureDetector;
	private int CURRENT_VIEW_ID = 0;
	private int mPageSize = 0;

	private OnTableChangedListener mOnTableChangedListener;
	private AsyncTask<Void, Void, PTable[]> mQueryTableTask;

	public void setOnTableChangedListener(OnTableChangedListener l){
		mOnTableChangedListener = l;
	}
	
	public interface OnTableChangedListener{
		void onTableChanged(PTable table);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mTableRefreshHandler = new TableRefreshHandler(this);
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
		
		mGestureDetector = new GestureDetector(getActivity(), this);
		mFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper_dialogTab1);
		
		/*
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
				if(--curNum >= 1)
					cstmNumTextView.setText("" + curNum);
			}
		});

		mRegionRefreshHandler.sendEmptyMessage(0);
		mTableRefreshHandler.sendEmptyMessage(0);
		
		return view;
	}

	public void refreshTableState(){
		mQueryTableTask = new QueryTableTask().execute();
	}
	
	public void cancelTask(){
		if(mQueryTableTask != null)
			mQueryTableTask.cancel(true);
	}
	/**
	 * select and refresh all legal regions
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
			for(PTable tbl : WirelessOrder.tables){
				validRegionID.add(tbl.regionID);
			}
			
			/*
			 * 根据条件筛选出所有要显示的区域
			 */
			final List<PRegion> validRegions = new ArrayList<PRegion>();
			validRegions.add(new PRegion(FILTER_REGION_ALL, REGION_ALL_STR, 0));
			for(PRegion region : WirelessOrder.regions){
				if(validRegionID.contains(region.getRegionId())){
					validRegions.add(region);
				}
			}
			//区域
			LinearLayout hScrollViewLinearLayout = (LinearLayout) fragment.getView().findViewById(R.id.hScrollView_linearLayout);
			hScrollViewLinearLayout.removeAllViews();
			for(PRegion r : validRegions)
			{
				RelativeLayout view = (RelativeLayout) LayoutInflater.from(fragment.getActivity()).inflate(R.layout.region_item, null);
				((TextView)view.findViewById(R.id.textView_region)).setText(r.getName());
				view.setTag(r);
				
				view.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						PRegion region = (PRegion) v.getTag();
						fragment.mRegionCond = region.getRegionId();
						fragment.mTableRefreshHandler.sendEmptyMessage(0);
					}
				});
				
				hScrollViewLinearLayout.addView(view);
			}
		}
	}
	/**
	 * 餐台显示的handler,
	 * 根据区域显示不同数量的餐台
	 */
	private static class TableRefreshHandler extends Handler{
		private List<PTable> mFilterTable = new ArrayList<PTable>();
		private WeakReference<TablePanelFragment> mFragment;
		
		TableRefreshHandler(TablePanelFragment fragment)
		{
			mFragment = new WeakReference<TablePanelFragment>(fragment);
		}
		
		/**
		 * according to the condition, select tables and refresh display
		 */
		@Override
		public void handleMessage(Message msg)
		{
			TablePanelFragment fragment = mFragment.get();
			fragment.CURRENT_VIEW_ID = 0;
			mFilterTable.clear();
			mFilterTable.addAll(Arrays.asList(WirelessOrder.tables));
			Iterator<PTable> iter = mFilterTable.iterator();

			/**
			 * Filter the table source according to status & region condition
			 */
			while(iter.hasNext()){
				PTable t = iter.next();
				if(fragment.mTableCond == FILTER_TABLE_IDLE && !t.isIdle()){
					iter.remove();
					
				}else if(fragment.mTableCond == FILTER_TABLE_BUSY && !t.isBusy()){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_1 && t.regionID != PRegion.REGION_1){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_2 && t.regionID != PRegion.REGION_2){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_3 && t.regionID != PRegion.REGION_3){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_4 && t.regionID != PRegion.REGION_4){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_5 && t.regionID != PRegion.REGION_5){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_6 && t.regionID != PRegion.REGION_6){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_7 && t.regionID != PRegion.REGION_7){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_8 && t.regionID != PRegion.REGION_8){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_9 && t.regionID != PRegion.REGION_9){
					iter.remove();
					
				}else if(fragment.mRegionCond == PRegion.REGION_10 && t.regionID != PRegion.REGION_10){
					iter.remove();
					
				}else if(fragment.mFilterCond.length() != 0){
					if(!(t.getName().contains(fragment.mFilterCond) || Integer.toString(t.getAliasId()).startsWith(fragment.mFilterCond))){
						iter.remove();
					}
				}
			}
			fragment.mTables = mFilterTable;
			// 加载餐台信息
			fragment.refreshTableArea();
		}
	}
	
	/**
	 * refresh the table's area and arrange the tables 
	 * 
	 */
	private void refreshTableArea() {
		int size = mTables.size();
		// 计算屏幕的页数
		mPageSize  = (size / TABLE_AMOUNT_PER_PAGE)
				+ (size	% TABLE_AMOUNT_PER_PAGE == 0 ? 0 : 1);

//		// 清空所有Grid View
		mFlipper.removeAllViews();
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		
		for (int pageNo = 0; pageNo < mPageSize; pageNo++) {
			// 每页餐台的Grid View
			GridView grid = new GridView(this.getActivity());

			lp.gravity = Gravity.CENTER;
			grid.setLayoutParams(lp);
			// 设置显示的列数
			grid.setNumColumns(6);
			grid.setVerticalSpacing(16);

			grid.setSelector(android.R.color.transparent);

			// 获取显示在此page显示的Table对象
			ArrayList<PTable> tables4Page = new ArrayList<PTable>();
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
			
			grid.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return mGestureDetector.onTouchEvent(event);
				}
			});
			
			grid.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					PTable table = (PTable) view.getTag();
					
					short customNum = Short.parseShort(((TextView)getView().findViewById(R.id.textView_customNum)).getText().toString());
					table.setCustomNum(customNum);
					
					if(OptionBarFragment.isTableFixed())
					{
						Editor editor = getActivity().getSharedPreferences(Params.TABLE_ID, Context.MODE_PRIVATE).edit();
						editor.putInt(Params.TABLE_ID, table.getAliasId());
						editor.commit();
					}
					
					if(mOnTableChangedListener != null)
						mOnTableChangedListener.onTableChanged(table);
					
				}
			});

			// 添加Grid
			mFlipper.addView(grid);
		}

		LinearLayout pageIndicator = (LinearLayout) getView().findViewById(R.id.page_point);
		pageIndicator.removeAllViews();
		// 初始化页码指示器的每一项
		for (int i = 0; i < mPageSize; i++) {
			ImageView point = new ImageView(this.getActivity());
			point.setImageResource(R.drawable.page_point);
			lp.width =  LinearLayout.LayoutParams.WRAP_CONTENT;
			lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
			lp.setMargins(0, 0, 25, 0);
			point.setLayoutParams(lp);
			pageIndicator.addView(point);
		}
		 //刷新页码指示器
		refreshPageIndicator();
	}
	
	/**
	 * 刷新页码指示器
	 */
	private void refreshPageIndicator() {
		LinearLayout pageIndicator = (LinearLayout) getView().findViewById(R.id.page_point);
		if (mFlipper.getChildCount() > 0) {
			pageIndicator.setVisibility(View.VISIBLE);
			for (int i = 0; i < pageIndicator.getChildCount(); i++) {
				((ImageView) pageIndicator.getChildAt(i))
						.setImageResource(R.drawable.page_point);
			}
			// highlight the active page point
			((ImageView) pageIndicator.getChildAt(CURRENT_VIEW_ID)).setImageResource(R.drawable.page_point_on);

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

		private ArrayList<PTable> _tables;

		TableAdapter(ArrayList<PTable> tables) {
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
			return _tables.get(position).getTableId();
		}

		@Override
		public View getView(int position, View convertView,	ViewGroup parent) {

			View view;

			if (convertView == null) {
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gridview_item, null);
			} else {
				view = convertView;
			}

			final PTable table = _tables.get(position);
			view.setTag(table);
			// 根据餐台的不同状态设置背景
			if (table.isBusy()) {
				((RelativeLayout) view.findViewById(R.id.table_bg)).setBackgroundResource(R.drawable.table_busy);
				((TextView) view.findViewById(R.id.textView_customNum_gridItem)).setText("" + table.getCustomNum());

			} 
			// 设置餐台台号
			if(Integer.toString(table.getAliasId()).length() > 3)
				((TextView) view.findViewById(R.id.textView_tableNum)).setText(Integer.toString(table.getAliasId()).substring(0, 2));
			else ((TextView) view.findViewById(R.id.textView_tableNum)).setText(Integer.toString(table.getAliasId()));
			// 设置餐台名称
			((TextView) view.findViewById(R.id.textView_tableName)).setText(table.getName());

			return view;
		}
	}
	
	/*
	 * 请求餐台信息
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask {
		/*
		 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则执行请求餐厅的操作。
		 */
		@Override
		protected void onPostExecute(PTable[] tables) {
			/**
			 * Prompt user message if any error occurred.
			 */
			if(mBusinessException != null) {
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
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	/**
	 * the {@link GestureDetector} method, handle the finger's sliding
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		//fly 60px will start scroll
		if (e1.getX() - e2.getX() > 60) {
			this.mFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_in));
			this.mFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_out));
			this.mFlipper.showNext();
			//refresh indicator
			if(++CURRENT_VIEW_ID == mPageSize)
				CURRENT_VIEW_ID = 0;
			refreshPageIndicator();
			return true;
		} else if (e1.getX() - e2.getX() < -60) {
			this.mFlipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_in));
			this.mFlipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_out));
			this.mFlipper.showPrevious();
			//refresh indicator
			if(--CURRENT_VIEW_ID < 0)
				CURRENT_VIEW_ID = mPageSize - 1;
			refreshPageIndicator();
			return true;
		}		
		return false;
	}	

}
