package com.wireless.Actions.menuMgr.basic;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.DataPaging;
import com.wireless.util.WebParams;

public class QueryMenuMgrAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		
		JObject jobject = new JObject();
		List<Food> root = null;
		
		String imageBrowseDefaultFile = this.getServlet().getInitParameter("imageBrowseDefaultFile");
		String imageBrowsePath = this.getServlet().getInitParameter("imageBrowsePath");
		
		try{
			
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantId = request.getParameter("restaurantId");
			String kitchen = request.getParameter("kitchen");
			String alias = request.getParameter("alias");
			String operqtor = request.getParameter("operator");
			String name = request.getParameter("name");
			String pinyin = request.getParameter("pinyin");
			String price = request.getParameter("price");
			String stockStatus = request.getParameter("stockStatus");
			
			String isSpecial = request.getParameter("isSpecial");
			String isRecommend = request.getParameter("isRecommend");
			String isStop = request.getParameter("isStop");
			String isFree = request.getParameter("isFree");
			String isCurrPrice = request.getParameter("isCurrPrice");
			String isCombination = request.getParameter("isCombination");
			String isHot = request.getParameter("isHot");
			String isWeight = request.getParameter("isWeight");
			List<String> statusList = new ArrayList<String>();
			
			String extraCond = "", orderClause = " ORDER BY FOOD.food_alias";
			extraCond += (" AND FOOD.restaurant_id = " + restaurantId);
			//****************  基本条件处理
			if(operqtor != null && !operqtor.trim().isEmpty() && !operqtor.equals("")){
				try{
					switch(Integer.valueOf(operqtor)){
						case 1:
							operqtor = "=";
							break;
						case 2:
							operqtor = ">=";
							break;
						case 3:
							operqtor = "<=";
							break;
						default:
							operqtor = "=";
					}					
				}catch(NumberFormatException e){
					operqtor = "=";
				}
			}
			if(kitchen != null && !kitchen.trim().isEmpty() && !kitchen.equals("")){
				extraCond += (" AND FOOD.kitchen_alias = " + kitchen);
			}
			if(alias != null && !alias.trim().isEmpty() && !alias.equals("")){
				extraCond += (" AND FOOD.food_alias " + operqtor + alias);
			}
			if(name != null && !name.trim().isEmpty() && !name.equals("")){
				extraCond += (" AND FOOD.name like '%" + name.trim() + "%'");
			}
			if(pinyin != null && !pinyin.trim().isEmpty() && !pinyin.equals("")){
				extraCond += (" AND FOOD.pinyin like '" + pinyin.trim() + "%'");
			}
			if(price != null && !price.trim().isEmpty() && !price.equals("")){
				extraCond += (" AND FPP.unit_price " + operqtor + price);
			}
			if(stockStatus != null && !stockStatus.trim().isEmpty() && !stockStatus.equals("")){
				extraCond += (" AND FOOD.stock_status = " + stockStatus);
			}
			//****************  菜品状态处理
			if(isSpecial != null && isSpecial.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.SPECIAL + ") <> 0");
			}
			if(isRecommend != null && isRecommend.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.RECOMMEND + ") <> 0");
			}
			if(isStop != null && isStop.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.SELL_OUT + ") <> 0");
			}
			if(isFree != null && isFree.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.GIFT + ") <> 0");
			}
			if(isCurrPrice != null && isCurrPrice.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.CUR_PRICE + ") <> 0");
			}
			if(isCombination != null && isCombination.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.COMBO + ") <> 0");
			}
			if(isHot != null && isHot.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.HOT + ") <> 0");
			}
			if(isWeight != null && isWeight.trim().equals("true")){
				statusList.add("(FOOD.status & " + Food.WEIGHT + ") <> 0");
			}
			if(!statusList.isEmpty()){
				String strStatus = "";
				for(int i = 0; i < statusList.size(); i++){
					if(i > 0)
						strStatus += " OR ";
					strStatus += statusList.get(i);
				}
				strStatus = (" AND (" + strStatus + ")");
				extraCond += strStatus;
			}
			
			root = FoodDao.getPureFoods(extraCond, orderClause);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				root = DataPaging.getPagingData(root, isPaging, start, limit);
				for(Food temp : root){
					if(temp.hasImage()){
						temp.setImage((imageBrowsePath + "/" + temp.getRestaurantId() + "/" + temp.getImage()));
					}else{
						temp.setImage(imageBrowseDefaultFile);
					}
				}
				jobject.setRoot(root);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
