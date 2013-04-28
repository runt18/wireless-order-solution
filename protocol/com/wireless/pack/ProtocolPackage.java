package com.wireless.pack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;
 
public class ProtocolPackage {
	
	private final static int INTERVAL_TO_WAIT_FOR_READ = 200;
	
	public ProtocolHeader header;					//the header of the package
	public byte[] body;								//the body of the package
	public final static byte[] EOP = {'\r', '\n'};	//the flag indicating the end of the package
	
	public ProtocolPackage(){
		header = new ProtocolHeader();
//		body = new byte[0];
	}
	
	public ProtocolPackage(ProtocolHeader header, Parcelable[] parcelableArray, int flag){
		this.header = header;
		fillBody(parcelableArray, flag);
	}
	
	public ProtocolPackage(ProtocolHeader header, Parcelable parcelable, int flag){
		this.header = header;
		fillBody(parcelable, flag);
	}
	
	public ProtocolPackage(ProtocolHeader header, Parcel parcel){
		this.header = header;
		fillBody(parcel);
	}

	/**
	 * Fill body according to a list of parcelable objects.
	 * @param parcelableArray The source that is a list of parcelable objects to marshal.
	 * @param flag Additional flags about how each object should be written.
	 */
	protected void fillBody(List<? extends Parcelable> parcelableList, int flag){
		if(parcelableList != null){
			Parcel p = new Parcel();
			p.writeParcelList(parcelableList, flag);
			this.body = p.marshall();
		}else{
			this.body = new byte[0];
		}
	}
	
	/**
	 * Fill body according to an array of parcelable objects.
	 * @param parcelableArray The source that is an array of parcelable objects to marshal.
	 * @param flag Additional flags about how each object should be written.
	 */
	protected void fillBody(Parcelable[] parcelableArray, int flag){
		if(parcelableArray != null){
			Parcel p = new Parcel();
			p.writeParcelArray(parcelableArray, flag);
			this.body = p.marshall();
		}else{
			this.body = new byte[0];
		}
	}
	
	/**
	 * Fill body according to a parcelable object.
	 * @param parcelable The source which is a parcelable object to marshal.
	 * @param flag Additional flags about how the object should be written.
	 */
	protected void fillBody(Parcelable parcelable, int flag){
		if(parcelable != null){
			Parcel p = new Parcel();
			parcelable.writeToParcel(p, flag);
			this.body = p.marshall();
		}else{
			this.body = new byte[0];
		}
	}
	
	/**
	 * Fill body by a parcel.
	 * @param parcel the source which is a parcel to marshal.
	 */
	protected void fillBody(Parcel parcel){
		if(parcel != null){
			this.body = parcel.marshall();
		}else{
			this.body = new byte[0];
		}
	}
	
	/**
	 * Write order protocol package to output stream.
	 * 
	 * @param out
	 *            the output steam to write the data
	 * @throws IOException
	 *             Throws if any error occurs while sending package
	 */
	public void writeToStream(OutputStream out) throws IOException{
		
		//make the request and send it to server
		byte[] bytesToSend = new byte[ProtocolHeader.SIZE +
		                              body.length +
		                              ProtocolPackage.EOP.length];
		
		//assign the header to request buffer
		bytesToSend[0] = header.mode;
		bytesToSend[1] = header.type;
		bytesToSend[2] = header.seq;
		bytesToSend[3] = header.reserved;
		bytesToSend[4] = header.pin[0];
		bytesToSend[5] = header.pin[1];
		bytesToSend[6] = header.pin[2];
		bytesToSend[7] = header.pin[3];
		bytesToSend[8] = header.pin[4];
		bytesToSend[9] = header.pin[5];
		bytesToSend[10] = (byte)(body.length & 0x000000FF);
		bytesToSend[11] = (byte)((body.length & 0x0000FF00) >> 8);
		
		//assign the body to request buffer
		System.arraycopy(body, 0, bytesToSend, ProtocolHeader.SIZE, body.length);
		
		//assign the EOP to request buffer
		System.arraycopy(ProtocolPackage.EOP, 0, bytesToSend, ProtocolHeader.SIZE + body.length, ProtocolPackage.EOP.length);
		
		//send the buffer
		out.write(bytesToSend, 0, bytesToSend.length);
		out.flush();
	}
	
