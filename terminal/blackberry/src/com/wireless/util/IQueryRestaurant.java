package com.wireless.util;

import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.Restaurant;

public interface IQueryRestaurant {
	public void preQueryRestaurant();
	public void passQueryRestaurant(ProtocolPackage resp, Restaurant info);
	public void failQueryRestuarant(ProtocolPackage resp, String errMsg);
	public void postQueryRestaurant();
}
