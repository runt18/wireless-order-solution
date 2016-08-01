package com.wireless.pojo.distributionMgr;

import com.wireless.exception.BusinessException;
import com.wireless.exception.DistributionError;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.SubType;

public class StockDistribution implements Jsonable{

	private int id;
	private int stockActionId;
	private StockAction stockAction;
	private int stockInRestaurantId;
	private Restaurant stockInRestaurant;
	private int stockOutRestaurantId;
	private Restaurant stockOutRestaurant;
	private int associateId;
	private Status status;
	
	public StockDistribution(int id){
		this.id = id;
	}
	
	public StockDistribution(InsertBuilder insertBuilder){
		this.stockInRestaurantId = insertBuilder.stockInRestaurantId;
		this.stockOutRestaurantId = insertBuilder.stockOutRestaurantId;
		this.status = StockDistribution.Status.SINGLE;
		this.stockAction = insertBuilder.builder.build();
		this.associateId = insertBuilder.associateId;
	}
	
	public StockDistribution(UpdateBuilder updateBuilder){
		this.id = updateBuilder.id;
		this.associateId = updateBuilder.associateId;
		this.stockInRestaurantId = updateBuilder.stockInRestaurantId;
		this.stockOutRestaurantId = updateBuilder.stockOutRestaurantId;
		this.status = updateBuilder.status;
		if(updateBuilder.builder != null){
			this.stockAction = updateBuilder.builder.build();
		}
	}
	
	public StockDistribution(ReAuditBuilder reAuditBuilder){
		this.id = reAuditBuilder.id;
		this.associateId = reAuditBuilder.associateId;
		this.stockInRestaurantId = reAuditBuilder.stockInRestaurantId;
		this.stockOutRestaurantId = reAuditBuilder.stockOutRestaurantId;
		if(reAuditBuilder.builder != null){
			this.stockAction = reAuditBuilder.builder.build();
		}
	}
	
	public StockDistribution(AuditBuilder builder){
		this.id = builder.id;
		builder.builder = StockAction.AuditBuilder.newStockActionAudit(builder.stockActionId)
												  .setApprover(builder.approver)
												  .setApproverId(builder.approverId)
												  .setStockInitApproverDate();
		this.stockAction = builder.builder.build();
	}
	
	public int getAssociateId() {
		return associateId;
	}

