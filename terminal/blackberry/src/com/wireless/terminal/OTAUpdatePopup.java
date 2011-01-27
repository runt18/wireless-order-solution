package com.wireless.terminal;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqOTAUpdate;
import com.wireless.protocol.Type;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class OTAUpdatePopup extends PopupScreen{

	private OTAUpdatePopup _self = this;
	private ProtocolPackage _resp;
	private Exception _excep = null;
	private String _ver;
	private String _otaIP;
	private int _otaPort;
	private boolean _isSilent = true;
	
	/**
	 * The constructor of OTA update pop up screen.
	 * @param isSlient If set silent flag to true, no messages prompt to user except new version update.
	 * 				   Otherwise, rich message would prompt to user, including the network problem, version status and so forth.
	 */
	public OTAUpdatePopup(boolean isSilent){
		super(new VerticalFieldManager());
		add(new LabelField("检查更新中...请稍候"));
		_isSilent = isSilent;
	}
	
	protected void onUiEngineAttached(boolean attached){
		if(attached == true){
			new Thread(){
				public void run(){
					
					HttpConnection connection = null;
					InputStream in = null;
					
					try{
						_resp = ServerConnector.instance().ask(new ReqOTAUpdate());
						if(_resp.header.type == Type.NAK)
							throw new Exception("无法获取更新服务器信息，请检查网络设置");
						
						//get the apn name from the persistent storage
						Params.restore();
						
						String netAPN = Params.getParam(Params.NET_APN);
						//get the remote cod version and compare it with the local cod file 

						//parse the ip address from the response
						_otaIP = new Short((short)(_resp.body[0] & 0xFF)) + "." + 
									new Short((short)(_resp.body[1] & 0xFF)) + "." + 
									new Short((short)(_resp.body[2] & 0xFF)) + "." + 
									new Short((short)(_resp.body[3] & 0xFF));
						_otaPort = (_resp.body[4] & 0x000000FF) | ((_resp.body[5] & 0x000000FF ) << 8);
						String url = "http://" + _otaIP + ":" + _otaPort + "/ota/version.php;deviceside=true";
						if(netAPN.length() != 0){
							url = url + ";apn=" + netAPN;
						}
						connection = (HttpConnection)Connector.open(url);
						connection.setRequestMethod(HttpConnection.GET);
						in = connection.openInputStream();
						int length = (int)connection.getLength();
						if(length > 0){

							//get the local cod version
							ApplicationDescriptor descriptor = ApplicationDescriptor.currentApplicationDescriptor();
							_ver = descriptor.getVersion();
							int beg = 0;
							int end = 0;
							//extract the major, minor, revision value respectively
							end = _ver.indexOf('.', beg);
							int majorLocal = Integer.parseInt(_ver.substring(beg, end));

							beg = end + 1;
							end = _ver.indexOf('.', beg);
							int minorLocal = Integer.parseInt(_ver.substring(beg, end));

							int revLocal = Integer.parseInt(_ver.substring(end + 1));

							//get the remote cod file version
							byte servletData[] = new byte[length];
							in.read(servletData);
							//parse the version whose format is "major.minor.revsion"
							_ver = new String(servletData);
							//cut the '\n' or '\r\n'
							if(_ver.indexOf('\r') != -1){
								_ver = _ver.substring(0, _ver.indexOf('\r'));
							}else if(_ver.indexOf('\n') != -1){
								_ver = _ver.substring(0, _ver.indexOf('\n'));
							}

							beg = 0;
							end = 0;
							//extract the major, minor, revision value respectively
							end = _ver.indexOf('.', beg);
							int majorRemote = Integer.parseInt(_ver.substring(beg, end));

							beg = end + 1;
							end = _ver.indexOf('.', beg);
							int minorRemote = Integer.parseInt(_ver.substring(beg, end));

							int revRemote = Integer.parseInt(_ver.substring(end + 1));

							//compare the remote cod version with the local cod 
							boolean isUpdate = false;
							if(majorRemote > majorLocal){
								isUpdate = true;
							}else if(majorRemote == majorLocal){
								if(minorRemote > minorLocal){
									isUpdate = true;
								}else if(minorRemote == minorLocal){
									if(revRemote > revLocal){
										isUpdate = true;
									}
								}
							}

							if(isUpdate){
								UiApplication.getUiApplication().invokeLater(new Runnable(){
									public void run(){
										Dialog.alert("新版本v" + _ver + "准备就绪，按“确定”开始升级");
										BrowserSession browserSession = Browser.getDefaultSession();
										// now launch the URL
										browserSession.displayPage("http://" + _otaIP + ":" + _otaPort + "/ota/WirelessOrderTerminal.jad");
										browserSession.showBrowser();
										//UiApplication.getUiApplication().pushScreen(new OTAUpdateScreen("cmnet"));
									}
								});
							}else{
								if(!_isSilent){
									UiApplication.getUiApplication().invokeLater(new Runnable(){
										public void run(){
											Dialog.alert("目前程序已经是最新版本");
										}
									});	
								}
							}
						}
					}catch(IOException e){
						if(!_isSilent){
							_excep = e;
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_excep.getMessage());
								}
							});
						}
					}catch(Exception e){
						if(!_isSilent){
							_excep = e;
							UiApplication.getUiApplication().invokeLater(new Runnable(){
								public void run(){
									Dialog.alert(_excep.toString());
								}
							});				
						}
					}finally{
						try{
							if(in != null){
								in.close();
							}
							if(connection != null){
								connection.close();
							}						
						}catch(IOException e){}
						
						UiApplication.getUiApplication().invokeLater(new Runnable(){
							public void run(){
								UiApplication.getUiApplication().popScreen(_self);
							}
						});
					}	
				}				
			}.start();
		}
	}
}
