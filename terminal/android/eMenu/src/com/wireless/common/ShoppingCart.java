package com.wireless.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import com.wireless.exception.BusinessException;
import com.wireless.pack.Type;
import com.wireless.pack.req.PrintOption;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;

public final class ShoppingCart {
	
	public static interface OnStaffChangedListener{
		/**
		 * Called when new staff is set.
		 * @param staff
		 * @param id
		 * @param pwd
		 */
		public void onStaffChanged(Staff staff);
	}
	
	public static interface OnTableChangedListener{
		/**
		 * Called when new table is set in shopping cart.
		 * @param table the table to set
		 */
		public void onTableChanged(Table table);
	}
	
	public static interface OnCartChangedListener{
		/**
		 * Called when new foods are changed in shopping cart,
		 * such as add {@link ShoppingCart#addFood(OrderFood)}, remove {@link ShoppingCart#remove(OrderFood)} 
		 * would notify to invoke this call back if the listener is set.
		 * @param foodsInCart the foods in shopping cart
		 */
		public void onCartChanged(List<OrderFood> foodsInCart);
	}
	
	public static interface OnCommitListener{
		public void onPreCommit(Order reqOrder);
		public void onSuccess(Order reqOrder);
		public void onFail(BusinessException e);
	}
	
	public static interface OnPayListener{
		public void onPrePay(Order orderToPay);
		public void onSuccess(Order orderToPay);
		public void onFail(BusinessException e);
	}
	
	private Context mContext;
	private Staff mStaff;
	private Table mDestTable;
	//private List<OrderFood> mExtraFoods = new LinkedList<OrderFood>();
	
	private Order mNewOrder;
	
	private Order mOriOrder;
	
	private final static ShoppingCart mInstance = new ShoppingCart();

	private OnCartChangedListener mOnCartChangedListener;

	private OnTableChangedListener mOnTableChangedListener;
	
	private OnStaffChangedListener mOnStaffChangedListener;
	
	private ShoppingCart(){
		
	}
	
	public static ShoppingCart instance(){
		return mInstance;
	}


	public void setOnCartChangeListener(OnCartChangedListener l){
		mOnCartChangedListener = l;
	}
	
	public void setOnTableChangedListener(OnTableChangedListener l){
		mOnTableChangedListener = l;
	}
	
	public void setOnStaffChangedListener(OnStaffChangedListener l){
		mOnStaffChangedListener = l;
	}
	
	public void init(Context context){
		this.mStaff = null;
		this.mNewOrder = null;
		this.mDestTable = null;
		this.mOriOrder = null;
		this.mContext = context;
	}
	
	/**
	 * 
	 * @param payListener
	 * @throws BusinessException
	 */
	public void pay(final OnPayListener payListener) throws BusinessException{
		
		commit(new OnCommitListener() {
			@Override
			public void onPreCommit(Order reqOrder) {
				if(payListener != null){
					payListener.onPrePay(reqOrder);
				}
			}
			
			@Override
			public void onSuccess(Order reqOrder) {
				new com.wireless.lib.task.QueryOrderTask(WirelessOrder.loginStaff, reqOrder.getDestTbl().getAliasId(), WirelessOrder.foodMenu){
					@Override
					public void onSuccess(Order order){
						new PayOrderTask(order, payListener).execute();
					}
					@Override
					public void onFail(BusinessException e){
						if(payListener != null){
							payListener.onFail(mBusinessException);
						}
					}
				}.execute();
			}

			@Override
			public void onFail(BusinessException e) {
				if(payListener != null){
					payListener.onFail(e);
				}				
			}
		});
	}
	
