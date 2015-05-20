package com.wireless.pojo.diagnose;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.printScheme.Printer;

public class PrinterDiagnose implements Parcelable, Jsonable{

	//flag to offline status
	private final int PRINTER_OFFLINE_FLAG_9100 = 1 << 3;
	private final int COVER_OPEN_FLAG_9100 = 1 << (2 + 8);
	private final int PAPER_END_FLAG_9100 = 1 << (5 + 8);
	//flag to error status
	private final int CUTTER_ERROR_FLAG_9100 = 1 << (3 + 16);
	
	private final int PRINTER_OFFLINE_FLAG_4000 = 1 << 3;
	private final int COVER_OPEN_FLAG_4000 = 1 << 5;
	private final int PAPER_END_FLAG_4000 = 0x03 << 2;
	private final int CUTTER_ERROR_FLAG_4000 = 1 << 3;
	
	private String printerName;
	private String printerAlias;
	private String printerPort;
	private String gateway;
	private int status4000;
	private int status9100;
	private boolean pingOk;
	private boolean driverOk;
	
	public boolean isOffline(){
		if(status9100 != 0){
			return (status9100 & PRINTER_OFFLINE_FLAG_9100) != 0; 
		}else if(status4000 != 0){
			return (status4000 & PRINTER_OFFLINE_FLAG_4000) != 0; 
		}else{
			return false;
		}
	}
	
	public boolean isCoverOpen(){
		if(status9100 != 0){
			return (status9100 & COVER_OPEN_FLAG_9100) != 0; 
		}else if(status4000 != 0){
			return (status4000 & COVER_OPEN_FLAG_4000) != 0;
		}else{
			return false;
		}
	}
	
	public boolean isPaperEnd(){
		if(status9100 != 0){
			return (status9100 & PAPER_END_FLAG_9100) != 0; 
		}else if(status4000 != 0){
			return (status4000 >> 16 & PAPER_END_FLAG_4000) == 0x0C;
		}else{
			return false;
		}
	}
	
	public boolean isCutterError(){
		if(status9100 != 0){
			return (status9100 & CUTTER_ERROR_FLAG_9100) != 0; 
		}else if(status4000 != 0){
			return (status4000 >> 8 & CUTTER_ERROR_FLAG_4000) != 0;
		}else{
			return false;
		}
	}
	
	public void setPrinter(Printer printer){
		this.printerName = printer.getName();
		this.printerAlias = printer.getAlias();
	}
	
	public String getPrinterName(){
		if(this.printerName == null){
			return "";
		}
		return this.printerName;
	}
	
	public String getPrinterAlias(){
		if(this.printerAlias == null){
			return "";
		}
		return this.printerAlias;
	}
	
	public String getPrinterPort(){
		if(this.printerPort == null){
			return "";
		}
		return this.printerPort;
	}
	
	public String getGateway(){
		if(this.gateway == null){
			return "";
		}
		return this.gateway;
	}
	
	public boolean isPingOk(){
		return this.pingOk;
	}
	
	public boolean isDriverOk(){
		return this.driverOk;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(printerName);
		dest.writeString(printerAlias);
		dest.writeString(printerPort);
		dest.writeString(gateway);
		dest.writeInt(status4000);
		dest.writeInt(status9100);
		dest.writeBoolean(pingOk);
		dest.writeBoolean(driverOk);
	}

	@Override
	public void createFromParcel(Parcel source) {
		this.printerName = source.readString();
		this.printerAlias = source.readString();
		this.printerPort = source.readString();
		this.gateway = source.readString();
		this.status4000 = source.readInt();
		this.status9100 = source.readInt();
		this.pingOk = source.readBoolean();
		this.driverOk = source.readBoolean();
	}
	
	public final static Parcelable.Creator<PrinterDiagnose> CREATOR = new Parcelable.Creator<PrinterDiagnose>(){
		@Override
		public PrinterDiagnose newInstance() {
			return new PrinterDiagnose();
		}
		@Override
		public PrinterDiagnose[] newInstance(int size){
			return new PrinterDiagnose[size];
		}
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("printerName", this.printerName);
		jm.putString("printerAlias", this.printerAlias);
		jm.putString("printerPort", this.printerPort);
		jm.putString("gateway", this.gateway);
		jm.putBoolean("driver", this.driverOk);
		jm.putBoolean("ping", this.pingOk);
		jm.putBoolean("offline", this.isOffline());
		jm.putBoolean("coverOpen", this.isCoverOpen());
		jm.putBoolean("paperEnd", this.isPaperEnd());
		jm.putBoolean("cutterError", this.isCutterError());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		if(jm.containsKey("gateway")){
			this.gateway = jm.getString("gateway");
		}
		if(jm.containsKey("printer_port")){
			this.printerPort = jm.getString("printer_port");
		}
		if(jm.containsKey("ping")){
			this.pingOk = jm.getInt("ping") == 1;
		}
		if(jm.containsKey("driver")){
			this.driverOk = jm.getInt("driver") == 1;
		}
		if(jm.containsKey("status_4000")){
			this.status4000 = jm.getInt("status_4000");
		}
		if(jm.containsKey("status_9100")){
			this.status9100 = jm.getInt("status_9100");
		}

	}
	
	public static Jsonable.Creator<PrinterDiagnose> JSON_CREATOR = new Jsonable.Creator<PrinterDiagnose>() {
		@Override
		public PrinterDiagnose newInstance() {
			return new PrinterDiagnose();
		}
	};
	
	@Override
	public String toString(){
		return JSONObject.toJSONString(this.toJsonMap(0));
	}
}
