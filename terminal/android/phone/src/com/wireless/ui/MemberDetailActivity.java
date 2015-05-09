package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.parcel.MemberParcel;
import com.wireless.parcel.OrderParcel;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberComment;
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
			
			TextView txtViewAmount = ((TextView)theActivity.findViewById(R.id.txtView_amount_right_topBar));
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
			if(theActivity.mMember.getConsumptionAmount() > 0){
				//���ù�˴���
				((TextView)theActivity.findViewById(R.id.txtView_content_1_memberDetail)).setText("���" + theActivity.mMember.getConsumptionAmount() + "��");
				//�������һ������
				((TextView)theActivity.findViewById(R.id.txtView_content_2_memberDetail)).setText("���һ�ι����" + new SimpleDateFormat("M��d��", Locale.getDefault()).format(theActivity.mMember.getLastConsumption()));
			}else{
				((TextView)theActivity.findViewById(R.id.txtView_content_1_memberDetail)).setText("Ta��û��˹���Ŷ");
				((TextView)theActivity.findViewById(R.id.txtView_content_2_memberDetail)).setText("");
			}
			
			theActivity.findViewById(R.id.relativeLayout_favorFood_memberDetail).setVisibility(View.GONE);
			theActivity.findViewById(R.id.button_favorFood_memberDetail).setPressed(false);
			
			theActivity.findViewById(R.id.relativeLayout_comment_memberDetail).setVisibility(View.GONE);
			theActivity.findViewById(R.id.button_comment_memberDetail).setPressed(false);
			
			if(msg.what == theActivity.MEMBER_FAVOR_TAB){ //ˢ��"ϲ��"
				theActivity.mCurrentTab = theActivity.MEMBER_FAVOR_TAB;
				theActivity.findViewById(R.id.relativeLayout_favorFood_memberDetail).setVisibility(View.VISIBLE);
				theActivity.findViewById(R.id.button_favorFood_memberDetail).setPressed(true);

				//ˢ��"Taϲ���Ĳ�Ʒ"
				((TextView)theActivity.findViewById(R.id.linearLayout_favor_memberDetail).findViewById(R.id.txtView_desc_memberDetailItemBar)).setText("Taϲ���Ĳ�Ʒ");
				((GridView)theActivity.findViewById(R.id.gridView_favor_memberDetail)).setAdapter(theActivity.new FoodAdaptor(theActivity.mMember.getFavorFoods()));
				//ˢ��"��Ta�Ƽ�"
				((TextView)theActivity.findViewById(R.id.linearLayout_recommend_memberDetail).findViewById(R.id.txtView_desc_memberDetailItemBar)).setText("��Ta�Ƽ�");
				((GridView)theActivity.findViewById(R.id.gridView_recommend_memberDetail)).setAdapter(theActivity.new FoodAdaptor(theActivity.mMember.getRecommendFoods()));
				
			}else if(msg.what == theActivity.MEMBER_COMMENT_TAB){	
				theActivity.mCurrentTab = theActivity.MEMBER_COMMENT_TAB;
				//ˢ��"����"Tab
				theActivity.findViewById(R.id.relativeLayout_comment_memberDetail).setVisibility(View.VISIBLE);
				theActivity.findViewById(R.id.button_comment_memberDetail).setPressed(true);
				
				((TextView)theActivity.findViewById(R.id.linearLayout_publicComment_memberDetail).findViewById(R.id.txtView_desc_memberDetailItemBar)).setText("�������");
				((TextView)theActivity.findViewById(R.id.linearLayout_privateComment_memberDetail).findViewById(R.id.txtView_desc_memberDetailItemBar)).setText("�ҵ�����");
				
				ListView publicCommentListView = (ListView)theActivity.findViewById(R.id.listView_publicComment_memberDetail);
				//ˢ�¹�������
				final List<MemberComment> comments = new ArrayList<MemberComment>();
				for(MemberComment comment : theActivity.mMember.getPublicComments()){
					if(comment.getComment().trim().length() > 0){
						comments.add(comment);
					}
				}
				publicCommentListView.setAdapter(new BaseAdapter(){
					
					@Override
					public int getCount() {
						return comments.size();
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
							txtView.setTextColor(theActivity.getResources().getColor(R.color.brown));
							txtView.setSingleLine(true);
						}else{
							txtView = (TextView)convertView;
						}
	
						MemberComment comment = comments.get(position);
						txtView.setText(comment.getStaff().getName() + " " +
										new SimpleDateFormat("MM��dd��", Locale.getDefault()).format(comment.getLastModified()) + "˵ " +
										comment.getComment());
						
						return txtView;
					}
					
				});
				
				//ˢ��˽������
				if(theActivity.mMember.hasPrivateComment()){
					((TextView)theActivity.findViewById(R.id.txtView_privateCmment_memberDetail))
											.setText("��" +
												     new SimpleDateFormat("MM��dd��", Locale.getDefault()).format(theActivity.mMember.getPrivateComment().getLastModified()) + "˵ " +
													 theActivity.mMember.getPrivateComment().getComment());
				}else{
					((TextView)theActivity.findViewById(R.id.txtView_privateCmment_memberDetail)).setText("����û�����۹�TaŶ");
				}
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_detail_activity);
		
		//����
		TextView title = (TextView) findViewById(R.id.txtView_centralTitle_topBar);
		title.setVisibility(View.VISIBLE);
		title.setText("��Ա��Ϣ");

		//����Button
		TextView left = (TextView) findViewById(R.id.txtView_leftBtn_topBar);
		left.setText("����");
		left.setVisibility(View.VISIBLE);

		ImageButton back = (ImageButton) findViewById(R.id.imageButton_left_topBar);
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		//���Button
		TextView right = (TextView)findViewById(R.id.txtView_rightBtn_topBar);
		right.setVisibility(View.VISIBLE);
		right.setText("���");
		
		ImageButton quick = (ImageButton) findViewById(R.id.imageButton_right_topBar);
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
		
		//"ϲ��"Button
		View favorBtn = findViewById(R.id.button_favorFood_memberDetail);
		favorBtn.setPressed(true);
		favorBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRefreshMemberDetailHandler.sendEmptyMessage(MEMBER_FAVOR_TAB);
			}
		});
		
		//"����"Button
		View commentBtn = findViewById(R.id.button_comment_memberDetail);
		commentBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mRefreshMemberDetailHandler.sendEmptyMessage(MEMBER_COMMENT_TAB);
			}
		});
		
		//"����/˽��"���ۿ���
		final ToggleButton toggleBtnPublic = (ToggleButton)findViewById(R.id.toggleButton_setPublic_memberDetail);
		
		//"����"Button
		((Button)findViewById(R.id.button_commitComment_memberDetail)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				String commentValue = ((EditText)findViewById(R.id.editText_commitComment_memberDetail)).getText().toString();
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
			}
		});
	}
	
	@Override
	public void onRestart(){
		super.onRestart();
		mRefreshMemberDetailHandler.sendEmptyMessage(mCurrentTab);
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
	 * �ύ��������
	 */
	private class CommitMemberCommentTask extends com.wireless.lib.task.CommitMemberCommentTask{
		
		private ProgressDialog _progDialog;
		
		CommitMemberCommentTask(MemberComment.CommitBuilder builder){
			super(WirelessOrder.loginStaff, builder);
		}
		
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MemberDetailActivity.this, "", "���ڷ�������...���Ժ�", true);
		}
		
		@Override
		protected void onPostExecute(Void args){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
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
				mQueryMemberDetailTask = new QueryMemberDetailTask(mMember);
				mQueryMemberDetailTask.execute();
			}
		}
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
		public void onSuccess(Member member){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			mMember = member;
			//���������
			EditText editTxtComment = (EditText)findViewById(R.id.editText_commitComment_memberDetail);
			editTxtComment.setText("");
			((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(editTxtComment.getWindowToken(), 0);
			//�����������ҳ��
			mRefreshMemberDetailHandler.sendEmptyMessage(mCurrentTab);
		}

		@Override 
		public void onFail(BusinessException e){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			mMember = null;
			
			new AlertDialog.Builder(MemberDetailActivity.this)
			   .setTitle("��ʾ")
			   .setMessage(e.getMessage())
			   .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				   	public void onClick(DialogInterface dialog, int id) {
				   		dialog.dismiss();
				   	}
			   }).show();
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
				View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_detail_food_item, parent, false);
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
							//Toast.makeText(MemberDetailActivity.this, "���" + f.getName(), Toast.LENGTH_SHORT).show();
							buttonView.setBackgroundColor(getResources().getColor(R.color.orange));
						} catch (BusinessException e) {
							Toast.makeText(MemberDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
						}
						
					}else{
						mQuickOrder.delete(new OrderFood(f, 1));
						//Toast.makeText(MemberDetailActivity.this, "ɾ��" + f.getName(), Toast.LENGTH_SHORT).show();
						buttonView.setBackgroundColor(getResources().getColor(R.color.brown));
					}
					mRefreshOrderAmountHandler.sendEmptyMessage(0);
				}
			});
			
			return checkBox;
		}
	}
}
