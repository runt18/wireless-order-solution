package com.wireless.Actions.login;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.token.TokenDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.token.Token;

public class VerifyRestaurantAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		JObject jobject = new JObject();
		String account = request.getParameter("account");
		String token = request.getParameter("token");
		final Token tokenBean;
		try {
			Restaurant r;
			//FIXME 过渡阶段
			if(token == null || token.trim().isEmpty()){
				r = RestaurantDao.getByAccount(account);
				int tokenId = TokenDao.insert(new Token.InsertBuilder(r));
				tokenBean = TokenDao.getById(tokenId); 
				int code = tokenBean.getCode();
				TokenDao.generate(new Token.GenerateBuilder(r.getAccount(), code));
			}else{
				int tokenId = TokenDao.verify(new Token.VerifyBuilder(account, token));
				tokenBean = TokenDao.getById(tokenId);
				r = tokenBean.getRestaurant();

			}
			
			List<Restaurant> list = new ArrayList<>();
			list.add(r);
			
			jobject.setRoot(list);
			
			jobject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					try {
						jm.putString("token", tokenBean.encrypt());
					} catch (BusinessException e) {
						e.printStackTrace();
					}
					return jm;
				}
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});				
			
		}catch (BusinessException e) {
			jobject.initTip(e);
			e.printStackTrace();
		}catch(SQLException e){
			jobject.initTip(e);
			e.printStackTrace();
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}		
		return null;
	}
}
