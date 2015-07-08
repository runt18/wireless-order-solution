package com.wireless.Actions.weixin;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.marker.weixin.HandleMessageAdapter;
import org.marker.weixin.auth.AuthParam;
import org.marker.weixin.auth.AuthorizationInfo;
import org.marker.weixin.auth.AuthorizerToken;
import org.marker.weixin.msg.Msg;
import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.session.WxSession;

public class WxAccessHandleMessage extends HandleMessageAdapter {

	private final static String ACCESS_USER_NAME = "gh_3c884a361561";
	
	public WxAccessHandleMessage(WxSession session) {
		super(session);
	}

	@Override
	public void onTextMsg(final Msg4Text msg) {
		if(msg.getToUserName().equalsIgnoreCase(ACCESS_USER_NAME) && msg.getContent().equals("TESTCOMPONENT_MSG_TYPE_TEXT")){
			session.callback(new Msg4Text(msg, "TESTCOMPONENT_MSG_TYPE_TEXT_callback"));
			
		}else if(msg.getToUserName().equalsIgnoreCase(ACCESS_USER_NAME) && msg.getContent().contains("QUERY_AUTH_CODE")){
			final String queryAuthCode = msg.getContent().split(":")[1];
			//System.out.println("Query Auth Code : " + queryAuthCode);
			
			AuthorizationInfo authorizationInfo;
			try {
				authorizationInfo = AuthorizationInfo.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, queryAuthCode);
				AuthorizerToken authorizerToken = AuthorizerToken.newInstance(AuthParam.COMPONENT_ACCESS_TOKEN, authorizationInfo.getAuthorizerAppId(), authorizationInfo.getAuthorizerRefreshToken());
				session.callback(authorizerToken, new Callable<Msg>(){
					@Override
					public Msg call() throws Exception {
						return new Msg4Text(msg, "$query_auth_code$_from_api".replace("$query_auth_code$", queryAuthCode));
					}
					
				});
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
	
	@Override
	public void onEventMsg(Msg4Event msg) {
		if(msg.getToUserName().equalsIgnoreCase(ACCESS_USER_NAME)){
			session.callback(new Msg4Text(msg, msg.getEvent().toString() + "from_callback"));
		}
	}
}
