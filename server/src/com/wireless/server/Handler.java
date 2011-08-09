package com.wireless.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import com.wireless.protocol.ProtocolHeader;
import com.wireless.protocol.ProtocolPackage;


public abstract class Handler {
	/**
	 * Read the data from the socket connect, and the data would be parsed using 
	 * wireless order protocol.
	 * @param in the input stream to read the data
	 * @param timeout read time out, 0 means no time out 
	 * @return the package parsed by wireless order protocol
	 * @throws IOException if any error occurs while reading data from socket or the received
	 *                     data doesn't reach the EOF, or timeout
	 */	
	protected ProtocolPackage recv(InputStream in, long timeout) throws IOException{
		//check if receiving the response every 200ms
		//use the member variable to control the timeout times
		ArrayList<byte[]> reqBuf = new ArrayList<byte[]>();
		boolean isReachEOP = false;
		for(int i = 0; i <= timeout / 200; i++){
			int bytes_avail = in.available();
			if(bytes_avail == 0){
				try{
					Thread.sleep(200);
				}catch(InterruptedException e){}
				
			}else{
				byte[] rec_buf = new byte[bytes_avail];
				final int bytes_read = in.read(rec_buf);
				reqBuf.add(rec_buf);
				isReachEOP = true;
				//check if receiving the EOP from the response
				for(i = 0; i < ProtocolPackage.EOP.length; i++){
					if(rec_buf[bytes_read - i - 1] != ProtocolPackage.EOP[ProtocolPackage.EOP.length - i - 1]){
						isReachEOP = false;
						break;
					}
				}					
				if(isReachEOP){
					break;
				}
			}
		}
		
		//in the case receive the response with the EOP,
		//means the response is complete
		if(isReachEOP){
			byte[] req = null;
			if(reqBuf.size() == 1){
				//in the case there is only one fragment,
				//just get it from the request vector
				req = reqBuf.get(0);
			}else{
				//in the case there is more than one fragments,
				//need to incorporate these fragments into one request buffer
				int index = 0;
				Iterator<byte[]> it = reqBuf.iterator();
				//calculate the length of the request
				while(it.hasNext()){
					index += it.next().length;
				}
				//allocate the memory for request buffer
				req = new byte[index];
				//assign each request fragment into one request buffer
				it = reqBuf.iterator();
				index = 0;
				while(it.hasNext()){
					byte[] tmp = it.next();
					System.arraycopy(tmp, 0, req, index, tmp.length);
					index += tmp.length;
				}
			}
			
			ProtocolPackage recPack = new ProtocolPackage();
			//parse the request buffer into protocol header
			recPack.header.mode = req[0];
			recPack.header.type = req[1];
			recPack.header.seq = req[2];
			recPack.header.reserved = req[3];
			recPack.header.pin[0] = req[4];
			recPack.header.pin[1] = req[5];
			recPack.header.pin[2] = req[6];
			recPack.header.pin[3] = req[7];
			recPack.header.pin[4] = req[8];
			recPack.header.pin[5] = req[9];
			recPack.header.length[0] = req[10];
			recPack.header.length[1] = req[11];					
			//parse the request buffer into protocol body
			//note that the body's length exclude the EOP
			recPack.body = new byte[req.length - ProtocolHeader.SIZE - ProtocolPackage.EOP.length];
			for(int i = 0; i < recPack.body.length; i++){
				recPack.body[i] = req[ProtocolHeader.SIZE + i];
			}
			
			return recPack;
			
		}else{
			throw new IOException("The received package doesn't reach the EOP.");
		}
	}
	
	/**
	 * Write the wireless order protocol package to socket
	 * @param out the output steam to write the data
	 * @param sendPack the package to send
	 * @throws IOException if any error occurs while sending package
	 */
	protected void send(OutputStream out, ProtocolPackage sendPack) throws IOException{
		if(out != null && sendPack != null){
			//calculate the length of response
			int respLen = ProtocolHeader.SIZE + sendPack.body.length + ProtocolPackage.EOP.length;
			//allocate the send buffer
			byte[] send_buf = new byte[respLen];
			//assign the header to send buffer
			send_buf[0] = sendPack.header.mode;
			send_buf[1] = sendPack.header.type;
			send_buf[2] = sendPack.header.seq;
			send_buf[3] = sendPack.header.reserved;
			send_buf[4] = sendPack.header.pin[0];
			send_buf[5] = sendPack.header.pin[1];
			send_buf[6] = sendPack.header.pin[2];
			send_buf[7] = sendPack.header.pin[3];
			send_buf[8] = sendPack.header.pin[4];
			send_buf[9] = sendPack.header.pin[5];
			send_buf[10] = (byte)(sendPack.body.length & 0x000000FF);
			send_buf[11] = (byte)((sendPack.body.length & 0x0000FF00) >> 8);
			//assign the body to send buffer
			for(int i = 0; i < sendPack.body.length; i++){
				send_buf[ProtocolHeader.SIZE + i] = sendPack.body[i];
			}
			//assign the EOP to send buffer
			for(int i = 0; i < ProtocolPackage.EOP.length; i++){
				send_buf[ProtocolHeader.SIZE + sendPack.body.length + i] = ProtocolPackage.EOP[i];
			}
			out.write(send_buf);
			out.flush();
		}
	}
}
