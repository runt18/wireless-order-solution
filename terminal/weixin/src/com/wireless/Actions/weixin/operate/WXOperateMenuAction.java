package com.wireless.Actions.weixin.operate;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.api.Menu;
import org.marker.weixin.api.Token;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.AuthorizerToken;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4Text;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.menuAction.WxMenuAction;
import com.wireless.db.weixin.menuAction.WxMenuActionDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;

public class WXOperateMenuAction extends DispatchAction {
	
	/**
	 * 添加菜单对应key
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insertMenu(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String text = request.getParameter("text");
		String rid = request.getParameter("rid");
		JObject jobject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction.InsertBuilder4Text insert4Text = new WxMenuAction.InsertBuilder4Text(text);
			final int actionId = WxMenuActionDao.insert(staff, insert4Text);
			
			jobject.setExtra(new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putInt("key", actionId);
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					
				}
			});
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	

	/**
	 * 获取微信菜单
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward weixinMenu(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject(); 
		int rid = Integer.parseInt(request.getParameter("rid"));
		
//		String appId = "wx49b3278a8728ff76";
//		String appSecret = "0ba130d87e14a1a37e20c78a2b0ee3ba";
//		System.out.println(Menu.newInstance(Token.newInstance(appId, appSecret)));
		
		WxRestaurant wxRestaurant = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(rid));
		AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken());
		jobject.setRoot(Menu.newInstance(Token.newInstance(authorizerToken)));
		
		response.getWriter().print(jobject.toString());
		
		return null;
	}
	
	/**
	 * 提交菜单
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward commitMenu(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String menu = request.getParameter("menu");
		int rid = Integer.parseInt(request.getParameter("rid"));
		//String appId = "wx49b3278a8728ff76";
		//String appSecret = "0ba130d87e14a1a37e20c78a2b0ee3ba";
		
		JObject jobject = new JObject(); 
		
		try{
			Menu weixinMenu = JObject.parse(Menu.JSON_CREATOR, 0, menu);
			WxRestaurant wxRestaurant = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(rid));
			AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken());
			System.out.println(weixinMenu.create(Token.newInstance(authorizerToken)));			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}	
	
	public ActionForward menuReply(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String key = request.getParameter("key");
		String rid = request.getParameter("rid");
		JObject jobject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction wxMenu = WxMenuActionDao.getById(staff, Integer.parseInt(key));
			
			Msg msg = new WxMenuAction.MsgProxy(wxMenu).toMsg();
			if(msg.getHead().getMsgType() == MsgType.MSG_TYPE_TEXT){
				System.out.println(((Msg4Text)msg).getContent());
				final String text = ((Msg4Text)msg).getContent();
				jobject.setExtra(new Jsonable() {
					
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putString("text", text);
						return jm;
					}
					
					@Override
					public void fromJsonMap(JsonMap jm, int flag) {
						
					}
				});
			}
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}	
	

	public static void main(String[] args) throws IOException{
		String appId = "wx49b3278a8728ff76";
		String appSecret = "0ba130d87e14a1a37e20c78a2b0ee3ba";
		
//		Menu menu = new Menu();
//		//Menu.delete(Token.newInstance(appId, appSecret));
//		menu.set1stButton(new Button.ClickBuilder("餐厅导航", WeiXinHandleMessage.NAVI_EVENT_KEY).build());
//		menu.set2ndButton(new Button.ScanMsgBuilder("扫一扫", WeiXinHandleMessage.SCAN_EVENT_KEY).build());
//		
//		menu.set3rdButton(new Button.ClickBuilder("我的", "AAA")
//						.addChild(new Button.ClickBuilder("优惠活动", WeiXinHandleMessage.PROMOTION_EVENT_KEY))
//						.addChild(new Button.ClickBuilder("我的订单", WeiXinHandleMessage.ORDER_EVENT_KEY))
//						.addChild(new Button.ClickBuilder("我的会员卡", WeiXinHandleMessage.MEMBER_EVENT_KEY))
//						//.addChild(new Button.ClickBuilder("我的大转盘", WeiXinHandleMessage.ZHUAN_EVENT_KEY))
//						.build());
//		
//		System.out.println(token.getAccessToken());
//		System.out.println(menu.create(Token.newInstance(appId, appSecret)));
		
		System.out.println(Menu.newInstance(Token.newInstance(appId, appSecret)));
	}
}
