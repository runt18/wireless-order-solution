package com.wireless.pojo.billStatistics;

public class IncomeByService {
	
	private int mServiceAmount;		//服务费账单数
	private float mTotalService;	//服务费金额
	
	public IncomeByService(){
		
	}
	
	public IncomeByService(int serviceAmount, float totalService){
		setServiceAmount(serviceAmount);
		setTotalService(totalService);
	}

	public int getServiceAmount() {
		return mServiceAmount;
	}

	public void setServiceAmount(int mServiceAmount) {
		this.mServiceAmount = mServiceAmount;
	}

	public float getTotalService() {
		return mTotalService;
	}

	public void setTotalService(float totalService) {
		this.mTotalService = totalService;
	}


	
}
