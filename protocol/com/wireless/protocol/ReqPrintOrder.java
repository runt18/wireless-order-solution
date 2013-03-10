package com.wireless.protocol;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pack.req.ReqPackage;

/******************************************************
 * Design the print order request looks like below
 * <Header>
 * mode : type : seq : reserved[4] : pin[6] : len[2] : print_content
 * mode - PRINT
 * type - PRINT_BILL
 * seq - auto calculated and filled in
 * reserved[1..3] - 0x00
 * reserved[0] - one of the print functions 
 * pin[6] - auto calculated and filled in
 * len[2] - length of the <Body>
 * <Body>
 * print_content - the print content
 *******************************************************/
public class ReqPrintOrder extends ReqPackage{
	/**
	 * In the constructor, use order information to replace the template's variables. 
	 * @param printContent the print content 
	 * @param printFunc one of the print function values
	 */
	public ReqPrintOrder(byte[] printContent, byte printFunc){
		header.mode = Mode.PRINT;
		header.type = Type.PRINT_BILL;
		header.reserved = printFunc;
		header.length[0] = (byte)(printContent.length & 0x000000FF);
		header.length[1] = (byte)((printContent.length & 0x0000FF00) >> 8);
		this.body = printContent;

	}
}
