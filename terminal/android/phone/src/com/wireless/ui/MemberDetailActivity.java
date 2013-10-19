package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.parcel.MemberParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;

public class MemberDetailActivity extends FragmentActivity {
	
	public final static String KEY_MEMBER_ID = "KEY_MEMBER_ID";
	
	private Order mQuickOrder = new Order();
	
	private Member mMember;
	
	private QueryMemberDetailTask mQueryMemberDetailTask;
	
	private Handler mRefreshMemberDetailHandler;
	
	private Handler mRefreshOrderAmountHandler;
	
	private static class RefreshOrderAmountHandler extends Handler{
		private WeakReference<MemberDetailActivity> mActivity;
		
		RefreshOrderAmountHandler(MemberDetailActivity activity){
			mActivity = new WeakReference<MemberDetailActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg){
			final MemberDetailActivity theActivity = mActivity.get();
			
			TextView txtViewAmount = ((TextView)theActivity.findViewById(R.id.txtView_amount_right));
			if(theActivity.mQuickOrder.getOrderFoods().isEmpty()){
				txtViewAmount.setVisibility(View.GONE);
			}else{
				txtViewAmount.setVisibility(View.VISIBLE);
				txtViewAmount.setText(theActivity.mQuickOrder.getOrderFoods().size() + "");
			}
		}

	}
	
	private static class RefreshMemberDetailHandler extends Handler{
		
		private WeakReference<MemberDetailActivity> mActivity;
		
