package com.wireless.Actions.tasteMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryTasteAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		List<Taste> root = null;
		JObject jobject = new JObject();
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			String alias = request.getParameter("alias");
			String price = request.getParameter("price");
			String name = request.getParameter("name");
			String cate = request.getParameter("cate");
			
			String ope = request.getParameter("ope");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			String extraCond = "";
			if(ope != null && !ope.trim().isEmpty() && !ope.equals("")){
				try{
					switch(Integer.valueOf(ope)){
						case 1:
							ope = "=";
							break;
						case 2:
							ope = ">=";
							break;
						case 3:
							ope = "<=";
							break;
						default:
							ope = "=";
					}					
				}catch(NumberFormatException e){
					ope = "=";
				}
			}
			
			if(alias != null && !alias.trim().isEmpty() && !alias.equals("")){
				extraCond += (" AND TASTE.taste_alias " + ope + alias);
			}
			if(price != null && !price.trim().isEmpty() && !price.equals("")){
				extraCond += (" AND TASTE.price " + ope + price);
			}
			if(name != null && !name.trim().isEmpty() && !name.equals("")){
				extraCond += (" AND TASTE.preference like '%" + name + "%' ");
			}
			if(cate != null && !cate.trim().isEmpty() && !cate.equals("")){
				extraCond += (" AND TASTE.category = " + cate);
			}
			
			root = TasteDao.getTastes(staff, extraCond, " ORDER BY TASTE.taste_alias ");
		} catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		} catch(Exception e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}