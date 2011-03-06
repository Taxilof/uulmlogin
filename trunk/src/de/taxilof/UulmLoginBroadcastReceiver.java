/*
* ----------------------------------------------------------------------------
* "THE BEER-WARE LICENSE" (Revision 42):
* <simon.fuchs@uni-ulm.de> wrote this file. As long as you retain this notice you
* can do whatever you want with this stuff. If we meet some day, and you think
* this stuff is worth it, you can buy me a beer in return. Simon Fuchs
* ----------------------------------------------------------------------------
*/

package de.taxilof;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


public class UulmLoginBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// check if there is a connection to wifi
		NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
		if (info != null) {
			if (info.getState().equals(NetworkInfo.State.CONNECTED)){
			    // wifi connection available
				Log.d("uulmLogin:", "wifi connection available");
				UulmLoginAgent agent = new UulmLoginAgent(context);
				agent.login();

			} else {
				Log.d("uulmLogin:", "no connection");
			}
		} else {
			Log.d("uulmLogin:", "could not get networkinfo");
		}
	}
}