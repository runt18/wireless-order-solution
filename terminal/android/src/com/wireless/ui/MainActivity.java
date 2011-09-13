package com.wireless.ui;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;

import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.PinGen;
import com.wireless.protocol.ProtocolPackage;
import com.wireless.protocol.ReqPackage;
import com.wireless.protocol.ReqQueryMenu;
import com.wireless.protocol.RespParser;
import com.wireless.protocol.Terminal;
import com.wireless.sccon.ServerConnector;
import com.wireless.ui.main.R;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServerConnector.instance().setNetAddr("125.88.20.194");
        ServerConnector.instance().setNetPort(55555);
        ReqPackage.setGen(new PinGen(){
			@Override
			public int getDeviceId() {
				// TODO Auto-generated method stub
				return 0x2100000A;
			}

			@Override
			public short getDeviceType() {
				// TODO Auto-generated method stub
				return Terminal.MODEL_BB;
			}
        	
        });
        ProtocolPackage resp;
        FoodMenu foodMenu;
        try{
        	resp = ServerConnector.instance().ask(new ReqQueryMenu());
            foodMenu = RespParser.parseQueryMenu(resp);
        }catch(IOException e){
        	
        }
        setContentView(R.layout.main);

    }
}