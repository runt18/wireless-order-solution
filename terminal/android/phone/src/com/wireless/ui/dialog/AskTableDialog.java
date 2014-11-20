package com.wireless.ui.dialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.wireless.common.WirelessOrder;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.ui.R;

public class AskTableDialog extends DialogFragment {

	public static interface OnTableSelectedListener{
		/**
		 * Called when specific activity_table is selected.
		 * @param selectedTable the activity_table selected
		 */
		public void onTableSelected(Table selectedTable);
	}
	
	private OnTableSelectedListener mOnTableSelectedListener;
	
	public final static String TAG = "AskTableDialog";
	
	private static class ViewHandler extends Handler{
		
		private WeakReference<View> mDialogView;
		private WeakReference<AskTableDialog> mDialogFgm;
		
		ViewHandler(View dialogView, AskTableDialog dialogFgm){
			this.mDialogView = new WeakReference<View>(dialogView);
			this.mDialogFgm = new WeakReference<AskTableDialog>(dialogFgm);
		}
		
		@Override
		public void handleMessage(Message message){
			
			final List<Table> filterTbls = new ArrayList<Table>();
			
			Table matchedTbl = null;
			
			if(mDialogFgm.get().mFilterCond.length() != 0){
				for(Table tbl : WirelessOrder.tables){
					if(Integer.toString(tbl.getAliasId()).startsWith(mDialogFgm.get().mFilterCond) || tbl.getName().contains(mDialogFgm.get().mFilterCond)){
						filterTbls.add(tbl);
					}
					if(Integer.toString(tbl.getAliasId()).equals(mDialogFgm.get().mFilterCond) || tbl.getName().equalsIgnoreCase(mDialogFgm.get().mFilterCond)){
						matchedTbl = tbl;
					}
				}
			}
			GridView tblGridView = (GridView)mDialogView.get().findViewById(R.id.gridView_askTable_dialog);
			
			if(matchedTbl != null){
				if(matchedTbl.getName().length() != 0){
					mDialogFgm.get().getDialog().setTitle("选择餐台" + "(" + matchedTbl.getName() + ")");
				}else{
					mDialogFgm.get().getDialog().setTitle("选择餐台" + "(" + matchedTbl.getAliasId() + ")");
				}
			}else{
				mDialogFgm.get().getDialog().setTitle("选择餐台");
			}
			
			//只显示前6个关联餐台
			while(filterTbls.size() > 6){
				filterTbls.remove(filterTbls.size() - 1);
			}
			
			tblGridView.setAdapter(new BaseAdapter(){

				@Override
				public int getCount() {
					return filterTbls.size();
				}

				@Override
				public Object getItem(int position) {
					return filterTbls.get(position);
				}

				@Override
				public long getItemId(int position) {
					return position;
				}

				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					
					CheckBox checkBox;
					
					final Table tbl = filterTbls.get(position);
					if(convertView == null){
						View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ask_table_dialog_item, parent, false);
						checkBox = (CheckBox) view;

					}else{
						checkBox = (CheckBox)convertView;
					}
					
					//设置餐台名称
					if(tbl.getName().length() == 0){
						checkBox.setText(Integer.toString(tbl.getAliasId()));
					}else{
						checkBox.setText(tbl.getName());
					}
					
					//设置点击处理回调函数
					checkBox.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							if(mDialogFgm.get().mOnTableSelectedListener != null){
								mDialogFgm.get().mOnTableSelectedListener.onTableSelected(tbl);
							}
							mDialogFgm.get().dismiss();
						}
						
					});
					
