package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.parcel.MemberParcel;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.util.SortedList;

public class MemberListActivity extends FragmentActivity {

	private final static int ALL_MEMBER_PAGE = 0;
	private final static int INTERESTED_MEMBER_PAGE = 1;
	
	private int mCurrentPage = ALL_MEMBER_PAGE;
	
	private String mConditionFilter;
	
	private List<Member> mMembers;
	private SortedList<Member> mInterestedMembers;
	
	private MemberListHandler mMemberListHandler;
	
	private QueryMemberTask mQueryMemberTask;
	private QueryInterestedMemberTask mQueryInterestedTask;
	
	private ListView mMemberListView;
	
	private static class MemberListHandler extends Handler{
		private WeakReference<MemberListActivity> mActivity;
		private TextView mHintText;

		MemberListHandler(MemberListActivity activity){
			mActivity = new WeakReference<MemberListActivity>(activity);
			mHintText = (TextView) activity.findViewById(R.id.textView_hintText_memberList);
		}

		@Override
		public void handleMessage(Message msg) {
			
			MemberListActivity activity = mActivity.get();
			
			if(activity.mMembers == null || activity.mInterestedMembers == null){
				return;
			}
			
			MemberListAdapter adapter;
			activity.findViewById(R.id.button_all_memberList).setPressed(false);
			activity.findViewById(R.id.button_interested_memberList).setPressed(false);
			
			//设置底部数量显示
			((TextView)activity.findViewById(R.id.txtView_allMemberAmount_memberList)).setText("" + activity.mMembers.size());
			((TextView)activity.findViewById(R.id.txtView_interestedMemberAmount_memberList)).setText("" + activity.mInterestedMembers.size());
			
			switch(msg.what){
			case ALL_MEMBER_PAGE:
				activity.mCurrentPage = ALL_MEMBER_PAGE;
				adapter = activity.new MemberListAdapter(filter(activity.mMembers, activity.mConditionFilter));
				activity.findViewById(R.id.button_all_memberList).setPressed(true);
				break;
			case INTERESTED_MEMBER_PAGE:
				activity.mCurrentPage = INTERESTED_MEMBER_PAGE;
				adapter = activity.new MemberListAdapter(filter(activity.mInterestedMembers, activity.mConditionFilter));
				activity.findViewById(R.id.button_interested_memberList).setPressed(true);
				break;
			default:
				activity.mCurrentPage = ALL_MEMBER_PAGE;
				adapter = activity.new MemberListAdapter(activity.mMembers);
			}
			
			if(adapter.getCount() == 0)	{
				mHintText.setVisibility(View.VISIBLE);
			}else {
				mHintText.setVisibility(View.GONE);
			}
			
			activity.mMemberListView.setAdapter(adapter);
		}
		
		private List<Member> filter(List<Member> source, String filterCond){
			if(filterCond == null){
				return source;
				
			}else if(filterCond.length() == 0){
				return source;
				
			}else{
				List<Member> result;
				result = new ArrayList<Member>(source);
				Iterator<Member> iter = result.iterator();
				while(iter.hasNext()){
					Member member = iter.next();
					String cond = filterCond.toLowerCase(Locale.getDefault())
											.replace("，", "").replace(",", "")
											.replace("。", "").replace(".", "")
											.replace(" ", "");
					if(!(member.getName().toLowerCase(Locale.getDefault()).contains(cond) || 
					     //f.getPinyin().contains(cond) || 
					     //f.getPinyinShortcut().contains(cond) ||
					     String.valueOf(member.getMobile()).startsWith(cond))){
						
						iter.remove();
					}				
				}	
				
				//Sort the member by consumption amount
				Collections.sort(result, new Comparator<Member>(){

					@Override
					public int compare(Member lhs, Member rhs) {
						if(lhs.getConsumptionAmount() > rhs.getConsumptionAmount()){
							return -1;
						}else if(lhs.getConsumptionAmount() < rhs.getConsumptionAmount()){
							return 1;
						}else{
							return 0;
						}
					}
				
				});
				return result;
			}
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.member_list_activity);
		
		//返回Button和标题
		TextView title = (TextView) findViewById(R.id.toptitle);
		title.setVisibility(View.VISIBLE);
		title.setText("会员列表");

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
		
