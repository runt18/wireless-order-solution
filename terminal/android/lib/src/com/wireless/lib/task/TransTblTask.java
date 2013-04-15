package com.wireless.lib.task;

import java.io.IOException;

import android.os.AsyncTask;

import com.wireless.pack.ErrorCode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqTransTbl;
import com.wireless.protocol.PTable;
import com.wireless.sccon.ServerConnector;

public class TransTblTask extends AsyncTask<PTable, Void, Void>{

	protected String mErrMsg;
	
	@Override
	protected Void doInBackground(PTable... table) {
		
		PTable srcTbl = table[0];
		PTable destTbl = table[1];
		
		try{
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqTransTbl(new PTable[]{srcTbl, destTbl}));
			if(resp.header.type == Type.ACK){
				
			}else{
				if(resp.header.reserved == ErrorCode.TABLE_NOT_EXIST){
					mErrMsg = srcTbl.getAliasId() + "或" + destTbl.getAliasId() + "号台信息不存在";
					
				}else if(resp.header.reserved == ErrorCode.TABLE_IDLE){
					mErrMsg = "原" + srcTbl.getAliasId() + "号台是空闲状态";
					
				}else if(resp.header.reserved == ErrorCode.TABLE_BUSY){
					mErrMsg = "新" + destTbl.getAliasId() + "号台是就餐状态，请跟餐厅经理确认";
					
				}else{
					mErrMsg = srcTbl.getAliasId() + "号台转至" + destTbl.getAliasId() + "号台不成功";
				}
			}			
		}catch(IOException e){
			mErrMsg = e.getMessage();
		}
		
		return null;
		
	}

}
