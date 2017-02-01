package com.utis.chambermaid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CommonClass {
	private static final boolean D = true;
	private static final String TAG = "CommonClass";
	public static final String PREF_NAME = "MyPref";
	public static final int clrDkRed = 0xff8B0000;
	public static final String noData = "н/д";
	public static final int FAIL_REPETITIONS = 10;
    public static final String DUMP_FILE_NAME = "rooms.txt";
    public static final long UPD_DATE_SHIFT = 60*60*1000;
    private static final String LAST_UPD = "lastUpd";

	public static final String LOGIN = "sait";
	public static final String PSW = "sait555";
	//    public final static String URL = "http://192.168.9.250/gostproba/ws/GetnomObmen1_ws1.1cws?wsdl";//   /WebService/services/WebService?wsdl";

    public static final String UPDATE_SERVER_URI = "85.238.112.13/contenti";
//    public static final String UPDATE_SERVER_URI = "192.168.2.7";
    public final static String UPDATE_URL_PFX = "https://";
//    public final static String UPDATE_URL_SFX = "/contenti"
    public static final String SERVER_URI = "192.168.9.250/gost/";

    public static final String USER_ID = "0000000";
    public static final String POSITION = "0";
    public static final String IP_ADDRESS = "0";
    public static final String IP_LIST = "0";
    public static final String IP_LIST_HOTEL = "0";
    public static final String IP_INTENT = "intent";
    public static Set<String> stringSet ;

	public final static String URL_PFX = "http://";
	//public final static String URL_SFX = "/gostproba/ws/GetnomObmen_ws1.1cws";//   /WebService/services/WebService?wsdl";
	public final static String URL_SFX = "/ws/GetnomObmen_ws1.1cws";//   /WebService/services/WebService?wsdl";
	private final static String URL = "http://192.168.9.250/gostproba/ws/GetnomObmen_ws1.1cws";//   /WebService/services/WebService?wsdl";
	public static final String NAMESPACE = "Otel";//http://sample";
	public static final String SOAP_ACTION_PREFIX = "Otel#GetNomobmen:";

	public static String lastErrorMsg = "";
	public static boolean keepLog;
	public static boolean newVerAvail = false;
    public static int dispWidth, dispHeight;
    public static double screenInches;
//	public static int idOwner, idEntGroup;
	private static final String LAST_DICTIONARY_UPDATE_FILENAME = "last_dict_update.txt";
	private static final String LAST_REPLICATOR_TIME_FILENAME = "last_repl_time.txt";
	private static final String OWNER_ID = "IdOwner";
	private static final String OWNER_NAME = "OwnerName";
	private static final String GROUP_NAME = "IdGroup";

    public static String getServerIP(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(context.getString(R.string.pref_server_addr_key), SERVER_URI);
    }

    public static String getServerURI(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String serv = sharedPrefs.getString(context.getString(R.string.pref_server_addr_key), SERVER_URI);
        return URL_PFX + serv + URL_SFX;
    }

    public static String getUpdateServerURI(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String serv = sharedPrefs.getString(context.getString(R.string.pref_update_server_addr_key), UPDATE_SERVER_URI);
        return UPDATE_URL_PFX + serv;// + URL_SFX;
    }

    public static boolean getDebugMode(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getBoolean(context.getString(R.string.pref_debug_checkbox_key), false);
    }


    public static String getUserID(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String user = sharedPrefs.getString(context.getString(R.string.pref_user_id_key), USER_ID);
        return user;
    }

    public static String getPosition(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String position = sharedPrefs.getString(context.getString(R.string.pref_position_key), POSITION);
        return position;
    }



    public static String getIpIntent(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ip_intent = sharedPrefs.getString(context.getString(R.string.pref_ip_intent), IP_INTENT);
        return ip_intent;
    }
//    public static String getUserID(Context context){
//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String usrId = sharedPrefs.getString(context.getString(R.string.pref_user_id_key), USER_ID);
//        return usrId;
//    }

    public static int getInterval(Context context) {
        int interval;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String serv = sharedPrefs.getString(context.getString(R.string.pref_sync_frequency_key), "NULL");
        try {
            interval = Integer.parseInt(serv);
        } catch (NumberFormatException e) {
            interval = 3;
        }
        return interval;
    }


    public static boolean isEqServDateValid(Date beginDate, Date endDate, Date date2compare) {
		boolean res = false;
		if (date2compare != null) {
			if (beginDate != null) {
				if (endDate != null) {
					res = (date2compare.before(endDate) && date2compare.after(beginDate))||
							date2compare.equals(endDate)||date2compare.equals(beginDate);
				} else {
					res = date2compare.equals(beginDate) || date2compare.after(beginDate);
				}
			} else {
				if (endDate != null) {
					res = date2compare.equals(endDate) || date2compare.before(endDate);
				} 			
			}			
		}		
		
//		return (((beginDate != null)&&(date2compare >= beginDate)&&(date2compare <= endDate))||
//    		((date2compare >= beginDate)&&(beginDate != null)&&(endDate == null)));
		
		return res;
	}
	
	public static void showMessage(Context mContext, String msgTitle, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    builder
	    .setTitle(msgTitle)
	    .setMessage(msg)
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener()
	    {
	        public void onClick(DialogInterface dialog, int which)
	        {       
	            //do some thing here which you need
	        	dialog.dismiss();
	        }
	    });             
	    AlertDialog alert = builder.create();
	    alert.show();
	}
	
	//Create a new file and write some data
	private static boolean writeDate2File(Context mContext, Date aDate, String fileName) {
		boolean res = false;
		try { 
			FileOutputStream mOutput = mContext.openFileOutput(fileName, Activity.MODE_PRIVATE);
//			String data = aDate.toString(); 
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
			String data = df.format(aDate);
			if (D) Log.e(TAG, "writeLastUpdate = " + data);
			mOutput.write(data.getBytes()); 
			mOutput.close();
			res = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace(); 
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		return res;
	}
	
	private static String readDateFromFile(Context mContext, String fileName) {
		String res = "";
		try { 
			FileInputStream mInput = mContext.openFileInput(fileName);
			byte[] data = new byte[12]; 
			mInput.read(data); 
			mInput.close(); 
			res = new String(data);
			if (res.length() == 0)
				res = "201309010000";
		} catch (FileNotFoundException e) {
			e.printStackTrace(); 
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		return res;
	}

	public static boolean writeReplTime(Context mContext, Date aDate) {
		return writeDate2File(mContext, aDate, LAST_REPLICATOR_TIME_FILENAME);
	}
	
	public static String readReplTime(Context mContext) {
		return readDateFromFile(mContext, LAST_REPLICATOR_TIME_FILENAME);
	}	
	
	//Create a new file and write some data
	public static boolean writeLastUpdate(Context mContext, Date aDate) {
		return writeDate2File(mContext, aDate, LAST_DICTIONARY_UPDATE_FILENAME);
	}

	public static String readLastUpdate(Context mContext) {
		return readDateFromFile(mContext, LAST_DICTIONARY_UPDATE_FILENAME);
	}
	
    
//    public static void ShowWarrantEmpWorkTimeMarks(Context mContext, String wId, String wNum) {
//        Intent intent = new Intent(mContext, EmpWorkTimeActivity.class);
//        Bundle b = new Bundle();
//        b.putString("id", wId); 
//        b.putString("num", wNum); 
//        intent.putExtras(b); 
//        mContext.startActivity(intent);    	    	    	
//    }
    
    public static void Copy2Clipboard(String text, Activity activity) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
		    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		    clipboard.setText(text);
		} else {
		    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		    android.content.ClipData clip = android.content.ClipData.newPlainText("", text);
		    clipboard.setPrimaryClip(clip);
		}
    }

    public static void OpenOnlineWarrants(Context mContext) {
    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https:////85.238.112.13/contenti/Ol3"));
    	mContext.startActivity(browserIntent);
    }

	public static int convertDpToPixel(Context mContext, float dp) {
	       DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
	       float px = dp * (metrics.densityDpi / 160f);
	       return (int) px;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void showUndo(final View viewContainer) {
	    viewContainer.setVisibility(View.VISIBLE);
	    viewContainer.setAlpha(1);
	    viewContainer.animate().alpha(0.4f).setDuration(5000)
	        .withEndAction(new Runnable() {

                @Override
                public void run() {
                    viewContainer.setVisibility(View.GONE);
                }
            });
	}

	public static boolean isNetworkAvailable(Context mContext) {
	    ConnectivityManager connectivityManager
	          = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}	
	
//+++++++++++++++++	
	public static int getOwner(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getInt(OWNER_ID, 0);
	}	
		
	public static void setOwner(Context mContext, int idOwner) {
		SharedPreferences prefs = mContext.getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putInt(OWNER_ID, idOwner);
    	editor.commit();			
	}
	
//+++++++++++++++++	
	public static String getOwnerName(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getString(OWNER_NAME, "");
	}	
		
	public static void setOwnerName(Context mContext, String ownerName) {
		SharedPreferences prefs = mContext.getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putString(OWNER_NAME, ownerName);
    	editor.commit();			
	}
		
//+++++++++++++++++	
	public static int getGroup(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getInt(GROUP_NAME, 0);
	}	
		
	public static void setGroup(Context mContext, int idGroup) {
		SharedPreferences prefs = mContext.getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putInt(GROUP_NAME, idGroup);
    	editor.commit();			
	}

    public static int daysBetween(Date startDate, Date finishDate) {
        long startTime = startDate.getTime();
        long endTime = finishDate.getTime();
        long diffTime = endTime - startTime;
        return (int) (diffTime / (1000 * 60 * 60 * 24));
    }


    public static Date getLastDateUpdate(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String sDate = prefs.getString(LAST_UPD, "");
        Date date = Calendar.getInstance().getTime();
        date.setTime(date.getTime() - UPD_DATE_SHIFT); // an hour earlier
        try {
            date = df.parse(sDate);
        } catch (ParseException e) {

        }
        return date;
    }

    public static void setLastDateUpd(Context mContext, Date date) {
        SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        String sDate = df.format(date);
        editor.putString(LAST_UPD, sDate);
        editor.commit();
    }


}
