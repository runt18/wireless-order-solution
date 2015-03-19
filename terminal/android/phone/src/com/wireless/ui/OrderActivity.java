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
		title.setText("账单");

		//"返回"Button
		TextView left = (TextView)findViewById(R.id.textView_left);
		left.setText("返回");
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
										 	.setTitle("请输入人数")
										 	.setIcon(android.R.drawable.ic_dialog_info)
										 	.setView(amountEditText)
										 	.setPositiveButton("确定", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													customerTextView.setText(amountEditText.getText());
												}
											})
										 	.setNegativeButton("取消", null)
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
		rightTxtView.setText("提交");
		rightTxtView.setVisibility(View.VISIBLE);
		
		//"提交"Button
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
		//下单逻辑
		TableParcel table = getIntent().getExtras().getParcelable(KEY_TABLE_ID);
		
		int customNum;
		String custNumString = ((TextView)findViewById(R.id.editText_orderActivity_customerNum)).getText().toString();
		//如果人数为空，则默认为1
		if(custNumString.length() != 0){
			customNum = Integer.parseInt(custNumString);
		}else{
			customNum = 1;
		}
		
		try{
			if(forceInsert){
				//强制下单
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
			.setTitle("提示")
			.setMessage("账单还未提交，是否确认退出?")
			.setNeutralButton("确定",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							finish();
						}
					})
			.setNegativeButton("取消", null)
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
		mProgressDialog = ProgressDialog.show(OrderActivity.this, "", "正在提交账单信息...请稍候");		
	}

	@Override
	public void postSuccess(Order order) {
		mProgressDialog.dismiss();			
		final Table table = getIntent().getExtras().getParcelable(KEY_TABLE_ID);
		Toast.makeText(OrderActivity.this, table.getName() + "下单成功", Toast.LENGTH_SHORT).show();
		finish();		
	}

	@Override
	public void postFailed(BusinessException e, Order order) {
		mProgressDialog.dismiss();	
		
		if(e.getErrCode().equals(FrontBusinessError.ORDER_EXPIRED)){
			//如果返回是账单过期的错误状态，表示账单已有更新
			//则提示用户重新请求账单，再次确认提交
			new AlertDialog.Builder(OrderActivity.this)
				.setTitle("提示")
				.setMessage(e.getMessage())
				.setPositiveButton("继续提交", 
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							commit(true);
						}
					})
				.setNeutralButton("刷新账单",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which){
							((OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(OrderFoodFragment.TAG)).refresh();
						}
					})
				.show();
		}else{
			new AlertDialog.Builder(OrderActivity.this)
			.setTitle("提示")
			.setMessage(e.getMessage())
			.setNeutralButton("刷新账单",
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
				mProgDialog = ProgressDialog.show(OrderActivity.this, "", "正在交换数据...请稍后", true);
			}		
		
			protected void onSuccess(){
				mProgDialog.dismiss();
				Toast.makeText(getApplicationContext(), "换台成功", Toast.LENGTH_SHORT).show();
				((TextView)findViewById(R.id.txtView_orderActivity_tableName)).setText(selectedTable.getName());
			};
			
			protected void onFail(BusinessException e){
				mProgDialog.dismiss();
				new AlertDialog.Builder(OrderActivity.this)
					.setTitle("提示")
					.setMessage(e.getDesc())
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
