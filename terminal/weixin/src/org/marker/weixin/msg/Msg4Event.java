package org.marker.weixin.msg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Msg4Event extends Msg {
	
	public static enum Event{
		UNKNOWN("what's it?"),
		SUBSCRIBE("subscribe"),
		UNSUBSCRIBE("unsubscribe"),
		CLICK("CLICK"),
		LOCATION("LOCATION"),
		SCAN_WAIT_MSG("scancode_waitmsg"),
		SCAN_PUSH("scancode_push");
		
		private final String val;
		
		Event(String val){
			this.val = val;
		}
		
		public static Event valueOf(String val, int flag){
			for(Event event : values()){
				if(event.val.equalsIgnoreCase(val)){
					return event;
				}
			}
			return UNKNOWN;
		}
		
		@Override
		public String toString(){
			return val;
		}
	}
	
	private Event event;
	private String eventKey;
	private String ticket;
	private String latitude;
	private String longitude;
	private String precision;
	private String scanType;
	private String scanResult;
	
	
	public Msg4Event(Msg4Head head) {
		super(head);
	}

	public void write(Document document) {
	}

	public void read(Document document) {
		this.event = Event.valueOf(getElementContent(document, "Event"), 0);
		if(event == Event.SUBSCRIBE || event == Event.UNSUBSCRIBE) {
			this.eventKey = getElementContent(document, "EventKey");
			this.ticket = getElementContent(document, "Ticket");
		} else if (event == Event.LOCATION) {
			this.latitude = getElementContent(document, "Latitude");
			this.longitude = getElementContent(document, "Longitude");
			this.precision = getElementContent(document, "Precision");
		} else if (event == Event.CLICK) {
			this.eventKey = getElementContent(document, "EventKey");
		}else if(event == Event.SCAN_PUSH || event == Event.SCAN_WAIT_MSG){
			this.eventKey = getElementContent(document, "EventKey");
			NodeList nl = document.getElementsByTagName("ScanCodeInfo");
			if(nl != null){
				this.scanType = ((Element)nl.item(0)).getElementsByTagName("ScanType").item(0).getFirstChild().getNodeValue();
				this.scanResult = ((Element)nl.item(0)).getElementsByTagName("ScanResult").item(0).getFirstChild().getNodeValue();
			}
		}
	}

	public Event getEvent() {
		return this.event;
	}

	public void setEvent(String event) {
		this.event = Event.valueOf(event, 0);
	}

	public String getEventKey() {
		if(eventKey == null){
			return "";
		}
		return this.eventKey;
	}

	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	public String getTicket() {
		if(ticket == null){
			return "";
		}
		return this.ticket;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public String getLatitude() {
		return this.latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return this.longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getPrecision() {
		return this.precision;
	}

	public void setPrecision(String precision) {
		this.precision = precision;
	}
	
	public String getScanType(){
		if(this.scanType == null){
			return "";
		}
		return this.scanType;
	}
	
	public String getScanResult(){
		if(this.scanResult == null){
			return "";
		}
		return this.scanResult;
	}
}
