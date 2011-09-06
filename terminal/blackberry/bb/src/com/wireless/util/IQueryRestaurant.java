package com.wireless.util;

import com.wireless.protocol.ProtocolPackage;

public interface IQueryRestaurant {
	public void preQueryRestaurant();
	public void passQueryRestaurant(ProtocolPackage resp);
	public void failQueryRestuarant(ProtocolPackage resp, String errMsg);
	public void postQueryRestaurant();
}