		RefreshMemberDetailHandler(MemberDetailActivity activity){
			mActivity = new WeakReference<MemberDetailActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg){

			final MemberDetailActivity theActivity = mActivity.get();
			
			//���û�Ա����
			((TextView)theActivity.findViewById(R.id.txtView_name_memberDetail)).setText(theActivity.mMember.getName());
			//���û�Ա����
			((TextView)theActivity.findViewById(R.id.txtView_type_memberDetail)).setText(theActivity.mMember.getMemberType().getName());
			//���õ绰����
			((TextView)theActivity.findViewById(R.id.txtView_mobile_memberDetail)).setText(theActivity.mMember.getMobile());
			//���ù�˴���
			((TextView)theActivity.findViewById(R.id.txtView_content_1_memberDetail)).setText("���" + theActivity.mMember.getConsumptionAmount() + "��");
			//FIXME �������һ������
			((TextView)theActivity.findViewById(R.id.txtView_content_2_memberDetail)).setText("���һ�ι����10��1��");
			
			ListView memberDetailListView = (ListView)theActivity.findViewById(R.id.listView_memberDetail);
			memberDetailListView.setAdapter(new BaseAdapter(){

				@Override
				public int getCount() {
					return 2;
				}

				@Override
				public Object getItem(int position) {
					return null;
				}

				@Override
				public long getItemId(int position) {
					return position;
				}

				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					final View layout;
					if(convertView == null){
						layout = LayoutInflater.from(theActivity.getApplicationContext()).inflate(R.layout.member_detail_item, null);
					}else{
						layout = convertView;
					}

					final GridView foodGridView = (GridView)layout.findViewById(R.id.gridView_memberDetailItem);

					if(position == 0){
						((TextView)layout.findViewById(R.id.txtView_desc_memberDetailItem)).setText("Taϲ���Ĳ�Ʒ");
						foodGridView.setAdapter(theActivity.new FoodAdaptor(theActivity.mMember.getFavorFoods()));
						
					}else if(position == 1){
						((TextView)layout.findViewById(R.id.txtView_desc_memberDetailItem)).setText("��Ta�Ƽ�");
						foodGridView.setAdapter(theActivity.new FoodAdaptor(theActivity.mMember.getRecommendFoods()));
					}
					
					((LinearLayout)layout.findViewById(R.id.linearLayout_title_memberDetailItem)).setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							ImageView arrowImgView = (ImageView)layout.findViewById(R.id.imgView_arrow_memberDetailItem);
							
							if(foodGridView.isEnabled()){
								arrowImgView.setImageResource(R.drawable.arrow_up);
								foodGridView.setVisibility(View.GONE);
								foodGridView.setEnabled(false);
							}else{
								arrowImgView.setImageResource(R.drawable.arrow_down);
								foodGridView.setVisibility(View.VISIBLE);
								foodGridView.setEnabled(true);
							}
						}
						
					});
			
					
					return layout;
				}
				
			});
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_detail_activity);
		
		//����
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("��Ա��Ϣ");

		//����Button
		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("����");
		left.setVisibility(View.VISIBLE);

		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		//���Button
		TextView right = (TextView)findViewById(R.id.textView_right);
		right.setVisibility(View.VISIBLE);
		right.setText("���");
		
		ImageButton quick = (ImageButton) findViewById(R.id.btn_right);
		quick.setVisibility(View.VISIBLE);
		quick.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//��ת�����Activity
				Intent intent = new Intent(MemberDetailActivity.this, QuickPickActivity.class);
				if(mQuickOrder.hasOrderFood()){
					Bundle bundle = new Bundle();
					bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(mQuickOrder));
					intent.putExtras(bundle);
				}
				startActivity(intent);
			}
		});
		
		//��ȡ��Ա����ϸ��Ϣ
		MemberParcel memberParcel = getIntent().getParcelableExtra(MemberParcel.KEY_VALUE);
		mQueryMemberDetailTask = new QueryMemberDetailTask(memberParcel.asMember());
		mQueryMemberDetailTask.execute();
		
		//��ʼ����Ա��ˢ��Handler
		mRefreshMemberDetailHandler = new RefreshMemberDetailHandler(this);

		//��ʼ�����������ˢ��Handler
		mRefreshOrderAmountHandler = new RefreshOrderAmountHandler(this);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mQueryMemberDetailTask.cancel(true);
	}
	
	/**
	 * �����Ա����ϸ��Ϣ
	 */
	private class QueryMemberDetailTask extends com.wireless.lib.task.QueryMemberDetailTask{
		
		private ProgressDialog _progDialog;
		
		private final Member mMemberToQuery;
		
		QueryMemberDetailTask(Member memberToQuery){
			super(WirelessOrder.loginStaff, memberToQuery, WirelessOrder.foodMenu.foods);
			mMemberToQuery = memberToQuery;
		}
		
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MemberDetailActivity.this, "", "��ѯ" + mMemberToQuery.getName() + "����ϸ��Ϣ...���Ժ�", true);
		}
		
		@Override
		protected void onPostExecute(Member result){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			mMember = result;
			
			if(mBusinessException != null){
				new AlertDialog.Builder(MemberDetailActivity.this)
							   .setTitle("��ʾ")
							   .setMessage(mBusinessException.getMessage())
							   .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
								   	public void onClick(DialogInterface dialog, int id) {
								   		dialog.dismiss();
								   	}
							   }).show();
				
			}else{
				mRefreshMemberDetailHandler.sendEmptyMessage(0);
			}
		}
	}
	
	private class FoodAdaptor extends BaseAdapter{
		
		private final List<Food> foods;
		
		FoodAdaptor(List<Food> foods){
			this.foods = foods;
		}
		
		@Override
		public int getCount() {
			return foods.size() > 6 ? 6 : foods.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final CheckBox checkBox;
			
			if(convertView == null){
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_detail_food_item, null);
				checkBox = (CheckBox) view;
			}else{
				checkBox = (CheckBox)convertView;
			}
			
			final Food f = foods.get(position);
			
			//���ò�Ʒ����
			checkBox.setText(f.getName());
			
			//���ò�Ʒ�������ص�����
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					if(buttonView.isChecked()){
						try {
							mQuickOrder.addFood(new OrderFood(f, 1), WirelessOrder.loginStaff);
							Toast.makeText(MemberDetailActivity.this, "���" + f.getName(), Toast.LENGTH_SHORT).show();
						} catch (BusinessException ignored) {}
						
						buttonView.setBackgroundColor(getResources().getColor(R.color.orange));
					}else{
						try {
							mQuickOrder.remove(new OrderFood(f, 1), WirelessOrder.loginStaff);
							Toast.makeText(MemberDetailActivity.this, "ɾ��" + f.getName(), Toast.LENGTH_SHORT).show();
						} catch (BusinessException ignored) {}
						
						buttonView.setBackgroundColor(getResources().getColor(R.color.brown));
					}
					mRefreshOrderAmountHandler.sendEmptyMessage(0);
				}
			});
			
			return checkBox;
		}
	}
}
