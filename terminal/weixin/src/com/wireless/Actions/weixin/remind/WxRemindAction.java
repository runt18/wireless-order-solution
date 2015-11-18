package com.wireless.Actions.weixin.remind;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.api.Status;
import org.marker.weixin.api.Template;
import org.marker.weixin.api.Template.Keyword;
import org.marker.weixin.api.Token;

import com.wireless.Actions.weixin.finance.FinanceWeixinAction;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class WxRemindAction extends DispatchAction {

	private final static float LIVENESS_UPPER_LIMIT = 0.9f;
	private final static float LIVENESS_LOWER_LIMIT = 0f;
	
	private final static String OPEN_ID_4_MARCO = "odgTwtwjcJPhFm9xhNNnds7bNkNc";
	private final static String OPEN_ID_4_VINCENT = "odgTwt4tqd_mu-q9EY02rqHrp_M0";
	
	public ActionForward liveness(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		Token token = Token.newInstance(FinanceWeixinAction.APP_ID, FinanceWeixinAction.APP_SECRET);
		
		final StringBuilder work = new StringBuilder();
		for(Restaurant restaurant : RestaurantDao.getByCond(null, null)){
			if(restaurant.getLiveness() > LIVENESS_LOWER_LIMIT && restaurant.getLiveness() <= LIVENESS_UPPER_LIMIT){
				if(work.length() != 0){
					work.append(", ");
				}
				work.append(restaurant.getName() + "(" + restaurant.getLiveness() + ")");
			}
		}
		
		if(work.length() != 0){
			/**
			 * {{first.DATA}}
			 * 待办工作：{{work.DATA}}
			 * {{remark.DATA}}
			 */
			Status status = Template.send(token, new Template.Builder().setToUser(OPEN_ID_4_MARCO)
						.setTemplateId("UdyaL-jQJjC5aUh8A3VdeOrzkm2DQDZpUvny8kZ1kZ0")
						.addKeyword(new Keyword("first", "餐厅活跃度异常")).addKeyword(new Keyword("work", work.toString())));
			
			status = Template.send(token, new Template.Builder().setToUser(OPEN_ID_4_VINCENT)
					  						.setTemplateId("UdyaL-jQJjC5aUh8A3VdeOrzkm2DQDZpUvny8kZ1kZ0")
					  						.addKeyword(new Keyword("first", "餐厅活跃度异常")).addKeyword(new Keyword("work", work.toString())));
			
			response.getWriter().write(status.toString());
		}
		return null;
	}
	
	private final static long FOUR_WEEKS = 3600 * 24 * 30 * 1000;
	
	public ActionForward expired(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

		Token token = Token.newInstance(FinanceWeixinAction.APP_ID, FinanceWeixinAction.APP_SECRET);
		
		final StringBuilder work = new StringBuilder();
		for(Restaurant restaurant : RestaurantDao.getByCond(null, null)){
			long remaining = restaurant.getExpireDate() - System.currentTimeMillis();
					
			if(Math.abs(remaining) < FOUR_WEEKS && remaining > 0){
				if(work.length() != 0){
					work.append(", ");
				}
				work.append(restaurant.getName() + "(" + restaurant.getLiveness() + ")");
			}
		}
		
		if(work.length() != 0){
			/**
			 * {{first.DATA}}
			 * 待办工作：{{work.DATA}}
			 * {{remark.DATA}}
			 */
			Status status = Template.send(token, new Template.Builder().setToUser(OPEN_ID_4_MARCO)
						.setTemplateId("UdyaL-jQJjC5aUh8A3VdeOrzkm2DQDZpUvny8kZ1kZ0")
						.addKeyword(new Keyword("first", "1月内餐厅到期提醒")).addKeyword(new Keyword("work", work.toString())));

			status = Template.send(token, new Template.Builder().setToUser(OPEN_ID_4_VINCENT)
					  						.setTemplateId("UdyaL-jQJjC5aUh8A3VdeOrzkm2DQDZpUvny8kZ1kZ0")
					  						.addKeyword(new Keyword("first", "1月内餐厅到期提醒")).addKeyword(new Keyword("work", work.toString())));
			
			response.getWriter().write(status.toString());
		}
		return null;
	}
}
