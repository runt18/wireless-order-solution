package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.parcel.MemberParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberComment;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;

public class MemberDetailActivity extends FragmentActivity {
	
	private final int MEMBER_FAVOR_TAB = 0;
	private final int MEMBER_COMMENT_TAB = 1;
	private int mCurrentTab = MEMBER_FAVOR_TAB;
	
	public final static String KEY_MEMBER_ID = "KEY_MEMBER_ID";
	
	private Order mQuickOrder = new Order();
	
	private Member mMember;
	
	private QueryMemberDetailTask mQueryMemberDetailTask;
	
	private CommitMemberCommentTask mCommitMemberCommentTask;
	
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
			//FIXME 
			if(theActivity.mMember.getConsumptionAmount() > 0){
				//设置光顾次数
				((TextView)theActivity.findViewById(R.id.txtView_content_1_memberDetail)).setText("光顾" + theActivity.mMember.getConsumptionAmount() + "次");
				//设置最近一次消费
				((TextView)theActivity.findViewById(R.id.txtView_content_2_memberDetail)).setText("最近一次光顾在" + new SimpleDateFormat("M月d日", Locale.getDefault()).format(theActivity.mMember.getLastConsumption()));
			}else{
				((TextView)theActivity.findViewById(R.id.txtView_content_1_memberDetail)).setText("Ta还没光顾过你哦");
				((TextView)theActivity.findViewById(R.id.txtView_content_2_memberDetail)).setText("");
			}
			
			theActivity.findViewById(R.id.listView_favorFood_memberDetail).setVisibility(View.GONE);
			theActivity.findViewById(R.id.button_favorFood_memberDetail).setPressed(false);
			
			theActivity.findViewById(R.id.relativeLayout_comment_memberDetail).setVisibility(View.GONE);
			theActivity.findViewById(R.id.button_comment_memberDetail).setPressed(false);
			
			if(msg.what == theActivity.MEMBER_FAVOR_TAB){ //刷新"喜好"
				theActivity.mCurrentTab = theActivity.MEMBER_FAVOR_TAB;
				theActivity.findViewById(R.id.button_favorFood_memberDetail).setPressed(true);

				ListView memberDetailListView = (ListView)theActivity.findViewById(R.id.listView_favorFood_memberDetail);
				memberDetailListView.setVisibility(View.VISIBLE);
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
				
			}else if(msg.what == theActivity.MEMBER_COMMENT_TAB){	
				theActivity.mCurrentTab = theActivity.MEMBER_COMMENT_TAB;
				//刷新"评论"Tab
				theActivity.findViewById(R.id.relativeLayout_comment_memberDetail).setVisibility(View.VISIBLE);
				theActivity.findViewById(R.id.button_comment_memberDetail).setPressed(true);
				
				ListView publicCommentListView = (ListView)theActivity.findViewById(R.id.listView_publicComment_memberDetail);
				//刷新公开评论
				publicCommentListView.setAdapter(new BaseAdapter(){
					
					List<MemberComment> mComments = theActivity.mMember.getPublicComments();
					
					@Override
					public int getCount() {
						return mComments.size();
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
						final TextView txtView;
						if(convertView == null){
							txtView = new TextView(theActivity);
							txtView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
							txtView.setTextColor(theActivity.getResources().getColor(R.color.white));
						}else{
							txtView = (TextView)convertView;
						}
	
						MemberComment comment = mComments.get(position);
						txtView.setText(comment.getStaff().getName() + " " +
										new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(comment.getLastModified()) + "说 " +
										comment.getComment());
						
						return txtView;
					}
					
				});
				
				//刷新私人评论
				if(theActivity.mMember.hasPrivateComment()){
					((TextView)theActivity.findViewById(R.id.txtView_privateCmment_memberDetail))
											.setText("我" +
												     new SimpleDateFormat("MM月dd日", Locale.getDefault()).format(theActivity.mMember.getPrivateComment().getLastModified()) + "说 " +
													 theActivity.mMember.getPrivateComment().getComment());
				}else{
					((TextView)theActivity.findViewById(R.id.txtView_privateCmment_memberDetail)).setText("您还没有评论过Ta哦");
				}
			}
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
		
		//"喜好"Button
		View favorBtn = findViewById(R.id.button_favorFood_memberDetail);
		favorBtn.setPressed(true);
		favorBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRefreshMemberDetailHandler.sendEmptyMessage(MEMBER_FAVOR_TAB);
			}
		});
		
		//"评论"Button
		View commentBtn = findViewById(R.id.button_comment_memberDetail);
		commentBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRefreshMemberDetailHandler.sendEmptyMessage(MEMBER_COMMENT_TAB);
			}
		});
		
		//"公开/私人"评论开关
		final ToggleButton toggleBtnPublic = (ToggleButton)findViewById(R.id.toggleButton_setPublic_memberDetail);
		
		//"发表"Button
		((Button)findViewById(R.id.button_commitComment_memberDetail)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String commentValue = ((EditText)findViewById(R.id.editText_commitComment_memberDetail)).getText().toString().trim();
				if(commentValue.length() != 0){
					if(toggleBtnPublic.isChecked()){
						mCommitMemberCommentTask = new CommitMemberCommentTask(MemberComment.CommitBuilder.newPublicBuilder(WirelessOrder.loginStaff.getId(), 
								  																							mMember.getId(),
								  																							commentValue));
					}else{
						mCommitMemberCommentTask = new CommitMemberCommentTask(MemberComment.CommitBuilder.newPrivateBuilder(WirelessOrder.loginStaff.getId(), 
																															 mMember.getId(),
																															 commentValue));
					}
					mCommitMemberCommentTask.execute();
				}else{
					Toast.makeText(MemberDetailActivity.this, "还没有输入任何评论哦", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mQueryMemberDetailTask != null){
			mQueryMemberDetailTask.cancel(true);
		}
		if(mCommitMemberCommentTask != null){
			mCommitMemberCommentTask.cancel(true);
		}
	}
	
	/**
	 * 提交评论请求
	 */
	private class CommitMemberCommentTask extends com.wireless.lib.task.CommitMemberCommentTask{
		
		private ProgressDialog _progDialog;
		
		CommitMemberCommentTask(MemberComment.CommitBuilder builder){
			super(WirelessOrder.loginStaff, builder);
		}
		
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MemberDetailActivity.this, "", "正在发表评论...请稍候", true);
		}
		
		@Override
		protected void onPostExecute(Void args){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
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
				mQueryMemberDetailTask = new QueryMemberDetailTask(mMember);
				mQueryMemberDetailTask.execute();
			}
		}
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
				//隐藏软键盘
				EditText editTxtComment = (EditText)findViewById(R.id.editText_commitComment_memberDetail);
				editTxtComment.setText("");
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editTxtComment.getWindowToken(), 0);
				//重新请求更新页面
				mRefreshMemberDetailHandler.sendEmptyMessage(mCurrentTab);
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
