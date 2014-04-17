package com.wireless.ui.dialog;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pack.Type;
import com.wireless.parcel.OrderParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.Order.PayBuilder;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.MainActivity;
import com.wireless.ui.R;


/**
 * 快点界面中的提交Dialog
 */
public class QuickPickCommitDialog extends DialogFragment{
	
	public static final String TAG = "CommitDialog";
	
	private ListView mListView;
	
	private boolean mIsPayOrder = false;
	
	private boolean mIsTempPay = false;
	
	private Order mOrderToCommit;
	
	private Order mReqOrder;
	
	private Handler mTableNameHandler;
	
	private static class RefreshTableNameHandler extends Handler{
		private WeakReference<QuickPickCommitDialog> mDialog;

		RefreshTableNameHandler(QuickPickCommitDialog fragment) {
			this.mDialog = new WeakReference<QuickPickCommitDialog>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg){
			final QuickPickCommitDialog dialog = mDialog.get();
			
			int tableAlias = Integer.parseInt(((EditText)dialog.getView().findViewById(R.id.autoCompleteTextView_commitDialog)).getText().toString());
			TextView txtViewTblName = (TextView)dialog.getView().findViewById(R.id.txtView_tableName_quickPick_commitDialog);
			
			txtViewTblName.setVisibility(View.INVISIBLE);
			for(Table tbl : WirelessOrder.tables){
				if(tbl.getAliasId() == tableAlias){
					txtViewTblName.setVisibility(View.VISIBLE);
					if(tbl.getName().length() > 0){
						txtViewTblName.setText(tbl.getName());
					}else{
						txtViewTblName.setText(tbl.getAliasId() + "号台");
					}
					break;
				}
			}
		}
	}
	
