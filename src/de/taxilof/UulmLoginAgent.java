/*
* ----------------------------------------------------------------------------
* "THE BEER-WARE LICENSE" (Revision 42):
* <simon.fuchs@uni-ulm.de> wrote this file. As long as you retain this notice you
* can do whatever you want with this stuff. If we meet some day, and you think
* this stuff is worth it, you can buy me a beer in return. Simon Fuchs
* ----------------------------------------------------------------------------
*/

package de.taxilof;

import java.net.InetAddress;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

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
    	String ipAddress = getIp();
    	if (ipAddress == null) {
    		Log.w("uulmLogin:", "could not get IP Address, aborting");
    		return; 
    	} 
        Log.d("uulmLogin:", "got IP: " + ipAddress + ", trying to log in");
   
        // check ip prefix is wrong
        if (!(ipAddress.startsWith(context.getString(R.string.ip_prefix)))) {
        	Log.w("uulmLogin:", "wrong ip prefix");
        	return;
        }
        // try to login via GET Request
    	try {
    		HttpGet get = new HttpGet(String.format("%s?username=%s&password=%s&login=Anmelden", context.getString(R.string.capo_uri),username, password));
    		DefaultHttpClient client = new DefaultHttpClient();
    		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Mozilla/5.0 (Linux; U; Android; uulmLogin " + context.getString(R.string.app_version) + ")");
			HttpResponse response = client.execute(get);
    		Log.v("uulmLogin","Login done, HttpResponse:"+ HttpHelper.request(response));
    	} catch (Exception e)	{
    		Log.w("uulmLogin","Error in GET Request:"+e.toString());
    		return;
    	}  
        	
    	// should be loged in now, but we should be sure, so check it now
    	String notifyString = "bla";
		try {
			InetAddress uniUlm = InetAddress.getByName("134.60.1.1");	// 
        	if (uniUlm.isReachable(200)) {
        		notifyString ="Login to welcome successful ";
        		Log.d("uulmLogin:", "Login successful");
        	} else {
        		notifyString ="Login to welcome failed ";
        		Log.w("uulmLogin:", "Login failed");
        	}
		} catch (Exception e) {
			notifyString = "Error while checking loginstate";
			Log.w("uulmLogin:", "Login failed?");
		}
		
		// inform the user what 
		notify(notifyString, "Your IP: "+ipAddress);
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
	private void notify(String subject, String message) {
		// build notification with notifyString
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notifyDetails = new Notification(R.drawable.icon,subject,System.currentTimeMillis());
		PendingIntent myIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
		notifyDetails.setLatestEventInfo(context, subject, message, myIntent);
		notifyDetails.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotificationManager.notify(SIMPLE_NOTFICATION_ID, notifyDetails);		
	}    
}