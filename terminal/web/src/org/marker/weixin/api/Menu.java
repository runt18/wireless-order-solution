package org.marker.weixin.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

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

	private List<Button> buttons = new ArrayList<Button>(3);

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
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonableList(Button.Key4Json.BUTTON.key, buttons, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		//TODO
	}
	
	public static void main(String[] args) throws IOException{
		String appId = "wxa7b4687daedda86f";
		String appSecret = "4322fbf1c4bba4cccd90424e2e16306b";
		
		Menu menu = new Menu();

		Button b1 = new Button.ClickBuilder("营业日报", "AAA")
							.addChild(new Button.ClickBuilder("即时战报", "AAA-1"))
							.addChild(new Button.ClickBuilder("最近日结", "AAA-2"))
							.build();
		menu.set1stButton(b1);
		
		Button b2 = new Button.ClickBuilder("周报", "BBB")
							.addChild(new Button.ClickBuilder("本周报表", "AAA-1"))
							.addChild(new Button.ClickBuilder("上周报表", "AAA-2"))
							.build();
		menu.set2ndButton(b2);

		Button b3 = new Button.ClickBuilder("月报", "AAA")
							.addChild(new Button.ClickBuilder("本月报表", "AAA-1"))
							.addChild(new Button.ClickBuilder("上月报表", "AAA-2"))
							.build();
		menu.set3rdButton(b3);
		
		Token token = Token.newInstance(appId, appSecret);
		System.out.println(token.getAccessToken());
		System.out.println(menu.create(token));
	}
}