					return checkBox;
				}
				
			});
		}
	}
	
	public static AskTableDialog newInstance(){
		AskTableDialog fgm = new AskTableDialog();
		return fgm;
	}
	
	public static AskTableDialog newInstance(int parentFgmId){
		AskTableDialog fgm = new AskTableDialog();
		Bundle bundle = new Bundle();
		bundle.putInt(PARENT_FGM_ID_KEY, parentFgmId);
		fgm.setArguments(bundle);
		return fgm;
	}
	
	private final static String PARENT_FGM_ID_KEY = "ParentFgmIdKey";
	
	private ViewHandler mViewHanlder;
	
	private String mFilterCond; 
	
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        // Verify that the host activity implements the callback interface
//        try {
//            // Instantiate the NoticeDialogListener so we can send events to the host
//        	mOnTableSelectedListener = (OnTableSelectedListener) activity;
//        } catch (ClassCastException ignored) {
//            // The activity doesn't implement the interface, throw exception
//            // throw new ClassCastException(activity.toString() + " must implement OnTableSelectedListener");
//        }
//    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getArguments() != null && getArguments().containsKey(PARENT_FGM_ID_KEY)){
			try{
				// Instantiate the mOnTableSelectedListener so we can send events to the host
				mOnTableSelectedListener = (OnTableSelectedListener)getFragmentManager().findFragmentById(getArguments().getInt(PARENT_FGM_ID_KEY));
			}catch(ClassCastException e){
				throw new ClassCastException(getFragmentManager().findFragmentById(getArguments().getInt(PARENT_FGM_ID_KEY)).toString() + " must implement OnTableSelectedListener");
			}
		}else{
	        // Verify that the host activity implements the callback interface
	        try {
	            // Instantiate the NoticeDialogListener so we can send events to the host
	        	mOnTableSelectedListener = (OnTableSelectedListener) getActivity();
	        } catch (ClassCastException ignored) {
	            // The activity doesn't implement the interface, throw exception
	            throw new ClassCastException(getActivity().toString() + " must implement OnTableSelectedListener");
	        }
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		View dialogView = getActivity().getLayoutInflater().inflate(R.layout.ask_table_dialog, (ViewGroup)getActivity().getWindow().getDecorView(), false);
		 
		//
		final EditText tblNumEditTxt = (EditText)dialogView.findViewById(R.id.edtTxt_askTable_dialog);
		tblNumEditTxt.addTextChangedListener(new TextWatcher(){

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				mFilterCond = s.toString().trim();
				mViewHanlder.sendEmptyMessage(0);
			}
			
		});
		
        // Request focus and show soft keyboard automatically
	    tblNumEditTxt.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
            	if(hasFocus){
	            	tblNumEditTxt.post(new Runnable() {
	                    @Override
	                    public void run() {
	                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	                        imm.showSoftInput(tblNumEditTxt, InputMethodManager.SHOW_IMPLICIT);
	                    }
	                });
            	}
            }
        });
	    dialogView.post(new Runnable(){
			@Override
			public void run() {
			    tblNumEditTxt.requestFocus();
			}
	    });
	    
		//餐台名称和编号切换的Button
		Button switchBtn = (Button)dialogView.findViewById(R.id.button_switch_askTable_dialog);
		switchBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				int inputType = tblNumEditTxt.getInputType();
				if((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER){
					tblNumEditTxt.setInputType(InputType.TYPE_CLASS_TEXT);
					tblNumEditTxt.setHint("请输入餐台名称");
					
				}else if((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT){
					tblNumEditTxt.setInputType(InputType.TYPE_CLASS_NUMBER);
					tblNumEditTxt.setHint("请输入餐台编号");
				}
			}
		});
		
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    builder.setTitle("餐台选择")
	    	   .setView(dialogView)
	           .setPositiveButton("确定", new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int id) {
		            	if(mOnTableSelectedListener != null){
		            		if(mOnTableSelectedListener != null){
		            			try{
				            		Table tbl = new Table();
				            		tbl.setTableAlias(Integer.parseInt(tblNumEditTxt.getText().toString()));
			            			mOnTableSelectedListener.onTableSelected(tbl);
		            			}catch(NumberFormatException e){
		    						Toast.makeText(getActivity(), "您输入的台号" + tblNumEditTxt.getText().toString() + "格式不正确，请重新输入" , Toast.LENGTH_SHORT).show();
		    					}
		            		}
		            	}
		            }
	           })
	           .setNegativeButton("取消", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		            	dismiss();
		            }
	            }); 
	    
	    
	    mViewHanlder = new ViewHandler(dialogView, this);
	    
	    return builder.create();
	}
	
}