	/**
	 * Commit the order.
	 * Perform to insert a new order if original order NOT exist.
	 * Otherwise perform to update the order.
	 * @param commitListener
	 * 			the commit listener
	 * @throws BusinessException
	 * 			if NOT be valid to commit order
	 */
	public void commit(OnCommitListener commitListener) throws BusinessException{
		if(mOriOrder != null){
			checkCommitValid();
			Order reqOrder = new Order(mOriOrder.getOrderFoods(), mDestTable.getAliasId(), mDestTable.getCustomNum());	
			reqOrder.setId(mOriOrder.getId());
			reqOrder.setOrderDate(mOriOrder.getOrderDate());
			//Skip the new food in case of sell out.
			for(OrderFood newFood : getNewFoods()){
				if(!newFood.asFood().isSellOut()){
					reqOrder.addFood(newFood, WirelessOrder.loginStaff);
				}
			}
			new CommitOrderTask(reqOrder, Type.UPDATE_ORDER, commitListener).execute();
			
		}else{
			checkCommitValid();
			Order reqOrder = new Order(null, mDestTable.getAliasId(), mDestTable.getCustomNum());			
			//Skip the new food in case of sell out.
			for(OrderFood newFood : getNewFoods()){
				if(!newFood.asFood().isSellOut()){
					reqOrder.addFood(newFood, WirelessOrder.loginStaff);
				}
			}
			new CommitOrderTask(reqOrder, Type.INSERT_ORDER, commitListener).execute();
		}
	}
	
	/**
	 * Commit the order with both original and extra foods.
	 * This method is used to update order.
	 * @param oriOrder
	 * 			the original order
	 * @param commitListener
	 * 			the commit listener
	 * @throws BsinessException
	 * 			throws if NOT valid to commit order
	 */
	public void commit(Order oriOrder, OnCommitListener commitListener) throws BusinessException{
		if(oriOrder != null){
			mOriOrder = oriOrder;
			commit(commitListener);			
		}else{
			throw new IllegalArgumentException("The original order can NOT be null.");
		}
	}
	/**
	 * Check to see whether the commit parameters are valid.
	 * @throws BusinessException
	 * 				Throws in one of cases below.<br>
	 * 				1 - table NOT be set<br>
	 * 				2 - staff NOT be set<br>
	 * 				3 - order NOT be set<br>
	 */
	public void checkCommitValid() throws BusinessException{
		if(mDestTable == null){
			throw new BusinessException("您还未设置餐台，暂时不能提交");
		}else if(mStaff == null){
			throw new BusinessException("您还未设置服务员，暂时不能提交");
		}else if(!hasOrder()){
			throw new BusinessException("您还未点菜，暂时不能提交");
		}
	}
	
	/**
	 * Remove all the extra foods to this shopping cart.
	 */
	public void removeAll(){
		if(mNewOrder != null){
			mNewOrder.setOrderFoods(null);
			notifyFoodsChanged();
		}
	}
	
