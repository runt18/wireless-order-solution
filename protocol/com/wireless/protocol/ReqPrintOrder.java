package com.wireless.protocol;

/******************************************************
 * Design the print order request looks like below
 * <Header>
 * mode : type : seq : reserved : pin[6] : len[2] : print_content
 * mode - PRINT
 * type - PRINT_BILL
 * seq - auto calculated and filled in
 * reserved - PRINT_ORDER
 * pin[6] - auto calculated and filled in
 * len[2] - length of the <Body>
 * <Body>
 * print_content - the print content
 *******************************************************/
public class ReqPrintOrder extends ReqPrintPackage{
	/**
	 * In the constructor, use order information to replace the template's variables. 
	 * @param printContent the print content 
	 * @param orderInfo the order information used to replace the template's variable
	 * @param printFunc one of the print function values
	 */
	public ReqPrintOrder(byte[] printContent, Order orderInfo, byte printFunc){
		header.mode = Mode.PRINT;
		header.type = Type.PRINT_BILL;
		header.reserved = printFunc;
		header.length[0] = (byte)(printContent.length & 0x000000FF);
		header.length[1] = (byte)((printContent.length & 0x0000FF00) >> 8);
		this.body = printContent;

	}
}
