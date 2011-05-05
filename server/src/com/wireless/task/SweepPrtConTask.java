package com.wireless.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import org.tiling.scheduling.SchedulerTask;



/**
 * The sweep printer connection task is designed to clean up the invalid socket 
 * connected between socket and printer server.
 * The socket server listen on port to wait for the printer server, 
 * in the case printer server close the connection, the socket would become invalid,
 * so here, we schedule a task to scan all the printer socket connection, and clean up
 * the invalid ones. The task would be scheduled every day.
 */
public class SweepPrtConTask extends SchedulerTask{

	private TreeMap<Integer, ArrayList<Socket>> _printerConnections = null;
	
	public SweepPrtConTask(TreeMap<Integer, ArrayList<Socket>> conn){
		_printerConnections = conn;
	}
	
	public void run(){
		String sep = System.getProperty("line.separator");
		String taskInfo = "Sweep print connection task starts on " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new java.util.Date()) + sep;
		
		try{			
			int nConnection = 0;
			int nRestaurant = 0;
			synchronized(_printerConnections){
				//enumerate the values of printer connection to remove the sockets not connected,
				Iterator<ArrayList<Socket>> iter1 = _printerConnections.values().iterator();
				while(iter1.hasNext()){
					ArrayList<Socket> sockets = iter1.next();
					Iterator<Socket> iter2 = sockets.iterator();
					while(iter2.hasNext()){
						Socket printSocket = iter2.next();
						try{
							printSocket.sendUrgentData(0);
						}catch(IOException e){
							try{
								printSocket.close();
							}catch(IOException ex){}
							nConnection++;
							iter2.remove();							
						}
					}
				}
				//enumerate the keys of printer connection to remove the restaurant not containing any 
				//printer socket connections
				Iterator<Integer> iter3 = _printerConnections.keySet().iterator();
				while(iter3.hasNext()){
					Integer restaurantID = iter3.next();
					if(_printerConnections.get(restaurantID).size() == 0){
						nRestaurant++;
						iter3.remove();
					}
				}
			}	
			taskInfo += "info : " + nConnection + " printer socket(s) are removed" + sep;
			taskInfo += "info : " + nRestaurant + " restaurant's printer info is removed" + sep;
			
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
