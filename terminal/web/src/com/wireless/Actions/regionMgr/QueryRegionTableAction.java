package com.wireless.Actions.regionMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.Terminal;

public class QueryRegionTableAction extends Action {

	// 操作符： 1表示等于号，2表示大于等于，3表示小于等于
	private final static String EQUAL_TO = "1";// 等于
	private final static String EQUAL_THAN = "2";// 大于等于
	private final static String EQUAL_LESS = "3";// 小于等于

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/json; charset=utf-8");
		StringBuffer jsonSB = new StringBuffer();
		try{
			String pin = request.getParameter("pin");
			
			Terminal term = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
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
			String sqlAllCount = " AND TBL.restaurant_id = "
					+ restaurantID
					+ " "
					+ ""
					+ (regionID == null || regionID.trim().equals("")
							|| regionID == "" || regionID.equals("-1")
							|| regionID == "-1" ? ""
							: (" AND TBL.region_id = " + regionID))
					+ (operatorName == null || operatorName.trim().equals("") ? ""
							: " AND TBL.name like '%" + operatorName.trim() + "%' ")
					+ (operatorNumbersN == null
							|| operatorNumbersN.trim().equals("") ? ""
							: " AND TBL.table_alias " + operatorNumbersOS + " "
									+ operatorNumbersN.trim() + " ")
					+ (operatorNumbersA == null
							|| operatorNumbersA.trim().equals("") ? ""
							: " AND TBL.minimum_cost " + operatorNumbersOS + ""
									+ operatorNumbersA.trim() + " ")
					+ (operatorStates == null || operatorStates.trim().equals("") ? ""
							: " AND TBL.status like '%" + operatorStates.trim() + "%' ")
					+ (operatorTypes == null || operatorTypes.trim().equals("") ? ""
							: " AND TBL.category like '%" + operatorTypes.trim()
									+ "%' ");//
			totalProperty = TableDao.getTableCount(term, sqlAllCount);
			String extraCond = sqlAllCount
					+ " ORDER BY"
					+ " TBL.table_id "
					+ "LIMIT " + start + "," + limit + "";
			List<Table> tables = TableDao.getTables(term, extraCond, null); 
			int index = 0;
			jsonSB.append("{totalProperty:" + totalProperty + ",root:[");

			for(int i = 0;i < tables.size();i ++){
				jsonSB.append(index > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("tableID : '" + tables.get(i).getTableId() + "'");
				jsonSB.append(",");
				jsonSB.append("tableAlias : '" + tables.get(i).getTableAlias()+ "'");
				jsonSB.append(",");
				jsonSB.append("tableName : '" + tables.get(i).getTableName()+ "'");
				jsonSB.append(",");
				jsonSB.append("tableRegion : '" + tables.get(i).getRegion().getId()+ "'");
				jsonSB.append(",");
				jsonSB.append("tableMinCost : '"+ tables.get(i).getMinimumCost() + "'");
				jsonSB.append(",");
				jsonSB.append("tableServiceRate : '"+ tables.get(i).getServiceRate() + "'");
				jsonSB.append(",");
				jsonSB.append("tableStatusDisplay : '"+ tables.get(i).getStatus() + "'");
				jsonSB.append(",");
				jsonSB.append("tableCategoryDisplay : '"+ tables.get(i).getCategory() + "'");
				jsonSB.append(",");
				jsonSB.append("tableOpt : 'tableOpt'");
				jsonSB.append("}");
				index++;
			}
			jsonSB.append("]}");
		}
		finally{
			response.getWriter().print(jsonSB.toString());
		}
		return null;
	}
}
