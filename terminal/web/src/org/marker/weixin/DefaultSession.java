package org.marker.weixin;

import java.util.ArrayList;
import java.util.List;

import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Image;
import org.marker.weixin.msg.Msg4Link;
import org.marker.weixin.msg.Msg4Location;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.msg.Msg4Video;
import org.marker.weixin.msg.Msg4Voice;

public class DefaultSession extends Session {
	private List<HandleMessageListener> listeners = new ArrayList<>(3);

	public static DefaultSession newInstance() {
		return new DefaultSession();
	}

	public void addOnHandleMessageListener(HandleMessageListener handleMassge) {
		this.listeners.add(handleMassge);
	}

	public void removeOnHandleMessageListener(HandleMessageListener handleMassge) {
		this.listeners.remove(handleMassge);
	}

	public void onTextMsg(Msg4Text msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onTextMsg(msg);
		}
	}

	public void onImageMsg(Msg4Image msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onImageMsg(msg);
		}
	}

	public void onEventMsg(Msg4Event msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onEventMsg(msg);
		}
	}

	public void onLinkMsg(Msg4Link msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onLinkMsg(msg);
		}
	}

	public void onLocationMsg(Msg4Location msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onLocationMsg(msg);
		}
	}

	public void onErrorMsg(int errorCode) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onErrorMsg(errorCode);
		}
	}

	public void onVoiceMsg(Msg4Voice msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onVoiceMsg(msg);
		}
	}

	public void onVideoMsg(Msg4Video msg) {
		for (HandleMessageListener currentListener : this.listeners) {
			currentListener.onVideoMsg(msg);
		}
	}
}
