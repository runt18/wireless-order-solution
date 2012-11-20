package com.wireless.terminal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.browser.field.BrowserContent;
import net.rim.device.api.browser.field.BrowserContentChangedEvent;
import net.rim.device.api.browser.field.Event;
import net.rim.device.api.browser.field.RedirectEvent;
import net.rim.device.api.browser.field.RenderingApplication;
import net.rim.device.api.browser.field.RenderingException;
import net.rim.device.api.browser.field.RenderingSession;
import net.rim.device.api.browser.field.RequestedResource;
import net.rim.device.api.browser.field.UrlRequestedEvent;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.StringUtilities;

public class OTAUpdateScreen extends MainScreen implements RenderingApplication{
	
    private static String REFERER = "referer";      
    private RenderingSession _renderingSession = RenderingSession.getNewInstance();   
    private HttpConnection  _currentConnection;    
	   
    public OTAUpdateScreen(String netAPN){
    	//_renderingSession = RenderingSession.getNewInstance();
    	String url = "http://61.145.9.186:10080/ota/WirelessOrderTerminal.jad;deviceside=true";
		if(netAPN.length() != 0){
			url = url + ";apn=" + netAPN;
		}
    	PrimaryResourceFetchThread thread = new PrimaryResourceFetchThread(url, null, null, null, this);
        thread.start();    
    }
    
    /**
     * Processes an http connection
     * 
     * @param connection The connection to the web content
     * @param e The event triggering the connection
     */
    void processConnection(HttpConnection connection, Event e) 
    {
        // Cancel previous request
        if(_currentConnection != null){
            try{
                _currentConnection.close();
            } 
            catch (IOException e1){}
        }
        
        _currentConnection = connection;
        
        BrowserContent browserContent = null;
        
        try{
            browserContent = _renderingSession.getBrowserContent(connection, this, e);
            
            if(browserContent != null){
                Field field = browserContent.getDisplayableContent();
                
                if(field != null){
                    synchronized(Application.getEventLock()){
                        deleteAll();
                        add(field);
                    }
                }
                
                browserContent.finishLoading();
            }
                                                         
        } 
        catch (RenderingException re){
            Utilities.errorDialog("RenderingSession#getBrowserContent() threw " + re.toString());
            
        }finally{
            SecondaryResourceFetchThread.doneAddingImages();
        }        
    }    
    
