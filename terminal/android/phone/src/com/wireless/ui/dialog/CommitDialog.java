package com.wireless.ui.dialog;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
 * �������е��ύDialog
 */
public class CommitDialog extends DialogFragment{
	
	public static final String TAG = "CommitDialog";
	
	private ListView mListView;
	
	private boolean mIsPayOrder = false;
	
	private Order mOrderToCommit;
	
	private Order mReqOrder;
	
	public static CommitDialog newCommitDialog(Order reqOrder){
		CommitDialog dlg = new CommitDialog();
		Bundle bundle = new Bundle();
		bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(reqOrder));
		dlg.setArguments(bundle);
		return dlg;
	}
	
	public CommitDialog(){
		
	}
	
	@Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		OrderParcel orderParcel = getArguments().getParcelable(OrderParcel.KEY_VALUE);
		mReqOrder = orderParcel.asOrder();
		
		getDialog().setTitle("�������̨�Ż�˶Ե����Ϣ");
		
        // Inflate the layout to use as dialog or embedded fragment
        final View view = inflater.inflate(R.layout.commit_dialog, container, false);
		
       	final AutoCompleteTextView tableText = (AutoCompleteTextView)view.findViewById(R.id.autoCompleteTextView_commitDialog);
       	
		// ���������
        // Request focus and show soft keyboard automatically
       	tableText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	tableText.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(tableText, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
       	tableText.requestFocus();
       	
       	//�ύ����ӡ��ť
       	final Button changeBtn = (Button)view.findViewById(R.id.button_changeOrder_commitDialog);
       	changeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsPayOrder = false;
				try{
					short tableAlias = Short.parseShort(tableText.getText().toString());
					new QueryAndCommitOrderTask(tableAlias, PrintOption.DO_NOT_PRINT).execute();
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
				}
			}
		});
       	
       	//�ύ�����˰�ť  
       	Button commitBtn = (Button)view.findViewById(R.id.button_commitDialog_payBill);
       	commitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsPayOrder = true;
				try{
					short tableAlias = Short.parseShort(tableText.getText().toString());
					new QueryAndCommitOrderTask(tableAlias).execute();
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
				}
			}
		});
       	
       	//ȡ����ť
    	((Button)view.findViewById(R.id.button_cancel_commitDialog)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();					
			}
		});
    	
    	//ȷ����ť
    	((Button)view.findViewById(R.id.button_confirm_commitDialog)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mReqOrder.hasOrderFood()){
					mIsPayOrder = false;
					try{
						short tableAlias = Short.parseShort(tableText.getText().toString());
						new QueryAndCommitOrderTask(tableAlias).execute();
					}catch(NumberFormatException e){
						Toast.makeText(getActivity(), "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
					}
					
				}else{
					Toast.makeText(getActivity(), "����û�е��", Toast.LENGTH_SHORT).show();						
				}
			}
		});
       	
       	mListView = (ListView) view.findViewById(R.id.listView_commitDialog);
       	
       	//������������ʱ���ؼ���
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
				
				((TextView)view.findViewById(R.id.textView_amount_quickPick_commitDialog_item)).setText(NumericUtil.float2String2(food.getCount()));
				((TextView)view.findViewById(R.id.textView_price_quickPick_commitDialog_item)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(food.calcPriceWithTaste()));
				return view;
			}
       	});
       	
       	return view;
	}
	
	/**
	 * ִ�������Ӧ��̨���˵���Ϣ 
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
		 * ��ִ������ɾ������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(getActivity(), "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
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
				Toast.makeText(CommitDialog.this.getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
				.setTitle("��ʾ")
				.setMessage(mBusinessException.getMessage())
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				})
				.show();
			}
		}
		
	}
	
	/**
	 * ִ���µ����������
	 */
	private class InsertOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgDialog;
		
		public InsertOrderTask(Order reqOrder, byte type, PrintOption printOption) {
			super(WirelessOrder.loginStaff, reqOrder, type, printOption);
		}
		
		/**
		 * ��ִ�������µ�����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			mProgDialog = ProgressDialog.show(getActivity(), "", "�����ύ�˵��µ���Ϣ...���Ժ�", true);
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
				//ֱ�ӷ��ص�MainActivity
				Intent intent = new Intent(getActivity(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getActivity().startActivity(intent);
				Toast.makeText(getActivity(), reqOrder.getDestTbl().getAliasId() + "��̨�µ��ɹ���", Toast.LENGTH_SHORT).show();
			}
		}
		
		@Override
		protected void onFail(BusinessException e, Order reqOrder){
			mProgDialog.dismiss();
			//Prompt user message if any error occurred.
			new AlertDialog.Builder(getActivity())
				.setTitle("��ʾ")
				.setMessage(e.getMessage())
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
			mProgressDialog = ProgressDialog.show(getActivity(), "", "��ѯ" + mTblAlias + "�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		@Override
		public void onSuccess(Order order){
			mProgressDialog.dismiss();
			new PayOrderTask(Order.PayBuilder.build(order.getId())).execute();
		}
		
		@Override 
		public void onFail(BusinessException e){
			mProgressDialog.dismiss();
			new AlertDialog.Builder(getActivity())
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
						getActivity().finish();
					}
				}).show();
		}
		
	}
	/**
	 * ִ�н����������
	 */
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask {

		private ProgressDialog mProgDialog;

		PayOrderTask(Order.PayBuilder payBuilder) {
			super(WirelessOrder.loginStaff, payBuilder);
		}

		/**
		 * ��ִ��������ʲ���ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute() {
			mProgDialog = ProgressDialog.show(getActivity(), "", "�ύ" + getPromptInfo() + "��Ϣ...���Ժ�", true);
		}

		@Override
		protected void onSuccess(PayBuilder payBuilder) {
			mProgDialog.dismiss();	
			Toast.makeText(getActivity(),"�˵��ύ��" + (payBuilder.isTemp() ? "�ݽ�" : "����") + "�ɹ�", Toast.LENGTH_SHORT).show();
			dismiss();
			//ֱ�ӷ��ص�MainActivity
			Intent intent = new Intent(getActivity(), MainActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			getActivity().startActivity(intent);
		}

		@Override
		protected void onFail(final PayBuilder payBuilder, BusinessException e) {
			mProgDialog.dismiss();		
			new AlertDialog.Builder(getActivity())
						.setTitle(e.getMessage())
						.setMessage("��Ʒ����ӣ�����������ʧ�ܣ��Ƿ����ԣ�")
						.setPositiveButton("����", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								new PayOrderTask(payBuilder).execute();
							}
						})
						.setNegativeButton("�˳�", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dismiss();
								getActivity().finish();
							}
						}).show();
		}
	}
}
