package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.tiling.scheduling.SchedulerTask;



/**
 * @deprecated
 * The sweep printer connection task is designed to clean up the invalid socket 
 * connected between socket and printer server.
 * The socket server listen on port to wait for the printer server, 
 * in the case printer server close the connection, the socket would become invalid,
 * so here, we schedule a task to scan all the printer socket connection, and clean up
 * the invalid ones. The task would be scheduled every day.
 */
public class SweepPrtConTask extends SchedulerTask{

	private Map<Integer, List<Socket>> _printerConnections = null;
	
	public SweepPrtConTask(Map<Integer, List<Socket>> conn){
		_printerConnections = conn;
	}
	
	public void run(){
		String sep = System.getProperty("line.separator");
		String taskInfo = "Sweep print connection task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date()) + sep;
		
		try{			
			int nConnection = 0;
			int nRestaurant = 0;
			synchronized(_printerConnections){
				Iterator<Entry<Integer, List<Socket>>> iter = _printerConnections.entrySet().iterator();
				
				while(iter.hasNext()){
					Entry<Integer, List<Socket>> entry = iter.next();
					
					Iterator<Socket> iterSock = entry.getValue().iterator();
					//Enumerate the socket list and remove the socket has been disconnected. 
					while(iterSock.hasNext()){
						Socket printSock = iterSock.next();
						try{
							printSock.sendUrgentData(0);
						}catch(IOException e){
							try{
								printSock.close();
							}catch(IOException ex){
								
							}finally{
								nConnection++;
								iterSock.remove();
							}
						}
					}
					
					//Remove the restaurant if no valid socket exist 
					if(entry.getValue().isEmpty()){
						nRestaurant++;
						iter.remove();
					}					
				}
			}	
			taskInfo += "info : " + nConnection + " printer socket(s) are removed" + sep;
			taskInfo += "info : " + nRestaurant + " restaurant's printer(s) info is removed" + sep;
			
		}catch(Exception e){
			taskInfo += "info : " + e.getMessage() + sep;
			
		}finally{			
			//append to the log file
			taskInfo += "***************************************************************" + sep;
			try{
				File parent = new File("log/");
				if(!parent.exists()){
					parent.mkdir();
				}
				File logFile = new File("log/sweep_prt_conn.log");
				if(!logFile.exists()){
					logFile.createNewFile();
				}
				FileWriter logWriter = new FileWriter(logFile, true);
				logWriter.write(taskInfo);
				logWriter.close();
			}catch(IOException e){}
		}
	}
}
