package com.wireless.server;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.wireless.db.printScheme.PrintLossDao;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPrintOrder;
import com.wireless.pack.req.RequestPackage;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.printScheme.PrintLoss;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.print.content.Content;
import com.wireless.server.PrintHandler.PrintResult;

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
public class PrintHandler implements Callable<PrintResult>{
	
//	private static class ContentParcel implements Parcelable{
//
//		final static int CONTENT_PARCELABLE_SIMPLE = 0;
//		final static int CONTENT_APRCELABLE_COMPLEX = 1;
//		
//		private final Content mContent;
//		
//		ContentParcel(Content content){
//			mContent = content;
//		}
//		
//		@Override
//		public void writeToParcel(Parcel dest, int flag) {
//			dest.writeByte(flag);
//			if(flag == CONTENT_PARCELABLE_SIMPLE){
//				dest.writeInt(mContent.getId());
//			}
//		}
//
//		@Override
//		public void createFromParcel(Parcel source) {
//			// TODO Auto-generated method stub
//			
//		}
//		
//		public Content asContent(){
//			return mContent;
//		}
//		
//	}
	
	public static class PrintResult implements Parcelable{

		private boolean isOk;
		
		public boolean isOk(){
			return isOk;
		}
		
		@Override
		public void writeToParcel(Parcel dest, int flag) {
			dest.writeBoolean(this.isOk);
		}

		@Override
		public void createFromParcel(Parcel source) {
			this.isOk = source.readBoolean();
		}
		
	}
	
	private final Staff _staff;
	private final List<Content> _contents = new ArrayList<Content>();
	
	public PrintHandler(Staff staff){
		if(staff == null){
			throw new IllegalArgumentException("The staff passed can NOT be NULL.");
		}
		_staff = staff;
	}
	
	public PrintHandler(Staff staff, Content content){
		this(staff);
		if(content != null){
			_contents.add(content);
		}
	}
	
	public PrintHandler(Staff staff, List<Content> contents){
		this(staff);
		_contents.addAll(contents);
	}
	
	public PrintHandler addContent(Content content){
		if(content != null){
			_contents.add(content);
		}
		return this;
	}
	
	/**
	 * Fire to execute print action in thread pool.
	 */
	public final void fireAsync(){
		if(!_contents.isEmpty()){
			WirelessSocketServer.threadPool.submit(this);
		}
	}
	
	/**
	 * Fire to execute print handler in the thread of caller.
	 */
	public final PrintResult fireSync(){
		return call();
	}
	
	/**
	 * In this method, the print handler gets the template according to the print
	 * function code, send the print request and waits for an ACK (means successfully)
	 * or a NAK (means failed). If an IO error occurs, we close this connection and remove
	 * it from the printer connections.  
	 */
	@Override
	public PrintResult call(){
		
		//PrintResult result = new PrintResult();
		
		for(Content content : _contents){
			
			boolean isPrintOk = false;
			
			RequestPackage reqPrint = new ReqPrintOrder(content.toBytes());
			
			for(Socket sock : PrinterConnections.instance().get(_staff.getRestaurantId())){
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
								throw new IOException("The response from printer server is not an ACK.");
							}							
						}
					}
					
				}catch(IOException e){
					e.printStackTrace();
					
					try{
						sock.close();
					}catch(IOException ignored){
						
					}finally{
						//Remove the invalid socket.
						PrinterConnections.instance().remove(_staff.getRestaurantId(), sock);
						sock = null;
					}		
				}
			}	
			
			//Store the content if print failed.
			if(!isPrintOk){
				try {
					PrintLossDao.insert(_staff, new PrintLoss.InsertBuilder(content.toBytes()));
				} catch (SQLException e) {
					e.printStackTrace();
				}
				//PrinterLosses.instance().add(_staff.getRestaurantId(), content);
			}
		}
		
		return null;
	}
	
}




