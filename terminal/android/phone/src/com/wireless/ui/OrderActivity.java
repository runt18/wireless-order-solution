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

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.fragment.OrderFoodFragment;
import com.wireless.fragment.OrderFoodFragment.OnOrderChangedListener;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;

public class OrderActivity extends FragmentActivity implements OnOrderChangedListener{
	
	public static final String KEY_TABLE_ID = "TableAmount";
	
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
		final EditText tblNoEditTxt = ((EditText)findViewById(R.id.editText_orderActivity_tableNum));
		tblNoEditTxt.setText((getIntent().getExtras().getString(KEY_TABLE_ID)));
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
				    OrderFoodFragment.newInstance(Integer.valueOf(getIntent().getExtras().getString(KEY_TABLE_ID))),
				    OrderFoodFragment.TAG).commit();
		
	}

	private void commit(boolean forceInsert){
		OrderFoodFragment ofFgm = (OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(OrderFoodFragment.TAG);
		//下单逻辑
		String tableIdString = ((EditText)findViewById(R.id.editText_orderActivity_tableNum)).getText().toString();
		
		//如果餐台非空则继续，否则提示
		if(tableIdString.trim().length() != 0){
				
			int tableAlias = Integer.parseInt(tableIdString);
			
			int customNum;
			String custNumString = ((EditText)findViewById(R.id.editText_orderActivity_customerNum)).getText().toString();
			//如果人数为空，则默认为1
			if(custNumString.length() != 0){
				customNum = Integer.parseInt(custNumString);
			}else{
				customNum = 1;
			}
			
			if(ofFgm.hasOrderFood()){
				if(forceInsert){
					//强制下单
					new CommitOrderTask(ofFgm.buildNewOrder(tableAlias, customNum), Type.INSERT_ORDER_FORCE).execute();
				}else{
					Order reqOrder = ofFgm.buildRequestOrder(tableAlias, customNum);
					if(reqOrder.getId() != 0){
						//改单
						new CommitOrderTask(reqOrder, Type.UPDATE_ORDER).execute();
					}else{
						//下单
						new CommitOrderTask(reqOrder, Type.INSERT_ORDER).execute();
					}
				}
			}else{
				Toast.makeText(OrderActivity.this, "您还未点菜，不能下单", Toast.LENGTH_SHORT).show();
			}
			
		} else {
			Toast.makeText(OrderActivity.this, "请输入正确的餐台号", Toast.LENGTH_SHORT).show();
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

	private class CommitOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgressDialog;

		public CommitOrderTask(Order reqOrder, byte type) {
			super(WirelessOrder.loginStaff, reqOrder, type);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = ProgressDialog.show(OrderActivity.this, "", "正在提交账单信息...请稍候");
		}

		@Override
		protected void onSuccess(Order reqOrder){
			mProgressDialog.dismiss();			
			Toast.makeText(OrderActivity.this, reqOrder.getDestTbl().getAliasId() + "号餐台下单成功", Toast.LENGTH_SHORT).show();
			finish();
		}
		
		@Override
		protected void onFail(BusinessException e, Order reqOrder){
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

	@Override
	public void onOrderChanged(Order oriOrder, List<OrderFood> newFoodList) {
		if(oriOrder != null){
			//set the table ID
			((EditText)findViewById(R.id.editText_orderActivity_tableNum)).setText(Integer.toString(oriOrder.getDestTbl().getAliasId()));
			//set the amount of customer
			((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText(Integer.toString(oriOrder.getCustomNum()));	
		}
	}
	
}
