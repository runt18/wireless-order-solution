package org.marker.weixin.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import com.wireless.Actions.weixin.WeiXinHandleMessage;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;



/**
 * 自定义菜单最多包括3个一级菜单，每个一级菜单最多包含5个二级菜单。一级菜单最多4个汉字，二级菜单最多7个汉字，多出来的部分将会以“...”代替。请注意，
 * 创建自定义菜单后，由于微信客户端缓存，需要24小时微信客户端才会展现出来。建议测试时可以尝试取消关注公众账号后再次关注，则可以看到创建后的效果。
 * 
 * @author LiYi
 */
public class Menu implements Jsonable{

	private final List<Button> buttons = new ArrayList<Button>(3);

	public Menu(){
		for(int i = 0; i < 3; i++){
			buttons.add(null);
		}
	}
	
	public Status create(Token token) throws ClientProtocolException, IOException{
		return BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/menu/create?access_token=" + token.getAccessToken(), this);
	}
	
	public static Status delete(Token token) throws ClientProtocolException, IOException{
		return JObject.parse(Status.JSON_CREATOR, 0, BaseAPI.doGet(BaseAPI.BASE_URI + "/cgi-bin/menu/delete?access_token=" + token.getAccessToken()));
	}
	
	public static Menu newInstance(Token token) throws ClientProtocolException, IOException{
		return JObject.parse(Menu.JSON_CREATOR, 0, BaseAPI.doGet(BaseAPI.BASE_URI + "/cgi-bin/get_current_selfmenu_info?access_token=" + token.getAccessToken()));
	}
	
	public void set1stButton(Button button){
		buttons.set(0, button);
	}

	public void set2ndButton(Button button){
		buttons.set(1, button);
	}
	
	public void set3rdButton(Button button){
		buttons.set(2, button);
	}

	public List<Button> getButtons(){
		return Collections.unmodifiableList(buttons);
	}
	
	@Override
	public String toString(){
		final String sep = System.getProperty("line.separator");
		StringBuilder menu = new StringBuilder();
		for(Button button : buttons){
			if(button != null){
				menu.append(button).append(sep);
				for(Button sub : button.getChildren()){
					menu.append(" |-").append(sub).append(sep);
				}
			}
		}
		return menu.toString();
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonableList(Button.Key4Json.BUTTON.key, buttons, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		this.buttons.clear();
		this.buttons.addAll(JObject.parseList(Button.JSON_CREATOR, 0, jsonMap.getJSONObject().getJSONObject("selfmenu_info").getJSONArray("button").toString()));
	}
	
	public static Jsonable.Creator<Menu> JSON_CREATOR = new Jsonable.Creator<Menu>() {
		@Override
		public Menu newInstance() {
			return new Menu();
		}
	};
	
	public static void main(String[] args) throws IOException{
		String appId = "wx49b3278a8728ff76";
		String appSecret = "0ba130d87e14a1a37e20c78a2b0ee3ba";
		
		Menu menu = new Menu();
		//Menu.delete(Token.newInstance(appId, appSecret));
		menu.set1stButton(new Button.ClickBuilder("餐厅导航", WeiXinHandleMessage.NAVI_EVENT_KEY).build());
		menu.set2ndButton(new Button.ScanMsgBuilder("扫一扫", WeiXinHandleMessage.SCAN_EVENT_KEY).build());
		
		menu.set3rdButton(new Button.ClickBuilder("我的", "AAA")
						.addChild(new Button.ClickBuilder("优惠活动", WeiXinHandleMessage.PROMOTION_EVENT_KEY))
						.addChild(new Button.ClickBuilder("我的订单", WeiXinHandleMessage.ORDER_EVENT_KEY))
						.addChild(new Button.ClickBuilder("我的会员卡", WeiXinHandleMessage.MEMBER_EVENT_KEY))
						//.addChild(new Button.ClickBuilder("我的大转盘", WeiXinHandleMessage.ZHUAN_EVENT_KEY))
						.build());
//		
//		System.out.println(token.getAccessToken());
		System.out.println(menu.create(Token.newInstance(appId, appSecret)));
		
		System.out.println(Menu.newInstance(Token.newInstance(appId, appSecret)));
	}
}
