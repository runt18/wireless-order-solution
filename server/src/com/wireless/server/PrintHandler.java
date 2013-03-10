package com.wireless.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPackage;
import com.wireless.print.type.TypeContent;
import com.wireless.protocol.ReqPrintOrder;
import com.wireless.protocol.Terminal;

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
public class PrintHandler implements Runnable{
	
	private Socket[] _socks;
	private Terminal _term;
	private List<TypeContent> _contents = new ArrayList<TypeContent>();
	
	public PrintHandler(Terminal term, Socket[] socks){
		if(term == null || socks == null){
			throw new IllegalArgumentException("The restaurant and socks passed can NOT be NULL.");
		}
		_term = term;
		_socks = socks;
	}
	
	public PrintHandler(Terminal term, Socket[] socks, TypeContent typeContent){
		this(term, socks);
		if(typeContent != null){
			_contents.add(typeContent);
		}
	}
	
	public PrintHandler(Terminal term, Socket[] socks, List<TypeContent> contents){
		this(term, socks);
		_contents.addAll(contents);
	}
	
	public PrintHandler addTypeContent(TypeContent typeContent){
		if(typeContent != null){
			_contents.add(typeContent);
		}
		return this;
	}
	
	/**
	 * Fire to execute print action in thread pool.
	 */
	public final void fireAsync(){
		if(!_contents.isEmpty()){
			WirelessSocketServer.threadPool.execute(this);
		}
	}
	
	/**
	 * Fire to execute print handler in the thread of caller.
	 */
	public final void fireSync(){
		run();
	}
	
	/**
	 * In this method, the print handler gets the template according to the print
	 * function code, send the print request and waits for an ACK (means successfully)
	 * or a NAK (means failed). If an IO error occurs, we close this connection and remove
	 * it from the printer connections.  
	 */
	public void run(){
		
		for(TypeContent content : _contents){
			
			boolean isPrintOk = false;
			
			ReqPackage reqPrint = new ReqPrintOrder(content.toBytes(), (byte)content.getPrintType().getVal());
			
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
						reqPrint.writeToStream(_socks[i].getOutputStream());
						
						//_socks[i].sendUrgentData(0);
						
						//any socket works means print OK
						isPrintOk = true;							
						
						//receive the response and timeout after 10s
						ProtocolPackage respPrint = new ProtocolPackage(); 
						respPrint.readFromStream(_socks[i].getInputStream(), 10 * 1000);
						
						//check whether the response's sequence equals to the request's
						if(respPrint.header.seq == reqPrint.header.seq){
							if(respPrint.header.mode != Mode.PRINT || respPrint.header.type != Type.ACK){
								throw new IOException("The Print Response(printType = " + content.getPrintType().getVal() + ")from printer server is not an ACK.");
							}							
						}
					}
					
				}catch(IOException e){
					e.printStackTrace();
					
					try{
						_socks[i].close();
						
					}catch(IOException e2){
						
					}finally{
						//remove the invalid socket
						synchronized(WirelessSocketServer.printerConnections){
							List<Socket> socks = WirelessSocketServer.printerConnections.get(_term.restaurantID);
							if(socks != null){
								socks.remove(_socks[i]);
								if(socks.size() == 0){
									WirelessSocketServer.printerConnections.remove(_term.restaurantID);
								}
							}
						}		
						_socks[i] = null;
					}		
				}
			}	
			
			if(!isPrintOk){
				storeLostPrintRequest(reqPrint);
			}
		}
	}
	
	private void storeLostPrintRequest(ReqPackage req){
		if(req != null){
			synchronized(WirelessSocketServer.printLosses){
				
				LinkedList<ProtocolPackage> printLosses = WirelessSocketServer.printLosses.get(_term.restaurantID);
	
				//Create a new list of the restaurant to store the lost print request if NOT exist before.
				if(printLosses == null){
					printLosses = new LinkedList<ProtocolPackage>();
					WirelessSocketServer.printLosses.put(_term.restaurantID, printLosses);
				}
				
				/*
				 * Add the lost print request to the list.
				 * and remove the first one if the requests exceed a specific amount. 
				 */
				printLosses.add(req);
				if(printLosses.size() > 50){
					printLosses.removeFirst();
				}
			}
		}
	}
	
	/**
	 * Store the unprinted requests to the list
	 * @param printType 
	 * 			the print type
	 * @param reqPrint
	 * 			the print request
	 */	
