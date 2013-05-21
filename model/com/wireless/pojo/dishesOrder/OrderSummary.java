package com.wireless.pojo.dishesOrder;

public class OrderSummary {

	public final static OrderSummary DUMMY = new OrderSummary(new Builder());
	
	public static class Builder{
		private int totalAmount;
		private float totalPrice;
		private float totalRepaidPrice;
		private float totalActualPrice;
		private float totalGiftPrice;
		private float totalDiscountPrice;
		private float totalCancelPrice;
		
		public Builder setTotalAmount(int totalAmount){
			this.totalAmount = totalAmount;
			return this;
		}
		
		public Builder setTotalPrice(float totalPrice){
			this.totalPrice = totalPrice;
			return this;
		}
		
		public Builder setTotalRepaidPrice(float totalRepaidPrice){
			this.totalRepaidPrice = totalRepaidPrice;
			return this;
		}
		
		public Builder setTotalActualPrice(float totalActualPrice){
			this.totalActualPrice = totalActualPrice;
			return this;
		}
		
		public Builder setTotalGiftPrice(float totalGiftPrice){
			this.totalGiftPrice = totalGiftPrice;
			return this;
		}
		
		public Builder setTotalDiscountPrice(float totalDiscountPrice){
			this.totalDiscountPrice = totalDiscountPrice;
			return this;
		}
		
		public Builder setTotalCancelPrice(float totalCancelPrice){
			this.totalCancelPrice = totalCancelPrice;
			return this;
		}
		
		public OrderSummary build(){
			return new OrderSummary(this);
		}
	}
	
	private final int totalAmount;
	private final float totalPrice;
	private final float totalRepaidPrice;
	private final float totalActualPrice;
	private final float totalGiftPrice;
	private final float totalDiscountPrice;
	private final float totalCancelPrice;
	
	private OrderSummary(Builder builder){
		this.totalAmount = builder.totalAmount;
		this.totalPrice = builder.totalPrice;
		this.totalRepaidPrice = builder.totalRepaidPrice;
		this.totalActualPrice = builder.totalActualPrice;
		this.totalGiftPrice = builder.totalGiftPrice;
		this.totalDiscountPrice = builder.totalDiscountPrice;
		this.totalCancelPrice = builder.totalCancelPrice;
	}
	
	public int getTotalAmount(){
		return this.totalAmount;
	}
	
	public float getTotalPrice(){
		return this.totalPrice;
	}

	public float getTotalRepaidPrice() {
		return totalRepaidPrice;
	}

	public float getTotalActualPrice() {
		return totalActualPrice;
	}

	public float getTotalGiftPrice() {
		return totalGiftPrice;
	}

	public float getTotalDiscountPrice() {
		return totalDiscountPrice;
	}

	public float getTotalCancelPrice() {
		return totalCancelPrice;
	}
}
