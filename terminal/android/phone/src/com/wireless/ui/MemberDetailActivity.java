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
			
			//设置会员姓名
			((TextView)theActivity.findViewById(R.id.txtView_name_memberDetail)).setText(theActivity.mMember.getName());
			//设置会员类型
			((TextView)theActivity.findViewById(R.id.txtView_type_memberDetail)).setText(theActivity.mMember.getMemberType().getName());
			//设置电话号码
			((TextView)theActivity.findViewById(R.id.txtView_mobile_memberDetail)).setText(theActivity.mMember.getMobile());
			//设置光顾次数
			((TextView)theActivity.findViewById(R.id.txtView_content_1_memberDetail)).setText("光顾" + theActivity.mMember.getConsumptionAmount() + "次");
			//FIXME 设置最近一次消费
			((TextView)theActivity.findViewById(R.id.txtView_content_2_memberDetail)).setText("最近一次光顾在10月1日");
			
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
						((TextView)layout.findViewById(R.id.txtView_desc_memberDetailItem)).setText("Ta喜欢的菜品");
						foodGridView.setAdapter(theActivity.new FoodAdaptor(theActivity.mMember.getFavorFoods()));
						
					}else if(position == 1){
						((TextView)layout.findViewById(R.id.txtView_desc_memberDetailItem)).setText("向Ta推荐");
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
		
		//标题
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("会员信息");

		//返回Button
		TextView left = (TextView) findViewById(R.id.textView_left);
		left.setText("返回");
		left.setVisibility(View.VISIBLE);

		ImageButton back = (ImageButton) findViewById(R.id.btn_left);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		//快点Button
		TextView right = (TextView)findViewById(R.id.textView_right);
		right.setVisibility(View.VISIBLE);
		right.setText("快点");
		
		ImageButton quick = (ImageButton) findViewById(R.id.btn_right);
		quick.setVisibility(View.VISIBLE);
		quick.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//跳转到快点Activity
				Intent intent = new Intent(MemberDetailActivity.this, QuickPickActivity.class);
				if(mQuickOrder.hasOrderFood()){
					Bundle bundle = new Bundle();
					bundle.putParcelable(OrderParcel.KEY_VALUE, new OrderParcel(mQuickOrder));
					intent.putExtras(bundle);
				}
				startActivity(intent);
			}
		});
		
		//获取会员的详细信息
		MemberParcel memberParcel = getIntent().getParcelableExtra(MemberParcel.KEY_VALUE);
		mQueryMemberDetailTask = new QueryMemberDetailTask(memberParcel.asMember());
		mQueryMemberDetailTask.execute();
		
		//初始化会员的刷新Handler
		mRefreshMemberDetailHandler = new RefreshMemberDetailHandler(this);

		//初始化点菜数量的刷新Handler
		mRefreshOrderAmountHandler = new RefreshOrderAmountHandler(this);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mQueryMemberDetailTask.cancel(true);
	}
	
	/**
	 * 请求会员的详细信息
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
			_progDialog = ProgressDialog.show(MemberDetailActivity.this, "", "查询" + mMemberToQuery.getName() + "的详细信息...请稍候", true);
		}
		
		@Override
		protected void onPostExecute(Member result){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			mMember = result;
			
			if(mBusinessException != null){
				new AlertDialog.Builder(MemberDetailActivity.this)
							   .setTitle("提示")
							   .setMessage(mBusinessException.getMessage())
							   .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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
			
			//设置菜品名称
			checkBox.setText(f.getName());
			
			//设置菜品点击处理回调函数
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					if(buttonView.isChecked()){
						try {
							mQuickOrder.addFood(new OrderFood(f, 1), WirelessOrder.loginStaff);
							Toast.makeText(MemberDetailActivity.this, "添加" + f.getName(), Toast.LENGTH_SHORT).show();
						} catch (BusinessException ignored) {}
						
						buttonView.setBackgroundColor(getResources().getColor(R.color.orange));
					}else{
						try {
							mQuickOrder.remove(new OrderFood(f, 1), WirelessOrder.loginStaff);
							Toast.makeText(MemberDetailActivity.this, "删除" + f.getName(), Toast.LENGTH_SHORT).show();
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
