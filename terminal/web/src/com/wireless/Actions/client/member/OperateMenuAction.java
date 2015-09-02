package com.wireless.Actions.client.member;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Head.MsgType;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.menuAction.WxMenuAction;
import com.wireless.db.weixin.menuAction.WxMenuAction.Cate;
import com.wireless.db.weixin.menuAction.WxMenuActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;

public class OperateMenuAction extends DispatchAction{
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
			
			if(image == null || image.isEmpty()){
				image = "";
			}
			
			final Cate cate;
			if(subscribe != null && !subscribe.isEmpty()){
				cate = Cate.SUBSCRIBE_REPLY;
			}else{
				cate = Cate.NORMAL;
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
		
		try{
			final Staff staff = StaffDao.getAdminByRestaurant(Integer.parseInt(rid));
			WxMenuAction.UpdateBuilder4Text update4Text = new WxMenuAction.UpdateBuilder4Text(Integer.parseInt(key), text);
			if(subscribe != null && !subscribe.isEmpty()){
				update4Text.setCate(WxMenuAction.Cate.SUBSCRIBE_REPLY);
			}else{
				update4Text.setCate(WxMenuAction.Cate.NORMAL);
			}
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