	public static QuickPickCommitDialog newCommitDialog(Order reqOrder){
		QuickPickCommitDialog dlg = new QuickPickCommitDialog();
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(reqOrder));
		dlg.setArguments(bundle);
		return dlg;
	}
	
	public QuickPickCommitDialog(){
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTableNameHandler = new RefreshTableNameHandler(this);
	}
	
	@Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		OrderParcel orderParcel = getArguments().getParcelable(OrderParcel.KEY_VALUE);
		mReqOrder = orderParcel.asOrder();
		
		getDialog().setTitle("请输入餐台号");
		
        // Inflate the layout to use as dialog or embedded fragment
        final View view = inflater.inflate(R.layout.quick_pick_commit_dialog, container, false);
		
       	final EditText tableText = (EditText)view.findViewById(R.id.autoCompleteTextView_commitDialog);
       	
        // Request focus and show soft keyboard automatically
       	tableText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	if(hasFocus){
	            	tableText.post(new Runnable() {
	                    @Override
	                    public void run() {
	                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	                        imm.showSoftInput(tableText, InputMethodManager.SHOW_IMPLICIT);
	                    }
	                });
            	}
            }
        });
       	tableText.requestFocus();
       
        //点击TableText后全选内容并弹出软键盘
       	tableText.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				tableText.selectAll();
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			    imm.showSoftInput(v, 0);
				return true;
			}
        });
       	
       	//Refresh the table name while input the table alias.
       	tableText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().trim().length() != 0){
					mTableNameHandler.sendEmptyMessage(0);
				}else{
					getView().findViewById(R.id.txtView_tableName_quickPick_commitDialog).setVisibility(View.INVISIBLE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
       	
       	//提交不打印按钮
       	((Button)view.findViewById(R.id.button_commitDialog_commit_not_print)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsPayOrder = false;
				try{
					short tableAlias = Short.parseShort(tableText.getText().toString());
					new QueryAndCommitOrderTask(tableAlias, PrintOption.DO_NOT_PRINT).execute();
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "你输入的台号不正确，请重新输入", Toast.LENGTH_SHORT).show();
				}
			}
		});
       	
       	//暂账按钮  
       	((Button)view.findViewById(R.id.button_commitDialog_payTempBill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsPayOrder = true;
				mIsTempPay = true;
				try{
					short tableAlias = Short.parseShort(tableText.getText().toString());
					new QueryAndCommitOrderTask(tableAlias).execute();
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "你输入的台号不正确，请重新输入", Toast.LENGTH_SHORT).show();
				}
			}
		});
       	
       	//提交并结账按钮  
       	((Button)view.findViewById(R.id.button_commitDialog_payBill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsPayOrder = true;
				mIsTempPay = false;
				try{
					short tableAlias = Short.parseShort(tableText.getText().toString());
					new QueryAndCommitOrderTask(tableAlias).execute();
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "你输入的台号不正确，请重新输入", Toast.LENGTH_SHORT).show();
				}
			}
		});
       	
       	//取消按钮
    	((Button)view.findViewById(R.id.button_commitDialog_cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();					
			}
		});
    	
    	//下单按钮
    	((Button)view.findViewById(R.id.button_commitDialog_commit)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mReqOrder.hasOrderFood()){
					mIsPayOrder = false;
					mIsTempPay = false;
					try{
						short tableAlias = Short.parseShort(tableText.getText().toString());
						new QueryAndCommitOrderTask(tableAlias).execute();
					}catch(NumberFormatException e){
						Toast.makeText(getActivity(), "你输入的台号不正确，请重新输入", Toast.LENGTH_SHORT).show();
					}
					
				}else{
					Toast.makeText(getActivity(), "您还没有点菜", Toast.LENGTH_SHORT).show();						
				}
			}
		});
       	
       	mListView = (ListView) view.findViewById(R.id.listView_commitDialog);
       	
       	//当被点击或滚动时隐藏键盘
       	mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tableText.getWindowToken(), 0);
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				
			}
		});
       	
       	mListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(tableText.getWindowToken(), 0); 
			}
       	});
       	
       	if(mReqOrder.getOrderFoods().size() > 5){
       		mListView.getLayoutParams().height = 230;
       		mListView.setLayoutParams(mListView.getLayoutParams());
       	}
       	
       	mListView.setAdapter(new BaseAdapter(){

       		List<OrderFood> mSrcFoods = mReqOrder.getOrderFoods();
       		
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
					view = LayoutInflater.from(getActivity()).inflate(R.layout.quick_pick_commit_dialog_item, null);
				}else{
					view = convertView;
				}
				
				OrderFood food = mSrcFoods.get(position);
				if(food.getName().length() >= 8){
					((TextView)view.findViewById(R.id.textView_foodName_commit_dialog_item)).setText(food.getName().substring(0, 8));
				}else{
					((TextView)view.findViewById(R.id.textView_foodName_commit_dialog_item)).setText(food.getName());
				}
				
				((TextView)view.findViewById(R.id.textView_amount_quickPick_commitDialog_item)).setText("(" + NumericUtil.float2String2(food.getCount()) + ")");
				((TextView)view.findViewById(R.id.textView_price_quickPick_commitDialog_item)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(food.calcPriceWithTaste()));
				return view;
			}
       	});
       	
       	//显示小计金额
       	((TextView)view.findViewById(R.id.txtView_totalPrice_quickPick_commitDialog)).setText("小计：" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(mReqOrder.calcPriceBeforeDiscount()));
       	
       	return view;
	}
	
	/**
	 * 执行请求对应餐台的账单信息 
	 */
	private class QueryAndCommitOrderTask extends com.wireless.lib.task.QueryOrderTask{

		private ProgressDialog mProgDialog;
	
		private final PrintOption mPrintOption;
		
		
		QueryAndCommitOrderTask(int tableAlias){
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
			this.mPrintOption = PrintOption.DO_PRINT;
		}
		
		QueryAndCommitOrderTask(int tableAlias, PrintOption printOption){
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
			this.mPrintOption = printOption;
		}
		
		/**
		 * 在执行请求删单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(getActivity(), "", "查询" + mTblAlias + "号餐台的信息...请稍候", true);
		}
		
		@Override
		public void onSuccess(Order order){
			mProgDialog.dismiss();
			//Merge the original order and update if the activity_table is BUSY.
			try{
				order.addFoods(mReqOrder.getOrderFoods(), WirelessOrder.loginStaff);
				mOrderToCommit = order;
				new InsertOrderTask(mOrderToCommit, Type.UPDATE_ORDER, mPrintOption).execute();
			}catch(BusinessException e){
				Toast.makeText(QuickPickCommitDialog.this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
		
		@Override
		public void onFail(BusinessException e){
			mProgDialog.dismiss();
			if(mBusinessException.getErrCode().equals(ProtocolError.ORDER_NOT_EXIST)){				
				
				//Perform to insert a new order in case of the activity_table is IDLE.
				mOrderToCommit = mReqOrder;
				mOrderToCommit.setDestTbl(new Table(mTblAlias));
				new InsertOrderTask(mOrderToCommit, Type.INSERT_ORDER, mPrintOption).execute();						
				
			}else{
				new AlertDialog.Builder(getActivity())
				.setTitle("提示")
				.setMessage(mBusinessException.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				})
				.show();
			}
		}
		
	}
	
	/**
	 * 执行下单的请求操作
	 */
	private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgDialog;
		
		public InsertOrderTask(Order reqOrder, byte type, PrintOption printOption) {
			super(WirelessOrder.loginStaff, reqOrder, type, printOption);
		}
		
		/**
		 * 在执行请求下单操作前显示提示信息
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(getActivity(), "", "正在提交账单下单信息...请稍候", true);
		}			
		
		@Override
		protected void onSuccess(Order reqOrder){
			mProgDialog.dismiss();
			//Perform to pay order in case the flag is true,
			//otherwise back to the main activity and show the message
			if(mIsPayOrder){
				new QueryOrderTask2(mOrderToCommit.getDestTbl().getAliasId()).execute();
				
			}else{
				dismiss();
				//直接返回到MainActivity
				Intent intent = new Intent(getActivity(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getActivity().startActivity(intent);
				Toast.makeText(getActivity(), reqOrder.getDestTbl().getAliasId() + "号台下单成功。", Toast.LENGTH_SHORT).show();
			}
		}
		
		@Override
		protected void onFail(BusinessException e, Order reqOrder){
			mProgDialog.dismiss();
			//Prompt user message if any error occurred.
			new AlertDialog.Builder(getActivity())
				.setTitle("提示")
				.setMessage(e.getMessage())
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).show();
		}
		
	}	
	
	private class QueryOrderTask2 extends com.wireless.lib.task.QueryOrderTask{
		
		public QueryOrderTask2(int tableAlias) {
			super(WirelessOrder.loginStaff, tableAlias, WirelessOrder.foodMenu);
		}

		private ProgressDialog mProgressDialog;
		@Override
		protected void onPreExecute(){
			mProgressDialog = ProgressDialog.show(getActivity(), "", "查询" + mTblAlias + "号餐台的信息...请稍候", true);
		}
		
		@Override
		public void onSuccess(Order order){
			mProgressDialog.dismiss();
			new PayOrderTask(Order.PayBuilder.build(order.getId()).setTemp(mIsTempPay)).execute();
		}
		
		@Override 
		public void onFail(BusinessException e){
			mProgressDialog.dismiss();
			new AlertDialog.Builder(getActivity())
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
						getActivity().finish();
					}
				}).show();
		}
		
	}
	/**
	 * 执行结帐请求操作
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

		private ProgressDialog mProgDialog;

		PayOrderTask(Order.PayBuilder payBuilder) {
			super(WirelessOrder.loginStaff, payBuilder);
		}

		/**
		 * 在执行请求结帐操作前显示提示信息
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(getActivity(), "", "提交" + getPromptInfo() + "信息...请稍候", true);
		}

		@Override
		protected void onSuccess(PayBuilder payBuilder) {
			mProgDialog.dismiss();	
			Toast.makeText(getActivity(),"账单提交并" + (payBuilder.isTemp() ? "暂结" : "结账") + "成功", Toast.LENGTH_SHORT).show();
			dismiss();
			//直接返回到MainActivity
			Intent intent = new Intent(getActivity(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			getActivity().startActivity(intent);
		}

		@Override
		protected void onFail(final PayBuilder payBuilder, BusinessException e) {
			mProgDialog.dismiss();		
			new AlertDialog.Builder(getActivity())
						.setTitle(e.getMessage())
						.setMessage("菜品已添加，但结账请求失败，是否重试？")
						.setPositiveButton("重试", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								new PayOrderTask(payBuilder).execute();
							}
						})
						.setNegativeButton("退出", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dismiss();
								getActivity().finish();
							}
						}).show();
		}
	}
}
