package com.wireless.Actions.regionMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.regionMgr.TableDao;

public class QueryRegionTableAction extends Action {

	// 操作符： 1表示等于号，2表示大于等于，3表示小于等于
	private final static String EQUAL_TO = "1";// 等于
	private final static String EQUAL_THAN = "2";// 大于等于
	private final static String EQUAL_LESS = "3";// 小于等于

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");
		DBCon dbCon = new DBCon();
		StringBuffer jsonSB = new StringBuffer();
		try{
			int totalProperty = 0;// 总记录数；
			int start = Integer.parseInt(request.getParameter("start").toString());
			int limit = Integer.parseInt(request.getParameter("limit").toString());
			String regionID = request.getParameter("regionID");

			String restaurantID = request.getParameter("restaurantID");

			String operatorNumbersOS;
			if (EQUAL_TO.equals(request.getParameter("operatorNumbersO"))) {
				operatorNumbersOS = " = ";
			} else if (EQUAL_THAN.equals(request.getParameter("operatorNumbersO"))) {
				operatorNumbersOS = " >= ";
			} else if (EQUAL_LESS.equals(request.getParameter("operatorNumbersO"))) {
				operatorNumbersOS = " <= ";
			} else {
				operatorNumbersOS = " = ";
			}
			// 操作值
			String operatorNumbersN = request.getParameter("operatorNumbersN");
			String operatorNumbersA = request.getParameter("operatorNumbersA");
			// 餐台名字
			String operatorName = request.getParameter("operatorName");
			// 餐台状态
			String operatorStates = request.getParameter("operatorStates");
			// 餐台类型
			String operatorTypes = request.getParameter("operatorTypes");

			TableDao.queryAll(jsonSB, restaurantID, regionID, operatorName, operatorNumbersN, operatorNumbersOS, operatorNumbersA, operatorStates, operatorTypes, start, limit, totalProperty);
		}
		finally{
			response.getWriter().print(jsonSB.toString());
		}
		return null;
	}
}
