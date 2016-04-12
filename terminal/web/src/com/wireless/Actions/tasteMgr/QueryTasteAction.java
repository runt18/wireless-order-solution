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

public class QueryTasteAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		
		List<Taste> root = null;
		JObject jobject = new JObject();
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String)request.getAttribute("pin");
			String price = request.getParameter("price");
			String name = request.getParameter("name");
			String cate = request.getParameter("cate");
			
			String ope = request.getParameter("ope");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			TasteDao.ExtraCond extraCond = new TasteDao.ExtraCond();
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
			
			if(price != null && !price.trim().isEmpty() && !price.equals("")){
				extraCond.setPrice(ope, Float.parseFloat(price));
			}
			if(name != null && !name.trim().isEmpty() && !name.equals("")){
				extraCond.setPreference(name);
			}
			if(cate != null && !cate.trim().isEmpty() && !cate.equals("-1")){
				extraCond.setCategory(Integer.parseInt(cate));
			}
			
			root = TasteDao.getByCond(staff, extraCond, " ORDER BY TASTE.taste_id ");
		} catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
			
		} catch(Exception e) {
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(DataPaging.getPagingData(root, Boolean.parseBoolean(isPaging), start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
}