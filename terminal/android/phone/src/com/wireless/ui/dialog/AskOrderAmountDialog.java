package com.wireless.ui.dialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.parcel.ComboOrderFoodParcel;
import com.wireless.parcel.FoodUnitParcel;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.TasteGroupParcel;
import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodUnit;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.PickTasteActivity;
import com.wireless.ui.R;
import com.wireless.ui.view.IndicatorView;
import com.wireless.ui.view.ScrollLayout;
import com.wireless.ui.view.ScrollLayout.OnViewChangedListener;

public class AskOrderAmountDialog extends DialogFragment {

	public static enum ActionType{
		ADD(1, "增加"),
		MODIFY(2, "修改");
		
		private final int val;
		private final String desc;
		ActionType(int val, String desc){
			this.val = val;
			this.desc = desc;
		} 
		
		public static ActionType valueOf(int val){
			for(ActionType type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return desc;
		}
	}
	
	public static interface OnFoodPickedListener {
		/**
		 * 当选中菜品后，回调此函数通知选中的Food信息
		 * @param of
		 *            选中Food的信息
		 */
		public void onFoodPicked(OrderFood of, ActionType type);

	}

	private static class DialogRefreshHandler extends Handler{
		
		private final WeakReference<AskOrderAmountDialog> mDlgFgm;
		
		public DialogRefreshHandler(AskOrderAmountDialog dlgFgm) {
			this.mDlgFgm = new WeakReference<AskOrderAmountDialog>(dlgFgm);
		}
		
