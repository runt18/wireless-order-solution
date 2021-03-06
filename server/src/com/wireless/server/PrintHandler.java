package com.wireless.server;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

import com.wireless.db.printScheme.PrintLossDao;
import com.wireless.db.printScheme.PrinterConnectionDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ErrorCode;
import com.wireless.exception.ErrorType;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintOrder;
import com.wireless.pack.req.RequestPackage;
import com.wireless.pack.resp.RespACK;
import com.wireless.pack.resp.RespNAK;
import com.wireless.pack.resp.RespPackage;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.printScheme.PrintLoss;
import com.wireless.pojo.printScheme.PrinterConnection;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.print.content.Content;
import com.wireless.print.content.ContentParcel;
import com.wireless.sccon.ServerConnector;

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
public class PrintHandler {
	
	private static class ReqPrintDispatch extends RequestPackage{
		protected ReqPrintDispatch(Staff staff, ContentParcel dispatchedContent) {
			super(staff);
			header.mode = Mode.PRINT;
			header.type = Type.PRINT_DISPATCH_CONTENT;
			fillBody(dispatchedContent, 0);
		}
	}
	
	private final Staff _staff;
	
	public PrintHandler(Staff staff){
		_staff = staff;
	}
	
	/**
	 * Process the print content using local & remote connections in asynchronous,
	 * the content would become lost if both local and remote procession is failed.
	 * @param contents
	 * 			the print contents
	 */
	public void process(final List<Content> contents){
		for(final Content content : contents){
			process(content);
		}
	}
	
