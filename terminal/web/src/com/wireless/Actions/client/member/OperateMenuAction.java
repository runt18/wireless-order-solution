package com.wireless.Actions.client.member;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.api.Menu;
import org.marker.weixin.api.Token;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;

import com.wireless.db.oss.OssImageDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.menuAction.WxMenuAction;
import com.wireless.db.weixin.menuAction.WxMenuActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.staffMgr.Staff;

public class OperateMenuAction extends DispatchAction{
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
		JObject jobject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction.UpdateBuilder4Text update4Text = new WxMenuAction.UpdateBuilder4Text(Integer.parseInt(key), text);
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
		JObject jobject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction.InsertBuilder4ImageText insert4ImageText;
			if(image != null && !image.isEmpty()){
				OssImage ossImage = OssImageDao.getById(staff, Integer.parseInt(image));
				insert4ImageText = new WxMenuAction.InsertBuilder4ImageText(new Data4Item(title, content, ossImage.getObjectUrl(), url));				
			}else{
				insert4ImageText = new WxMenuAction.InsertBuilder4ImageText(new Data4Item(title, content, "", url));	
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
		JObject jobject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction.UpdateBuilder4ImageText update4ImageText;
			if(image != null && !image.isEmpty()){
				OssImage ossImage = OssImageDao.getById(staff, Integer.parseInt(image));
				update4ImageText = new WxMenuAction.UpdateBuilder4ImageText(Integer.parseInt(key), new Data4Item(title, content, ossImage.getObjectUrl(), url));				
			}else{
				update4ImageText = new WxMenuAction.UpdateBuilder4ImageText(Integer.parseInt(key), new Data4Item(title, content, "", url));	
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
	 * 删除menu
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward deleteMenu(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		String key = request.getParameter("key");
		String rid = request.getParameter("rid");
		JObject jobject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			
			WxMenuActionDao.deleteById(staff, Integer.parseInt(key));
			jobject.initTip(true, "删除成功");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}

		return null;
	}		
	
	

}
