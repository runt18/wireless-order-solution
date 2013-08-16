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
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryTableAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		List<Table> tables = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			String alias = request.getParameter("alias");
			String name = request.getParameter("name");
			String regionId = request.getParameter("regionId");
			
			String extraCond = "", orderClause = "";
			if(alias != null && !alias.trim().isEmpty()){
				extraCond += (" AND TBL.table_alias = " + alias.trim());
			}
			if(name != null && !name.trim().isEmpty()){
				extraCond += (" AND TBL.name LIKE '%" + name.trim() + "%' ");
			}
			if(regionId != null && !regionId.trim().isEmpty()){
				extraCond += (" AND REGION.region_id = " + regionId);
			}
			
			orderClause = "ORDER BY TBL.table_alias";
			tables = TableDao.getTables(StaffDao.verify(Integer.parseInt(pin), Privilege.Code.FRONT_BUSINESS), extraCond, orderClause);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(tables != null){
				jobject.setTotalProperty(tables.size());
				tables = DataPaging.getPagingData(tables, isPaging, start, limit);
				jobject.setRoot(tables);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
