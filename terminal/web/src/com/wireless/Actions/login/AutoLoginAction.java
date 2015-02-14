package com.wireless.Actions.login;

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

public class AutoLoginAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception {
				
		JObject jobject = new JObject();
		try{
			final Restaurant r = RestaurantDao.getById(40);
			int tokenId = TokenDao.insert(new Token.InsertBuilder(r));
			Token tokenBean = TokenDao.getById(tokenId); 
			int code = tokenBean.getCode();
			tokenId = TokenDao.generate(new Token.GenerateBuilder(r.getAccount(), code));			
			final Token token2Save = TokenDao.getById(tokenId); 
			
			jobject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					try {
						jm.putString("token", token2Save.encrypt());
						jm.putString("account", r.getAccount());
						jm.putInt("pin", 29);
						jm.putString("password", "1");
					} catch (BusinessException e) {
						e.printStackTrace();
					}
					return jm;
				}
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});				
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
