package com.utis.chambermaid.notnow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.chambermaid.CommonClass;
import com.utis.chambermaid.ConnectSettingsActivity;
import com.utis.chambermaid.ConnectionManagerService;
import com.utis.chambermaid.DBSchemaHelper;
import com.utis.chambermaid.LoginActivity;
import com.utis.chambermaid.R;
import com.utis.chambermaid.RestTask;
import com.utis.chambermaid.RestTask.ResponseCallback;
import com.utis.chambermaid.RoomsListActivity;
import com.utis.chambermaid.records.EmpRecord;
import com.utis.chambermaid.records.HotelRoomRecord;
import com.utis.chambermaid.records.IpRecord;
import com.utis.chambermaid.records.LogsRecord;
import com.utis.chambermaid.records.OperRecord;
import com.utis.chambermaid.tables.HotelRoomTable;
import com.utis.chambermaid.tables.IpTables;
import com.utis.chambermaid.tables.OperTable;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class WelcomeActivity extends Activity implements ResponseCallback {
	private static final boolean D = true;
	private static final String TAG = "WelcomeActivity";
    public static final String DOWNLOAD_SERVER_URI_SSL = "https://85.238.112.13:443/contenti";
    private static final int STATE_GET_VER = 1;
    private static final String VER = "/maid/ver.php";
    private static final String APK_PATH = "/Chambermaid.apk";

    private String helpURI, ipHotel, ipHotel2, wifiStr, wifiStrBroadcast, wifiStrGen;
	private String serverURI, updateServerURI, verURI, infoURI, loginURI, userId, position;
	private View authPswButton;
	private TextView mRequestView, mVersionView;
	Context mContext;
    private TelephonyManager tm;
    private String mPhoneNumber, mIMEI, mVersion = "";
    private int mVerCode;
    private DBSchemaHelper dbSch;
	private ProgressDialog mProgress;
	private int mState;
	private boolean verChecked = false, mDownloading = false;
    private Handler mHandler = new Handler();
    HotelRoomRecord operRecord2;
    List<String> myList = new ArrayList<String>();
    WiFiBroadcast wifi = new WiFiBroadcast();
    ArrayList<String> list = new ArrayList<String>();
    IpRecord ipRecord;
    IpTables db;
    ArrayList<String> kate = new ArrayList<>();
    ArrayList<String> kate2 = new ArrayList<>();
    boolean kateOne, kateTwo;
    Cursor c;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		mContext = this;
        dbSch = DBSchemaHelper.getInstance(this);
        db = new IpTables(this);
        db.abrirBaseData();
        kateOne = false;
        kateTwo = false;
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneNumber = tm.getLine1Number();
		mIMEI = tm.getDeviceId();
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            int ip = wifiInfo.getIpAddress();

            wifiStr  = String.format(
                    "%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff));

            Log.d(TAG, "## " + wifiStr);


        }
    	String devModel = android.os.Build.MODEL; // Device model
    	String devVer = android.os.Build.VERSION.RELEASE; // Device OS version

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		CommonClass.dispHeight = displaymetrics.heightPixels;
		CommonClass.dispWidth = displaymetrics.widthPixels;
		double x = Math.pow(displaymetrics.widthPixels/displaymetrics.xdpi, 2);
		double y = Math.pow(displaymetrics.heightPixels/displaymetrics.ydpi, 2);
		CommonClass.screenInches = Math.sqrt(x + y);


		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			mVersion = pInfo.versionName;
			mVerCode = pInfo.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        dbSch.addLogItem(LogsRecord.INFO, new Date(),
                String.format(getString(R.string.m_dev_info), devModel, devVer));

		mVersionView = (TextView) findViewById(R.id.textViewVer);
		mVersionView.setText(getString(R.string.title_version) + mVersion);
		mRequestView = (TextView) findViewById(R.id.requestView);

		authPswButton = findViewById(R.id.auth_name_button);
		authPswButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showLogin();
//					showRooms();
                    }
                }
        );
		authPswButton.setEnabled(true/*false*/);
	}

    @Override
    public void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        getHotel();
        getHotel2();
