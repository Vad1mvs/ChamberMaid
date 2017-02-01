package com.utis.chambermaid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.chambermaid.notnow.ShowDumpActivity;
import com.utis.chambermaid.records.EmpRecord;
import com.utis.chambermaid.records.EntRecord;
import com.utis.chambermaid.records.LogsRecord;
import com.utis.chambermaid.records.OperRecord;
import com.utis.chambermaid.records.SignUserRecord;
import com.utis.chambermaid.records.ZToolRecord;
import com.utis.chambermaid.tables.EmpTable;
import com.utis.chambermaid.tables.EntTable;
import com.utis.chambermaid.tables.OperTable;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity implements ConnectionManagerService.ServiceResponseCallback {
	private static final boolean D = true;
	private static final String TAG = "LoginActivity";
	private static final int STATE_POST_AUTH = 6;
	private static final int STATE_POSITION = 17;
	private static final String KEY_USER = "loginUser";
    private static final String MAX_DATE_ENABLED = "15.09.2015";

    public static final int STATE_FINISH = 0; // process finishes
    private static final int STATE_GET_FIO = 1;
    private static final int STATE_GET_ROOMS = 2;
    private static final int STATE_GET_OPER = 3;
    private static final String METHOD_STATUS = "GetStatusNom";
    private static final String METHOD_FIO = "GetFio";
	private static final String METHOD_POST_AUTH = "SendPassword";

    private String SOAPAction, SOAPMethod;

	private SharedPreferences prefs;
	private int savedUserPos;
    private ConnectionManagerService connectionService;
    private Intent serviceIntent;
    BroadcastReceiver onNotice;
    private ArrayAdapter<OperRecord> mDBArrayAdapter2;

	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	private static final String DEBUG_USER_NAME = "CONTENT_65";

	// Values for email and password at the time of the login attempt.
	public String mEmail;
	public String mPassword;
	public String mNewPassword;
    public String mPosition;
    public String mUser;

    public boolean idUserExist;
    public boolean idHotelExist;
    public boolean idUser = false;

	public Context mContext;
	// UI references.
//	private Layout loginLayout, ownerLayout;
	private View loginLayout, ownerLayout;
	private Spinner mUserSpinner, mEntSpinner, mOwnerSpinner,  spinner;
	private EditText mEmailView, mPasswordView;
	private EditText mPasswordNewView, mPasswordNew2View;
	private View signInButton, changePswButton;
	private View okButton, okOwnerButton;
	private View cancelButton, cancelOwnerButton;
	private View okCancelView, OKCancelOwnerView;
	private View signInView, mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView, caption, tvId, tvEnt;
	private DBSchemaHelper dbSch;
    private TelephonyManager tm;
    private String mSIMId, returnString;
    private String mPhoneNumber, mIMEI;
    private String uLogin = "";
    private boolean signMode = false;
    private int mState = 0, mSOAPState = STATE_FINISH;
    private ProgressDialog mProgress;
//    private ArrayAdapter<UserRecord> mDBArrayAdapter;
    private ArrayAdapter<EmpRecord> mDBArrayAdapter;
    private ArrayAdapter<OperRecord> mDBArrayFilterAdapter;
    private ArrayAdapter<EntRecord> mDBEntArrayAdapter;
    private ArrayAdapter<ZToolRecord> mDBZToolArrayAdapter;
    String arr [];
//    private UserRecord userSelected;
    public EmpRecord userSelected;
    private EntRecord hotelSelected;
    private ZToolRecord zToolSelected;
    private SignUserRecord signUserSelected;
    private String uFullName;
    boolean debugMode;
    private LocalBroadcastManager lbm;
    List<String> myList = new ArrayList<String>();
    String idName = "";
    String  ssid, strUser, strUserId, nameHotel, idHotel , surname;

    private class CustomSignUserAdapter extends ArrayAdapter<SignUserRecord> {
    	private int id;
  	
		public CustomSignUserAdapter(Context context, int textViewResourceId) {
			//Call through to ArrayAdapter implementation 
			super(context, textViewResourceId);
			id = textViewResourceId;
		} 
		
		private void setColor(View mView, int position) {
			 TextView tv = (TextView) mView.findViewById(android.R.id.text1);
		     if (tv != null) {
		    	 SignUserRecord item = getItem(position);
		    	 if (item.userOnline.length() == 0)
		    		 tv.setTextColor(Color.RED);
		    	 else
		    		 tv.setTextColor(Color.BLACK);
		      }		        			
		}
  	
		  @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			 View mView =  super.getView(position, convertView, parent);
			 setColor(mView, position);
		     return mView;
		}
		  
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
		     View mView = super.getDropDownView(position, convertView, parent);
		     setColor(mView, position);
		     return mView;
		}		  
    }


    private BroadcastReceiver onTask = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            if (b != null && b.containsKey("task")) {
                mSOAPState = Integer.parseInt(b.getString("task"));
            }
            String message = b.getString("message");
