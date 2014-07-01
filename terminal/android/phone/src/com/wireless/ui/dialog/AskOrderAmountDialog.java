package com.wireless.ui.dialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
import android.widget.Toast;
import android.widget.ToggleButton;

import com.wireless.common.WirelessOrder;
import com.wireless.parcel.ComboOrderFoodParcel;
import com.wireless.parcel.OrderFoodParcel;
import com.wireless.parcel.TasteGroupParcel;
import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.ui.PickTasteActivity;
import com.wireless.ui.R;
import com.wireless.ui.view.ScrollLayout;
import com.wireless.ui.view.ScrollLayout.OnViewChangedListener;

public class AskOrderAmountDialog extends DialogFragment {

	public static enum ActionType{
		ADD(1, "����"),
		MODIFY(2, "�޸�");
		
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
		 * ��ѡ�в�Ʒ�󣬻ص��˺���֪ͨѡ�е�Food��Ϣ
		 * @param food
		 *            ѡ��Food����Ϣ
		 */
		public void onFoodPicked(OrderFood food, ActionType type);

	}

	private static class DialogRefreshHandler extends Handler{
		
		private final WeakReference<AskOrderAmountDialog> mDlgFgm;
		
		public DialogRefreshHandler(AskOrderAmountDialog dlgFgm) {
			this.mDlgFgm = new WeakReference<AskOrderAmountDialog>(dlgFgm);
		}
		
		@Override
		public void handleMessage(Message msg) {
			AskOrderAmountDialog thisDlgFgm = mDlgFgm.get();
			//����Dialog��Title
			if((Boolean)thisDlgFgm.mCurrentTasteView.getTag(R.id.combo_of_indicator_key)){
				ComboOrderFood cof = (ComboOrderFood)thisDlgFgm.mCurrentTasteView.getTag(R.id.combo_of_key);
				thisDlgFgm.getDialog().setTitle(cof.toString());
				if(cof.hasTmpTaste()){
					EditText pinzhuEdtTxt = ((EditText) thisDlgFgm.mCurrentTasteView.findViewById(R.id.edtTxt_pinzhu_askOrderAmount_dialog));
					pinzhuEdtTxt.removeTextChangedListener((TextWatcher)pinzhuEdtTxt.getTag());
					pinzhuEdtTxt.setText(cof.getTasteGroup().getTmpTastePref());
					pinzhuEdtTxt.addTextChangedListener((TextWatcher)pinzhuEdtTxt.getTag());
				}
				
			}else{
				thisDlgFgm.getDialog().setTitle(thisDlgFgm.mSelectedFood.toString());
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
			//ˢ�³��ÿ�ζGridView
			//((BaseAdapter)((GridView)mCurrentTasteView.findViewById(R.id.gridView_askOrderAmount_dialog)).getAdapter()).notifyDataSetChanged();
			((GridView)thisDlgFgm.mCurrentTasteView.findViewById(R.id.gridView_askOrderAmount_dialog)).invalidateViews();
		}
	}
	
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