		@Override
		public void handleMessage(Message msg) {
			AskOrderAmountDialog thisDlgFgm = mDlgFgm.get();
			
			int index = (Integer)thisDlgFgm.mCurrentTasteView.getTag(R.id.combo_of_index);
			//设置Indicator
			((IndicatorView)thisDlgFgm.getView().findViewById(R.id.indicator_askOrderAmount_dialog)).setCurr(index);
			
			//设置套菜NaviBar
			LinearLayout comboLinearLayout = (LinearLayout)thisDlgFgm.getView().findViewById(R.id.linearLayout_comboFood_askOrderAmount_dialog);
			for(int i = 0; i < comboLinearLayout.getChildCount(); i++){
				if(i == index){
					comboLinearLayout.getChildAt(i).setBackgroundColor(thisDlgFgm.getResources().getColor(R.color.orange));
					((TextView)comboLinearLayout.getChildAt(i).findViewById(R.id.txtView_foodName_comboFood_item)).setTextColor(thisDlgFgm.getResources().getColor(R.color.black));
					((TextView)comboLinearLayout.getChildAt(i).findViewById(R.id.txtView_status_comboFood_item)).setTextColor(thisDlgFgm.getResources().getColor(R.color.black));
				}else{
					comboLinearLayout.getChildAt(i).setBackgroundColor(thisDlgFgm.getResources().getColor(R.color.dodger_blue));
					((TextView)comboLinearLayout.getChildAt(i).findViewById(R.id.txtView_foodName_comboFood_item)).setTextColor(thisDlgFgm.getResources().getColor(R.color.white));
					((TextView)comboLinearLayout.getChildAt(i).findViewById(R.id.txtView_status_comboFood_item)).setTextColor(thisDlgFgm.getResources().getColor(R.color.white));
				}
			}
			
			if((Boolean)thisDlgFgm.mCurrentTasteView.getTag(R.id.combo_of_indicator_key)){
				thisDlgFgm.getView().findViewById(R.id.linearLayout_top_askOrderAmount_dialog).setVisibility(View.INVISIBLE);
				thisDlgFgm.getView().findViewById(R.id.linearLayout_combo_askOrderAmount_dialog).setVisibility(View.VISIBLE);
				
				ComboOrderFood cof = (ComboOrderFood)thisDlgFgm.mCurrentTasteView.getTag(R.id.combo_of_key);
				//显示Title
				thisDlgFgm.getDialog().setTitle(thisDlgFgm.mSelectedFood.getName() + "#" + cof.getName());
				//显示数量和口味
				((TextView)thisDlgFgm.getView().findViewById(R.id.txtView_comboAmount_askOrderAmount_dialog)).setText(Integer.toString(cof.asComboFood().getAmount()) + "份");
				if(cof.hasTasteGroup()){
					((TextView)thisDlgFgm.getView().findViewById(R.id.txtView_comboTaste_askOrderAmount_dialog)).setText(cof.getTasteGroup().getPreference());
				}else{
					((TextView)thisDlgFgm.getView().findViewById(R.id.txtView_comboTaste_askOrderAmount_dialog)).setText("无口味");
				}
				if(cof.hasTmpTaste()){
					EditText pinzhuEdtTxt = ((EditText) thisDlgFgm.mCurrentTasteView.findViewById(R.id.edtTxt_pinzhu_askOrderAmount_dialog));
					pinzhuEdtTxt.removeTextChangedListener((TextWatcher)pinzhuEdtTxt.getTag());
					pinzhuEdtTxt.setText(cof.getTasteGroup().getTmpTastePref());
					pinzhuEdtTxt.addTextChangedListener((TextWatcher)pinzhuEdtTxt.getTag());
				}
				
			}else{
				thisDlgFgm.getDialog().setTitle(thisDlgFgm.mSelectedFood.toString());
				thisDlgFgm.getView().findViewById(R.id.linearLayout_combo_askOrderAmount_dialog).setVisibility(View.INVISIBLE);
				thisDlgFgm.getView().findViewById(R.id.linearLayout_top_askOrderAmount_dialog).setVisibility(View.VISIBLE);
				
				//显示叫起状态
				((ToggleButton) thisDlgFgm.getView().findViewById(R.id.toggleButton_askOrderAmount_hangUp)).setChecked(thisDlgFgm.mSelectedFood.isHangup());
				//显示赠送状态
				((ToggleButton) thisDlgFgm.getView().findViewById(R.id.toggleButton_askOrderAmount_gift)).setChecked(thisDlgFgm.mSelectedFood.isGift());
				
				if(thisDlgFgm.mSelectedFood.hasTmpTaste()){
					EditText pinzhuEdtTxt = ((EditText) thisDlgFgm.mCurrentTasteView.findViewById(R.id.edtTxt_pinzhu_askOrderAmount_dialog));
					pinzhuEdtTxt.removeTextChangedListener((TextWatcher)pinzhuEdtTxt.getTag());
					pinzhuEdtTxt.setText(thisDlgFgm.mSelectedFood.getTasteGroup().getTmpTastePref());
					pinzhuEdtTxt.addTextChangedListener((TextWatcher)pinzhuEdtTxt.getTag());
					
					EditText priceEdtTxt = ((EditText) thisDlgFgm.mCurrentTasteView.findViewById(R.id.edtTxt_pinzhuPrice_askOrderAmount_dialog));
					priceEdtTxt.removeTextChangedListener((TextWatcher)priceEdtTxt.getTag());
					priceEdtTxt.setText(NumericUtil.float2String2(thisDlgFgm.mSelectedFood.getTasteGroup().getTmpTastePrice()));
					priceEdtTxt.addTextChangedListener((TextWatcher)priceEdtTxt.getTag());
				}
			}
			if(msg.what == REFRESH_ALL){
				//刷新常用口味GridView
				((GridView)thisDlgFgm.mCurrentTasteView.findViewById(R.id.gridView_askOrderAmount_dialog)).invalidateViews();
			}
		}
		
	}
	
	private final static int REFRESH_ALL = 0;
	private final static int REFRESH_EXCEPT_GRID = 1;
	
	private static final int PICK_WITH_TASTE = 1;

	public final static String TAG = "AskOrderAmountDialog";

	private OrderFood mSelectedFood;

	private View mCurrentTasteView;
	
	private DialogRefreshHandler mRefreshHandler;
	
	private final List<OnFoodPickedListener> mFoodPickedListener = new ArrayList<OnFoodPickedListener>();

	private ActionType mActionType;
	
	private final static String PARENT_FGM_ID_KEY = "ParentIdKey";
	private final static String ACTION_TYPE_KEY = "ActionTypeKey";
	
	private int mParentFgmId;

	public static AskOrderAmountDialog newInstance(Food food, FoodUnit unit4Current, ActionType actionType, int parentId) {
		if(!food.isCurPrice()){
			throw new IllegalArgumentException(food.getName() + "不是时价菜品");
		}
		AskOrderAmountDialog fgm = new AskOrderAmountDialog();
		Bundle bundles = new Bundle();
		bundles.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(new OrderFood(food)));
		bundles.putParcelable(FoodUnitParcel.KEY_VALUE, new FoodUnitParcel(unit4Current));
		bundles.putInt(PARENT_FGM_ID_KEY, parentId);
		bundles.putInt(ACTION_TYPE_KEY, actionType.val);
		fgm.setArguments(bundles);
		return fgm;
	}
	
