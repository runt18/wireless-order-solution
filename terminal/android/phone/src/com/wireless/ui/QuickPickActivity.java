package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.ProtocolError;
import com.wireless.fragment.KitchenFragment;
import com.wireless.fragment.OrderFoodFragment;
import com.wireless.fragment.PickFoodFragment;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqInsertOrder;
import com.wireless.pack.req.ReqPayOrder;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.dialog.AskOrderAmountDialog.OnFoodPickedListener;

public class QuickPickActivity extends FragmentActivity implements OnFoodPickedListener{
	//ÿ����˷�ʽ�ı�ǩ
	private static final int NUMBER_FRAGMENT = 6320;
	private static final int KITCHEN_FRAGMENT = 6321;
	private static final int PINYIN_FRAGMENT = 6322;
	private static final int ORDER_FOOD_FRAGMENT = 6323;
	private int mLastView;

	
	//ˢ��ÿ��view��handler
	private ViewHandler mViewHandler;
	
	private static class ViewHandler extends Handler{
		private WeakReference<QuickPickActivity> mActivity;
		
		private TextView mTitleTextView;
		private ImageButton mNumBtn;
		private ImageButton mKitchenBtn;
		private ImageButton mSpellBtn;
		private ImageButton mPickedBtn;

		ViewHandler(QuickPickActivity activity){
			mActivity = new WeakReference<QuickPickActivity>(activity);
			mTitleTextView = (TextView) activity.findViewById(R.id.toptitle);
			mTitleTextView.setVisibility(View.VISIBLE);
			
			mNumBtn = (ImageButton) activity.findViewById(R.id.imageButton_num_quickPick);
			mKitchenBtn = (ImageButton) activity.findViewById(R.id.imageButton_kitchen_quickPick);
			mSpellBtn = (ImageButton) activity.findViewById(R.id.imageButton_spell_quickPick);
			mPickedBtn = (ImageButton) activity.findViewById(R.id.imageButton_remark_quickPick);
			
		}
		
		@Override
		public void handleMessage(Message msg) {
			final QuickPickActivity activity = mActivity.get();
			FragmentTransaction fgmTrans = activity.getSupportFragmentManager().beginTransaction();
			
			switch(msg.what)
			{
			case NUMBER_FRAGMENT:
				//��ʾ���ѡ���Ʒ��Fragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.INVISIBLE);
				fgmTrans.replace(R.id.frameLayout_container_quickPick, PickFoodFragment.newInstanceByNum(), Integer.toString(NUMBER_FRAGMENT)).commit();
				
				activity.mLastView = NUMBER_FRAGMENT;
				mTitleTextView.setText("��� - ���");
				setLastCate(NUMBER_FRAGMENT);
				
				break;
				
			case KITCHEN_FRAGMENT:
				
				//��ʾ�ֳ�ѡ���Ʒ��Fragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.INVISIBLE);
				fgmTrans.replace(R.id.frameLayout_container_quickPick, new KitchenFragment(), Integer.toString(KITCHEN_FRAGMENT)).commit();
				
				activity.mLastView = KITCHEN_FRAGMENT;
				mTitleTextView.setText("��� - �ֳ�");
				setLastCate(KITCHEN_FRAGMENT);
				break;
				
			case PINYIN_FRAGMENT:
				//��ʾƴ��ѡ���Ʒ��Fragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.VISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.INVISIBLE);
				fgmTrans.replace(R.id.frameLayout_container_quickPick, PickFoodFragment.newInstanceByPinyin(), Integer.toString(PINYIN_FRAGMENT)).commit();
				
				activity.mLastView = PINYIN_FRAGMENT;
				mTitleTextView.setText("��� - ƴ��");
				setLastCate(PINYIN_FRAGMENT);
				break;
				
			case ORDER_FOOD_FRAGMENT:
				//����OrderFoodFragment
				activity.findViewById(R.id.frameLayout_container_quickPick).setVisibility(View.INVISIBLE);
				activity.findViewById(R.id.frameLayout_orderFood_quickPick).setVisibility(View.VISIBLE);
				
				//OrderFoodFragment��"���"Button
//				ExpandableListView listView = (ExpandableListView)((OrderFoodFragment)activity.getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).getView().findViewById(R.id.expandableListView_orderActivity);
//				listView.getExpandableListAdapter()
//				orderImgView.setOnClickListener(new OnClickListener(){
//					@Override
//					public void onClick(View v) {
//						activity.switchToOrderView();
//					}
//					
//				});
				
				activity.mLastView = ORDER_FOOD_FRAGMENT;
				mTitleTextView.setText("�ѵ��");
				setLastCate(ORDER_FOOD_FRAGMENT);
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
			case ORDER_FOOD_FRAGMENT:
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
		
		mViewHandler = new ViewHandler(this);
		
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
		
		//�ύ��ť
		TextView right = (TextView) findViewById(R.id.textView_right);
		right.setText("�ύ");
		right.setVisibility(View.VISIBLE);
		
		ImageButton commit = (ImageButton) findViewById(R.id.btn_right);
		commit.setVisibility(View.VISIBLE);
		commit.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//��δ��ˣ�����ʾ��
				if(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).hasNewOrderFood()){
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
				mViewHandler.sendEmptyMessage(ORDER_FOOD_FRAGMENT);
			}
		});

