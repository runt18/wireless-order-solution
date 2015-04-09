package org.marker.weixin;

import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Image;
import org.marker.weixin.msg.Msg4Link;
import org.marker.weixin.msg.Msg4Location;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.msg.Msg4Video;
import org.marker.weixin.msg.Msg4Voice;
import org.marker.weixin.session.WxSession;

public class HandleMessageAdapter implements HandleMessageListener {
	protected final WxSession session;
	
	public HandleMessageAdapter(final WxSession session){
		this.session = session;
	}
	
	@Override
	public void onTextMsg(Msg4Text msg) {
	}

	@Override
	public void onImageMsg(Msg4Image msg) {
	}

	@Override
	public void onEventMsg(Msg4Event msg) {
	}

	@Override
	public void onLinkMsg(Msg4Link msg) {
	}

	@Override
	public void onLocationMsg(Msg4Location msg) {
	}

	@Override
	public void onErrorMsg(int errorCode) {
	}

	@Override
	public void onVoiceMsg(Msg4Voice msg) {
	}

	@Override
	public void onVideoMsg(Msg4Video msg) {
	}
}
