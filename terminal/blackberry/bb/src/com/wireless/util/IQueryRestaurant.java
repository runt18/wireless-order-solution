package com.wireless.util;

import com.wireless.pack.ProtocolPackage;

public interface IQueryRestaurant {
	public void preQueryRestaurant();
	public void passQueryRestaurant(ProtocolPackage resp);
	public void failQueryRestuarant(ProtocolPackage resp, String errMsg);
	public void postQueryRestaurant();
}