		switchToOrderView();
	}

	@Override
	public void onBackPressed() {
		
		if(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).hasNewOrderFood()){
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
		}else{
			super.onBackPressed();
		}
	}

	private void switchToOrderView(){
		//�����ϴα���ļ�¼���л�����Ӧ�ĵ�˷�ʽ
		int lastPickCate = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getInt(Params.LAST_PICK_CATE, Params.PICK_BY_PINYIN);
		switch(lastPickCate){
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
	
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data){
//		if(resultCode == Activity.RESULT_OK){			
//			if(requestCode == OrderFoodFragment.PICK_TASTE){
//				//��ζ�ı�ʱ֪ͨListView���и���
//				OrderFoodParcel foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
//				((OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(OrderFoodFragment.TAG)).addFood(foodParcel.asOrderFood());
//				
//			}else if(requestCode == OrderFoodFragment.PICK_FOOD){
//				//ѡ�˸ı�ʱ֪ͨ�µ�˵�ListView���и���
//				OrderParcel orderParcel = data.getParcelableExtra(OrderParcel.KEY_VALUE);
//				((OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(OrderFoodFragment.TAG)).addFoods(orderParcel.asOrder().getOrderFoods());
//				
//			}
//		}
//	}
	
//	//activity���غ󽫲�Ʒ��ӽ��ѵ����
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		if (resultCode == RESULT_OK) {
//			OrderFoodParcel foodParcel;
//			switch (requestCode) {
//			case PICK_WITH_TASTE:
//				
//				 //��ӿ�ζ����ӵ�pickList��
//				foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
//				addFood(foodParcel.asOrderFood());
//				
//				break;
//				
//			case OrderFoodListView.PICK_TASTE:
//				
//				 //��ζ�ı�ʱ֪ͨListView���и���
//				foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
//				mNewFoodLstView.setFood(foodParcel.asOrderFood());				
//
//				break;
//			case OrderActivity.ALL_ORDER_REMARK:
//				
//				foodParcel = data.getParcelableExtra(OrderFoodParcel.KEY_VALUE);
//				if(foodParcel.asOrderFood().hasTaste()){
//					mNewFoodLstView.setAllTaste(foodParcel.asOrderFood().getTasteGroup().getNormalTastes());
//				}
//				break;
//			}
//		}
//	}
	
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
           	
           	//�ύ����ӡ��ť
           	final Button changeBtn = (Button) findViewById(R.id.button_changeOrder_commitDialog);
           	changeBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mIsPayOrder = false;
					try{
						short tableAlias = Short.parseShort(tableText.getText().toString());
						new QueryAndCommitOrderTask(tableAlias, ReqInsertOrder.DO_NOT_PRINT).execute();
					}catch(NumberFormatException e){
						Toast.makeText(QuickPickActivity.this, "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
					}
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
						new QueryAndCommitOrderTask(tableAlias).execute();
					}catch(NumberFormatException e){
						Toast.makeText(QuickPickActivity.this, "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
					}
				}
			});
           	
           	//ȡ����ť
        	((Button)this.findViewById(R.id.button_cancel_commitDialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();					
				}
			});
        	
        	//ȷ����ť
        	((Button)this.findViewById(R.id.button_confirm_commitDialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					if(((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).hasNewOrderFood()){
						mIsPayOrder = false;
						try{
							short tableAlias = Short.parseShort(tableText.getText().toString());
							new QueryAndCommitOrderTask(tableAlias).execute();
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

           		List<OrderFood> mSrcFoods = ((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).buildRequestOrder(0, 0).getOrderFoods();
           		
				@Override
				public int getCount() {
					return mSrcFoods.size();
				}

				@Override
				public Object getItem(int position) {
					return mSrcFoods.get(position);
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
					
					OrderFood food = mSrcFoods.get(position);
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

				Order reqOrder = ((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).buildRequestOrder(mTblAlias, 1);
				
				if(mBusinessException != null){ 
					if(mBusinessException.getErrCode().equals(ProtocolError.ORDER_NOT_EXIST)){				
							
						//Perform to insert a new order in case of the table is IDLE.
						mOrderToCommit = reqOrder;
						new InsertOrderTask(mOrderToCommit, Type.INSERT_ORDER, mReserved).execute();						
						
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
					order.addFoods(reqOrder.getOrderFoods());
					mOrderToCommit = order;
					new InsertOrderTask(mOrderToCommit, Type.UPDATE_ORDER, mReserved).execute();
				}
			}
		}
		
		/**
		 * ִ���µ����������
		 */
		private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask{

			private ProgressDialog mProgDialog;
			
			public InsertOrderTask(Order reqOrder, byte type, byte reserved) {
				super(WirelessOrder.pinGen, reqOrder, type, reserved);
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
						new QueryOrderTask2(mOrderToCommit.getDestTbl().getAliasId()).execute();
						
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
				super(WirelessOrder.pinGen, tableAlias, WirelessOrder.foodMenu);
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
							new QueryOrderTask2(mTblAlias).execute();
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
					new PayOrderTask(result, ReqPayOrder.PAY_CATE_NORMAL).execute();
				}
			}
		}
		/**
		 * ִ�н����������
		 */
		private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

			private ProgressDialog mProgDialog;

			PayOrderTask(Order order, byte payCate) {
				super(WirelessOrder.pinGen, order, payCate);
			}

			/**
			 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
			 */
			@Override
			protected void onPreExecute() {
				mProgDialog = ProgressDialog.show(QuickPickActivity.this, 
												  "", 
												  "�ύ"	+ mOrderToPay.getDestTbl().getAliasId() + "��̨" + 
												 (mPayCate == ReqPayOrder.PAY_CATE_NORMAL ? "����"	: "�ݽ�") + "��Ϣ...���Ժ�",
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
								  mOrderToPay.getDestTbl().getAliasId()	+ "��̨�ύ��" + (mPayCate == ReqPayOrder.PAY_CATE_NORMAL ? "����" : "�ݽ�") + "�ɹ�", 
								  Toast.LENGTH_SHORT).show();
					dismiss();
					QuickPickActivity.this.finish();	
				}
			}
		}
		
		
	}

	@Override
	public void onFoodPicked(OrderFood food) {
		//((OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(Integer.toString(ORDER_FOOD_FRAGMENT))).addFood(food);
		((OrderFoodFragment)getSupportFragmentManager().findFragmentById(R.id.fgm_orderFood_quickPick)).addFood(food);
	}

}

