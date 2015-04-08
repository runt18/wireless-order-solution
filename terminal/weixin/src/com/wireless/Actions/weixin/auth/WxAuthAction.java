package com.wireless.Actions.weixin.auth;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.maker.weixin.auth.AuthorizationInfo;
import org.maker.weixin.auth.AuthorizerInfo;
import org.maker.weixin.auth.AuthorizerToken;
import org.marker.weixin.api.Button;
import org.marker.weixin.api.Menu;
import org.marker.weixin.api.Token;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.wireless.Actions.weixin.WeiXinHandleMessage;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.weixin.restaurant.WeixinRestaurant;

public class WxAuthAction extends Action {
	
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try{
			Restaurant restaurant = RestaurantDao.getById(Integer.parseInt(request.getParameter("rid")));
			
			AuthorizationInfo authorizationInfo = AuthorizationInfo.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, request.getParameter("auth_code"));
			//System.out.println(authorizationInfo);
			
			AuthorizerInfo authorizerInfo = AuthorizerInfo.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, authorizationInfo.getAuthorizerAppId());
			//System.out.println(authorizerInfo);
			
			//Parse the qr code url from image.
            LuminanceSource source = new BufferedImageLuminanceSource(ImageIO.read(new URL(authorizerInfo.getQrCodeUrl())));  
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));  
  
            @SuppressWarnings("serial")
			Result result = new MultiFormatReader().decode(bitmap, new HashMap<DecodeHintType, Object>(){{  
            															put(DecodeHintType.CHARACTER_SET, "GBK");
            											   }});  
            System.out.println(result.getText());
            
			WeixinRestaurantDao.update(StaffDao.getAdminByRestaurant(restaurant.getId()), 
									   new WeixinRestaurant.UpdateBuilder().setWeixinAppId(authorizerInfo.getAppId())
									   									   .setNickName(authorizerInfo.getNickName())
									   									   .setHeadImgUrl(authorizerInfo.getHeadImg())
									   									   .setQrCodeUrl(authorizerInfo.getQrCodeUrl())
									   									   .setRefreshToken(authorizationInfo.getAuthorizerRefreshToken()));
			
			AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, authorizationInfo.getAuthorizerAppId(), authorizationInfo.getAuthorizerRefreshToken());
			//System.out.println(authorizerToken);
			
			//Make the menu.
			Menu menu = new Menu();
			Menu.delete(Token.newInstance(authorizerToken));
			menu.set1stButton(new Button.ClickBuilder("餐厅导航", WeiXinHandleMessage.NAVI_EVENT_KEY).build());
			menu.set2ndButton(new Button.ClickBuilder("最新优惠", WeiXinHandleMessage.PROMOTION_EVENT_KEY).build());
			
			menu.set3rdButton(new Button.ClickBuilder("我的", "AAA")
							.addChild(new Button.ClickBuilder("我的订单", WeiXinHandleMessage.ORDER_EVENT_KEY))
							.addChild(new Button.ClickBuilder("我的会员卡", WeiXinHandleMessage.MEMBER_EVENT_KEY))
							//.addChild(new Button.ClickBuilder("我的大转盘", WeiXinHandleMessage.ZHUAN_EVENT_KEY))
							.build());
			if(menu.create(Token.newInstance(authorizerToken)).isOk()){
				response.getWriter().print("create menu ok");
			}else{
				response.getWriter().print("create menu failed");
			}
			
		}catch(BusinessException | SQLException e){
			response.getWriter().print(e.getMessage());
		}
		
		return null;
	}

}
