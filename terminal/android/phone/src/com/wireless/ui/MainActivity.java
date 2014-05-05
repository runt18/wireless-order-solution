package com.wireless.ui;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.wireless.common.Params;
import com.wireless.common.WirelessOrder;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.ui.dialog.AskTableDialog;
import com.wireless.ui.dialog.AskTableDialog.OnTableSelectedListener;

public class MainActivity extends FragmentActivity implements OnTableSelectedListener{

	public static final int NETWORK_SET = 6;
	
	private int mDialogType;
	private static final int DIALOG_INSERT_ORDER = 0;
	private static final int DIALOG_BILL_ORDER = 3;
	private static final int DIALOG_STAFF_LOGIN = 4;
	
	private static final int REDRAW_FOOD_MENU = 1;
	private static final int REDRAW_RESTAURANT = 2;
	private static final int REDRAW_STAFF_LOGIN = 3;
	
	private Staff mStaffLogin;

	/**
	 * ������׺Ͳ�����Ϣ�󣬸��µ���صĽ���ؼ�
	 */
	private static class RefreshHandler extends Handler{
		
		private WeakReference<MainActivity> mActivity;
		
		RefreshHandler(MainActivity theActivity){
			this.mActivity = new WeakReference<MainActivity>(theActivity);
		}
		
		@Override
		public void handleMessage(Message message){
			if(message.what == REDRAW_FOOD_MENU){

				if(WirelessOrder.foodMenu == null){
					((TextView)mActivity.get().findViewById(R.id.txtView_staffName_main)).setText("");
					((TextView)mActivity.get().findViewById(R.id.marqueeTxt_billBoard_main)).setText("");
				}
				
			}else if(message.what == REDRAW_STAFF_LOGIN){
				if(WirelessOrder.staffs == null){
					((TextView)mActivity.get().findViewById(R.id.txtView_staffName_main)).setText("");
					((TextView)mActivity.get().findViewById(R.id.marqueeTxt_billBoard_main)).setText("");
				}
				
			}else if(message.what == REDRAW_RESTAURANT){
				if(WirelessOrder.restaurant != null){
					TextView billBoard = (TextView)mActivity.get().findViewById(R.id.marqueeTxt_billBoard_main);
					billBoard.setText(WirelessOrder.restaurant.getInfo().replaceAll("\n", ""));
					
					TextView userName = (TextView)mActivity.get().findViewById(R.id.txtView_staffName_main);
					if(mActivity.get().mStaffLogin != null){
						if(mActivity.get().mStaffLogin.getName().length() != 0){
							userName.setText(WirelessOrder.restaurant.getName() + "(" + mActivity.get().mStaffLogin.getName() + ")");							
						}else{
							userName.setText(WirelessOrder.restaurant.getName());							
						}
					}else{
						userName.setText(WirelessOrder.restaurant.getName());
					}
				}				
			}
		}
		
	}
	
