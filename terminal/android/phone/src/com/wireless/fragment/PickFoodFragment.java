package com.wireless.fragment;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.R;
import com.wireless.ui.dialog.AskOrderAmountDialog;
import com.wireless.ui.dialog.AskOrderAmountDialog.ActionType;

public class PickFoodFragment extends Fragment{
	private static final int REFRESH_FOODS = 43552;
	
	private static final String PICK_FOOD_FRAGMENT_TYPE_KEY = "pickFoodFragmentTag";
	private static final int PICK_FOOD_FRAGMENT_NUMBER = 0;
	private static final int PICK_FOOD_FRAGMENT_PINYIN = 1;

	private FoodHandler mHandler ;
	private GridView mGridView;

	private String mFilterCond = "";
	
	private static class FoodHandler extends Handler{
		private WeakReference<PickFoodFragment> mFragment;

		FoodHandler(PickFoodFragment fragment) {
			this.mFragment = new WeakReference<PickFoodFragment>(fragment);
		}
		
		@Override
		public void handleMessage(Message msg){
			PickFoodFragment fragment = mFragment.get();
			//�����в�Ʒ��������ɸѡ�����adapter
			fragment.mGridView.setAdapter(new PickFoodAdapter(fragment.getActivity(), WirelessOrder.foodMenu.foods.filter(fragment.mFilterCond)));
		}
	}
	
	public PickFoodFragment(){
		
	}
	
