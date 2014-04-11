package com.wireless.pojo.sms;

import com.wireless.pojo.sms.SMSDetail.Operation;

public class SMStat {

	public static class InsertBuilder{
		private final int restaurantId;
		public InsertBuilder(int restaurantId){
			this.restaurantId = restaurantId;
		}
		
		public SMStat build(){ 
			return new SMStat(restaurantId);
		}
	}
	
	public static class UpdateBuilder{
		private final int restaurantId;
		private final Operation operation;
		private int amount;
		
		public UpdateBuilder(int restaurantId, Operation operation){
			this.restaurantId = restaurantId;
			this.operation = operation;
		}
		
		public UpdateBuilder setAmount(int amount){
			if(amount < 0){
				throw new IllegalArgumentException();
			}else{
				this.amount = amount;
			}
			return this;
		}
		
		public int getRestaurantId(){
			return this.restaurantId;
		}
		
		public Operation getOperation(){
			return this.operation;
		}
		
		public int getAmount(){
			return this.amount;
		}
	}
	
	private int restaurantId;
	private int totalUsed;
	private int verificationUsed;
	private int consumptionUsed;
	private int chargeUsed;
	private int remaining;
	
	public SMStat(int restaurantId){
		this.restaurantId = restaurantId;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public int getTotalUsed() {
		return totalUsed;
	}
	
	public void setTotalUsed(int totalUsed) {
		this.totalUsed = totalUsed;
	}
	
	public int getVerificationUsed() {
		return verificationUsed;
	}
	
	public void setVerificationUsed(int verificationUsed) {
		this.verificationUsed = verificationUsed;
	}
	
	public int getConsumptionUsed() {
		return consumptionUsed;
	}
	
	public void setConsumptionUsed(int consumptionUsed) {
		this.consumptionUsed = consumptionUsed;
	}
	
	public int getChargeUsed() {
		return chargeUsed;
	}
	
	public void setChargeUsed(int chargeUsed) {
		this.chargeUsed = chargeUsed;
	}
	
	public int getRemaining() {
		return remaining;
	}
	
	public void setRemaining(int remaining) {
		this.remaining = remaining;
	}
	
	public SMSDetail use4Verify(int amount){
		totalUsed += amount;
		verificationUsed += amount;
		remaining -= amount;
		remaining = remaining >= 0 ? remaining : 0;
		
		SMSDetail detail = new SMSDetail(0);
		detail.setOperation(Operation.USE_VERIFY);
		detail.setModified(System.currentTimeMillis());
		detail.setDelta(amount);
		detail.setRemaining(remaining);
		
		return detail;
	}
	
	public SMSDetail use4Charge(int amount){
		totalUsed += amount;
		chargeUsed += amount;
		remaining -= amount;
		remaining = remaining >= 0 ? remaining : 0;
		
		SMSDetail detail = new SMSDetail(0);
		detail.setOperation(Operation.USE_CHARGE);
		detail.setModified(System.currentTimeMillis());
		detail.setDelta(amount);
		detail.setRemaining(remaining);
		
		return detail;
	}
	
	public SMSDetail use4Consume(int amount){
		totalUsed += amount;
		consumptionUsed += amount;
		remaining -= amount;
		remaining = remaining >= 0 ? remaining : 0;
		
		SMSDetail detail = new SMSDetail(0);
		detail.setOperation(Operation.USE_CONSUME);
		detail.setModified(System.currentTimeMillis());
		detail.setDelta(amount);
		detail.setRemaining(remaining);
		
		return detail;
	}
	
	
	public SMSDetail add(int amount){
		remaining += amount;
		
		SMSDetail detail = new SMSDetail(0);
		detail.setOperation(Operation.ADD);
		detail.setModified(System.currentTimeMillis());
		detail.setDelta(amount);
		detail.setRemaining(remaining);
		
		return detail;
	}
	
	public SMSDetail deduct(int amount){
		remaining -= amount;
		remaining = remaining >= 0 ? remaining : 0;
		
		SMSDetail detail = new SMSDetail(0);
		detail.setOperation(Operation.DEDUCT);
		detail.setModified(System.currentTimeMillis());
		detail.setDelta(amount);
		detail.setRemaining(remaining);
		
		return detail;
	}
	
	@Override
	public int hashCode(){
		return restaurantId * 17 + 31;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof SMStat)){
			return false;
		}else{
			return restaurantId == ((SMStat)obj).restaurantId;
		}
	}
	
	@Override
	public String toString(){
		return "sms stat(" + restaurantId + ")";
	}
}
