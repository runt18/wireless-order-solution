package com.wireless.db.client.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	/**
	 * 
	 * @param mp
	 * @return
	 * @throws Exception
	 */
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
	
	/**
	 * 
	 * @param dbCon
	 * @param mp
	 * @return
	 * @throws Exception
	 */
	public static int deleteMemberOperation(DBCon dbCon, MemberOperation mp) throws Exception {
		int count = 0;
		
		return count;
	}
	
	/**
	 * 
	 * @param mp
	 * @return
	 * @throws Exception
	 */
	public static int deleteMemberOperation(MemberOperation mp) throws Exception {
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberOperationDao.deleteMemberOperation(dbCon, mp);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<MemberOperation> getMemberOperation(DBCon dbCon, Map<Object, Object> params) throws Exception{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<MemberOperation> getMemberOperation(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getMemberOperation(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
}
