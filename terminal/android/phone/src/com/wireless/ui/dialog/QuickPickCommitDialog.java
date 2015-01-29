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
import android.text.Selection;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
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
import com.wireless.exception.TableError;
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
public class QuickPickCommitDialog extends DialogFragment{
	
	public static final String TAG = "CommitDialog";
	
	private ListView mListView;
	
	private boolean mIsPayOrder = false;
	
	private boolean mIsTempPay = false;
	
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
					txtViewTblName.setText(tbl.getName());
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
		
		getDialog().setTitle("�������̨��");
		
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
       
        //���TableText��ȫѡ���ݲ����������
//       	tableText.setOnTouchListener(new OnTouchListener(){
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				tableText.selectAll();
//				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//			    imm.showSoftInput(v, 0);
//				return true;
//			}
//        });
       	tableText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tableText.setText(tableText.getText());
				Selection.selectAll(tableText.getText());
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(v, 0);
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
       	
       	//�ύ����ӡ��ť
       	((Button)view.findViewById(R.id.button_commitDialog_commit_not_print)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsPayOrder = false;
				try{
					Table table = findByAlias(Short.parseShort(tableText.getText().toString()));
					if(table != null){
						new InsertOrderForceTask(table, PrintOption.DO_NOT_PRINT).execute();
					}else{
						throw new BusinessException(TableError.TABLE_NOT_EXIST);
					}
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
					
				} catch (BusinessException e) {
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
       	
       	//�ݽᰴť  
       	((Button)view.findViewById(R.id.button_commitDialog_payTempBill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsPayOrder = true;
				mIsTempPay = true;
				try{
					Table table = findByAlias(Short.parseShort(tableText.getText().toString()));
					if(table != null){
						new InsertOrderForceTask(table).execute();
					}else{
						throw new BusinessException(TableError.TABLE_NOT_EXIST);
					}
					
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
					
				} catch (BusinessException e) {
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
       	
       	//�ύ�����˰�ť  
       	((Button)view.findViewById(R.id.button_commitDialog_payBill)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsPayOrder = true;
				mIsTempPay = false;
				try{
					Table table = findByAlias(Short.parseShort(tableText.getText().toString()));
					if(table != null){
						new InsertOrderForceTask(table).execute();
					}else{
						throw new BusinessException(TableError.TABLE_NOT_EXIST);
					}
					
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "�������̨�Ų���ȷ������������", Toast.LENGTH_SHORT).show();
					
				} catch (BusinessException e) {
					Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		});
       	
       	//ȡ����ť
    	((Button)view.findViewById(R.id.button_commitDialog_cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();					
			}
		});
    	
    	//�µ���ť
    	((Button)view.findViewById(R.id.button_commitDialog_commit)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mReqOrder.hasOrderFood()){
					mIsPayOrder = false;
					mIsTempPay = false;
					try{
						Table table = findByAlias(Short.parseShort(tableText.getText().toString()));
						if(table != null){
							new InsertOrderForceTask(table).execute();
						}else{
							throw new BusinessException(TableError.TABLE_NOT_EXIST);
						}
					}catch (BusinessException e) {
						Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
							
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
					view = LayoutInflater.from(getActivity()).inflate(R.layout.quick_pick_commit_dialog_item, parent, false);
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
				((TextView)view.findViewById(R.id.textView_price_quickPick_commitDialog_item)).setText(NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(food.calcPrice()));
				return view;
			}
       	});
       	
       	//��ʾС�ƽ��
       	((TextView)view.findViewById(R.id.txtView_totalPrice_quickPick_commitDialog)).setText("С�ƣ�" + NumericUtil.CURRENCY_SIGN + NumericUtil.float2String2(mReqOrder.calcPriceBeforeDiscount()));
       	
       	return view;
	}

	private Table findByAlias(int tableAlias){
		for(final Table table : WirelessOrder.tables){
			if(table.getAliasId() == tableAlias){
				return table;
			}
		}
		return null;
	}
	
	/**
	 * ִ���µ����������
	 */
	private class InsertOrderForceTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgDialog;
		
		InsertOrderForceTask(Table table, PrintOption printOption) throws BusinessException {
			super(WirelessOrder.loginStaff, new Order.InsertBuilder(new Table.Builder(table.getId())).setWxOrders(mReqOrder.getWxOrders()).addAll(mReqOrder.getOrderFoods(), WirelessOrder.loginStaff).setForce(true), printOption);
			mReqOrder.setDestTbl(table);
		}
		
		InsertOrderForceTask(Table table) throws BusinessException {
			this(table, PrintOption.DO_PRINT);
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
				new QueryAndPayOrderTask(new Table.Builder(mReqOrder.getDestTbl().getId())).execute();
				
			}else{
				dismiss();
				//ֱ�ӷ��ص�MainActivity
				Intent intent = new Intent(getActivity(), MainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				getActivity().startActivity(intent);
				Toast.makeText(getActivity(), mReqOrder.getDestTbl().getName() + "�µ��ɹ�", Toast.LENGTH_SHORT).show();
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
	
	private class QueryAndPayOrderTask extends com.wireless.lib.task.QueryOrderTask{
		
		private final Table.Builder mTblBuilder;
		
		public QueryAndPayOrderTask(Table.Builder tblBuilder) {
			super(WirelessOrder.loginStaff, tblBuilder);
			mTblBuilder = tblBuilder;
		}

		private ProgressDialog mProgressDialog;
		@Override
		protected void onPreExecute(){
			mProgressDialog = ProgressDialog.show(getActivity(), "", "��ѯ�Ų�̨����Ϣ...���Ժ�", true);
		}
		
		@Override
		public void onSuccess(Order order){
			mProgressDialog.dismiss();
			new PayOrderTask(Order.PayBuilder.build4Normal(order.getId()).setTemp(mIsTempPay)).execute();
		}
		
		@Override 
		public void onFail(BusinessException e){
			mProgressDialog.dismiss();
			new AlertDialog.Builder(getActivity())
				.setTitle(e.getMessage())
				.setMessage("��Ʒ����ӣ�����������ʧ�ܣ��Ƿ����ԣ�")
				.setPositiveButton("����", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						new QueryAndPayOrderTask(mTblBuilder).execute();
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
						.setTitle("��ʾ")
						.setMessage("��Ʒ����ӣ���" + (payBuilder.isTemp() ? "�ݽ�" : "����") + "����ʧ��(" + e.getMessage() + ")���Ƿ����ԣ�")
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
