package com.wireless.util;

import java.util.Map;

public class SQLUtil {
	
	/**
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public static String bindSQLParams(String sql, Map<String, Object> params){
		if(params != null){
			Object extra = null, groupBy = null, having = null, orderBy = null, limit_offSet = null, limit_rowCount = null;
			String ts = " ";
			extra = params.get(WebParams.SQL_PARAMS_EXTRA);
			groupBy = params.get(WebParams.SQL_PARAMS_GROUPBY);
			having = params.get(WebParams.SQL_PARAMS_HAVING);
			orderBy = params.get(WebParams.SQL_PARAMS_ORDERBY);
			limit_offSet = params.get(WebParams.SQL_PARAMS_LIMIT_OFFSET);
			limit_rowCount = params.get(WebParams.SQL_PARAMS_LIMIT_ROWCOUNT);
			
			sql += (extra != null ? ts + extra : ts);
			sql += (groupBy != null ? ts + groupBy : ts);
			sql += (having != null ? ts + having : "");
			sql += (orderBy != null ? ts + orderBy : ts);
			sql += (limit_offSet != null && limit_rowCount != null ? ts + "LIMIT " + limit_offSet + "," + limit_rowCount : ts);
			
			ts = null;
		}
		return sql;
	}
	
}