//        for (String d: kate2)
//            Log.d(TAG,"***> " + d);
//        for (String e: kate)
//            Log.d(TAG,"###> " + e);
//        serverMode = ConnectionManagerService.getServerMode(this);
		serverURI = CommonClass.getServerURI(this);
        updateServerURI = CommonClass.getUpdateServerURI(this);
       userId = CommonClass.getUserID(mContext);
       position = CommonClass.getPosition(mContext);
//		loginURI = serverURI + ConnectionManagerService.LOGIN_IMEI_URI;
//		helpURI = serverURI + ConnectionManagerService.HELP_URI;
		verURI = updateServerURI + VER;
//		infoURI = serverURI + ConnectionManagerService.REST_WARRANTS_URI + ConnectionManagerService.INFO;

        String ddd= CommonClass.getPosition(this);



		boolean debugMode = CommonClass.getDebugMode(mContext);
		String debug = debugMode ? " Debug mode" : "";
		serverURI = CommonClass.getServerIP(mContext);// CommonClass.URL;
        Log.d(TAG, serverURI);
		String msg = getString(R.string.pref_title_server_addr) + ": " + serverURI + debug;
		mRequestView.setText(msg);
		dbSch.addLogItem(LogsRecord.INFO, new Date(), msg);
		if (!verChecked) {
            sendVerRequest();
		}

        this.registerReceiver(wifi, new IntentFilter("android.net.wifi.STATE_CHANGE"));
        wifiSwitch();
    }


    // use Broadcast Receiver to find Wi-Fi
    @Override
    protected void onStart() {
        super.onStart();
        this.registerReceiver(wifi, new IntentFilter("android.net.wifi.STATE_CHANGE"));;
        wifiSwitch();
    }
    @Override
    protected void onPause() {
        super.onPause();
        this.registerReceiver(wifi, new IntentFilter("android.net.wifi.STATE_CHANGE"));
        wifiSwitch();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(wifi);
        Log.e(TAG,"onDestroy()");
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.welcome, menu);
		return true;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
        switch (item.getItemId()) {
			case R.id.action_settings:
				mRequestView.setTextColor(Color.BLACK);
				intent = new Intent(this, ConnectSettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_show_log:
				intent = new Intent(this, LogsActivity.class);
				startActivity(intent);
				return true;
			case R.id.action_full_exit:
				fullExit();
				return true;
			case R.id.action_soft_download:
                showWebSoft();
				return true;
        }
        return false;
	}

    private void showWebSoft() {
		Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(DOWNLOAD_SERVER_URI_SSL + "/maid.php"));
		startActivity(webIntent);
	}

	private void showHelp() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(helpURI));
		startActivity(i);
	}

	private void fullExit() {
		Intent serviceIntent = new Intent(this, ConnectionManagerService.class);
		stopService(serviceIntent);
        finish();
	}


	private void showRooms() {
        Intent intent = new Intent(this, RoomsListActivity.class);
		Bundle b = new Bundle();
		b.putString("num", "203");
        b.putString("hotel", "1");
		intent.putExtras(b);
        startActivity(intent);
	}

    // sort and show chambermaid name  and hotel

	private void showLogin() {
        if (wifi.sid != null) {
        wifiSwitch();

        if(kateOne == true){
            Intent runLoginActivity = new Intent(mContext, LoginActivity.class);
            runLoginActivity.putExtra("name", ipHotel);
            runLoginActivity.putExtra("idHotel", "000000001");
            startActivity(runLoginActivity);
        }else if (kateTwo == true){
            Intent runLoginActivity = new Intent(mContext, LoginActivity.class);
            runLoginActivity.putExtra("name", ipHotel2);
            runLoginActivity.putExtra("idHotel", "000000002");
            startActivity(runLoginActivity);
        }else {
            Log.d(TAG, "No coincidence");
            Intent runLoginActivity = new Intent(mContext, LoginActivity.class);
            startActivity(runLoginActivity);
        }

            } else {
                Intent runLoginActivity = new Intent(mContext, LoginActivity.class);
                startActivity(runLoginActivity);
            }

	}

    private void clearPaused() {
        authPswButton.setEnabled(true);
    }

    private void sendVerRequest() {
        String warr = "";
        mState = STATE_GET_VER;
        String url = verURI + ConnectionManagerService.VER;

        try {
            JSONObject header = new JSONObject();
            header.put("dev_model", android.os.Build.MODEL); // Device model
            header.put("dev_ver", android.os.Build.VERSION.RELEASE); // Device OS version
            header.put("soft_num", mVersion);
            warr = header.toString();

            HttpPost postRequest = new HttpPost(url);
            postRequest.addHeader(BasicScheme.authenticate(
                    new UsernamePasswordCredentials(mIMEI, ConnectionManagerService.MOBILE), "UTF-8", false));
            if (warr.length() > 0) {
                StringEntity se = new StringEntity(warr, "UTF-8");
                se.setContentType("text/plain;charset=UTF-8");
                se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "text/plain;charset=UTF-8"));
                postRequest.setEntity(se);
            }
            RestTask task = new RestTask();
            task.setResponseCallback((ResponseCallback) mContext);
            task.execute(postRequest);
        } catch (Exception e) {
            if (D) Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onRequestSuccess(String response) {
        if (D) Log.d(TAG, "onRequestSuccess");
        boolean resultOK = false;
        String res = "";
        int res_code, verCode = 0;
        try {
            JSONArray records = new JSONArray(response);
            for (int i = 0; i < records.length(); i++) {
                JSONObject warrant = records.getJSONObject(i);
                switch (mState) {
                    case STATE_GET_VER:
                        res = warrant.getString("result");
                        res_code = warrant.getInt("code");
                        resultOK = res_code == 0;
                        try {
                            verCode = warrant.getInt("ver_code");
                        } catch (Exception e) {
                            verCode = 0;
                        }
                        CommonClass.newVerAvail = verCode > mVerCode;
                        if (CommonClass.newVerAvail) {
                            Date lastDate = CommonClass.getLastDateUpdate(mContext);
                            Date now = Calendar.getInstance().getTime();
                            long interval = now.getTime() - lastDate.getTime();
                            if (true/*interval > CommonClass.UPD_DATE_INTERVAL*/) {
                                Toast.makeText(mContext, getString(R.string.m_update_avail), Toast.LENGTH_SHORT).show();
                                if (!mDownloading) {
                                    CommonClass.setLastDateUpd(mContext, now);
                                    mDownloading = true;
                                    LoadApkTask loadApk = new LoadApkTask();
                                    loadApk.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, updateServerURI + APK_PATH);
                                }
                            }
                        } else {
                            Toast.makeText(mContext, getString(R.string.m_actual_ver), Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "Exception: " + e.getMessage() + " " + response);
            res =/* e.getMessage() + " "+ */response;
        }
        switch (mState) {
            case STATE_GET_VER:
                if (resultOK)
                    verChecked = true;
                if (!mDownloading)
                    clearPaused();
                break;
        }

    }

    @Override
    public void onRequestError(Exception error) {
        if (D) Log.d(TAG, "onRequestError: " + error.getMessage());
        clearPaused();
    }

    private class LoadApkTask extends AsyncTask<String, Integer, String> {
        private static final int CONNECTION_TIMEOUT = 20*1000;
        private static final int SOCKET_TIMEOUT_MIN = 18*1000;
        private static final int SOCKET_TIMEOUT = 18*1000;
        boolean downloaded = false;


        protected String doInBackground(String... sUrl) {
            String path =  "/sdcard"+ APK_PATH;
            String msg = "";
            mDownloading = true;
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[] {
                        new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[]{}; }
                        }
                }, null);
                HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

                URL url = new URL(sUrl[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

/*
			        URL url = new URL("http://192.168.2.7/TimeTracking.apk");
			        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
*/
                long startTime = System.currentTimeMillis();
                connection.connect();

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(path);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                long endTime = System.currentTimeMillis();
                msg = String.format("[LoadApkTask] Time to download %s (Size=%d bytes): %s mc",
                        url.getFile(), total, String.valueOf(endTime - startTime));
                dbSch.addLogItem(LogsRecord.INFO, new Date(), msg);
                Log.i(TAG, msg);

                downloaded = true;
            } catch (FileNotFoundException e) {
                msg = e.getMessage();
                Log.e(TAG, msg);
                dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), msg);
                final String err = msg;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, mContext.getString(R.string.m_file_not_found) + err,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                msg = e.getMessage();
                Log.e(TAG, msg);
                dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), msg);
            }
            return path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String msg = getString(R.string.m_update_download);
            dbSch.addLogItem(LogsRecord.DEBUG, new Date(), msg);
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }

        // begin the installation by opening the resulting file
        @Override
        protected void onPostExecute(String path) {
            mDownloading = false;
            if (downloaded) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive" );
              //  Log.d(TAG, "About to install new .apk");
                mContext.startActivity(i);
            } else {
                clearPaused();
            }
        }
    }


    public void getHotel() {
        String query = "SELECT * FROM " + IpTables.TABLE_NAME + " WHERE "
                + IpTables.HOTEL+ " LIKE 'Екатерина '";
        Cursor c = null;
        IpRecord entRecord;
        SQLiteDatabase sqdb = db.getWritableDatabase();
        try {
            c = sqdb.rawQuery(query, null);

            while (c.moveToNext()) {
                entRecord = new IpRecord(c);
                kate.add(String.valueOf(entRecord));
                ipHotel = entRecord.getHotel();

            }
        } catch(Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
    }
    public void getHotel2() {
        String query = "SELECT * FROM " + IpTables.TABLE_NAME + " WHERE "
                + IpTables.HOTEL+ " LIKE 'Екатерина II '";
        Cursor c = null;
        IpRecord entRecord;
        SQLiteDatabase sqdb = db.getWritableDatabase();

        try {
            c = sqdb.rawQuery(query, null);

            while (c.moveToNext()) {
                entRecord = new IpRecord(c);
                kate2.add(String.valueOf(entRecord));
                ipHotel2 = entRecord.getHotel();

            }
        } catch(Exception e) {
           // Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
    }

    public void wifiSwitch()

    {

        if(wifi.sid != null && wifiStr != null && wifi.sid != "") {
            wifiStrBroadcast = wifi.sid.concat(".0");
            wifiStrGen = wifiStr.concat(".0");

            Log.d(TAG, "^-^ " +wifiStr + " == " + wifi.sid + " >>" + wifiStrBroadcast + " == " + wifiStrGen);

            String[] arrKate = kate.toArray(new String[kate.size()]);
            for (String i : arrKate) {
              //  Log.d(TAG, "(#-#)" + i);
                if (wifiStrBroadcast.equals(i) || wifiStrGen.equals(i)) {
                    kateOne = true;


                 //   Log.d(TAG, "***> " + "equals" + kateOne);
                } else {
                }
            }
            String[] arrKate2 = kate2.toArray(new String[kate2.size()]);
            for (String y : arrKate2) {
              //  Log.d(TAG, "(#''#)" + y);
                if (wifiStrBroadcast.equals(y)|| wifiStrGen.equals(y)) {
                    kateTwo = true;
                    Log.d(TAG, "###> " + "equals");
                } else {
                }
            }
        }
    }




}