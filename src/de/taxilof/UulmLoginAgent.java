/*
* ----------------------------------------------------------------------------
* "THE BEER-WARE LICENSE" (Revision 42):
* <simon.fuchs@uni-ulm.de> wrote this file. As long as you retain this notice you
* can do whatever you want with this stuff. If we meet some day, and you think
* this stuff is worth it, you can buy me a beer in return. Simon Fuchs
* ----------------------------------------------------------------------------
*/

package de.taxilof;


import java.net.URI;

import javax.net.ssl.SSLException;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HttpContext;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


public class UulmLoginAgent  {
	private NotificationManager mNotificationManager;
	private int SIMPLE_NOTFICATION_ID;	
	private static final String PREFS_NAME = "UulmLoginPrefs";
	String password;
	String username;
	Context context;
	
	public  UulmLoginAgent(Context contextIn) {
		context = contextIn;
        // read preferences
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        username = settings.getString("username", "");
        password = settings.getString("password", "");		
	}
	/**
	 * perform the Login & necessary Checks
	 */
    public void login() {
    	// setting up my http client
		DefaultHttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Linux; U; Android; uulmLogin " + context.getString(R.string.app_version) + ")");
		// disable redirects in client, used from isLoggedIn method
		client.setRedirectHandler(new RedirectHandler() {
			public URI getLocationURI(HttpResponse arg0, HttpContext arg1)
					throws ProtocolException {
				return null;
			}
			public boolean isRedirectRequested(HttpResponse arg0,
					HttpContext arg1) {
				return false;
			}
	    });
		
 	
    	// get IP
    	String ipAddress = getIp();
    	if (ipAddress == null) {
    		Log.w("uulmLogin:", "Could not get IP Address, aborting.");
    		return; 
    	} 
        Log.d("uulmLogin:", "Got IP: " + ipAddress + ", starting Login Process.");
   
        // check if IP prefix is wrong
        if (!(ipAddress.startsWith(context.getString(R.string.ip_prefix)))) {
        	Log.d("uulmLogin:", "Wrong IP Prefix.");
        	return;
        }
                
        // check if we are already logged in
    	if (isLoggedIn(client)) { // 1000ms should be enough..
    		Log.d("uulmLogin", "Already logged in, aborting.");
    		return;
    	}

        // try to login via GET Request
    	try {
    		// login
    		HttpGet get = new HttpGet(String.format("%s?username=%s&password=%s&login=Anmelden", context.getString(R.string.capo_uri),username, password));
			@SuppressWarnings("unused")
			HttpResponse response = client.execute(get);
    		//Log.d("uulmLogin","Login done, HttpResponse:"+ HttpHelper.request(response));
    	} catch (SSLException ex) {
    		Log.w("uulmLogin","SSL Error while sending Login Request: "+ex.toString());
    		notify("Login to Welcome failed", "SSL Error: could not verify Host", true);
    		return;
    	} catch (Exception e)	{
    		Log.w("uulmLogin","Error while sending Login Request: "+e.toString());
    		notify("Login to Welcome failed", "Error while sending Login Request.", true);
    		return;
    	}  
        	
    	// should be logged in now, but we check it now, just to be sure
    	if (isLoggedIn(client)) {
    		notify("Login to welcome successful.", "Your IP: "+ipAddress, false);
    		Log.d("uulmLogin", "Login successful.");
    	} else {
    		notify("Login to welcome failed.", "Maybe wrong Username/Password?", true);
    		Log.w("uulmLogin", "Login failed, wrong user/pass?");
    	}
    }
    
	/**
	 * fetch the IP of the Device 
	 * @return ip
	 */
	private String getIp() {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String ipAddress = null;
        if (wifiInfo != null) {
            long addr = wifiInfo.getIpAddress();
            if (addr != 0) {
                if (addr < 0) addr += 0x100000000L;   // handle negative values whe first octet > 127
                ipAddress = String.format("%d.%d.%d.%d",addr & 0xFF, (addr >> 8) & 0xFF, (addr >> 16) & 0xFF, (addr >> 24) & 0xFF);
            }
        }	
        return ipAddress;
	}
	/**
	 * notify the User in Statusbar
	 */
	private void notify(String subject, String message, boolean errorIcon) {
		// build notification with notifyString
		Notification notifyDetails;
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (errorIcon) {
        	notifyDetails = new Notification(R.drawable.icon_red,subject,System.currentTimeMillis());
        } else {
        	notifyDetails = new Notification(R.drawable.icon,subject,System.currentTimeMillis());
        }
		PendingIntent myIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
		notifyDetails.setLatestEventInfo(context, subject, message, myIntent);
		notifyDetails.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(SIMPLE_NOTFICATION_ID, notifyDetails);		
	}    
	/**
	 * check if we are logged in
	 */
	private boolean isLoggedIn(DefaultHttpClient client) {
		HttpResponse response;
		try {
			response = client.execute(new HttpGet("http://service.fs-et.de/uulmlogin/"));
			if (response.containsHeader("Location")) { 
				return false;
			} else {
				return true;
			}  
		} catch (Exception e1) {
			Log.d("uulmLogin","Error in isLoggedIn"+e1.toString());
				return false;
		}
	}
}