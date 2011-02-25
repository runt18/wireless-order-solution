
/**
 * LoginFaultException.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5.4  Built on : Dec 19, 2010 (08:18:42 CET)
 */

package com.wireless.wirelessorder;

public class LoginFaultException extends java.lang.Exception{
    
    private com.wireless.wirelessorder.LoginFault faultMessage;

    
        public LoginFaultException() {
            super("LoginFaultException");
        }

        public LoginFaultException(java.lang.String s) {
           super(s);
        }

        public LoginFaultException(java.lang.String s, java.lang.Throwable ex) {
          super(s, ex);
        }

        public LoginFaultException(java.lang.Throwable cause) {
            super(cause);
        }
    

    public void setFaultMessage(com.wireless.wirelessorder.LoginFault msg){
       faultMessage = msg;
    }
    
    public com.wireless.wirelessorder.LoginFault getFaultMessage(){
       return faultMessage;
    }
}
    