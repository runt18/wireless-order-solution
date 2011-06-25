package com.wireless.ui.networkset;

public interface IPostNetworkSet{
	/**
	 * Perform the action after network setting. 
	 * @param isDirty indicates whether the network parameters is modified
	 */
	public void postNetworkSet(boolean isDirty);
}