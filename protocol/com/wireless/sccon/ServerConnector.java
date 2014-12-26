package com.wireless.sccon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.exception.BusinessException;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.req.ReqPing;
import com.wireless.pack.req.RequestPackage;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class ServerConnector{
	
	public static class Connector implements Parcelable{
		private String addr;
		private int port;
		private int success;
		
		private Connector(){}
		
		public Connector(String addr, int port){
			this.addr = addr;
			this.port = port;
		}
		
		public String getAddress(){
			return this.addr;
		}
		
		public int getPort(){
			return this.port;
		}
		
		@Override
		public int hashCode(){
			return addr.hashCode() + (port * 31 + 17);
		}
		
		@Override
		public boolean equals(Object obj){
			if(obj == null || !(obj instanceof Connector)){
				return false;
			}else{
				return addr.equals(((Connector)obj).addr) && port == ((Connector)obj).port;
			}
		}
		
		@Override
		public String toString(){
			return addr + ":" + port;
		}

		@Override
		public void writeToParcel(Parcel dest, int flag) {
			dest.writeString(addr);
			dest.writeInt(port);
		}

		@Override
		public void createFromParcel(Parcel source) {
			addr = source.readString();
			port = source.readInt();
		}
		
		public final static Parcelable.Creator<Connector> CREATOR = new Parcelable.Creator<Connector>(){

			@Override
			public Connector newInstance() {
				return new Connector();
			}
			
			@Override
			public Connector[] newInstance(int size){
				return new Connector[size];
			}
			
		};
	}
	
	private final List<Connector> backupConnectors = new ArrayList<Connector>();
	private Connector masterConnector;
	
	private final static int CONNECTOR_ROTATE_THRESHOLD = 10;
	private final static int DEFAULT_TIME_OUT = 10 * 1000;
	
	private static ServerConnector _instance = new ServerConnector();
	
 	private ServerConnector(){
		
	}
	
	public static ServerConnector instance(){
		return _instance;
	}
	
	public void init(){
		backupConnectors.clear();
	}
	
	public void setMaster(Connector master){
		this.masterConnector = master;
	}
	
	public void addBackup(Connector backup){
		if(!backupConnectors.contains(backup)){
			backupConnectors.add(backup);
		}
	}
	
	private void resetBackup(){
		for(Connector backup : backupConnectors){
			backup.success = 0;
		}
	}
	
	public List<Connector> getBackups(){
		return new ArrayList<Connector>(backupConnectors);
	}
	
	/**
	 * Ask the server to get the result. This function is synchronized.
	 * The thread call this function would be blocked until receiving the
	 * response in the receive thread. 
	 * @throws BusinessException 
	 **/
	public ProtocolPackage ask(RequestPackage req) throws IOException, BusinessException{
		return ask(req, DEFAULT_TIME_OUT);
	}
	
	/**
	 * Ask the server to get the result. This function is synchronized.
	 * The thread call this function would be blocked until receiving the
	 * response in the receive thread. 
	 * @throws BusinessException 
	 **/
	public ProtocolPackage ask(RequestPackage req, int timeout) throws IOException, BusinessException{
		try{
			ProtocolPackage response = new Session(req, timeout).execute(masterConnector.addr, masterConnector.port);
			resetBackup();
			return response;
		}catch(IOException e){
			/**
			 * The fail-over scheme as below.
			 * Use the backup connector to send request again if the master failed,
			 * If the amount to success of the backup connector request reaches the threshold,
			 * then exchange this backup and the master connector.  
			 */
			ProtocolPackage response = null;
			for(Connector backup : new ArrayList<Connector>(backupConnectors)){
				try{
					ask(backup, new ReqPing(), 2000);
					response = new Session(req, timeout).execute(backup.addr, backup.port);
					if(backup.success++ >= CONNECTOR_ROTATE_THRESHOLD){
						addBackup(masterConnector);
						backupConnectors.remove(backup);
						masterConnector = backup;
						resetBackup();
					}
					break;
				}catch(BusinessException ignored){
					continue;
				}catch(IOException ignored){
					continue;
				}
			}
			
			if(response != null){
				return response;
			}else{
				throw e;
			}
		}
	}
	
	/**
	 * Ask the server to get the result. This function is synchronized.
	 * The thread call this function would be blocked until receiving the
	 * response in the receive thread. 
	 * @throws BusinessException 
	 **/
	public ProtocolPackage ask(Connector connector, RequestPackage req, int timeout) throws IOException, BusinessException{
		return new Session(req, timeout).execute(connector.addr, connector.port);
	}
}