//	private void storePrintLosses(List<PrintRequest> printReqs){
//		synchronized(WirelessSocketServer.printLosses){
//			
//			LinkedList<ProtocolPackage> printLosses = WirelessSocketServer.printLosses.get(_param.restaurant.getId());
//			
//			Iterator<PrintRequest> iter = printReqs.iterator();
//			while(iter.hasNext()){
//				PrintRequest reqPrint = iter.next();
//				if(reqPrint != null){
//					if(printLosses != null){
//						/**
//						 * Insert the unprinted request to the list,
//						 * and remove the 1st one if the requests exceed a specific amount. 
//						 */
//						printLosses.add(reqPrint.pack);
//						if(printLosses.size() > 50){
//							printLosses.removeFirst();
//						}
//					}else{
//						/**
//						 * If the list does NOT exist before,
//						 * create a new link list to hold the unprinted request
//						 */
//						printLosses = new LinkedList<ProtocolPackage>();
//						printLosses.add(reqPrint.pack);
//						WirelessSocketServer.printLosses.put(_param.restaurant.getId(), printLosses);
//					}
//				}
//			}
//		}
//	}
	
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
//	private ProtocolPackage genPrintReq(PType printType) throws PrintError, UnsupportedEncodingException{
//		
//		//get the print templates according to the function code
//		HashMap<PStyle, String> templates = null;
//		if(printType == PType.PRINT_ORDER || printType == PType.PRINT_ALL_CANCELLED_FOOD || 
//				printType == PType.PRINT_ALL_EXTRA_FOOD || printType == PType.PRINT_ALL_HURRIED_FOOD){
//			//the template to order
//			templates = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER);
//			
//		}else if(printType == PType.PRINT_ORDER_DETAIL || printType == PType.PRINT_EXTRA_FOOD || 
//				printType == PType.PRINT_CANCELLED_FOOD || printType == PType.PRINT_HURRIED_FOOD){
//			//the template to order detail
//			templates = WirelessSocketServer.printTemplates.get(PType.PRINT_ORDER_DETAIL);
//			
//		}else if(printType == PType.PRINT_RECEIPT || printType == PType.PRINT_TEMP_RECEIPT){
//			//the template to receipt
//			templates = WirelessSocketServer.printTemplates.get(PType.PRINT_RECEIPT);
//			
//		}else if(printType == PType.PRINT_TRANSFER_TABLE){
//			//the template to table transfer
//			templates = WirelessSocketServer.printTemplates.get(PType.PRINT_TRANSFER_TABLE);
//			
//		}else if(printType == PType.PRINT_SHIFT_RECEIPT || printType == PType.PRINT_TEMP_SHIFT_RECEIPT || 
//				printType == PType.PRINT_DAILY_SETTLE_RECEIPT || printType == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT ||
//				printType == PType.PRINT_HISTORY_SHIFT_RECEIPT){
//			//the template to shift
//			templates = WirelessSocketServer.printTemplates.get(PType.PRINT_SHIFT_RECEIPT);
//		}
//		
//		HashMap<byte[], byte[]> printContent = new HashMap<byte[], byte[]>(); 
//		int totalBytes = 0;
//		//enumerate to get each style of print template to this function code 
//		Iterator<PStyle> iter = templates.keySet().iterator();
//		while(iter.hasNext()){	
//			PStyle style = iter.next();
//			byte[] itemBody = null;
//			String printTemplate = templates.get(style);
//			if(printTemplate != null){
//				
//				//handle the print request to order
//				if(printType == PType.PRINT_ORDER || printType == PType.PRINT_ALL_CANCELLED_FOOD || 
//						printType == PType.PRINT_ALL_EXTRA_FOOD || printType == PType.PRINT_ALL_HURRIED_FOOD){					
//					
//					
//					HashMap<Department, List<OrderFood>> foodsByDept = new HashMap<Department, List<OrderFood>>();
//					for(Department dept : _param.depts){
//						foodsByDept.put(dept, new ArrayList<OrderFood>());
//					}
//					foodsByDept.put(new Department("", Department.DEPT_ALL, _param.term.restaurantID, Department.TYPE_RESERVED), Arrays.asList(_param.orderToPrint.getOrderFoods()));					
//					
//					/**
//					 * Put the order foods to the corresponding department list. 
//					 */
//					for(OrderFood orderFood : _param.orderToPrint.getOrderFoods()){
//						List<OrderFood> foods = foodsByDept.get(orderFood.getKitchen().getDept());
//						if(foods != null){
//							foods.add(orderFood);
//							foodsByDept.put(orderFood.getKitchen().getDept(), foods);
//						}
//					}
//
//					int totalOrderBytes = 0;
//					/**
//					 * The hash map contains the order's header and body bytes to print of each department.
//					 */
//					HashMap<byte[], byte[]> bytesByDept = new HashMap<byte[], byte[]>();
//					
//					/**
//					 * Generate the content of order, the body looks like below.
//					 * <order_1> : <order_2> : ...
//					 * Each order looks like below.
//					 * dept : len[2] : content
//					 * dept - 1-byte indicating the department no
//					 * len[2] - 2-byte indicating the length of order content
//					 * content - the order content
//					 */		
//					OrderFood[] tmpFoods = _param.orderToPrint.getOrderFoods();
//					Iterator<Map.Entry<Department, List<OrderFood>>> iterFoodByDept = foodsByDept.entrySet().iterator();
//					while(iterFoodByDept.hasNext()){
//						Map.Entry<Department, List<OrderFood>> entry = iterFoodByDept.next();
//						Department dept = entry.getKey();
//						List<OrderFood> foods = entry.getValue();
//						/**
//						 * Remove the department list not containing any order foods.
//						 */
//						if(foods.size() == 0){
//							iterFoodByDept.remove();
//							
//						}else{		
//							_param.orderToPrint.setOrderFoods(foods.toArray(new OrderFood[foods.size()]));
//							byte[] body = new SummaryContent(dept, 
//															   printTemplate,  
//															   PFormat.RECEIPT_FORMAT_DEF, 
//															   _param.orderToPrint,
//															   _param.term.owner,
//															   printType, 
//															   style).toString().getBytes("GBK");
//							
//							//allocate the memory to header
//							byte[] header = new byte[3];	
//							//assign the department id
//							header[0] = (byte)dept.getId();
//							//assign the length of body
//							header[1] = (byte)(body.length & 0x000000FF);
//							header[2] = (byte)((body.length & 0x0000FF00) >> 8);
//							
//							bytesByDept.put(header, body);
//							
//							totalOrderBytes += header.length + body.length;
//									
//						}
//					}
//					_param.orderToPrint.setOrderFoods(tmpFoods);
//					
//					//allocate the memory to bytes to print 
//					itemBody = new byte[totalOrderBytes];
//					int offset = 0;
//					Iterator<Map.Entry<byte[], byte[]>> iterBytesByDept = bytesByDept.entrySet().iterator();
//					while(iterBytesByDept.hasNext()){
//						Map.Entry<byte[], byte[]> entry = iterBytesByDept.next();
//						byte[] header = entry.getKey();
//						byte[] body = entry.getValue();
//						//append the header to the bytes to print
//						System.arraycopy(header, 0, itemBody, offset, header.length);
//						offset += header.length;
//						//append the body to the bytes to print
//						System.arraycopy(body, 0, itemBody, offset, body.length);
//						offset += body.length;
//					}
//					
//					
//				//handle the print request to order detail and extra food detail
//				}else if(printType == PType.PRINT_ORDER_DETAIL || printType == PType.PRINT_EXTRA_FOOD || 
//						printType == PType.PRINT_CANCELLED_FOOD || printType == PType.PRINT_HURRIED_FOOD){
//					
//					/**
//					 * Generate the content of order detail, the body looks like below.
//					 * <order_detail_1> : <order_detail_2> : ...
//					 * Each order detail looks like below.
//					 * kitchen : len[2] : content
//					 * kitchen - 1-byte indicating the kitchen no
//					 * len[2] - 2-byte indicating the length of detail content
//					 * content - the order detail content
//					 */
//					int totalOrderDetail = 0;
//					List<HashMap<byte[], byte[]>> detailContents = new ArrayList<HashMap<byte[], byte[]>>();
//					for(OrderFood food : _param.orderToPrint.getOrderFoods()){
//						//append the food to order detail if not belong any kitchen
//						if(food.getKitchen().getAliasId() != Kitchen.KITCHEN_NULL){
//							
//							if(food.isCombo()){
//								for(Food childFood : food.getChildFoods()){
//									//assign the body to order detail
//									byte[] detailBody = new OrderDetailContent(printTemplate, 
//																			   food, 
//																			   childFood,
//																			   _param.orderToPrint,
//																			   _param.term.owner,
//																			   printType, 
//																			   style
//																			   ).toString().getBytes("GBK");
//									
//									//generate the header to order detail
//									byte[] detailHeader = new byte[3];
//									//assign the kitchen 
//									detailHeader[0] = (byte)childFood.getKitchen().getAliasId();
//									//assign the length of order detail to its header
//									detailHeader[1] = (byte)((detailBody.length) & 0x000000FF);
//									detailHeader[2] = (byte)(((detailBody.length) >> 8) & 0x000000FF);
//									
//									//assign the header and body to the content
//									HashMap<byte[], byte[]> content = new HashMap<byte[], byte[]>(1);
//									content.put(detailHeader, detailBody);
//									detailContents.add(content);
//									
//									//sum the size to order detail
//									totalOrderDetail += detailBody.length + detailHeader.length;
//								}
//								
//							}else{
//								
//								//assign the body to order detail
//								byte[] detailBody = new OrderDetailContent(printTemplate, 
//																		   food,
//																		   _param.orderToPrint,
//																		   _param.term.owner,
//																		   printType, 
//																		   style
//																		   ).toString().getBytes("GBK");
//								
//								//generate the header to order detail
//								byte[] detailHeader = new byte[3];
//								//assign the kitchen 
//								detailHeader[0] = (byte)food.getKitchen().getAliasId();
//								//assign the length of order detail to its header
//								detailHeader[1] = (byte)((detailBody.length) & 0x000000FF);
//								detailHeader[2] = (byte)(((detailBody.length) >> 8) & 0x000000FF);
//								
//								//assign the header and body to the content
//								HashMap<byte[], byte[]> content = new HashMap<byte[], byte[]>(1);
//								content.put(detailHeader, detailBody);
//								detailContents.add(content);
//								
//								//sum the size to order detail
//								totalOrderDetail += detailBody.length + detailHeader.length;
//							}
//						}
//					}
//					
//					//allocate the memory for bytes to print
//					itemBody = new byte[totalOrderDetail];
//					int offset = 0;					
//					for(HashMap<byte[], byte[]> content : detailContents){
//						for(Map.Entry<byte[], byte[]> entry : content.entrySet()){
//							//append the header to the bytes to print
//							System.arraycopy(entry.getKey(), 0, itemBody, offset, entry.getKey().length);
//							offset += entry.getKey().length;
//							//append the body to the bytes print
//							System.arraycopy(entry.getValue(), 0, itemBody, offset, entry.getValue().length);
//							offset += entry.getValue().length;							
//						}
//					}					
//									
//				//handle the print request to receipt
//				}else if(printType == PType.PRINT_RECEIPT || printType == PType.PRINT_TEMP_RECEIPT){
//
//					itemBody = new ReceiptContent(0,
//												  _param.restaurant, 
//												  printTemplate,
//												  _param.orderToPrint,
//												  _param.term.owner,
//												  printType, 
//												  style).toString().getBytes("GBK");
//					
//				}else if(printType == PType.PRINT_TRANSFER_TABLE){				
//
//					itemBody = new TransTableContent(printTemplate, 
//													 0,
//													 _param.srcTbl,
//													 _param.destTbl,
//													 _param.term.owner,
//													 printType, 
//													 style
//													 ).toString().getBytes("GBK");
//					
//				}else if(printType == PType.PRINT_SHIFT_RECEIPT || 
//						 printType == PType.PRINT_TEMP_SHIFT_RECEIPT || 
//						 printType == PType.PRINT_DAILY_SETTLE_RECEIPT ||
//						 printType == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT ||
//						 printType == PType.PRINT_HISTORY_SHIFT_RECEIPT){
//					
//					ShiftDetail result;
//					try{
//						if(printType == PType.PRINT_DAILY_SETTLE_RECEIPT || printType == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT){
//							/**
//							 * Get the details to daily settlement from history tables,
//							 * since records to today has been moved to history before printing daily settlement receipt. 
//							 */
//							result = QueryShiftDao.exec(_param.term, 
//									   				 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.onDuty),
//									   				 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.offDuty),
//									   				 QueryShiftDao.QUERY_HISTORY);
//							
//						}else if(printType == PType.PRINT_HISTORY_SHIFT_RECEIPT){
//							result = QueryShiftDao.exec(_param.term, 
//	   				 				 			     new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.onDuty),
//	   				 				 			     new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.offDuty),
//	   				 				 			     QueryShiftDao.QUERY_HISTORY);
//			
//						}else{
//							result = QueryShiftDao.exec(_param.term, 
//					   				 				 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.onDuty),
//					   				 				 new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(_param.offDuty),
//					   				 				 QueryShiftDao.QUERY_TODAY);
//						}
//						
//					}catch(SQLException e){
//						throw new PrintError((byte)printType.getVal(), "Fail to print shift receipt due to failed shift info query.");
//					}
//					
//					itemBody = new ShiftContent(result, 
//												printTemplate, 
//												_param.term.owner,
//												printType,
//												style).toString().getBytes("GBK");
//					
//				}else{
//					itemBody = new byte[0];
//				}
//				
//				/**
//				 * Generate the <print_item> to this style.
//				 * Each <print_item> looks like below.
//				 * <item_header> : <item_body>
//				 * <item_header>
//				 * style : region : order_id[4] : len : order_date : len[2]
//				 * style - 1-byte indicating one of the printer style
//				 * region - 1-byte indicating the region to this print request
//				 * order_id[4] - 4-byte indicating the order id
//				 * len - the length to order_date 
//				 * order_date - the order date represented as string
//				 * len[2] - 2-byte indicating the length of following print content
//				 * <item_body>
//				 * print_content - the print content
//				 */				
//				byte[] orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).getBytes("GBK");			
//
//				int headerLen = 1 + /* style takes up 1 byte */
//								1 + /* region takes up 1 byte */
//								4 + /* order id takes up 4 bytes */
//								1 + /* length of order date takes up 1 byte */
//								orderDate.length  + /* length of value to order date */
//								2  /* length of print content takes up 2 bytes */;
//				byte[] itemHeader = new byte[headerLen];
//				//assign the style
//				itemHeader[0] = (byte)(style.getVal() & 0x000000FF);
//				//assign the region
//				itemHeader[1] = (byte)(_param.orderToPrint.getRegion().getRegionId());
//				//assign the order id
//				int orderId = _param.orderToPrint.getId();
//				itemHeader[2] = (byte)(orderId & 0x000000FF);
//				itemHeader[3] = (byte)((orderId & 0x0000FF00) >> 8);
//				itemHeader[4] = (byte)((orderId & 0x00FF0000) >> 16);
//				itemHeader[5] = (byte)((orderId & 0xFF000000) >> 24);
//				//assign the length of order date
//				itemHeader[6] = (byte)orderDate.length;
//				//assign the value of order date
//				System.arraycopy(orderDate, 0, itemHeader, 7, orderDate.length);
//				//assign the length of print content
//				itemHeader[7 + orderDate.length] = (byte)(itemBody.length & 0x000000FF);
//				itemHeader[7 + orderDate.length + 1] = (byte)((itemBody.length >> 8) & 0x000000FF);
//				//sum the total bytes
//				totalBytes += itemHeader.length + itemBody.length;
//				//append this style of print content
//				printContent.put(itemHeader, itemBody);
//			}
//		}
//		/**
//		 * Generate the entire body for the print request, 
//		 * the body consist of all the print items.
//		 * The entire body looks like as below 
//		 * <Print_Item_1> : <Print_Item_2> : ... : <Print_Item_n>
//		 */
//		byte[] body = new byte[totalBytes];
//		int offset = 0;
//		for(Map.Entry<byte[], byte[]> entry : printContent.entrySet()){
//			byte[] itemHeader = entry.getKey();
//			//assign the header of print content to the entire print request
//			System.arraycopy(itemHeader, 0, body, offset, itemHeader.length);
//			offset += itemHeader.length;
//			
//			byte[] itemBody = entry.getValue();
//			//assign the body of print content to the entire print request
//			System.arraycopy(itemBody, 0, body, offset, itemBody.length);
//			offset += itemBody.length;
//		}
//		
//		ProtocolPackage reqPrint = null;
//		if(body.length != 0){
//			reqPrint = new ReqPrintOrder(body, (byte)printType.getVal());
//		}		
//		
//		return reqPrint;
//
//	}
//	
}




