package com.wireless.server;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.wireless.db.printScheme.PrinterConnectionDao;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pack.Mode;
import com.wireless.pack.ProtocolPackage;
import com.wireless.pack.Type;
import com.wireless.pack.req.RequestPackage;
import com.wireless.pack.resp.RespNAK;
import com.wireless.pack.resp.RespPackage;
import com.wireless.parcel.Parcel;
import com.wireless.pojo.diagnose.PrinterDiagnose;
import com.wireless.pojo.diagnose.DiagnoseResult;
import com.wireless.pojo.printScheme.Printer;
import com.wireless.pojo.printScheme.PrinterConnection;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.sccon.ServerConnector;

public class PrinterDiagnoseHandler {

	private static class ReqPrinterDiagnose extends RequestPackage{

		ReqPrinterDiagnose(Staff staff, final Printer printer) {
			super(staff);
			this.header.mode = Mode.DIAGNOSIS;
			this.header.type = Type.PRINTER;
			this.body = JSONObject.toJSONString(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("printer_name", printer.getName());
					return jm;
				}
				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
			}.toJsonMap(0)).toString().getBytes();
		}
		
	}
	
	private final Staff staff;
	
	public PrinterDiagnoseHandler(Staff staff){
		this.staff = staff;
	}
	
	public ProtocolPackage processDispatch(ProtocolPackage request) throws IOException, SQLException, BusinessException{
		DiagnoseResult result = processLocal();
		return result != null ? new RespPackage(request.header).fillBody(result, 0) : new RespNAK(request.header);
	}
	
	public ProtocolPackage process(ProtocolPackage request) throws SQLException, IOException, BusinessException{
		final List<PrinterConnection> connections = PrinterConnectionDao.getByCond(staff, null);
		if(connections.isEmpty()){
			return new RespNAK(request.header);
		}else{
			DiagnoseResult result = processLocal();
			if(result == null){
				result = processRemote();
				return result != null ? new RespPackage(request.header).fillBody(result, 0) : new RespNAK(request.header);
			}else{
				return new RespPackage(request.header).fillBody(result, 0);
			}
		}
	}
	
	private DiagnoseResult processLocal() throws IOException, SQLException, BusinessException{
		final List<Socket> localConnections = PrinterConnections.instance().get(staff.getRestaurantId());
		for(Socket sock : localConnections){
			DiagnoseResult result = new DiagnoseResult(PrinterConnectionDao.getByCond(staff, null).size());
			for(Printer printer : PrinterDao.getByCond(staff, new PrinterDao.ExtraCond().setEnabled(true))){
				new ReqPrinterDiagnose(staff, printer).writeToStream(sock.getOutputStream());
				
				ProtocolPackage response = new ProtocolPackage(); 
				response.readFromStream(sock.getInputStream(), 30 * 1000);
				
				PrinterDiagnose printerResult = JObject.parse(PrinterDiagnose.JSON_CREATOR, 0, new String(response.body, 0, response.body.length - 1));
				printerResult.setPrinter(printer);
				result.addPrinter(printerResult);
			}
			//System.out.println(result);
			return result;
		}
		return null;
	}
	
	private DiagnoseResult processRemote() throws SQLException, IOException, BusinessException{
		final List<PrinterConnection> remoteConnections = PrinterConnectionDao.getByCond(staff, new PrinterConnectionDao.ExtraCond4ExcludeLocal());
		for(PrinterConnection remote : remoteConnections){
			ProtocolPackage resp = ServerConnector.instance().ask(new ServerConnector.Connector(remote.getDest(), WirelessSocketServer.socket_listen),
										   						  new RequestPackage(staff, Mode.DIAGNOSIS, Type.PRINTER_DISPATCH), 10 * 1000);
			if(resp.header.type == Type.ACK){
				return new Parcel(resp.body).readParcel(DiagnoseResult.CREATOR);
			}else{
				return null;
			}
		}
		return null;
	}
}
