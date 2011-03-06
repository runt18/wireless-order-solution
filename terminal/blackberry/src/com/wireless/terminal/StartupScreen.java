package com.wireless.terminal;


import com.wireless.ui.field.*;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.*;

public class StartupScreen extends MainScreen{
	

	public StartupScreen(){
	    int displayWidth = Display.getWidth();
	    int displayHeight = Display.getHeight();
	    int fieldSpacerSize = displayHeight / 24;
	    Bitmap splashLogo = Bitmap.getBitmapResource("splash-logo.png");
	    int throbberSize = displayWidth / 4;
	    int fontHeight = Font.getDefault().getHeight();
	    int spacerSize = (displayHeight / 2) - ((splashLogo.getHeight() + throbberSize + fontHeight) / 2) - fieldSpacerSize;
	    if(spacerSize < 0) { spacerSize = 0; }

	    add(new BlankSeparatorField(spacerSize));
	    add(new BitmapField(splashLogo, Field.FIELD_HCENTER));
	    add(new BlankSeparatorField(fieldSpacerSize));
	    add(new ThrobberField(throbberSize, Field.FIELD_HCENTER));
	    add(new BlankSeparatorField(fieldSpacerSize));
	    
	    StringBuffer buf = new StringBuffer();
	 
	    add(new LabelField(buf.toString(), Field.FIELD_HCENTER));
	}

	protected void onUiEngineAttached(boolean attached){
		if(attached){
			//here wait until the startup is done
			ApplicationManager myApp = ApplicationManager.getApplicationManager();
			while(myApp.inStartup()) {
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					// Catch Exception
				}
			}
			UiApplication.getApplication().invokeLater(new Runnable(){
				public void run(){
					close();					
				}
			}, 1000, false);
		}else{
			UiApplication.getApplication().invokeLater(new Runnable(){
				public void run(){
					UiApplication.getApplication().requestForeground();
					UiApplication.getUiApplication().pushScreen(new OrderMainScreen());
				}
			});
		}
	}
}


