package com.wireless.util;

import java.util.Map;

public class SQLUtil {
	
	public static final String SQL_PARAMS_EXTRA = "EXTRA";
	public static final String SQL_PARAMS_GROUPBY = "GROUPBY";
	public static final String SQL_PARAMS_HAVING = "HAVING";
	public static final String SQL_PARAMS_ORDERBY = "ORDERBY";
	public static final String SQL_PARAMS_LIMIT_OFFSET = "OFFSET";
	public static final String SQL_PARAMS_LIMIT_ROWCOUNT = "ROWCOUNT";
	public static final int SQL_PARAMS_LIMIT_OFFSET_VALUE = 0;
	public static final int SQL_PARAMS_LIMIT_ROWCOUNT_VALUE = 15;
	
	public static final String SQL_QUERY_LAST_INSERT_ID = "SELECT LAST_INSERT_ID()";
	
	/**
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public static String bindSQLParams(String sql, Map<Object, Object> params){
		String tempSQL = null, ts = null;
		if(sql != null && !sql.trim().isEmpty() && params != null){
			tempSQL = sql.substring(0);
			ts = " ";
			Object extra = null, groupBy = null, having = null, orderBy = null, limit_offSet = null, limit_rowCount = null;
			extra = params.get(SQLUtil.SQL_PARAMS_EXTRA);
			groupBy = params.get(SQLUtil.SQL_PARAMS_GROUPBY);
			having = params.get(SQLUtil.SQL_PARAMS_HAVING);
			orderBy = params.get(SQLUtil.SQL_PARAMS_ORDERBY);
			limit_offSet = params.get(SQLUtil.SQL_PARAMS_LIMIT_OFFSET);
			limit_rowCount = params.get(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT);
			
			tempSQL += (extra != null ? ts + extra : ts);
			tempSQL += (groupBy != null ? ts + groupBy : ts);
			tempSQL += (having != null ? ts + having : ts);
			tempSQL += (orderBy != null ? ts + orderBy : ts);
			tempSQL += (limit_offSet != null && limit_rowCount != null ? ts + "LIMIT" + ts + limit_offSet + "," + limit_rowCount : ts);
			
			ts = null;
		}
		return tempSQL;
	}
	
}
