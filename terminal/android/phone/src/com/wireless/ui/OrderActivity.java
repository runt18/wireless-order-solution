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
		if(selectedTable.getName().length() != 0){
			txtViewTblName.setText(selectedTable.getName());
		}else{
			txtViewTblName.setText(selectedTable.getAliasId() + "��̨");
		}
		
		//set the default customer to 1
		((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText("1");
		
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
		String custNumString = ((EditText)findViewById(R.id.editText_orderActivity_customerNum)).getText().toString();
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
			//set the table ID
			if(oriOrder.getDestTbl().getName().length() != 0){
				((TextView)findViewById(R.id.txtView_orderActivity_tableName)).setText(oriOrder.getDestTbl().getName());
			}else{
				((TextView)findViewById(R.id.txtView_orderActivity_tableName)).setText(Integer.toString(oriOrder.getDestTbl().getAliasId()) + "��̨");
			}
			//set the amount of customer
			((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText(Integer.toString(oriOrder.getCustomNum()));	
		}
	}

	@Override
	public void preCommit() {
		mProgressDialog = ProgressDialog.show(OrderActivity.this, "", "�����ύ�˵���Ϣ...���Ժ�");		
	}

	@Override
	public void postSuccess(Order order) {
		mProgressDialog.dismiss();			
		Toast.makeText(OrderActivity.this, order.getDestTbl().getAliasId() + "�Ų�̨�µ��ɹ�", Toast.LENGTH_SHORT).show();
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
	
}
