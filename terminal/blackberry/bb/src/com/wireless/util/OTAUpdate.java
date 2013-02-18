package com.wireless.util;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.system.ApplicationDescriptor;

import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.protocol.ReqOTAUpdate;
import com.wireless.terminal.Params;
import com.wireless.terminal.WirelessOrder;

public class OTAUpdate extends Thread{
	
	private IOTAUpdate _callBack = null;

	
	public OTAUpdate(IOTAUpdate callBack){
		if(callBack == null){
			throw new IllegalArgumentException();
		}
		_callBack = callBack;
	}
	
	public void run(){
		HttpConnection connection = null;
		InputStream in = null;
	
		String otaIP = null;
		int otaPort = 0;
		String ver = null;
		
		try{
			
			_callBack.preOTAUpdate();
			
			ProtocolPackage resp = ServerConnector.instance().ask(new ReqOTAUpdate());
			if(resp.header.type == Type.NAK)
				throw new Exception("无法获取更新服务器信息，请检查网络设置");
			
			//get the apn name from the persistent storage
			Params.restore();
			
			String netAPN = Params.getParam(Params.NET_APN);
			//get the remote cod version and compare it with the local cod file 

			//parse the ip address from the response
			otaIP = new Short((short)(resp.body[0] & 0xFF)) + "." + 
						new Short((short)(resp.body[1] & 0xFF)) + "." + 
						new Short((short)(resp.body[2] & 0xFF)) + "." + 
						new Short((short)(resp.body[3] & 0xFF));
			otaPort = (resp.body[4] & 0x000000FF) | ((resp.body[5] & 0x000000FF ) << 8);
			String url = "http://" + otaIP + ":" + otaPort + WirelessOrder.pfInfo.getVersionUrl();
			int connType = Integer.parseInt(Params.getParam(Params.CONN_TYPE));
			if(connType == Params.CONN_MOBILE){
				url += ";deviceside=true";
			}else if(connType == Params.CONN_WIFI){
				url += ";interface=wifi";
			}else{
				url += ";interface=wifi";
			}
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
				ver = descriptor.getVersion();
				int beg = 0;
				int end = 0;
				//extract the major, minor, revision value respectively
				end = ver.indexOf('.', beg);
				int majorLocal = Integer.parseInt(ver.substring(beg, end));

				beg = end + 1;
				end = ver.indexOf('.', beg);
				int minorLocal = Integer.parseInt(ver.substring(beg, end));

				int revLocal = Integer.parseInt(ver.substring(end + 1));

				//get the remote cod file version
				byte servletData[] = new byte[length];
				in.read(servletData);
				//parse the version whose format is "major.minor.revsion"
				ver = new String(servletData);
				//cut the '\n' or '\r\n'
				if(ver.indexOf('\r') != -1){
					ver = ver.substring(0, ver.indexOf('\r'));
				}else if(ver.indexOf('\n') != -1){
					ver = ver.substring(0, ver.indexOf('\n'));
				}

				ver = ver.trim();
				beg = 0;
				end = 0;
				//extract the major, minor, revision value respectively
				end = ver.indexOf('.', beg);
				int majorRemote = Integer.parseInt(ver.substring(beg, end));

				beg = end + 1;
				end = ver.indexOf('.', beg);
				int minorRemote = Integer.parseInt(ver.substring(beg, end));

				int revRemote = Integer.parseInt(ver.substring(end + 1));

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
					_callBack.passOTAUpdate(ver, "http://" + otaIP + ":" + otaPort + WirelessOrder.pfInfo.getOTAUrl());
				}else{
					_callBack.passOTAUpdate(null, null);
				}
				
			}else{
				_callBack.passOTAUpdate(null, null);
			}
				
		}catch(IOException e){
			_callBack.failOTAUpdate(e.getMessage());
			
		}catch(Exception e){
			_callBack.failOTAUpdate(e.getMessage());
			
		}finally{
			try{
				if(in != null){
					in.close();
				}
				if(connection != null){
					connection.close();
				}						
			}catch(IOException e){}
			
			_callBack.postOTAUpdate();
		}
	}
}