	public void setAssociateId(int associateId) {
		this.associateId = associateId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public StockAction getStockAction() {
		return stockAction;
	}

	public void setStockAction(StockAction stockAction) {
		this.stockAction = stockAction;
	}

	public Restaurant getStockInRestaurant() {
		return stockInRestaurant;
	}

	public void setStockInRestaurant(Restaurant stockInRestaurant) {
		this.stockInRestaurant = stockInRestaurant;
	}

	public Restaurant getStockOutRestaurant() {
		return stockOutRestaurant;
	}

	public void setStockOutRestaurant(Restaurant stockOutRestaurant) {
		this.stockOutRestaurant = stockOutRestaurant;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getStockActionId() {
		return stockActionId;
	}

	public void setStockActionId(int stockActionId) {
		this.stockActionId = stockActionId;
	}

	public int getStockInRestaurantId() {
		return stockInRestaurantId;
	}

	public void setStockInRestaurantId(int stockInRestaurantId) {
		this.stockInRestaurantId = stockInRestaurantId;
	}

	public int getStockOutRestaurantId() {
		return stockOutRestaurantId;
	}

	public void setStockOutRestaurantId(int stockOutRestaurantId) {
		this.stockOutRestaurantId = stockOutRestaurantId;
	}
	
	/**
	 * insertBuilder
	 * @author Administrator
	 */
	public static class InsertBuilder{
		private StockAction.InsertBuilder builder;
		private int stockInRestaurantId;
		private int stockOutRestaurantId;
		private int associateId; 
		
		private InsertBuilder(StockAction.InsertBuilder builder){
			this.builder = builder;
		}
		
		public static InsertBuilder newDistributionApply(StockAction.InsertBuilder builder){
			StockAction stockAction = builder.build();
			if(stockAction.getSubType() != SubType.DISTRIBUTION_APPLY){
				throw new IllegalArgumentException("库单类型建立错误");
			}
			return new InsertBuilder(builder);
		}
		
		public static InsertBuilder newDistributionSend(StockAction.InsertBuilder builder){
			StockAction stockAction = builder.build();
			if(stockAction.getSubType() != SubType.DISTRIBUTION_SEND){
				throw new IllegalArgumentException("库单类型建立错误");
			}
			
			return new InsertBuilder(builder);
		}
		
		public static InsertBuilder newDistributionRecevie(StockAction.InsertBuilder builder, int associateId){
			StockAction stockAction = builder.build();
			if(stockAction.getSubType() != SubType.DISTRIBUTION_RECEIVE){
				throw new IllegalArgumentException("库单类型建立错误");
			}
			
			return new InsertBuilder(builder).setAssociateId(associateId);
		}
		
		public static InsertBuilder newDistributionReturn(StockAction.InsertBuilder builder, int associateId){
			StockAction stockAction = builder.build();
			if(stockAction.getSubType() != SubType.DISTRIBUTION_RETURN){
				throw new IllegalArgumentException("库单类型建立错误");
			}
			
			return new InsertBuilder(builder).setAssociateId(associateId);
		}
		
		public static InsertBuilder newDistributionRecovery(StockAction.InsertBuilder builder, int associateId){
			StockAction stockAction = builder.build();
			if(stockAction.getSubType() != SubType.DISTRIBUTION_RECOVERY){
				throw new IllegalArgumentException("库单类型建立错误");
			}
			
			return new InsertBuilder(builder).setAssociateId(associateId);
		}
		
		public InsertBuilder setAssociateId(int associateId) {
			this.associateId = associateId;
			return this;
		}

		public InsertBuilder setBuilder(StockAction.InsertBuilder builder) {
			this.builder = builder;
			return this;
		}

		public StockAction.InsertBuilder getBuilder() {
			return builder;
		}

		public InsertBuilder setStockInRestaurantId(int stockInRestaurantId) {
			this.stockInRestaurantId = stockInRestaurantId;
			return this;
		}

		public InsertBuilder setStockInRestaurant(Restaurant stockInRestaurant) {
			this.stockInRestaurantId = stockInRestaurant.getId();
			return this;
		}
		
		public InsertBuilder setStockInRestaurant(int stockInRestaurantId) {
			this.stockInRestaurantId = stockInRestaurantId;
			return this;
		}
		
		public InsertBuilder setStockOutRestaurant(int stockOutRestaurantId) {
			this.stockOutRestaurantId = stockOutRestaurantId;
			return this;
		}
		
		public InsertBuilder setStockOutRestaurant(Restaurant restaurant){
			this.stockOutRestaurantId = restaurant.getId();
			return this;
		}
		
		public StockDistribution build(){
			return new StockDistribution(this);
		}
	}
	
	/**
	 * UpdateBuilder
	 * @author Administrator
	 */
	public static class UpdateBuilder{
		private int id;
		private StockAction.UpdateBuilder builder;
		private int stockInRestaurantId;
		private int stockOutRestaurantId;
		private int associateId;
		private Status status;
		
		private UpdateBuilder(int id, StockAction.UpdateBuilder builder){
			this.id = id;
			this.builder = builder;
		}
		
		
		public static UpdateBuilder newDistributionSend(int id){
			return newDistributionSend(id, null);
		}
		//配送发货
		public static UpdateBuilder newDistributionSend(int id, StockAction.UpdateBuilder builder){
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_SEND){
					throw new IllegalArgumentException("库单类型建立错误");
				}
			}
			
			return new UpdateBuilder(id, builder);
		}
		
		
		public static UpdateBuilder newDistributionRecevie(int id){
			return newDistributionRecevie(id, null);
		}
		//配送收货
		public static UpdateBuilder newDistributionRecevie(int id, StockAction.UpdateBuilder builder){
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_RECEIVE){
					throw new IllegalArgumentException("库单类型建立错误");
				}
			}
			return new UpdateBuilder(id, builder);
		}
		
		
		public static UpdateBuilder newDistributionReturn(int id){
			return newDistributionReturn(id, null);
		}
		//配送退货
		public static UpdateBuilder newDistributionReturn(int id, StockAction.UpdateBuilder builder){
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_RETURN){
					throw new IllegalArgumentException("库单类型建立错误");
				}
			}
			return new UpdateBuilder(id, builder);
		}
		
		
		public static UpdateBuilder newDistributionRecovery(int id){
			return newDistributionRecovery(id, null);
		}
		//配送回收
		public static UpdateBuilder newDistributionRecovery(int id, StockAction.UpdateBuilder builder){
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_RECOVERY){
					throw new IllegalArgumentException("库单类型建立错误");
				}
			}
			return new UpdateBuilder(id, builder);
		}
		
		public static UpdateBuilder newDistributionApply(int id){
			return newDistributionApply(id, null);
		}
		//配送申请
		public static UpdateBuilder newDistributionApply(int id, StockAction.UpdateBuilder builder){
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_APPLY){
					throw new IllegalArgumentException("库单类型建立错误");
				}
			}
			return new UpdateBuilder(id, builder);
		}
		
		
		public StockAction.UpdateBuilder getBuilder() {
			return builder;
		}

		public void setBuilder(StockAction.UpdateBuilder builder) {
			this.builder = builder;
		}

		public void setId(int id) {
			this.id = id;
		}

		public UpdateBuilder setStockInRestaurant(int stockInRestaurantId) {
			this.stockInRestaurantId = stockInRestaurantId;
			return this;
		}
		
		public UpdateBuilder setStockInRestaurant(Restaurant restaurant){
			this.stockInRestaurantId = restaurant.getId();
			return this;
		}
		
		public boolean isStockInRestaurantChange(){
			return this.stockInRestaurantId != 0;
		}

		public UpdateBuilder setStockOutRestaurant(int stockOutRestaurantId) {
			this.stockOutRestaurantId = stockOutRestaurantId;
			return this;
		}
		
		public UpdateBuilder setStockOutRestaurant(Restaurant restaurant){
			this.stockOutRestaurantId = restaurant.getId();
			return this;
		}
		
		public boolean isStockOutRestaurantChange(){
			return this.stockOutRestaurantId != 0;
		}

		public UpdateBuilder setAssociateId(int associateId) {
			this.associateId = associateId;
			return this;
		}
		
		public boolean isAssociateIdChange(){
			return this.associateId != 0;
		}

		public UpdateBuilder setStatus(Status status) {
			this.status = status;
			return this;
		}
		
		public boolean isStatusChange(){
			return this.status != null;
		}
		
		public StockDistribution build(){
			return new StockDistribution(this);
		}
		
	}
	
	/**
	 * auditBuilder
	 * @author Administrator
	 */
	public static class AuditBuilder{
		private final int id;
		private StockAction.AuditBuilder builder;
		private int approverId;
		private String approver;
		private int stockActionId;

		public AuditBuilder(int id){
			this.id = id;
		} 
		
		public AuditBuilder setBuilder(StockAction.AuditBuilder builder) {
			this.builder = builder;
			return this;
		}

		public AuditBuilder setApproverId(int approverId) {
			this.approverId = approverId;
			return this;
		}

		public AuditBuilder setApprover(String approver) {
			this.approver = approver;
			return this;
		}

		public AuditBuilder setStockActionId(int stockActionId) {
			this.stockActionId = stockActionId;
			return this;
		}

		public StockDistribution build(){
			return new StockDistribution(this);
		}
		
		public StockAction.AuditBuilder getBuilder(){
			return this.builder;
		}
	}
	
	public static class ReAuditBuilder{
		private int id;
		private StockAction.ReAuditBuilder builder;
		private int stockInRestaurantId;
		private int stockOutRestaurantId;
		private int associateId;
		
		private ReAuditBuilder(int id, StockAction.ReAuditBuilder builder){
			this.id = id;
			this.builder = builder;
		}
		
		public static ReAuditBuilder newDistributionApply(int id, StockAction.ReAuditBuilder builder) throws BusinessException{
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_APPLY){
					throw new BusinessException(DistributionError.DISTRIBUTION_TYPE_NOMAP);
				}
			}
			return new ReAuditBuilder(id, builder);
		}
		
		public static ReAuditBuilder newDistributionSend(int id, StockAction.ReAuditBuilder builder)throws BusinessException{
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_SEND){
					throw new BusinessException(DistributionError.DISTRIBUTION_TYPE_NOMAP);
				}
			}
			
			return new ReAuditBuilder(id, builder);
		}
		
		public static ReAuditBuilder newDistributionRecevie(int id, StockAction.ReAuditBuilder builder)throws BusinessException{
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_RECEIVE){
					throw new BusinessException(DistributionError.DISTRIBUTION_TYPE_NOMAP);
				}
			}
			return new ReAuditBuilder(id, builder);
		}
		
		public static ReAuditBuilder newDistributionReturn(int id, StockAction.ReAuditBuilder builder)throws BusinessException{
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_RETURN){
					throw new BusinessException(DistributionError.DISTRIBUTION_TYPE_NOMAP);
				}
			}
			return new ReAuditBuilder(id, builder);
		}
		
		public static ReAuditBuilder newDistributionRecovery(int id, StockAction.ReAuditBuilder builder)throws BusinessException{
			if(builder != null){
				StockAction stockAction = builder.build();
				if(stockAction.getSubType() != SubType.DISTRIBUTION_RECOVERY){
					throw new BusinessException(DistributionError.DISTRIBUTION_TYPE_NOMAP);
				}
			}
			return new ReAuditBuilder(id, builder);
		}
		
		public StockAction.ReAuditBuilder getBuilder() {
			return builder;
		}

		public void setBuilder(StockAction.ReAuditBuilder builder) {
			this.builder = builder;
		}

		public void setId(int id) {
			this.id = id;
		}

		public ReAuditBuilder setStockInRestaurant(int stockInRestaurantId) {
			this.stockInRestaurantId = stockInRestaurantId;
			return this;
		}
		
		public ReAuditBuilder setStockInRestaurant(Restaurant restaurant){
			this.stockInRestaurantId = restaurant.getId();
			return this;
		}
		
		public boolean isStockInRestaurantChange(){
			return this.stockInRestaurantId != 0;
		}

		public ReAuditBuilder setStockOutRestaurant(int stockOutRestaurantId) {
			this.stockOutRestaurantId = stockOutRestaurantId;
			return this;
		}
		
		public ReAuditBuilder setStockOutRestaurant(Restaurant restaurant){
			this.stockOutRestaurantId = restaurant.getId();
			return this;
		}
		
		public boolean isStockOutRestaurantChange(){
			return this.stockOutRestaurantId != 0;
		}

		public ReAuditBuilder setAssociateId(int associateId) {
			this.associateId = associateId;
			return this;
		}
		
		public boolean isAssociateIdChange(){
			return this.associateId != 0;
		}

		public StockDistribution build(){
			return new StockDistribution(this);
		}
	}
	
	public static enum Status{
		SINGLE(1, "未绑定"),
		MARRIED(2, "已绑定");
		
		private int val;
		private String desc;
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("the val = (" + val + ") is invaild");
		}
		
		@Override
		public String toString() {
			return "Static ( " + 
				   "val = " + this.val + "," +
				   "desc = " + this.desc + ")";
		}
		
		public int getValue(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putJsonable("stockAction", this.stockAction, 0);
		jm.putJsonable("stockInRestaurant", this.stockInRestaurant, 0);
		jm.putJsonable("stockOutRestaurant", this.stockOutRestaurant, 0);
		jm.putInt("associateId", this.associateId);
		if(this.status != null){
			jm.putString("statusText", this.status.desc);
			jm.putInt("statusValue", this.status.val);
		}
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
	}
	
	@Override
	public String toString(){
		return Integer.toString(this.id);
	}
	
}
