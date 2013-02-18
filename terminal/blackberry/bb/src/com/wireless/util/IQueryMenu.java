package com.wireless.util;

import com.wireless.pack.ProtocolPackage;

public interface IQueryMenu {
	public void preQueryMenu();
	public void passMenu(ProtocolPackage resp);
	public void failMenu(ProtocolPackage resp, String errMsg);
	public void postQueryMenu();
}