	/**
	 * Remove the specific order food from new order list.
	 * @param foodToDel 
	 * 			the food to remove
	 * @param staff
	 * 			the staff to remove food
	 * @return true if the food to be removed exist before, otherwise return false
	 * @throws BusinessException 
	 * 			throws if the staff does NOT own the cancel food privilege
	 */
	public boolean remove(OrderFood foodToDel, Staff staff) throws BusinessException{
		if(mNewOrder != null){
			if(mNewOrder.remove(foodToDel, staff)){
				notifyFoodsChanged();
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	/**
	 * Remove the specific order food from new order list without privilege check.
	 * @param foodToDel 
	 * 			the food to remove
	 * @param staff
	 * 			the staff to remove food
	 * @return true if the food to be removed exist before, otherwise return false
	 */
	public boolean delete(OrderFood foodToDel){
		if(mNewOrder != null){
			if(mNewOrder.delete(foodToDel)){
				notifyFoodsChanged();
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	/**
	 * 把多个新点菜添加到购物篮中。 遍历查找已点的新菜品中是否相同的菜品， 如果有就将他们的点菜数量相加， 否则就直接添加到新菜品List中。
	 * 
	 * @param extraFoods
	 *            要添加的多个菜品
	 * @throws BusinessException
	 * 				Throws if the order amount of the added food exceed MAX_ORDER_AMOUNT
	 */
	public void addAll(List<OrderFood> extraFoods) throws BusinessException{
		if(mNewOrder == null){
			mNewOrder = new Order();
		}
		for(OrderFood extraFood : extraFoods){
			mNewOrder.addFood(extraFood, WirelessOrder.loginStaff);
		}
		notifyFoodsChanged();
	}
	
	/**
	 * 把新点菜添加到购物篮中。 遍历查找已点的新菜品中是否相同的菜品， 如果有就将他们的点菜数量相加， 否则就直接添加到新菜品List中。
	 * 
	 * @param fooodToAdd
	 *            要添加的菜品
	 * @throws BusinessException
	 * 				throws if the order amount of the added food exceed MAX_ORDER_AMOUNT
	 */
	public void addFood(OrderFood foodToAdd) throws BusinessException{
		if(mNewOrder == null){
			mNewOrder = new Order();
		}
		
		mNewOrder.addFood(new OrderFood(foodToAdd), WirelessOrder.loginStaff);
		notifyFoodsChanged();
	}

	/**
	 * 替换原来购物篮中的菜品。
	 * @param foodToReplace
	 * @return true if the food to be replaced exist as before, otherwise return false
	 */
	public boolean replaceFood(OrderFood foodToReplace){
		boolean result = mNewOrder.replace(foodToReplace);
		notifyFoodsChanged();
		return result;
	}
	
	/**
	 * Remove specific amount from original food list. 
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodToDel
	 * 			the food to remove
	 * @param removeAmount
	 * 			the amount to remove
	 * @return true if the food to remove exist, otherwise false
	 * @throws BusinessException
	 * 			throws if the staff has no privilege to remove food
	 */
	public boolean removeCount(Staff staff, OrderFood foodToDel, float removeAmount) throws BusinessException{
		if(mOriOrder != null){
			for(OrderFood of : mOriOrder.getOrderFoods()){
				if(of.equals(foodToDel)){
					of.removeCount(removeAmount, staff);
					notifyFoodsChanged();
					return true;
				}
			}
			return false;
		}else{
			return false;
		}
	}
	
	/**
	 * Get the staff. 
	 * @return the mStaff
	 */
	public Staff getStaff() {
		return mStaff;
	}
	
	/**
	 * Check to see whether the staff has been set.
	 * @return true if the staff has been set, otherwise false
	 */
	public boolean hasStaff(){
		return mStaff == null ? false : true;
	}

	/**
	 * Set the staff.
	 * @param mStaff
	 *            the staff to set
	 */
	public void setStaff(Staff staff) {
		this.mStaff = staff;
		if(mOnStaffChangedListener != null){
			mOnStaffChangedListener.onStaffChanged(mStaff);
		}
	}

	/**
	 * @return the mTable
	 */
	public Table getDestTable() {
		return mDestTable;
	}

	/**
	 * Refresh the shopping cart.
	 * @see {@link #setDestTable(Table)}
	 */
	public void refresh(){
		new com.wireless.lib.task.QuerySellOutTask(WirelessOrder.loginStaff, WirelessOrder.foodMenu.foods){
			@Override
			public void onSuccess(List<Food> sellOutFoods){
				//Check to see whether any new food is sold out.
				for(OrderFood newFood : getNewFoods()){
					for(Food f : sellOutFoods){
						if(f.equals(newFood.asFood())){
							newFood.asFood().setSellOut(true);
							break;
						}
					}
				}
				//Set the table to refresh the original order.
				setDestTable(mDestTable);
			}
			@Override
			public void onFail(BusinessException e){
				
			}
		}.execute();
		
	}
	
	/**
	 * Set the table. 
	 * The call back {@link OnTableChangedListener} and {@link OnCartChangedListener} would be invoked to notify the change.
	 * @param tabel
	 *            the table to set
	 */
	public void setDestTable(Table table) {
		mDestTable = table;
		if(table != null){
			new com.wireless.lib.task.QueryOrderTask(WirelessOrder.loginStaff, table.getAliasId(), WirelessOrder.foodMenu){
				@Override
				public void onSuccess(Order order){
					setOriOrder(order);
					if(mOnTableChangedListener != null){
						mOnTableChangedListener.onTableChanged(mDestTable);
					}
				}
				
				@Override
				public void onFail(BusinessException e){
					setOriOrder(null);
					if(mOnTableChangedListener != null){
						mOnTableChangedListener.onTableChanged(mDestTable);
					}
				}
				
			}.execute();
		}else{
			setOriOrder(null);
			if(mOnTableChangedListener != null){
				mOnTableChangedListener.onTableChanged(mDestTable);
			}
		}
	}

	/**
	 * Return the original foods in shopping cart.
	 * @return the original foods
	 */
	public List<OrderFood> getOriFoods(){
		if(mOriOrder != null){
			return Collections.unmodifiableList(mOriOrder.getOrderFoods());
		}else{
			return Collections.emptyList();
		}		
	}
	
	/**
	 * Return the amount to original foods. 
	 * @return the amount to original foods
	 */
	public int getOriAmount(){
		if(mOriOrder != null){
			return mOriOrder.getOrderFoods().size();
		}else{
			return 0;
		}	
	}
	
	/**
	 * Return the new foods to this shopping cart.
	 * @return the new foods
	 */
	public List<OrderFood> getNewFoods(){
		if(mNewOrder != null){
			return Collections.unmodifiableList(mNewOrder.getOrderFoods());
		}else{
			return Collections.emptyList();
		}
	}
	
	/**
	 * Return the amount to new foods.
	 * @return the amount to new foods
	 */
	public int getNewAmount(){
		if(mNewOrder != null){
			return mNewOrder.getOrderFoods().size();
		}else{
			return 0;
		}
	}
	
	/**
	 * Return all foods to shopping cart.
	 * @return all foods to shopping cart
	 */
	public List<OrderFood> getAllFoods(){
		List<OrderFood> foodsInCart = new ArrayList<OrderFood>();
		if(mNewOrder != null){
			foodsInCart.addAll(mNewOrder.getOrderFoods());
		}
		if(mOriOrder != null){
			foodsInCart.addAll(mOriOrder.getOrderFoods());
		}
		return Collections.unmodifiableList((foodsInCart));
	}

	/**
	 * Return the amount to all foods.
	 * @return the amount to all foods
	 */
	public int getAllAmount(){
		return getAllFoods().size();
	}
	
	private void setOriOrder(Order mOriOrder) {
		this.mOriOrder = mOriOrder;
		notifyFoodsChanged();
	}
	
	public boolean hasTable(){
		return mDestTable != null;
	}
	
	public boolean hasOriOrder(){
		if(mOriOrder != null){
			return mOriOrder.hasOrderFood();
		}else{
			return false;
		}
	}
	
	public boolean hasNewOrder(){
		if(mNewOrder != null){
			return mNewOrder.hasOrderFood();
		}else{
			return false;
		}
	}
	
	public boolean hasOrder(){
		return hasOriOrder() || hasNewOrder();
	}	


	private void notifyFoodsChanged(){
		if(mOnCartChangedListener != null){
			mOnCartChangedListener.onCartChanged(getAllFoods());
		}
	}
	
	/**
	 * Search the original order food in shopping cart according to food alias id.
	 * @param aliasId the food alias to search
	 * @return the original order food in shopping cart matched the food alias, return null if not found. 
	 */
	public OrderFood searchOriFoodByAlias(int aliasId){
		if(mOriOrder != null){
			for(OrderFood of : mOriOrder.getOrderFoods()){
				if(of.getAliasId() == aliasId){
					return new OrderFood(of);
				}
			}
		}
		return null;
	}
	
	/**
	 * Search the new order food in shopping cart according to food alias id.
	 * @param aliasId the food alias to search
	 * @return the new order food in shopping cart matched the food alias, return null if not found. 
	 */
	public OrderFood searchNewFoodByAlias(int aliasId){
		if(mNewOrder != null){
			for(OrderFood of : mNewOrder.getOrderFoods()){
				if(of.getAliasId() == aliasId){
					return new OrderFood(of);
				}
			}
		}
		return null;
	}
	
	/**
	 * Search the order food in shopping cart according to food alias id.
	 * @param aliasId the food alias to search
	 * @return the order food in shopping cart matched the food alias, return null if not found. 
	 */
	public OrderFood searchFoodByAlias(int aliasId){
		for(OrderFood of : getAllFoods()){
			if(of.getAliasId() == aliasId){
				return new OrderFood(of);
			}
		}
		return null;
	}
	
	/**
	 * 执行账单的提交请求
	 */
	private class CommitOrderTask extends com.wireless.lib.task.CommitOrderTask{		
		
		private OnCommitListener mCommitListener;
		
		CommitOrderTask(Order reqOrder, byte type, OnCommitListener commitListener){
			super(WirelessOrder.loginStaff, reqOrder, type);
			mCommitListener = commitListener;
		}
		
		@Override
		protected void onPreExecute(){
			if(mCommitListener != null){
				mCommitListener.onPreCommit(mReqOrder);
			}
		}		
		
		@Override
		protected void onPostExecute(Void arg){

			if(mBusinessException == null){
				//如果是锁定餐台状态则重新设置锁定的餐台，否则清除餐台数据
				SharedPreferences pref = mContext.getSharedPreferences(Params.PREFS_NAME, Context.MODE_PRIVATE);
				if(pref.getBoolean(Params.TABLE_FIXED, false)){
					mNewOrder = null;
					setDestTable(mDestTable);
				}else{
					initTable();
				}
				//如果不是锁定服务员状态则清除服务员数据
				if(!pref.getBoolean(Params.STAFF_FIXED, false)){
					setStaff(null);
				}
				if(mCommitListener != null){	
					mCommitListener.onSuccess(mReqOrder);
				}
			}else{
				if(mCommitListener != null){	
					mCommitListener.onFail(mBusinessException);
				}
			}
		}		

	}
	
	private class PayOrderTask extends com.wireless.lib.task.PayOrderTask{

		private OnPayListener mPayListener;
		
		public PayOrderTask(Order orderToPay, OnPayListener payListener) {
			super(WirelessOrder.loginStaff, orderToPay, Type.PAY_TEMP_ORDER, PrintOption.DO_PRINT);
			mPayListener = payListener;
		}
		
		@Override
		protected void onPostExecute(Void arg){
			if(mPayListener != null){
				if(mBusinessException != null){
					mPayListener.onFail(mBusinessException);
				}else{
					mPayListener.onSuccess(mOrderToPay);
				}
			}
		}	
	}
	
	private void initTable(){
		this.mNewOrder = null;
		this.mDestTable = null;
		this.mOriOrder = null;
		if(mOnTableChangedListener != null){
			mOnTableChangedListener.onTableChanged(null);
		}
	}
	
	/**
	 * Return the total amount to foods in shopping cart.
	 * @return the total amount to foods in shopping cart
	 */
	public float getTotalCount(){
		float count = 0f;
		if(mNewOrder != null){
			for(OrderFood f: mNewOrder.getOrderFoods()){
				count += f.getCount();
			}
		}
		if(mOriOrder != null){
			for(OrderFood f: mOriOrder.getOrderFoods()){
				count += f.getCount();
			}
		}
		return count;
	}
	
	/**
	 * Return the total price to foods in shopping cart.
	 * @return the total price to foods in shopping cart
	 */
	public float getTotalPrice(){
		float price = 0f;
		if(mNewOrder != null){
			price += mNewOrder.calcTotalPrice();
		}
		if(mOriOrder != null){
			price += mOriOrder.calcTotalPrice();
		}
		return price;
	}
}
