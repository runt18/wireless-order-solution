package com.wireless.Actions.weixin.query;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.DataPaging;

public class QueryFoodAction extends DispatchAction{
	
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		String imageBrowseDefaultFile = this.getServlet().getInitParameter("imageBrowseDefaultFile");
		String imageBrowsePath = this.getServlet().getInitParameter("imageBrowsePath");
		
		try{
			String rid = request.getParameter("rid");
			
			String extraCond = " AND FOOD.restaurant_id = " + rid, orderClause = " ORDER BY FOOD.food_alias";
			extraCond += (" AND FOOD.status & " + Food.RECOMMEND + " <> 0");
			List<Food> list = FoodDao.getPureFoods(extraCond, orderClause);
			if(list != null){
				jobject.setTotalProperty(list.size());
				list = DataPaging.getPagingData(list, true, 0, 20);
				for(Food temp : list){
					if(temp.hasImage()){
						temp.setImage((imageBrowsePath + "/" + temp.getRestaurantId() + "/" + temp.getImage()));
					}else{
						temp.setImage(imageBrowseDefaultFile);
					}
				}
				jobject.setRoot(list);
			}
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
}