	/**
	 * ������׺Ͳ�����Ϣ�󣬸��µ���صĽ���ؼ�
	 */
	private Handler _handler = new RefreshHandler(this);
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_activity);
		
		int[] imageIcons = { 
							 R.drawable.btnup01, R.drawable.btnup10, R.drawable.btnup02, 
							 R.drawable.btnup04, R.drawable.btnup03, R.drawable.btnup06, 
							 R.drawable.btnup07, R.drawable.btnup08, R.drawable.btnup09 
						   };

		String[] iconDesc = { 
							 "���", "���ٵ��", "�鿴", 
							 "����", "���ٹ���", "����", 
							 "���׸���", "ע��", "��Ա" 
							};

		// ���ɶ�̬���飬����ת������
		final String imgGridItem = "image_main_grid_view_item";
		final String txtGridItem = "text_main_grid_view_item";;
		List<Map<String, Object>> imgItems = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < imageIcons.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(imgGridItem, imageIcons[i]);// ���ͼ����Դ��ID
			map.put(txtGridItem, iconDesc[i]);// �������ItemText
			imgItems.add(map);
		}

		GridView funGridView = (GridView)findViewById(R.id.gridView_9_item_main);
		/**
		 * ������������ImageItem <====> ��̬�����Ԫ�أ�����һһ��Ӧ����Ӳ�����ʾ�Ź���
		 */
		funGridView.setAdapter(new SimpleAdapter(MainActivity.this, 		// context
												  imgItems, 			  	// ������Դ
												  R.layout.main_grid_view_item, 				// night_item��XMLʵ��
												  new String[] { imgGridItem, txtGridItem }, 	// ��̬������ImageItem��Ӧ������
												  new int[] { R.id.imgView_main_gridView_item, R.id.txtView_main_gridView_item }));// ImageItem��XML�ļ������һ��ImageView,һ��TextView ID

		funGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0,// The AdapterView where the click happened
									View arg1, 			// The view within the AdapterView that was clicked
									int position, 		// The position of the view in the adapter
									long arg3 			// The row id of the item that was clicked
			) {
				
				/**
				 * "����", "ע��", "����" ���κ�������ǿ���ʹ�õ�
				 */
				if(position != 5 && position != 7 && position != 8){
					if(WirelessOrder.staffs.isEmpty()){
						Toast.makeText(MainActivity.this, "û�в�ѯ���κε�Ա����Ϣ�����ڹ����̨�����Ա����Ϣ", Toast.LENGTH_SHORT).show();
						return;
					}else if(WirelessOrder.foodMenu == null){
						Toast.makeText(MainActivity.this, "û�в�ѯ��������Ϣ��������ִ�в��׸��²���", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				switch (position) {
				case 0:
					//�µ�
					//showDialog(DIALOG_INSERT_ORDER);
					mDialogType = DIALOG_INSERT_ORDER;
					AskTableDialog.newInstance().show(getSupportFragmentManager(), AskTableDialog.TAG);
					break;

					//���
				case 1:
					Intent intent = new Intent(MainActivity.this, QuickPickActivity.class);
					startActivity(intent);

					break;
					
				case 2:
					//�鿴
					intent = new Intent(MainActivity.this, TableActivity.class);
					startActivity(intent);

					break;
					
				case 3:
					//����
					//showDialog(DIALOG_BILL_ORDER);
					mDialogType = DIALOG_BILL_ORDER;
					AskTableDialog.newInstance().show(getSupportFragmentManager(), AskTableDialog.TAG);
					break;

				case 4:
					//����
					Intent sellOutIntent = new Intent(MainActivity.this,SellOutActivity.class);
					startActivity(sellOutIntent);
					break;

				case 5:
					//����
					Intent netIntent = new Intent(MainActivity.this, SettingActivity.class);
					startActivityForResult(netIntent, NETWORK_SET);
					break;

				case 6:
					//���׸���
					new QueryMenuTask().execute();		
					break;

				case 7:
					//ע��
					new QueryStaffTask(false).execute();
					break;
					
				case 8:
					//����
					intent = new Intent(MainActivity.this, MemberListActivity.class);
					startActivity(intent);
					break;
				}
			}

		});

		//ȡ�ò���ʾ����汾��
		TextView topTitle = (TextView)findViewById(R.id.toptitle);
		try{
			topTitle.setText("e��ͨ(v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + ")");
		}catch(NameNotFoundException e) {
			topTitle.setText("e��ͨ");
		}
		 
		if(!WirelessOrder.staffs.isEmpty()){
			SharedPreferences sharedPreferences = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
			long loginStaffId = sharedPreferences.getLong(Params.STAFF_LOGIN_ID, Params.DEF_STAFF_LOGIN_ID);
			if(loginStaffId == Params.DEF_STAFF_LOGIN_ID){
				//Show the login dialog if logout before. 
				showDialog(DIALOG_STAFF_LOGIN);
			}else{
				//Directly login with the previous staff account if user does NOT logout before.
				//Otherwise show the login dialog. 
				mStaffLogin = null;
				for(Staff staff : WirelessOrder.staffs){
					if(staff.getId() == loginStaffId){
						mStaffLogin = staff;
					}
				}
				if(mStaffLogin != null){
					WirelessOrder.loginStaff = mStaffLogin;
				}else{
					showDialog(DIALOG_STAFF_LOGIN);
				}
			}
			
			_handler.sendEmptyMessage(REDRAW_RESTAURANT);
		}		  
	}
	
	@Override
	protected Dialog onCreateDialog(int dialogID){
		if(dialogID == DIALOG_STAFF_LOGIN){
			return new AskLoginDialog();
		}else{
			return null;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setTitle("��ʾ")
					.setMessage("��ȷ���˳�e��ͨ?")
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

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onTableSelected(Table selectedTable) {
		if(mDialogType == DIALOG_INSERT_ORDER){
			//Jump to order activity
			Intent intent = new Intent(MainActivity.this, OrderActivity.class);
			intent.putExtra(OrderActivity.KEY_TABLE_ID, String.valueOf(selectedTable.getAliasId()));
			startActivity(intent);
		}else if(mDialogType == DIALOG_BILL_ORDER){
			//Jump to bill activity
			Intent intent = new Intent(MainActivity.this, BillActivity.class);
			intent.putExtra(BillActivity.KEY_TABLE_ID, String.valueOf(selectedTable.getAliasId()));
			startActivity(intent);
		}
	}

	/**
	 * �ж�����һ��Activity���ص�
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == NETWORK_SET ){
		     if(resultCode == RESULT_OK){
		    	 //��������Ա����Ϣ�����²���
		    	 new QueryStaffTask(true).execute();
		     }
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ���������Ϣ
	 */
	private class QueryMenuTask extends com.wireless.lib.task.QueryMenuTask{

		private ProgressDialog _progDialog;

		QueryMenuTask() {
			super(WirelessOrder.loginStaff);
		}
		
		/**
		 * ִ�в����������ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "���ڸ��²�����Ϣ...���Ժ�", true);
		}
		
		@Override
		protected void onSuccess(FoodMenu foodMenu){
			//make the progress dialog disappeared
			_progDialog.dismiss();					
			//notify the main activity to redraw the food menu
			_handler.sendEmptyMessage(REDRAW_FOOD_MENU);
			
			WirelessOrder.foodMenu = foodMenu;
			new QueryRestaurantTask().execute();
		}
		
		@Override 
		protected void onFail(BusinessException e){
			//make the progress dialog disappeared
			_progDialog.dismiss();					
			//notify the main activity to redraw the food menu
			_handler.sendEmptyMessage(REDRAW_FOOD_MENU);
			
			new AlertDialog.Builder(MainActivity.this)
					.setTitle("��ʾ")
					.setMessage(e.getMessage())
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).show();
		}
		
	}
	
	/**
	 * �����ѯ������Ϣ
	 */
	private class QueryRestaurantTask extends com.wireless.lib.task.QueryRestaurantTask{
		
		private ProgressDialog _progDialog;
		
		QueryRestaurantTask(){
			super(WirelessOrder.loginStaff);
		}
		/**
		 * ��ִ���������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "���²�����Ϣ...���Ժ�", true);
		}
	
		@Override
		protected void onSuccess(Restaurant restaurant){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			WirelessOrder.restaurant = restaurant;
			//notify the main activity to update the food menu
			_handler.sendEmptyMessage(REDRAW_RESTAURANT);

			new QueryRegionTask().execute();
		}
		
		@Override
		protected void onFail(BusinessException e){
			//make the progress dialog disappeared
			_progDialog.dismiss();
			
			new AlertDialog.Builder(MainActivity.this)
			.setTitle("��ʾ")
			.setMessage(e.getMessage())
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			}).show();
		}
	
	}
	
	/**
	 * �����ѯ������Ϣ
	 */
	private class QueryRegionTask extends com.wireless.lib.task.QueryRegionTask{
		
		private ProgressDialog _progDialog;
		
		QueryRegionTask(){
			super(WirelessOrder.loginStaff);
		}
		/**
		 * ��ִ���������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "����������Ϣ...���Ժ�", true);
		}
	
		@Override
		protected void onSuccess(List<Region> regions){
			_progDialog.dismiss();
			WirelessOrder.regions.clear();
			WirelessOrder.regions.addAll(regions);
			
			new QueryTableTask().execute();
		}
		
		@Override
		protected void onFail(BusinessException e){
			_progDialog.dismiss();
			
			new AlertDialog.Builder(MainActivity.this)
			.setTitle("��ʾ")
			.setMessage(e.getMessage())
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			}).show();
		}
	}
	
	/**
	 * �����̨��Ϣ
	 */
	private class QueryTableTask extends com.wireless.lib.task.QueryTableTask{
		
		private ProgressDialog _progDialog;
		
		/**
		 * ��ִ������������Ϣǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "���²�̨��Ϣ...���Ժ�", true);
		}
		
		QueryTableTask(){
			super(WirelessOrder.loginStaff);
		}
		
		@Override
		protected void onSuccess(List<Table> tables){
			_progDialog.dismiss();
			WirelessOrder.tables.clear();
			WirelessOrder.tables.addAll(tables);
			Toast.makeText(MainActivity.this, "������Ϣ���³ɹ�", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		protected void onFail(BusinessException e){
			_progDialog.dismiss();
			new AlertDialog.Builder(MainActivity.this)
			.setTitle("��ʾ")
			.setMessage(e.getMessage())
			.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			}).show();
		}
	}
	
	/**
	 * �����ѯԱ����Ϣ 
	 */
	private class QueryStaffTask extends com.wireless.lib.task.QueryStaffTask{
		
		private ProgressDialog _progDialog;
		
		private final boolean _isMenuUpdate;
		
		QueryStaffTask(boolean isMenuUpdate){
			super(MainActivity.this);
			_isMenuUpdate = isMenuUpdate;
		}
		
		/**
		 * ִ��Ա����Ϣ����ǰ��ʾ��ʾ��Ϣ
		 */
		@Override
		protected void onPreExecute(){
			_progDialog = ProgressDialog.show(MainActivity.this, "", "���ڸ���Ա����Ϣ...���Ժ�", true);
		}
		
		@Override
		protected void onSuccess(List<Staff> staffs){
			_progDialog.dismiss();
			
			_handler.sendEmptyMessage(REDRAW_STAFF_LOGIN);
			WirelessOrder.staffs.clear();
			WirelessOrder.staffs.addAll(staffs);
			
			if(WirelessOrder.staffs.isEmpty()){
				new AlertDialog.Builder(MainActivity.this)
							   .setTitle("��ʾ")
				               .setMessage("û�в�ѯ���κε�Ա����Ϣ�����ڹ����̨�����Ա����Ϣ")
				               .setPositiveButton("ȷ��", null)
				               .show();
				
			}else{
				Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//��ȡ�༭��
				editor.putLong(Params.STAFF_LOGIN_ID, Params.DEF_STAFF_LOGIN_ID);
				editor.commit();
				showDialog(DIALOG_STAFF_LOGIN);
				if(_isMenuUpdate){
					new QueryMenuTask().execute();
				}
			}
		}
		
		@Override
		protected void onFail(BusinessException e){
			_progDialog.dismiss();		

			new AlertDialog.Builder(MainActivity.this)
			.setTitle("��ʾ")
			.setMessage(e.getMessage())
			.setPositiveButton("ȷ��", null)
			.show();
		}
		
	}
	
	//��¼��Dialog
	public class AskLoginDialog extends Dialog{

		private PopupWindow _popupWindow;
		private BaseAdapter _staffAdapter;
		
		AskLoginDialog() {
			super(MainActivity.this, R.style.FullHeightDialog);
			setContentView(R.layout.login_dialog);
			getWindow().getAttributes().width = (int) (getWindow().getWindowManager().getDefaultDisplay().getWidth()* 0.85);
			getWindow().getAttributes().height = (int) (getWindow().getWindowManager().getDefaultDisplay().getHeight() * 0.5);
			getWindow().setBackgroundDrawableResource(android.R.color.transparent);//���ñ���͸��
			final EditText pwdEdtTxt = (EditText)findViewById(R.id.pwd);
	        
	        final TextView staffTxtView = (TextView)findViewById(R.id.staffname);
	        
	        /**
	         * �ʺ��������ʾԱ���б�
	         */
	        staffTxtView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(_popupWindow.isShowing()){
						_popupWindow.dismiss();
					}else{
						_popupWindow.showAsDropDown(findViewById(R.id.click), -330, 15);
					}					
				}
			});
	        
	        /**
	         * ������ͷ��ʾԱ����Ϣ�б�
	         */
	        ((ImageView)findViewById(R.id.click)).setOnClickListener(new View.OnClickListener() {				
				@Override
				public void onClick(View v) {
					if(_popupWindow.isShowing()){
						_popupWindow.dismiss();
					}else{
						_popupWindow.showAsDropDown(v, -330, 15);
					}					
				}
			});
	        
	        // ��ȡ�Զ��岼���ļ�����ͼ
			View popupWndView = getLayoutInflater().inflate(R.layout.login_popup_wnd, null, false);
			// ����PopupWindowʵ��
			_popupWindow = new PopupWindow(popupWndView, 380, 200, true);
			_popupWindow.setOutsideTouchable(true);
			_popupWindow.setBackgroundDrawable(new BitmapDrawable());
			
			ListView staffLstView = (ListView)popupWndView.findViewById(R.id.loginpopuwindow);
			_staffAdapter = new StaffsAdapter();
			staffLstView.setAdapter(_staffAdapter);

			/**
			 * �������б����ѡ��Ա����Ϣ�Ĳ���
			 */
			staffLstView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					mStaffLogin = WirelessOrder.staffs.get(position);
					staffTxtView.setText(mStaffLogin.getName());
				   _popupWindow.dismiss();
				}
			});
			
			/**
			 * ��¼Button�ĵ������
			 */
			((Button)findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
				
					TextView errTxtView = (TextView)findViewById(R.id.error);
					
					try {
						//Convert the password into MD5
						MessageDigest digester = MessageDigest.getInstance("MD5");
						digester.update(pwdEdtTxt.getText().toString().getBytes(), 0, pwdEdtTxt.getText().toString().getBytes().length); 
					
						if(staffTxtView.getText().toString().equals("")){
							errTxtView.setText("�˺Ų���Ϊ��");
							
						}else if(mStaffLogin.getPwd().equals(toHexString(digester.digest()))){
							//����staff pin���ļ�����
							Editor editor = getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE).edit();//��ȡ�༭��
							editor.putLong(Params.STAFF_LOGIN_ID, mStaffLogin.getId());
							//�ύ�޸�
							editor.commit();	
							_handler.sendEmptyMessage(REDRAW_RESTAURANT);
							//set the pin generator according to the staff login
							WirelessOrder.loginStaff = mStaffLogin;
							dismiss();
							
						}else{		
							errTxtView.setText("�������");
						}
						
					}catch(NoSuchAlgorithmException e) {
						errTxtView.setText(e.getMessage());;
					}
				}
			});
			
		}		
		
		@Override
		public void onAttachedToWindow(){
			((TextView)findViewById(R.id.error)).setText("");
			((EditText)findViewById(R.id.pwd)).setText("");
	        ((TextView)findViewById(R.id.staffname)).setText("");
	        if(_staffAdapter != null){
	        	_staffAdapter.notifyDataSetChanged();
	        }
		}
		
		/**
		 * Convert the md5 byte to hex string.
		 * @param md5Msg the md5 byte value
		 * @return the hex string to this md5 byte value
		 */
		private String toHexString(byte[] md5Msg){
			StringBuffer hexString = new StringBuffer();
			for (int i=0; i < md5Msg.length; i++) {
				if(md5Msg[i] >= 0x00 && md5Msg[i] < 0x10){
					hexString.append("0").append(Integer.toHexString(0xFF & md5Msg[i]));
				}else{
					hexString.append(Integer.toHexString(0xFF & md5Msg[i]));					
				}
			}
			return hexString.toString();
		}
		
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK){
				finish();
			}
			return super.onKeyDown(keyCode, event);
		}
		
		/**
		 * Ա����Ϣ�������Adapter 
		 */
		private class StaffsAdapter extends BaseAdapter{
			
			public StaffsAdapter(){

			}
			
			@Override
			public int getCount() {			
				return WirelessOrder.staffs.size();
			}

			@Override
			public Object getItem(int position) {
				return null;
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if(convertView == null){
					convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.orderpopuwindowitem, null);
					((TextView)convertView.findViewById(R.id.popuwindowfoodname)).setText(WirelessOrder.staffs.get(position).getName());
				}else{
					((TextView)convertView.findViewById(R.id.popuwindowfoodname)).setText(WirelessOrder.staffs.get(position).getName());
				}				
				return convertView;
			}			
			
		}
	}
	
}