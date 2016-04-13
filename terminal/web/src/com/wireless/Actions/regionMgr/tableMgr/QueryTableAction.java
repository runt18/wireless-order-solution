package com.wireless.Actions.regionMgr.tableMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.util.DataPaging;

public class QueryTableAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		response.setContentType("text/json;charset=utf-8");
		JObject jobject = new JObject();
		List<Table> tables = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String pin = (String)request.getAttribute("pin");
			String tableId = request.getParameter("tableID");
			String alias = request.getParameter("alias");
			String name = request.getParameter("name");
			String regionId = request.getParameter("regionId");
			
			String orderClause = "";
			TableDao.ExtraCond extraCond = new TableDao.ExtraCond();
			
			if(tableId != null && !tableId.trim().isEmpty()){
				extraCond.setId(Integer.parseInt(tableId.trim()));
			}			
			if(alias != null && !alias.trim().isEmpty()){
				extraCond.setAliasId(Integer.parseInt(alias.trim()));
			}
			if(name != null && !name.trim().isEmpty()){
				extraCond.setName(name.trim());
			}
			if(regionId != null && !regionId.trim().isEmpty()){
				extraCond.setRegion(Integer.parseInt(regionId));
			}
			
			orderClause = "ORDER BY TBL.table_alias";
			tables = TableDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), extraCond, orderClause);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(tables != null){
				jobject.setTotalProperty(tables.size());
				tables = DataPaging.getPagingData(tables, Boolean.parseBoolean(isPaging), start, limit);
				jobject.setRoot(tables);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
