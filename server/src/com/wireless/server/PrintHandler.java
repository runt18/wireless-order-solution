package com.wireless.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.wireless.db.QueryShift;
import com.wireless.print.PFormat;
import com.wireless.print.content.OrderDetailContent;
import com.wireless.print.content.OrderListContent;
import com.wireless.print.content.ReceiptContent;
import com.wireless.print.content.ShiftContent;
import com.wireless.print.content.TransTableContent;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Mode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPrintOrder;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Type;

/**
 * The print handler is used for handling the print request.
 * Usually, the print request would be sent after inserting or being paid order,
 * it can run in synchronized or asynchronous mode.
 * In the synchronized mode, the order request must wait until the print request is done,
 * and responds with an ACK or NAK to terminal, so that let the terminal know whether succeed in printing or not.
 * While in the asynchronous mode, the order request returns immediately regardless of the print request,
 * that means the terminal doesn't care the status of print request any more.
 * And the synchronized and asynchronous mode can be chosen by setting the reserved byte of the protocol header. 
 */
public class PrintHandler extends Handler implements Runnable{
	
	public static class PrintParam{
		public PrintParam(){
			orderInfo = new Order();
		}
		PrintParam(PrintParam param){
			this.orderInfo = param.orderInfo;
			this.restaurant = param.restaurant;
			this.depts = param.depts;
			this.term = param.term;
			this.onDuty = param.onDuty;
			this.offDuty = param.offDuty;
		}
		public Order orderInfo;
		public Restaurant restaurant;
		public Department[] depts;
		public Terminal term;
		public long onDuty;
		public long offDuty;
	}
	
//	private Order _orderInfo = null;
	private Socket[] _socks = null;
//	private Restaurant _restaurant = null;
//	private Terminal _term = null;
	
	private PrintParam _param;
	private byte[] _funcCodes = null;
	
	public PrintHandler(Socket[] socks, int conf, PrintParam param){
		_param = new PrintParam(param);
		_socks = socks;
		ArrayList<Byte> printFuncs = new ArrayList<Byte>();
		if((conf & Reserved.PRINT_ORDER_2) != 0){
			printFuncs.add(Reserved.PRINT_ORDER); 
		}
		if((conf & Reserved.PRINT_ORDER_DETAIL_2) != 0){
			printFuncs.add(Reserved.PRINT_ORDER_DETAIL);
		}
		if((conf & Reserved.PRINT_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_RECEIPT);
		}
		if((conf & Reserved.PRINT_ALL_EXTRA_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_ALL_EXTRA_FOOD);
		}
		if((conf & Reserved.PRINT_EXTRA_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_EXTRA_FOOD);
		}
		if((conf & Reserved.PRINT_ALL_CANCELLED_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_ALL_CANCELLED_FOOD);
		}
		if((conf & Reserved.PRINT_CANCELLED_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_CANCELLED_FOOD);
		}
		if((conf & Reserved.PRINT_HURRIED_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_HURRIED_FOOD);
		}
		if((conf & Reserved.PRINT_ALL_HURRIED_FOOD_2) != 0){
			printFuncs.add(Reserved.PRINT_ALL_HURRIED_FOOD);
		}
		if((conf & Reserved.PRINT_TRANSFER_TABLE_2) != 0){
			if(_param.orderInfo.destTbl.aliasID != _param.orderInfo.srcTbl.aliasID){
				printFuncs.add(Reserved.PRINT_TRANSFER_TABLE);
			}
		}
		if((conf & Reserved.PRINT_TEMP_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_TEMP_RECEIPT);
		}
		if((conf & Reserved.PRINT_SHIFT_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_SHIFT_RECEIPT);
		}
		if((conf & Reserved.PRINT_TEMP_SHIFT_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_TEMP_SHIFT_RECEIPT);
		}
		if((conf & Reserved.PRINT_DAILY_SETTLE_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_DAILY_SETTLE_RECEIPT);
		}		
		if((conf & Reserved.PRINT_HISTORY_SHIFT_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_HISTORY_SHIFT_RECEIPT);
		}
		if((conf & Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT_2) != 0){
			printFuncs.add(Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT);
		}
		_funcCodes = new byte[printFuncs.size()];
		for(int i = 0; i < _funcCodes.length; i++){
			_funcCodes[i] = printFuncs.get(i);
		}
	}
	
