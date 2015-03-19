package com.wireless.ui;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.fragment.OrderFoodFragment;
import com.wireless.fragment.OrderFoodFragment.OnCommitListener;
import com.wireless.fragment.OrderFoodFragment.OnOrderChangedListener;
import com.wireless.lib.task.TransTblTask;
import com.wireless.parcel.TableParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.regionMgr.Table.InsertBuilder4Join.Suffix;
import com.wireless.ui.dialog.AskTableDialog;
import com.wireless.ui.dialog.AskTableDialog.OnTableSelectedListener;

public class OrderActivity extends FragmentActivity implements OnOrderChangedListener, 
															   OnCommitListener,
															   OnTableSelectedListener{
	
	public static final String KEY_TABLE_ID = OrderActivity.class.getName() + ".tableKey";
	
	private ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_activity);

		//Title
		TextView title = (TextView)findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("�˵�");

		//"����"Button
		TextView left = (TextView)findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);
		ImageButton backBtn = (ImageButton)findViewById(R.id.btn_left);
		backBtn.setVisibility(View.VISIBLE);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBackPressed();
			}
		});
		
		//set the table No
		final TextView txtViewTblName = ((TextView)findViewById(R.id.txtView_orderActivity_tableName));
		final Table selectedTable = getIntent().getExtras().getParcelable(KEY_TABLE_ID);
		txtViewTblName.setText(selectedTable.getName());
		txtViewTblName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AskTableDialog.newInstance().show(getSupportFragmentManager(), AskTableDialog.TAG);
			}
		});
		
		//set the default customer to 1
		final TextView customerTextView = ((TextView)findViewById(R.id.editText_orderActivity_customerNum));
		customerTextView.setText("1");
		customerTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final EditText amountEditText = new EditText(OrderActivity.this);
				amountEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
				amountEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
		            	if(hasFocus){
		            		amountEditText.post(new Runnable() {
			                    @Override
			                    public void run() {
			                        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(amountEditText, InputMethodManager.SHOW_IMPLICIT);
			                    }
			                });
		            	}
					}
				});
			 	Dialog amountDialog = new AlertDialog.Builder(OrderActivity.this)
										 	.setTitle("����������")
										 	.setIcon(android.R.drawable.ic_dialog_info)
										 	.setView(amountEditText)
										 	.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													customerTextView.setText(amountEditText.getText());
												}
											})
										 	.setNegativeButton("ȡ��", null)
										 	.create();
			 	amountDialog.setOnShowListener(new OnShowListener() {
					@Override
					public void onShow(DialogInterface dialog) {
						amountEditText.requestFocus();
					}
				});
			 	
			 	amountDialog.show();
			}
		});
		
		TextView rightTxtView = (TextView)findViewById(R.id.textView_right);
		rightTxtView.setText("�ύ");
		rightTxtView.setVisibility(View.VISIBLE);
		
		//"�ύ"Button
		ImageButton commitBtn = (ImageButton)findViewById(R.id.btn_right);
		commitBtn.setVisibility(View.VISIBLE);
		commitBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				commit(false);
			}
		});
		
		//Add OrderFoodFragment
		FragmentTransaction fgTrans = getSupportFragmentManager().beginTransaction();
		fgTrans.add(R.id.frameLayout_container_orderFood, 
				    OrderFoodFragment.newInstance(selectedTable),
				    OrderFoodFragment.TAG).commit();
		
	}

	private void commit(boolean forceInsert){
		OrderFoodFragment ofFgm = (OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(OrderFoodFragment.TAG);
		//�µ��߼�
		TableParcel table = getIntent().getExtras().getParcelable(KEY_TABLE_ID);
		
		int customNum;
		String custNumString = ((TextView)findViewById(R.id.editText_orderActivity_customerNum)).getText().toString();
		//�������Ϊ�գ���Ĭ��Ϊ1
		if(custNumString.length() != 0){
			customNum = Integer.parseInt(custNumString);
		}else{
			customNum = 1;
		}
		
		try{
			if(forceInsert){
				//ǿ���µ�
				ofFgm.commitForce(new Table.Builder(table.getId()), customNum, PrintOption.DO_PRINT);
			}else{
				ofFgm.commit(new Table.Builder(table.getId()), customNum, PrintOption.DO_PRINT);
			}
		}catch(BusinessException e){			
			Toast.makeText(OrderActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onBackPressed() {
		if(((OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(OrderFoodFragment.TAG)).hasNewOrderFood()){			
			new AlertDialog.Builder(this)
			.setTitle("��ʾ")
			.setMessage("�˵���δ�ύ���Ƿ�ȷ���˳�?")
			.setNeutralButton("ȷ��",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							finish();
						}
					})
			.setNegativeButton("ȡ��", null)
			.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
					return true;
				}
			}).show();
		}else{
			super.onBackPressed();
		}
	}

	@Override
	public void onOrderChanged(Order oriOrder, List<OrderFood> newFoodList) {
		if(oriOrder != null){
			//set the table name
			((TextView)findViewById(R.id.txtView_orderActivity_tableName)).setText(oriOrder.getDestTbl().getName());
			//set the amount of customer
			((TextView)findViewById(R.id.editText_orderActivity_customerNum)).setText(Integer.toString(oriOrder.getCustomNum()));	
		}
	}

	@Override
	public void preCommit() {
		mProgressDialog = ProgressDialog.show(OrderActivity.this, "", "�����ύ�˵���Ϣ...���Ժ�");		
	}

	@Override
	public void postSuccess(Order order) {
		mProgressDialog.dismiss();			
		final Table table = getIntent().getExtras().getParcelable(KEY_TABLE_ID);
		Toast.makeText(OrderActivity.this, table.getName() + "�µ��ɹ�", Toast.LENGTH_SHORT).show();
		finish();		
	}

	@Override
	public void postFailed(BusinessException e, Order order) {
		mProgressDialog.dismiss();	
		
		if(e.getErrCode().equals(FrontBusinessError.ORDER_EXPIRED)){
			//����������˵����ڵĴ���״̬����ʾ�˵����и���
			//����ʾ�û����������˵����ٴ�ȷ���ύ
			new AlertDialog.Builder(OrderActivity.this)
				.setTitle("��ʾ")
				.setMessage(e.getMessage())
				.setPositiveButton("�����ύ", 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							commit(true);
						}
					})
				.setNeutralButton("ˢ���˵�",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							((OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(OrderFoodFragment.TAG)).refresh();
						}
					})
				.show();
		}else{
			new AlertDialog.Builder(OrderActivity.this)
			.setTitle("��ʾ")
			.setMessage(e.getMessage())
			.setNeutralButton("ˢ���˵�",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,	int which){
						((OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(OrderFoodFragment.TAG)).refresh();
					}
				})
			.show();
		}
		
	}

	@Override
	public void onTableSelected(final Table selectedTable) {
		final Table dest = getIntent().getExtras().getParcelable(KEY_TABLE_ID);
		new TransTblTask(WirelessOrder.loginStaff, new Table.Builder(dest.getId()), new Table.Builder(selectedTable.getId())){
			
			private ProgressDialog mProgDialog;
			
			@Override
			protected void onPreExecute(){			
				mProgDialog = ProgressDialog.show(OrderActivity.this, "", "���ڽ�������...���Ժ�", true);
			}		
		
			protected void onSuccess(){
				mProgDialog.dismiss();
				Toast.makeText(getApplicationContext(), "��̨�ɹ�", Toast.LENGTH_SHORT).show();
				((TextView)findViewById(R.id.txtView_orderActivity_tableName)).setText(selectedTable.getName());
			};
			
			protected void onFail(BusinessException e){
				mProgDialog.dismiss();
				new AlertDialog.Builder(OrderActivity.this)
					.setTitle("��ʾ")
					.setMessage(e.getDesc())
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();
			}
			
		}.execute();
	}

	@Override
	public void onJoinedSelected(Table parent, Suffix suffix) {
		
	}
	
}