	public static PickFoodFragment newInstanceByNum(){
		PickFoodFragment fgm = new PickFoodFragment();
		Bundle args = new Bundle();
		args.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TYPE_KEY, PickFoodFragment.PICK_FOOD_FRAGMENT_NUMBER);
		fgm.setArguments(args);
		return fgm;
	}
	
	public static PickFoodFragment newInstanceByPinyin(){
		PickFoodFragment fgm = new PickFoodFragment();
		Bundle args = new Bundle();
		args.putInt(PickFoodFragment.PICK_FOOD_FRAGMENT_TYPE_KEY, PickFoodFragment.PICK_FOOD_FRAGMENT_PINYIN);
		fgm.setArguments(args);
		return fgm;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHandler = new FoodHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pick_food_by_pinyin_fgm, container, false);
		Bundle args = getArguments();
		
		//������
        final EditText searchTxtView = (EditText)view.findViewById(R.id.editText_search_pickFoodFragment);
        //������������
        if(args.getInt(PICK_FOOD_FRAGMENT_TYPE_KEY) == PICK_FOOD_FRAGMENT_NUMBER){
        	searchTxtView.setInputType(InputType.TYPE_CLASS_NUMBER);
            searchTxtView.setHint("������������");
        }else{
        	searchTxtView.setInputType(InputType.TYPE_CLASS_TEXT);
        	searchTxtView.setHint("���������ֻ�ƴ������");
        }
        
        mGridView = (GridView) view.findViewById(R.id.gridView_foods_pickFoodFragment);
        //���õ������
        mGridView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Food food = (Food)view.getTag();
				if(food.isSellOut()){
					Toast.makeText(getActivity(), food.getName() + "������", Toast.LENGTH_SHORT).show();
					
				}else if(food.isCurPrice()){
					final EditText currentPriceEdtTxt = new EditText(getActivity());
					currentPriceEdtTxt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
					Dialog currentPriceDialog = new AlertDialog.Builder(getActivity()).setTitle("��ȷ��" + food.getName() + "��ʱ��")
						.setView(currentPriceEdtTxt)
						.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								AskOrderAmountDialog.newInstance(food, FoodUnit.newInstance4CurPrice(Float.parseFloat(currentPriceEdtTxt.getText().toString())), ActionType.ADD, getId()).show(getFragmentManager(), AskOrderAmountDialog.TAG);
							}
						})
						.setNegativeButton("ȡ��", null)
						.create();
					//��������̲�ȫѡ���������
					currentPriceDialog.setOnShowListener(new DialogInterface.OnShowListener() {
						@Override
						public void onShow(DialogInterface arg0) {
							currentPriceEdtTxt.setText(NumericUtil.float2String2(food.getPrice()));
							currentPriceEdtTxt.setSelection(0, currentPriceEdtTxt.getText().length());
	                        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(currentPriceEdtTxt, InputMethodManager.SHOW_IMPLICIT);
						}
					});
					currentPriceDialog.show();
					
				}else{
					AskOrderAmountDialog.newInstance(food, ActionType.ADD, getId()).show(getFragmentManager(), AskOrderAmountDialog.TAG);
				}
			}
        });
        
        searchTxtView.addTextChangedListener(new TextWatcher(){
        	
        	Runnable mSrchHandler = new Runnable(){
        		@Override
        		public void run(){
        			mHandler.sendEmptyMessage(REFRESH_FOODS);
        		}
        	};
        	
			@Override 
			public void afterTextChanged(Editable s) {}
			
			@Override
			public void beforeTextChanged(CharSequence s,int start, int count, int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.toString().trim().length() != 0){
					mFilterCond  = s.toString().trim();
					
					searchTxtView.removeCallbacks(mSrchHandler);
					
					//���������ţ�����ִ��������
					//�����ӳ�500msִ������
				    if(Pattern.compile("[0-9]*").matcher(mFilterCond).matches()){;   
				    	searchTxtView.postDelayed(mSrchHandler, 500);				    
				    }else{
						mHandler.sendEmptyMessage(REFRESH_FOODS);
				    }
				}else{
					mFilterCond = "";
					mHandler.sendEmptyMessage(REFRESH_FOODS);
				}
			}
		});
        
		//ɾ������������ť
		((ImageButton) view.findViewById(R.id.imageButton_delete_pickFoodFragment)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				searchTxtView.setText("");
			}
		});
		
		/**
		 * ��ƷList����ʱ���������
		 */
		mGridView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchTxtView.getWindowToken(), 0);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		
		//ˢ�²�Ʒ
		mHandler.sendEmptyMessage(REFRESH_FOODS);
        return view;
	}
	
	//�ر�ʱ�������
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mGridView.setOnScrollListener(null);
	}

	//��Ʒ��ʾ��adapter
	static class PickFoodAdapter extends BaseAdapter{

		private final List<Food> mFoods;
		private final Context mContext;

		PickFoodAdapter(Context context, List<Food> foods){
			mContext = context;
			mFoods = foods;
		}
		
		@Override
		public int getCount() {
			return mFoods.size() >= 100 ? 100 : mFoods.size();
		}

		@Override
		public Object getItem(int position) {
			return mFoods.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if(convertView == null){
				view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pick_food_by_pinyin_fgm_item, parent, false);
			}else{
				view = convertView;
			}
			
			Food food = mFoods.get(position);
			view.setTag(food);
			//�������̫�����10�ض�
			if(food.getName().length() >= 10){
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.getName().substring(0, 10));
			}else{
				((TextView) view.findViewById(R.id.textView_foodName_pickFoodFragment_item)).setText(food.getName());
			}

			((TextView) view.findViewById(R.id.textView_num_pickFoodFragment_item)).setText(Integer.toString(food.getAliasId()));
			((TextView) view.findViewById(R.id.textView_price_pickFoodFragment_item)).setText(NumericUtil.float2String2(food.getPrice()));
			
			if(food.isLimit()){
				//���������������ʾ
				view.findViewById(R.id.linearLayout_limit_pickFoodFgm_item).setVisibility(View.VISIBLE);
				view.findViewById(R.id.textView_status_pickFoodFgm_item).setVisibility(View.GONE);
				((TextView)view.findViewById(R.id.textView_limitAmount_pickFoodFgm_item)).setText("��" + food.getLimitAmount());
				((TextView)view.findViewById(R.id.textView_limitRemaining_pickFoodFgm_item)).setText("ʣ" + food.getLimitRemaing());

			}else{ 
				view.findViewById(R.id.linearLayout_limit_pickFoodFgm_item).setVisibility(View.GONE);
				view.findViewById(R.id.textView_status_pickFoodFgm_item).setVisibility(View.VISIBLE);
				if(food.isSellOut()){
					//������������ʾ
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setText("ͣ");
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setTextColor(mContext.getResources().getColor(R.color.red));
					
				}else if(food.isCurPrice()){
					//����ʱ�۵���ʾ
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setText("ʱ");
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setTextColor(mContext.getResources().getColor(R.color.brown));
					
				}else if(food.isGift() && WirelessOrder.loginStaff.getRole().hasPrivilege(Privilege.Code.GIFT)){
					//�������͵���ʾ
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setText("��");
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setTextColor(mContext.getResources().getColor(R.color.maroon));
					
				}else if(food.isCombo()){
					//�����ײ˵���ʾ
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setText("��");
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setTextColor(mContext.getResources().getColor(R.color.green));
					
				}else if(food.isWeight()){
					//���ó��ص���ʾ
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setText("��");
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setTextColor(mContext.getResources().getColor(R.color.green));
					
				}else if(food.isSpecial()){
					//�����ؼ۵���ʾ
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setText("��");
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setTextColor(mContext.getResources().getColor(R.color.green));
					
				}else{
					((TextView)view.findViewById(R.id.textView_status_pickFoodFgm_item)).setVisibility(View.GONE);
				}
			}
			return view;
		}
	}
}
