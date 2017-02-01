package com.utis.chambermaid;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.chambermaid.notnow.InputNameDialogFragment;
import com.utis.chambermaid.notnow.LogsActivity;
import com.utis.chambermaid.records.LogsRecord;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault12;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RoomActivity extends FragmentActivity implements View.OnClickListener,
        InputNameDialogFragment.InputNameDialogListener {
    private static final boolean D = true;
    private static final String TAG = "RoomActivity";
    private static final int STATE_POST_QUIT = 1;
    private static final int STATE_POST_SERVICE = 2;
    private static final int STATE_POST_MESSAGE = 3;

   // private static final int STATE_POST_CHANGE_BED = 4;//---------------------

    private static final int STATE_FINISH = 0;
    private static final String METHOD_POST_QUIT = "SendQuit";
    private static final String METHOD_POST_MESSAGE = "SendMessage";
    private static final String METHOD_POST_SERVICE = "SendUborka";

 //   private static final String METHOD_POST_CHANGE_BED = "SendSmenPostel";//-----------


    private DBSchemaHelper dbSch;
    private Context mContext;
    private RelativeLayout roomLayout;
    private TextView roomNumTextView;
    private View evenRowView, oddRowView, currentRowView;
    private ImageView personImageView, broomImageView, windowImageView, doorImageView, balconyImageView;
    private Button  btnService, btnQuit, btnMessage, btnBed;
    private int mIdHotel, mIdEmp, mBalcony, mRoom, mSOAPState = STATE_FINISH, changeBedExtra;
    private boolean mServiceNeeded, mPersonInRoom, mQuit, mOccupied, mWindow, mDoor, mClearenceNeeded, statNom, repair;
    private String mIdHotelStr, mIdEmpStr, mHotel, mMessage = "";
    private String SOAPAction, SOAPMethod, title, noteRepair;

    LoginActivity loginActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        mContext = this;
        dbSch = DBSchemaHelper.getInstance(this);
        roomLayout = (RelativeLayout) findViewById(R.id.rlImages);
        roomNumTextView = (TextView) findViewById(R.id.txtRoomNum);
        evenRowView = (View) findViewById(R.id.even_row);
        oddRowView = (View) findViewById(R.id.odd_row);
        personImageView = (ImageView) findViewById(R.id.personRoom);
        broomImageView = (ImageView) findViewById(R.id.broomRoom);
        //btnBar = (Button) findViewById(R.id.btnBar);
        //btnBar.setOnClickListener(this);
        btnService = (Button) findViewById(R.id.btnService);
        btnService.setOnClickListener(this);
        btnQuit = (Button) findViewById(R.id.btnQuit);
        btnQuit.setOnClickListener(this);
        btnMessage = (Button) findViewById(R.id.btnMessage);
        btnMessage.setOnClickListener(this);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.containsKey("idEmp")) {
                mIdEmpStr = b.getString("idEmp");
                mIdEmp = Integer.parseInt(mIdEmpStr);
            }
            if (b.containsKey("idHotel")) {
                mIdHotelStr = b.getString("idHotel");
                mIdHotel = Integer.parseInt(mIdHotelStr);
            }
            if (b.containsKey("Hotel"))
                mHotel = b.getString("Hotel");
            if (b.containsKey("Room"))
                mRoom = Integer.parseInt(b.getString("Room"));
            if (b.containsKey("broom"))
                mServiceNeeded = Boolean.parseBoolean(b.getString("broom"));
            if (b.containsKey("person"))
                mPersonInRoom = Boolean.parseBoolean(b.getString("person"));
            if (b.containsKey("occupied"))
                mOccupied = Boolean.parseBoolean(b.getString("occupied"));
            if (b.containsKey("quit"))
                mQuit = Boolean.parseBoolean(b.getString("quit"));
            if (b.containsKey("window"))
                mWindow = Boolean.parseBoolean(b.getString("window"));
            if (b.containsKey("door"))
                mDoor = Boolean.parseBoolean(b.getString("door"));
            if (b.containsKey("balcony")) {
                mBalcony = Integer.parseInt(b.getString("balcony"));
            }
            if (b.containsKey("status_nom"))
                statNom = Boolean.parseBoolean(b.getString("status_nom"));
            if (b.containsKey("repair"))
                repair = Boolean.parseBoolean(b.getString("repair"));
            if (b.containsKey("note_repair"))
                noteRepair = b.getString("note_repair");
            if (b.containsKey("change_bed_extra")) {
                changeBedExtra = Integer.parseInt(b.getString("change_bed_extra"));
            }
            Log.d(TAG, mIdEmp +" / " + String.valueOf(mIdHotelStr) +" / " +mHotel +" / " + String.valueOf(mRoom));
            Log.d(TAG, String.valueOf(mServiceNeeded) +" / " +String.valueOf(mPersonInRoom) +" / " + String.valueOf(statNom) +" / " +String.valueOf(repair));
            Log.d(TAG,  " STATUS_NOM =>" + String.valueOf(statNom));
            Log.d(TAG, " REMONT =>" + String.valueOf(repair));
            Log.d(TAG, " NOTE =>" + String.valueOf(noteRepair) + " / " + String.valueOf(changeBedExtra));
            Log.d(TAG, " changeBedExtra =>" + String.valueOf(changeBedExtra));
            Log.d(TAG, " changeBedExtra =>" + String.valueOf(changeBedExtra));
            Log.d(TAG, " changeBedExtra =>" + String.valueOf(changeBedExtra));
            Log.d(TAG, " mQuit =>" + String.valueOf(mQuit));
        }
        if (mRoom % 2 == 0) {
            evenRowView.setVisibility(View.VISIBLE);
            oddRowView.setVisibility(View.INVISIBLE);
            currentRowView = evenRowView;
        } else {
            evenRowView.setVisibility(View.INVISIBLE);
            oddRowView.setVisibility(View.VISIBLE);
            currentRowView = oddRowView;
        }
        roomNumTextView = (TextView) currentRowView.findViewById(R.id.roomNum);
        personImageView = (ImageView) currentRowView.findViewById(R.id.person);
        broomImageView = (ImageView) currentRowView.findViewById(R.id.broom);
        windowImageView = (ImageView) currentRowView.findViewById(R.id.window);
        doorImageView = (ImageView) currentRowView.findViewById(R.id.door);
        balconyImageView = (ImageView) currentRowView.findViewById(R.id.balcony);
        if (mQuit) {
            roomNumTextView.setTextColor(getResources().getColor(R.color.quit_t));
            currentRowView.setBackgroundResource(R.drawable.list_selector_quit);
        } else if (mOccupied) {
            roomNumTextView.setTextColor(getResources().getColor(R.color.occupied_t));
            currentRowView.setBackgroundResource(R.drawable.list_selector_occupied);
        }
        if (mBalcony == 2) {
            if (mRoom % 2 == 0)
                balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
            else
                balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
            balconyImageView.setVisibility(View.VISIBLE);
        } else if (mBalcony == 1) {
            if (mRoom % 2 == 0)
                balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
            else
                balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
            balconyImageView.setVisibility(View.VISIBLE);
        } else
                /*balconyImageView.setVisibility(View.INVISIBLE)*/;

        if (mBalcony == 0) {
            windowImageView.setVisibility(View.INVISIBLE);
            if (mWindow)
                if (mRoom % 2 == 0)
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
                else
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
            else
                if (mRoom % 2 == 0)
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
                else
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
        } else {
            windowImageView.setVisibility(View.VISIBLE);
            if (mWindow)
                if (mRoom % 2 == 0)
                    windowImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                else
                    windowImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
            else
                if (mRoom % 2 == 0)
                    windowImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
                else
                    windowImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
        }

        if (mDoor)
            if (mRoom % 2 == 0)
                doorImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
            else
                doorImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
        else
            if (mRoom % 2 == 0)
                doorImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
            else
                doorImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));



        title = mHotel + "; №" + mRoom;
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(title);
        btnService.setEnabled(mServiceNeeded);
        btnQuit.setEnabled(mQuit);

        if (!mServiceNeeded)
            btnService.setText(getString(R.string.btn_service));


        broomImageView.setVisibility(mServiceNeeded ? View.VISIBLE : View.INVISIBLE);
        personImageView.setVisibility(mPersonInRoom ? View.VISIBLE : View.INVISIBLE);
        roomNumTextView.setText(""+ mRoom);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_show_log:
                intent = new Intent(this, LogsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnService:
                sendSOAPRequest(STATE_POST_SERVICE);
                break;
            case R.id.btnQuit:
                sendSOAPRequest(STATE_POST_QUIT);
                break;
            case R.id.btnMessage:
                showInputNameDialog();
                break;
        }
    }

    private void updateButtonState() {
        btnService.setEnabled(mServiceNeeded && mSOAPState == STATE_FINISH);
        btnQuit.setEnabled(mQuit && mSOAPState == STATE_FINISH);
        btnMessage.setEnabled(mSOAPState == STATE_FINISH);
    }

    private void sendSOAPRequest(int state) {
        try {
            mSOAPState = state;
            switch (mSOAPState) {
                case STATE_POST_QUIT:
                    SOAPMethod = METHOD_POST_QUIT;
                    SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                    Log.d(TAG," QUIT "+ SOAPAction);
                    break;
                case STATE_POST_SERVICE:
                    SOAPMethod = METHOD_POST_SERVICE;
                    SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                    Log.d(TAG," SERVICE "+ SOAPAction);
                    break;
                case STATE_POST_MESSAGE:
                    SOAPMethod = METHOD_POST_MESSAGE;
                    SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                    Log.d(TAG," MESSAGE "+ SOAPAction);
                    break;

                default:
                    SOAPMethod = "";
                    SOAPAction = "";
            }
            updateButtonState();
            if (SOAPMethod.length() > 0 && SOAPAction.length() > 0) {
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute();
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, e.getMessage());
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private String resp = "";
        private boolean isPostOK = false;

        @Override
        protected String doInBackground(String... params) {
            publishProgress(mContext.getString(R.string.title_init_post)); // Calls onProgressUpdate()
            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
                SoapObject request = new SoapObject(CommonClass.NAMESPACE, SOAPMethod);

                switch (mSOAPState) {
                    case STATE_POST_MESSAGE:
                        request.addProperty("Massege", mMessage);
                    case STATE_POST_SERVICE:
                    case STATE_POST_QUIT:
                        request.addProperty("idHotell", mIdHotelStr);
                        request.addProperty("idGorn", mIdEmpStr);
                        request.addProperty("nom", mRoom);
                        break;
                }
                envelope.bodyOut = request;
                envelope.setAddAdornments(false);
                envelope.implicitTypes = true;

//                envelope.dotNet = true;
                HttpTransportSE transport = new HttpTransportSE(CommonClass.getServerURI(mContext));
//                HttpTransportSE transport = new HttpTransportBasicAuth(URL, LOGIN, PSW);
                transport.debug = true;
                try {
                    String auth = CommonClass.LOGIN + ":" + CommonClass.PSW;
                    List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
                    headerList.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode(auth.getBytes())));

                    SoapObject response = null;
                    transport.call(SOAPAction, envelope, headerList);
                    //bodyIn is the body object received with this envelope
                    if (envelope.bodyIn != null) {
                        if (envelope.bodyIn instanceof SoapObject) {
                            publishProgress(mContext.getString(R.string.title_parse)); // Calls onProgressUpdate()
                            response = (SoapObject) envelope.bodyIn;
                            parseSOAPResponse(response);
                        } else if (envelope.bodyIn instanceof SoapFault12) {
                            resp = ((SoapFault12) envelope.bodyIn).getMessage().toString();
                            String msg = TAG + " " + mSOAPState + "; " + resp;
                            dbSch.addLogItem(LogsRecord.ERROR, new Date(), msg);
                        }
                    }
                } catch (IOException e) {
                    resp = "Exception: " + e.getMessage();
                    Log.e(TAG, resp);
                } catch (XmlPullParserException e) {
                    resp = "Exception Xml: " + e.getMessage();
                    Log.e(TAG, resp);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
                resp = e.getMessage();
            }
            return resp;
        }

        private void parseSOAPResponse(SoapObject response) {
            switch (mSOAPState) {
                case STATE_POST_SERVICE:
                case STATE_POST_MESSAGE:
                case STATE_POST_QUIT:
                    parsePostQuitResponse(response);
                    break;
            }
        }

        private void parsePostQuitResponse(SoapObject response) {
            String result = response.getProperty("return").toString();
            try {
                isPostOK = Boolean.parseBoolean(result);
            } catch (Exception e) {
                isPostOK = false;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String msg = "";
            switch (mSOAPState) {
                case STATE_POST_SERVICE:
                    if (isPostOK) {
                        mServiceNeeded = false;
                        btnService.setText(getString(R.string.btn_service));
                        broomImageView.setVisibility(mServiceNeeded ? View.VISIBLE : View.INVISIBLE);
                        dbSch.updateHotelRoomService(false, mIdHotelStr, mRoom);
                    }
                case STATE_POST_MESSAGE:
                case STATE_POST_QUIT:
                    if (isPostOK)
                       msg = getString(R.string.m_post_ok);
                    else
                        msg = getString(R.string.m_post_err);
                    break;
            }
            mSOAPState = STATE_FINISH;
            updateButtonState();

            msg = result + msg;
            if (msg.length() >  0)
                Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... text) {
            Toast.makeText(mContext, text[0], Toast.LENGTH_SHORT).show();
        }

    }

    private void showInputNameDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        InputNameDialogFragment inputNameDialog = new InputNameDialogFragment();
        inputNameDialog.setCancelable(false);
        inputNameDialog.setDialogTitle(getString(R.string.title_msg));
        inputNameDialog.setDialogText("");
        inputNameDialog.show(fragmentManager, "input dialog");
    }

    @Override
    public void onFinishInputDialog(String inputText) {
//		Toast.makeText(this, "Returned from dialog: "+ inputText, Toast.LENGTH_SHORT).show();
        mMessage = inputText;
        if (mMessage.length() > 0)
            sendSOAPRequest(STATE_POST_MESSAGE);
    }


}