	public static AskOrderAmountDialog newInstance(Food food, ActionType actionType, int parentId) {
		AskOrderAmountDialog fgm = new AskOrderAmountDialog();
		Bundle bundles = new Bundle();
		bundles.putParcelable(OrderFoodParcel.KEY_VALUE, new OrderFoodParcel(new OrderFood(food)));
		bundles.putInt(PARENT_FGM_ID_KEY, parentId);
		bundles.putInt(ACTION_TYPE_KEY, actionType.val);
		fgm.setArguments(bundles);
		return fgm;
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
		
		// �������EditText��ȫѡ���ݲ����������
		countEditText.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				countEditText.selectAll();
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(v, 0);
				return true;
			}

		});

		// �����Ӱ�ť
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

		// ��������ť
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

		// "ȷ��"Button
		Button okBtn = (Button) view.findViewById(R.id.button_askOrderAmount_confirm);
		okBtn.setText("ȷ��");
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
					Toast.makeText(getActivity(), "�������������ʽ����ȷ������������", Toast.LENGTH_SHORT).show();
				}
			}
		});

		// "�����ζ"Button
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

		// "ȡ��"Button
		Button cancelBtn = (Button) view.findViewById(R.id.button_askOrderAmount_cancel);
		cancelBtn.setText("ȡ��");
		cancelBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		// "����"Toggle
		ToggleButton hangupToggle = (ToggleButton) view.findViewById(R.id.toggleButton_askOrderAmount_hangUp);
		hangupToggle.setChecked(mSelectedFood.isHangup());
		hangupToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				if (isChecked) {
					mSelectedFood.setHangup(true);
					Toast.makeText(getActivity(), "����\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
				} else {
					mSelectedFood.setHangup(false);
				}
			}
		});

		// "����"Toggle
		ToggleButton giftedToggle = (ToggleButton) view.findViewById(R.id.toggleButton_askOrderAmount_gift);
		if (WirelessOrder.loginStaff.getRole().hasPrivilege(Privilege.Code.GIFT) && mSelectedFood.asFood().isGift()) {
			giftedToggle.setVisibility(View.VISIBLE);
			giftedToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					if (isChecked) {
						mSelectedFood.setGift(true);
						Toast.makeText(getActivity(), "����\"" + mSelectedFood.toString() + "\"", Toast.LENGTH_SHORT).show();
					} else {
						mSelectedFood.setGift(false);
					}
				}
			});
		} else {
			giftedToggle.setVisibility(View.GONE);
		}

		final ScrollLayout scrollLayout = (ScrollLayout) view.findViewById(R.id.scrollLayout_askOrderAmount_dialog);

		scrollLayout.setOnViewChangedListener(new OnViewChangedListener() {
			@Override
			public void onViewChanged(int curScreen, View parent, View curView) {
				((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(scrollLayout.getWindowToken(), 0);
				mCurrentTasteView = curView;
				//����Dialog��Title
				if((Boolean)mCurrentTasteView.getTag(R.id.combo_of_indicator_key)){
					getDialog().setTitle(((ComboOrderFood)mCurrentTasteView.getTag(R.id.combo_of_key)).toString());
				}else{
					getDialog().setTitle(mSelectedFood.toString());
				}
			}
		});

		//���Ӳ�Ʒ�ĳ��ÿ�ζ��Ʒע
		scrollLayout.addView(mCurrentTasteView = setTasteView());
		
		//�����Ӳ˵ĳ��ÿ�ζ��Ʒע(������ײ�)
		if (mSelectedFood.asFood().isCombo()) {
			for (ComboFood combo : mSelectedFood.asFood().getChildFoods()) {
				scrollLayout.addView(setComboTasteView(combo));
			}
		}
		
		mRefreshHandler = new DialogRefreshHandler(this);
		
		return view;
	}

	private View setTasteView() {
		final View tasteView = LayoutInflater.from(getActivity()).inflate(R.layout.ask_order_amount_dialog_taste, null);
		// ���ó��ÿ�ζGridView
		GridView tasteGridView = (GridView) tasteView.findViewById(R.id.gridView_askOrderAmount_dialog);

		tasteView.setTag(R.id.combo_of_indicator_key, Boolean.FALSE);
		
		if (mSelectedFood.asFood().hasPopTastes()) {

			tasteGridView.setVisibility(View.VISIBLE);

			final List<Taste> popTastes = new ArrayList<Taste>(mSelectedFood.asFood().getPopTastes());
			// ֻ��ʾǰ8�����ÿ�ζ
			while (popTastes.size() > 8) {
				popTastes.remove(popTastes.size() - 1);
			}

			tasteGridView.setAdapter(new BaseAdapter() {

				@Override
				public View getView(int position, View convertView,	ViewGroup parent) {
					CheckBox checkBox = (CheckBox) LayoutInflater.from(getActivity()).inflate(R.layout.ask_order_amount_dialog_item, null);
					Taste thisTaste = popTastes.get(position);
					checkBox.setTag(thisTaste);
					checkBox.setText(thisTaste.getPreference());

					if (mSelectedFood.getTasteGroup().contains(thisTaste)) {
						checkBox.setBackgroundColor(getResources().getColor(R.color.orange));
					} else {
						checkBox.setBackgroundColor(getResources().getColor(R.color.green));
					}

					checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
							Taste taste = (Taste) buttonView.getTag();
							if (mSelectedFood.getTasteGroup().contains(taste)) {
								mSelectedFood.removeTaste(taste);
							} else {
								mSelectedFood.addTaste(taste);
							}
							mRefreshHandler.sendEmptyMessage(0);
						}
					});
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

		// Ʒע��EditText�Ĵ�����
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

		//Ʒעɾ��Button
		tasteView.findViewById(R.id.imgButton_deletePinZhu_askOrderAmount_dialog).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pinzhuEdtTxt.setText("");
			}
		});
		
		// �۸�EditText�Ĵ�����
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

		//Ʒע��Ǯɾ��Button
		tasteView.findViewById(R.id.imgButton_deletePrice_askOrderAmount_dialog).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				priceEdtTxt.setText("");
			}
		});
		
		return tasteView;
	}

	private ComboOrderFood matchComboFood(ComboFood comboFood){
		for(ComboOrderFood cof : mSelectedFood.getCombo()){
			if(cof.asComboFood().equals(comboFood)){
				return cof;
			}
		}
		return null;
	}
	
	private View setComboTasteView(final ComboFood comboFood){
		final View comboTasteView = LayoutInflater.from(getActivity()).inflate(R.layout.ask_order_amount_dialog_taste, null);
		//���ó��ÿ�ζGridView
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
			//ֻ��ʾǰ8�����ÿ�ζ
			while(popTastes.size() > 8){
				popTastes.remove(popTastes.size() - 1);
			}
			
			tasteGridView.setAdapter(new BaseAdapter() {
				
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					CheckBox checkBox = (CheckBox) LayoutInflater.from(getActivity()).inflate(R.layout.ask_order_amount_dialog_item, null);
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

		//����ʾƷע��Ǯ
		comboTasteView.findViewById(R.id.relativeLayout_pinzhuPrice_askOrderAmount_dialog).setVisibility(View.GONE);
		
		//Ʒע��EditText�Ĵ�����
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
					getDialog().setTitle(thisCombo.toString());
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
		
		//Ʒעɾ��Button
		comboTasteView.findViewById(R.id.imgButton_deletePinZhu_askOrderAmount_dialog).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pinzhuEdtTxt.setText("");
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
						((ComboOrderFood)mCurrentTasteView.getTag(R.id.combo_of_key)).setTasteGroup(tgParcel.asTasteGroup());
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
