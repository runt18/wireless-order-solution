package com.wireless.server;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.tiling.scheduling.DailyIterator;
import org.tiling.scheduling.MonthlyIterator;
import org.tiling.scheduling.Scheduler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.wireless.db.DBCon;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.task.DailySettlementTask;
import com.wireless.task.SweepDBTask;

public class WirelessSocketServer {

	//the version of the wireless socket server
	static final String VERSION = "1.1.6";
	//the OTA server address
	static String OTA_IP = ""; 
	//the OTA server port
	static String OTA_Port = "";
	//the database name
	static String database = "wireless_order_db";
	//the host to database
	static String host = "localhost";
	//the port to database
	static String port = "3306";
	//the listened port to the wireless order socket
	static int listen = 55555;
	//the listened port to the printer socket
	static int printer_listen = 44444;
	//the listened port to the monitor
	static int monitor_listen = 33333;
	//the user name to database
    static String user = "";   
    //the password to database
    static String password = "";
    
    static int coolPoolSize = 100;
    static int maxPoolSize = 200;
    static long aliveTime = 600;
    static int blockQueueSize = 200;
	
    //the hash map holding the information is as below
    public static final Map<PType, Map<PStyle, String>> printTemplates = new HashMap<PType, Map<PStyle, String>>();    
    
    //the thread pool
    static ThreadPoolExecutor threadPool = null;
    
    //monitor handler
    static MonitorHandler monitorHandler = null;
    //order request handler
    static OrderReqHandler orderReqHandler = null;
    //printer login handler
    static PrinterLoginHandler printerLoginHandler = null;
    //the sweep db scheduler task
    static Scheduler scheDbTask = null;
    //the daily settlement task
    static Scheduler scheDailySettlement = null;
    
	/**
	 * @param args
	 * @throws PropertyVetoException 
	 */
	public static void main(String[] args) throws PropertyVetoException{

		if(args.length == 1){
			File param = new File(args[0]);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try{
				DocumentBuilder builder = factory.newDocumentBuilder(); 
				Document doc = builder.parse(param);
				NodeList nl = null;
				nl = doc.getElementsByTagName("db");
				if(nl.item(0) != null){
					database = nl.item(0).getFirstChild().getNodeValue();
				}
				
				nl = doc.getElementsByTagName("host");
				if(nl.item(0) != null){
					host = nl.item(0).getFirstChild().getNodeValue();
				}
				
				nl = doc.getElementsByTagName("port");
				if(nl.item(0) != null){
					port = nl.item(0).getFirstChild().getNodeValue();
				}
				
				nl = doc.getElementsByTagName("user");
				if(nl.item(0) != null){
					user = nl.item(0).getFirstChild().getNodeValue();
				}
				
				nl = doc.getElementsByTagName("pwd");
				if(nl.item(0) != null){
					password = nl.item(0).getFirstChild().getNodeValue();
				}
				
				DBCon.init(host, port, database, user, password);
				
				nl = doc.getElementsByTagName("listen");
				if(nl.item(0) != null){
					listen = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue());
				}
				
				nl = doc.getElementsByTagName("printer_listen");
				if(nl.item(0) != null){
					printer_listen = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue());
				}
				
				nl = doc.getElementsByTagName("monitor_listen");
				if(nl.item(0) != null){
					monitor_listen = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue());
				}
				
				nl = doc.getElementsByTagName("print_template");
				for(int i = 0; i < nl.getLength(); i++){
					int func = Integer.parseInt(((Element)nl.item(i)).getAttribute("func"));
					NodeList fileList = ((Element)nl.item(i)).getElementsByTagName("file");
					HashMap<PStyle, String> templates = new HashMap<PStyle, String>();
					for(int j = 0; j < fileList.getLength(); j++){
						int style = Integer.parseInt(((Element)fileList.item(j)).getAttribute("style"));
						FileInputStream fis = new FileInputStream(new File(((Element)fileList.item(j)).getAttribute("path")));
						byte[] buf = new byte[fis.available()];
						fis.read(buf);
						templates.put(PStyle.valueOf(style), new String(buf, "GBK"));
						fis.close();
					}

					printTemplates.put(PType.valueOf(func), templates);
				}
				