	/**
	 * Read the data from the input stream.
	 * 
	 * @param in
	 *            the input stream to read the data
	 * @param timeout
	 *            read time out, 0 means no time out
	 * @return the package parsed by wireless order protocol
	 * @throws IOException
	 *             Throws if any error occurs while reading data from socket or
	 *             the received data doesn't reach the EOF, or timeout
	 */
	public void readFromStream(InputStream in, long timeout) throws IOException{
		
		byte[] bytesToReceive = new byte[0];
		
		boolean isReachEOP = false;
		/*
		 * Check if the received bytes is available every 200ms.
		 * And read the bytes until reach the EOP or timeout.
		 */
		for(int i = 0; i <= timeout / INTERVAL_TO_WAIT_FOR_READ; i++){
			int bytesAvail = in.available();
			if(bytesAvail == 0){
				try{
					Thread.sleep(INTERVAL_TO_WAIT_FOR_READ);
				}catch(InterruptedException e){}
				
			}else{
				
				byte[] buf = new byte[bytesAvail];
				int bytesToRead = in.read(buf);
				/*
				 * Note that the bytes available may not equal to bytes to actual receive sometimes.
				 * At least bytes available is less than bytes to receive.
				 */
				if(bytesToReceive.length > 0){
					byte[] tmp = bytesToReceive;
					bytesToReceive = new byte[tmp.length + bytesToRead];
					System.arraycopy(tmp, 0, bytesToReceive, 0, tmp.length);
					System.arraycopy(buf, 0, bytesToReceive, tmp.length, bytesToRead);
				}else{
					bytesToReceive = new byte[bytesToRead];
					System.arraycopy(buf, 0, bytesToReceive, 0, bytesToRead);
				}
				
				isReachEOP = true;
				// Check if receiving the EOP from the response.
				for(i = 0; i < ProtocolPackage.EOP.length; i++){
					if(bytesToReceive[bytesToReceive.length - i - 1] != ProtocolPackage.EOP[ProtocolPackage.EOP.length - i - 1]){
						isReachEOP = false;
						break;
					}
				}				
				// If the response ends with EOP, means the response is complete,
				// and exit the loop.
				if(isReachEOP){
					break;
				}
			}
		}
		
		
		if(isReachEOP){
			
			//Assign protocol header
			header.mode = bytesToReceive[0];
			header.type = bytesToReceive[1];
			header.seq = bytesToReceive[2];
			header.reserved = bytesToReceive[3];
			header.pin[0] = bytesToReceive[4];
			header.pin[1] = bytesToReceive[5];
			header.pin[2] = bytesToReceive[6];
			header.pin[3] = bytesToReceive[7];
			header.pin[4] = bytesToReceive[8];
			header.pin[5] = bytesToReceive[9];
			header.length[0] = bytesToReceive[10];
			header.length[1] = bytesToReceive[11];
			
			//Assign the protocol body.
			//Note that the body exclude EOP
			body = new byte[bytesToReceive.length - ProtocolHeader.SIZE - ProtocolPackage.EOP.length];
			System.arraycopy(bytesToReceive, ProtocolHeader.SIZE, body, 0, body.length);
			
			//Check if expected request length(calculated by length field to header) is equal to the actual length.				
			if(!isLengthMatched()){
				throw new IOException("The length calculated by header length field doesn't match the its body length.");
			}
			
		}else{
			throw new IOException("The received package doesn't reach the EOP.");
		}
	}
	
	/**
	 * Check if expected length calculated by header field is matched with the actual length calculated by body.
	 * Note that just compared against lower 2-byte of actual body length,
	 * since the header field only use 2-byte to indicate the length of body.
	 * @return true if matched, otherwise false
	 */
	public boolean isLengthMatched(){
		int expectedLen = (header.length[0] & 0x000000FF) | ((header.length[1] & 0x000000FF) << 8);
		/*
		 * Just compared against lower 2-byte of actual body length 
		 * since the header field only use 2-byte to indicate the length of body.
		 */
		int actualLen = body.length & 0x0000FFFF;
		return expectedLen == actualLen;
	}
}
