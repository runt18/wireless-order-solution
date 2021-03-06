package com.wireless.lib.task;

import java.io.IOException;
import java.util.List;

import android.os.AsyncTask;

import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqMakeOnSale;
import com.wireless.pack.req.ReqMakeSellOut;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public abstract class UpdateSelloutStatusTask extends AsyncTask<Void, Void, Void>{

	private BusinessException mBusinessException;
	
	private final Staff mStaff;
	
	private final List<Food> mToSellOut;
	
	private final List<Food> mToOnSale;
	
	public UpdateSelloutStatusTask(Staff staff, List<Food> toSellOut, List<Food> toOnSale){
		mStaff = staff;
		mToSellOut = toSellOut;
		mToOnSale = toOnSale;
	}
	
	/**
	 * 在新的线程中执行请求更新菜品沽清状态
	 */
	@Override
	protected Void doInBackground(Void... args) {
	
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqMakeSellOut(mStaff, mToSellOut));
			if(resp.header.type == Type.NAK){
				mBusinessException = new BusinessException("提交菜品沽清信息不成功");
			}
			
			resp = ServerConnector.instance().ask(new ReqMakeOnSale(mStaff, mToOnSale));
			if(resp.header.type == Type.NAK){
				mBusinessException = new BusinessException("提交菜品开售信息不成功");
			}
		}catch(IOException e){
			mBusinessException = new BusinessException(e.getMessage());
		} catch (BusinessException e) {
			mBusinessException = e;
		}
		
		return null;
	}
	
	@Override
	protected final void onPostExecute(Void result){
		if(mBusinessException != null){
			onFail(mBusinessException);
		}else{
			onSuccess();
		}
	}
	
	public abstract void onSuccess();
	
	public abstract void onFail(BusinessException e);
}
