package com.wireless.Actions.restaurantMgr;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.sms.SMStatDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.token.TokenDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.sms.SMStat;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.token.Token;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.util.DataPaging;

public class QueryRestaurantAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String isPaging = request.getParameter("isPaging");
		final String id = request.getParameter("id");
		final String name = request.getParameter("name");
		final String fuzzy = request.getParameter("fuzzy");
		final String account = request.getParameter("account");
		final String expireDate = request.getParameter("expireDate");
		final String alive = request.getParameter("alive");
		final String byId = request.getParameter("byId");
		final JObject jObject = new JObject();
		
		try{

			if(byId != null && !byId.isEmpty() && Boolean.parseBoolean(byId)){
				jObject.setRoot(RestaurantDao.getById(Integer.parseInt((String) request.getAttribute("restaurantID"))));
				
			}else{
				final RestaurantDao.ExtraCond extraCond = new RestaurantDao.ExtraCond();
				if(id != null && !id.isEmpty()){
					extraCond.setId(Integer.parseInt(id));
				}
				if(name != null && !name.trim().isEmpty()){
					extraCond.setFuzzy(name);
				}
				if(account != null && !account.trim().isEmpty()){
					extraCond.setAccount(account);
				}
				if(fuzzy != null && !fuzzy.isEmpty()){
					extraCond.setFuzzy(fuzzy);
				}
				
				final String orderClause;
				if((expireDate != null && !expireDate.isEmpty()) || (alive != null && !alive.isEmpty())){
					if(alive == null || alive.isEmpty()){
						orderClause = (" ORDER BY expire_date" );
					}else if(expireDate == null || expireDate.isEmpty()){
						orderClause = (" ORDER BY liveness" );
					}else if(expireDate != null && alive != null){
						orderClause = (" ORDER BY expire_date, liveness" );
					}else{
						orderClause = " ORDER BY id";
					}
				}else{
					orderClause = " ORDER BY id";
				}
				final List<Restaurant> list = RestaurantDao.getByCond(extraCond, orderClause);
				List<Jsonable> result = new ArrayList<Jsonable>();
				if(!list.isEmpty()){
					for(final Restaurant rest : list) {
						
						Staff adminStaff = StaffDao.getAdminByRestaurant(rest.getId()); 
						//为restaurant加上短信条数
						final SMStat sms = SMStatDao.get(adminStaff);
						
						final WxRestaurant wxRest = WxRestaurantDao.get(adminStaff);
						
						//为restaurant加上验证码个数
						int used = 0, unUsed = 0;
						List<Token> tokens = TokenDao.getByCond(new TokenDao.ExtraCond().setRestaurant(rest.getId()));
						for (Token token : tokens) {
							if(token.getStatus() == Token.Status.TOKEN){
								used ++;
							}else if(token.getStatus() == Token.Status.DYN_CODE && !token.isCodeExpired()){
								unUsed ++;
							}
						}
						final int usedCode = used, unUsedCode = unUsed;

						result.add(new Jsonable() {
							@Override
							public JsonMap toJsonMap(int flag) {
								JsonMap jm = new JsonMap();
								jm.putJsonable(rest, 0);
								jm.putInt("smsRemain", sms.getRemaining());
								jm.putInt("usedCode", usedCode);
								jm.putInt("unUsedCode", unUsedCode);
								jm.putBoolean("qrCode", wxRest.hasQrCode());
								return jm;
							}
							
							@Override
							public void fromJsonMap(JsonMap jsonMap, int flag) {
								
							}
						});
						
					}
					jObject.setTotalProperty(list.size());
					result = DataPaging.getPagingData(result, isPaging, start, limit);
				}
				jObject.setRoot(result);
			}
			
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
		
	}
}
