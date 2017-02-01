package com.utis.chambermaid;

import android.app.ActionBar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.chambermaid.notnow.InputNameDialogFragment;
import com.utis.chambermaid.records.HotelRoomRecord;
import com.utis.chambermaid.records.LogsRecord;
import com.utis.chambermaid.records.OperRecord;
import com.utis.chambermaid.records.RoomBarGoodsRecord;
import com.utis.chambermaid.tables.HotelRoomTable;
import com.utis.chambermaid.tables.OperTable;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault12;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RoomSwipeActivity extends FragmentActivity {
    private static final boolean D = true;
    private static final String TAG = "RoomSwipeActivity";
    private static final int BLINK_INTERVAL = 2;
    CollectionPagerAdapter mCollectionPagerAdapter;
    ViewPager mViewPager;
    private DBSchemaHelper dbSch;
    private Context mContext;
    private int mRoomCnt, mSelectedRoom, tipeClearence;
    public int mIdHotel, mIdEmp, mBalcony, mRoom, mRoomPos, mFloor;
   // public static String idEmp;
    private boolean mServiceNeeded, mPersonInRoom, mQuit, mOccupied, mWindow, mDoor, statNom, repair,
            noDisturb, callChambermaid, chambermaidInRoom;
    private String mIdHotelStr, mIdEmpStr, mHotel, mMessage = "", mTitle = "", noteRepair;
    private String operNum, operName, operNameGorn;
    private int operNumInt, operNameInt;
    private boolean operNumCheck, operCall, operInRoom, operQuit;
    int[] roomsArray;
    boolean debugMode, manualRefresh;
    private SparseArray<Fragment> mapFragment;
    static boolean  bool = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final ActionBar actionBar;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_swipe);
        mContext = this;
        dbSch = DBSchemaHelper.getInstance(this);
        mapFragment = new SparseArray<Fragment>();
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
            if (b.containsKey("floor"))
                mFloor = Integer.parseInt(b.getString("floor"));
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
            if (b.containsKey("balcony"))
                mBalcony = Integer.parseInt(b.getString("balcony"));
                if (b.containsKey("status_nom"))
                    statNom = Boolean.parseBoolean(b.getString("status_nom"));
                if (b.containsKey("repair"))
                    repair = Boolean.parseBoolean(b.getString("repair"));
                if (b.containsKey("note_repair"))
                    noteRepair = b.getString("note_repair");
            if (b.containsKey("tipe_clearence"))
                tipeClearence = Integer.parseInt(b.getString("tipe_clearence"));
            if (b.containsKey("noy_disturb"))
                noDisturb = Boolean.parseBoolean(b.getString("noy_disturb"));
            if (b.containsKey("call_chambermaid"))
                callChambermaid = Boolean.parseBoolean(b.getString("call_chambermaid"));
            if (b.containsKey("chambermaid_in_room"))
                chambermaidInRoom = Boolean.parseBoolean(b.getString("chambermaid_in_room"));

            if (b.containsKey("oper_num"))
                operNum = b.getString("oper_num");

            if (b.containsKey("oper_name"))
                operName = b.getString("oper_name");

            if (b.containsKey("oper_num_check"))
                operNumCheck = Boolean.parseBoolean(b.getString("oper_num_check"));
            if (b.containsKey("oper_call"))
                operCall = Boolean.parseBoolean(b.getString("oper_call"));
            if (b.containsKey("oper_in_room"))
                operInRoom = Boolean.parseBoolean(b.getString("oper_in_room"));
            if (b.containsKey("oper_quit"))
                operQuit = Boolean.parseBoolean(b.getString("oper_quit"));

            if (b.containsKey("oper__name_gorn"))
                operNameGorn = b.getString("oper__name_gorn");

           // idEmp = "000000" + Integer.toString(mIdEmp);
        }

        debugMode = CommonClass.getDebugMode(mContext);
        mRoomCnt = (int) dbSch.getHotelRoomCount(mIdHotelStr, mFloor);
        roomsArray = new int[mRoomCnt];
        initTabs();
        actionBar = this.getActionBar();

        mCollectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        mTitle = mHotel + "; №" + roomsArray[position];
                        actionBar.setTitle(mTitle);
                    }
                });

        mViewPager.setAdapter(mCollectionPagerAdapter);
        if (mViewPager != null)
            mViewPager.setCurrentItem(mRoomPos);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room_swipe, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initTabs() {
        String query = "SELECT * FROM " + HotelRoomTable.TABLE_NAME +
                " WHERE " + HotelRoomTable.ID_HOTEL_STR + " = '" + mIdHotelStr + "' AND " +
                HotelRoomTable.ROOM_FLOOR + " = " + mFloor;

        Cursor c = null;
        int colid, id, cntr = 0;
        String name;
//        MyTabListener tl = new MyTabListener();
        SQLiteDatabase sqdb = dbSch.getWritableDatabase();
        try {
            c = sqdb.rawQuery(query, null);
            while (c.moveToNext()) {
                colid = c.getColumnIndex(HotelRoomTable.ROOM_NUM);
                id = c.getInt(colid);
                roomsArray[cntr] = id;
                if (id == mRoom)
                    mRoomPos = cntr;
                cntr++;
            }
        } catch(Exception e) {
            if (D) Log.e(TAG, "initTabs: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
    }


    public class CollectionPagerAdapter extends FragmentStatePagerAdapter {

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = RoomObjectFragment.newInstance(i, mHotel, roomsArray[i],
                    mFloor, mIdHotelStr, mIdEmpStr);
            mapFragment.put(i, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mapFragment.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public int getCount() {
            return mRoomCnt;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "№" + roomsArray[position];
        }

    }

    // Instances of this class are fragments representing a single
    // object in our collection.
    public static class RoomObjectFragment extends Fragment implements View.OnClickListener,
            InputNameDialogFragment.InputNameDialogListener {
        public static final int DIALOG_FRAGMENT = 1;
        public static final String ARG_NUM = "num";
        public static final String ARG_ROOM_NUM = "room_num";
        public static final String ARG_NAME = "nm";
        public static final String ARG_FLOOR = "floor";
        public static final String ARG_HOTEL = "hotel";
        public static final String ARG_EMP = "emp";
        private static final int STATE_POST_QUIT = 1;
        private static final int STATE_POST_SERVICE = 2;
        private static final int STATE_POST_MESSAGE = 3;
        private static final int STATE_POST_CHANGE_BED = 4;
        private static final int STATE_POST_CHECK_ROOM_ACCEPTED = 5;
        private static final int STATE_POST_CHECK_ROOM_CALL_ACCEPTED = 6;
        private static final int STATE_FINISH = 0;
        private static final String METHOD_POST_QUIT = "SendQuit";
        private static final String METHOD_POST_MESSAGE = "SendMessage";
        private static final String METHOD_POST_SERVICE = "SendUborka";
        private static final String METHOD_POST_CHANGE_BED = "SendSmenPostel";
        private static final String METHOD_POST_CHECK_ROOM_ACCEPTED = "SendPrinyal";
        private static final String METHOD_POST_CHECK_ROOM_CALL_ACCEPTED = "SendGornVizit";

        private int mIdHotel, mNum, mRoom, mFloor, mSOAPState = STATE_FINISH;
        private String mIdEmpStr, mIdHotelStr, fName, mMessage = "";
        private String a, b, d, c, timeRes, timeDep;
        int timeR, timeD, countDay;
        private DBSchemaHelper dbSchR;
        private HotelRoomRecord roomRecord;
        private OperRecord operRecord;
        private FragmentActivity mContext;
        private Context context;
        private TextView roomNumTextView, changeBedDateTextView, departureDateTextView, txtExit,
            reservationDateTextView, acceptCheckNumberTextView, tvMiniBar, tvRepairNote,  tvName;
        private View evenRowView, oddRowView, currentRowView;
        private ImageView personImageView, broomImageView, windowImageView, doorImageView, balconyImageView,broomImageView2;
        public ImageView imgBeds, imgDrops, imgBedWash;
        private Button btnBarSale, btnMovement,btnService, btnChangeBed, btnMessage, btnAcceptCheckRoom, btnGetCall, btnChambermaidQuit;
        private String SOAPAction, SOAPMethod, title;
        LinearLayout btnBar;
        final Notification n_rooms = new Notification(R.drawable.utis_logo,
                "Ком", System.currentTimeMillis());
        private String empId = "";
        private ArrayList<HotelRoomRecord> notifyRooms;
        private boolean notifyNewMsg;
        private NotificationManager mNManager;
        private static final int CHECK_ROOM_NOTIFY_ID = 1100+10;
        private DBSchemaHelper dbSch;
        private String mUser, strUser , strUserId ;


        /**
         * Create a new instance of CountingFragment, providing "num" as an argument.
         */
        static RoomObjectFragment newInstance(int num, String nm, int room, int floor,
                                               String idHotel, String idEmp) {
            RoomObjectFragment f = new RoomObjectFragment();
            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt(ARG_NUM, num);
            args.putInt(ARG_ROOM_NUM, room);
            args.putString(ARG_NAME, nm);
            args.putInt(ARG_FLOOR, floor);
            args.putString(ARG_HOTEL, idHotel);
            args.putString(ARG_EMP, idEmp);
            f.setArguments(args);
            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mNum = getArguments() != null ? getArguments().getInt(ARG_NUM) : 0;
            fName = getArguments() != null ? getArguments().getString(ARG_NAME) : "";
            mRoom = getArguments() != null ? getArguments().getInt(ARG_ROOM_NUM) : 0;
            mFloor = getArguments() != null ? getArguments().getInt(ARG_FLOOR) : 0;
            mIdHotelStr = getArguments() != null ? getArguments().getString(ARG_HOTEL) : "";
            mIdHotel = Integer.parseInt(mIdHotelStr);
            mIdEmpStr = getArguments() != null ? getArguments().getString(ARG_EMP) : "";
            dbSchR = DBSchemaHelper.getInstance(getActivity());
            getRoomRecord();
        }

        private void setRoomBedView() {
            imgBeds.setImageDrawable(roomRecord.isTwin() ? getResources().getDrawable(R.drawable.twin_bed_s) :
                    getResources().getDrawable(R.drawable.double_bed_s));
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            boolean evenRoomNumber;
            // The last two arguments ensure LayoutParams are inflated properly.
            View rootView = inflater.inflate(R.layout.activity_room, container, false);
            roomNumTextView = (TextView) rootView.findViewById(R.id.txtRoomNum);
            evenRowView = (View) rootView.findViewById(R.id.even_row);
            oddRowView = (View) rootView.findViewById(R.id.odd_row);
            if (mRoom % 2 == 0) {
                evenRoomNumber = true;
                evenRowView.setVisibility(View.VISIBLE);
                oddRowView.setVisibility(View.INVISIBLE);
                currentRowView = evenRowView;
            } else {
                evenRoomNumber = false;
                evenRowView.setVisibility(View.INVISIBLE);
                oddRowView.setVisibility(View.VISIBLE);
                currentRowView = oddRowView;
            }
            roomNumTextView = (TextView) currentRowView.findViewById(R.id.roomNum);
            roomNumTextView.setText("" + mRoom);
            title = fName + "; №" + mRoom;

            changeBedDateTextView = (TextView) rootView.findViewById(R.id.txtChangeBed);

            //---------------------------------------------------------------------------
            changeBedDateTextView.setText(getString(R.string.m_change_bed) + DBSchemaHelper.dateFormatDay.format(roomRecord.getChangeBedDate())
                    /*roomRecord.getChangeBedDateStr()/* + "; "+ roomRecord.getDaysAfterChangeBed()*/);
            //---------------------------------------------------------------------------

            departureDateTextView = (TextView) rootView.findViewById(R.id.txtDepartureDate);
            departureDateTextView.setText(getString(R.string.m_departure_date) +/*
                    roomRecord.getDepartureDateStr() + "; " +*/
                    DBSchemaHelper.dateFormatMM.format(roomRecord.getDepartureDate()));
            departureDateTextView.setVisibility(roomRecord.isRoomOccupied() || roomRecord.isTodayClearence() ? View.VISIBLE : View.INVISIBLE);
            reservationDateTextView = (TextView) rootView.findViewById(R.id.txtReservationDate);
            if (roomRecord.getReservationsDate() != null) {
                reservationDateTextView.setText(getString(R.string.m_reservation_date) +
                        DBSchemaHelper.dateFormatMM.format(roomRecord.getReservationsDate()));
                reservationDateTextView.setVisibility(View.VISIBLE);
            } else {
                reservationDateTextView.setVisibility(View.GONE);
            }

            acceptCheckNumberTextView = (TextView) rootView.findViewById(R.id.txtAcceptCheckNumber);
            acceptCheckNumberTextView.setVisibility(roomRecord.operNumCheck ? View.VISIBLE : View.INVISIBLE);



            tvName = (TextView)rootView.findViewById(R.id.tvName);

            txtExit = (TextView) rootView.findViewById(R.id.txtExit);
            tvRepairNote = (TextView) rootView.findViewById(R.id.tvRepairNote);
            btnBar = (LinearLayout) rootView.findViewById(R.id.btnBar);
            tvMiniBar = (TextView) rootView.findViewById(R.id.tvMiniBar);
            context = getActivity();
            dbSch = DBSchemaHelper.getInstance(getActivity());

            btnBarSale = (Button) rootView.findViewById(R.id.btnBarSale);
            btnBarSale.setOnClickListener(this);
            btnMovement = (Button) rootView.findViewById(R.id.btnMovement);
            btnMovement.setOnClickListener(this);
            btnService = (Button) rootView.findViewById(R.id.btnService);
            btnService.setOnClickListener(this);

            btnChangeBed = (Button) rootView.findViewById(R.id.btnBed);//-----------------
            btnChangeBed.setOnClickListener(this);//-------------------
            //btnChangeBed.setEnabled(false);

            //btnQuit = (Button) rootView.findViewById(R.id.btnQuit);
            btnChambermaidQuit = (Button) rootView.findViewById(R.id.btnChambermaidQuit);
           // btnQuit.setOnClickListener(this);
            btnChambermaidQuit.setOnClickListener(this);

            btnMessage = (Button) rootView.findViewById(R.id.btnMessage);
            btnMessage.setOnClickListener(this);

            btnAcceptCheckRoom = (Button) rootView.findViewById(R.id.btnAcceptCheckRoom);
            btnAcceptCheckRoom.setOnClickListener(this);
            btnAcceptCheckRoom.setVisibility( roomRecord.operCall  ? View.VISIBLE : View.GONE);

            btnGetCall= (Button) rootView.findViewById(R.id.btnGetCall);
            btnGetCall.setOnClickListener(this);
            btnGetCall.setVisibility(roomRecord.operNumCheck ? View.VISIBLE : View.GONE);

            txtExit.setVisibility(roomRecord.operInRoom ? View.VISIBLE: View.GONE);

            //----- when room is check -----
           // btnQuit.setVisibility(roomRecord.isCheckRoom()|| roomRecord.isCallChambermaid()|| roomRecord.isChambermaidInRoom() ? View.INVISIBLE : View.VISIBLE);
            btnMessage.setVisibility( roomRecord.operNumCheck||  roomRecord.operCall ||roomRecord.operInRoom? View.INVISIBLE : View.VISIBLE);
            btnBarSale.setVisibility(roomRecord.isStatusNom() ||  roomRecord.operNumCheck || roomRecord.operInRoom ? View.VISIBLE : View.GONE);
            btnMovement.setVisibility( roomRecord.operCall? View.INVISIBLE : View.VISIBLE);
            tvMiniBar.setVisibility( roomRecord.operCall ? View.INVISIBLE : View.VISIBLE);
            btnBar.setVisibility( roomRecord.operCall ? View.INVISIBLE : View.VISIBLE);
            btnChambermaidQuit.setVisibility(roomRecord.operInRoom ? View.VISIBLE : View.INVISIBLE);

            //----- when room today is clear (pink color) -----
            btnChangeBed.setVisibility(roomRecord.isTodayClearence()||roomRecord.isRoomOccupied() ? View.VISIBLE : View.GONE);
            if ( roomRecord.operNumCheck || roomRecord.operInRoom){
            btnChangeBed.setVisibility(View.INVISIBLE);
//                btnBarSale.setEnabled(false);
//                btnMovement.setEnabled(false);
            }
            if (
                   // roomRecord.isCheckRoom() ||
                            roomRecord.operNumCheck){
                btnBarSale.setEnabled(false);
                btnMovement.setEnabled(false);
            }
            if(roomRecord.operCall){
                btnChangeBed.setVisibility(View.GONE);
  //              btnQuit.setVisibility(View.INVISIBLE);
            }
            btnService.setVisibility(
                    //roomRecord.isTodayClearence() ||
                            roomRecord.operNumCheck||
                    roomRecord.operCall  || roomRecord.operInRoom? View.GONE : View.VISIBLE);

            personImageView = (ImageView) currentRowView.findViewById(R.id.person);
            broomImageView = (ImageView) currentRowView.findViewById(R.id.broom);
            broomImageView2 = (ImageView) currentRowView.findViewById(R.id.broom2);
            windowImageView = (ImageView) currentRowView.findViewById(R.id.window);
            doorImageView = (ImageView) currentRowView.findViewById(R.id.door);
            balconyImageView = (ImageView) currentRowView.findViewById(R.id.balcony);
            imgBeds = (ImageView) currentRowView.findViewById(R.id.bed);
            imgBedWash = (ImageView) currentRowView.findViewById(R.id.bedWash);
            imgDrops = (ImageView) currentRowView.findViewById(R.id.drop);

            imgDrops.setVisibility(roomRecord.isWaterLeakage() ? View.VISIBLE : View.GONE);
            //imgBedWash.setVisibility(roomRecord.isRoomOccupied()? View.VISIBLE : View.GONE);


                a = String.valueOf(roomRecord.getDepartureDate());
                b = String.valueOf(roomRecord.getReservationsDate());
                countDay = roomRecord.getChangeBedExtra();
            Log.d(TAG, String.valueOf(countDay));
           // countDay = r
                dateRedact();
            //  if (false/*roomRecord.isRoomOccupied()*/) {
           if (roomRecord.isRoomOccupied() && countDay < 4) {
               imgBedWash.setVisibility(View.VISIBLE);//----------------
                switch (roomRecord.getDaysAfterChangeBed()) {
                    case 0:
                        imgBedWash.setImageDrawable(getResources().getDrawable(R.drawable.change_bed2));
                        break;
                    case 1:
                        imgBedWash.setImageDrawable(getResources().getDrawable(R.drawable.change_bed22));
                        break;
                    case 2:
                        imgBedWash.setImageDrawable(getResources().getDrawable(R.drawable.change_bed21));
                        break;
                    default:
                        setRoomBedView();
                        btnChangeBed.setEnabled(true);
                        break;
                }
            } else {
                setRoomBedView();
            }

            // if noDisterb is true
            if (roomRecord.isNotDisturb() ) {
                roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                if ( roomRecord.isTodayClearence()) {
                    roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                    currentRowView.setBackgroundResource(R.drawable.list_selector_quit);
                } else if (roomRecord.isRoomOccupied()) {
                    roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                    currentRowView.setBackgroundResource(R.drawable.list_selector_occupied);
                } else if (roomRecord.getDaysBeforeReservation() == 0) {
                    roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                    currentRowView.setBackgroundResource(R.drawable.list_selector_reserved);
                } else if (b.equals(a) && c.equals(d) && roomRecord.isStatusNom()) {
                    roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                    currentRowView.setBackgroundResource(R.drawable.list_selector_status);
                }
            }

            // if call chambermaid is true
            if (roomRecord.operCall) {
                roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                if (roomRecord.isTodayClearence()) {
                    roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                    currentRowView.setBackgroundResource(R.drawable.list_selector_quit);
                } else if (roomRecord.isRoomOccupied()) {
                    roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                    currentRowView.setBackgroundResource(R.drawable.list_selector_occupied);
                } else if (roomRecord.getDaysBeforeReservation() == 0) {
                    roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                    currentRowView.setBackgroundResource(R.drawable.list_selector_reserved);

                } else if (b.equals(a) && c.equals(d) && roomRecord.isStatusNom()) {
                    roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                    currentRowView.setBackgroundResource(R.drawable.list_selector_status);
                }
            }
            // if check num is true
            if (roomRecord.operNumCheck){
                roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                 if(roomRecord.isTodayClearence()){
                    currentRowView.setBackgroundResource(R.drawable.list_selector_quit);
                     roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                } else if(roomRecord.isRoomOccupied()){
                    currentRowView.setBackgroundResource(R.drawable.list_selector_occupied);
                     roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                }else if(roomRecord.getDaysBeforeReservation() == 0 && roomRecord.isRoomOccupied()) {
                    currentRowView.setBackgroundResource(R.drawable.list_selector_reserved);
                     roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                }  else  if(b.equals(a)&& c.equals(d)&& roomRecord.isStatusNom()){
                     roomNumTextView.setTextColor(getResources().getColor(R.color.needs_checking_t));
                     currentRowView.setBackgroundResource(R.drawable.list_selector_status);
                }

            } else
            // if chambermaid in room is true
            if( roomRecord.operInRoom){
                tvName.setVisibility(View.VISIBLE);
                tvName.setText("Номер принемает : " + roomRecord.operNameGorn);
                roomNumTextView.setTextColor(getResources().getColor(R.color.occupied_t));
                currentRowView.setBackgroundResource(R.drawable.list_selector_chambermaid_in_room);
//                if(idEmp.equals(roomRecord.operName)){
//                }else {
//                    btnChambermaidQuit.setEnabled(false);
//                }

            } else


            if(b.equals(a)&& c.equals(d)&& roomRecord.isStatusNom()){
                roomNumTextView.setTextColor(getResources().getColor(R.color.occupied_t));
                if(timeD<timeR){
                    currentRowView.setBackgroundResource(R.drawable.list_selector_status);
                    departureDateTextView.setVisibility(View.VISIBLE);
                    btnChangeBed.setVisibility(View.VISIBLE);                                       //----------------
                } else {
                    currentRowView.setBackgroundResource(R.drawable.list_selector_status2);
                    btnChangeBed.setVisibility(View.VISIBLE);                                       //----------------
                }

            }
//            else if( roomRecord.operInRoom){
//                tvName.setVisibility(View.VISIBLE);
//
//                tvName.setText("Номер принемает : " + roomRecord.operNameGorn);
//                roomNumTextView.setTextColor(getResources().getColor(R.color.occupied_t));
//                currentRowView.setBackgroundResource(R.drawable.list_selector_chambermaid_in_room);
//                btnChangeBed.setVisibility(View.GONE);
////                if(idEmp.equals(roomRecord.operName)){
////                }else {
////                    btnChambermaidQuit.setEnabled(false);
////                }
//
//            }


            else if (roomRecord.isTodayClearence()) {
                roomNumTextView.setTextColor(getResources().getColor(R.color.quit_t));
                currentRowView.setBackgroundResource(R.drawable.list_selector_quit);
            } else if (roomRecord.isRoomOccupied()) {
                roomNumTextView.setTextColor(getResources().getColor(R.color.occupied_t));
                currentRowView.setBackgroundResource(R.drawable.list_selector_occupied);
            } else if (roomRecord.getDaysBeforeReservation() == 0) {
                roomNumTextView.setTextColor(getResources().getColor(R.color.occupied_t));
                currentRowView.setBackgroundResource(R.drawable.list_selector_reserved);
            }else if (roomRecord.isRepair()){
                roomNumTextView.setTextColor(getResources().getColor(R.color.occupied_t));
                currentRowView.setBackgroundResource(R.drawable.list_selector_repair);
                btnService.setVisibility(View.GONE);
                btnMessage.setVisibility(View.INVISIBLE);
                changeBedDateTextView.setVisibility(View.GONE);
                String str;
                str  = roomRecord.getNoteRepair();
                tvRepairNote.setText(str);
                tvRepairNote.setVisibility(View.VISIBLE);
            }else if (roomRecord.isStatusNom()){
                currentRowView.setBackgroundResource(R.drawable.list_selector_quit);
            }
            if (roomRecord.getBalcony() == 2) {
                if (evenRoomNumber)
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                else
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                balconyImageView.setVisibility(View.VISIBLE);
            } else if (roomRecord.getBalcony() == 1) {
                if (evenRoomNumber)
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
                else
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
                balconyImageView.setVisibility(View.VISIBLE);
            }  else
                /*balconyImageView.setVisibility(View.INVISIBLE)*/;

            if (roomRecord.getBalcony() == 0) {
                windowImageView.setVisibility(View.INVISIBLE);
                if (roomRecord.isWindow())
                    if (evenRoomNumber)
                        balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                    else
                        balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                else
                if (evenRoomNumber)
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
                else
                    balconyImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
            } else {
                windowImageView.setVisibility(View.VISIBLE);
                if (roomRecord.isWindow())
                    if (evenRoomNumber)
                        windowImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
                    else
                        windowImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
                else
                if (evenRoomNumber)
                    windowImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
                else
                    windowImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
            }

            if (roomRecord.isDoor())
                if (evenRoomNumber)
                    doorImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
                else
                    doorImageView.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
            else
                if (evenRoomNumber)
                    doorImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
                else
                    doorImageView.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));

            btnService.setEnabled(roomRecord.isServiceNeeded());
            if (!roomRecord.isServiceNeeded())
                btnService.setText(getString(R.string.btn_service));

            if(roomRecord.operCall){
                btnChangeBed.setVisibility(View.GONE);
                //btnQuit.setVisibility(View.INVISIBLE);
            }
          //  btnChangeBed.setEnabled(roomRecord.);

            broomImageView.setVisibility(roomRecord.isServiceNeeded() && roomRecord.getTipeClearence()==1 ? View.VISIBLE : View.INVISIBLE);
            broomImageView2.setVisibility(roomRecord.isServiceNeeded() && roomRecord.getTipeClearence()==2 ? View.VISIBLE : View.INVISIBLE);
            personImageView.setVisibility(roomRecord.isGuestInRoom() ? View.VISIBLE : View.INVISIBLE);

            return rootView;
        }

        private void getRoomRecord() {
            SQLiteDatabase db = dbSchR.getWritableDatabase();
            Cursor c = null;


            String subQueryNm = "(SELECT "  + OperTable.ROOM_NUM + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_NM;
            String subQueryName = "(SELECT "  + OperTable.SURNAME + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_NAME;
            String subQueryNameGorn = "(SELECT "  + OperTable.NAME + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_NAME_GORN;
            String subQueryCheck = "(SELECT "  + OperTable.CHECK_ROOM + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_NUM_CHECK;
            String subQueryCall= "(SELECT "  + OperTable.CALL_CHAMBERMAID + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_CALL;
            String subQueryInRoom= "(SELECT "  + OperTable.CHAMBERMAID_IN_ROOM + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_IN_ROOM;
            String subQueryQuit= "(SELECT "  + OperTable.CHAMBERMAID_QUIT + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_QUIT;

            String query = "SELECT h.*, "  + subQueryNm +" , "+ subQueryName + " , " + subQueryCheck +
                    " , " + subQueryCall +" , " + subQueryInRoom +" , " + subQueryQuit + " , " + subQueryNameGorn +
                    " FROM " + HotelRoomTable.TABLE_NAME +
                    " h WHERE " + HotelRoomTable.ID_HOTEL_STR + " = '" + mIdHotelStr + "' AND " +
                    HotelRoomTable.ROOM_FLOOR + " = " + mFloor + " AND " +
                    HotelRoomTable.ROOM_NUM + " = " + mRoom;
            try {
                c = db.rawQuery(query, null);
                while (c.moveToNext()) {
                    roomRecord = new HotelRoomRecord(c);

                    Log.d(TAG,roomRecord + "|| №: " + roomRecord.operNum + " name: " + roomRecord.operName
                            + " check: " + roomRecord.operNumCheck + " call: " + roomRecord.operCall + " inRoom: " + roomRecord.operInRoom +
                            " quit: " + roomRecord.operQuit + " nameGorn: " + roomRecord.operNameGorn);
                }
            } catch (Exception e) {
                if (D) Log.e(TAG, "Exception: " + e.getMessage());
            } finally {
                if (c != null) c.close();
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnBarSale:
                    showRoom(RoomBarGoodsRecord.DOC_SALE_TYPE);

                    if(roomRecord.operNumCheck || roomRecord.operInRoom){
                    btnChambermaidQuit.setVisibility(View.VISIBLE);
                    btnChambermaidQuit.setEnabled(true);}
                    break;
                case R.id.btnMovement:
                    showRoom(RoomBarGoodsRecord.DOC_STORE_TYPE);
                    if(roomRecord.operNumCheck|| roomRecord.operInRoom){
                    btnChambermaidQuit.setVisibility(View.VISIBLE);
                    btnChambermaidQuit.setEnabled(true);
                    }
                    break;
                case R.id.btnAcceptCheckRoom:
                        sendSOAPRequest(STATE_POST_CHECK_ROOM_CALL_ACCEPTED);
                        btnAcceptCheckRoom.setText("ВЫЗОВ ПРИНЯТ");
                        Log.d(TAG, "isCallChambermaid()" + mIdEmpStr + mIdHotelStr + fName);
                    break;
                case R.id.btnGetCall:
                    sendSOAPRequest(STATE_POST_CHECK_ROOM_ACCEPTED);
                    btnGetCall.setVisibility(View.GONE);
                    btnChambermaidQuit.setVisibility(View.VISIBLE);
                    btnBarSale.setEnabled(true);
                    btnMovement.setEnabled(true);
                    Log.d(TAG, "STATE_POST_CHECK_ROOM_ACCEPTED" + mIdEmpStr + mIdHotelStr + fName);
                   // getChambermaid();
                    break;
                case R.id.btnService:
                    sendSOAPRequest(STATE_POST_SERVICE);
                    break;
                case R.id.btnBed:
                    sendSOAPRequest(STATE_POST_CHANGE_BED);
                    sendNewCheckRoomNotification();
                    break;
                case R.id.btnChambermaidQuit:
                    sendSOAPRequest(STATE_POST_QUIT);
                    Log.d(TAG, "STATE_POST_QUIT " + mIdEmpStr + mIdHotelStr + fName);

                    break;
                case R.id.btnMessage:
                    showInputNameDialog();
                    break;
            }
        }

        private void showRoom(int mode) {
            if (CommonClass.getDebugMode(context)) {
                dbSchR.clearRoomBarGoodSales();
            }
            Intent intent = new Intent(context, RoomBarGoodsActivity.class);
            Bundle b = new Bundle();
            b.putString("mode", ""+ mode);
            b.putString("Room", ""+ mRoom);
            b.putString("idHotel", mIdHotelStr);
            b.putString("idEmp", mIdEmpStr);
            intent.putExtras(b);

            startActivityForResult(intent, DIALOG_FRAGMENT);
        }

        private void updateButtonState() {
            btnService.setEnabled(roomRecord.isServiceNeeded() && mSOAPState == STATE_FINISH);
            btnChambermaidQuit.setEnabled(roomRecord.isTodayClearence() && mSOAPState == STATE_FINISH);
            //btnBar.setEnabled(mSOAPState == STATE_FINISH);
            btnMessage.setEnabled(mSOAPState == STATE_FINISH);

            btnChangeBed.setEnabled(mSOAPState == STATE_FINISH);//---------------------
        }

        private void sendSOAPRequest(int state) {
            try {
                mSOAPState = state;
                switch (mSOAPState) {
                    case STATE_POST_QUIT:
                        SOAPMethod = METHOD_POST_QUIT;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        break;
                    case STATE_POST_SERVICE:
                        SOAPMethod = METHOD_POST_SERVICE;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        break;
                    case STATE_POST_MESSAGE:
                        SOAPMethod = METHOD_POST_MESSAGE;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        break;
                    case STATE_POST_CHANGE_BED:
                        SOAPMethod = METHOD_POST_CHANGE_BED;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        sendNewCheckRoomNotification();
                        break;
                    case STATE_POST_CHECK_ROOM_ACCEPTED:
                        SOAPMethod = METHOD_POST_CHECK_ROOM_ACCEPTED;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                        break;
                    case STATE_POST_CHECK_ROOM_CALL_ACCEPTED:
                        SOAPMethod = METHOD_POST_CHECK_ROOM_CALL_ACCEPTED;
                        SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
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
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        private void showInputNameDialog() {
            FragmentManager fragmentManager = getChildFragmentManager();// getActivity().getSupportFragmentManager();
            InputNameDialogFragment inputNameDialog = new InputNameDialogFragment();
            inputNameDialog.setCancelable(false);
            inputNameDialog.setDialogTitle(getString(R.string.title_msg));
            inputNameDialog.setDialogText("");
            inputNameDialog.setTargetFragment(this, DIALOG_FRAGMENT);
            inputNameDialog.show(fragmentManager, "input dialog");
        }

        @Override
        public void onFinishInputDialog(String inputText) {
            mMessage = inputText;
            if (mMessage.length() > 0)
                sendSOAPRequest(STATE_POST_MESSAGE);
        }

        private class AsyncTaskRunner extends AsyncTask<String, String, String> {
            private String resp = "";
            private boolean isPostOK = false;

            @Override
            protected String doInBackground(String... params) {
                publishProgress(getActivity().getString(R.string.title_init_post)); // Calls onProgressUpdate()
                try {
                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
                    SoapObject request = new SoapObject(CommonClass.NAMESPACE, SOAPMethod);

                    switch (mSOAPState) {
                        case STATE_POST_MESSAGE:
                            request.addProperty("Massege", mMessage);
                        case STATE_POST_SERVICE:
                        case STATE_POST_CHANGE_BED:
                            sendNewCheckRoomNotification();
                        case STATE_POST_CHECK_ROOM_ACCEPTED:

                        case STATE_POST_CHECK_ROOM_CALL_ACCEPTED:

                        case STATE_POST_QUIT:
                            request.addProperty("idHotell", mIdHotelStr);
                            request.addProperty("idGorn", mIdEmpStr);
                            request.addProperty("nom", mRoom);
                            break;
                    }
                    envelope.bodyOut = request;
                    envelope.setAddAdornments(false);
                    envelope.implicitTypes = true;
                    HttpTransportSE transport = new HttpTransportSE(CommonClass.getServerURI(getActivity()));
                    transport.debug = true;
                    try {
                        String auth = CommonClass.LOGIN + ":" + CommonClass.PSW;
                        List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
                        headerList.add(new HeaderProperty("Authorization",
                                "Basic " + org.kobjects.base64.Base64.encode(auth.getBytes())));

                        SoapObject response = null;
                        transport.call(SOAPAction, envelope, headerList);
                        //bodyIn is the body object received with this envelope
                        if (envelope.bodyIn != null) {
                            if (envelope.bodyIn instanceof SoapObject) {
                                publishProgress(getActivity().getString(R.string.title_parse)); // Calls onProgressUpdate()
                                response = (SoapObject) envelope.bodyIn;
                                parseSOAPResponse(response);
                            } else if (envelope.bodyIn instanceof SoapFault12) {
                                resp = ((SoapFault12) envelope.bodyIn).getMessage().toString();
                                String msg = TAG + " " + mSOAPState + "; " + resp;
                                dbSchR.addLogItem(LogsRecord.ERROR, new Date(), msg);
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
                    case STATE_POST_CHANGE_BED:
                    case STATE_POST_CHECK_ROOM_ACCEPTED:
                    case STATE_POST_CHECK_ROOM_CALL_ACCEPTED:
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
                            roomRecord.setServiceNeeded(false);
                            btnService.setText(getString(R.string.btn_service));
                            broomImageView.setVisibility(roomRecord.isServiceNeeded() ? View.VISIBLE : View.INVISIBLE);
                            broomImageView2.setVisibility(roomRecord.isServiceNeeded() ? View.VISIBLE : View.INVISIBLE);
                            dbSchR.updateHotelRoomService(false, mIdHotelStr, mRoom);
                        }
                    case STATE_POST_CHANGE_BED:
                        if (isPostOK){
                            sendNewCheckRoomNotification();
                        }
                    case STATE_POST_CHECK_ROOM_ACCEPTED:
                    case STATE_POST_CHECK_ROOM_CALL_ACCEPTED:
                    case STATE_POST_MESSAGE:
                    case STATE_POST_QUIT:
                        if (isPostOK)
                            msg = getString(R.string.m_post_ok);
                        else
                           // msg = getString(R.string.m_post_err);
                        break;
                }
                mSOAPState = STATE_FINISH;
                updateButtonState();

                msg = result + msg;
                if (msg.length() >  0)
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected void onProgressUpdate(String... text) {
                Toast.makeText(getActivity(), text[0], Toast.LENGTH_SHORT).show();
            }

        }
        private void sendNewCheckRoomNotification() {
            if (notifyNewMsg && empId.length() > 0 && notifyRooms != null && notifyRooms.size() > 0) {
                String msg = getHotelRoomNeedsChecking();
                Context context = getActivity();
                CharSequence contentTitle = "JJJJJJ";
                CharSequence contentText = "Комнаты " + msg;
                Intent msgIntent = new Intent(getActivity(), HotelRoomsSwipeActivity.class);
                Bundle b = new Bundle();
                b.putString("idEmp", empId);
                b.putString("idHotel", notifyRooms.get(0).getHotelIdStr());
                b.putString("Hotel", "" + notifyRooms.get(0).getHotelName());
                b.putString("Floor", "" + notifyRooms.get(0).getRoomFloor());
                msgIntent.putExtras(b);

                PendingIntent intent = PendingIntent.getActivity(
                        getActivity(), 0, msgIntent, Intent.FLAG_ACTIVITY_NEW_TASK); // FLAG_ACTIVITY_NEW_TASK
                n_rooms.flags |= Notification.FLAG_AUTO_CANCEL;
                n_rooms.setLatestEventInfo(context, contentTitle, contentText, intent);
                mNManager.notify(CHECK_ROOM_NOTIFY_ID, n_rooms);
            }
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
        // method comparison date
        public void dateRedact(){
            String a_res = a.replaceAll("\\s", "");
            timeRes = a_res.substring(8,10);
            timeR = Integer.parseInt(timeRes);
            int i = a_res.length();
            int s = (i - 4);
            String a_sub = a_res.substring(0,8);
            String a_sub2 = a_res.substring(s,i);
            a = a_sub.concat(a_sub2);
            String b_res = b.replaceAll("\\s", "");
            if(b !="null" && b_res.length()>5){
                timeDep = b_res.substring(8,10);
                timeD = Integer.parseInt(timeDep);
                int r = b_res.length();
                int t = (r - 4);
                String b_sub = b_res.substring(0, 8);
                String b_sub2 = b_res.substring(t, r);
                b = b_sub.concat(b_sub2);
            }
            long date = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            d = sdf.format(date);
            String d_res = d.replaceAll("\\s", "");
            String d_sub = d_res.substring(0, 2);
           // String d_sub2 = d_res.substring(6, 10);
            d = d_sub;
            c = a;
            String c_c = c.substring(6, 8);
            c = c_c;

        }

    }
}
