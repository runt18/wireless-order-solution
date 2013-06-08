package com.wireless.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.wireless.db.DBCon;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pojo.restaurantMgr.Restaurant;

/**
 * The monitor is designed to wait for the control command to the wireless order socket.<br>
 * It'll listen on a specific port (set from the outside conf.xml), and only accept the command
 * send from local host (127.0.0.1).<br>
 * It has commands below.<br> 
 * 1. "start_monitor [-t interval]"
 *    for example, "start_monitor -t 1000" means start running the monitor,
 *    and the monitor would check the socket status every 1 second.<br>
 * 2. "kill_monitor"
 *    stop the monitor.<br>
 * 3. "kill_socket"
 * 	  it's a nice way to stop the wireless socket, the "kill_socket" command would make all the running
 *    threads exit normally.<br>
 * 4. "check_version"
 *    get the version of this server socket
 */
public class MonitorHandler implements Runnable{
	private boolean _isRunning = false;
	private ServerSocket _server = null;
	
	public void kill(){
		_isRunning = false;
		if(_server != null){
			try{
				_server.close();
			}catch(IOException e){}
		}
	}
	
	public void run(){
		try{
			MonitorStatus ms = null;
			//InetAddress localHost = InetAddress.getByName("localhost");
			//listened on port of local host for the print login service
			_server = new ServerSocket(WirelessSocketServer.monitor_listen, 0, InetAddress.getByName("localhost"));
			InputStream in = null;
			OutputStream out = null;
			Socket connection = null;
			String response = null;
			String sep = System.getProperty("line.separator");
			_isRunning = true;
			while(_isRunning){
				try{
					connection = _server.accept();
					//only accept the connection from local host
//					if(!connection.getInetAddress().equals(localHost)){
//						continue;
//					}					
					in = new BufferedInputStream(new DataInputStream(connection.getInputStream()));
					out = new BufferedOutputStream(new DataOutputStream(connection.getOutputStream()));
					byte[] rec_buf = new byte[256];
					int bytesToRead = in.read(rec_buf);
					String cmd = new String(rec_buf, 0, bytesToRead);
					cmd = cmd.split("\n")[0];
					//check if command is "start_monitor"
					if(cmd.startsWith(Cmd.SMonitor)){
						int interval = 1000;
						String[] param = cmd.split(" ");
						if(param.length >= 3){
							if(param[1].equals("-t")){
								try{
									interval = Integer.parseInt(param[2]);
									if(interval < 0){
										throw new NumberFormatException();
									}
								}catch(NumberFormatException e){
									 //if the interval is invalid, set the interval to 1000ms as default
									interval = 1000;
									response = "syntax error..." + Cmd.SMonitor + sep;
									response += "the syntax is \"start_monotir [-t interval]\" and the interval must be greater than zero" + sep;
									out.write(response.getBytes());
									out.flush();
									e.printStackTrace();
								}
							}
						}
						if(ms == null){
							ms = new MonitorStatus();
							ms.setInterval(interval);
							ms.start();
							response = "ok..." + Cmd.SMonitor + "(interval:" + interval + "ms)" + sep;
						}else{
							response = "error..." + Cmd.SMonitor + sep;
							response += "monitor status has been running" + sep;
						}
					//check if the command is "kill_monitor"
					}else if(cmd.startsWith(Cmd.KMonitor)){
						if(ms != null){
							ms.kill();
							ms = null;
							response = "ok..." + Cmd.KMonitor + sep;
						}else{
							response = "error..." + Cmd.KMonitor + sep;
							response += "monitor status hasn't started" + sep;
						}
					//check if the command is "kill_socket"
					}else if(cmd.startsWith(Cmd.KSocket)){
						//terminate the monitor status thread
						response = "ok..." + Cmd.KSocket + sep;
						if(ms != null){
							ms.kill();
							response += "stop the monitor status" + sep;
						}
						//terminate the monitor thread
						if(WirelessSocketServer.monitorHandler != null){
							WirelessSocketServer.monitorHandler.kill();
							response += "stop the monitor handler" + sep;
						}
						//terminate the order request handler
						if(WirelessSocketServer.orderReqHandler != null){
							WirelessSocketServer.orderReqHandler.kill();
							response += "stop the order request handler" + sep;
						}
						//terminate the printer handler
						if(WirelessSocketServer.printerLoginHandler != null){
							WirelessSocketServer.printerLoginHandler.kill();
							response += "stop the printer login handler" + sep;
						}
						//shutdown the thread pool
						if(WirelessSocketServer.threadPool != null){
							WirelessSocketServer.threadPool.shutdown();
							response += "stop the thread pool" + sep;
						}
						//terminate the sweep db task
						if(WirelessSocketServer.scheDBTask != null){
							WirelessSocketServer.scheDBTask.cancel();
							response += "stop the sweeping db task" + sep;
						}
						//terminate the daily settlement task
						if(WirelessSocketServer.scheDailySettlement != null){
							WirelessSocketServer.scheDailySettlement.cancel();
							response += "stop the daily settlement task" + sep;
						}
						//terminate the db connection pool
						try{
							DBCon.destroy();
							response += "destroy the db connection pool" + sep;
						}catch(SQLException e){
							response += "failed to destroy the db connection pool!!!" + sep;
						}
						
					//check if the command is "check_version"
					}else if(cmd.equals(Cmd.CVersion)){
						response = "ok..." + cmd + sep;
						response += "version is " + WirelessSocketServer.VERSION + sep;						
					}else{
						response = "error...command \"" + cmd + "\" is unknown" + sep;
					}
					
				}catch(IOException e){
					e.printStackTrace();
					
				}finally{	
					if(out != null){
						try{
							out.write(response.getBytes());
							out.flush();
							out.close();
						}catch(IOException e){}
						out = null;
					}
					if(in != null){
						try{
							in.close();
						}catch(IOException e){}
						in = null;
					}
					if(connection != null){
						try{
							connection.close();
						}catch(IOException e){}
						connection = null;
					}
				}
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}

class Cmd{
	final static String SMonitor = "start_monitor";
	final static String KMonitor = "kill_monitor";
	final static String KSocket = "kill_socket";
	final static String CVersion = "check_version";
}

/**
 * This class is to log the socket status (such as the thread pool status)
 * to a log file (named "status.log") every specific seconds. *
 */
class MonitorStatus extends Thread{
	private boolean _isRunning = false;
	private long _interval = 1000;
	
	MonitorStatus(){
		this.setPriority(Thread.MIN_PRIORITY);
	}
	
	void setInterval(long interval){
		if(interval > 0)
			_interval = interval;
	}
	
	void kill(){
		_isRunning = false;
	}
	
	boolean isRunning(){
		return _isRunning;
	}
	
	public void run(){
		_isRunning = true;
		String sep = System.getProperty("line.separator");
		while(_isRunning){		
			try{
				File parent = new File("log/");
				if(!parent.exists()){
					parent.mkdir();
				}
				File statusFile = new File("log/status.log");
				if(!statusFile.exists()){
					statusFile.createNewFile();
				}	
				FileWriter statusWriter = new FileWriter(statusFile);
				String status = "Thread pool status: $(core) core,  $(max) max,  $(alive)s alive,  $(queue_size) queues" + sep;
				status += "Thread pool statistics: $(working) working,  $(queued) queued,  $(largest) largest,  $(completed) completed" + sep;
				status += "Db connection pool status: $(init_pool_size) init,  $(min_pool_size) min,  $(max_pool_size) max,  $(busy_pool_size) busy,  $(idle_pool_size) idle" + sep;
				status += "Printer status: $(restaurant_printer) restaurant(s),  $(printer_socket) socket(s)" + sep;
				status += "Print loss status: $(restaurant_loss) restaurant(s),  $(printer_loss) receipt(s)";
				
				//replace the thread pool status
				status = status.replace("$(core)", Integer.toString(WirelessSocketServer.threadPool.getCorePoolSize()));
				status = status.replace("$(max)", Integer.toString(WirelessSocketServer.threadPool.getMaximumPoolSize()));
				status = status.replace("$(alive)", Long.toString(WirelessSocketServer.threadPool.getKeepAliveTime(TimeUnit.SECONDS)));
				status = status.replace("$(queue_size)", Integer.toString(WirelessSocketServer.threadPool.getQueue().size() + WirelessSocketServer.threadPool.getQueue().remainingCapacity()));
				status = status.replace("$(working)", Integer.toString(WirelessSocketServer.threadPool.getActiveCount()));
				status = status.replace("$(queued)", Integer.toString(WirelessSocketServer.threadPool.getQueue().size()));
				status = status.replace("$(largest)", Integer.toString(WirelessSocketServer.threadPool.getLargestPoolSize()));
				status = status.replace("$(completed)", Long.toString(WirelessSocketServer.threadPool.getCompletedTaskCount()));
				
				//replace the db connection pool status
				status = status.replace("$(init_pool_size)", Integer.toString(DBCon.getPoolSource().getInitialPoolSize()));
				status = status.replace("$(min_pool_size)", Integer.toString(DBCon.getPoolSource().getMinPoolSize()));
				status = status.replace("$(max_pool_size)", Integer.toString(DBCon.getPoolSource().getMaxPoolSize()));
				try{
					status = status.replace("$(busy_pool_size)", Integer.toString(DBCon.getPoolSource().getNumBusyConnectionsDefaultUser()));
				}catch(SQLException e){
					status = status.replace("$(busy_pool_size)", "0");
				}
				try{
					status = status.replace("$(idle_pool_size)", Integer.toString(DBCon.getPoolSource().getNumIdleConnectionsDefaultUser()));
				}catch(SQLException e){
					status = status.replace("$(idle_pool_size)", "0");
				}
				
				//calculate the amount of restaurant and printer sockets
				int restaurantAmount = 0;
				int sockAmount = 0;
				for(Entry<Restaurant, List<Socket>> entry : PrinterConnections.instance().stat()){
					if(!entry.getValue().isEmpty()){
						restaurantAmount++;
						sockAmount += entry.getValue().size();
					}
				}
				//replace the amount to restaurant which logged in printer server  
				status = status.replace("$(restaurant_printer)", Integer.toString(restaurantAmount));
				
				//calculate the number of the printer sockets and replace the printer sockets status
				status = status.replace("$(printer_socket)", Integer.toString(sockAmount));
				
				//get the amount of restaurant to print loss
				int nRestaurant = WirelessSocketServer.printLosses.keySet().size();
				status = status.replace("$(restaurant_loss)", Integer.toString(nRestaurant));
				
				//calculate the number of the receipt loss
				Iterator<LinkedList<ProtocolPackage>> iterLoss = WirelessSocketServer.printLosses.values().iterator();
				int nPrintLoss = 0;
				while(iterLoss.hasNext()){
					nPrintLoss += iterLoss.next().size();
				}
				//replace the number of receipt to print loss
				status = status.replace("$(printer_loss)", Integer.toString(nPrintLoss));
				
				//write to the log file
				statusWriter.write(status);
				statusWriter.close();
				try{
					Thread.sleep(_interval);
				}catch(InterruptedException e){}
				
			}catch(IOException e){
				e.printStackTrace();
			}
		}						
	}
}
