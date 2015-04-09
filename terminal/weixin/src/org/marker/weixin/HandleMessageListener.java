package org.marker.weixin;

import org.marker.weixin.msg.Msg4Event;
import org.marker.weixin.msg.Msg4Image;
import org.marker.weixin.msg.Msg4Link;
import org.marker.weixin.msg.Msg4Location;
import org.marker.weixin.msg.Msg4Text;
import org.marker.weixin.msg.Msg4Video;
import org.marker.weixin.msg.Msg4Voice;

public interface HandleMessageListener{
  public void onTextMsg(Msg4Text paramMsg4Text);
  
  public void onImageMsg(Msg4Image paramMsg4Image);
  
  public void onEventMsg(Msg4Event paramMsg4Event);
  
  public void onLinkMsg(Msg4Link paramMsg4Link);
  
  public void onLocationMsg(Msg4Location paramMsg4Location);
  
  public void onVoiceMsg(Msg4Voice paramMsg4Voice);
  
  public void onErrorMsg(int paramInt);
  
  public void onVideoMsg(Msg4Video paramMsg4Video);
}