	/**
	 * In this method, the print handler gets the template according to the print
	 * function code, send the print request and waits for an ACK (means successfully)
	 * or a NAK (means failed). If an IO error occurs, we close this connection and remove
	 * it from the printer connections.  
	 */
	public void run(){
		
		if(_funcCodes.length == 0){
			return;
		}
		
		LinkedList<PrintRequest> printReqs = new LinkedList<PrintRequest>();
		
		try{
			
			/**
			 * Generate each print request and have them stored in a queue.
			 */			
			for(int i = 0; i < _funcCodes.length; i++){	
				try{
					printReqs.add(new PrintRequest(_funcCodes[i], genPrintReq(_funcCodes[i])));
				}catch(PrintError e){
					e.printStackTrace();
				}catch(UnsupportedEncodingException e){
					e.printStackTrace();
				}
			}
			
			/**
			 * Enumerate the print request queue to send client all the requests.
			 * Remove the request from the queue if succeed to send the request.
			 */
			Iterator<PrintRequest> iter = printReqs.iterator();
			while(iter.hasNext()){
				
				PrintRequest reqPrint = iter.next();
				
				boolean isPrintOK = false;
				for(int i = 0; i < _socks.length; i++){
					if(_socks[i] == null){
						continue;
					}
					try{						
						/**
						 * Since socket would be shared with the print actions to the same restaurant,
						 * make the communication to the socket in synchronized mode.
						 */
						synchronized(_socks[i]){
							
							/**
							 * Check to see whether the printer connection is connected before performing the print action.
							 * Remove the socket if the connection has been disconnected.
							 */
							_socks[i].sendUrgentData(0);
							
							//send the print request
							send(_socks[i].getOutputStream(), reqPrint.pack);
							
							//_socks[i].sendUrgentData(0);
							
							//any socket works means print OK
							isPrintOK = true;							
							
							//receive the response and timeout after 10s
							ProtocolPackage respPrint = recv(_socks[i].getInputStream(), 10 * 1000);
							//check whether the response's sequence equals to the request's
							if(respPrint.header.seq == reqPrint.pack.header.seq){
								if(respPrint.header.mode != Mode.PRINT || respPrint.header.type != Type.ACK){
									throw new IOException("The Print Response(printType=" + reqPrint.conf + ")from printer server is not an ACK.");
								}							
							}
						}
						
					}catch(IOException e){
						e.printStackTrace();
						
						try{
							_socks[i].close();
							//throw an PrintSocketException
							//throw new PrintSocketException("The printer socket has been disconnected.");	
							
						}catch(IOException e2){
							
						}finally{
							//remove the invalid socket
							synchronized(WirelessSocketServer.printerConnections){
								ArrayList<Socket> printerConn = WirelessSocketServer.printerConnections.get(_param.restaurant.id);
								if(printerConn != null){
									printerConn.remove(_socks[i]);
									if(printerConn.size() == 0){
										WirelessSocketServer.printerConnections.remove(_param.restaurant.id);
									}
								}
							}		
							_socks[i] = null;
						}		
					}
				}	
				
				//remove the print request content if print OK
				if(isPrintOK){
					iter.remove();
				}
			}		
			
		}finally{
			/**
			 * Finally store the unprinted request,
			 * so that reprint these request while client re-connect to print server.
			 */
			storePrintLosses(printReqs);
		}
	}
	
