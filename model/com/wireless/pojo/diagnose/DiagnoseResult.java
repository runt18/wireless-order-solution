package com.wireless.pojo.diagnose;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class DiagnoseResult implements Parcelable, Jsonable{

	private int nConnections;
	private final List<PrinterDiagnose> printerDiagnose = new ArrayList<PrinterDiagnose>();
	
	public DiagnoseResult(int nConnections){
		this.nConnections = nConnections;
	}
	
	public void addPrinter(PrinterDiagnose printerDiagnose){
		this.printerDiagnose.add(printerDiagnose);
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(nConnections);
		dest.writeParcelList(printerDiagnose, 0);
	}

	@Override
	public void createFromParcel(Parcel source) {
		nConnections = source.readInt();
		printerDiagnose.addAll(source.readParcelList(PrinterDiagnose.CREATOR));
	}
	
	public final static Parcelable.Creator<DiagnoseResult> CREATOR = new Parcelable.Creator<DiagnoseResult>(){
		@Override
		public DiagnoseResult newInstance() {
			return new DiagnoseResult(0);
		}
		@Override
		public DiagnoseResult[] newInstance(int size){
			return new DiagnoseResult[size];
		}
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("connectionAmount", nConnections);
		jm.putJsonableList("printers", printerDiagnose, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}

	@Override
	public String toString(){
		return JSONObject.toJSONString(this.toJsonMap(0));
	}
}
