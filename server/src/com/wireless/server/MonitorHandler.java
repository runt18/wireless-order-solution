package com.wireless.server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
			InetAddress localHost = InetAddress.getByName("localhost");
			//listened on port for the print login service
			_server = new ServerSocket(WirelessSocketServer.monitor_listen);
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
					if(!connection.getInetAddress().equals(localHost)){
						continue;
					}					
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
						//terminate the sweep printer connection task
						if(WirelessSocketServer.schePrtConTask != null){
							WirelessSocketServer.schePrtConTask.cancel();
							response += "stop the sweeping print connection task" + sep;
						}
						//terminate the daily settlement task
						if(WirelessSocketServer.scheDailySettlement != null){
							WirelessSocketServer.scheDailySettlement.cancel();
							response += "stop the daily settlement task" + sep;
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
				status += "Printer status: $(restaurant_printer) restaurant(s),  $(printer_socket) socket(s)";
				//replace the thread pool status
				status = status.replace("$(core)", new Integer(WirelessSocketServer.threadPool.getCorePoolSize()).toString());
				status = status.replace("$(max)", new Integer(WirelessSocketServer.threadPool.getMaximumPoolSize()).toString());
				status = status.replace("$(alive)", new Long(WirelessSocketServer.threadPool.getKeepAliveTime(TimeUnit.SECONDS)).toString());
				status = status.replace("$(queue_size)", new Integer(WirelessSocketServer.threadPool.getQueue().size() + WirelessSocketServer.threadPool.getQueue().remainingCapacity()).toString());
				status = status.replace("$(working)", new Integer(WirelessSocketServer.threadPool.getActiveCount()).toString());
				status = status.replace("$(queued)", new Integer(WirelessSocketServer.threadPool.getQueue().size()).toString());
				status = status.replace("$(largest)", new Integer(WirelessSocketServer.threadPool.getLargestPoolSize()).toString());
				status = status.replace("$(completed)", new Long(WirelessSocketServer.threadPool.getCompletedTaskCount()).toString());
				//get the amount of the restaurant logging in in the printer server
				int nRestaurant = WirelessSocketServer.printerConnections.keySet().size();
				status = status.replace("$(restaurant_printer)", new Integer(nRestaurant).toString());
				//calculate the number of the printer sockets
				Iterator<ArrayList<Socket>> iter = WirelessSocketServer.printerConnections.values().iterator();
				int nPrtSocket = 0;
				while(iter.hasNext()){
					nPrtSocket += iter.next().size();
				}
				//replace the printer sockets status
				status = status.replace("$(printer_socket)", new Integer(nPrtSocket).toString());
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
