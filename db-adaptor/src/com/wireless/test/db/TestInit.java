package com.wireless.test.db;

import java.beans.PropertyVetoException;

import com.wireless.db.DBCon;
import com.wireless.pojo.oss.OSSParams;
import com.wireless.pojo.oss.OssImage;
import com.wireless.sccon.ServerConnector;

public final class TestInit {
	
	public static void init() throws PropertyVetoException{
		DBCon.init("192.168.33.100", "3306", "wireless_order_db", "root", "HelloZ315", false);
		OssImage.Params.init("digie-image-test", OSSParams.init("KMLtoTwkG5Jqaapu", "VZtrdLaO6WFcJQrvffO9XBPVpbKGRP", "oss.aliyuncs.com", "oss.aliyuncs.com"));
		ServerConnector.instance().setMaster(new ServerConnector.Connector("localhost", 55555));
	}
}
