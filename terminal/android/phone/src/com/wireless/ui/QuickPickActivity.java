package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
import com.wireless.excep.BusinessException;
import com.wireless.fragment.KitchenFragment;
import com.wireless.fragment.PickFoodFragment;
import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Type;
import com.wireless.protocol.Util;
import com.wireless.ui.view.OrderFoodListView;
import com.wireless.ui.view.OrderFoodListView.OnChangedListener;

public class QuickPickActivity extends FragmentActivity implements com.wireless.fragment.PickFoodFragment.OnFoodPickedListener, 
	com.wireless.fragment.KitchenFragment.OnFoodPickedListener,
	com.wireless.ui.view.OrderFoodListView.OnOperListener{
	//ÿ����˷�ʽ�ı�ǩ
	private static final int NUMBER_FRAGMENT = 6320;
	private static final int KITCHEN_FRAGMENT = 6321;
	private static final int SPELL_FRAGMENT = 6322;
	private static final int PICKED_FOOD_INTERFACE = 6323;

	//activity���ر�ǩ
	private final static int PICK_WITH_TASTE = 7755;
	
	private static final int REFRESH_TOTAL_TEXT = 7734;
	private static final int REFRESH_ALL = 7733;
	private static final int REFRESH_FOODS = 7735;
	
	//�����ѵ�˵��б�
	private ArrayList<OrderFood> mPickFoods = new ArrayList<OrderFood>();

	private TextHandler mTextHandler;
	/*
	 * ˢ���ѵ����ʾ��handler
	 */
	private static class TextHandler extends Handler{
		private WeakReference<QuickPickActivity> mActivity;
		private TextView mTotalCnt;
		private TextView mTotalPrice;
		private OrderFoodListView mOriFoodLstView;
		
		TextHandler(final QuickPickActivity activity)
		{
			mActivity = new WeakReference<QuickPickActivity>(activity);
			mTotalCnt = (TextView) activity.findViewById(R.id.textView_totalCount_revealFood__quickPick);
			mTotalPrice = (TextView) activity.findViewById(R.id.textView_totalPrice_revealFood_quickPick);
			
			mOriFoodLstView = (OrderFoodListView)activity.findViewById(R.id.orderFoodListView_revealFood_quickPick);
			mOriFoodLstView.setOperListener(activity);
			//�����ѵ��listview�����ͺ�������
			mOriFoodLstView.setType(Type.INSERT_ORDER);
			mOriFoodLstView.setChangedListener(new OnChangedListener(){
				@Override
				public void onSourceChanged() {
					activity.mTextHandler.sendEmptyMessage(REFRESH_TOTAL_TEXT);
				}
			});

		}

		@Override
		public void handleMessage(Message msg) {
			QuickPickActivity activity = mActivity.get();
			//���ݲ�ͬ����Ϣˢ��list����ʾ����������ʾ
			switch(msg.what)
			{
			case REFRESH_ALL:
				refreshFoods(activity);
				refreshTotalText(activity);
				break;
			case REFRESH_TOTAL_TEXT:
				refreshTotalText(activity);
				break;
			case REFRESH_FOODS:
				refreshFoods(activity);
				break;
			}
		}
		
		private void refreshFoods(QuickPickActivity activity){
			mOriFoodLstView.notifyDataChanged(activity.mPickFoods);
			mOriFoodLstView.expandGroup(0);
		}
		
		private void refreshTotalText(QuickPickActivity activity)
		{
			//�����������ܼ�
			int totalCount = 0;
			float totalPrice = 0.0f;
			for(OrderFood f:activity.mPickFoods)
			{
				totalCount += f.getCount();
				totalPrice += f.getPriceWithTaste() * f.getCount();
			}
			
			mTotalCnt.setText(""+ totalCount);
			mTotalPrice.setText(Util.float2String((float)Math.round(totalPrice * 100) / 100));
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
		private int LAST_VIEW;

		ViewHandler(QuickPickActivity activity)
		{
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
				if(LAST_VIEW != NUMBER_FRAGMENT)
				{
					//�����²�Ʒѡ��fragment
					PickFoodFragment numFragment = new PickFoodFragment();
					numFragment.setFoodPickedListener(activity);
					//������ʾ����
					Bundle args = new Bundle();
					args.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_NUMBER);
					args.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "��ţ�");
					numFragment.setArguments(args);
					//�滻ԭ����fragment
					ftrans.replace(R.id.frameLayout_container_quickPick, numFragment).commit();
					
					LAST_VIEW = NUMBER_FRAGMENT;
				}
				mTitleTextView.setText("��� - ���");
				setLastCate(NUMBER_FRAGMENT);
				break;
				
			case KITCHEN_FRAGMENT:
				if(LAST_VIEW != KITCHEN_FRAGMENT)
				{
					KitchenFragment kitchenFragment = new KitchenFragment();
					kitchenFragment.setFoodPickedListener(activity);
					ftrans.replace(R.id.frameLayout_container_quickPick, kitchenFragment).commit();
					
					LAST_VIEW = KITCHEN_FRAGMENT;
				}
				mTitleTextView.setText("��� - �ֳ�");
				setLastCate(KITCHEN_FRAGMENT);
				break;
			case SPELL_FRAGMENT:
				if(LAST_VIEW != SPELL_FRAGMENT)
				{
					//�����²�Ʒѡ��fragment
					PickFoodFragment spellFragment = new PickFoodFragment();
					spellFragment.setFoodPickedListener(activity);
					//������ʾ����
					Bundle spellAargs = new Bundle();
					spellAargs.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG, PickFoodFragment.PICK_FOOD_FRAGMENT_SPELL);
					spellAargs.putString(PickFoodFragment.PICK_FOOD_FRAGMENT_TAG_NAME, "ƴ����");
					spellFragment.setArguments(spellAargs);
					//�滻ԭ����fragment
					ftrans.replace(R.id.frameLayout_container_quickPick, spellFragment).commit();
					LAST_VIEW = SPELL_FRAGMENT;
				}
				
				mTitleTextView.setText("��� - ƴ��");
				setLastCate(SPELL_FRAGMENT);
				break;
			case PICKED_FOOD_INTERFACE:
				//��fragment�������أ���ʾ�ѵ�˽���
				mFgmContainer.setVisibility(View.GONE);
				((RelativeLayout) activity.findViewById(R.id.relativeLayout_bottom_revealFood_quickPick)).setVisibility(View.VISIBLE);
				mTitleTextView.setText("�ѵ��");
				//ˢ���ѵ�����ݺͰ�ť״̬
				activity.mTextHandler.sendEmptyMessage(REFRESH_ALL); 
				setLastCate(PICKED_FOOD_INTERFACE);
				break;
			}
		}
		
		private void setLastCate(int cate)
		{
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
			case SPELL_FRAGMENT:
				editor.putInt(Params.LAST_PICK_CATE, Params.PICK_BY_PINYIN);
				mSpellBtn.setImageResource(R.drawable.pinyin_down);
				break;
			case PICKED_FOOD_INTERFACE:
				mPickedBtn.setImageResource(R.drawable.picked_food_down);
			}
			editor.commit();
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quick_pick);
		
		mViewHandler = new ViewHandler(this);
		mTextHandler = new TextHandler(this);
		
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
				finish();
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
				if(mPickFoods.size() != 0)
				{
					CommitDialog dialog = new CommitDialog(QuickPickActivity.this, android.R.style.Theme_Dialog);
					dialog.setTitle("�������̨�Ż�˶Ե����Ϣ");
					dialog.show();
				}
				else Toast.makeText(getApplicationContext(), "����δ���", Toast.LENGTH_SHORT).show();
			}
		});

		//���
		((ImageButton) findViewById(R.id.imageButton_num_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
			}
		});
		//�ֳ�
		((ImageButton) findViewById(R.id.imageButton_kitchen_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
			}
		});
		
		//ƴ��
		((ImageButton) findViewById(R.id.imageButton_spell_quickPick)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mViewHandler.sendEmptyMessage(SPELL_FRAGMENT);
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
		int lastPickCate = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getInt(Params.LAST_PICK_CATE, Params.PICK_BY_KITCHEN);
		switch(lastPickCate)
		{
		case Params.PICK_BY_NUMBER:
			mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
			break;
		case Params.PICK_BY_KITCHEN:
			mViewHandler.sendEmptyMessage(KITCHEN_FRAGMENT);
			break;
		case Params.PICK_BY_PINYIN:
			mViewHandler.sendEmptyMessage(SPELL_FRAGMENT);
			break;
		default :
			mViewHandler.sendEmptyMessage(NUMBER_FRAGMENT);
		}
	}

	//activity���غ󽫲�Ʒ��ӽ��ѵ����
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			FoodParcel foodParcel;
			switch (requestCode) {
			case PICK_WITH_TASTE:
				/*
				 * ��ӿ�ζ����ӵ�pickList��
				 */
				foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				addFood(foodParcel);
				break;
			case OrderFoodListView.PICK_TASTE:
				/*
				 * ��ζ�ı�ʱ֪ͨListView���и���
				 */
				foodParcel = data.getParcelableExtra(FoodParcel.KEY_VALUE);
				for(int i =0;i<mPickFoods.size();i++)
				{
					if(mPickFoods.get(i).equalsIgnoreTaste(foodParcel))
					{
						mPickFoods.set(i, foodParcel);
						mTextHandler.sendEmptyMessage(REFRESH_FOODS);
					}
				}
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
	 * 
	 * @param food
	 *            ѡ�в�Ʒ����Ϣ
	 */
	@Override
	public void onPickedWithTaste(OrderFood food) {
		Intent intent = new Intent(this, PickTasteActivity.class);
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(food));
		intent.putExtras(bundle);
		startActivityForResult(intent, PICK_WITH_TASTE);
	}
	
	/**
	 * ��Ӳ�Ʒ���ѵ�˵�List��
	 * 
	 * @param food
	 *            ѡ�еĲ�Ʒ��Ϣ
	 */
	private void addFood(OrderFood food) {

		int index = mPickFoods.indexOf(food);

		if (index != -1) {
			/**
			 * ���ԭ���Ĳ�Ʒ�б����Ѱ�������ͬ�Ĳ�Ʒ�� ���µ�˵������ۼӵ�ԭ���Ĳ�Ʒ��
			 */
			OrderFood pickedFood = mPickFoods.get(index);

			float orderAmount = food.getCount() + pickedFood.getCount();
			if (orderAmount > 255) {
				Toast.makeText(this, "�Բ���\"" + food.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "���"	+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "������\"" : "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "��", Toast.LENGTH_SHORT)	.show();
				pickedFood.setCount(orderAmount);
				mPickFoods.set(index, pickedFood);
			}
		} else {
			if (food.getCount() > 255) {
				Toast.makeText(this, "�Բ���\"" + food.toString() + "\"���ֻ�ܵ�255��", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "����"	+ (food.hangStatus == OrderFood.FOOD_HANG_UP ? "������\"" : "\"") + food.toString() + "\""
								+ Util.float2String2(food.getCount()) + "��", Toast.LENGTH_SHORT).show();
				mPickFoods.add(food);
			}
		}
	}

	@Override
	public void onPickTaste(OrderFood selectedFood) {
		if(selectedFood.isTemporary){
			Toast.makeText(this, "��ʱ�˲�����ӿ�ζ", Toast.LENGTH_SHORT).show();
		}else{
			Intent intent = new Intent(QuickPickActivity.this, PickTasteActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(selectedFood));
			intent.putExtras(bundle);
			startActivityForResult(intent, OrderFoodListView.PICK_TASTE);			
		}		
	}

	@Override
	public void onPickFood() {
	}
	
	/**
	 * ִ���µ����������
	 */
	private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog _progDialog;
		
		public InsertOrderTask(Order reqOrder) {
			super(reqOrder);
		}
		
		/**
		 * ��ִ�������µ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(QuickPickActivity.this, "", "�ύ" + mReqOrder.destTbl.aliasID + "�Ų�̨���µ���Ϣ...���Ժ�", true);
		}
		
		
		/**
		 * ���ݷ��ص�error message�жϣ���������쳣����ʾ�û���
		 * ����ɹ����򷵻ص������棬����ʾ�û��µ��ɹ�
		 */
		@Override
		protected void onPostExecute(BusinessException e){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			/**
			 * Prompt user message if any error occurred.
			 */
			if(e != null){
				new AlertDialog.Builder(QuickPickActivity.this)
				.setTitle("��ʾ")
				.setMessage(e.getMessage())
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
			}else{
				//return to the main activity and show the message
				QuickPickActivity.this.finish();
				Toast.makeText(QuickPickActivity.this, mReqOrder.destTbl.aliasID + "��̨�µ��ɹ���", Toast.LENGTH_SHORT).show();
			}
		}
	}	
	
	class CommitDialog extends Dialog{

		private ListView mListView;
		
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
			LayoutParams lp = getWindow().getAttributes();
			lp.height = LayoutParams.WRAP_CONTENT;
			lp.width = 440;
			getWindow().setAttributes(lp);
			
			//���������
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); 
			final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
           	imm.showSoftInput(this.getCurrentFocus(), 0); //��ʾ�����
           	imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
           	
           	final AutoCompleteTextView tableText = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_commitDialog);
           	//�ĵ���ť
           	final Button changeBtn = (Button) findViewById(R.id.button_changeOrder_commitDialog);
           	changeBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
					mViewHandler.sendEmptyMessage(PICKED_FOOD_INTERFACE);
				}
			});
           	
           	final View line = (View) findViewById(R.id.view1);
           	((Button)this.findViewById(R.id.button_detail_commit_dialog)).setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					//�ж�listview״̬���������
					if(mListView.isShown())
					{
						//��ʾ������
						mListView.setVisibility(View.GONE);
						changeBtn.setVisibility(View.GONE);
						line.setVisibility(View.GONE);
						//���öԻ��������Ӧ
						LayoutParams lp = getWindow().getAttributes();
						lp.height = LayoutParams.WRAP_CONTENT;
						lp.width = 440;
						getWindow().setAttributes(lp);
					}
					else {
						imm.hideSoftInputFromWindow(tableText.getWindowToken(), 0);
						mListView.requestFocus();
						mListView.setVisibility(View.VISIBLE);
						changeBtn.setVisibility(View.VISIBLE);
						line.setVisibility(View.VISIBLE);
						//���öԻ��򳤿�Ϊ600px
						LayoutParams lp = getWindow().getAttributes();
						lp.height = 600;
						lp.width = 440;
						getWindow().setAttributes(lp);
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
        	
        	final TextView peopleCountTextView = (TextView)findViewById(R.id.textView_peopleCnt_commitDialog);
        	//ȷ����ť
        	((Button)this.findViewById(R.id.button_confirm_commitDialog)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//FIXME 
					OrderFood[] foods = mPickFoods.toArray(new OrderFood[mPickFoods.size()]);
					if(foods.length != 0 && !tableText.getText().toString().equals("")){
						Order reqOrder = new Order(foods,											   
												   Short.parseShort(tableText.getText().toString()),
												   Integer.parseInt(peopleCountTextView.getText().toString()));
						new InsertOrderTask(reqOrder).execute(Type.INSERT_ORDER);
						
					}else{
						Toast.makeText(QuickPickActivity.this, "������̨��", Toast.LENGTH_SHORT).show();
					}
				}
			});
        	
			//�����Ӱ�ť
			((Button) findViewById(R.id.button_plus_people_commitDialog)).setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					if(!peopleCountTextView.getText().toString().equals(""))
					{
						float curNum = Float.parseFloat(peopleCountTextView.getText().toString());
						peopleCountTextView.setText(Util.float2String2(++curNum));
					}
				}
			});
			//��������ť
			((Button) findViewById(R.id.button_minus_people_commitDialog)).setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					if(!peopleCountTextView.getText().toString().equals(""))
					{
						float curNum = Float.parseFloat(peopleCountTextView.getText().toString());
						if(--curNum >= 1.0f)
						{
							peopleCountTextView.setText(Util.float2String2(curNum));
						}
					}
				}
			});
           	
           	mListView = (ListView) this.findViewById(R.id.listView_commitDialog);
           	mListView.setAdapter(new BaseAdapter(){

				@Override
				public int getCount() {
					return mPickFoods.size();
				}

				@Override
				public Object getItem(int position) {
					return mPickFoods.get(position);
				}

				@Override
				public long getItemId(int position) {
					return position;
				}

				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					//TODO add more
					View view;
					if(convertView == null)
						view = LayoutInflater.from(getContext()).inflate(R.layout.quick_pick_commit_dialog_item, null);
					else view = convertView;
					
					OrderFood food = mPickFoods.get(position);
					((TextView)view.findViewById(R.id.textView_foodName_commit_dialog_item)).setText(food.name);
					((TextView)view.findViewById(R.id.textView_amount_quickPick_commitDialog_item)).setText(Util.float2String2(food.getCount()));
					((TextView)view.findViewById(R.id.textView_price_quickPick_commitDialog_item)).setText(Util.CURRENCY_SIGN + Util.float2String2(food.calcPriceWithTaste()));
					return view;
				}
           	});
		}
	}
}