	/**
	 * Store the unprinted requests to the list
	 * @param printType 
	 * 			the print type
	 * @param reqPrint
	 * 			the print request
	 */	
	private void storePrintLosses(List<PrintRequest> printReqs){
		synchronized(WirelessSocketServer.printLosses){
			
			LinkedList<ProtocolPackage> printLosses = WirelessSocketServer.printLosses.get(_param.restaurant.id);
			
			Iterator<PrintRequest> iter = printReqs.iterator();
			while(iter.hasNext()){
				PrintRequest reqPrint = iter.next();
				if(reqPrint != null){
					if(printLosses != null){
						/**
						 * Insert the unprinted request to the list,
						 * and remove the 1st one if the requests exceed a specific amount. 
						 */
						printLosses.add(reqPrint.pack);
						if(printLosses.size() > 50){
							printLosses.removeFirst();
						}
					}else{
						/**
						 * If the list does NOT exist before,
						 * create a new link list to hold the unprinted request
						 */
						printLosses = new LinkedList<ProtocolPackage>();
						printLosses.add(reqPrint.pack);
						WirelessSocketServer.printLosses.put(_param.restaurant.id, printLosses);
					}
				}
			}
		}
	}
	
	/**
	 * Do the specific print action.
	 * @param printType 
	 * 				the print type to perform
	 * @param restaurant 
	 * 				the related restaurant information used for print template
	 * @throws PrintError
	 * 				throws if one of the cases below.<br>
	 * 				- The corresponding template is NOT found.<br> 				
	 * @throws UnsupportedEncodingException
	 *
	 */
	private ProtocolPackage genPrintReq(byte printType) throws PrintError, UnsupportedEncodingException{
		
		//get the print templates according to the function code
		HashMap<Integer, String> templates = null;
		if(printType == Reserved.PRINT_ORDER || printType == Reserved.PRINT_ALL_CANCELLED_FOOD || 
				printType == Reserved.PRINT_ALL_EXTRA_FOOD || printType == Reserved.PRINT_ALL_HURRIED_FOOD){
			//the template to order
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_ORDER);
			
		}else if(printType == Reserved.PRINT_ORDER_DETAIL || printType == Reserved.PRINT_EXTRA_FOOD || 
				printType == Reserved.PRINT_CANCELLED_FOOD || printType == Reserved.PRINT_HURRIED_FOOD){
			//the template to order detail
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_ORDER_DETAIL);
			
		}else if(printType == Reserved.PRINT_RECEIPT || printType == Reserved.PRINT_TEMP_RECEIPT){
			//the template to receipt
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_RECEIPT);
			
		}else if(printType == Reserved.PRINT_TRANSFER_TABLE){
			//the template to table transfer
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_TRANSFER_TABLE);
			
		}else if(printType == Reserved.PRINT_SHIFT_RECEIPT || printType == Reserved.PRINT_TEMP_SHIFT_RECEIPT || 
				printType == Reserved.PRINT_DAILY_SETTLE_RECEIPT || printType == Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT ||
				printType == Reserved.PRINT_HISTORY_SHIFT_RECEIPT){
			//the template to shift
			templates = WirelessSocketServer.printTemplates.get((int)Reserved.PRINT_SHIFT_RECEIPT);
		}
		
		HashMap<byte[], byte[]> printContent = new HashMap<byte[], byte[]>(); 
		int totalBytes = 0;
		//enumerate to get each style of print template to this function code 
		Iterator<Integer> iter = templates.keySet().iterator();
		while(iter.hasNext()){	
			Integer style = iter.next();
			byte[] itemBody = null;
			String printTemplate = templates.get(style);
			if(printTemplate != null){
				
//				String category;
//				if(_param.orderInfo.category == Order.CATE_JOIN_TABLE){
//					category = "(并台)";
//				}else if(_param.orderInfo.category == Order.CATE_MERGER_TABLE){
//					category = "(拼台)";
//				}else if(_param.orderInfo.category == Order.CATE_TAKE_OUT){
//					category = "(外卖)";
//				}else{
//					category = "";
//				}
				
				//handle the print request to order
				if(printType == Reserved.PRINT_ORDER || printType == Reserved.PRINT_ALL_CANCELLED_FOOD || 
						printType == Reserved.PRINT_ALL_EXTRA_FOOD || printType == Reserved.PRINT_ALL_HURRIED_FOOD){					
					
					
					HashMap<Department, List<OrderFood>> foodsByDept = new HashMap<Department, List<OrderFood>>();
					for(Department dept : _param.depts){
						foodsByDept.put(dept, new ArrayList<OrderFood>());
					}
					foodsByDept.put(new Department("", Department.DEPT_ALL, _param.term.restaurantID), Arrays.asList(_param.orderInfo.foods));					
					
					/**
					 * Put the order foods to the corresponding department list. 
					 */
					for(OrderFood orderFood : _param.orderInfo.foods){
						List<OrderFood> foods = foodsByDept.get(orderFood.kitchen.dept);
						if(foods != null){
							foods.add(orderFood);
							foodsByDept.put(orderFood.kitchen.dept, foods);
						}
					}

					int totalOrderBytes = 0;
					/**
					 * The hash map contains the order's header and body bytes to print of each department.
					 */
					HashMap<byte[], byte[]> bytesByDept = new HashMap<byte[], byte[]>();
					
					/**
					 * Generate the content of order, the body looks like below.
					 * <order_1> : <order_2> : ...
					 * Each order looks like below.
					 * dept : len[2] : content
					 * dept - 1-byte indicating the department no
					 * len[2] - 2-byte indicating the length of order content
					 * content - the order content
					 */		
					Iterator<Map.Entry<Department, List<OrderFood>>> iterFoodByDept = foodsByDept.entrySet().iterator();
					while(iterFoodByDept.hasNext()){
						Map.Entry<Department, List<OrderFood>> entry = iterFoodByDept.next();
						Department dept = entry.getKey();
						List<OrderFood> foods = entry.getValue();
						/**
						 * Remove the department list not containing any order foods.
						 */
						if(foods.size() == 0){
							iterFoodByDept.remove();
							
						}else{				
							byte[] body = new OrderListContent(dept, 
															   printTemplate,  
															   PFormat.RECEIPT_FORMAT_DEF, 
															   _param.orderInfo,
															   _param.term,
															   printType, 
															   style).toString().getBytes("GBK");
							
							//allocate the memory to header
							byte[] header = new byte[3];	
							//assign the department id
							header[0] = (byte)dept.deptID;
							//assign the length of body
							header[1] = (byte)(body.length & 0x000000FF);
							header[2] = (byte)((body.length & 0x0000FF00) >> 8);
							
							bytesByDept.put(header, body);
							
							totalOrderBytes += header.length + body.length;
									
						}
					}
					
					//allocate the memory to bytes to print 
					itemBody = new byte[totalOrderBytes];
					int offset = 0;
					Iterator<Map.Entry<byte[], byte[]>> iterBytesByDept = bytesByDept.entrySet().iterator();
					while(iterBytesByDept.hasNext()){
						Map.Entry<byte[], byte[]> entry = iterBytesByDept.next();
						byte[] header = entry.getKey();
						byte[] body = entry.getValue();
						//append the header to the bytes to print
						System.arraycopy(header, 0, itemBody, offset, header.length);
						offset += header.length;
						//append the body to the bytes to print
						System.arraycopy(body, 0, itemBody, offset, body.length);
						offset += body.length;
					}
					
					
				//handle the print request to order detail and extra food detail
				}else if(printType == Reserved.PRINT_ORDER_DETAIL || printType == Reserved.PRINT_EXTRA_FOOD || 
						printType == Reserved.PRINT_CANCELLED_FOOD || printType == Reserved.PRINT_HURRIED_FOOD){
					
					/**
					 * Generate the content of order detail, the body looks like below.
					 * <order_detail_1> : <order_detail_2> : ...
					 * Each order detail looks like below.
					 * kitchen : len[2] : content
					 * kitchen - 1-byte indicating the kitchen no
					 * len[2] - 2-byte indicating the length of detail content
					 * content - the order detail content
					 */
					int totalOrderDetail = 0;
					List<HashMap<byte[], byte[]>> detailContents = new ArrayList<HashMap<byte[], byte[]>>();
					for(OrderFood food : _param.orderInfo.foods){
						//append the food to order detail if not belong any kitchen
						if(food.kitchen.aliasID != Kitchen.KITCHEN_NULL){
							
							if(food.isCombo()){
								for(Food childFood : food.childFoods){
									//assign the body to order detail
									byte[] detailBody = new OrderDetailContent(printTemplate, 
																			   food, 
																			   childFood,
																			   _param.orderInfo,
																			   _param.term,
																			   printType, 
																			   style
																			   ).toString().getBytes("GBK");
									
									//generate the header to order detail
									byte[] detailHeader = new byte[3];
									//assign the kitchen 
									detailHeader[0] = (byte)childFood.kitchen.aliasID;
									//assign the length of order detail to its header
									detailHeader[1] = (byte)((detailBody.length) & 0x000000FF);
									detailHeader[2] = (byte)(((detailBody.length) >> 8) & 0x000000FF);
									
									//assign the header and body to the content
									HashMap<byte[], byte[]> content = new HashMap<byte[], byte[]>(1);
									content.put(detailHeader, detailBody);
									detailContents.add(content);
									
									//sum the size to order detail
									totalOrderDetail += detailBody.length + detailHeader.length;
								}
								
							}else{
								
								//assign the body to order detail
								byte[] detailBody = new OrderDetailContent(printTemplate, 
																		   food,
																		   _param.orderInfo,
																		   _param.term,
																		   printType, 
																		   style
																		   ).toString().getBytes("GBK");
								
								//generate the header to order detail
								byte[] detailHeader = new byte[3];
								//assign the kitchen 
								detailHeader[0] = (byte)food.kitchen.aliasID;
								//assign the length of order detail to its header
								detailHeader[1] = (byte)((detailBody.length) & 0x000000FF);
								detailHeader[2] = (byte)(((detailBody.length) >> 8) & 0x000000FF);
								
								//assign the header and body to the content
								HashMap<byte[], byte[]> content = new HashMap<byte[], byte[]>(1);
								content.put(detailHeader, detailBody);
								detailContents.add(content);
								
								//sum the size to order detail
								totalOrderDetail += detailBody.length + detailHeader.length;
							}
						}
					}
					
					//allocate the memory for bytes to print
					itemBody = new byte[totalOrderDetail];
					int offset = 0;					
					for(HashMap<byte[], byte[]> content : detailContents){
						for(Map.Entry<byte[], byte[]> entry : content.entrySet()){
							//append the header to the bytes to print
							System.arraycopy(entry.getKey(), 0, itemBody, offset, entry.getKey().length);
							offset += entry.getKey().length;
							//append the body to the bytes print
							System.arraycopy(entry.getValue(), 0, itemBody, offset, entry.getValue().length);
							offset += entry.getValue().length;							
						}
					}					
									
				//handle the print request to receipt
				}else if(printType == Reserved.PRINT_RECEIPT || printType == Reserved.PRINT_TEMP_RECEIPT){

					itemBody = new ReceiptContent(_param.restaurant, 
													  printTemplate,
													  _param.orderInfo,
													  _param.term,
													  printType, 
													  style).toString().getBytes("GBK");
					
				}else if(printType == Reserved.PRINT_TRANSFER_TABLE){				

					itemBody = new TransTableContent(printTemplate, 
													 _param.orderInfo,
													 _param.term,
													 printType, 
													 style
													 ).toString().getBytes("GBK");
					
				}else if(printType == Reserved.PRINT_SHIFT_RECEIPT || 
						 printType == Reserved.PRINT_TEMP_SHIFT_RECEIPT || 
						 printType == Reserved.PRINT_DAILY_SETTLE_RECEIPT ||
						 printType == Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT ||
						 printType == Reserved.PRINT_HISTORY_SHIFT_RECEIPT){
					
					QueryShift.Result result;
					try{
						if(printType == Reserved.PRINT_DAILY_SETTLE_RECEIPT || printType == Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT){
							/**
							 * Get the details to daily settlement from history tables,
							 * since records to today has been moved to history before printing daily settlement receipt. 
							 */
							result = QueryShift.exec(_param.term, 
									   				 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.onDuty),
									   				 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.offDuty),
									   				 QueryShift.QUERY_HISTORY);
							
						}else if(printType == Reserved.PRINT_HISTORY_SHIFT_RECEIPT){
							result = QueryShift.exec(_param.term, 
	   				 				 			     new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.onDuty),
	   				 				 			     new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.offDuty),
	   				 				 			     QueryShift.QUERY_HISTORY);
			
						}else{
							result = QueryShift.exec(_param.term, 
					   				 				 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.onDuty),
					   				 				 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.offDuty),
					   				 				 QueryShift.QUERY_TODAY);
						}
						
					}catch(SQLException e){
						throw new PrintError(printType, "Fail to print shift receipt due to failed shift info query.");
					}
					
					itemBody = new ShiftContent(result, 
												printTemplate, 
												_param.orderInfo,
												_param.term,
												printType,
												style).toString().getBytes("GBK");
					
				}else{
					itemBody = new byte[0];
				}
				
				/**
				 * Generate the <print_item> to this style.
				 * Each <print_item> looks like below.
				 * <item_header> : <item_body>
				 * <item_header>
				 * style : region : order_id[4] : len : order_date : len[2]
				 * style - 1-byte indicating one of the printer style
				 * region - 1-byte indicating the region to this print request
				 * order_id[4] - 4-byte indicating the order id
				 * len - the length to order_date 
				 * order_date - the order date represented as string
				 * len[2] - 2-byte indicating the length of following print content
				 * <item_body>
				 * print_content - the print content
				 */				
				byte[] orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).getBytes("GBK");			

				int headerLen = 1 + /* style takes up 1 byte */
								1 + /* region takes up 1 byte */
								4 + /* order id takes up 4 bytes */
								1 + /* length of order date takes up 1 byte */
								orderDate.length  + /* length of value to order date */
								2  /* length of print content takes up 2 bytes */;
				byte[] itemHeader = new byte[headerLen];
				//assign the style
				itemHeader[0] = (byte)(style.intValue() & 0x000000FF);
				//assign the region
				itemHeader[1] = (byte)(_param.orderInfo.region.regionID);
				//assign the order id
				itemHeader[2] = (byte)(_param.orderInfo.id & 0x000000FF);
				itemHeader[3] = (byte)((_param.orderInfo.id & 0x0000FF00) >> 8);
				itemHeader[4] = (byte)((_param.orderInfo.id & 0x00FF0000) >> 16);
				itemHeader[5] = (byte)((_param.orderInfo.id & 0xFF000000) >> 24);
				//assign the length of order date
				itemHeader[6] = (byte)orderDate.length;
				//assign the value of order date
				System.arraycopy(orderDate, 0, itemHeader, 7, orderDate.length);
				//assign the length of print content
				itemHeader[7 + orderDate.length] = (byte)(itemBody.length & 0x000000FF);
				itemHeader[7 + orderDate.length + 1] = (byte)((itemBody.length >> 8) & 0x000000FF);
				//sum the total bytes
				totalBytes += itemHeader.length + itemBody.length;
				//append this style of print content
				printContent.put(itemHeader, itemBody);
			}
		}
		/**
		 * Generate the entire body for the print request, 
		 * the body consist of all the print items.
		 * The entire body looks like as below 
		 * <Print_Item_1> : <Print_Item_2> : ... : <Print_Item_n>
		 */
		byte[] body = new byte[totalBytes];
		int offset = 0;
		for(Map.Entry<byte[], byte[]> entry : printContent.entrySet()){
			byte[] itemHeader = entry.getKey();
			//assign the header of print content to the entire print request
			System.arraycopy(itemHeader, 0, body, offset, itemHeader.length);
			offset += itemHeader.length;
			
			byte[] itemBody = entry.getValue();
			//assign the body of print content to the entire print request
			System.arraycopy(itemBody, 0, body, offset, itemBody.length);
			offset += itemBody.length;
		}
		
		ProtocolPackage reqPrint = null;
		if(body.length != 0){
			reqPrint = new ReqPrintOrder(body, _param.orderInfo, printType);
		}		
		
		return reqPrint;

	}
	
}

final class PrintRequest{
	byte conf;
	ProtocolPackage pack;	
	
	PrintRequest(byte conf, ProtocolPackage pack){
		this.conf = conf;
		this.pack = pack;
	}
}