	public static AskOrderAmountDialog newInstance(Food food, ActionType actionType, int parentId) {
		return newInstance(new OrderFood(food), actionType, parentId);
	}

	public static AskOrderAmountDialog newInstance(OrderFood of, ActionType actionType, int parentId) {
		AskOrderAmountDialog fgm = new AskOrderAmountDialog();
		Bundle bundles = new Bundle();
		bundles.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(of));
		bundles.putInt(PARENT_FGM_ID_KEY, parentId);
		bundles.putInt(ACTION_TYPE_KEY, actionType.val);
		fgm.setArguments(bundles);
		return fgm;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the host
			mFoodPickedListener.add((OnFoodPickedListener)activity);
		} catch (ClassCastException ignored) {
			// The activity doesn't implement the interface, throw exception
			//throw new ClassCastException(activity.toString() + " must implement FoodPickedListener");
		}
		
		try{
			mFoodPickedListener.add((OnFoodPickedListener)getFragmentManager().findFragmentById(getArguments().getInt(PARENT_FGM_ID_KEY)));
		}catch (ClassCastException ignored) {}
		
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,	ViewGroup container, Bundle savedInstanceState) {

		mParentFgmId = getArguments().getInt(PARENT_FGM_ID_KEY);
		
		OrderFoodParcel orderFoodParcel = getArguments().getParcelable(OrderFoodParcel.KEY_VALUE);
		mSelectedFood = orderFoodParcel.asOrderFood();

		FoodUnitParcel unitParcel = getArguments().getParcelable(FoodUnitParcel.KEY_VALUE);
		if(unitParcel != null){
			mSelectedFood.setFoodUnit(unitParcel.asFoodUnit());
		}
		
		mActionType = ActionType.valueOf(getArguments().getInt(ACTION_TYPE_KEY));
		
		// Set title for this dialog
		getDialog().setTitle(mSelectedFood.toString());

		// Inflate the layout to use as dialog or embedded fragment
		final View view = inflater.inflate(R.layout.ask_order_amount_dialog, container, false);

		final EditText countEditText = (EditText) view.findViewById(R.id.editText_askOrderAmount_amount);

		if(mActionType == ActionType.ADD){
			countEditText.setText("1");
		}else{
			countEditText.setText(NumericUtil.float2String2(mSelectedFood.getCount()));
		}
		
		countEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				countEditText.setText(countEditText.getText());
				Selection.selectAll(countEditText.getText());
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(v, 0);
			}
		});

		// 数量加按钮
		((ImageButton) view.findViewById(R.id.button_askOrderAmount_plus)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					float curNum = Float.parseFloat(countEditText.getText().toString());
					curNum++;
					countEditText.setText(NumericUtil.float2String2(curNum));
				} catch (NumberFormatException ignored) {}
			}
		});

		// 数量减按钮
		((ImageButton) view.findViewById(R.id.button_askOrderAmount_minus)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					float curNum = Float.parseFloat(countEditText.getText().toString());
					if (--curNum >= 1.0f) {
						countEditText.setText(NumericUtil.float2String2(curNum));
					}
				} catch (NumberFormatException ignored) {

				}
			}
		});

		// "确定"Button
		Button okBtn = (Button) view.findViewById(R.id.button_askOrderAmount_confirm);
		okBtn.setText("确定");
		okBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					float orderAmount = Float.parseFloat(((EditText) getView().findViewById(R.id.editText_askOrderAmount_amount)).getText().toString());

					mSelectedFood.setCount(orderAmount);
					for(OnFoodPickedListener listener : mFoodPickedListener){
						listener.onFoodPicked(mSelectedFood, mActionType);
					}

					// Clear up the text to search box
					View srchEditText = getFragmentManager().findFragmentById(mParentFgmId).getView().findViewById(R.id.editText_search_pickFoodFragment);
					if (srchEditText != null) {
						((EditText) srchEditText).setText("");
					}

					dismiss();

				} catch (NumberFormatException e) {
					Toast.makeText(getActivity(), "您输入的数量格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
				}
			}
		});

		// "更多口味"Button
		Button tasteBtn = (Button) view.findViewById(R.id.button_askOrderAmount_taste);
		tasteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(getActivity(), PickTasteActivity.class);
				Bundle bundle = new Bundle();
				if((Boolean)mCurrentTasteView.getTag(R.id.combo_of_indicator_key)){
					bundle.putParcelable(ComboOrderFoodParcel.KEY_VALUE, new ComboOrderFoodParcel((ComboOrderFood)mCurrentTasteView.getTag(R.id.combo_of_key)));
				}else{
					bundle.putParcelable(OrderFoodParcel.KEY_VALUE,	new OrderFoodParcel(mSelectedFood));
				}
				bundle.putInt(PickTasteActivity.PICK_TASTE_INIT_FGM, PickTasteActivity.ALL_TASTE_FRAGMENT);
				intent.putExtras(bundle);
				AskOrderAmountDialog.this.startActivityForResult(intent, PICK_WITH_TASTE);
			}
		});

		// "取消"Button
		Button cancelBtn = (Button) view.findViewById(R.id.button_askOrderAmount_cancel);
		cancelBtn.setText("取消");
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		// "叫起"Toggle
		ToggleButton hangupToggle = (ToggleButton) view.findViewById(R.id.toggleButton_askOrderAmount_hangUp);
		hangupToggle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mSelectedFood.isHangup()){
					mSelectedFood.setHangup(false);
				}else{
					mSelectedFood.setHangup(true);;
					Toast.makeText(getActivity(), "叫起\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				}
				mRefreshHandler.sendEmptyMessage(REFRESH_EXCEPT_GRID);
			}
		});

		// "赠送"Toggle
		ToggleButton giftedToggle = (ToggleButton) view.findViewById(R.id.toggleButton_askOrderAmount_gift);
		if (WirelessOrder.loginStaff.getRole().hasPrivilege(Privilege.Code.GIFT) && mSelectedFood.asFood().isGift()) {
			giftedToggle.setVisibility(View.VISIBLE);
			giftedToggle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mSelectedFood.isGift()) {
						mSelectedFood.setGift(false);
					} else {
						mSelectedFood.setGift(true);
						Toast.makeText(getActivity(), "赠送\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
					}
					mRefreshHandler.sendEmptyMessage(REFRESH_EXCEPT_GRID);
				}
			});
		} else {
			giftedToggle.setVisibility(View.GONE);
		}

		final ScrollLayout scrollLayout = (ScrollLayout) view.findViewById(R.id.scrollLayout_askOrderAmount_dialog);

		scrollLayout.setOnViewChangedListener(new OnViewChangedListener() {
			
//			Handler waitHandler = new Handler();
//			Runnable refreshRunnable = new Runnable() {
//				@Override
//				public void run() {
//					//刷新数据
//					mRefreshHandler.sendEmptyMessage(REFRESH_EXCEPT_GRID);					
//				}
//			};
			
			@Override
			public void onViewChanged(int curScreen, View parent, View curView) {
				((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(scrollLayout.getWindowToken(), 0);
				mCurrentTasteView = curView;
				mCurrentTasteView.setTag(R.id.combo_of_index, Integer.valueOf(curScreen));
				
//				waitHandler.removeCallbacks(refreshRunnable);
//				waitHandler.postDelayed(refreshRunnable, 100);
				mRefreshHandler.sendEmptyMessage(REFRESH_EXCEPT_GRID);	
			}
		});

		//增加菜品的常用口味和品注
		scrollLayout.addView(mCurrentTasteView = setTasteView(container));
		mCurrentTasteView.setTag(R.id.combo_of_index, Integer.valueOf(0));
		
		//增加子菜的常用口味和品注(如果是套菜)
		if (mSelectedFood.asFood().isCombo()) {
			//显示套菜快速导航的主菜部分
			LinearLayout hsvContainer = (LinearLayout)view.findViewById(R.id.linearLayout_comboFood_askOrderAmount_dialog);
			View mainFoodView = inflater.inflate(R.layout.combo_food_item, hsvContainer, false);
			((TextView)mainFoodView.findViewById(R.id.txtView_status_comboFood_item)).setText("(主)");
			((TextView)(TextView)mainFoodView.findViewById(R.id.txtView_foodName_comboFood_item)).setText(mSelectedFood.asFood().getName().length() > 4 ? mSelectedFood.asFood().getName().substring(0, 4) : mSelectedFood.asFood().getName());
			
			mainFoodView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					scrollLayout.setToScreen(0);
				}
			});
			hsvContainer.addView(mainFoodView);
			
			int index = 1;
			for (final ComboFood eachChild : mSelectedFood.asFood().getChildFoods()) {
				
				scrollLayout.addView(setComboTasteView(container, eachChild));
				View childFoodView = inflater.inflate(R.layout.combo_food_item, hsvContainer, false);
				((TextView)childFoodView.findViewById(R.id.txtView_status_comboFood_item)).setText("(套" + index + ")");
				//显示套菜快速导航的子菜部分
				((TextView)childFoodView.findViewById(R.id.txtView_foodName_comboFood_item)).setText(eachChild.asFood().getName().length() > 4 ? eachChild.asFood().getName().substring(0, 4) : eachChild.asFood().getName());
				final int i = index++;
				childFoodView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						scrollLayout.setToScreen(i);
					}
				});
				hsvContainer.addView(childFoodView);
			}
			
			//显示Indicator
			((IndicatorView)view.findViewById(R.id.indicator_askOrderAmount_dialog)).setTotal(mSelectedFood.asFood().getChildFoods().size() + 1);
		}else{
			view.findViewById(R.id.indicator_askOrderAmount_dialog).setVisibility(View.GONE);
		}
		
		mRefreshHandler = new DialogRefreshHandler(this);
		mRefreshHandler.sendEmptyMessage(REFRESH_EXCEPT_GRID);
		
		return view;
	}

	private View setTasteView(final ViewGroup container) {
		final View tasteView = LayoutInflater.from(getActivity()).inflate(R.layout.ask_order_amount_dialog_taste, container, false);
		// 设置常用口味GridView
		GridView tasteGridView = (GridView) tasteView.findViewById(R.id.gridView_askOrderAmount_dialog);

		tasteView.setTag(R.id.combo_of_indicator_key, Boolean.FALSE);
		
		if (mSelectedFood.asFood().hasPopTastes() || mSelectedFood.asFood().hasFoodUnit()) {

			tasteGridView.setVisibility(View.VISIBLE);

			final List<Object> popTastes = new ArrayList<Object>();
			popTastes.addAll(mSelectedFood.asFood().getFoodUnits());
			popTastes.addAll(mSelectedFood.asFood().getPopTastes());
			if(popTastes.size() <= 8){
				tasteGridView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			}else{
				tasteGridView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
											 (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 134, getResources().getDisplayMetrics())));
			}

			tasteGridView.setAdapter(new BaseAdapter() {

				@Override
				public View getView(int position, View convertView,	ViewGroup parent) {
					CheckBox checkBox;
					if(convertView != null){
						checkBox = (CheckBox)convertView;
					}else{
						checkBox = (CheckBox) LayoutInflater.from(getActivity()).inflate(R.layout.ask_order_amount_dialog_item, container, false);
						checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
								if(buttonView.getTag() instanceof Taste){
									Taste taste = (Taste) buttonView.getTag();
									if (mSelectedFood.getTasteGroup().contains(taste)) {
										mSelectedFood.removeTaste(taste);
									} else {
										mSelectedFood.addTaste(taste);
									}
								}else if(buttonView.getTag() instanceof FoodUnit){
									final FoodUnit unit = (FoodUnit)buttonView.getTag();
									if(unit.equals(mSelectedFood.getFoodUnit())){
										mSelectedFood.setFoodUnit(null);
									}else{
										mSelectedFood.setFoodUnit(unit);
									}
								}
								mRefreshHandler.sendEmptyMessage(0);
							}
						});
					}
					
					if(popTastes.get(position) instanceof Taste){
						Taste thisTaste = (Taste)popTastes.get(position);
						checkBox.setTag(thisTaste);
						checkBox.setText(thisTaste.getPreference());
	
						if (mSelectedFood.getTasteGroup().contains(thisTaste)) {
							checkBox.setBackgroundColor(getResources().getColor(R.color.orange));
						} else {
							checkBox.setBackgroundColor(getResources().getColor(R.color.green));
						}
					}else if(popTastes.get(position) instanceof FoodUnit){
						FoodUnit thisUnit = (FoodUnit)popTastes.get(position);
						checkBox.setTag(thisUnit);
						checkBox.setText(NumericUtil.float2String2(thisUnit.getPrice()) + "元/" + thisUnit.getUnit());
	
						if (thisUnit.equals(mSelectedFood.getFoodUnit())){
							checkBox.setBackgroundColor(getResources().getColor(R.color.orange));
						} else {
							checkBox.setBackgroundColor(getResources().getColor(R.color.yellow));
						}
					}

					return checkBox;
				}

				@Override
				public long getItemId(int position) {
					return position;
				}

				@Override
				public Object getItem(int position) {
					return popTastes.get(position);
				}

				@Override
				public int getCount() {
					return popTastes.size();
				}
			});
		} else {
			tasteGridView.setVisibility(View.GONE);
		}

		// 品注的EditText的处理函数
		final EditText pinzhuEdtTxt = ((EditText) tasteView.findViewById(R.id.edtTxt_pinzhu_askOrderAmount_dialog));
		if(mSelectedFood.getTasteGroup().hasTmpTaste()){
			pinzhuEdtTxt.setText(mSelectedFood.getTasteGroup().getTmpTastePref());
		}
		TextWatcher pinzhuTxtWatcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				String pinzhu = s.toString().trim();
				float priceToPinzhu = 0;
				try{
					priceToPinzhu = Float.valueOf(((EditText) tasteView.findViewById(R.id.edtTxt_pinzhuPrice_askOrderAmount_dialog)).getText().toString().trim());
				}catch(NumberFormatException ignored){}
				
				if (pinzhu.length() != 0 || priceToPinzhu != 0) {
					mSelectedFood.setTmpTaste(Taste.newTmpTaste(pinzhu, priceToPinzhu));
				}else{
					mSelectedFood.setTmpTaste(null);
				}
				getDialog().setTitle(mSelectedFood.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

		};
		
		pinzhuEdtTxt.setTag(pinzhuTxtWatcher);
		pinzhuEdtTxt.addTextChangedListener(pinzhuTxtWatcher);

		//品注语音Button
		tasteView.findViewById(R.id.imgButton_voicePinZhu_askOrderAmount_dialog).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				voice2Pinzhu(pinzhuEdtTxt);
			}
		});
		
		// 价格EditText的处理函数
		final EditText priceEdtTxt = ((EditText) tasteView.findViewById(R.id.edtTxt_pinzhuPrice_askOrderAmount_dialog));
		if(mSelectedFood.getTasteGroup().hasTmpTaste() && mSelectedFood.getTasteGroup().getTmpTastePrice() != 0){
			priceEdtTxt.setText(NumericUtil.float2String2(mSelectedFood.getTasteGroup().getTmpTastePrice()));
		}
		TextWatcher priceTxtWatcher = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				float pinZhuPrice = 0;
				try{
					pinZhuPrice = Float.valueOf(s.toString());
				}catch (NumberFormatException ignored) {
					
				}
				String pinZhu = ((EditText) tasteView.findViewById(R.id.edtTxt_pinzhu_askOrderAmount_dialog)).getText().toString().trim();
				if (pinZhu.length() != 0 || pinZhuPrice != 0) {
					mSelectedFood.setTmpTaste(Taste.newTmpTaste(pinZhu, pinZhuPrice));
				}else{
					mSelectedFood.setTmpTaste(null);
				}
				getDialog().setTitle(mSelectedFood.toString());

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

		};
		
		priceEdtTxt.setTag(priceTxtWatcher);
		priceEdtTxt.addTextChangedListener(priceTxtWatcher);

		//品注价钱删除Button
		tasteView.findViewById(R.id.imgButton_deletePrice_askOrderAmount_dialog).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				priceEdtTxt.setText("");
			}
		});
		
		return tasteView;
	}

	private void voice2Pinzhu(final EditText pinzhuEdtTxt){
		
		//pinzhuEdtTxt.setText("");
		
		final RecognizerDialog iatDialog = new RecognizerDialog(getActivity(), new InitListener() {
			@Override
			public void onInit(int code) {
				if (code != ErrorCode.SUCCESS) {
					Toast.makeText(getActivity(), "初始化失败,错误码：" + code, Toast.LENGTH_SHORT).show();
	        	}
			}
		});
		// 清空参数
		iatDialog.setParameter(SpeechConstant.PARAMS, null);
		// 设置听写引擎
		iatDialog.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
		// 设置返回结果格式
		iatDialog.setParameter(SpeechConstant.RESULT_TYPE, "json");
		// 设置语言
		iatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		// 设置语言区域
		final String accent = getActivity().getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).getString(Params.ACCENT_LANGUAGE, Params.Accent.MANDARIN.val); 
		iatDialog.setParameter(SpeechConstant.ACCENT, accent);
		// 设置语音前端点
		iatDialog.setParameter(SpeechConstant.VAD_BOS, "4000");
		// 设置语音后端点
		iatDialog.setParameter(SpeechConstant.VAD_EOS, "1000");
		// 设置标点符号
		iatDialog.setParameter(SpeechConstant.ASR_PTT, "0");
		// 设置音频保存路径
		iatDialog.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/iflytek/wavaudio.pcm");
		
		iatDialog.setListener(new RecognizerDialogListener(){
			@Override
			public void onResult(RecognizerResult results, boolean isLast) {
				try{
					StringBuilder result = new StringBuilder();
					JSONObject joResult = new JSONObject(new JSONTokener(results.getResultString()));
					JSONArray words = joResult.getJSONArray("ws");
					for (int i = 0; i < words.length(); i++) {
						// 转写结果词，默认使用第一个结果
						JSONArray items = words.getJSONObject(i).getJSONArray("cw");
						JSONObject obj = items.getJSONObject(0);
						result.append(obj.getString("w"));
					}
					pinzhuEdtTxt.setText(result.toString());
				}catch(JSONException e){
					//Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
			
			/**
			 * 识别回调错误.
			 */
			@Override
			public void onError(SpeechError error) {
				Toast.makeText(getActivity(), (error.getPlainDescription(true)), Toast.LENGTH_SHORT).show();
			}
		});
		iatDialog.show();
		
		Toast.makeText(getActivity(), "您正在使用" + Params.Accent.valueOf(accent, 0).toString() + "输入", Toast.LENGTH_LONG).show();
	}
	
	private ComboOrderFood matchComboFood(ComboFood comboFood){
		for(ComboOrderFood cof : mSelectedFood.getCombo()){
			if(cof.asComboFood().equals(comboFood)){
				return cof;
			}
		}
		return null;
	}
	
	private View setComboTasteView(final ViewGroup container, final ComboFood comboFood){
		final View comboTasteView = LayoutInflater.from(getActivity()).inflate(R.layout.ask_order_amount_dialog_taste, container, false);
		//设置常用口味GridView
		GridView tasteGridView = (GridView)comboTasteView.findViewById(R.id.gridView_askOrderAmount_dialog);
    	
		//Check to see whether this combo food is contained in this order food.
		comboTasteView.setTag(R.id.combo_of_indicator_key, Boolean.TRUE);
		ComboOrderFood cof = matchComboFood(comboFood);
		if(cof != null){
			comboTasteView.setTag(R.id.combo_of_key, cof);
		}else{
			comboTasteView.setTag(R.id.combo_of_key, new ComboOrderFood(comboFood));
		}
		
		if(comboFood.asFood().hasPopTastes()){
			
			tasteGridView.setVisibility(View.VISIBLE);
			
			final List<Taste> popTastes = new ArrayList<Taste>(comboFood.asFood().getPopTastes());
			//只显示前8个常用口味
//			while(popTastes.size() > 8){
//				popTastes.remove(popTastes.size() - 1);
//			}
			
			if(popTastes.size() <= 8){
				tasteGridView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			}else{
				tasteGridView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
											 (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 134, getResources().getDisplayMetrics())));
			}
			
			tasteGridView.setAdapter(new BaseAdapter() {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					CheckBox checkBox;
					if(convertView != null){
						checkBox = (CheckBox)convertView;
					}else{
						checkBox = (CheckBox) LayoutInflater.from(getActivity()).inflate(R.layout.ask_order_amount_dialog_item, container, false);
						checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
							
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								//Check to see whether this combo food is contained in this order food.
								ComboOrderFood thisCombo = matchComboFood(comboFood);
								
								if(thisCombo == null){
									thisCombo = (ComboOrderFood)comboTasteView.getTag(R.id.combo_of_key);
									mSelectedFood.addCombo(thisCombo);
								}
								
								Taste taste = (Taste) buttonView.getTag();
								if(thisCombo.getTasteGroup().contains(taste)){
									thisCombo.removeTaste(taste);
								} else {
									thisCombo.addTaste(taste);
								}
								mRefreshHandler.sendEmptyMessage(0);
							}
						});
					}
					
					Taste thisTaste = popTastes.get(position);
					checkBox.setTag(thisTaste);
					checkBox.setText(thisTaste.getPreference());
					
					//Check to see whether this combo food is contained in this order food.
					ComboOrderFood thisCombo = matchComboFood(comboFood);
					
					if(thisCombo != null && thisCombo.getTasteGroup().contains(thisTaste)){
						checkBox.setBackgroundColor(getResources().getColor(R.color.orange));
					}else{
						checkBox.setBackgroundColor(getResources().getColor(R.color.green));
					}

					return checkBox;
				}
				
				@Override
				public long getItemId(int position) {
					return position;
				}
				
				@Override
				public Object getItem(int position) {
					return popTastes.get(position);
				}
				
				@Override
				public int getCount() {
					return popTastes.size();
				}
			});
		}else{
			tasteGridView.setVisibility(View.GONE);
		}

		//不显示品注价钱
		comboTasteView.findViewById(R.id.relativeLayout_pinzhuPrice_askOrderAmount_dialog).setVisibility(View.GONE);
		
		//品注的EditText的处理函数
		final EditText pinzhuEdtTxt = ((EditText)comboTasteView.findViewById(R.id.edtTxt_pinzhu_askOrderAmount_dialog));
		TextWatcher pinzhuTextWatcher = new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				//Check to see whether this combo food is contained in this order food.
				ComboOrderFood thisCombo = matchComboFood(comboFood);
				
				if(thisCombo == null && s.toString().trim().length() != 0){
					thisCombo = (ComboOrderFood)comboTasteView.getTag(R.id.combo_of_key);
					mSelectedFood.addCombo(thisCombo);
				}
				
				if(s.toString().trim().length() != 0){
					thisCombo.setTmpTaste(Taste.newTmpTaste(s.toString().trim(), 0f));
				}
				if(thisCombo.hasTasteGroup()){
					((TextView)getView().findViewById(R.id.txtView_comboTaste_askOrderAmount_dialog)).setText(thisCombo.getTasteGroup().getPreference());
				}else{
					((TextView)getView().findViewById(R.id.txtView_comboTaste_askOrderAmount_dialog)).setText("无口味");
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
		};
		pinzhuEdtTxt.setTag(pinzhuTextWatcher);
		pinzhuEdtTxt.addTextChangedListener(pinzhuTextWatcher);
		
		//品注语音Button
		comboTasteView.findViewById(R.id.imgButton_voicePinZhu_askOrderAmount_dialog).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				voice2Pinzhu(pinzhuEdtTxt);
			}
		});
		
		return comboTasteView;
	}

	public AskOrderAmountDialog() {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == PICK_WITH_TASTE) {

				TasteGroupParcel tgParcel = data.getParcelableExtra(TasteGroupParcel.KEY_VALUE);
				
				if(tgParcel.asTasteGroup() != null){
					if((Boolean)mCurrentTasteView.getTag(R.id.combo_of_indicator_key)){
						ComboOrderFood cof = (ComboOrderFood)mCurrentTasteView.getTag(R.id.combo_of_key);
						cof.setTasteGroup(tgParcel.asTasteGroup());
						//Add the combo order food if NOT contained in this order food before.
						if(matchComboFood(cof.asComboFood()) == null){
							mSelectedFood.addCombo(cof);
						}
					}else{
						mSelectedFood.setTasteGroup(tgParcel.asTasteGroup());
					}
				}else{
					if((Boolean)mCurrentTasteView.getTag(R.id.combo_of_indicator_key)){
						((ComboOrderFood)mCurrentTasteView.getTag(R.id.combo_of_key)).clearTasteGroup();
					}else{
						mSelectedFood.clearTasetGroup();
					}
				}
				
				mRefreshHandler.sendEmptyMessage(0);
				// ((Button)getView().findViewById(R.id.button_askOrderAmount_confirm)).performClick();
			}
		}
	}

}
