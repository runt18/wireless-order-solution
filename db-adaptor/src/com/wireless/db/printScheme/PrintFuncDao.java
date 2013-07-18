package com.wireless.db.printScheme;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.printScheme.PrintFunc;
import com.wireless.protocol.Terminal;

public class PrintFuncDao {
	
	public static int addFunc(DBCon dbCon, Terminal term, int printerId, PrintFunc.SummaryBuilder builder) throws SQLException, BusinessException{
		return 0;
	}
	
	public static int addFunc(DBCon dbCon, Terminal term, int printerId, PrintFunc.DetailBuilder builder) throws SQLException, BusinessException{
		return 0;
	}
	
	public static void removeFunc(DBCon dbCon, Terminal term, int funcId) throws SQLException{
		
	}
	
	public static void updateFunc(DBCon dbCon, Terminal term, int printerId, PrintFunc func) throws SQLException{
		
	}
}
