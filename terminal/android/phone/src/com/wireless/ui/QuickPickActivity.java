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
import com.wireless.excep.BusinessException;
import com.wireless.fragment.KitchenFragment;
import com.wireless.fragment.PickFoodFragment;
import com.wireless.pack.ErrorCode;
import com.wireless.pack.Type;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Discount;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;
import com.wireless.ui.view.OrderFoodListView;
import com.wireless.ui.view.OrderFoodListView.AllMarkClickListener;
import com.wireless.ui.view.OrderFoodListView.OnChangedListener;
import com.wireless.util.NumericUtil;

public class QuickPickActivity extends FragmentActivity implements 
							com.wireless.ui.view.OrderFoodListView.OnOperListener, OnFoodPickedListener,
							AllMarkClickListener
{
	//ÿ����˷�ʽ�ı�ǩ
	private static final int NUMBER_FRAGMENT = 6320;
	private static final int KITCHEN_FRAGMENT = 6321;
	private static final int PINYIN_FRAGMENT = 6322;
	private static final int PICKED_FOOD_INTERFACE = 6323;
	private int mLastView;

	//activity���ر�ǩ
	private final static int PICK_WITH_TASTE = 7755;
	
	//�����ѵ�˵��б�
	//private ArrayList<OrderFood> mPickFoods = new ArrayList<OrderFood>();
	

	private TextHandler mTextHandler;
	
	private OrderFoodListView mNewFoodLstView;
	
	/**
	 * ˢ���µ����ʾ��Handler
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
			//�����ѵ��ListView�����ͺ�������
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
			//ˢ���µ��List����ʾ�����ͽ��
			Order order = new Order(activity.mNewFoodLstView.getSourceData());
			mTotalCnt.setText(String.valueOf(order.getOrderFoods().length));
			mTotalPrice.setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String(order.calcTotalPrice()));
		}		

	}
	
	//ˢ��ÿ��view��handler
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
				//�����²�Ʒѡ��fragment
				PickFoodFragment numFragment = new PickFoodFragment();
				numFragment.setFoodPickedListener(activity);
				//������ʾ����
				Bundle args = new Bundle();
				args.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_NUMBER);
				args.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "������������");
				numFragment.setArguments(args);
				//�滻ԭ����fragment
				ftrans.replace(R.id.frameLayout_container_quickPick, numFragment).commit();
				activity.mLastView = NUMBER_FRAGMENT;
				mTitleTextView.setText("��� - ���");
				setLastCate(NUMBER_FRAGMENT);
				
				break;
				
			case KITCHEN_FRAGMENT:
				
				KitchenFragment kitchenFragment = new KitchenFragment();
				kitchenFragment.setFoodPickedListener(activity);
				ftrans.replace(R.id.frameLayout_container_quickPick, kitchenFragment).commit();
					
				activity.mLastView = KITCHEN_FRAGMENT;
				
				mTitleTextView.setText("��� - �ֳ�");
				setLastCate(KITCHEN_FRAGMENT);
				break;
				
			case PINYIN_FRAGMENT:
				//�����²�Ʒѡ��fragment
				PickFoodFragment spellFragment = new PickFoodFragment();
				spellFragment.setFoodPickedListener(activity);
				//������ʾ����
				Bundle spellAargs = new Bundle();
				spellAargs.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_SPELL);
				spellAargs.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "������ƴ������");
				spellFragment.setArguments(spellAargs);
				//�滻ԭ����fragment
				ftrans.replace(R.id.frameLayout_container_quickPick, spellFragment).commit();
				
				activity.mLastView = PINYIN_FRAGMENT;
				
				mTitleTextView.setText("��� - ƴ��");
				setLastCate(PINYIN_FRAGMENT);
				break;
				
			case PICKED_FOOD_INTERFACE:
				//��fragment�������أ���ʾ�ѵ�˽���
				mFgmContainer.setVisibility(View.GONE);
				((RelativeLayout) activity.findViewById(R.id.relativeLayout_bottom_revealFood_quickPick)).setVisibility(View.VISIBLE);
				mTitleTextView.setText("�ѵ��");
				
				activity.mLastView = PICKED_FOOD_INTERFACE;
				
				//չ���µ��ListView
				activity.mNewFoodLstView.expandGroup(0);
				activity.mTextHandler.sendEmptyMessage(0);
				setLastCate(PICKED_FOOD_INTERFACE);
				break;
			}
		}
		
		private void setLastCate(int cate){
			
			QuickPickActivity activity = mActivity.get();
			//��ԭ����ʽ
			mNumBtn.setImageResource(R.drawable.number_btn);
			mKitchenBtn.setImageResource(R.drawable.kitchen);
			mSpellBtn.setImageResource(R.drawable.pinyin);
			mPickedBtn.setImageResource(R.drawable.picked_food);
			//�л���˷�ʽʱ�����浱ǰ�ĵ��ģʽ
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
		new QuerySellOutTask().execute(WirelessOrder.foodMenu.foods);
		
		mNewFoodLstView = (OrderFoodListView)findViewById(R.id.orderFoodListView_revealFood_quickPick);
		mNewFoodLstView.init(Type.INSERT_ORDER);
		mNewFoodLstView.setAllMarkClickListener(this);
		
		mViewHandler = new ViewHandler(this);
		mTextHandler = new TextHandler(this);
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		//����Button
		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("����");
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
		right.setText("�ύ");
		right.setVisibility(View.VISIBLE);
		
		//�ύ��ť
		ImageButton commit = (ImageButton) findViewById(R.id.btn_right);
		commit.setVisibility(View.VISIBLE);
		commit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//��δ��ˣ�����ʾ��
				if(mNewFoodLstView.getSourceData().length != 0){
					CommitDialog dialog = new CommitDialog(QuickPickActivity.this);
					dialog.setTitle("�������̨�Ż�˶Ե����Ϣ");
					dialog.show();
					
				}else{
					Toast.makeText(getApplicationContext(), "����δ���", Toast.LENGTH_SHORT).show();
				}
			}
		});

		//���
		((ImageButton) findViewById(R.id.imageButton_num_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != NUMBER_FRAGMENT){
					mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
				}
			}
		});
		
		//�ֳ�
		((ImageButton) findViewById(R.id.imageButton_kitchen_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != KITCHEN_FRAGMENT){
					mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
				}
			}
		});
		
		//ƴ��
		((ImageButton) findViewById(R.id.imageButton_spell_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mLastView != PINYIN_FRAGMENT){
					mViewHandler.sendEmptyMessage(PINYIN_FRAGMENT);
				}
			}
		});
		
		//�ѵ��
		((ImageButton) findViewById(R.id.imageButton_remark_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(PICKED_FOOD_INTERFACE);
			}
		});

		/*
		 * �����ϴα���ļ�¼���л�����Ӧ�ĵ�˷�ʽ
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
				.setTitle("�˳�ȷ��")
				.setMessage("�ѵ����δ�ύ��ȷ��Ҫ�˳���")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener(){
					@Override
					public void onClick( DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				})
				.setNegativeButton("ȡ��", null).show();
		}
	}

	//activity���غ󽫲�Ʒ��ӽ��ѵ����
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			FoodParcel foodParcel;
			switch (requestCode) {
			case PICK_WITH_TASTE:
				
				 //��ӿ�ζ����ӵ�pickList��
				foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				addFood(foodParcel);
				
				break;
				
			case OrderFoodListView.PICK_TASTE:
				
				 //��ζ�ı�ʱ֪ͨListView���и���
				foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				mNewFoodLstView.setFood(foodParcel);				

				break;
			case OrderActivity.ALL_ORDER_REMARK:
				
				foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				if(foodParcel.hasTaste()){
					Taste[] tempTastes = foodParcel.getTasteGroup().getNormalTastes();
					mNewFoodLstView.setAllTaste(tempTastes);
				}
				break;
			}
		}
	}
	
	/**
	 * ͨ��"���"��"�ֳ�"��"ƴ��"��ʽѡ�в�Ʒ�� ����Ʒ���浽List��
	 * 
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
	 */
	@Override
	public void onPicked(OrderFood food) {
		addFood(food);
	}

	/**
	 * ͨ��"���"��"�ֳ�"��"ƴ��"��ʽѡ�в�Ʒ�� ����Ʒ���浽List�У�����ת����ζActivityѡ���ζ
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
	 */
	@Override
	public void onPickedWithTaste(OrderFood food, boolean isTempTaste) {
		Intent intent = new Intent(this, PickTasteActivity.class);
		Bundle bundle = new Bundle(); 
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
		
		if(isTempTaste)
			bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_PINZHU);
		else bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
		
		intent.putExtras(bundle);
		startActivityForResult(intent, PICK_WITH_TASTE);
	}
	
	/**
	 * ���ѵ���б���ѡ��ĳ����Ʒ��ѡ���ζ��������ת����ζActivity
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
	 */
	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemp()){
			Toast.makeText(this, "��ʱ�˲�����ӿ�ζ", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(QuickPickActivity.this, PickTasteActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(selectedFood));
			intent.putExtras(bundle);
			startActivityForResult(intent, OrderFoodListView.PICK_TASTE);			
		}		
	}

	/**
	 * �����˰�ť����ת���ϴε�˷�ʽ��Tab
	 */
	@Override
	public void onPickFood() {
		/*
		 * �����ϴα���ļ�¼���л�����Ӧ�ĵ�˷�ʽ
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
	 * ��Ӳ�Ʒ���ѵ�˵�List��
	 * 
	 * @param food
	 *            ѡ�еĲ�Ʒ��Ϣ
	 */
	private void addFood(OrderFood food) {

		try{

			mNewFoodLstView.addFood(food);
			
			Toast.makeText(this, "���"	+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "������\"" : "\"") + food.toString() + "\"" +
								 NumericUtil.float2String2(food.getCount()) + "��", Toast.LENGTH_SHORT)	.show();
			
			
		}catch(BusinessException e){
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}


	
	/**
	 * �������е��ύDialog
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

			//���öԻ��򳤿�
			final LayoutParams lp = getWindow().getAttributes();
			lp.height = 660;
			lp.width = LayoutParams.MATCH_PARENT;
			getWindow().setAttributes(lp);
			
           	final AutoCompleteTextView tableText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_commitDialog);
           	
			//���������
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
			final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
           	imm.showSoftInput(tableText, 0); //��ʾ�����
           	imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
           	
           	//�޸İ�ť
           	final Button changeBtn = (Button) findViewById(R.id.button_changeOrder_commitDialog);
           	changeBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					mViewHandler.sendEmptyMessage(PICKED_FOOD_INTERFACE);
				}
			});
           	//�ύ�����˰�ť  
           	Button commitBtn = (Button) findViewById(R.id.button_commitDialog_payBill);
           	commitBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIsPayOrder = true;
					try{
						short tableAlias = Short.parseShort(tableText.getText().toString());
						new QueryOrderTask(tableAlias).execute(WirelessOrder.foodMenu);
					}catch(NumberFormatException e){
						Toast.makeText(QuickPickActivity.this, "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
					}
				}
			});
           	
//           	final View line = (View) findViewById(R.id.view1);
//           	((Button)this.findViewById(R.id.button_detail_commit_dialog)).setOnClickListener(new View.OnClickListener(){
//				@Override
//				public void onClick(View v) {
//					//�ж�listview״̬���������
//					if(mListView.isShown())
//					{
//						//��ʾ������
//						mListView.setVisibility(View.GONE);
//						changeBtn.setVisibility(View.GONE);
//						line.setVisibility(View.GONE);
//						//���öԻ��������Ӧ
//						lp.height = LayoutParams.WRAP_CONTENT;
//						lp.width = 440;
//						getWindow().setAttributes(lp);
//					}
//					else {
//						imm.hideSoftInputFromWindow(tableText.getWindowToken(), 0);
//						mListView.requestFocus();
//						mListView.setVisibility(View.VISIBLE);
//						changeBtn.setVisibility(View.VISIBLE);
//						line.setVisibility(View.VISIBLE);
//						//���öԻ��򳤿�Ϊ600px
//						lp.height = 600;
//						lp.width = 440;
//						getWindow().setAttributes(lp);
//					}
//				}
//           	});
           	
           	//ȡ����ť
        	((Button)this.findViewById(R.id.button_cancel_commitDialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();					
				}
			});
        	
//        	final TextView peopleCountTextView = (TextView)findViewById(R.id.textView_peopleCnt_commitDialog);
        	//ȷ����ť
        	((Button)this.findViewById(R.id.button_confirm_commitDialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					if(mNewFoodLstView.getSourceData().length > 0){
						mIsPayOrder = false;
						try{
							short tableAlias = Short.parseShort(tableText.getText().toString());
							new QueryOrderTask(tableAlias).execute(WirelessOrder.foodMenu);
						}catch(NumberFormatException e){
							Toast.makeText(QuickPickActivity.this, "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
						}
						
					}else{
						Toast.makeText(QuickPickActivity.this, "����û�е��", Toast.LENGTH_SHORT).show();						
					}
				}
			});
           	
           	mListView = (ListView) this.findViewById(R.id.listView_commitDialog);
           	
           	//������������ʱ���ؼ���
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
		 * ִ�������Ӧ��̨���˵���Ϣ 
		 */
		private class QueryOrderTask extends com.wireless.lib.task.QueryOrderTask{

			private ProgressDialog mProgDialog;
		
			QueryOrderTask(int tableAlias){
				super(tableAlias);
			}
			
			/**
			 * ��ִ������ɾ������ǰ��ʾ��ʾ��Ϣ
			 */
			@Override
			protected void onPreExecute(){
				mProgDialog = ProgressDialog.show(QuickPickActivity.this, "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
			}
			
			/**
			 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
			 * ����ɹ�����Ǩ�Ƶ��ĵ�ҳ��
			 */
			@Override
			protected void onPostExecute(Order order){
				
				mProgDialog.dismiss();

//				int customAmount = Integer.parseInt(((TextView)CommitDialog.this.findViewById(R.id.textView_peopleCnt_commitDialog)).getText().toString());

				if(mBusinessException != null){ 
					if(mBusinessException.getErrCode() == ErrorCode.ORDER_NOT_EXIST){				
							
						//Perform to insert a new order in case of the table is IDLE.
						mOrderToCommit = new Order(mNewFoodLstView.getSourceData(), mTblAlias, 1);
						new InsertOrderTask(mOrderToCommit).execute(Type.INSERT_ORDER);						
						
					}else{
						new AlertDialog.Builder(QuickPickActivity.this)
						.setTitle("��ʾ")
						.setMessage(mBusinessException.getMessage())
						.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
					new InsertOrderTask(mOrderToCommit).execute(Type.UPDATE_ORDER);
				}
			}
		}
		
		/**
		 * ִ���µ����������
		 */
		private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask{

			private ProgressDialog mProgDialog;
			
			public InsertOrderTask(Order reqOrder) {
				super(reqOrder);
			}
			
			/**
			 * ��ִ�������µ�����ǰ��ʾ��ʾ��Ϣ
			 */
			@Override
			protected void onPreExecute(){
				mProgDialog = ProgressDialog.show(QuickPickActivity.this, "", "�ύ" + mReqOrder.getDestTbl().getAliasId() + "�Ų�̨���µ���Ϣ...���Ժ�", true);
			}			
			
			/**
			 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
			 * ����ɹ����򷵻ص������棬����ʾ�û��µ��ɹ�
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
					.setTitle("��ʾ")
					.setMessage(mBusinessException.getMessage())
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
						new QueryOrderTask2(mOrderToCommit.getSrcTbl().getAliasId()).execute(WirelessOrder.foodMenu);
						
					}else{
						dismiss();
						QuickPickActivity.this.finish();						
						Toast.makeText(QuickPickActivity.this, mReqOrder.getDestTbl().getAliasId() + "��̨�µ��ɹ���", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}	
		
		private class QueryOrderTask2 extends com.wireless.lib.task.QueryOrderTask{
			
			public QueryOrderTask2(int tableAlias) {
				super(tableAlias);
			}

			private ProgressDialog mProgressDialog;
			@Override
			protected void onPreExecute(){
				mProgressDialog = ProgressDialog.show(QuickPickActivity.this, "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
			}
			
			@Override
			protected void onPostExecute(Order result) {
				super.onPostExecute(result);
				mProgressDialog.dismiss();
				
				if(mBusinessException != null){
					new AlertDialog.Builder(QuickPickActivity.this)
					.setTitle(mBusinessException.getMessage())
					.setMessage("��Ʒ����ӣ�����������ʧ�ܣ��Ƿ����ԣ�")
					.setPositiveButton("����", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new QueryOrderTask2(mTblAlias).execute(WirelessOrder.foodMenu);
						}
					})
					.setNegativeButton("�˳�", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismiss();
							finish();
						}
					}).show();
				}
				else {
					new PayOrderTask(result, PayOrderTask.PAY_NORMAL_ORDER).execute();
				}
			}
		}
		/**
		 * ִ�н����������
		 */
		private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

			private ProgressDialog mProgDialog;

			PayOrderTask(Order order, int payCate) {
				super(order, payCate);
			}

			/**
			 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
			 */
			@Override
			protected void onPreExecute() {
				mProgDialog = ProgressDialog.show(QuickPickActivity.this, 
												  "", 
												  "�ύ"	+ mOrderToPay.getDestTbl().getAliasId() + "��̨" + 
												 (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "����"	: "�ݽ�") + "��Ϣ...���Ժ�",
												 true);
			}


			/**
			 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û��� ����ɹ����򷵻ص������棬����ʾ�û����ʳɹ�
			 */
			@Override
			protected void onPostExecute(Void arg) {
				mProgDialog.dismiss();

				if (mBusinessException != null) {
					new AlertDialog.Builder(QuickPickActivity.this)
					.setTitle(mBusinessException.getMessage())
					.setMessage("��Ʒ����ӣ�����������ʧ�ܣ��Ƿ����ԣ�")
					.setPositiveButton("����", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							new PayOrderTask(mOrderToPay, mPayCate).execute();
						}
					})
					.setNegativeButton("�˳�", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismiss();
							finish();
						}
					}).show();

				} else {

					Toast.makeText(QuickPickActivity.this, 
								  mOrderToPay.getDestTbl().getAliasId()	+ "��̨�ύ��" + (mPayCate == PayOrderTask.PAY_NORMAL_ORDER ? "����" : "�ݽ�") + "�ɹ�", 
								  Toast.LENGTH_SHORT).show();
					dismiss();
					QuickPickActivity.this.finish();	
				}
			}
		}
		
		
	}

	/**
	 * ������¹����Ʒ
	 */
	private class QuerySellOutTask extends com.wireless.lib.task.QuerySellOutTask{
		@Override
		protected void onPostExecute(Food[] sellOutFoods){
			if(mErrMsg != null){
				Toast.makeText(QuickPickActivity.this, "�����Ʒ����ʧ��", Toast.LENGTH_SHORT).show();				
			}else{
				//mViewHandler.sendEmptyMessage(mLastView);
				Toast.makeText(QuickPickActivity.this, "�����Ʒ���³ɹ�", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void allMarkClick() {
		Intent intent = new Intent(this, PickTasteActivity.class);
		Bundle bundle = new Bundle(); 
		OrderFood dummyFood = new OrderFood();
		dummyFood.setName("ȫ����ע");
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(dummyFood));
		bundle.putString(PickTasteActivity.INIT_TAG, PickTasteActivity.TAG_TASTE);
		bundle.putBoolean(PickTasteActivity.PICK_ALL_ORDER_TASTE, true);
		intent.putExtras(bundle);
		startActivityForResult(intent, OrderActivity.ALL_ORDER_REMARK);
	}
	
}

