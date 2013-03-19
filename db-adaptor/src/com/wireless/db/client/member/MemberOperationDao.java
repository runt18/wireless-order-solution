package com.wireless.db.client.member;

import com.wireless.db.DBCon;
import com.wireless.pojo.client.MemberOperation;

public class MemberOperationDao {

	/**
	 * 
	 * @param dbCon
	 * @param mp
	 * @return
	 */
	public static int insertMemberOperation(DBCon dbCon, MemberOperation mp) throws Exception {
		int count = 0;
		
		return count;
	}
	
	
	public static int insertMemberOperation(MemberOperation mp) throws Exception {
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberOperationDao.insertMemberOperation(dbCon, mp);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
}
