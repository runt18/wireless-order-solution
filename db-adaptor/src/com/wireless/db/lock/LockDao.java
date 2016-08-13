package com.wireless.db.lock;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.lock.Lock;

public class LockDao {

	private static boolean insert(DBCon dbCon, Lock.InsertBuilder builder){
		Lock lock = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".lock " +
			  "( operation_id, associated_id, birth_date ) VALUES ( " + +
			  lock.getOperation().getVal() + "," +
			  lock.getAssociatedId() + "," +
			  " NOW() " +
			  ")";
		try{
			dbCon.stmt.executeUpdate(sql);
			return true;
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}
	}
	
	private static void delete(DBCon dbCon, Lock.InsertBuilder builder) throws SQLException{
		Lock lock = builder.build();
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".lock WHERE 1 = 1 " +
			  " AND operation_id = " + lock.getOperation().getVal() +
			  " AND associated_id = " + lock.getAssociatedId();
		dbCon.stmt.executeUpdate(sql);
	}
	 
	public static <T> T lock(Lock.InsertBuilder builder, Callable<T> lockAction) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		boolean acquired = false;
		try{
			dbCon.connect();
			acquired = insert(dbCon, builder);
			if(acquired){
				if(lockAction != null){
					return lockAction.call();
				}else{
					return null;
				}
			}else{
				throw new BusinessException(("【$(operation)】的操作重复执行，系统已屏蔽此次操作").replace("$(operation)", builder.build().getOperation().toString()));
			}
			
		}catch(Exception e){
			if(e instanceof SQLException){
				throw (SQLException)e;
			}else if(e instanceof BusinessException){
				throw (BusinessException)e;
			}else{
				throw new BusinessException(e.getMessage());
			}
			
		}finally{
			if(acquired){
				delete(dbCon, builder);
			}
			dbCon.disconnect();
		}
	}
	
}
