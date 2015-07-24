package com.wireless.Actions.weixin.operate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;

import com.wireless.Actions.weixin.WeiXinHandleMessage;
import com.wireless.Actions.weixin.WeiXinHandleMessage.EventKey;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.menuAction.WxMenuAction;
import com.wireless.db.weixin.menuAction.WxMenuAction.Cate;
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
		
		String appId = "wx49b3278a8728ff76";
		String appSecret = "0ba130d87e14a1a37e20c78a2b0ee3ba";
		System.out.println(Menu.newInstance(Token.newInstance(appId, appSecret)));
		jobject.setRoot(Menu.newInstance(Token.newInstance(appId, appSecret)));
		
//		int rid = Integer.parseInt(request.getParameter("rid"));
//		WxRestaurant wxRestaurant = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(rid));
//		AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, wxRestaurant.getWeixinAppId(), wxRestaurant.getRefreshToken());
//		jobject.setRoot(Menu.newInstance(Token.newInstance(authorizerToken)));
		
		response.getWriter().print(jobject.toString());
		
		return null;
	}			
	/**
	 * 获取系统保留的menu选项
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward systemMenu(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		JObject jobject = new JObject(); 

		List<Jsonable> list = new ArrayList<>();
		for(final EventKey key : WeiXinHandleMessage.EventKey.values()){
			Jsonable j = new Jsonable() {
				
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("key", key.getKey());
					jm.putString("desc", key.toString());
					return jm;
				}
				
				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					
				}
			};	
			
			list.add(j);
		}
		jobject.setRoot(list);
		
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
	
	
	/**
	 * 保存单图文信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insertImageText(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String title = request.getParameter("title");
		String image = request.getParameter("image");
		String content = request.getParameter("content");
		String url = request.getParameter("url");
		String rid = request.getParameter("rid");
		String subItems = request.getParameter("subItems");
		String subscribe = request.getParameter("subscribe");
		JObject jobject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction.InsertBuilder4ImageText insert4ImageText;
			Cate cate = Cate.NORMAL;
			
			if(image == null || image.isEmpty()){
				image = "";
			}
			if(subscribe != null && !subscribe.isEmpty()){
				cate = Cate.SUBSCRIBE_REPLY;
			}
			
			insert4ImageText = new WxMenuAction.InsertBuilder4ImageText(new Data4Item(title, content, image, url), cate);
			if(subItems != null && !subItems.isEmpty()){
				String[] subItemArry = subItems.split("<ul>");
				for (String s : subItemArry) {
					String[] subItem = s.split("<li>");
					String sub_ossImage = subItem[2];
					if(subItem[2].equals("-1")){
						sub_ossImage = "";
					}
					insert4ImageText.addItem(new Data4Item(subItem[0], "", sub_ossImage, subItem[1]));
				}
				
			}

			final int actionId = WxMenuActionDao.insert(staff, insert4ImageText);
			jobject.initTip(true, "添加成功");
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
	
	
	public ActionForward updateImageText(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String title = request.getParameter("title");
		String image = request.getParameter("image");
		String content = request.getParameter("content");
		String url = request.getParameter("url");
		String key = request.getParameter("key");
		String rid = request.getParameter("rid");
		String subItems = request.getParameter("subItems");
		String subscribe = request.getParameter("subscribe");
		JObject jobject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction.UpdateBuilder4ImageText update4ImageText;
			Cate cate = Cate.NORMAL;
			
			if(image == null || image.isEmpty()){
				image = "";
			}
			if(subscribe != null && !subscribe.isEmpty()){
				cate = Cate.SUBSCRIBE_REPLY;
			}
			
			update4ImageText = new WxMenuAction.UpdateBuilder4ImageText(Integer.parseInt(key), new Data4Item(title, content, image, url));
			update4ImageText.setCate(cate);
			if(subItems != null && !subItems.isEmpty()){
				String[] subItemArry = subItems.split("<ul>");
				for (String s : subItemArry) {
					String[] subItem = s.split("<li>");
					String sub_ossImage = subItem[2];
					if(subItem[2].equals("-1")){
						sub_ossImage = "";
					}
					update4ImageText.addItem(new Data4Item(subItem[0], "", sub_ossImage, subItem[1]));
				}
				
			}

			WxMenuActionDao.update(staff, update4ImageText);
			jobject.initTip(true, "修改成功");
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
		String subscribe = request.getParameter("subscribe");
		JObject jobject = new JObject(); 
		Cate cate = Cate.NORMAL;
		
		if(subscribe != null && !subscribe.isEmpty()){
			cate = Cate.SUBSCRIBE_REPLY;
		}
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction.InsertBuilder4Text insert4Text = new WxMenuAction.InsertBuilder4Text(text, cate);
			final int actionId = WxMenuActionDao.insert(staff, insert4Text);
			jobject.initTip(true, "添加成功");
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
	 * 修改文字
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateMenu(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String text = request.getParameter("text");
		String rid = request.getParameter("rid");
		String key = request.getParameter("key");
		String subscribe = request.getParameter("subscribe");
		JObject jobject = new JObject(); 
		Cate cate = Cate.NORMAL;
		
		if(subscribe != null && !subscribe.isEmpty()){
			cate = Cate.SUBSCRIBE_REPLY;
		}
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction.UpdateBuilder4Text update4Text = new WxMenuAction.UpdateBuilder4Text(Integer.parseInt(key), text);
			update4Text.setCate(cate);
			WxMenuActionDao.update(staff, update4Text);
			jobject.initTip(true, "修改成功");
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
	 * 根据key获取回复内容
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
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
			}else if(msg.getHead().getMsgType() == MsgType.MSG_TYPE_IMAGE_TEXT){
				jobject.setRoot(((Msg4ImageText)msg).getItems());
			}
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}		
	
	/**
	 * 获取自动关注回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward subscribeReply(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String rid = request.getParameter("rid");
		JObject jobject = new JObject(); 
		final int key;
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			List<WxMenuAction> wxMenus = WxMenuActionDao.getByCond(staff, new WxMenuActionDao.ExtraCond().setCate(Cate.SUBSCRIBE_REPLY));
			Msg msg = null;
			if(!wxMenus.isEmpty()){
				key = wxMenus.get(0).getId();
				msg = new WxMenuAction.MsgProxy(wxMenus.get(0)).toMsg();
			}else{
				key = -1;
			}
			
			if(msg != null){
				if(msg.getHead().getMsgType() == MsgType.MSG_TYPE_TEXT){
					final String text = ((Msg4Text)msg).getContent();
					jobject.setExtra(new Jsonable() {
						
						@Override
						public JsonMap toJsonMap(int flag) {
							JsonMap jm = new JsonMap();
							jm.putString("text", text);
							jm.putInt("key", key);
							return jm;
						}
						
						@Override
						public void fromJsonMap(JsonMap jm, int flag) {
							
						}
					});
				}else if(msg.getHead().getMsgType() == MsgType.MSG_TYPE_IMAGE_TEXT){
					jobject.setRoot(((Msg4ImageText)msg).getItems());
					jobject.setExtra(new Jsonable() {
						
						@Override
						public JsonMap toJsonMap(int flag) {
							JsonMap jm = new JsonMap();
							jm.putInt("key", key);
							return jm;
						}
						
						@Override
						public void fromJsonMap(JsonMap jm, int flag) {
							
						}
					});
				}
			}else{
				jobject.initTip(false, "");
			}
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}	
	
	/**
	 * 删除自动回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward deleteSubscribe(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String rid = request.getParameter("rid");
		JObject jobject = new JObject(); 
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			List<WxMenuAction> wxMenus = WxMenuActionDao.getByCond(staff, new WxMenuActionDao.ExtraCond().setCate(Cate.SUBSCRIBE_REPLY));
			if(!wxMenus.isEmpty()){
				WxMenuActionDao.deleteById(staff, wxMenus.get(0).getId());
				jobject.initTip(true, "删除成功");
			}else{
				jobject.initTip(false, "还没设置自动回复");
			}
			
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
