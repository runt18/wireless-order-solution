package com.wireless.ui;

import java.lang.ref.WeakReference;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.excep.ProtocolException;
import com.wireless.fragment.KitchenFragment;
import com.wireless.fragment.PickFoodFragment;
import com.wireless.pack.ErrorCode;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;
import com.wireless.ui.view.OrderFoodListView;
import com.wireless.ui.view.OrderFoodListView.AllMarkClickListener;
import com.wireless.ui.view.OrderFoodListView.OnChangedListener;
import com.wireless.ui.view.OrderFoodListView.OnOperListener;

public class QuickPickActivity extends FragmentActivity 
							   implements OnOperListener, 
							   			  OnFoodPickedListener,
							   			  AllMarkClickListener
{
	//每个点菜方式的标签
	private static final int NUMBER_FRAGMENT = 6320;
	private static final int KITCHEN_FRAGMENT = 6321;
	private static final int PINYIN_FRAGMENT = 6322;
	private static final int PICKED_FOOD_INTERFACE = 6323;
	private int mLastView;

	//activity返回标签
	private final static int PICK_WITH_TASTE = 7755;
	
	//储存已点菜的列表
	//private ArrayList<OrderFood> mPickFoods = new ArrayList<OrderFood>();
	

	private TextHandler mTextHandler;
	
	private OrderFoodListView mNewFoodLstView;
	
	/**
	 * 刷新新点菜显示的Handler
	 */
	private static class TextHandler extends Handler{
		private WeakReference<QuickPickActivity> mActivity;
		private TextView mTotalCnt;
		private TextView mTotalPrice;
		//private OrderFoodListView mNewFoodLstView;
		
		TextHandler(final QuickPickActivity activity){
			mActivity = new WeakReference<QuickPickActivity>(activity);
			mTotalCnt = (TextView) activity.findViewById(R.id.textView_totalCount_revealFood__quickPick);
			mTotalPrice = (TextView) activity.findViewById(R.id.textView_totalPrice_revealFood_quickPick);
			
			//mNewFoodLstView = mActivity.get().mNewFoodLstView;
			activity.mNewFoodLstView.setOperListener(activity);
			//设置已点菜ListView的类型和侦听器
			activity.mNewFoodLstView.setChangedListener(new OnChangedListener(){
				@Override
				public void onSourceChanged() {
					activity.mTextHandler.sendEmptyMessage(0);
				}
			});
		}

		@Override
		public void handleMessage(Message msg) {
			QuickPickActivity activity = mActivity.get();
			//刷新新点菜List的显示总数和金额
			Order order = new Order(activity.mNewFoodLstView.getSourceData());
			mTotalCnt.setText(String.valueOf(order.getOrderFoods().length));
			mTotalPrice.setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(order.calcTotalPrice()));
		}		

	}
	
	//刷新每个view的handler
	private ViewHandler mViewHandler;
	
	private static class ViewHandler extends Handler{
		private WeakReference<QuickPickActivity> mActivity;
		
		private TextView mTitleTextView;
		private ImageButton mNumBtn;
		private ImageButton mKitchenBtn;
		private ImageButton mSpellBtn;
		private ImageButton mPickedBtn;

		private FrameLayout mFgmContainer;

		ViewHandler(QuickPickActivity activity){
			mActivity = new WeakReference<QuickPickActivity>(activity);
			mTitleTextView = (TextView) activity.findViewById(R.id.toptitle);
			mTitleTextView.setVisibility(View.VISIBLE);
			
			mNumBtn = (ImageButton) activity.findViewById(R.id.imageButton_num_quickPick);
			mKitchenBtn = (ImageButton) activity.findViewById(R.id.imageButton_kitchen_quickPick);
			mSpellBtn = (ImageButton) activity.findViewById(R.id.imageButton_spell_quickPick);
			mPickedBtn = (ImageButton) activity.findViewById(R.id.imageButton_remark_quickPick);
			
			mFgmContainer = (FrameLayout) activity.findViewById(R.id.frameLayout_container_quickPick);
		}
		
		@Override
		public void handleMessage(Message msg) {
			QuickPickActivity activity = mActivity.get();
			FragmentTransaction ftrans = activity.getSupportFragmentManager().beginTransaction();
			
			mFgmContainer.setVisibility(View.VISIBLE);
			
			switch(msg.what)
			{
			case NUMBER_FRAGMENT:
				//创建新菜品选择fragment
				PickFoodFragment numFragment = new PickFoodFragment();
				numFragment.setFoodPickedListener(activity);
				//设置显示参数
				Bundle args = new Bundle();
				args.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_NUMBER);
				args.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "请输入编号搜索");
				numFragment.setArguments(args);
				//替换原本的fragment
				ftrans.replace(R.id.frameLayout_container_quickPick, numFragment).commit();
				activity.mLastView = NUMBER_FRAGMENT;
				mTitleTextView.setText("点菜 - 编号");
				setLastCate(NUMBER_FRAGMENT);
				
				break;
				
			case KITCHEN_FRAGMENT:
				
				KitchenFragment kitchenFragment = new KitchenFragment();
				kitchenFragment.setFoodPickedListener(activity);
				ftrans.replace(R.id.frameLayout_container_quickPick, kitchenFragment).commit();
					
				activity.mLastView = KITCHEN_FRAGMENT;
				
				mTitleTextView.setText("点菜 - 分厨");
				setLastCate(KITCHEN_FRAGMENT);
				break;
				
			case PINYIN_FRAGMENT:
				//创建新菜品选择fragment
				PickFoodFragment spellFragment = new PickFoodFragment();
				spellFragment.setFoodPickedListener(activity);
				//设置显示参数
				Bundle spellAargs = new Bundle();
				spellAargs.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_SPELL);
				spellAargs.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "请输入拼音搜索");
				spellFragment.setArguments(spellAargs);
				//替换原本的fragment
				ftrans.replace(R.id.frameLayout_container_quickPick, spellFragment).commit();
				
				activity.mLastView = PINYIN_FRAGMENT;
				
				mTitleTextView.setText("点菜 - 拼音");
				setLastCate(PINYIN_FRAGMENT);
				break;
				
			case PICKED_FOOD_INTERFACE:
				//将fragment容器隐藏，显示已点菜界面
				mFgmContainer.setVisibility(View.GONE);
				((RelativeLayout) activity.findViewById(R.id.relativeLayout_bottom_revealFood_quickPick)).setVisibility(View.VISIBLE);
				mTitleTextView.setText("已点菜");
				
				activity.mLastView = PICKED_FOOD_INTERFACE;
				
				//展开新点菜ListView
				activity.mNewFoodLstView.expandGroup(0);
				activity.mTextHandler.sendEmptyMessage(0);
				setLastCate(PICKED_FOOD_INTERFACE);
				break;
			}
		}
		
		private void setLastCate(int cate){
			
			QuickPickActivity activity = mActivity.get();
			//还原按样式
			mNumBtn.setImageResource(R.drawable.number_btn);
			mKitchenBtn.setImageResource(R.drawable.kitchen);
			mSpellBtn.setImageResource(R.drawable.pinyin);
			mPickedBtn.setImageResource(R.drawable.picked_food);
			//切换点菜方式时，保存当前的点菜模式
			Editor editor = activity.getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();
			
			switch(cate)
			{
			case NUMBER_FRAGMENT:
				editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_NUMBER);
				mNumBtn.setImageResource(R.drawable.number_btn_down);
				break;
			case KITCHEN_FRAGMENT:
				editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
				mKitchenBtn.setImageResource(R.drawable.kitchen_down);
				break;
			case PINYIN_FRAGMENT:
				editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_PINYIN);
				mSpellBtn.setImageResource(R.drawable.pinyin_down);
				break;
			case PICKED_FOOD_INTERFACE:
				mPickedBtn.setImageResource(R.drawable.picked_food_down);
				break;
			}
			editor.commit();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quick_pick);
		
		//Update the sell out foods
		new QuerySellOutTask().execute();
		
		mNewFoodLstView = (OrderFoodListView)findViewById(R.id.orderFoodListView_revealFood_quickPick);
		mNewFoodLstView.init(Type.INSERT_ORDER);
		mNewFoodLstView.setAllMarkClickListener(this);
		
		mViewHandler = new ViewHandler(this);
		mTextHandler = new TextHandler(this);
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		//返回Button
		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);
		
		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		TextView right = (TextView) findViewById(R.id.textView_right);
		right.setText("提交");
		right.setVisibility(View.VISIBLE);
		
		//提交按钮
		ImageButton commit = (ImageButton) findViewById(R.id.btn_right);
		commit.setVisibility(View.VISIBLE);
		commit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//若未点菜，则提示。
				if(mNewFoodLstView.getSourceData().length != 0){
					CommitDialog dialog = new CommitDialog(QuickPickActivity.this);
					dialog.setTitle("请输入餐台号或核对点菜信息");
					dialog.show();
					
				}else{
					Toast.makeText(getApplicationContext(), "您尚未点菜", Toast.LENGTH_SHORT).show();
				}
			}
		});

		//编号
		((ImageButton) findViewById(R.id.imageButton_num_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != NUMBER_FRAGMENT){
					mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
				}
			}
		});
		
		//分厨
		((ImageButton) findViewById(R.id.imageButton_kitchen_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != KITCHEN_FRAGMENT){
					mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
				}
			}
		});
		
		//拼音
		((ImageButton) findViewById(R.id.imageButton_spell_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != PINYIN_FRAGMENT){
					mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
				}
			}
		});
		
		//已点菜
		((ImageButton) findViewById(R.id.imageButton_remark_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(PICKED_FOOD_INTERFACE);
			}
		});

		/*
		 * 根据上次保存的记录，切换到相应的点菜方式
		 */
		int lastPickCate = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getInt(Params.LAST_PICK_CATE, Params.PICK_BY_PINYIN);
		switch(lastPickCate)
		{
		case Params.PICK_BY_NUMBER:
			mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
			break;
		case Params.PICK_BY_KITCHEN:
			mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
			break;
		case Params.PICK_BY_PINYIN:
			mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
			break;
		default :
			mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
		}
	}

	@Override
	public void onBackPressed() {
		
		if(mNewFoodLstView.getSourceData().length <= 0){
			super.onBackPressed();
			finish();
			
		}else{ 
			new AlertDialog.Builder(QuickPickActivity.this)
				.setTitle("退出确认")
				.setMessage("已点菜尚未提交，确定要退出？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener(){
					@Override
					public void onClick( DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				})
				.setNegativeButton("取消", null).show();
		}
	}

	//activity返回后将菜品添加进已点菜中
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			OrderFoodParcel foodParcel;
			switch (requestCode) {
			case PICK_WITH_TASTE:
				
				 //添加口味后添加到pickList中
				foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				addFood(foodParcel.asOrderFood());
				
				break;
				
			case OrderFoodListView.PICK_TASTE:
				
				 //口味改变时通知ListView进行更新
				foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				mNewFoodLstView.setFood(foodParcel.asOrderFood());				

				break;
			case OrderActivity.ALL_ORDER_REMARK:
				
				foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
				if(foodParcel.asOrderFood().hasTaste()){
					mNewFoodLstView.setAllTaste(foodParcel.asOrderFood().getTasteGroup().getNormalTastes());
				}
				break;
			}
		}
	}
	
	/**
	 * 通过"编号"、"分厨"、"拼音"方式选中菜品后， 将菜品保存到List中
	 * 
	 * @param food
	 *            选中菜品的信息
	 */
	@Override
	public void onPicked(OrderFood food) {
		addFood(food);
	}

	/**
	 * 通过"编号"、"分厨"、"拼音"方式选中菜品后， 将菜品保存到List中，并跳转到口味Activity选择口味
	 * @param food
	 *            选中菜品的信息
	 */
	@Override
	public void onPickedWithTaste(OrderFood food, boolean isTempTaste) {
		Intent intent = new Intent(this, PickTasteActivity.class);
		Bundle bundle = new Bundle(); 
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(food));
		
		if(isTempTaste)
			bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_PINZHU);
		else bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
		
		intent.putExtras(bundle);
		startActivityForResult(intent, PICK_WITH_TASTE);
	}
	
	/**
	 * 在已点菜列表中选中某个菜品后，选择口味操作，跳转到口味Activity
	 * @param food
	 *            选中菜品的信息
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemp()){
			Toast.makeText(this, "临时菜不能添加口味", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(QuickPickActivity.this, PickTasteActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(selectedFood));
			intent.putExtras(bundle);
			startActivityForResult(intent, OrderFoodListView.PICK_TASTE);			
		}		
	}

	/**
	 * 点击点菜按钮，跳转到上次点菜方式的Tab
	 */
	@Override
	public void onPickFood() {
		/*
		 * 根据上次保存的记录，切换到相应的点菜方式
		 */
		int lastPickCate = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getInt(Params.LAST_PICK_CATE, Params.PICK_BY_PINYIN);
		switch(lastPickCate)
		{
		case Params.PICK_BY_NUMBER:
			mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
			break;
		case Params.PICK_BY_KITCHEN:
			mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
			break;
		case Params.PICK_BY_PINYIN:
			mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
			break;
		default :
			mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
		}
	}
	
	/**
	 * 添加菜品到已点菜的List中
	 * 
	 * @param food
	 *            选中的菜品信息
	 */
	private void addFood(OrderFood food) {

		try{

			mNewFoodLstView.addFood(food);
			
			Toast.makeText(this, "添加"	+ (food.isHangup() ? "并叫起\"" : "\"") + food.toString() + "\"" +
								 NumericUtil.float2String2(food.getCount()) + "份", Toast.LENGTH_SHORT)	.show();
			
			
		}catch(ProtocolException e){
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}


	
	/**
	 * 快点界面中的提交Dialog
	 */
	private class CommitDialog extends Dialog{

		private ListView mListView;
		
		private boolean mIsPayOrder = false;
		
		private Order mOrderToCommit;
		
		public CommitDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
			super(context, cancelable, cancelListener);
			init();
		}

		public CommitDialog(Context context, int theme) {
			super(context, theme);
			init();
		}

		public CommitDialog(Context context) {
			super(context);
			init();
		}
		
		private void init(){
			this.setContentView(R.layout.commit_dialog);

			//设置对话框长宽
			final LayoutParams lp = getWindow().getAttributes();
			lp.height = 660;
			lp.width = LayoutParams.MATCH_PARENT;
			getWindow().setAttributes(lp);
			
           	final AutoCompleteTextView tableText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_commitDialog);
           	
			//弹出软键盘
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
			final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
           	imm.showSoftInput(tableText, 0); //显示软键盘
           	imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
           	
           	//提交不打印按钮
           	final Button changeBtn = (Button) findViewById(R.id.button_changeOrder_commitDialog);
           	changeBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIsPayOrder = false;
					try{
						short tableAlias = Short.parseShort(tableText.getText().toString());
						new QueryAndCommitOrderTask(tableAlias, ReqInsertOrder.DO_NOT_PRINT).execute();
					}catch(NumberFormatException e){
						Toast.makeText(QuickPickActivity.this, "你输入的台号不正确，请重新输入", Toast.LENGTH_SHORT).show();
					}
				}
			});
           	
           	//提交并结账按钮  
           	Button commitBtn = (Button) findViewById(R.id.button_commitDialog_payBill);
           	commitBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIsPayOrder = true;
					try{
						short tableAlias = Short.parseShort(tableText.getText().toString());
						new QueryAndCommitOrderTask(tableAlias).execute();
					}catch(NumberFormatException e){
						Toast.makeText(QuickPickActivity.this, "你输入的台号不正确，请重新输入", Toast.LENGTH_SHORT).show();
					}
				}
			});
           	
           	//取消按钮
        	((Button)this.findViewById(R.id.button_cancel_commitDialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();					
				}
			});
        	
        	//确定按钮
        	((Button)this.findViewById(R.id.button_confirm_commitDialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					if(mNewFoodLstView.getSourceData().length > 0){
						mIsPayOrder = false;
						try{
							short tableAlias = Short.parseShort(tableText.getText().toString());
							new QueryAndCommitOrderTask(tableAlias).execute();
						}catch(NumberFormatException e){
							Toast.makeText(QuickPickActivity.this, "你输入的台号不正确，请重新输入", Toast.LENGTH_SHORT).show();
						}
						
					}else{
						Toast.makeText(QuickPickActivity.this, "您还没有点菜", Toast.LENGTH_SHORT).show();						
					}
				}
			});
           	
           	mListView = (ListView) this.findViewById(R.id.listView_commitDialog);
           	
           	//当被点击或滚动时隐藏键盘
           	mListView.setOnScrollListener(new OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					imm.hideSoftInputFromWindow(tableText.getWindowToken(), 0);
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					
				}
			});
           	
           	mListView.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					imm.hideSoftInputFromWindow(tableText.getWindowToken(), 0); 
				}
           	});
           	
           	mListView.setAdapter(new BaseAdapter(){

           		OrderFood[] mSrcFoods = mNewFoodLstView.getSourceData();
           		
				@Override
				public int getCount() {
					return mSrcFoods.length;
				}

				@Override
				public Object getItem(int position) {
					return mSrcFoods[position];
				}

				@Override
				public long getItemId(int position) {
					return position;
				}

				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View view;
					if(convertView == null){
						view = LayoutInflater.from(getContext()).inflate(R.layout.quick_pick_commit_dialog_item, null);
					}else{
						view = convertView;
					}
					
					OrderFood food = mSrcFoods[position];
					if(food.getName().length() >= 8){
						((TextView)view.findViewById(R.id.textView_foodName_commit_dialog_item)).setText(food.getName().substring(0,	8));
					}else{
						((TextView)view.findViewById(R.id.textView_foodName_commit_dialog_item)).setText(food.getName());
					}
					
					((TextView)view.findViewById(R.id.textView_amount_quickPick_commitDialog_item)).setText(NumericUtil.float2String2(food.getCount()));
					((TextView)view.findViewById(R.id.textView_price_quickPick_commitDialog_item)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(food.calcPriceWithTaste()));
					return view;
				}
           	});
		}
		
		/**
		 * 执行请求对应餐台的账单信息 
		 */
		private class QueryAndCommitOrderTask extends com.wireless.lib.task.QueryOrderTask{

			private ProgressDialog mProgDialog;
		
			private final byte mReserved;
			
			QueryAndCommitOrderTask(int tableAlias){
				super(WirelessOrder.pinGen, tableAlias, WirelessOrder.foodMenu);
				this.mReserved = ReqInsertOrder.DO_PRINT;
			}
			
			QueryAndCommitOrderTask(int tableAlias, byte reserved){
				super(WirelessOrder.pinGen, tableAlias, WirelessOrder.foodMenu);
				this.mReserved = ReqInsertOrder.DO_NOT_PRINT;
			}
			
			/**
			 * 在执行请求删单操作前显示提示信息
			 */
			@Override
			protected void onPreExecute(){
				mProgDialog = ProgressDialog.show(QuickPickActivity.this, "", "查询" + mTblAlias + "号餐台的信息...请稍候", true);
			}
			
			/**
			 * 根据返回的error message判断，如果发错异常则提示用户，
			 * 如果成功，则迁移到改单页面
			 */
			@Override
			protected void onPostExecute(Order order){
				
				mProgDialog.dismiss();

//				int customAmount = Integer.parseInt(((TextView)CommitDialog.this.findViewById(R.id.textView_peopleCnt_commitDialog)).getText().toString());

				if(mBusinessException != null){ 
					if(mBusinessException.getErrCode() == ErrorCode.ORDER_NOT_EXIST){				
							
						//Perform to insert a new order in case of the table is IDLE.
						mOrderToCommit = new Order(mNewFoodLstView.getSourceData(), mTblAlias, 1);
						new InsertOrderTask(mOrderToCommit, Type.INSERT_ORDER, mReserved).execute();						
						
					}else{
						new AlertDialog.Builder(QuickPickActivity.this)
						.setTitle("提示")
						.setMessage(mBusinessException.getMessage())
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						})
						.show();
					}
				}else{
					//Merge the original order and update if the table is BUSY.
					order.addFoods(mNewFoodLstView.getSourceData());
					mOrderToCommit = order;
					new InsertOrderTask(mOrderToCommit, Type.UPDATE_ORDER, mReserved).execute();
				}
			}
		}
		
		/**
		 * 执行下单的请求操作
		 */
		private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask{

			private ProgressDialog mProgDialog;
			
			public InsertOrderTask(Order reqOrder, byte type, byte reserved) {
				super(WirelessOrder.pinGen, reqOrder, type, reserved);
			}
			
			/**
			 * 在执行请求下单操作前显示提示信息
			 */
			@Override
			protected void onPreExecute(){
				mProgDialog = ProgressDialog.show(QuickPickActivity.this, "", "提交" + mReqOrder.getDestTbl().getAliasId() + "号餐台的下单信息...请稍候", true);
			}			
			
			/**
			 * 根据返回的error message判断，如果发错异常则提示用户，
			 * 如果成功，则返回到主界面，并提示用户下单成功
			 */
			@Override
			protected void onPostExecute(Void arg){
				//make the progress dialog disappeared
				mProgDialog.dismiss();
				/**
				 * Prompt user message if any error occurred.
				 */
				if(mBusinessException != null){
					new AlertDialog.Builder(QuickPickActivity.this)
					.setTitle("提示")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();
				}else{
					//Perform to pay order in case the flag is true,
					//otherwise back to the main activity and show the message
					if(mIsPayOrder){
						//Set the default discount to committed order.
						for(Discount discount : WirelessOrder.foodMenu.discounts){
							if(discount.isDefault()){
								mOrderToCommit.setDiscount(discount);
								break;
							}
						}
						//TODO
						new QueryOrderTask2(mOrderToCommit.getDestTbl().getAliasId()).execute();
						
					}else{
						dismiss();
						QuickPickActivity.this.finish();						
						Toast.makeText(QuickPickActivity.this, mReqOrder.getDestTbl().getAliasId() + "号台下单成功。", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}	
		
		private class QueryOrderTask2 extends com.wireless.lib.task.QueryOrderTask{
			
			public QueryOrderTask2(int tableAlias) {
				super(WirelessOrder.pinGen, tableAlias, WirelessOrder.foodMenu);
			}

			private ProgressDialog mProgressDialog;
			@Override
			protected void onPreExecute(){
				mProgressDialog = ProgressDialog.show(QuickPickActivity.this, "", "查询" + mTblAlias + "号餐台的信息...请稍候", true);
			}
			
			@Override
			protected void onPostExecute(Order result) {
				super.onPostExecute(result);
				mProgressDialog.dismiss();
				
				if(mBusinessException != null){
					new AlertDialog.Builder(QuickPickActivity.this)
					.setTitle(mBusinessException.getMessage())
					.setMessage("菜品已添加，但结账请求失败，是否重试？")
					.setPositiveButton("重试", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new QueryOrderTask2(mTblAlias).execute();
						}
					})
					.setNegativeButton("退出", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismiss();
							finish();
						}
					}).show();
				}
				else {
					new PayOrderTask(result, ReqPayOrder.PAY_CATE_NORMAL).execute();
				}
			}
		}
		/**
		 * 执行结帐请求操作
		 */
		private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

			private ProgressDialog mProgDialog;

			PayOrderTask(Order order, byte payCate) {
				super(WirelessOrder.pinGen, order, payCate);
			}

			/**
			 * 在执行请求结帐操作前显示提示信息
			 */
			@Override
			protected void onPreExecute() {
				mProgDialog = ProgressDialog.show(QuickPickActivity.this, 
												  "", 
												  "提交"	+ mOrderToPay.getDestTbl().getAliasId() + "号台" + 
												 (mPayCate == ReqPayOrder.PAY_CATE_NORMAL ? "结帐"	: "暂结") + "信息...请稍候",
												 true);
			}


			/**
			 * 根据返回的error message判断，如果发错异常则提示用户， 如果成功，则返回到主界面，并提示用户结帐成功
			 */
			@Override
			protected void onPostExecute(Void arg) {
				mProgDialog.dismiss();

				if (mBusinessException != null) {
					new AlertDialog.Builder(QuickPickActivity.this)
					.setTitle(mBusinessException.getMessage())
					.setMessage("菜品已添加，但结账请求失败，是否重试？")
					.setPositiveButton("重试", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new PayOrderTask(mOrderToPay, mPayCate).execute();
						}
					})
					.setNegativeButton("退出", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismiss();
							finish();
						}
					}).show();

				} else {

					Toast.makeText(QuickPickActivity.this, 
								  mOrderToPay.getDestTbl().getAliasId()	+ "号台提交并" + (mPayCate == ReqPayOrder.PAY_CATE_NORMAL ? "结帐" : "暂结") + "成功", 
								  Toast.LENGTH_SHORT).show();
					dismiss();
					QuickPickActivity.this.finish();	
				}
			}
		}
		
		
	}

	/**
	 * 请求更新沽清菜品
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		
		QuerySellOutTask(){
			super(WirelessOrder.pinGen, WirelessOrder.foodMenu.foods);
		}
		
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mProtocolException != null){
				Toast.makeText(QuickPickActivity.this, "沽清菜品更新失败", Toast.LENGTH_SHORT).show();				
			}else{
				//mViewHandler.sendEmptyMessage(mLastView);
				Toast.makeText(QuickPickActivity.this, "沽清菜品更新成功", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void allMarkClick() {
		Intent intent = new Intent(this, PickTasteActivity.class);
		Bundle bundle = new Bundle(); 
		OrderFood dummyFood = new OrderFood();
		dummyFood.setName("全单备注");
		bundle.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(dummyFood));
		bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
		bundle.putBoolean(PickTasteActivity.PICK_ALL_ORDER_TASTE, true);
		intent.putExtras(bundle);
		startActivityForResult(intent, OrderActivity.ALL_ORDER_REMARK);
	}
	
}

