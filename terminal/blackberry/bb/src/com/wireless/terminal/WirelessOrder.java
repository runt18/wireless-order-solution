package com.wireless.terminal;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.GlobalEventListener;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.ui.UiApplication;

import com.wireless.pack.req.PinGen;
import com.wireless.pack.req.ReqPackage;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.PRestaurant;
import com.wireless.protocol.Terminal;
import com.wireless.util.ServerConnector;

/**
 * This sample enables client/server communication using a simple implementation 
 * of TCP sockets. The client application allows the user to select direct TCP as
 * the connection method.  If direct TCP is not selected, a proxy TCP connection
 * is opened using the BlackBerry MDS Connection Service. The server application 
 * can be found in com/rim/samples/server/socketdemo. 
 */
public class WirelessOrder extends UiApplication{

	public static PlatformInfo pfInfo;
	
	public static FoodMenu foodMenu = new FoodMenu();	
	
	public static PRestaurant restaurant = new PRestaurant();
	
	/**
	 * First of all, the program would restore the parameter from persist storage,
	 * and set the parameter for server connector
	 */
	static{
		Params.init();
		//get the network parameters from the persistent storage
 		ServerConnector.instance().setNetAddr(Params.getParam(Params.NET_ADDR));
 		ServerConnector.instance().setNetPort(Params.getParam(Params.NET_PORT));
 		ServerConnector.instance().setNetAPN(Params.getParam(Params.NET_APN));
 		ServerConnector.instance().setNetUser(Params.getParam(Params.NET_USER));
		ServerConnector.instance().setNetPwd(Params.getParam(Params.NET_PWD));
		ServerConnector.instance().setTimeout(Integer.parseInt(Params.getParam(Params.CONN_TIME_OUT)));
		ServerConnector.instance().setConnType(Integer.parseInt(Params.getParam(Params.CONN_TYPE)));
		//set device id generator
		ReqPackage.setGen(new PinGen(){
			public long getDeviceId(){
				return net.rim.device.api.system.DeviceInfo.getDeviceId();
			}
			public short getDeviceType(){
				return Terminal.MODEL_BB;
			}
		});
	}
	
	private boolean _isAutoStartup = false;
	
	private void startup(boolean autostartup){
		_isAutoStartup = autostartup;
		pushScreen(new StartupScreen(_isAutoStartup));
//		invokeLater(new Runnable(){
//			public void run(){
//				pushGlobalScreen(new StartupScreen(_isAutoStartup), 1, UiEngine.GLOBAL_MODAL);				
//			}
//		});
		
		addSystemListener(new SystemListener(){
			public void batteryGood() {	}

			public void batteryLow() { }

			public void batteryStatusChange(int status) {}

			public void powerOff() {}

			/**
			 * In the case the wireless order application is alive before power off,
			 * just have it restored if the auto startup option is set to on.
			 */
			public void powerUp() {
				
				Params.restore();
				
				if(isAlive() && !isForeground() && Integer.parseInt(Params.getParam(Params.AUTO_STARTUP)) == Params.ON){
					requestForeground();
				}
			}	

		});
		enterEventDispatcher();
	}
	
	public WirelessOrder(String[] args, PlatformInfo platformInfo){

		pfInfo = platformInfo;
		
		if(args != null && args.length > 0 && args[0].equals("autostartup")){
			startup(true);
			
		}else if(args != null && args.length > 0 && args[0].equals("run")){
			startup(false);
			
		}else{
			
			/**
			 * This system listener is used to launch the wireless order terminal application after power up.
			 * In the case of hard reset, the powerUp() would be invoked since the application descriptor has 
			 * defined it as general entry point.<br>
			 * In the case of soft reset, it would be launched as a system module after exit the order main screen
			 * if the auto startup option is set to ON.
			 */
			addSystemListener(new SystemListener(){
				public void batteryGood() {	}

				public void batteryLow() { }

				public void batteryStatusChange(int status) {}

				public void powerOff() {}

				public void powerUp() {

					int modHandle = CodeModuleManager.getModuleHandle(WirelessOrder.pfInfo.getModuleName());
					ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(modHandle);
					String[] args = { "autostartup" };
					ApplicationDescriptor apDes4Startup = new ApplicationDescriptor(apDes[0], args);
					try {
						ApplicationManager.getApplicationManager().runApplication(apDes4Startup);
					} catch (ApplicationManagerException e) {

					}

					System.exit(0);
				}	

			});
			
			/**
			 * This global event listener is used to listen to the event that exit the eBootup process
			 */
			this.addGlobalEventListener(new GlobalEventListener(){

				public void eventOccurred(long guid, int data0, int data1, Object object0, Object object1){					
					if(guid == GLOBAL_EVENT_EXIT){
						System.exit(0);
					}
				}
				
			});
			//make it run in background
			requestBackground();
			enterEventDispatcher();
		}
	}
	
	public final static long GLOBAL_EVENT_EXIT = 1; 
	public final static long E_BOOTUP_PROC_ID = 0x66ae754bd0827258L;

} 


