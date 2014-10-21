package com.wireless.pojo.staffMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.PricePlan;

public class Privilege4Price extends Privilege implements Jsonable{
	
	private final List<PricePlan> pricePlans = new ArrayList<PricePlan>();

	public Privilege4Price(){
		super(Privilege.Code.PRICE_PLAN);
	}
	
	public Privilege4Price(int id){
		super(id);
		setCode(Privilege.Code.PRICE_PLAN);
	}
	
	public List<PricePlan> getPricePlans(){
		return Collections.unmodifiableList(pricePlans);
	}
	
	public void addPricePlan(PricePlan pricePlan){
		if(pricePlan != null){
			pricePlans.add(pricePlan);
		}
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = super.toJsonMap(flag);
		jm.putJsonableList("prices", pricePlans, 0);
		return jm;
	}
}
