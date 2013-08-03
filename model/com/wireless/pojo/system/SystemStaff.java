package com.wireless.pojo.system;

public class SystemStaff {
	private long id;
	private int restaurantID;
	private int aliasID;
	private String name;
	private String pwd;
	private int terminalID;
	private Terminal terminal;
	
	public int getTerminalID() {
		return terminalID;
	}
	public void setTerminalID(int terminalID) {
		this.terminalID = terminalID;
	}
	public long getPin() {
		return this.terminal == null ? 0 : this.terminal.getPin();
	}
	public void setPin(long pin) {
		if(this.terminal != null)
			this.terminal.setPin(pin);
	}
	
	public Terminal getTerminal() {
		return terminal;
	}
	public void setTerminal(Terminal terminal) {
		this.terminal = terminal;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public int getAliasID() {
		return aliasID;
	}
	public void setAliasID(int aliasID) {
		this.aliasID = aliasID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
}