//            updateResultsDelayed();
        }
    };



    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		mContext = this;
		dbSch = DBSchemaHelper.getInstance(this);
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		getTelephonyInfo();
		long cntr = dbSch.getUserCount();
		prefs = getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
        mPosition = CommonClass.getPosition(this);
        mUser = CommonClass.getUserID(this);

        Log.d(TAG, "mPosition: "+mPosition +" mUser: "+ mUser);
        idUserExist = false;
        idHotelExist = false;

        Intent intent = getIntent();
        nameHotel = intent.getStringExtra("name");
        idHotel = intent.getStringExtra("idHotel");
        tvEnt = (TextView)findViewById(R.id.tvEnt);
        tvEnt.setText(nameHotel);
        Log.d(TAG, "######### " + nameHotel + " / " + idHotel);

		// Set up the login form.
		mEmail = "";//getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText(mEmail);
        mEmailView.setVisibility(View.GONE);
        caption = (TextView) findViewById(R.id.captionTextView);


            mUserSpinner = (Spinner) findViewById(R.id.userChooser);
            mUserSpinner.setOnItemSelectedListener(mUserSpinnerClickListener);
            mDBArrayAdapter = new ArrayAdapter<EmpRecord>(this, android.R.layout.simple_spinner_item);
            mDBArrayFilterAdapter = new ArrayAdapter<OperRecord>(this, android.R.layout.simple_spinner_item); //-----------------------

        getOper();

            mDBArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
           // mUserSpinner.setVisibility(View.VISIBLE);
            mUserSpinner.setAdapter(mDBArrayAdapter);
            //getUsers();
            savedUserPos = prefs.getInt(KEY_USER, 0);
            if (cntr == 1)
                mUserSpinner.setSelection(mDBArrayAdapter.getCount() - 1);
            else if (cntr > 1) {
                mUserSpinner.setSelection(savedUserPos);
            }

        mEntSpinner = (Spinner) findViewById(R.id.entChooser);
        mEntSpinner.setOnItemSelectedListener(mEntSpinnerClickListener);
        mDBEntArrayAdapter = new ArrayAdapter<EntRecord>(this, android.R.layout.simple_spinner_item);
        mDBEntArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       // mEntSpinner.setVisibility(View.VISIBLE);
        mEntSpinner.setAdapter(mDBEntArrayAdapter);

		mOwnerSpinner = (Spinner) findViewById(R.id.ownerChooser);
		mOwnerSpinner.setOnItemSelectedListener(mZToolSpinnerClickListener);
		mDBZToolArrayAdapter = new ArrayAdapter<ZToolRecord>(this, android.R.layout.simple_spinner_item);
		mDBZToolArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mOwnerSpinner.setAdapter(mDBZToolArrayAdapter);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});
		okCancelView = findViewById(R.id.button_Container2);
		signInView = findViewById(R.id.button_Container);
		
		mPasswordNewView = (EditText) findViewById(R.id.new_password);
		mPasswordNew2View = (EditText) findViewById(R.id.new_password2);
        tvId = (TextView) findViewById(R.id.tvId);

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		signInButton = findViewById(R.id.sign_in_button);
		signInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
		changePswButton = findViewById(R.id.change_psw_button);
		changePswButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				changePassword(true);
			}
		});
		okButton = findViewById(R.id.OK_button);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptChangePassword();
			}
		});
		cancelButton = findViewById(R.id.Cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				changePassword(false);
			}
		});

		okOwnerButton = findViewById(R.id.owner_OK_button);
		okOwnerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				zToolSelected = (ZToolRecord) mOwnerSpinner.getSelectedItem();
				CommonClass.setOwner(mContext, (int) zToolSelected.getIdEnt());
				CommonClass.setOwnerName(mContext, zToolSelected.getNm());
			}
		});
		cancelOwnerButton = findViewById(R.id.owner_Cancel_button);
		cancelOwnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
		
		mEmailView.setText(DEBUG_USER_NAME);

		ownerLayout = findViewById(R.id.owner_layout);
		loginLayout = findViewById(R.id.login_layout);
        lbm = LocalBroadcastManager.getInstance(this);
        serviceIntent = new Intent(this, ConnectionManagerService.class);

        debugMode = CommonClass.getDebugMode(mContext);
        cntr = dbSch.getHotelRoomCount();
        if (!debugMode /*&& cntr == 0*/) {
            dbSch.emptyTable(EmpTable.TABLE_NAME);
            dbSch.emptyTable(EntTable.TABLE_NAME);
            getUsers();
            getHotels();
            getOper();
            signInButton.setEnabled(false);
//            sendSOAPRequest(STATE_GET_FIO);
        } else {
            getUsers();
            getHotels();
            getOper();
        }
        Log.d(TAG, KEY_USER);
    }

    @Override
    protected void onStart() {
        super.onStart();
        lbm.registerReceiver(onTask, new IntentFilter(ConnectionManagerService.GET_TASK_ACTION));
    }
    @Override
    public void onResume() {
        super.onResume();
        lbm.registerReceiver(onTask, new IntentFilter(ConnectionManagerService.GET_TASK_ACTION));

        IntentFilter iff = new IntentFilter("my.custom.INTENT");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
        if (!(connectionService != null && !connectionService.isWorking())) {
            //Starting the service makes it stick, regardless of bindings
            startService(serviceIntent);
        }
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

    }
    @Override
    public void onPause() {
        super.onPause();
        lbm.unregisterReceiver(onTask);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);

        if (connectionService != null && connectionService.isWorking()) {
            connectionService.sendToasts(false);
            unbindService(serviceConnection);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            connectionService = ((ConnectionManagerService.ConnectionBinder)service).getService();
            connectionService.setServiceResponseCallback((ConnectionManagerService.ServiceResponseCallback) mContext);
            connectionService.sendToasts(true);
            connectionService.periodicTask(false);

            // SOAP request in android Service
            sendSOAPRequest(STATE_GET_FIO);
            sendSOAPRequest(STATE_GET_OPER);
        }

        public void onServiceDisconnected(ComponentName className) {
            connectionService = null;
        }
    };

    private OnItemSelectedListener mUserSpinnerClickListener = new OnItemSelectedListener() {
        @Override
    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            userSelected = (EmpRecord) mUserSpinner.getSelectedItem();
        }

        @Override
    	public void onNothingSelected(AdapterView<?> parent) {}
    };

    private OnItemSelectedListener mEntSpinnerClickListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            hotelSelected = (EntRecord) mEntSpinner.getSelectedItem();
            Log.d(TAG, String.valueOf(hotelSelected));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private OnItemSelectedListener mZToolSpinnerClickListener = new OnItemSelectedListener() {
        @Override
    	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        	zToolSelected = (ZToolRecord) mOwnerSpinner.getSelectedItem();
        }

        @Override
    	public void onNothingSelected(AdapterView<?> parent) {}
    };
    
	
	private void getTelephonyInfo() {
		//---get the SIM card ID---
		mSIMId = tm.getSimSerialNumber();
		//---get the phone number---
		mPhoneNumber = tm.getLine1Number();
		//---get the IMEI number---
		mIMEI = tm.getDeviceId();
	}
	
	private void changePassword(boolean show) {
		mPasswordNewView.setVisibility(show ? View.VISIBLE : View.GONE);
		mPasswordNew2View.setVisibility(show ? View.VISIBLE : View.GONE);
		okCancelView.setVisibility(show ? View.VISIBLE : View.GONE);
		signInView.setVisibility(show ? View.GONE : View.VISIBLE);
	}

    private void getUsers() {
        String query = "SELECT * from " + EmpTable.TABLE_NAME + " ORDER BY " +
                EmpTable.SURNAME + " ASC";
        Cursor c = null;
        EmpRecord user;
      //  String catName = "";
        SQLiteDatabase sqdb = dbSch.getWritableDatabase();
        try {
            c = sqdb.rawQuery(query, null);
            mDBArrayAdapter.clear();
            while (c.moveToNext()) {
                user = new EmpRecord(c);
                mDBArrayAdapter.add(user);
                myList.add(user.getIdExtStr());
                Log.d(TAG, String.valueOf(user)+ " / "+ user.getIdExtStr() + "/ " + user.getId());

                if(user.getIdExtStr().equals(mUser)){
                    idUserExist = true;
                    strUserId = String.valueOf(user.getIdExtStr());
                    strUser = String.valueOf(user);
                    strUserId = String.valueOf(user.getIdExtStr());
                }
            }
        } catch(Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            mDBArrayAdapter.notifyDataSetChanged();
        }
    }

    private void getHotels() {
        String query = "SELECT * from " + EntTable.TABLE_NAME
                + " ORDER BY " + EntTable.NM + " ASC";
        Cursor c = null;
        EntRecord entRecord;
        SQLiteDatabase sqdb = dbSch.getWritableDatabase();
        try {
            c = sqdb.rawQuery(query, null);
            mDBEntArrayAdapter.clear();
            while (c.moveToNext()) {
                entRecord = new EntRecord(c);
                mDBEntArrayAdapter.add(entRecord);
                Log.d(TAG, "Hotel: " + String.valueOf(entRecord));
                if(String.valueOf(entRecord).equals(nameHotel)){
                   idHotelExist = true;
//                    strUserId = String.valueOf(user.getIdExtStr());
//                    strUser = String.valueOf(user);
//                    strUserId = String.valueOf(user.getIdExtStr());
                }

            }
        } catch(Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            mDBEntArrayAdapter.notifyDataSetChanged();
        }
    }


            //  Call Table oper
    private void getOper() {
        SQLiteDatabase sqdb = dbSch.getWritableDatabase();
        Cursor c = null;
        OperRecord entRecord;
        String query = "SELECT * from " + OperTable.TABLE_NAME;
        Log.d(TAG, query);

        try {
            c = sqdb.rawQuery(query, null);
            mDBArrayFilterAdapter.clear();
            while (c.moveToNext()) {
                entRecord = new OperRecord(c);
                mDBArrayFilterAdapter.add(entRecord);
                Log.d(TAG, "Oper: "
                        + " nom: " + entRecord.getRoomNum()
                        + " gorn: " + entRecord.getSurname()
                        + "  check: " + entRecord.isCheckRoom()+
                    "  inRoom: " + entRecord.isChambermaidInRoom() + "  quit: "
                        + entRecord.isQuitChambermaid() + " name: " + entRecord.getNameChambermaid());

            }
        } catch(Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            mDBArrayFilterAdapter.notifyDataSetChanged();
        }
    }


    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_get_rooms:
                sendSOAPRequest(STATE_GET_FIO);

                return true;
            case R.id.action_show_rooms_dump:
                showDump();
                return true;
        }
        return false;
    }

	public void attemptChangePassword() {
		boolean cancel = false;
		View focusView = null;
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);
		mPasswordNewView.setError(null);
		mPasswordNew2View.setError(null);
		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mNewPassword = mPasswordNewView.getText().toString();
		String checkNewPsw = mPasswordNew2View.getText().toString();
		if (mNewPassword.endsWith(checkNewPsw)) {
			if (!mNewPassword.equals(mPassword)) {
				
			} else {  // new psw equals old psw
				mPasswordNewView.setError(getString(R.string.error_psw_new_old));
				focusView = mPasswordNewView;
				cancel = true;							
			}
		} else {  // new psws don't match 
			mPasswordNew2View.setError(getString(R.string.error_psw_not_match));
			focusView = mPasswordNew2View;
			cancel = true;			
		}		
		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.ch_psw_progress);
		}		
	}
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);
		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		
		if (mEmail.equals(mPassword)) {
			changePassword(true);
		} else {
			boolean cancel = false;
			View focusView = null;
			// Check for a valid password.
			if (TextUtils.isEmpty(mPassword)) {
				mPasswordView.setError(getString(R.string.error_field_required));
				focusView = mPasswordView;
				cancel = true;
			} else if (mPassword.length() < 4) {
				mPasswordView.setError(getString(R.string.error_invalid_password));
				focusView = mPasswordView;
				cancel = true;
			}
			// Check for a valid email address.
			if (TextUtils.isEmpty(mEmail)) {
				mEmailView.setError(getString(R.string.error_field_required));
				focusView = mEmailView;
				cancel = true;
			}
			if (cancel) {
				// There was an error; don't attempt login and focus the first
				// form field with an error.
				focusView.requestFocus();
			} else {
				// Show a progress spinner, and kick off a background task to
				// perform the user login attempt.
				mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
                if (debugMode)
                    showHotel();  //debug
                else
                    sendSOAPRequest(STATE_POST_AUTH);
                    //sendSOAPRequest(STATE_POSITION);

			}
		}
	}
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
				.alpha(show ? 1 : 0)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mLoginStatusView.setVisibility(show ? View.VISIBLE
								: View.GONE);
					}
				});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
				.alpha(show ? 0 : 1)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mLoginFormView.setVisibility(show ? View.GONE
								: View.VISIBLE);
					}
				});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

    private void sendSOAPRequest(int state) {
        mSOAPState = state;

        Log.d(TAG,"sendSOAPRequest: " + mSOAPState +" / "+ state );
        if (connectionService != null) {
            if (mSOAPState == STATE_POST_AUTH) {
                if(idUserExist == true){
                    connectionService.setAuthCredentials(strUserId, mPassword);
                }else{
                    connectionService.setAuthCredentials(userSelected.getIdExtStr(), mPassword);
                }
            }
            if (mSOAPState == STATE_POSITION) {
                 connectionService.setAuthCredentials2(mPosition);
            }
                connectionService.sendSOAPRequest(state);
        }
    }

    private void showDump() {
        Intent intent = new Intent(this, ShowDumpActivity.class);
        startActivity(intent);
    }

    private void showHotel() {
      // if (idUserExist == true) {
           if (userSelected != null && hotelSelected != null) {
               String msg;
               if(idUserExist == true){
                   msg = String.format("%s; %s", userSelected.getFullName(), hotelSelected.getNm());
               }else{
                   msg = String.format("%s; %s", userSelected.getFullName(), hotelSelected.getNm());
               }
                dbSch.addLogItem(LogsRecord.INFO, new Date(), msg);
                if (connectionService != null && idUserExist == true) {
                    connectionService.setEmpId(strUserId);
                } else if(connectionService != null){
                    connectionService.setEmpId(userSelected.getIdExtStr());
                }


                Intent intent = new Intent(this, HotelRoomsSwipeActivity.class);
                Bundle b = new Bundle();
               if (idUserExist == true) {
                   b.putString("idEmp", strUserId);
               } else {
                   b.putString("idEmp", userSelected.getIdExtStr());
               }
               if(idHotelExist == true){
                   b.putString("idHotel", idHotel);
                   b.putString("Hotel", nameHotel);
               }else {
                b.putString("idHotel", hotelSelected.getIdExtStr());
                b.putString("Hotel", hotelSelected.getNm());
               }

//                Log.d(TAG, "showHotel(): " + hotelSelected.getIdExtStr() + " / " + strUserId + " user: " +idUserExist);
//                Log.d(TAG, "*** default: " + nameHotel + " / " + idHotel + "hotel:" + idHotelExist);
                intent.putExtras(b);
                startActivity(intent);
            }
    }

    private boolean checkDate() {
        boolean res = false;
        try {
            Date maxDate = DBSchemaHelper.dateFormatDay.parse(MAX_DATE_ENABLED);
            Date today = Calendar.getInstance().getTime();
            res = today.before(maxDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
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

    @Override
    public void onSrvRequestSuccess(String response) {
        caption.setText(response);
        if (connectionService != null) {
            mSOAPState = connectionService.getSOAPState();
            switch (mSOAPState) {

                case STATE_GET_FIO:
                    getUsers();
                    Log.d(TAG, idName);
                    sendSOAPRequest(STATE_GET_ROOMS);
                    Log.e(TAG, "STATE_GET_FIO: " + STATE_GET_FIO);
                    break;
                case STATE_GET_ROOMS:                                                               // ----- ##### -----
                   getHotels();
                    long cntr = dbSch.getHotelRoomCount();
                    signInButton.setEnabled(cntr > 0 /*&& checkDate()*/);
                    sendSOAPRequest(STATE_GET_OPER);
                    Log.e(TAG, "STATE_GET_ROOMS: " + STATE_GET_ROOMS);
                    break;

                case STATE_GET_OPER:                                                               // ----- ##### -----
                    getOper();
                    //long cnt = dbSch.getOperCount();
                   // signInButton.setEnabled(cnt > 0 /*&& checkDate()*/);
                    sendSOAPRequest(STATE_POSITION);
                    Log.e(TAG, "onSrvRequestSuccess: " + STATE_GET_OPER);
                    break;

                case STATE_POSITION:
                    if (mPosition.equals("") || mPosition.equals("0") || idUserExist == true){
                    } else {
                        getUsers();
                    }
                    if(idUserExist == true){
                        tvId.setText(strUser);
                        //mUserSpinner.setVisibility(View.GONE);
                        tvId.setVisibility(View.VISIBLE);
                        mUserSpinner.setVisibility(View.GONE);
                    }
                    else {
                    mUserSpinner.setVisibility(View.VISIBLE);
                    }

                    if(idHotelExist == true){
                        tvEnt.setText(nameHotel);
                        tvEnt.setVisibility(View.VISIBLE);
                        mEntSpinner.setVisibility(View.GONE);
                    }else {
                    mEntSpinner.setVisibility(View.VISIBLE);
                    }
                    break;

                case STATE_POST_AUTH:
                    if (connectionService.isAuthOK())
                        showHotel();

                    else {
                        mPasswordView.setError(getString(R.string.error_incorrect_password));
                        Toast.makeText(mContext, getString(R.string.error_incorrect_password), Toast.LENGTH_SHORT).show();
                    }
                    Log.d(TAG,"onSrvRequestSuccess: " +  String.valueOf(connectionService.isAuthOK()));
                    break;
            }
        }
    }

    @Override
    public void onSrvRequestError(Exception error) {
        caption.setText(error.getMessage());
    }

    @Override
    public void onSrvTaskChanged(int Task) {

    }


}