    /**
     * @see net.rim.device.api.browser.RenderingApplication#eventOccurred(net.rim.device.api.browser.Event)
     */
    public Object eventOccurred(Event event) 
    {
        int eventId = event.getUID();

        switch (eventId) 
        {
            case Event.EVENT_URL_REQUESTED : 
            {
                UrlRequestedEvent urlRequestedEvent = (UrlRequestedEvent) event;    
                
                PrimaryResourceFetchThread thread = new PrimaryResourceFetchThread(urlRequestedEvent.getURL(),
                                                                                         urlRequestedEvent.getHeaders(), 
                                                                                         urlRequestedEvent.getPostData(),
                                                                                         event, this);
                thread.start();
    
                break;

            } 
            case Event.EVENT_BROWSER_CONTENT_CHANGED: 
            {                
                // Browser field title might have changed update title.
                BrowserContentChangedEvent browserContentChangedEvent = (BrowserContentChangedEvent) event; 
            
                if (browserContentChangedEvent.getSource() instanceof BrowserContent) 
                { 
                    BrowserContent browserField = (BrowserContent) browserContentChangedEvent.getSource(); 
                    String newTitle = browserField.getTitle();
                    if (newTitle != null) 
                    {
                        synchronized(Application.getEventLock()){ 
                            setTitle(newTitle);
                        }                                               
                    }                                       
                }                   

                break;                

            } 
            case Event.EVENT_REDIRECT : 
            {
                RedirectEvent e = (RedirectEvent) event;
                String referrer = e.getSourceURL();
                
                switch (e.getType()) 
                {  
                    case RedirectEvent.TYPE_SINGLE_FRAME_REDIRECT :
                        // Show redirect message.
                        Application.getApplication().invokeAndWait(new Runnable() 
                        {
                            public void run() 
                            {
                                Status.show("You are being redirected to a different page...");
                            }
                        });
                    
                    break;
                    
                    case RedirectEvent.TYPE_JAVASCRIPT :
                        break;
                    
                    case RedirectEvent.TYPE_META :
                        // MSIE and Mozilla don't send a Referer for META Refresh.
                        referrer = null;     
                        break;
                    
                    case RedirectEvent.TYPE_300_REDIRECT :
                        // MSIE, Mozilla, and Opera all send the original
                        // request's Referer as the Referer for the new
                        // request.
                        Object eventSource = e.getSource();
                        if (eventSource instanceof HttpConnection) 
                        {
                            referrer = ((HttpConnection)eventSource).getRequestProperty(REFERER);
                        }
                        
                        break;
                    }
                    
                    HttpHeaders requestHeaders = new HttpHeaders();
                    requestHeaders.setProperty(REFERER, referrer);
                    PrimaryResourceFetchThread thread = new PrimaryResourceFetchThread(e.getLocation(), requestHeaders,null, event, this);
                    thread.start();
                    break;

            } 
            case Event.EVENT_CLOSE :
                // TODO: close the appication
            	//close();
                break;
            
            case Event.EVENT_SET_HEADER :        // No cache support.
            case Event.EVENT_SET_HTTP_COOKIE :   // No cookie support.
            case Event.EVENT_HISTORY :           // No history support.
            case Event.EVENT_EXECUTING_SCRIPT :  // No progress bar is supported.
            case Event.EVENT_FULL_WINDOW :       // No full window support.
            case Event.EVENT_STOP :              // No stop loading support.
            default :
        }

        return null;
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getAvailableHeight(net.rim.device.api.browser.BrowserContent)
     */
    public int getAvailableHeight(BrowserContent browserField) 
    {
        // Field has full screen.
        return Display.getHeight();
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getAvailableWidth(net.rim.device.api.browser.BrowserContent)
     */
    public int getAvailableWidth(BrowserContent browserField) 
    {
        // Field has full screen.
        return Display.getWidth();
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getHistoryPosition(net.rim.device.api.browser.BrowserContent)
     */
    public int getHistoryPosition(BrowserContent browserField) 
    {
        // No history support.
        return 0;
    }
    

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getHTTPCookie(java.lang.String)
     */
    public String getHTTPCookie(String url) 
    {
        // No cookie support.
        return null;
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#getResource(net.rim.device.api.browser.RequestedResource,
     *      net.rim.device.api.browser.BrowserContent)
     */
    public HttpConnection getResource( RequestedResource resource, BrowserContent referrer) 
    {
        if (resource == null) 
        {
            return null;
        }

        // Check if this is cache-only request.
        if (resource.isCacheOnly()) 
        {
            // No cache support.
            return null;
        }

        String url = resource.getUrl();

        if (url == null) 
        {
            return null;
        }

        // If referrer is null we must return the connection.
        if (referrer == null) 
        {
            HttpConnection connection = Utilities.makeConnection(resource.getUrl(), resource.getRequestHeaders(), null);
            
            return connection;
            
        } 
        else 
        {
            // If referrer is provided we can set up the connection on a separate thread.
            SecondaryResourceFetchThread.enqueue(resource, referrer);
        }

        return null;
    }

    /**
     * @see net.rim.device.api.browser.RenderingApplication#invokeRunnable(java.lang.Runnable)
     */
    public void invokeRunnable(Runnable runnable) 
    {       
        (new Thread(runnable)).start();
    }
}

/**
 * A Thread class to fetch content using an http connection
 */
final class PrimaryResourceFetchThread extends Thread 
{    
    private OTAUpdateScreen _application;
    private Event _event;
    private byte[] _postData;
    private HttpHeaders _requestHeaders;
    private String _url;
    
    /**
     * Constructor to create a PrimaryResourceFetchThread which fetches the web
     * resource from the specified url.
     * 
     * @param url The url to fetch the content from
     * @param requestHeaders The http request headers used to fetch the content
     * @param postData Data which is to be posted to the url
     * @param event The event triggering the connection
     * @param application The application requesting the connection
     */
    PrimaryResourceFetchThread(String url, HttpHeaders requestHeaders, byte[] postData, Event event, OTAUpdateScreen application) 
    {
        _url = url;
        _requestHeaders = requestHeaders;
        _postData = postData;
        _application = application;
        _event = event;
    }

    /**
     * Connects to the url associated with this object
     * 
     * @see java.lang.Thread#run()
     */
    public void run() 
    {
        HttpConnection connection = Utilities.makeConnection(_url, _requestHeaders, _postData);
        _application.processConnection(connection, _event);        
    }
}

/**
 * This class provides the ability to set up an http connection if a referrer 
 * exists (a browser making the request).
 */
class SecondaryResourceFetchThread extends Thread 
{

    /**
     * Callback browser field.
     */
    private BrowserContent _browserField;
    
    /**
     * Images to retrieve.
     */
    private Vector _imageQueue;
    
    /**
     * True is all images have been enqueued.
     */
    private boolean _done;
    
    /**
     * Sync object.
     */
    private static Object _syncObject = new Object();
    
    /**
     * Secondary thread.
     */
    private static SecondaryResourceFetchThread _currentThread;
    
    
    /**
     * Enqueues secondary resource for a browser field.
     * 
     * @param resource - resource to retrieve.
     * @param referrer - call back browsr field.
     */
    static void enqueue(RequestedResource resource, BrowserContent referrer) 
    {
        if (resource == null) 
        {
            return;
        }
        
        synchronized( _syncObject ) 
        {
            
            // Create new thread.
            if (_currentThread == null) 
            {
                _currentThread = new SecondaryResourceFetchThread();
                _currentThread.start();
            } 
            else 
            {
                // If thread alread is running, check that we are adding images for the same browser field.
                if (referrer != _currentThread._browserField) 
                {  
                    synchronized( _currentThread._imageQueue) 
                    {
                        // If the request is for a different browser field,
                        // clear old elements.
                        _currentThread._imageQueue.removeAllElements();
                    }
                }
            }   
            
            synchronized( _currentThread._imageQueue) 
            {
                _currentThread._imageQueue.addElement(resource);
            }
            
            _currentThread._browserField = referrer;
        }
    }
    
    /**
     * Constructor
     *
     */
    private SecondaryResourceFetchThread() 
    {
        _imageQueue = new Vector();        
    }
    
    /**
     * Indicate that all images have been enqueued for this browser field.
     */
    static void doneAddingImages() 
    {
        synchronized( _syncObject ) 
        {
            if (_currentThread != null) 
            {
                _currentThread._done = true;
            }
        }
    }
    
    /**
     * Connects to the requested resource
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() 
    {
        while (true) 
        {
            if (_done) 
            {
                // Check if we are done requesting images.
                synchronized( _syncObject ) 
                {
                    synchronized( _imageQueue ) 
                    {
                        if (_imageQueue.size() == 0) 
                        {
                            _currentThread = null;   
                            break;
                        }
                    }
                }
            }
            
            RequestedResource resource = null;
                              
            // Request next image.
            synchronized( _imageQueue ) 
            {
                if (_imageQueue.size() > 0) 
                {
                    resource = (RequestedResource)_imageQueue.elementAt(0);
                    _imageQueue.removeElementAt(0);
                }
            }
            
            if (resource != null) 
            {
                
                HttpConnection connection = Utilities.makeConnection(resource.getUrl(), resource.getRequestHeaders(), null);
                resource.setHttpConnection(connection);
                
                // Signal to the browser field that resource is ready.
                if (_browserField != null) 
                {            
                    _browserField.resourceReady(resource);
                }
            }
        }       
    }   
    
}

/**
 * This class provides common functions required by the 
 * BrowserContentManagerDemo and BrowserFieldDemo. This class allows the
 * aforementioned classes to make a connection to a specified url.
 */
class Utilities 
{
    /**
     * Connect to a web resource
     * @param url The url of the resource
     * @param requestHeaders The request headers describing the connection to be made
     * @param postData The data to post to the web resource
     * @return The HttpConnection object representing the connection to the resource, null if no connection could be made
     */
    static HttpConnection makeConnection(String url, HttpHeaders requestHeaders, byte[] postData) 
    {
        HttpConnection conn = null;
        OutputStream out = null;
        
        try 
        {
            conn = (HttpConnection) Connector.open(url);           

            if (requestHeaders != null) 
            {
                // From
                // http://www.w3.org/Protocols/rfc2616/rfc2616-sec15.html#sec15.1.3
                //
                // Clients SHOULD NOT include a Referer header field in a (non-secure) HTTP 
                // request if the referring page was transferred with a secure protocol.
                String referer = requestHeaders.getPropertyValue("referer");
                boolean sendReferrer = true;
                
                if (referer != null && StringUtilities.startsWithIgnoreCase(referer, "https:") && !StringUtilities.startsWithIgnoreCase(url, "https:")) 
                {             
                    sendReferrer = false;
                }
                
                int size = requestHeaders.size();
                for (int i = 0; i < size;) 
                {                    
                    String header = requestHeaders.getPropertyKey(i);
                    
                    // Remove referer header if needed.
                    if ( !sendReferrer && header.equals("referer")) 
                    {
                        requestHeaders.removeProperty(i);
                        --size;
                        continue;
                    }
                    
                    String value = requestHeaders.getPropertyValue( i++ );
                    if (value != null) 
                    {
                        conn.setRequestProperty( header, value);
                    }
                }                
            }                          
            
            if (postData == null) 
            {
                conn.setRequestMethod(HttpConnection.GET);
            } 
            else 
            {
                conn.setRequestMethod(HttpConnection.POST);

                conn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH, String.valueOf(postData.length));

                out = conn.openOutputStream();
                out.write(postData);

            }
        } 
        catch (IOException e1) 
        {
       	    errorDialog(e1.toString());
        } 
        finally 
        {
            if (out != null) 
            {
                try 
                {
                    out.close();
                } 
                catch (IOException e2) 
                {
                    errorDialog("OutputStream#close() threw " + e2.toString());
                }
            }
        }    
        
        return conn;
    }
    
    /**
     * Presents a dialog to the user with a given message
     * @param message The text to display
     */
    public static void errorDialog(final String message)
    {
        UiApplication.getUiApplication().invokeLater(new Runnable()
        {
            public void run()
            {
                Dialog.alert(message);
            } 
        });
    }
}


