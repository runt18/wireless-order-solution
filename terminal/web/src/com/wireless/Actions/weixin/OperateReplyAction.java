package com.wireless.Actions.weixin;

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
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.msg.Msg4Head.MsgType;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.action.WxMenuActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.action.WxMenuAction;
import com.wireless.pojo.weixin.action.WxMenuAction.Cate;

public class OperateReplyAction extends DispatchAction {
	/**
	 * 新建图文回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insertImageText(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String title = request.getParameter("title");
		final String image = request.getParameter("image");
		final String content = request.getParameter("content");
		final String url = request.getParameter("url");
		final String pin = (String)request.getAttribute("pin");
		final String subItems = request.getParameter("subItems");
		final String subscribe = request.getParameter("subscribe");
		final String callback = request.getParameter("callback");
		JObject jObject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final Cate cate;
			if(subscribe != null && !subscribe.isEmpty() && Boolean.parseBoolean(subscribe)){
				cate = WxMenuAction.Cate.SUBSCRIBE_REPLY;
			}else{
				cate = WxMenuAction.Cate.NORMAL;
			}
			
			final WxMenuAction.InsertBuilder4ImageText builder = new WxMenuAction.InsertBuilder4ImageText(new Data4Item(title, content, image, url), cate);
			if(subItems != null && !subItems.isEmpty()){
				String[] subItemArry = subItems.split("<ul>");
				for (String s : subItemArry) {
					String[] subItem = s.split("<li>");
					String subOssImage = subItem[2];
					if(subItem[2].equals("-1")){
						subOssImage = "";
					}
					builder.addItem(new Data4Item(subItem[0], "", subOssImage, subItem[1]));
				}
				
			}

			final int actionId = WxMenuActionDao.insert(staff, builder);
			jObject.initTip(true, "添加成功");
			jObject.setExtra(new Jsonable() {
				
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
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		
		return null;
	}	
	
	/**
	 * 修改图文回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateImageText(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String title = request.getParameter("title");
		final String image = request.getParameter("image");
		final String content = request.getParameter("content");
		final String url = request.getParameter("url");
		final String key = request.getParameter("key");
		final String pin = (String)request.getAttribute("pin");
		final String subItems = request.getParameter("subItems");
		final String subscribe = request.getParameter("subscribe");
		final String callback = request.getParameter("callback");
		JObject jObject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final WxMenuAction.UpdateBuilder4ImageText builder = new WxMenuAction.UpdateBuilder4ImageText(Integer.parseInt(key), new Data4Item(title, content, image, url));
			if(subscribe != null && !subscribe.isEmpty() && Boolean.parseBoolean(subscribe)){
				builder.setCate(Cate.SUBSCRIBE_REPLY);
			}else{
				builder.setCate(Cate.NORMAL);
			}

			if(subItems != null && !subItems.isEmpty()){
				String[] subItemArry = subItems.split("<ul>");
				for (String s : subItemArry) {
					String[] subItem = s.split("<li>");
					String sub_ossImage = subItem[2];
					if(subItem[2].equals("-1")){
						sub_ossImage = "";
					}
					builder.addItem(new Data4Item(subItem[0], "", sub_ossImage, subItem[1]));
				}
				
			}

			WxMenuActionDao.update(staff, builder);
			jObject.initTip(true, "修改成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		
		return null;
	}	
	
	/**
	 * 增加图文回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward insertText(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String text = request.getParameter("text");
		final String pin = (String)request.getAttribute("pin");
		final String subscribe = request.getParameter("subscribe");
		final String callback = request.getParameter("callback");
		JObject jObject = new JObject();
		
		final WxMenuAction.Cate cate;
		if(subscribe != null && !subscribe.isEmpty() && Boolean.parseBoolean(subscribe)){
			cate = WxMenuAction.Cate.SUBSCRIBE_REPLY;
		}else{
			cate = WxMenuAction.Cate.NORMAL;
		}
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final WxMenuAction.InsertBuilder4Text insert4Text = new WxMenuAction.InsertBuilder4Text(text, cate);
			final int actionId = WxMenuActionDao.insert(staff, insert4Text);
			jObject.initTip(true, "添加成功");
			jObject.setExtra(new Jsonable() {
				
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
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		
		return null;
	}
	/**
	 * 修改文字回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateText(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String text = request.getParameter("text");
		final String key = request.getParameter("key");
		final String subscribe = request.getParameter("subscribe");
		final String callback = request.getParameter("callback");
		final JObject jObject = new JObject(); 
		final String pin = (String)request.getAttribute("pin");
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final WxMenuAction.UpdateBuilder4Text update4Text = new WxMenuAction.UpdateBuilder4Text(Integer.parseInt(key), text);
			if(subscribe != null && !subscribe.isEmpty() && Boolean.parseBoolean(subscribe)){
				update4Text.setCate(WxMenuAction.Cate.SUBSCRIBE_REPLY);
			}else{
				update4Text.setCate(WxMenuAction.Cate.NORMAL);
			}
			WxMenuActionDao.update(staff, update4Text);
			jObject.initTip(true, "修改成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
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
		final String key = request.getParameter("key");
		final String pin = (String)request.getAttribute("pin");
		final String callback = request.getParameter("callback");
		final JObject jObject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			final WxMenuAction wxMenu = WxMenuActionDao.getById(staff, Integer.parseInt(key));
			
			Msg msg = new WxMenuAction.MsgProxy(wxMenu).toMsg();
			if(msg.getHead().getMsgType() == MsgType.MSG_TYPE_TEXT){
				final String text = ((Msg4Text)msg).getContent();
				jObject.setExtra(new Jsonable() {
					
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
				jObject.setRoot(((Msg4ImageText)msg).getItems());
			}
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}

		return null;
	}
	
	/**
	 * 删除特定的回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String key = request.getParameter("key");
		final String pin = (String)request.getAttribute("pin");
		final String callback = request.getParameter("callback");
		JObject jobject = new JObject(); 
		
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			WxMenuActionDao.deleteById(staff, Integer.parseInt(key));
			jobject.initTip(true, "删除成功");
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jobject.toString() + ")");
			}else{
				response.getWriter().print(jobject.toString());
			}
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
		final String pin = (String)request.getAttribute("pin");
		final String callback = request.getParameter("callback");
		
		JObject jobject = new JObject(); 
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<WxMenuAction> wxMenus = WxMenuActionDao.getByCond(staff, new WxMenuActionDao.ExtraCond().setCate(Cate.SUBSCRIBE_REPLY));
			final int key;
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
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jobject.toString() + ")");
			}else{
				response.getWriter().print(jobject.toString());
			}
		}

		return null;
	}
	
	/**
	 * 删除关注回复
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward deleteSubscribe(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final String callback = request.getParameter("callback");
		final JObject jObject = new JObject(); 
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(WxMenuActionDao.deleteByCond(staff,  new WxMenuActionDao.ExtraCond().setCate(Cate.SUBSCRIBE_REPLY)) != 0){
				jObject.initTip(true, "删除成功");
			}else{
				jObject.initTip(false, "还没设置自动回复");
			}
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			if(callback != null && !callback.isEmpty()){
				response.getWriter().print(callback + "(" + jObject.toString() + ")");
			}else{
				response.getWriter().print(jObject.toString());
			}
		}
		
		return null;
	}
}