	/**
	 * Process the print content using local & remote connections in asynchronous,
	 * the content would become lost if both local and remote procession is failed.
	 * @param content the print content {@link Content}
	 */
	public void process(final Content content){
		if(content != null){
			WirelessSocketServer.threadPool.submit(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					boolean isLocalOk = processLocal(content);
					
					boolean isRemoteOk = processRemote(content);
					
					//Make the content to be lost if both local and remote is failed.
					if(!isLocalOk && !isRemoteOk){
						processFail(content);
					}
					
					return null;
				}
			});
		}
	}
	
	/**
	 * Process the dispatched content with local connections in synchronized,
	 * the dispatcher (another socket server) would deal with it according to the response received from this dispatch procession.
	 * @param request
	 * @param content
	 * 			the dispatched content
	 * @return RespACK {@link RespACK} if ok, otherwise RespNAK {@link RespNAK}
	 */
	public RespPackage processDispatch(final ProtocolPackage request, final ContentParcel content){
		if(content != null){
			if(processLocal(content)){
				return new RespACK(request.header);
			}else{
				return new RespNAK(request.header);
			}
		}else{
			return new RespACK(request.header);
		}
	}
	
	/**
	 * Process the lost content with only local connections in asynchronous,
	 * the content would become lost again if the procession is failed.
	 * @param content
	 * 			the lost content to process {@link Content}
	 */
	public void processLost(final Content content){
		if(content != null){
			WirelessSocketServer.threadPool.submit(new Callable<Void>(){
				@Override
				public Void call() throws Exception {
					if(!processLocal(content)){
						processFail(content);
					}
					return null;
				}
			});
		}
	}
	
	/**
	 * Process the lost contents with only local connections in asynchronous,
	 * the content would become lost again if the procession is failed.
	 * @param contents
	 * 			the lost contents to process
	 */
	public void processLost(final List<Content> contents){
		WirelessSocketServer.threadPool.submit(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				for(Content content : contents){
					if(!processLocal(content)){
						processFail(content);
					}
				}
				return null;
			}
		});
	}
	
	private void processFail(Content content){
		try {
			PrintLossDao.insert(_staff, new PrintLoss.InsertBuilder(content.toBytes()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private boolean processLocal(Content content){
		boolean isPrintOk = false;
		
		final List<Socket> socks = PrinterConnections.instance().get(_staff.getRestaurantId());
		if(socks.isEmpty()){
			try {
				PrinterConnectionDao.deleteLocal(_staff);
			} catch (UnknownHostException | SQLException e) {
				e.printStackTrace();
			}

		}else{

			RequestPackage reqPrint = new ReqPrintOrder(content.toBytes());
			
			for(Socket sock : socks){
				if(sock == null){
					continue;
				}
				try{						
					/**
					 * Since socket would be shared with the print actions to the same restaurant,
					 * make the communication to the socket in synchronized mode.
					 */
					synchronized(sock){
						
						//send the print request
						reqPrint.writeToStream(sock.getOutputStream());
						
						//any socket works means print OK
						isPrintOk = true;							
						
						//receive the response and timeout after 10s
						ProtocolPackage respPrint = new ProtocolPackage(); 
						respPrint.readFromStream(sock.getInputStream(), 10 * 1000);
						
						//check whether the response's sequence equals to the request's
						if(respPrint.header.seq == reqPrint.header.seq){
							if(respPrint.header.mode != Mode.PRINT || respPrint.header.type != Type.ACK){
								throw new BusinessException("The response from printer server is not an ACK.");
							}							
						}
					}
					
				}catch(IOException | BusinessException e){
					e.printStackTrace();
					
					try{
						sock.close();
					}catch(IOException ignored){
						
					}finally{
						//Remove the invalid socket.
						PrinterConnections.instance().remove(_staff.getRestaurantId(), sock);
					}		
				}
			}	
		}
		
		
		return isPrintOk;
	}

	private boolean processRemote(final Content content){
		boolean isPrintOk = false;
		try{
			//Get the remote printer connections except the local.
			final List<PrinterConnection> remoteConnections = PrinterConnectionDao.getByCond(_staff, new PrinterConnectionDao.ExtraCond4ExcludeLocal());
			
			if(remoteConnections.isEmpty()){
				isPrintOk = false;
				
			}else{
				final List<Callable<ProtocolPackage>> dispatchTasks = new ArrayList<>();
				/*
				 * Dispatch the print content to other remote printer connections, and wait for the response in synchronize respectively.
				 * And printer connection works means print OK. 
				 */
				for(final PrinterConnection connection : remoteConnections){
					dispatchTasks.add(new Callable<ProtocolPackage>(){
						@Override
						public ProtocolPackage call() throws Exception {
							ProtocolPackage response =  ServerConnector.instance().ask(new ServerConnector.Connector(connection.getDest(), WirelessSocketServer.socket_listen), 
																  new ReqPrintDispatch(_staff, new ContentParcel(content)), 10 * 1000);
							
							//Dispatch again if the failure NOT due to IO error.
							if(response.header.type == Type.NAK){
								if(new Parcel(response.body).readParcel(ErrorCode.CREATOR).getType() != ErrorType.IO_ERROR){
									return ServerConnector.instance().ask(new ServerConnector.Connector(connection.getDest(), WirelessSocketServer.socket_listen), 
																	      new ReqPrintDispatch(_staff, new ContentParcel(content)), 5 * 1000);
								}else{
									return response;
								}
								
							}else{
								return response;
							}
						}
					});
				}
				
				if(dispatchTasks.isEmpty()){
					isPrintOk = true;
				}else{
					CompletionService<ProtocolPackage> dispatchService = new ExecutorCompletionService<>(WirelessSocketServer.threadPool);
					for(Callable<ProtocolPackage> task : dispatchTasks){
						dispatchService.submit(task);
					}
					
					for(int i = 0; i < dispatchTasks.size(); i++){
						try {
							//Any printer connection works means print OK. 
							ProtocolPackage resp = dispatchService.take().get();
							if(resp.header.type == Type.ACK){
								isPrintOk = true;
								break;
							}
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}
				}	
			}
		
		}catch(SQLException e){
			e.printStackTrace();
		}
		
		return isPrintOk;
	}
}




