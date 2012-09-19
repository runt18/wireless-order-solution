package com.wireless.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.wireless.excep.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;
import com.wireless.protocol.Type;

public final class ShoppingCart {
	
	private StaffTerminal mStaff;
	private Table mDestTable;
	private List<OrderFood> mExtraFoods = new LinkedList<OrderFood>();
	
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
			for(OrderFood oriFood : mOriOrder.foods){
				addFood(oriFood);
			}
			mOriOrder.foods = mExtraFoods.toArray(new OrderFood[mExtraFoods.size()]);
			new CommitOrderTask(mOriOrder, commitListener).execute(Type.UPDATE_ORDER);
			
		}else{
			if(mExtraFoods.isEmpty()){
				throw new BusinessException(ErrorCode.UNKNOWN);
			}else{
				checkCommitValid();
				new CommitOrderTask(new Order(mExtraFoods.toArray(new OrderFood[mExtraFoods.size()]), mDestTable.aliasID, mDestTable.customNum), 
									commitListener).execute(Type.INSERT_ORDER);
			}
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
	 * 			throws with ErrorCode.TABLE_NOT_EXIST in case table NOT set<br>
	 * 			throws with ErrorCode.TERMINAL_NOT_ATTACHED is case staff NOT set
	 */
	public void checkCommitValid() throws BusinessException{
		if(mDestTable == null){
			throw new BusinessException(ErrorCode.TABLE_NOT_EXIST);
		}else if(mStaff == null){
			throw new BusinessException(ErrorCode.TERMINAL_NOT_ATTACHED);
		}
	}
	
	/**
	 * Return the list to extra foods.
	 * @return
	 */
	public List<OrderFood> getExtraFoods(){
		return mExtraFoods;
	}
	
	/**
	 * Remove all the extra foods to this shopping cart.
	 */
	public void removeAll(){
		mExtraFoods.clear();
	}
	
	/**
	 * Remove the specific order food.
	 * @param foodToDel 
	 * 			the food to remove
	 * @return true if the food to be removed exist as before, otherwise return false
	 */
	public boolean remove(OrderFood foodToDel){
		return mExtraFoods.remove(foodToDel);
	}
	
	/**
	 * 把多个新点菜添加到购物篮中。 遍历查找已点的新菜品中是否相同的菜品， 如果有就将他们的点菜数量相加， 否则就直接添加到新菜品List中。
	 * 
	 * @param extraFoods
	 *            要添加的多个菜品
	 */
	public void addAll(List<OrderFood> extraFoods){
		for(OrderFood extraFood : extraFoods){
			addFood(extraFood);
		}
	}
	
	/**
	 * 把新点菜添加到购物篮中。 遍历查找已点的新菜品中是否相同的菜品， 如果有就将他们的点菜数量相加， 否则就直接添加到新菜品List中。
	 * 
	 * @param extraFood
	 *            要添加的菜品
	 */
	public void addFood(OrderFood extraFood) {
		int index = mExtraFoods.indexOf(extraFood);
		if(index != -1){
			OrderFood oriFood = mExtraFoods.get(index);
			float orderAmount = oriFood.getCount() + extraFood.getCount();
			oriFood.setCount(orderAmount);
		}else{
			mExtraFoods.add(extraFood);
		}
		notifyFoodsChange();
	}

	/**
	 * 替换原来购物篮中的菜品。
	 * @param foodToReplace
	 * @return true if the food to be replaced exist as before, otherwise return false
	 */
	public boolean replaceFood(OrderFood foodToReplace){
		int index = mExtraFoods.indexOf(foodToReplace);
		if(index != -1){
			mExtraFoods.set(index, foodToReplace);
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * @return the mStaff
	 */
	public StaffTerminal getStaff() {
		return mStaff;
	}

	/**
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
	}

	
	public Order getOriOrder() {
		return mOriOrder;
	}

	public void setOriOrder(Order mOriOrder) {
		this.mOriOrder = mOriOrder;
	}

	public boolean hasExtraFoods(){
		return !mExtraFoods.isEmpty();
	}
	
	private void notifyFoodsChange(){
		if(mOnFoodsChangeListener != null)
		{
			ArrayList<OrderFood> newFoods = new ArrayList<OrderFood>();
			if(mOriOrder != null)
				for(OrderFood f:mOriOrder.foods)
					newFoods.add(f);
			for(OrderFood f:getExtraFoods())
				newFoods.add(f);
			mOnFoodsChangeListener.onFoodsChange(newFoods);
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
		protected void onPostExecute(BusinessException e){
			mExtraFoods.clear();
			if(mCommitListener != null){	
				mCommitListener.onPostCommit(mReqOrder, e);
			}
		}		

	}
	
	public static interface OnCommitListener{
		public void OnPreCommit(Order reqOrder);
		//public void onCommit(){};
		public void onPostCommit(Order reqOrder, BusinessException e);
	}
	
}
