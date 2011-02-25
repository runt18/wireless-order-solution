
/**
 * WirelessOrderSkeleton.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.4  Built on : Dec 19, 2010 (08:18:42 CET)
 */
    package com.wireless.wirelessorder;
    /**
     *  WirelessOrderSkeleton java skeleton for the axisService
     */
    public class WirelessOrderSkeleton{
        
         
        /**
         * Auto generated method signature
         * 
                                     * @param login
             * @throws LoginFaultException : 
         */
        
                 public com.wireless.wirelessorder.LoginResponse login
                  (
                  com.wireless.wirelessorder.Login login
                  )
            throws LoginFaultException{
                	 LoginResponse resp = new LoginResponse();
                	 resp.setToken(com.wireless.wirelessorder.impl.Login.perform(login.getUser(), login.getPwd()));
                	 return resp;
        }
     
    }
    