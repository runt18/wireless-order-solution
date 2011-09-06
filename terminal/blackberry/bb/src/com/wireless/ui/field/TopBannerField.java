package com.wireless.ui.field;

import java.util.Timer;  
import java.util.TimerTask;  
import net.rim.device.api.i18n.SimpleDateFormat;  
import net.rim.device.api.system.DeviceInfo;  
import net.rim.device.api.system.Display;  
import net.rim.device.api.system.RadioInfo;  
import net.rim.device.api.ui.DrawStyle;  
import net.rim.device.api.ui.Field;  
import net.rim.device.api.ui.Font;  
import net.rim.device.api.ui.FontFamily;  
import net.rim.device.api.ui.Graphics;  
  
public class TopBannerField extends Field implements DrawStyle {  
    private Font _headerFont = SmallFont();  
    private SimpleDateFormat _timeFormat = new SimpleDateFormat("h:mma");  
    private SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd");  
    private String _timeString;  
    private String _dateString;  
    private String _title;  
    private boolean _showSignal;  
    private boolean _showBattery;  
    private boolean _showDate;  
    private boolean _showTime;  
    private boolean _showTitle;  
    private int _fieldWidth;  
    private int _fieldHeight;  
    private int _fontColour;  
    private int _backgroundColour;  
    private int _batteryBackground;  
    private Timer _headerTimer;  
    private TimerTask _headerTask;  
    
    public TopBannerField(String title) {  
        super(Field.NON_FOCUSABLE);  
        _title = title;  
        _showSignal = true;  
        _showBattery = true;  
        _showDate = false;  
        _showTime = true;  
        _showTitle = true;  
        _fieldHeight = 20;  
        _fieldWidth = Display.getWidth();  
        _fontColour = 0xFFFFFF;  
        _backgroundColour = 0x00000;  
        _batteryBackground = 0x999999;   

    }  
  
    /**
     * Start the timer task while the field is pushed into the display stack
     */
    protected void onDisplay(){
    	if(_headerTimer == null && _headerTask == null){
            _headerTask = new TimerTask() {  
                public void run() {  
                    invalidate();  
                }  
            };  
            _headerTimer = new Timer();
            _headerTimer.scheduleAtFixedRate(_headerTask, 500, 30000); 
    	}
    }
    
    /**
     * Turn off the timer task while the field pop off.
     */
    protected void onUndisplay(){
    	if(_headerTimer != null){
    		_headerTimer.cancel();
    		_headerTask.cancel();
    		_headerTimer = null;
    		_headerTask = null;
    	}
    }
    
    public void setTitle(String title) {  
        _title = title;  
        invalidate();  
    }  
  
    public void setFontColour(int fontColour) {  
        _fontColour = fontColour;  
        invalidate();  
    }  
  
    public void setBatteryBackground(int batteryBackground) {  
        _batteryBackground = batteryBackground;  
        invalidate();  
    }  
  
    public void setBackgroundColour(int backgroundColour) {  
        _backgroundColour = backgroundColour;  
        invalidate();  
    }  
  
    public void showSignal(boolean bool) {  
        _showSignal = bool;  
        invalidate();  
    }  
  
    public void showBattery(boolean bool) {  
        _showBattery = bool;  
        invalidate();  
    }  
  
    public void showDate(boolean bool) {  
        _showDate = bool;  
        invalidate();  
    }  
  
    public void showTime(boolean bool) {  
        _showTime = bool;  
        invalidate();  
    }  
  
    public void showTitle(boolean bool) {  
        _showTitle = bool;  
        invalidate();  
    }  
  
    protected void layout(int width, int height) {  
        setExtent(getPreferredWidth(), getPreferredHeight());  
    }  
  
    public int getPreferredWidth() {  
        return _fieldWidth;  
    }  
  
    public int getPreferredHeight() {  
        return _fieldHeight;  
    }  
  
