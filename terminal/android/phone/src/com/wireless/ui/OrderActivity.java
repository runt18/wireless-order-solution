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
import com.wireless.exception.ProtocolError;
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
		final EditText tblNoEditTxt = ((EditText)findViewById(R.id.editText_orderActivity_tableNum));
		tblNoEditTxt.setText((getIntent().getExtras().getString(KEY_TABLE_ID)));
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
				OrderFoodFragment ofFgm = (OrderFoodFragment)getSupportFragmentManager().findFragmentByTag(OrderFoodFragment.TAG);
				//�µ��߼�
				String tableIdString = tblNoEditTxt.getText().toString();
				
				//�����̨�ǿ��������������ʾ
				if(tableIdString.trim().length() != 0){
						
					int tableAlias = Integer.parseInt(tableIdString);
					
					int customNum;
					String custNumString = ((EditText)findViewById(R.id.editText_orderActivity_customerNum)).getText().toString();
					//�������Ϊ�գ���Ĭ��Ϊ1
					if(custNumString.length() != 0){
						customNum = Integer.parseInt(custNumString);
					}else{
						customNum = 1;
					}
					
					Order reqOrder = ofFgm.buildRequestOrder(tableAlias, customNum);
					
					boolean hasOrderFood = false;
					for(OrderFood of : reqOrder.getOrderFoods()){
						if(of.getCount() > 0){
							hasOrderFood = true;
							break;
						}
					}
					
					if(hasOrderFood){
						if(reqOrder.getId() != 0){
							//�ĵ�
							new CommitOrderTask(reqOrder, Type.UPDATE_ORDER).execute();
						}else{
							//�µ�
							new CommitOrderTask(reqOrder, Type.INSERT_ORDER).execute();
						}
					}else{
						Toast.makeText(OrderActivity.this, "����δ��ˣ������µ�", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(OrderActivity.this, "��������ȷ�Ĳ�̨��", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		//Add OrderFoodFragment
		FragmentTransaction fgTrans = getSupportFragmentManager().beginTransaction();
		fgTrans.add(R.id.frameLayout_container_orderFood, 
				    OrderFoodFragment.newInstance(Integer.valueOf(getIntent().getExtras().getString(KEY_TABLE_ID))),
				    OrderFoodFragment.TAG).commit();
		
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

	private class CommitOrderTask extends com.wireless.lib.task.CommitOrderTask{

		private ProgressDialog mProgressDialog;

		public CommitOrderTask(Order reqOrder, byte type) {
			super(WirelessOrder.loginStaff, reqOrder, type);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog = ProgressDialog.show(OrderActivity.this, "", "��ѯ" + mReqOrder.getDestTbl().getAliasId() + "���˵���Ϣ...���Ժ�");
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mProgressDialog.cancel();
			
			if(mBusinessException == null){
				Toast.makeText(OrderActivity.this, mReqOrder.getDestTbl().getAliasId() + "�Ų�̨�µ��ɹ�", Toast.LENGTH_SHORT).show();
				finish();
			}else{
				if(mReqOrder.getId() != 0){

					if(mBusinessException.getErrCode().equals(ProtocolError.ORDER_EXPIRED)){
						//����Ǹĵ������ҷ������˵����ڵĴ���״̬����ʾ�˵����и���
						//����ʾ�û����������˵����ٴ�ȷ���ύ
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("��ʾ")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "�Ų�̨���˵���Ϣ�Ѿ����£��ѵ����Ϣ��ˢ�£��µ����Ϣ���ᱣ��")
							.setNeutralButton("ȷ��",
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
							.setMessage(mBusinessException.getMessage())
							.setNeutralButton("ȷ��", null)
							.show();
					}
				}else{
					if(mBusinessException.getErrCode().equals(ProtocolError.TABLE_BUSY)){
						//��������µ������ҷ����ǲ�̨�Ͳ͵Ĵ���״̬����ʾ�˵��Ѹ���
						//����ʾ�û����������˵����ٴ�ȷ���ύ
						new AlertDialog.Builder(OrderActivity.this)
							.setTitle("��ʾ")
							.setMessage(mReqOrder.getDestTbl().getAliasId() + "�Ų�̨���˵���Ϣ�Ѿ����£��ѵ����Ϣ��ˢ�£��µ����Ϣ���ᱣ��")
							.setNeutralButton("ȷ��",
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
							.setMessage(mBusinessException.getMessage())
							.setNeutralButton("ȷ��", null)
							.show();
					}
				}
			}	
		}
	}

	@Override
	public void onOrderChanged(Order oriOrder, List<OrderFood> newFoodList) {
		//set the table ID
		((EditText)findViewById(R.id.editText_orderActivity_tableNum)).setText(Integer.toString(oriOrder.getDestTbl().getAliasId()));
		//set the amount of customer
		((EditText)findViewById(R.id.editText_orderActivity_customerNum)).setText(Integer.toString(oriOrder.getCustomNum()));	

	}
	
}
