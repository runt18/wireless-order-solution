package com.wireless.Actions.login;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.token.TokenDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.token.Token;

public class AutoLoginAction extends Action{
	
	private final static String DEMO_ACCOUNT = "liyy";
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception {
				
		JObject jobject = new JObject();
		try{
			final Restaurant restaurant = RestaurantDao.getByAccount(DEMO_ACCOUNT);
			final Staff admin = StaffDao.getAdminByRestaurant(restaurant.getId());
			List<Token> result = TokenDao.getByCond(new TokenDao.ExtraCond().addStatus(Token.Status.TOKEN));
			int tokenId;
			if(result.isEmpty()){
				tokenId = TokenDao.insert(new Token.InsertBuilder(restaurant));
				tokenId = TokenDao.generate(new Token.GenerateBuilder(restaurant.getAccount(), TokenDao.getById(tokenId).getCode()));			
			}else{
				tokenId = result.get(0).getId();
			}
			final Token token = TokenDao.getById(tokenId); 
			
			jobject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					try {
						jm.putString("token", token.encrypt());
						jm.putString("account", restaurant.getAccount());
						jm.putInt("pin", admin.getId());
						jm.putString("password", admin.getPwd());
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