    protected void paint(Graphics graphics) {  
        graphics.setFont(_headerFont);  
        int graphicsDiff = 0;  
  
        graphics.setColor(_backgroundColour);  
        graphics.fillRect(0, 0, this.getPreferredWidth(), this.getPreferredHeight());  
  
        if (_showSignal) {  
            graphicsDiff = graphicsDiff + 28;  
            graphics.setColor(0x999999);  
            // draw blank background  
            graphics.fillRect(this.getPreferredWidth() - 26, 8, 6, 4);  
            graphics.fillRect(this.getPreferredWidth() - 21, 6, 6, 6);  
            graphics.fillRect(this.getPreferredWidth() - 16, 4, 6, 8);  
            graphics.fillRect(this.getPreferredWidth() - 11, 2, 6, 10);  
            graphics.fillRect(this.getPreferredWidth() - 6, 0, 6, 12);  
            int signalLevel = RadioInfo.getSignalLevel();  
            if (signalLevel > -77) {  
                // 5 bands  
                graphics.setColor(0xCCFFFF);  
                graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);  
                graphics.fillRect(this.getPreferredWidth() - 20, 7, 4, 4);  
                graphics.fillRect(this.getPreferredWidth() - 15, 5, 4, 6);  
                graphics.fillRect(this.getPreferredWidth() - 10, 3, 4, 8);  
                graphics.fillRect(this.getPreferredWidth() - 5, 1, 4, 10);  
            } else if (signalLevel > -86) {  
                // 4 bands  
                graphics.setColor(0xCCFFFF);  
                graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);  
                graphics.fillRect(this.getPreferredWidth() - 20, 7, 4, 4);  
                graphics.fillRect(this.getPreferredWidth() - 15, 5, 4, 6);  
                graphics.fillRect(this.getPreferredWidth() - 10, 3, 4, 8);  
            } else if (signalLevel > -92) {  
                // 3 bands  
                graphics.setColor(0xCCFFFF);  
                graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);  
                graphics.fillRect(this.getPreferredWidth() - 20, 7, 4, 4);  
                graphics.fillRect(this.getPreferredWidth() - 15, 5, 4, 6);  
            } else if (signalLevel > -101) {  
                // 2 bands  
                graphics.setColor(0xCCFFFF);  
                graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);  
                graphics.fillRect(this.getPreferredWidth() - 20, 7, 4, 4);  
            } else if (signalLevel > -120) {  
                // 1 band  
                graphics.setColor(0xCCFFFF);  
                graphics.fillRect(this.getPreferredWidth() - 25, 9, 4, 2);  
            }  
        }  
        if (_showBattery) {  
  
            int batteryLevel = DeviceInfo.getBatteryLevel();  
            graphics.setColor(_batteryBackground);  
            graphics.fillRect(this.getPreferredWidth() - 23 - graphicsDiff, 2,  
                    20, 8);  
            graphics.fillRect(this.getPreferredWidth() - 3 - graphicsDiff, 4,  
                    1, 4);  
            if (batteryLevel > 75) {  
                graphics.setColor(0x28f300);  
            } else if (batteryLevel > 50) {  
                graphics.setColor(0x91dc00);  
            } else if (batteryLevel > 25) {  
                graphics.setColor(0xefec00);  
            } else {  
                graphics.setColor(0xff2200);  
            }  
            double powerLong = ((18.00 / 100) * batteryLevel);  
            int power = (int) powerLong;  
            graphics.fillRect(this.getPreferredWidth() - 22 - graphicsDiff, 3,  
                    power, 6);  
            graphicsDiff = graphicsDiff + 24;  
        }  
  
        graphics.setColor(_fontColour);  
  
        if (_showTime) {  
            _timeString = " "  
                    + _timeFormat.formatLocal(System.currentTimeMillis()) + " ";  
        } else {  
            _timeString = "";  
        }  
  
        if (_showDate) {  
            _dateString = " "  
                    + _dateFormat.formatLocal(System.currentTimeMillis()) + " ";  
        } else {  
            _dateString = "";  
        }  
  
        graphics.drawText(_dateString + _timeString, 
        				 this.getPreferredWidth() - 
        				 _headerFont.getAdvance(_dateString + _timeString) - 
        				 graphicsDiff, 1);  
  
        if (_showTitle) {  
            int limit = 0;  
            if (_showSignal)  
                limit = limit + 28;  
            if (_showBattery)  
                limit = limit + 25;  
            if (_showTime)  
                limit = limit + _headerFont.getAdvance(_timeString);  
            if (_showDate)  
                limit = limit + _headerFont.getAdvance(_dateString);  
            if (_headerFont.getAdvance(_title) > this.getPreferredWidth() - limit) {  
                int elippsy = _headerFont.getAdvance("...");  
                int availableWidth = this.getPreferredWidth() - limit - elippsy;  
                String _txt = "";  
                String shorterTitle = "";  
                for (int i = 1; i < _title.length(); i++) {  
                    _txt = _title.substring(0, i);  
                    if (_headerFont.getAdvance(_txt) < availableWidth) {  
                        shorterTitle = _txt;  
                    }  
                }  
                _title = shorterTitle + "...";  
            }  
            graphics.drawText(_title, 1, 0);  
        }  
    }  
  
    public static Font SmallFont() {  
        try {  
            FontFamily theFam = FontFamily.forName("SYSTEM");  
            return theFam.getFont(Font.PLAIN | Font.BOLD, 14);  
        } catch (ClassNotFoundException ex) {  
            ex.printStackTrace();  
        }  
        return null;  
    }  
}  
