package com.wireless.pojo.distributionMgr;

import java.util.ArrayList;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.Material;

public class DistributionDelta implements Jsonable{

	private Material material;
	private int sendId;
	private float distributionSendAmount;
	private float distributionReceiveAmount;
	private float distributionReturnAmount;
	private float distributionRecoveryAmount;
	private List<StockDistribution> distributionSends = new ArrayList<StockDistribution>(); 
	private List<StockDistribution> distributionReceives = new ArrayList<StockDistribution>();
	private List<StockDistribution> distributionReturns = new ArrayList<StockDistribution>();
	private List<StockDistribution> distributionRecoverys = new ArrayList<StockDistribution>();
	
	public int getSendId() {
		return sendId;
	}

	public void setSendId(int associateId) {
		this.sendId = associateId;
	}

	public float getDistributionSendAmount() {
		return distributionSendAmount;
	}

	public void setDistributionSendAmount(float distributionSendAmount) {
		this.distributionSendAmount = distributionSendAmount;
	}

	public float getDistributionReceiveAmount() {
		return distributionReceiveAmount;
	}

	public void setDistributionReceiveAmount(float distributionReceiveAmount) {
		this.distributionReceiveAmount = distributionReceiveAmount;
	}

	public float getDistributionReturnAmount() {
		return distributionReturnAmount;
	}

	public void setDistributionReturnAmount(float distributionReturnAmount) {
		this.distributionReturnAmount = distributionReturnAmount;
	}

	public float getDistributionRecoveryAmount() {
		return distributionRecoveryAmount;
	}

	public void setDistributionRecoveryAmount(float distributionRecoveryAmount) {
		this.distributionRecoveryAmount = distributionRecoveryAmount;
	}

	public void setDistributionRecoverys(List<StockDistribution> distributionRecoverys) {
		this.distributionRecoverys = distributionRecoverys;
	}

	public void setDistributionSends(List<StockDistribution> distributionSends){
		this.distributionSends.clear();
		this.distributionSends.addAll(distributionSends);
	}
	
	public void setDistributionReceives(List<StockDistribution> distributionReceives){
		this.distributionReceives.clear();
		this.distributionReceives.addAll(distributionReceives);
	}
	
	public void setDistributionReturns(List<StockDistribution> distributionReturns){
		this.distributionReturns.clear();
		this.distributionReturns.addAll(distributionReturns);
	}
	
	public void setDistributionRecovery(List<StockDistribution> distributionRecovery){
		this.distributionRecoverys.clear();
		this.distributionRecoverys.addAll(distributionRecovery);
	}
	
	public void addDistributionRecoverys(StockDistribution stockDistribution){
		this.distributionRecoverys.add(stockDistribution);
	}
	
	public void addDistributionReturns(StockDistribution stockDistribution){
		this.distributionReturns.add(stockDistribution);
	}
	
	public void addDistributionReceives(StockDistribution stockDistribuion){
		this.distributionReceives.add(stockDistribuion);
	}
	
	public void addDistributionSends(StockDistribution stockDistribution){
		this.distributionSends.add(stockDistribution);
	}
	
	public List<StockDistribution> getDistributionSends() {
		return distributionSends;
	}

	public List<StockDistribution> getDistributionReceives() {
		return distributionReceives;
	}

	public List<StockDistribution> getDistributionReturns() {
		return distributionReturns;
	}

	public List<StockDistribution> getDistributionRecoverys() {
		return distributionRecoverys;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public float getSendDeltaAmount(){
		return this.distributionSendAmount - distributionReceiveAmount;
	}
	
	public float getReturnDeltaAmount(){
		return this.distributionReturnAmount - distributionRecoveryAmount;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("material", this.material, 0);
		jm.putInt("sendId", this.sendId);
		jm.putFloat("distributionSendAmount", this.distributionSendAmount);
		jm.putFloat("distributionReceiveAmount", this.distributionReceiveAmount);
		jm.putFloat("distributionReturnAmount", this.distributionReturnAmount);
		jm.putFloat("distributionRecoveryAmount", this.distributionRecoveryAmount);
		jm.putJsonableList("distributionSends", this.distributionSends, 0);
		jm.putJsonableList("distributionReceives", this.distributionReceives, 0);
		jm.putJsonableList("distributionReturns", this.distributionReturns, 0);
		jm.putJsonableList("distributionRecoverys", this.distributionRecoverys, 0);
		jm.putFloat("sendDeltaAmount", getSendDeltaAmount());
		jm.putFloat("returnDeltaAmount", getReturnDeltaAmount());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		// TODO Auto-generated method stub
	}

}