				nl = doc.getElementsByTagName("cool_pool_size");
				if(nl.item(0) != null){
					coolPoolSize = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue());
				}
				
				nl = doc.getElementsByTagName("max_pool_size");
				if(nl.item(0) != null){
					maxPoolSize = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue());
				}
				
				nl = doc.getElementsByTagName("alive_time");
				if(nl.item(0) != null){
					aliveTime = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue());
				}
				
				nl = doc.getElementsByTagName("block_queue_size");
				if(nl.item(0) != null){
					blockQueueSize = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue());
				}

				nl = doc.getElementsByTagName("OTA");
				if(nl.item(0) != null){
					NodeList hostTag = ((Element)nl.item(0)).getElementsByTagName("host");
					OTA_IP = hostTag.item(0) != null ? hostTag.item(0).getFirstChild().getNodeValue() : "";
					NodeList portTag = ((Element)nl.item(0)).getElementsByTagName("port");
					OTA_Port = portTag.item(0) != null ? portTag.item(0).getFirstChild().getNodeValue() : "";
				}
				
				threadPool = new ThreadPoolExecutor(coolPoolSize,
						  							maxPoolSize,
						  							aliveTime, 
						  							TimeUnit.SECONDS,
						  							new ArrayBlockingQueue<Runnable>(blockQueueSize), 
						  							new ThreadPoolExecutor.DiscardPolicy()); 
				
				//start to run the monitor handler
				monitorHandler = new MonitorHandler();
				new Thread(monitorHandler, "Monitor").start();
				//start to run the order request handler
				orderReqHandler = new OrderReqHandler();
				new Thread(orderReqHandler, "Order").start();
				//start to run the printer login handler
				printerLoginHandler = new PrinterLoginHandler();
				new Thread(printerLoginHandler, "Printer Login").start();
				
				//start to schedule the sweep db task
				scheDbTask = new Scheduler();
				//parse the time to run sweep db task from configuration file
				nl = doc.getElementsByTagName("sweep_db");
				if(nl.item(0) != null){
					String time = nl.item(0).getFirstChild().getNodeValue();
					int pos1 = 0;
					int pos2 = time.indexOf(",", pos1);
					int dayOfMonth = Integer.parseInt(time.substring(pos1, pos2));
					
					pos1 = pos2 + 1;
					pos2 = time.indexOf(",", pos1);
					int hourOfDay = Integer.parseInt(time.substring(pos1, pos2));
					
					pos1 = pos2 + 1;
					pos2 = time.indexOf(",", pos1);
					int minute = Integer.parseInt(time.substring(pos1, pos2));
					
					pos1 = pos2 + 1;
					int second = Integer.parseInt(time.substring(pos1));
					//schedule the sweep db task
					scheDbTask.schedule(new SweepDBTask(), 
										new MonthlyIterator(dayOfMonth, hourOfDay, minute, second));
				}else{
					//schedule the sweeper task on 15th 2:00am every month if not specified in conf.xml 
					//scheDbTask.schedule(new SweepDBTask(), new MonthlyIterator(15, 2, 0, 0));
				}
				
				
				//start to schedule the daily settlement task
				scheDailySettlement = new Scheduler();
				//parse the time to run daily settlement task from configuration file
				nl = doc.getElementsByTagName("daily_settlement");
				if(nl.item(0) != null){
					String time = nl.item(0).getFirstChild().getNodeValue();
					int pos1 = 0;
					int pos2 = time.indexOf(",", pos1);
					int hourOfDay = Integer.parseInt(time.substring(pos1, pos2));
					
					pos1 = pos2 + 1;
					pos2 = time.indexOf(",", pos1);
					int minute = Integer.parseInt(time.substring(pos1, pos2));
					
					pos1 = pos2 + 1;
					int second = Integer.parseInt(time.substring(pos1));
					//schedule the daily settlement task
					scheDailySettlement.schedule(new DailySettlementTask(), 
												 new DailyIterator(hourOfDay, minute, second));
				}else{
					//schedule the daily settlement task on 01:23:37 if not specified in conf.xml 
					//scheDailySettlement.schedule(new DailySettlementTask(), new DailyIterator(1, 23, 37));
				}
				
			}catch(ParserConfigurationException e){
				e.printStackTrace();
				
			}catch(IOException e){
				e.printStackTrace();
				
			}catch(SAXException e){
				e.printStackTrace();
			}
		}else{
			System.err.println("Must specify the config XML file before running the wireless order socket.");
		}
	}

}
