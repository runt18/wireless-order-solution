package com.wireless.db;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.mchange.v2.c3p0.ConnectionCustomizer;
import com.wireless.pojo.util.DateUtil;

public class VerboseConnectionCustomizer implements ConnectionCustomizer
{
	@Override
    public void onAcquire( Connection c, String pdsIdt ){ 
       System.out.println(new SimpleDateFormat(DateUtil.Pattern.DATE_TIME.getPattern(), Locale.getDefault()).format(new Date()) + " - Acquired " + c + " [" + pdsIdt + "]"); 
    }
	
	@Override
    public void onDestroy( Connection c, String pdsIdt ){
		System.out.println(new SimpleDateFormat(DateUtil.Pattern.DATE_TIME.getPattern(), Locale.getDefault()).format(new Date()) + " - Destroying " + c + " [" + pdsIdt + "]"); 
	}
	
	@Override
    public void onCheckOut( Connection c, String pdsIdt ){ 
		System.out.println(new SimpleDateFormat(DateUtil.Pattern.DATE_TIME.getPattern(), Locale.getDefault()).format(new Date()) + " - Checked out " + c + " [" + pdsIdt + "]"); 
	}
	
	@Override
    public void onCheckIn( Connection c, String pdsIdt ){
		System.out.println(new SimpleDateFormat(DateUtil.Pattern.DATE_TIME.getPattern(), Locale.getDefault()).format(new Date()) + " - Checking in " + c + " [" + pdsIdt + "]"); 
	}
}