package com.utis.chambermaid.notnow;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.utis.chambermaid.notnow.WelcomeActivity;

public class WiFiBroadcast extends BroadcastReceiver {
    private static final String TAG = "WiFi";
    NetworkInfo netInfo;
    WifiManager wifiManager;
    WifiInfo info;
    public  String sid ="";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action  = intent.getAction();

        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals (action)) {
            netInfo = intent.getParcelableExtra (WifiManager.EXTRA_NETWORK_INFO);
            if (ConnectivityManager.TYPE_WIFI == netInfo.getType ()) {
                 wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
                 info = wifiManager.getConnectionInfo ();
                int ip = info.getIpAddress();
                sid = String.format(
                        "%d.%d.%d",
                        (ip & 0xff),
                        (ip >> 8 & 0xff),
                        (ip >> 16 & 0xff));
            }
        }

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        boolean isConnected = wifi != null && wifi.isConnectedOrConnecting();
        boolean isDisconnected = mobile != null && mobile.isConnectedOrConnecting();

        if (isConnected) {
            Log.d(TAG, "YES");
        }else if (isDisconnected){
            Log.d(TAG, "NO");
        }else {
            Log.d(TAG, "I DON'T KNOW");
        }
        Intent notificationIntent = new Intent(context, WelcomeActivity.class);
    }

}
