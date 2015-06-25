package com.wireless.Actions.menuMgr.taste;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteCategoryDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.TasteCategory;

public class QueryFoodTasteCategoryAction extends DispatchAction{

	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws Exception, SQLException, BusinessException{
		String pin = (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		JObject jobject = new JObject();
		StringBuilder tree = new StringBuilder(); 
		List<TasteCategory> list = new ArrayList<TasteCategory>();
		try{
			list = TasteCategoryDao.getByCond(staff, null, null);
			for (int i = 0; i < list.size(); i++) {
				if(i > 0){
					tree.append(",");
				}
				tree.append("{");
				tree.append("id : " + list.get(i).getId());
				tree.append(",text : '" + list.get(i).getName() + "'");
				tree.append(",tasteCateName : '" + list.get(i).getName() + "'");
				tree.append(",status : " + list.get(i).getStatus().getVal() );
				tree.append(",type : " + list.get(i).getType().getVal());
				tree.append(",displayId : " + list.get(i).getDisplayId());
				tree.append(",leaf : true");
				tree.append("}");
			}
		}catch(SQLException e){
			jobject.initTip(e);
		}catch(Exception e){
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print("[" + tree.toString() + "]");
		}
		return null;
	}

}
