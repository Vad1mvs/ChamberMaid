package com.utis.chambermaid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.utis.chambermaid.RestTask.ResponseCallback;
import com.utis.chambermaid.records.EmpRecord;
import com.utis.chambermaid.records.HotelRoomRecord;
import com.utis.chambermaid.records.LogsRecord;
import com.utis.chambermaid.records.OperRecord;
import com.utis.chambermaid.tables.BarCodeGoodsTable;
import com.utis.chambermaid.tables.EmpTable;
import com.utis.chambermaid.tables.EntTable;
import com.utis.chambermaid.tables.GoodsTable;
import com.utis.chambermaid.tables.HotelRoomTable;
import com.utis.chambermaid.tables.OperTable;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault12;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ConnectionManagerService extends Service implements
		ResponseCallback {
	private static final boolean D = true;
	private static final String TAG = "ConManagServ";
	private static final int LOG_LIFE = 7;
	private static final int UPDATE_DICT_INTERVAL = 10;

    public interface ServiceResponseCallback {
		public void onSrvRequestSuccess(String response);
		public void onSrvRequestError(Exception error);
		public void onSrvTaskChanged(int Task);
	}

	private WeakReference<ServiceResponseCallback> mCallback;

    public static final String GET_TASK_ACTION = "GetTaskAction";
    public static final String SET_CAPTION_ACTION = "SetCaptionAction";
    private static final int STATE_GET_FIO = 1;
    private static final int STATE_GET_ROOMS = 2;
    private static final int STATE_POST_AUTH = 6;
    private static final int STATE_GET_OPER = 3;

    private static final int STATE_POSITION = 17;
  //  private static final int STATE_CALL_CHAMBERMAID = 3;
    private static final String METHOD_STATUS = "GetStatusNom";
    private static final String METHOD_FIO = "GetFio";
    private static final String METHOD_POST_AUTH = "SendPassword";
    private static final String STATE_GET_POSITION = "GetDolgnost";
    private static final String METHOD_GET_OPER = "GetOperInfo";

    private String SOAPAction, SOAPMethod;
    private int mSOAPState = STATE_FINISH;
    private String mUserId = "", mUserPsw = "", mPositionId = "";
    private boolean mSOAPWorking = false, mAuthOK, mAuthOK2;
    private LocalBroadcastManager lbm;
    private boolean periodicTask;

	public static final String DEBUG_SERVER_URI_SSL = "https://192.168.2.7:443";
	public static final String SERVER_URI_PRFX = "https://";
	public static final String SERVER_URI = "192.168.2.7";
	public static final String SERVER_URI_SSL_PORT = ":443";
	public static final String SERVER_URI_SSL = SERVER_URI_PRFX + SERVER_URI + SERVER_URI_SSL_PORT;//  "https://192.168.2.7:443"; //"https://85.238.112.13:443/contenti";
	public static final String LOGIN_IMEI_URI = "/hotel/i_login.php";
	public static final String LOGIN_PSW_URI = "/hotel/a_login.php";
	public static final String HELP_URI = "/hotel.htm";
	public static final String DOWNLOAD_SERVER_URI_SSL = "https://85.238.112.13:443/contenti";

	public static final String REST_WARRANTS_URI = "/hotel/hotel_online.php";
	public static final int AUTH_IMEI = 1;
	public static final int AUTH_PSW = 2;
	public static final String MOBILE = "+mobile";
	private static final int SRV_INTERVAL = 60; // seconds
	private static final int SRV_WARR_DIFF_INTERVAL = 3; // x*SRV_INTERVAL
    private static final int SRV_INTERVAL_OPER = 3; // seconds
	public static final String VER = "/ver";
	public static final String LOG_URI = "/logs";
	public static final String LOGIN = "/login";
	public static final String CHANGE_PSW = "/change_psw";
	public static final String INFO = "/info";
	public static final String SIGN_WARR = "/sign_warr";
	public static final String GET_SIGN_EMP = "/get_sign_emp/";
	public static final String GET_Z_TOOL = "/get_z_tool";
	public static final String GET_PLACEMENT = "/get_d_placement/";
	private static final String ENT = "/ent";
	private static final String ENT_GROUP = "/ent_group";
	private static final String ENT_GROUP_ENT = "/ent_group_ent";
	private static final String GDS_BRCD = "/gds_brcd";
	private static final String GDS = "/gds";
	private static final String EMPS = "/emps/";
	private static final String CHAMBERMAID = "/chambermaid/";
	private static final String USR = "/usr";
	private static final String MSGS = "/msgs";
	private static final String STATE = "/state";
	private static final String POST_INVOICE = "/invoice";
	private static final String POST_INVOICE_MARKED = "/invoice_marked";
	private static final String POST_INVOICE_GET_MARKS = "/invoice_get_marks";
	// Constants that indicate the current request state
	public static final int STATE_FINISH = 0; // process finishes
	public static final int STATE_GET_ENT = 2; // REST ent
	public static final int STATE_GET_GDS = 3; // REST gds
	public static final int STATE_GET_STATE = 4; // REST get d_state
	public static final int STATE_GET_EMPS = 5; // REST get t_emp
	public static final int STATE_GET_USER = 6; // REST get t_emp
//	public static final int STATE_GET_CHAMBER = 7; // REST get t_emp
	public static final int STATE_GET_NEW_ENT = 8;
	public static final int STATE_GET_NEW_GDS = 9;
	public static final int STATE_GET_ENT_GROUP = 10; // REST ent group
	public static final int STATE_GET_ENT_GROUP_ENT = 11; // REST ent group ent
	public static final int STATE_GET_POP_CENT = 12;
	public static final int STATE_GET_GDS_BRCD = 13; // REST 
	public static final int STATE_GET_MSGS = 30; // REST get messages
	public static final int STATE_GET_NEW_MSGS = 31; // REST get new msgs
	public static final int STATE_POST_SPECS_INVENTORY_NUM = 104; // REST post d_specs inv
	public static final int STATE_POST_INVOICE = 105; // REST post invoice
	public static final int STATE_POST_INVOICE_MARKED = 106; 
	public static final int STATE_POST_INVOICE_GET_MARKS = 107;

	private int mState = STATE_FINISH;
	private String ServerURI, notifyRingtone;
	private int serverWarrantInterval;
	private boolean notifyNewMsg;
	private boolean notifyNewMsgVibrate;
	private boolean keepLog;
	private boolean autoEmpWorktimeMark;
	private boolean keepWarrantTillReport;
	private boolean needEmpPhotoLoad;
	private int[] taskArray = new int[15];
	private DBSchemaHelper dbSch;
	private TelephonyManager tm;
	private String currentServerURI;
	public String authUserName;
	public String authUserPsw;
    private String empId = "";
    private ArrayList<HotelRoomRecord> notifyRooms;
	private String mIMEI;
	private String mPhoneNumber;
	private String wId;
	private int cntr = 1;
	private boolean firstRun = true;
	public Context mContext;
	private boolean isWorking = false;
	private boolean isRequestProcessing = false;
	private boolean isRequestResultError = false;
	public boolean sendToasts = false;
	private int  uid, idOwner;
	private NotificationManager mNManager;
    private static final int CHECK_ROOM_NOTIFY_ID = 1100+10;
    final Notification n_rooms = new Notification(R.drawable.utis_logo,
            "Комнаты: принять номер", System.currentTimeMillis());


	private long selfEmpId = 0;
	private long preMsgCount;
	private long preMaxMsgId;
	private long preMaxWarrantId;
	private int idPostInvoice = 0;

	/* Service Access Methods */
	public class ConnectionBinder extends Binder {
		ConnectionManagerService getService() {
			return ConnectionManagerService.this;
		}
	}

	private final IBinder binder = new ConnectionBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	private Handler mHandlerOper = new Handler();                                                       // ----- ##### -----
	private Runnable timerTaskOper = new Runnable() {
		int prevDayOfYear = -1;
		int updateDictCntr = 1;
		
		@Override
		public void run() {
			int curHour;
			int curDayOfYear;
			if (isWorking) {
				Calendar now = Calendar.getInstance();
				String sTime = String.format("%02d:%02d:%02d", now.get(Calendar.HOUR), now.get(Calendar.MINUTE),
						now.get(Calendar.SECOND));
				if (D) Log.d(TAG," oper: " + sTime);
				curHour = now.get(Calendar.HOUR_OF_DAY);
				curDayOfYear = now.get(Calendar.DAY_OF_YEAR);
				if (prevDayOfYear < 0) prevDayOfYear = curDayOfYear;
				if (curDayOfYear != prevDayOfYear) {
					prevDayOfYear = curDayOfYear; // starts new day
					deleteOldLogs();
				}
				cntr++;
				updateDictCntr++;
				// msg interval
				if (firstRun) {
					firstRun = false;
					deleteOldLogs();
				} else {
				}
                if (periodicTask && !mSOAPWorking) {
                   // sendSOAPRequest(STATE_GET_ROOMS);
                    Log.d(TAG, "run Oper");
                    sendSOAPRequest(STATE_GET_OPER);                                                // ----- ##### -----
                }
			}
			// Schedule the next update in one minute
			mHandlerOper.postDelayed(timerTaskOper, SRV_INTERVAL_OPER * 1000);
		}
	};

    private Handler mHandler = new Handler();
    private Runnable timerTask = new Runnable() {
        int prevDayOfYear = -1;
        int updateDictCntr = 1;

        @Override
        public void run() {
            int curHour;
            int curDayOfYear;
            if (isWorking) {
                Calendar now = Calendar.getInstance();
                String sTime = String.format("%02d:%02d:%02d", now.get(Calendar.HOUR), now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND));
                if (D) Log.d(TAG, sTime);
                curHour = now.get(Calendar.HOUR_OF_DAY);
                curDayOfYear = now.get(Calendar.DAY_OF_YEAR);
                if (prevDayOfYear < 0) prevDayOfYear = curDayOfYear;
                if (curDayOfYear != prevDayOfYear) {
                    prevDayOfYear = curDayOfYear; // starts new day
                    deleteOldLogs();
                }
                cntr++;
                updateDictCntr++;
                // msg interval
                if (firstRun) {
                    firstRun = false;
                    deleteOldLogs();
                } else {
                }
                if (periodicTask && !mSOAPWorking) {
                    sendSOAPRequest(STATE_GET_ROOMS);
                    // sendSOAPRequest(STATE_GET_OPER);                                                // ----- ##### -----
                    Log.d(TAG, ": run()");
                }
            }
            // Schedule the next update in one minute
            mHandler.postDelayed(timerTask, SRV_INTERVAL * 1000);
        }
    };


    private void getServicePrefs() {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		String serv = sharedPrefs.getString(getString(R.string.pref_sync_frequency_key), "NULL");
		try {
			serverWarrantInterval = Integer.parseInt(serv);
		} catch (NumberFormatException e) {
			serverWarrantInterval = SRV_WARR_DIFF_INTERVAL;
		}
		notifyNewMsg = sharedPrefs.getBoolean(getString(R.string.pref_notifications_new_message_key), false);
		notifyNewMsgVibrate = sharedPrefs.getBoolean(getString(R.string.pref_notifications_new_message_vibrate_key), false);
        notifyRingtone = sharedPrefs.getString(getString(R.string.pref_notifications_new_message_ringtone_key), "NULL");
		keepLog = sharedPrefs.getBoolean(getString(R.string.pref_logging_checkbox_key), true);
		CommonClass.keepLog = keepLog;
	}
    public void deleteOldLogs() {
		Calendar now = Calendar.getInstance();
		now.add(Calendar.DATE, -LOG_LIFE);
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
		String logDate = df.format(now.getTime());
		dbSch.clearOldLogs(logDate);
    }
		

	@Override
	public void onCreate() {
		dbSch = DBSchemaHelper.getInstance(this);
        lbm = LocalBroadcastManager.getInstance(this);
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mPhoneNumber = tm.getLine1Number();
		mIMEI = tm.getDeviceId();
		mContext = this;
		selfEmpId = dbSch.getUserId();
		String ns = Context.NOTIFICATION_SERVICE;
		mNManager = (NotificationManager) getSystemService(ns);
		periodicTask = false;

		ApplicationInfo ai;
		try {			
			ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
			uid = ai.uid;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			uid = 0;
		}
		mHandler.post(timerTask);
		mHandlerOper.post(timerTaskOper);

		if (D)
			Log.d(TAG, "ConnectionManager Service created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		getServicePrefs();
        Log.d(TAG, ": onStartCommand");
		idOwner = CommonClass.getOwner(mContext);
		ServerURI = CommonClass.getServerURI(mContext);
		currentServerURI = ServerURI + REST_WARRANTS_URI;
		String mLog = "ConnectionManager Service Started " + ServerURI;
		if (D) Log.d(TAG, mLog);
		if (keepLog)
			dbSch.addLogItem(LogsRecord.DEBUG, new Date(), mLog);
				
		isWorking = true;
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		firstRun = true;
		mHandler.removeCallbacks(timerTask);
		mHandlerOper.removeCallbacks(timerTaskOper);

		isWorking = false;
		String mLog = "ConnectionManager Service Destroyed";
		if (keepLog)
			dbSch.addLogItem(LogsRecord.DEBUG, new Date(), mLog);
		Toast.makeText(this, mLog, Toast.LENGTH_LONG).show();
		if (D)
			Log.d(TAG, mLog);
	}

	public void setServiceResponseCallback(ServiceResponseCallback callback) {
		mCallback = new WeakReference<ServiceResponseCallback>(callback);
	}

	public void stopWorking() {
		Toast.makeText(this, "Stopping ConnectionManager", Toast.LENGTH_SHORT)
				.show();
		isWorking = false;
	}

	public void setWorking() {
		isWorking = true;
	}

	public boolean isWorking() {
		return isWorking;
	}

    public void sendToasts(boolean value) {
        sendToasts = value;
    }

    public void setEmpId(String id) {
        empId = id;
    }

    public String getEmpId() {
        return empId;
    }

    public void periodicTask(boolean value) {
        periodicTask = value;
    }

    public void reRunTimer() {
        mHandler.removeCallbacks(timerTask);
        mHandler.postDelayed(timerTask, 1000);

        mHandlerOper.removeCallbacks(timerTaskOper);
        mHandlerOper.postDelayed(timerTaskOper, 1000);
    }

    public boolean isRequestProcessing() {
		return isRequestProcessing;
	}

	public boolean isRequestResultError() {
		return isRequestResultError;
	}

    public boolean isAuthOK() {
        return mAuthOK;
    }


	public int mState() {
		return mState;
	}

    public int getSOAPState() {
        return mSOAPState;
    }

    public void setSOAPState(int SOAPState) {
        mSOAPState = SOAPState;
    }

    public void setAuthCredentials(String userId, String userPsw) {
        mUserId = userId;
        mUserPsw = userPsw;
    }
    public void setAuthCredentials2(String positionId){
        mPositionId = positionId;
    }

	private void sendNextTask() {
		int task = getNextTask();
		sendActiveTask(task);
		switch (task) {
		case STATE_FINISH:
			break;
		case STATE_POST_SPECS_INVENTORY_NUM:
		case STATE_POST_INVOICE:
		case STATE_POST_INVOICE_MARKED:
		case STATE_POST_INVOICE_GET_MARKS:
			postStateRequest(task);
			break;
		default:
			sendRequest(task);
		}
	}

	private void clearAllTasks() {
		mState = STATE_FINISH;
		for (int i = 0; i < taskArray.length; i++) {
			taskArray[i] = STATE_FINISH;
		}
	}

	private int getNextTask() {
		int result = STATE_FINISH;
		for (int i = 0; i < taskArray.length; i++) {
			if (taskArray[i] != STATE_FINISH) {
				result = taskArray[i];
				taskArray[i] = STATE_FINISH;
				break;
			}
		}
		return result;
	}

	private boolean isNoTaskLeft() {
		boolean result = true;
		for (int i = 0; i < taskArray.length; i++) {
			if (taskArray[i] != STATE_FINISH) {
				result = false;
				break;
			}
		}
		return result;
	}

	public void getConstTables(boolean forced) {
		if (mState == STATE_FINISH) {
			prepareGetConstTask(forced);
			sendNextTask();
		}
	}
	
	public void getBarcodes(boolean forced) {
		if (mState == STATE_FINISH) {
			prepareGetBarcodesTask(forced);
			sendNextTask();
		}		
	}

	public void getEmpConstTables(boolean forced) {
		if (mState == STATE_FINISH) {
			prepareGetEmpConstTask(forced);
			sendNextTask();
		}
	}

	public void getMessagesTables() {
		if (mState == STATE_FINISH) {
			prepareGetMessagesTask();
			sendNextTask();
		}
	}

	public void postMessageTable() {
		if (mState == STATE_FINISH) {
			preparePostMessageTask();
			sendNextTask();
		}
	}

	public void postCombyMessageTable() {
		if (mState == STATE_FINISH) {
			prepareCombyPostEmpMsgTask();
			sendNextTask();
		}
	}

	public void postCombyMessageWarrantTable() {
		if (mState == STATE_FINISH) {
			prepareCombyPostEmpMsgWarrTask();
			sendNextTask();
		}
	}
	
	public void postCombyDictUpdateMessageWarrantTable() {
		if (mState == STATE_FINISH) {
			prepareCombyPostDictUpdateEmpMsgWarrTask();
			sendNextTask();
		}		
	}
	
	public void postDiscardWarrant() {
		if (mState == STATE_FINISH) {
//			preparePostWarrantsTask();
			sendNextTask();
		}
	}
	
	public void postInvoice(int idInv) {
		if (mState == STATE_FINISH) {
			idPostInvoice = idInv;
			preparePostInvoiceTask();
			sendNextTask();
		}				
	}
	
	public void postInvoiceMarked(int idInv) {
		if (mState == STATE_FINISH) {
			idPostInvoice = idInv;
			preparePostInvoiceMarkedTask();
			sendNextTask();
		}						
	}
	
	public void postInvoiceGetMarks(int idInv) {
		if (mState == STATE_FINISH) {
			idPostInvoice = idInv;
			preparePostInvoiceGetMarkTask();
			sendNextTask();
		}						
	}

	private void prepareGetConstTask(boolean forced) {		                                        // ----- ##### -----
        long cntr = 0;
		int idx = 0;
		clearAllTasks();
		cntr = dbSch.getEntCount();
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_ENT;
		cntr = dbSch.getGdsCount();
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_GDS;
		cntr = dbSch.getEntEmpsCount(idOwner);
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_EMPS;
		taskArray[idx++] = STATE_GET_STATE;
		cntr = dbSch.getBarCodeGoodsCount();
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_GDS_BRCD;

//		taskArray[idx++] = STATE_GET_USER;
	}
	
	private void prepareGetBarcodesTask(boolean forced) {
		long cntr = 0;
		int idx = 0;
		clearAllTasks();	
		cntr = dbSch.getBarCodeGoodsCount();
		if (forced || (cntr <= 0))
			taskArray[idx++] = STATE_GET_GDS_BRCD;
	}

	private void prepareGetEmpConstTask(boolean forced) {
		int idx = 0;
		clearAllTasks();	
		taskArray[idx++] = STATE_GET_EMPS;  // debug
	}

	private void prepareGetMessagesTask() {
		int idx = 0;
		long cntr = dbSch.getUserCount();
		clearAllTasks();
		taskArray[idx++] = STATE_GET_MSGS;
		if (cntr <= 0) {
//			taskArray[idx++] = STATE_GET_USER;
		} else {
			taskArray[idx++] = STATE_FINISH;
		}
	}

	private void preparePostInvoiceTask() {
        int idx = 0;
		clearAllTasks();
		taskArray[idx++] = STATE_POST_INVOICE;
	}
	
	private void preparePostInvoiceMarkedTask() {
        int idx = 0;
        clearAllTasks();
		taskArray[idx++] = STATE_POST_INVOICE_MARKED;
	}
	
	private void preparePostInvoiceGetMarkTask() {
        int idx = 0;
        clearAllTasks();
		taskArray[idx++] = STATE_POST_INVOICE_GET_MARKS;
	}
	
	private void preparePostMessageTask() {
        int idx = 0;
        long cntr = dbSch.getUserCount();
		clearAllTasks();
		taskArray[idx++] = STATE_GET_NEW_MSGS;
	}

	private void prepareCombyPostEmpMsgTask() {
		int idx = 0;
		long cntr = dbSch.getUserCount();
		clearAllTasks();
		taskArray[idx++] = STATE_POST_SPECS_INVENTORY_NUM;
		taskArray[idx++] = STATE_GET_NEW_MSGS;
	}

	private void prepareCombyPostEmpMsgWarrTask() {
		int idx = 0;
		clearAllTasks();
		taskArray[idx++] = STATE_POST_SPECS_INVENTORY_NUM;
		taskArray[idx++] = STATE_GET_NEW_MSGS;
	}
	
	private void prepareCombyPostDictUpdateEmpMsgWarrTask() {
		int idx = 0;
		clearAllTasks();
		taskArray[idx++] = STATE_GET_NEW_ENT;
//		taskArray[idx++] = STATE_GET_NEW_GDS;
	}

	private void sendRequest(Integer state) {
		try {
			String url = currentServerURI;
			String title;
			mState = state;
			switch (state) {
			case STATE_GET_ENT:
				url += ENT;
				title = "Предприятия";
                Log.d(TAG,title +": "+ url);
				break;
			case STATE_GET_ENT_GROUP:
				url += ENT_GROUP;
				title = "Группы предприятий";
                Log.d(TAG,title +": "+ url);
				break;
			case STATE_GET_ENT_GROUP_ENT:
				url += ENT_GROUP_ENT;
				title = "Предприятия и группы";
                Log.d(TAG,title +": "+ url);
				break;				
			case STATE_GET_GDS:
				url += GDS;
				title = "Товары/услуги";
                Log.d(TAG,title +": "+ url);
				break;
			case STATE_GET_GDS_BRCD:
				url += GDS_BRCD;
				title = "Штрихкоды";
                Log.d(TAG,title +": "+ url);
				break;
			case STATE_GET_NEW_ENT:
				url += ENT + "/" + CommonClass.readLastUpdate(mContext);
				title = "Предприятия";
                Log.d(TAG,"Предприятия " + title +": "+ url);
				break;
			case STATE_GET_NEW_GDS:
				url += GDS + "/" + CommonClass.readLastUpdate(mContext);
				title = "Товары/услуги";
                Log.d(TAG,title +": "+ url);
				break;
			case STATE_GET_EMPS:
				url += EMPS + idOwner;
				title = "Сотрудники";
                Log.d(TAG,title +": "+ url);
				break;
			case STATE_GET_STATE:
				url += STATE;
				title = "Справочник состояний";
                Log.d(TAG,title +": "+ url);
				break;
			case STATE_GET_USER:
				url += USR;
				title = "Пользователь";
                Log.d(TAG,title +": "+ url);
				break;
			default:
				mState = STATE_GET_ENT;
				url += ENT + "/0";
				title = "Предприятия";
                Log.d(TAG,title +": "+ url);
			}
			if (mState != STATE_FINISH) {
				if (D)
					Log.d(TAG, title);
				// Simple GET
				isRequestProcessing = true;
				HttpGet searchRequest = new HttpGet(url);
				searchRequest.addHeader(BasicScheme.authenticate(
						new UsernamePasswordCredentials(authUserName, authUserPsw), "UTF-8", false));
				RestTask task = new RestTask();
				task.setResponseCallback((ResponseCallback) mContext);
				task.execute(searchRequest);
				if (D)
					Log.d(TAG, "sendRequest: " + title);
			}
			if (keepLog)
				dbSch.addLogItem(LogsRecord.DEBUG, new Date(),
						String.format("sendRequest (%d)", mState));
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), String.format(
					"sendRequest: %s", e.getMessage()));			
		}
	}

	private void postStateRequest(Integer state) {
		try {
			String url = currentServerURI;
			String title = "";
			String warr = "";
			mState = state;
			switch (state) {
			case STATE_POST_INVOICE:
				url += POST_INVOICE;
				title = "Выгрузка накладной";
                Log.d(TAG,title +": "+ url);
				warr = dbSch.getSelectedInvoice(idPostInvoice);
				break;
			case STATE_POST_INVOICE_MARKED:
				url += POST_INVOICE_MARKED;
				title = "Выгрузка изменений накладной";
                Log.d(TAG,title +": "+ url);
				warr = dbSch.getSelectedInvoiceMarked(idPostInvoice);
				break;
			case STATE_POST_INVOICE_GET_MARKS:
				url += POST_INVOICE_GET_MARKS;
				title = "Запрос пересчета накладной";
                Log.d(TAG,title +": "+ url);
				warr = dbSch.getSelectedInvoiceGetMarks(idPostInvoice);				
				break;
			default:
				mState = STATE_FINISH;
			}
			if (mState != STATE_FINISH) {
				if (D)
					Log.d(TAG, title);
				if (warr.length() > 0) {
					isRequestProcessing = true;
					// Simple POST
					HttpPost postRequest = new HttpPost(url);
					postRequest.addHeader(BasicScheme.authenticate(
							new UsernamePasswordCredentials(authUserName,
									authUserPsw), "UTF-8", false));
					StringEntity se = new StringEntity(warr, "UTF-8");
					se.setContentType("application/json;charset=UTF-8");
					se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
							"application/json;charset=UTF-8"));
					postRequest.setEntity(se);

					// if (mState == STATE_POST_EMP_LOCATIONS)
					// dbSch.addLogItem(LogsRecord.DEBUG, new Date(),
					// "EMP_LOCATIONS: " + warr);
					if (keepLog)
						dbSch.addLogItem(LogsRecord.DEBUG, new Date(), String.format("postStateRequest (%d): size=%d",
										mState, warr.length()));
					
					RestTask task = new RestTask();
					task.setResponseCallback((ResponseCallback) mContext);
					task.execute(postRequest);
					if (D)
						Log.d(TAG, "postStateRequest: " + title);
				} else {
					String sRes = "Нет изменений для передачи!";
					if (D)
						Log.d(TAG, sRes);
					sendTaskResult(sRes);
					if (keepLog)
						dbSch.addLogItem(LogsRecord.DEBUG, new Date(), String.format("postStateRequest (%d): size=%d",
										mState, warr.length()));
					checkNextTask();
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), String.format(
					"postStateRequest: %s", e.getMessage()));			
		}
	}

	@Override
	public void onRequestSuccess(String response) {
		if (D) Log.d(TAG, "onRequestSuccess");
		isRequestResultError = false;
		String resp;
		if (response.length() > 50)
			resp = response.substring(1, 50);
		else
			resp = response;
		if (keepLog)
			dbSch.addLogItem(LogsRecord.INFO, new Date(), String.format(
					"(%d)response: (size=%d); %s", mState, response.length(), resp));

		ParseJSONArray parseJSON = new ParseJSONArray();
		parseJSON.execute(response);
	}

	@Override
	public void onRequestError(Exception error) {
		isRequestResultError = true;
		isRequestProcessing = false;
		clearAllTasks();
		if (D) Log.d(TAG, "onRequestError: " + error.getMessage());
		if (keepLog) 
			dbSch.addLogItem(LogsRecord.ERROR, new Date(),
					String.format("(%d)error: %s", mState, error.getMessage()));

		if (sendToasts && mCallback != null && mCallback.get() != null) {
			mCallback.get().onSrvRequestError(error);
		}
	}

	private class ParseJSONArray extends AsyncTask<String, Integer, Boolean> {
		String returnString = null;

		@Override
		protected Boolean doInBackground(String... sJSONStr) {
			long id, idWarrLocal, idContract;
			int id_const, id_parent, ttype, gdstype;
			int num, jobCnt, idEnt, idSub, idOwner, idState;
			Double lat, lng;
			Date dOpen;
			String sLat, sLng;
			String nm, addr, job, srl, inv;
			String openDate, sRem;
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			SimpleDateFormat showDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			boolean lastTask = isNoTaskLeft();
			try {
				JSONArray records = new JSONArray(sJSONStr[0]);
				// this will be useful so that you can show a typical 0-100% progress bar
				int recLength = records.length();
				returnString = String.format("Task=%d; Found: %d recs", mState, recLength);
				dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "ParseJSONArray: " + returnString);

				if (mState == STATE_GET_STATE) {
					dbSch.emptyStateTable();
				} else if (mState == STATE_GET_EMPS) {
					dbSch.emptyEmpTable();
				}
				long cntr;
				switch (mState) {
				case STATE_GET_ENT:
					dbSch.emptyTable(EntTable.TABLE_NAME);
					dbSch.insEntBulkJSON(records);
					cntr = dbSch.getEntCount();
					break;
				case STATE_GET_GDS:
					dbSch.emptyTable(GoodsTable.TABLE_NAME);
					dbSch.insGoodsBulkJSON(records);
					cntr = dbSch.getGdsCount();
					break;
				case STATE_GET_GDS_BRCD:
					dbSch.emptyTable(BarCodeGoodsTable.TABLE_NAME);
					dbSch.insBarCodeGoodsBulkJSON(records);
					cntr = dbSch.getBarCodeGoodsCount();					
					break;
				case STATE_GET_EMPS:
					needEmpPhotoLoad = false;
					dbSch.emptyTable(EmpTable.TABLE_NAME);
					dbSch.insEmpsBulkJSON(records);
					cntr = dbSch.getEmpsCount();
					break;

				}
				// if (D) Log.d(TAG, returnString);
				if (mState != STATE_GET_ENT && mState != STATE_GET_GDS && mState != STATE_GET_POP_CENT
						&& mState != STATE_GET_GDS_BRCD) {
					long startTime = System.currentTimeMillis();
					for (int i = 0; i < records.length(); i++) {
						JSONObject warrant = records.getJSONObject(i);
						switch (mState) {
						case STATE_GET_NEW_ENT:
							id = warrant.getInt("id");
							id_parent = warrant.getInt("id_p"); 
							sLat = warrant.getString("lat"); 
							lat = (double) Float.parseFloat(sLat.replace(",", "."));
							sLng = warrant.getString("lng"); 
							lng = (double) Float.parseFloat(sLng.replace(",", "."));
							nm = warrant.getString("nm"); 
							addr = warrant.getString("addr"); 
							dbSch.addEntItem(id, id_parent, lat, lng, addr, nm, true);
                            Log.d(TAG, String.valueOf(id)+ " // "+ String.valueOf(id_parent) +" // "+ nm);
							break;
						case STATE_GET_NEW_GDS:
							id = warrant.getInt("id"); 
							id_parent = warrant.getInt("id_p"); 
							ttype = warrant.getInt("ttype"); 
							gdstype = warrant.getInt("gdstype"); 
							nm = warrant.getString("nm"); 
							dbSch.addGdsItem(id, id_parent, ttype, gdstype, nm, true);
                            Log.d(TAG, String.valueOf(id)+ " // "+ String.valueOf(id_parent) +" / "+ nm);
							break;			
						case STATE_GET_STATE:
							id_const = warrant.getInt("id");
							id_parent = warrant.getInt("id_doc");
							nm = warrant.getString("nm");
							dbSch.addStateItem(id_const, id_parent, /* ttype, */nm);
                            Log.d(TAG, String.valueOf(id_const)+ " // "+ String.valueOf(id_parent) +" / "+ nm);
							break;
						case STATE_GET_USER:
							id = warrant.getInt("id");
							dbSch.addUserItem(id);
                            Log.d(TAG, String.valueOf(id));
							break;
						case STATE_GET_NEW_MSGS:
						case STATE_GET_MSGS:
							try {
								CommonClass.lastErrorMsg = "";
								id_const = warrant.getInt("id");
								id_parent = warrant.getInt("id_sender");
								int id_recipient = warrant.getInt("id_recip");
								idState = warrant.getInt("id_state");
								openDate = warrant.getString("m_date");
								dOpen = sDateFormat.parse(openDate);
								addr = warrant.getString("subj");
								nm = warrant.getString("msg");
								int status = warrant.getInt("status");
								int a_size = warrant.getInt("a_sz");
								String a_nm = warrant.getString("a_nm");
//								dbSch.addMsgItem(id_const, id_parent, id_recipient,idState, status, a_size, dOpen, addr, nm,a_nm);
                                Log.d(TAG, String.valueOf(id_const)+ " / "+ openDate +" / "+ nm);
							} catch (Exception e) {
								id_const = warrant.getInt("code");
								nm = warrant.getString("result");
								CommonClass.lastErrorMsg = nm;
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), "GET_MSGS error: "
										 + id_const + "; " + nm);
							}							
							break;
						case STATE_POST_INVOICE:
							nm = warrant.getString("result");
							id_const = warrant.getInt("code");
							if (0 == id_const) {
								JSONArray invResultArray = warrant.getJSONArray("inv_res");
								for (int inv1 = 0; inv1 < invResultArray.length(); inv1++) {
									JSONObject invResult = invResultArray.getJSONObject(inv1);
									long inv_new_id = invResult.getLong("id_new");
									int inv_loc_id = invResult.getInt("id_loc");
									String except = invResult.getString("except");
									if (inv_new_id > 0) {
										dbSch.updateInvoiceExtId(inv_loc_id, inv_new_id);
										JSONArray invContResultArray = invResult.getJSONArray("inv_cont");
										for (int inv11 = 0; inv11 < invContResultArray.length(); inv11++) {
											JSONObject invContResult = invContResultArray.getJSONObject(inv11);
											long inv_cont_new_id = invContResult.getLong("id_new");
											int inv_cont_loc_id = invContResult.getInt("id_loc");
											String except_cont = invContResult.getString("except");
											if (inv_cont_new_id > 0) {
												dbSch.updateInvoiceContExtId(inv_cont_loc_id, inv_cont_new_id);
											} else {
												dbSch.addLogItem(LogsRecord.ERROR, new Date(), "STATE_POST_INVOICE error: "
														 + inv_cont_loc_id + "; " + except_cont);																							
											}
										}										
									} else {
										dbSch.addLogItem(LogsRecord.ERROR, new Date(), "STATE_POST_INVOICE error: "
												 + inv_loc_id + "; " + except);											
									}
								}								
							} else {
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), "STATE_POST_INVOICE error: "
										 + id_const + "; " + nm);	
							}
							idPostInvoice = 0;
							break;
						case STATE_POST_INVOICE_MARKED:
							nm = warrant.getString("result");
							id_const = warrant.getInt("code");
							if (0 == id_const) {
								JSONArray invResultArray = warrant.getJSONArray("inv_res");
								for (int inv1 = 0; inv1 < invResultArray.length(); inv1++) {
									JSONObject invResult = invResultArray.getJSONObject(inv1);
									long inv_new_id = invResult.getLong("id_new");
									int inv_loc_id = invResult.getInt("id_loc");
									String except = invResult.getString("except");
									if (inv_new_id > 0) {
//										dbSch.updateInvoiceExtId(inv_loc_id, inv_new_id);
										JSONArray invContResultArray = invResult.getJSONArray("inv_cont");
										for (int inv11 = 0; inv11 < invContResultArray.length(); inv11++) {
											JSONObject invContResult = invContResultArray.getJSONObject(inv11);
											long inv_cont_new_id = invContResult.getLong("id_new");
											int inv_cont_loc_id = invContResult.getInt("id_loc");
											String except_cont = invContResult.getString("except");
											if (inv_cont_new_id > 0) {
												dbSch.updateInvoiceContClearMark(inv_cont_loc_id);
											} else {
												dbSch.addLogItem(LogsRecord.ERROR, new Date(), "STATE_POST_INVOICE_MARKED error: "
														 + inv_cont_loc_id + "; " + except_cont);																							
											}
										}										
									} else {
										dbSch.addLogItem(LogsRecord.ERROR, new Date(), "STATE_POST_INVOICE_MARKED error: "
												+ inv_loc_id + "; " + except);
									}
								}								
							} else {
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), "STATE_POST_INVOICE_MARKED error: "
										 + id_const + "; " + nm);	
							}
							idPostInvoice = 0;							
							break;
						case STATE_POST_INVOICE_GET_MARKS:
							nm = warrant.getString("result");
							id_const = warrant.getInt("code");
							if (0 == id_const) {
								JSONArray invResultArray = warrant.getJSONArray("inv_res");
								for (int inv1 = 0; inv1 < invResultArray.length(); inv1++) {
									JSONObject invResult = invResultArray.getJSONObject(inv1);
									long inv_cont_id = invResult.getLong("id");
									int mark = invResult.getInt("mark");
									if (inv_cont_id > 0) {
										dbSch.updateInvoiceContMark(inv_cont_id, mark);
									}
								}								
							} else {
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), "STATE_POST_INVOICE_GET_MARKS error: "
										 + id_const + "; " + nm);	
							}							
							break;
						default:
						}
						publishProgress((int) (i * 100 / recLength));
					}
					long endtime = System.currentTimeMillis();
					Log.i(TAG, "Time to insert Members: "+ String.valueOf(endtime - startTime));
				}
			} catch (Exception e) {
				dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), String.format(
						"ParseJSONArray: %s", e.getMessage()));													
				e.printStackTrace();
				returnString = "Exception: " + e.getMessage();
				return false;
			} finally {
				switch (mState) {
					case STATE_GET_MSGS:
	/*					long MsgCount = dbSch.getRecipientNewMsgsCount(selfEmpId);
						if (MsgCount > preMsgCount && notifyNewMsg) {
							sendNewMsgNotification();
						}
	*/					break;
					case STATE_GET_NEW_MSGS:
	/*					long MsgId = dbSch.getRecipientMaxMsgsId(selfEmpId);
						if (MsgId > preMaxMsgId && notifyNewMsg) {
							sendNewMsgNotification();
						}
	*/					break;
				}
				mState = STATE_FINISH;
			}
			return true;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// mProgressDialog.show();
		}
		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			// mProgressDialog.setProgress(progress[0]);
		}
		protected void onPostExecute(Boolean success) {
			// mProgressDialog.setProgress(0);
			// mProgressDialog.dismiss();
			if (D)
				Log.d(TAG, "onPostExecute");

			checkNextTask();
		}
	}


    public boolean sendSOAPRequest(int state) {
        boolean res = false;
        try {
            if (!mSOAPWorking) {
                mSOAPState = state;
                switch (mSOAPState) {
                    case STATE_GET_FIO:
                        SOAPMethod = METHOD_FIO;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        Log.d(TAG,"STATE_GET_FIO: " + SOAPAction);
                        break;
                    case STATE_GET_ROOMS:
                        SOAPMethod = METHOD_STATUS;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        Log.d(TAG,"STATE_GET_ROOMS: " +  SOAPAction);
                        break;
                    case STATE_GET_OPER:
                        SOAPMethod = METHOD_GET_OPER;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        Log.d(TAG,"STATE_POST_OPER: " + SOAPAction);
                        break;
                    case STATE_POST_AUTH:
                        SOAPMethod = METHOD_POST_AUTH;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        Log.d(TAG,"STATE_POST_AUTH: " + SOAPAction);
                        break;
                    case STATE_POSITION:
                        SOAPMethod = STATE_GET_POSITION;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        Log.d(TAG,"STATE_POSITION: " + SOAPAction);
                        break;
                    default:
                        SOAPMethod = "";
                        SOAPAction = "";
                }
                if (SOAPMethod.length() > 0 && SOAPAction.length() > 0) {
                    res = true;
                    AsyncTaskRunner runner = new AsyncTaskRunner();
                    runner.execute();
                    Log.d(TAG, " AsyncTaskRunner ");
                }
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, e.getMessage());
        } finally {
            return res;
        }
    }

	private class AsyncTaskRunner extends AsyncTask<String, String, String> {
		private String resp = "";
		private boolean isAuthOK = false, mException = false;
		private boolean isAuthOK2 = false;
        private Exception exception;

		@Override
		protected String doInBackground(String... params) {
			publishProgress(mContext.getString(R.string.title_init_load)); // Calls onProgressUpdate()
			try {
				SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
				SoapObject request = new SoapObject(CommonClass.NAMESPACE, SOAPMethod);
				if (mSOAPState == STATE_POST_AUTH) {
					request.addProperty("id", mUserId);
					request.addProperty("Password", mUserPsw);

                    Log.d(TAG, mUserId +" / " + mUserPsw);
                    Log.d(TAG, String.valueOf(STATE_POST_AUTH));
				}
//
                if(mSOAPState == STATE_POSITION ){
                    request.addProperty("id", mPositionId);
                    Log.d(TAG, mPositionId);
                    Log.d(TAG, String.valueOf(STATE_POSITION));
                }
                if(mSOAPState == STATE_GET_OPER  ){
                    Log.d(TAG, String.valueOf(STATE_GET_OPER));
                }

               // mSOAPState = 0;

				envelope.bodyOut = request;
				envelope.setAddAdornments(false);
				envelope.implicitTypes = true;

				if (mSOAPState != STATE_POST_AUTH)
					dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "Request: " + request.toString());

				HttpTransportSE transport = new HttpTransportSE(CommonClass.getServerURI(mContext));
				transport.debug = true;
				int cntr = 0; boolean responseOK = false;
				while (cntr < CommonClass.FAIL_REPETITIONS && !responseOK) {
					try {
						cntr++;
						String auth = CommonClass.LOGIN + ":" + CommonClass.PSW;
						List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
						headerList.add(new HeaderProperty("Authorization",
								"Basic " + org.kobjects.base64.Base64.encode(auth.getBytes())));

						SoapObject response = null;
						transport.call(SOAPAction, envelope, headerList);
						transport.reset();
						if (envelope.bodyIn != null) {
							responseOK = true;
							if (envelope.bodyIn instanceof SoapObject) {
								publishProgress(mContext.getString(R.string.title_parse)); // Calls onProgressUpdate()
								response = (SoapObject) envelope.bodyIn;

                                Log.d(TAG, "if () " + STATE_GET_ROOMS +" == "+ mSOAPState + " responce: " + response);

								if (STATE_GET_ROOMS == mSOAPState)
									writeToFile(transport.responseDump.toString());
								parseSOAPResponse(response);                                        // ----- ##### -----

                                Log.d(TAG,"response: "+  String.valueOf(response));


							} else if (envelope.bodyIn instanceof SoapFault12) {
								resp = ((SoapFault12) envelope.bodyIn).getMessage().toString();
								String msg = TAG + " " + mSOAPState + "; " + resp;
								dbSch.addLogItem(LogsRecord.ERROR, new Date(), msg);
							}
						} else {
							if (transport.responseDump != null) {
								resp = getString(R.string.m_post_err) + ": " + transport.responseDump.toString();
							} else {
								resp = getString(R.string.m_post_err) + ": responseDump is NULL";
							}
							dbSch.addLogItem(LogsRecord.ERROR, new Date(), resp);
						}
					} catch (IOException e) {
						resp = "IOException: " + e.getMessage();
						dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), resp);
						Log.e(TAG, resp);
						resp = "";
					} catch (XmlPullParserException e) {
						resp = "XmlParseException: " + e.getMessage();
						dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), resp);
						Log.e(TAG, resp);
                        mException = true;
                        exception = e;
					}
				}
			} catch (Exception e) {
				resp = "Exception: " + e.getMessage();
				Log.e(TAG, resp);
				dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), resp);
                mException = true;
                exception = e;
            }
			return resp;
		}

		private void parseSOAPResponse(SoapObject response) {                                       // ----- ##### -----
			switch (mSOAPState) {
				case STATE_GET_FIO:
					parseEmpResponse(response);
                    Log.d(TAG," > "+ SOAPAction + " == " + STATE_GET_FIO);
					break;
				case STATE_GET_ROOMS:
					parseRoomStateResponse(response);
                    Log.d(TAG," > "+  SOAPAction+ " == " + STATE_GET_ROOMS);
					break;
                case STATE_POSITION:
                    parsePositionResponse(response);
                    Log.d(TAG," > "+  SOAPAction+ " == " + STATE_POSITION);
                    break;

                case STATE_GET_OPER:
                    parseOperResponse(response);
                    Log.d(TAG," > "+  SOAPAction+ " == " + STATE_GET_OPER);
                    break;

				case STATE_POST_AUTH:
					parsePostAuthResponse(response);
                    Log.d(TAG," > "+  SOAPAction+ " == " + STATE_POST_AUTH);
					break;



			}
		}

		private void parsePostAuthResponse(SoapObject response) {
			String result = response.getProperty("return").toString();
			try {
				isAuthOK = Boolean.parseBoolean(result);
			} catch (Exception e) {
				isAuthOK = false;
			} finally {
                mAuthOK = isAuthOK;
            }
        }

		private void parseRoomStateResponse(SoapObject response) {
			dbSch.emptyTable(HotelRoomTable.TABLE_NAME);
			SoapObject t = (SoapObject) response.getProperty("return");
			for (int i = 0; i < t.getPropertyCount(); i++) {
				SoapObject bar = (SoapObject) t.getProperty(i);
				String idHotel = bar.getProperty(HotelRoomRecord.ID_HOTEL).toString();
				String hotel = bar.getProperty(HotelRoomRecord.HOTEL_NAME).toString();
				String floor = bar.getProperty(HotelRoomRecord.FLOOR).toString();
				String room = bar.getProperty(HotelRoomRecord.ROOM).toString();
				String occup = bar.getProperty(HotelRoomRecord.OCCUPIED).toString();
				String guest = bar.getProperty(HotelRoomRecord.GUEST).toString();
				String service = bar.getProperty(HotelRoomRecord.SERVICE).toString();
				String clear = bar.getProperty(HotelRoomRecord.CLEARENCE).toString();
				String door = bar.getProperty(HotelRoomRecord.DOOR).toString();
				String window = bar.getProperty(HotelRoomRecord.WINDOW).toString();
				String balcony = bar.getProperty(HotelRoomRecord.BALCONY).toString();
                String changeBedDate = bar.getProperty(HotelRoomRecord.CHANGE_BED).toString();
                String departureDate = bar.getProperty(HotelRoomRecord.DEPARTURE_DATE).toString();
                String twin = bar.getProperty(HotelRoomRecord.TWIN).toString();
                String water = bar.getProperty(HotelRoomRecord.WATER_LEAKAGE).toString();
                String check = bar.getProperty(HotelRoomRecord.CHECK_ROOM).toString();
                String reservDate = bar.getProperty(HotelRoomRecord.RESERVATIONS).toString();
                String statNom = bar.getProperty(HotelRoomRecord.STATUS_NOM).toString();
                String noteRepair = bar.getProperty(HotelRoomRecord.NOTE_REPAIR).toString();
                String changeBedExtra = bar.getProperty(HotelRoomRecord.CHANGE_BED_EXTRA).toString();
                String repair = bar.getProperty(HotelRoomRecord.REPAIR).toString();
                String tipeClearence = bar.getProperty(HotelRoomRecord.TIPE_CLEARENCE).toString();
                String notDesturb = bar.getProperty(HotelRoomRecord.NOT_DISTURB).toString();
                String callChambermaid = bar.getProperty(HotelRoomRecord.CALL_CHAMBERMAID).toString();
                String inRoom = bar.getProperty(HotelRoomRecord.CHAMBERMAID_IN_ROOM).toString();

				HotelRoomRecord hotelRoomRecord = new HotelRoomRecord(idHotel, hotel, floor,
						room, occup, guest, service, clear, door, window, balcony,
                        changeBedDate, departureDate, twin, water, reservDate, check
                        ,statNom, repair, noteRepair, changeBedExtra,tipeClearence, notDesturb, callChambermaid, inRoom);
                 Log.d(TAG, room + " check:  " + check + " status: " + statNom + " occup: " + occup +
                         " inRoom: " + inRoom + " callChamb: " + callChambermaid + " notDesturb: "+ notDesturb + " tipeClearence: "
                         + tipeClearence + " repair:  " + repair + " changeBedExtra: " + changeBedExtra +
                        " guest: " + guest  + " door: " + door + " window: " + window + " balcony: " + balcony + " service: " + service + " clear: " + clear + " changeBedDate: " + changeBedDate  + " changeBedExtra: " + changeBedExtra);
                if (hotelRoomRecord.getRoomFloor() > 0 && hotelRoomRecord.getRoomNum() > 0)
					dbSch.addHotelRoomItem(hotelRoomRecord);
			}
			long cntr = dbSch.getHotelRoomCount();
			if (cntr > 0) {
				dbSch.fillHotelInfo(getString(R.string.m_floor));
                cntr = dbSch.getHotelRoomNeedsCheckingCount();
                if (cntr > 0) {
                    notifyRooms = dbSch.getHotelRoomArrayNeedsChecking();
//                    String s = dbSch.getHotelRoomNeedsChecking();
                    sendNewCheckRoomNotification();
               }
			}
        }


        private void parseOperResponse(SoapObject response) {
            dbSch.emptyTable(OperTable.TABLE_NAME);
            SoapObject t = (SoapObject) response.getProperty("return");
            for (int i = 0; i < t.getPropertyCount(); i++) {
                SoapObject bar = (SoapObject) t.getProperty(i);
                String room = bar.getProperty(OperRecord.ROOM).toString();
                String check = bar.getProperty(OperRecord.CHECK_ROOM).toString();
                String callChambermaid = bar.getProperty(OperRecord.CALL_CHAMBERMAID).toString();
                String inRoom = bar.getProperty(OperRecord.CHAMBERMAID_IN_ROOM).toString();
                String quit = bar.getProperty(OperRecord.CHAMBERMAID_QUIT).toString();
                String surname = bar.getProperty(OperRecord.SURNAME).toString();
                String name = bar.getProperty(OperRecord.NAME).toString();
//Nоm=201; PrinytNom=true; vizovGorn=false; idHotell=000000001; GornInNom=false; GornQuit=false; idGorn=0;
                OperRecord operRecord = new OperRecord(  room, check, callChambermaid,
                        inRoom , quit, surname, name);
                dbSch.addOperItem(operRecord);
                 Log.d(TAG, room + " call:  " + check + " surname: " + surname + " inRoom: " + inRoom + " callChambermaid: "
                 + callChambermaid + " idHotel: "
                         + " quit: " + quit);
            }
            long cntr = dbSch.getOperCount();

        }


		private void parseEmpResponse(SoapObject response) {
			dbSch.emptyTable(EmpTable.TABLE_NAME);
			SoapObject t = (SoapObject) response.getProperty("return");
			for (int i = 0; i < t.getPropertyCount(); i++) {
				SoapObject bar = (SoapObject) t.getProperty(i);
				String idEmp = bar.getProperty(EmpRecord.E_ID).toString();
				String empFIO = bar.getProperty(EmpRecord.FIO).toString();
				EmpRecord empRec = new EmpRecord(idEmp, empFIO);
				dbSch.addEmpItem(empRec);
                Log.d(TAG,"parseEmpResponse: " + idEmp+" / "+ empFIO);
			}
			long cntr = dbSch.getEmpsCount();
		}

        private void parsePositionResponse(SoapObject response) {


            dbSch.emptyTable(EmpTable.TABLE_NAME);
            SoapObject t = (SoapObject) response.getProperty("return");
            for (int i = 0; i < t.getPropertyCount(); i++) {
                SoapObject bar = (SoapObject) t.getProperty(i);
                String idEmp = bar.getProperty(EmpRecord.E_ID).toString();
                String empFIO = bar.getProperty(EmpRecord.FIO).toString();
                EmpRecord empRec = new EmpRecord(idEmp, empFIO);
                dbSch.addEmpItem(empRec);
                Log.d(TAG,"parsePositionEmpResponse: " + idEmp+" / "+ empFIO);
            }
            long cntr = dbSch.getEmpsCount();
        }

        @Override
        protected void onPostExecute(String result) {
            mSOAPWorking = false;
            if (mException) {
                if (sendToasts && mCallback != null && mCallback.get() != null) {
                    mCallback.get().onSrvRequestError(exception);
                }
            } else {
//                sendCaptionUpdateMessage(result);
                sendTaskResult(result);
            }
		}

		@Override
		protected void onPreExecute() {
            mSOAPWorking = true;
		}

		@Override
		protected void onProgressUpdate(String... text) {
            sendCaptionUpdateMessage(text[0]);
		}

	}

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    openFileOutput(CommonClass.DUMP_FILE_NAME, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }
    }

    private void sendTaskUpdateMessage(int task) {
        Intent intent = new Intent(GET_TASK_ACTION);
        Bundle b = new Bundle();
        b.putString("task", Integer.toString(task));
        intent.putExtras(b);
        lbm.sendBroadcast(intent);
    }

    private void sendCaptionUpdateMessage(String caption) {
        Intent intent = new Intent(SET_CAPTION_ACTION);
        Bundle b = new Bundle();
        b.putString("txt", caption);
        intent.putExtras(b);
        lbm.sendBroadcast(intent);
    }

    private String getHotelRoomNeedsChecking() {
        String res = "";
        if (notifyRooms != null) {
            for (HotelRoomRecord roomRecord: notifyRooms) {
                if (roomRecord.getRoomNum() > 0) {
                    if (res.length() == 0)
                        res += roomRecord.getHotelName() + "/№" + roomRecord.getRoomNum();
                    else
                        res += "\n" + roomRecord.getHotelName() + "/№" + roomRecord.getRoomNum();
                }
            }
        }
        return res;
    }

    private void sendNewCheckRoomNotification() {
        if (notifyNewMsg && empId.length() > 0 && notifyRooms != null && notifyRooms.size() > 0) {
            String msg = getHotelRoomNeedsChecking();
            Context context = getApplicationContext();
            CharSequence contentTitle = "Требуется проверка комнат";
            CharSequence contentText = "Комнаты " + msg;
            Intent msgIntent = new Intent(this, HotelRoomsSwipeActivity.class);
            Bundle b = new Bundle();
            b.putString("idEmp", empId);
            b.putString("idHotel", notifyRooms.get(0).getHotelIdStr());
            b.putString("Hotel", "" + notifyRooms.get(0).getHotelName());
            b.putString("Floor", "" + notifyRooms.get(0).getRoomFloor());
            msgIntent.putExtras(b);

            PendingIntent intent = PendingIntent.getActivity(
                    ConnectionManagerService.this, 0, msgIntent, Intent.FLAG_ACTIVITY_NEW_TASK); // FLAG_ACTIVITY_NEW_TASK
            if (notifyRingtone != null && !notifyRingtone.contentEquals("NULL"))
                n_rooms.sound = Uri.parse(notifyRingtone);
            else
                n_rooms.defaults |= Notification.DEFAULT_SOUND;
            if (notifyNewMsgVibrate)
                n_rooms.defaults |= Notification.DEFAULT_VIBRATE;
            n_rooms.flags |= Notification.FLAG_AUTO_CANCEL;
            n_rooms.setLatestEventInfo(context, contentTitle, contentText, intent);
            mNManager.notify(CHECK_ROOM_NOTIFY_ID, n_rooms);
        }
    }

	private void checkNextTask() {
		if (isNoTaskLeft()) {
			mState = STATE_FINISH;
			//sendNotification();
			isRequestProcessing = false;
			sendTaskResult("Операция выполнена успешно!");
			sendActiveTask(mState);
			if (keepLog)
				dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "NoTaskLeft");
		} else {
			sendNextTask();
		}
	}

	private void sendTaskResult(String res) {
		if (sendToasts && mCallback != null && mCallback.get() != null) {
			mCallback.get().onSrvRequestSuccess(res);
		}
	}

	private void sendActiveTask(int Task) {
		if (sendToasts && mCallback != null && mCallback.get() != null) {
			mCallback.get().onSrvTaskChanged(Task);
		}
	}



}
