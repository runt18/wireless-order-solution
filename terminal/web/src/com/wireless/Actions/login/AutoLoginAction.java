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
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.token.Token;

public class AutoLoginAction extends Action{
	
	private final static String DEMO_ACCOUNT = "liyy";
	
	private static int DEMO_TOKEN_ID = 1;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception {
				
		JObject jobject = new JObject();
		try{
			final Restaurant restaurant = RestaurantDao.getByAccount(DEMO_ACCOUNT);
			final Staff admin = StaffDao.getAdminByRestaurant(restaurant.getId());
			final String encryptedToken;
			List<Token> result = TokenDao.getByCond(new TokenDao.ExtraCond().setRestaurant(restaurant).setId(DEMO_TOKEN_ID).addStatus(Token.Status.TOKEN));
			if(result.isEmpty()){
				DEMO_TOKEN_ID = TokenDao.insert(new Token.InsertBuilder(restaurant));
				encryptedToken = TokenDao.generate(new Token.GenerateBuilder(restaurant.getAccount(), TokenDao.getById(DEMO_TOKEN_ID).getCode()));			
			}else{
				DEMO_TOKEN_ID = result.get(0).getId();
				encryptedToken = TokenDao.getById(result.get(0).getId()).encrypt();
			}
			
			jobject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("token", encryptedToken);
					jm.putString("account", restaurant.getAccount());
					jm.putInt("pin", admin.getId());
					jm.putString("password", admin.getPwd());
					return jm;
				}
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			});				
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
