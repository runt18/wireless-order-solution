package com.wireless.Actions.weixin;

import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.marker.weixin.DefaultSession;
import org.marker.weixin.HandleMessageAdapter;
import org.marker.weixin.msg.Data4Item;
import org.marker.weixin.msg.Msg4ImageText;
import org.marker.weixin.msg.Msg4Text;

public class EntryAction extends Action{
	
	static final String WEIXIN_TOKEN = "qwe123";
	static final String WEIXIN_ADDR = "http://42.121.54.177/web-term/weixin/order/index.html";
	static final String WEIXIN_ABOUT = "http://42.121.54.177/web-term/weixin/order/about.html";
	static final String WEIXIN_SALES = "http://42.121.54.177/web-term/weixin/order/sales.html";
	
	static final String COMMAND_HELP = "h";
	static final String COMMAND_MENU = "m";
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		// TODO
		// 检查该餐厅是否已激活
		// ..............
		
		InputStream is = request.getInputStream();
		OutputStream os = response.getOutputStream();
	
		final DefaultSession session = DefaultSession.newInstance();
		final String rid = request.getParameter("rid");
		
		session.addOnHandleMessageListener(new HandleMessageAdapter(){
			
			@Override
			public void onTextMsg(Msg4Text msg) {
				System.out.println("微信收到消息：" + msg.getContent());
				System.out.println("openID: " + msg.getToUserName());
				System.out.println("开发者微信号: " + msg.getFromUserName());
				
				msg.setContent(msg.getContent().trim().toLowerCase());
				if("1".equals(msg.getContent()) || COMMAND_MENU.equals(msg.getContent())){
					Data4Item d2 = new Data4Item("logo", "最新促销信息", "http://42.121.54.177/web-term/weixin/title-image.jpg", WEIXIN_ADDR); 
					
					Data4Item main = new Data4Item();
					main.setTitle("点菜");
					main.setUrl(WEIXIN_ADDR);
					Data4Item baidu = new Data4Item();
					baidu.setTitle("促销信息");
					baidu.setUrl(WEIXIN_SALES+"?rid="+rid);
					Data4Item google = new Data4Item();
					google.setTitle("餐厅简介");
					google.setUrl(WEIXIN_ABOUT+"?rid="+rid);
					
					Msg4ImageText mit = new Msg4ImageText();
					mit.setFromUserName(msg.getToUserName());
					mit.setToUserName(msg.getFromUserName()); 
					mit.setCreateTime(msg.getCreateTime());
					mit.addItem(d2);
					mit.addItem(main);
					mit.addItem(baidu);
					mit.addItem(google);
					
					session.callback(mit);
				}else if(COMMAND_HELP.equals(msg.getContent())){
					Msg4Text rmsg =	new Msg4Text();
					rmsg.setFromUserName(msg.getToUserName());
					rmsg.setToUserName(msg.getFromUserName());
					rmsg.setContent("餐厅编号:"+rid+"\nToUserName(openID):"+msg.getToUserName()+"\nFromUserName(开发者微信号):"+msg.getFromUserName());
					session.callback(rmsg);
				}else{
					Msg4Text rmsg =	new Msg4Text();
					rmsg.setFromUserName(msg.getToUserName());
					rmsg.setToUserName(msg.getFromUserName());
					rmsg.setContent(new StringBuilder().append("未知命令\n")
							.append("输入【h】获取帮助信息\n")
							.append("输入【m】获得主菜单")
							.toString());
					session.callback(rmsg);
				}
				
			}
		});
		
		session.process(is, os);
		session.close();
		
		return null;
	}
	
/*
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		System.out.println("WEIXIN_TOKEN: " + WEIXIN_TOKEN);
		String signature = request.getParameter("signature");	// 微信加密签名
		String timestamp = request.getParameter("timestamp");	// 时间戳
		String nonce = request.getParameter("nonce");			// 随机数
		String echostr = request.getParameter("echostr");		// 随机字符串

		List<String> list = new ArrayList<String>(3) {
			private static final long serialVersionUID = 2621444383666420433L;
			public String toString() {
				return this.get(0) + this.get(1) + this.get(2);
			}
		};
		list.add(WEIXIN_TOKEN);
		list.add(timestamp);
		list.add(nonce);
		
		Collections.sort(list);
		
		String tmpStr = new MySecurity().encode(list.toString(), MySecurity.SHA_1);
		Writer out = response.getWriter();
		if (signature.equals(tmpStr)) {
			out.write(echostr);
		} else {
			out.write("");
		}
		out.flush();
		out.close();
		
		return null;
	}
*/
}
