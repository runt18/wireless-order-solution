package com.wireless.ui;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.fragment.OrderFoodFragment;
import com.wireless.fragment.OrderFoodFragment.OnCommitListener;
import com.wireless.fragment.OrderFoodFragment.OnOrderChangedListener;
import com.wireless.parcel.TableParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PrintOption;
import com.wireless.pojo.regionMgr.Table;

public class OrderActivity extends FragmentActivity implements OnOrderChangedListener, OnCommitListener{
	
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
		if(selectedTable.getName().length() != 0){
			txtViewTblName.setText(selectedTable.getName());
		}else{
			txtViewTblName.setText(selectedTable.getAliasId() + "号台");
		}
		
		//set the default customer to 1
		((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText("1");
		
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
		String custNumString = ((EditText)findViewById(R.id.editText_orderActivity_customerNum)).getText().toString();
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
			//set the table ID
			if(oriOrder.getDestTbl().getName().length() != 0){
				((TextView)findViewById(R.id.txtView_orderActivity_tableName)).setText(oriOrder.getDestTbl().getName());
			}else{
				((TextView)findViewById(R.id.txtView_orderActivity_tableName)).setText(Integer.toString(oriOrder.getDestTbl().getAliasId()) + "号台");
			}
			//set the amount of customer
			((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText(Integer.toString(oriOrder.getCustomNum()));	
		}
	}

	@Override
	public void preCommit() {
		mProgressDialog = ProgressDialog.show(OrderActivity.this, "", "正在提交账单信息...请稍候");		
	}

	@Override
	public void postSuccess(Order order) {
		mProgressDialog.dismiss();			
		Toast.makeText(OrderActivity.this, order.getDestTbl().getAliasId() + "号餐台下单成功", Toast.LENGTH_SHORT).show();
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
	
}