		findViewById(R.id.textView_right).setVisibility(View.GONE);
		findViewById(R.id.btn_right).setVisibility(View.GONE);
		
		//初始化MemberListHandler
		mMemberListHandler = new MemberListHandler(this);

		//更新会员信息
		mQueryMemberTask = new QueryMemberTask();
		mQueryMemberTask.execute();
		
		//更新关注的会员信息
		mQueryInterestedTask = new QueryInterestedMemberTask();
		mQueryInterestedTask.execute();
		
		//"全部会员"Button
		View onSaleBtn = findViewById(R.id.button_all_memberList);
		onSaleBtn.setPressed(true);
		onSaleBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMemberListHandler.sendEmptyMessage(ALL_MEMBER_PAGE);
			}
		});
		
		//"关注会员"Button
		View selloutBtn = findViewById(R.id.button_interested_memberList);
		selloutBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMemberListHandler.sendEmptyMessage(INTERESTED_MEMBER_PAGE);
			}
		});
		
		//set search text watcher
		final EditText searchEdit = (EditText) findViewById(R.id.txtView_search_memberList);
		
		searchEdit.addTextChangedListener(new TextWatcher(){
        	
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().trim().length() != 0){
					mConditionFilter = s.toString().trim();
				}else{
					mConditionFilter = "";
				}
				mMemberListHandler.sendEmptyMessage(mCurrentPage);
			}
		});
		
		//删除搜索条件按钮
		((ImageButton) findViewById(R.id.imgButton_searchClear_memberList)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchEdit.setText("");
			}
		});
		
		mMemberListView = (ListView) findViewById(R.id.listView_memberList);
		
		//滚动时隐藏soft-keyboard
		mMemberListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
	}
	
	@Override
	public void onRestart(){
		super.onRestart();
		EditText searchEdtText = ((EditText) findViewById(R.id.txtView_search_memberList));
		if(searchEdtText.getText().toString().length() == 0){
			mMemberListHandler.sendEmptyMessage(mCurrentPage);
		}else{
			searchEdtText.setText("");;
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mQueryMemberTask.cancel(true);
		mQueryInterestedTask.cancel(true);
	}
	
	private class MemberListAdapter extends BaseAdapter{
		private List<Member> mMembers;

		MemberListAdapter(List<Member> members){
			mMembers = members;
		}
		
		@Override
		public int getCount() {
			return mMembers.size() > 50 ? 50 : mMembers.size();
		}

		@Override
		public Object getItem(int position) {
			return mMembers.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View layout;
			if(convertView == null){
				layout = LayoutInflater.from(getApplicationContext()).inflate(R.layout.member_list_item, null);
			}else{
				layout = convertView;
			}
			
			final Member member = mMembers.get(position);
			
			layout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					//跳转到会员信息Activity
					Intent intent = new Intent(MemberListActivity.this, MemberDetailActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable(MemberParcel.KEY_VALUE, new MemberParcel(member));
					intent.putExtras(bundle);
					startActivity(intent);
				}
				
			});
			
			//"关注"or"取消关注"Button
			final Button button = (Button)layout.findViewById(R.id.button_interestedIn_memberList_listItem);
			button.setOnClickListener(new OnClickListener(){

				public void onClick(View v) {
					if(mCurrentPage == ALL_MEMBER_PAGE){
						if(!mInterestedMembers.containsElement(member)){
							mInterestedMembers.add(member);
							button.setBackgroundResource(R.drawable.member_list_has_interested_selector);
							new InterestedInMemberTask(member).execute();
						}
					}else if(mCurrentPage == INTERESTED_MEMBER_PAGE){
						if(mInterestedMembers.containsElement(member)){
							mInterestedMembers.removeElement(member);
							mMemberListHandler.sendEmptyMessage(INTERESTED_MEMBER_PAGE);
							new CancelInterestedInMemberTask(member).execute();
						}
					}
				}
				
			});
			
			if(mCurrentPage == ALL_MEMBER_PAGE){
				if(mInterestedMembers.containsElement(mMembers.get(position))){
					button.setBackgroundResource(R.drawable.member_list_has_interested_selector);
				}else{
					button.setBackgroundResource(R.drawable.member_list_interested_selector);
				}
			}else if(mCurrentPage == INTERESTED_MEMBER_PAGE){
				button.setBackgroundResource(R.drawable.member_list_not_interested_selector);
			}
			
			//设置姓名,会员类型,电话
			((TextView)layout.findViewById(R.id.txtView_name_memberList_listItem)).setText(mMembers.get(position).getName());
			((TextView)layout.findViewById(R.id.txtView_tele_memberList_listItem)).setText(mMembers.get(position).getMobile());
			((TextView)layout.findViewById(R.id.txtView_type_memberList_listItem)).setText(mMembers.get(position).getMemberType().getName());
			((TextView)layout.findViewById(R.id.txtView_consumptionAmount_memberList_listItem)).setText(mMembers.get(position).getConsumptionAmount() + "次光顾");

			return layout;
		}
	}
	
	/**
	 * 请求更新会员信息
	 */
	private class QueryMemberTask extends com.wireless.lib.task.QueryMemberTask{
		
		@Override
		protected void onPreExecute(){
			findViewById(R.id.progressBar_allMember_memberList).setVisibility(View.VISIBLE);
			findViewById(R.id.txtView_allMemberAmount_memberList).setVisibility(View.GONE);
		}
		
		QueryMemberTask(){
			super(WirelessOrder.loginStaff);
		}
		
		@Override
		protected void onPostExecute(List<Member> members){
			
			findViewById(R.id.progressBar_allMember_memberList).setVisibility(View.GONE);
			findViewById(R.id.txtView_allMemberAmount_memberList).setVisibility(View.VISIBLE);
			
			if(mBusinessException != null){
				Toast.makeText(MemberListActivity.this, "会员列表更新失败", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(MemberListActivity.this, "会员列表更新成功", Toast.LENGTH_SHORT).show();
				mMembers = members;
				mMemberListHandler.sendEmptyMessage(mCurrentPage);
			}
		}
	}
	
	/**
	 * 请求更新关注会员信息
	 */
	private class QueryInterestedMemberTask extends com.wireless.lib.task.QueryInterestedMemberTask{
		
		QueryInterestedMemberTask(){
			super(WirelessOrder.loginStaff);
		}
		
		@Override
		protected void onPreExecute(){
			findViewById(R.id.progressBar_interestedMember_memberList).setVisibility(View.VISIBLE);
			findViewById(R.id.txtView_interestedMemberAmount_memberList).setVisibility(View.GONE);
		}
		
		@Override
		protected void onPostExecute(List<Member> members){

			findViewById(R.id.progressBar_interestedMember_memberList).setVisibility(View.GONE);
			findViewById(R.id.txtView_interestedMemberAmount_memberList).setVisibility(View.VISIBLE);

			if(mBusinessException != null){
				Toast.makeText(MemberListActivity.this, "关注的会员列表更新失败", Toast.LENGTH_SHORT).show();				
			}else{
				Toast.makeText(MemberListActivity.this, "关注的会员列表更新成功", Toast.LENGTH_SHORT).show();
				mInterestedMembers = SortedList.newInstance(members);
				mMemberListHandler.sendEmptyMessage(mCurrentPage);
			}
		}
	}
	
	/**
	 * 请求关注会员
	 */
	private class InterestedInMemberTask extends com.wireless.lib.task.InterestedInMemberTask{
		
		private final Member mMemberToInterested;
		
		InterestedInMemberTask(Member memberToInterested){
			super(WirelessOrder.loginStaff, memberToInterested);
			mMemberToInterested = memberToInterested;
		}
		
		@Override
		protected void onPostExecute(Void result){
			Toast.makeText(MemberListActivity.this, "关注" + mMemberToInterested.getName() + "成功", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 请求关注会员
	 */
	private class CancelInterestedInMemberTask extends com.wireless.lib.task.CancelInterestedInMemberTask{
		
		private final Member mMemberToCancelInterested;
		
		CancelInterestedInMemberTask(Member memberToCancelInterested){
			super(WirelessOrder.loginStaff, memberToCancelInterested);
			mMemberToCancelInterested = memberToCancelInterested;
		}
		
		@Override
		protected void onPostExecute(Void result){
			Toast.makeText(MemberListActivity.this, "取消关注" + mMemberToCancelInterested.getName() + "成功", Toast.LENGTH_SHORT).show();
		}
	}
}
