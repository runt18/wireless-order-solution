package com.wireless.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wireless.excep.BusinessException;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Type;

public final class ShoppingCart {
	
	private StaffTerminal mStaff;
	private Table mDestTable;
	//private List<OrderFood> mExtraFoods = new LinkedList<OrderFood>();
	
	private Order mNewOrder;
	
	private Order mOriOrder;
	
	private static ShoppingCart mInstance = new ShoppingCart();
	
	private ShoppingCart(){
	}
	
	public static ShoppingCart instance(){
		return mInstance;
	}

	public interface OnFoodsChangeListener{
		void onFoodsChange(List<OrderFood> newFoods);
	}
	private OnFoodsChangeListener mOnFoodsChangeListener;
	
	public void setOnFoodsChangeListener(OnFoodsChangeListener l)
	{
		mOnFoodsChangeListener = l;
	}
	
	public interface OnTableChangeListener{
		void onTableChange(Table table);
	}
	private OnTableChangeListener mOnTableChangeListener;
	
	public void setOnTableChangeListener(OnTableChangeListener l)
	{
		mOnTableChangeListener = l;
	}
	
	/**
	 * Commit the order.
	 * Perform to insert a new order if original order NOT exist.
	 * Otherwise perform to update the order.
	 * @param commitListener
	 * 			the commit listener
	 * @throws BsinessException
	 * 			throws if NOT valid to commit order
	 */
	public void commit(OnCommitListener commitListener) throws BusinessException{
		if(mOriOrder != null){
			checkCommitValid();
			Order reqOrder = new Order(mOriOrder.foods, mDestTable.aliasID, mDestTable.customNum);		
			reqOrder.orderDate = mOriOrder.orderDate;
			if(hasNewOrder()){
				reqOrder.addFoods(mNewOrder.foods);
			}
			new CommitOrderTask(reqOrder, commitListener).execute(Type.UPDATE_ORDER);
			
		}else{
			checkCommitValid();
			Order reqOrder = new Order(mNewOrder.foods, mDestTable.aliasID, mDestTable.customNum);			
			new CommitOrderTask(reqOrder, commitListener).execute(Type.INSERT_ORDER);
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
			throw new NullPointerException("The original order can NOT be null.");
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
			mNewOrder.foods = new OrderFood[0];
		}
	}
	
	/**
	 * Remove the specific order food.
	 * @param foodToDel 
	 * 			the food to remove
	 * @return true if the food to be removed exist as before, otherwise return false
	 */
	public boolean remove(OrderFood foodToDel){
		if(mNewOrder != null){
			return mNewOrder.remove(foodToDel);
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
		mNewOrder.addFood(foodToAdd);
		notifyFoodsChange();
	}

	/**
	 * 替换原来购物篮中的菜品。
	 * @param foodToReplace
	 * @return true if the food to be replaced exist as before, otherwise return false
	 */
	public boolean replaceFood(OrderFood foodToReplace){
		if(mNewOrder != null){
			for(int i = 0; i < mNewOrder.foods.length; i++){
				if(mNewOrder.foods[i].equals(foodToReplace)){
					mNewOrder.foods[i] = foodToReplace;
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
		if(mOnTableChangeListener != null)
			mOnTableChangeListener.onTableChange(table);
	}

	
	public Order getOriOrder() {
		return mOriOrder;
	}
	
	public List<OrderFood> getOriFoods(){
		if(mOriOrder != null && mOriOrder.foods != null){
			return Arrays.asList(mOriOrder.foods);
		}else{
			return new ArrayList<OrderFood>(0);
		}		
	}
	
	/**
	 * Return the list to extra foods.
	 * @return
	 */
	public List<OrderFood> getNewFoods(){
		if(mNewOrder != null && mNewOrder.foods != null){
			return Arrays.asList(mNewOrder.foods);
		}else{
			return new ArrayList<OrderFood>(0);
		}
	}
	
	public Order getNewOrder(){
		return mNewOrder;
	}
	
	public List<OrderFood> getAllFoods(){
		ArrayList<OrderFood> allFoods = new ArrayList<OrderFood>();
		if(hasNewOrder()){
			allFoods.addAll(getNewFoods());
		}
		if(hasOriOrder()){
			allFoods.addAll(getOriFoods());
		}		
		return allFoods;		
	}

	public void setOriOrder(Order mOriOrder) {
		this.mOriOrder = mOriOrder;
		notifyFoodsChange();
	}
	
	public boolean hasTable(){
		return mDestTable == null ? false : true;
	}
	
	public boolean hasOriOrder(){
		if(mOriOrder != null){
			return mOriOrder.foods != null ? mOriOrder.foods.length != 0 : false;
		}else{
			return false;
		}
	}
	
	public boolean hasNewOrder(){
		if(mNewOrder != null){
			return mNewOrder.foods != null ? mNewOrder.foods.length != 0 : false;
		}else{
			return false;
		}
	}
	
	public boolean hasOrder(){
		return hasOriOrder() || hasNewOrder();
	}	


	private void notifyFoodsChange(){
		if(mOnFoodsChangeListener != null){
			mOnFoodsChangeListener.onFoodsChange(getAllFoods());
		}
	}
	
	/**
	 * 执行账单的提交请求
	 */
	private class CommitOrderTask extends com.wireless.lib.task.CommitOrderTask{		
		
		private OnCommitListener mCommitListener;
		
		CommitOrderTask(Order reqOrder, OnCommitListener commitListener){
			super(reqOrder);
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
	
	public static interface OnCommitListener{
		public void OnPreCommit(Order reqOrder);
		//public void onCommit(){};
		public void onPostCommit(Order reqOrder, BusinessException e);
	}
	
}
