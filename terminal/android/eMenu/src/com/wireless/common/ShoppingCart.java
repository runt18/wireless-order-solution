package com.wireless.common;

import java.util.ArrayList;
import java.util.List;

import com.wireless.exception.BusinessException;
import com.wireless.pack.Type;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.StaffTerminal;

public final class ShoppingCart {
	
	public static interface OnFoodsChangedListener{
		/**
		 * Called when new foods are changed in shopping cart,
		 * such as add {@link ShoppingCart#addFood(OrderFood)}, remove {@link ShoppingCart#remove(OrderFood)} 
		 * would notify to invoke this call back if the listener is set.
		 * @param newFoods the current new foods
		 */
		public void onFoodsChanged(List<OrderFood> newFoods);
	}
	
	public static interface OnTableChangedListener{
		/**
		 * Called when new table is set in shopping cart.
		 * @param table the table to set
		 */
		public void onTableChanged(Table table);
	}
	
	public static interface OnCommitListener{
		public void OnPreCommit(Order reqOrder);
		public void onPostCommit(Order reqOrder, BusinessException e);
	}
	
	private StaffTerminal mStaff;
	private Table mDestTable;
	//private List<OrderFood> mExtraFoods = new LinkedList<OrderFood>();
	
	private Order mNewOrder;
	
	private Order mOriOrder;
	
	private List<OrderFood> mFoodsInCart = new ArrayList<OrderFood>();
	
	private final static ShoppingCart mInstance = new ShoppingCart();
	
	private ShoppingCart(){
		
	}
	
	public static ShoppingCart instance(){
		return mInstance;
	}

	private OnFoodsChangedListener mOnFoodsChangedListener;

	private OnTableChangedListener mOnTableChangeListener;
	
	public void setOnFoodsChangeListener(OnFoodsChangedListener l){
		mOnFoodsChangedListener = l;
	}
	
	public void setOnTableChangeListener(OnTableChangedListener l){
		mOnTableChangeListener = l;
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
			if(hasNewOrder()){
				reqOrder.addFoods(mNewOrder.getOrderFoods());
			}
			new CommitOrderTask(reqOrder, Type.UPDATE_ORDER, commitListener).execute();
			
		}else{
			checkCommitValid();
			Order reqOrder = new Order(mNewOrder.getOrderFoods(), mDestTable.getAliasId(), mDestTable.getCustomNum());			
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
	 * @return true if the food to be removed exist before, otherwise return false
	 */
	public boolean remove(OrderFood foodToDel){
		if(mNewOrder != null){
			if(mNewOrder.remove(foodToDel)){
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
			mNewOrder.addFood(extraFood);
		}
		notifyFoodsChanged();
	}
	
	/**
	 * 把新点菜添加到购物篮中。 遍历查找已点的新菜品中是否相同的菜品， 如果有就将他们的点菜数量相加， 否则就直接添加到新菜品List中。
	 * 
	 * @param fooodToAdd
	 *            要添加的菜品
	 * @throws BusinessException
	 * 				Throws if the order amount of the added food exceed MAX_ORDER_AMOUNT
	 */
	public void addFood(OrderFood foodToAdd) throws BusinessException{
		if(mNewOrder == null){
			mNewOrder = new Order();
		}
		
		if(!foodToAdd.hasTaste()){
			foodToAdd.makeTasteGroup();
		}
		
		mNewOrder.addFood(new OrderFood(foodToAdd));
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
	 * Get the staff. 
	 * @return the mStaff
	 */
	public StaffTerminal getStaff() {
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
	 *            the mStaff to set
	 */
	public void setStaff(StaffTerminal staff) {
		this.mStaff = staff;
	}

	/**
	 * @return the mTable
	 */
	public Table getDestTable() {
		return mDestTable;
	}

	/**
	 * @param mDestTable
	 *            the mTable to set
	 */
	public void setDestTable(Table table) {
		this.mDestTable = table;
		if(mOnTableChangeListener != null){
			mOnTableChangeListener.onTableChanged(table);
		}
	}

	/**
	 * Return the original foods in shopping cart.
	 * @return the original foods
	 */
	public List<OrderFood> getOriFoods(){
		if(mOriOrder != null){
			return mOriOrder.getOrderFoods();
		}else{
			return new ArrayList<OrderFood>(0);
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
			return mNewOrder.getOrderFoods();
		}else{
			return new ArrayList<OrderFood>(0);
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
		return new ArrayList<OrderFood>(mFoodsInCart);
	}

	/**
	 * Return the amount to all foods.
	 * @return the amount to all foods
	 */
	public int getAllAmount(){
		return mFoodsInCart.size();
	}
	
	public void setOriOrder(Order mOriOrder) {
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
		
		mFoodsInCart.clear();
		if(mNewOrder != null){
			for(OrderFood of : mNewOrder.getOrderFoods()){
				mFoodsInCart.add(of);
			}
		}
		if(mOriOrder != null){
			for(OrderFood of : mOriOrder.getOrderFoods()){
				mFoodsInCart.add(of);
			}
		}
		
		if(mOnFoodsChangedListener != null){
			mOnFoodsChangedListener.onFoodsChanged(getAllFoods());
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
		for(OrderFood of : mFoodsInCart){
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
			super(WirelessOrder.pinGen, reqOrder, type);
			mCommitListener = commitListener;
		}
		
		@Override
		protected void onPreExecute(){
			if(mCommitListener != null){
				mCommitListener.OnPreCommit(mReqOrder);
			}
		}		
		
		@Override
		protected void onPostExecute(Void arg){
			if(mBusinessException == null){
				mNewOrder = null;
			}
			if(mCommitListener != null){	
				mCommitListener.onPostCommit(mReqOrder, mBusinessException);
			}
		}		

	}
	
	public void clear(){
		clearTable();
		clearStaff();
		this.mFoodsInCart.clear();
	}
	
	public void clearTable(){
		this.mNewOrder = null;
		this.mDestTable = null;
		this.mOriOrder = null;
		mFoodsInCart.clear();
	}
	
	public void clearStaff(){
		this.mStaff = null;
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
