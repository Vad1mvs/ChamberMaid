package com.utis.chambermaid;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.chambermaid.records.EmpRecord;
import com.utis.chambermaid.records.HotelRoomRecord;
import com.utis.chambermaid.records.OperRecord;
import com.utis.chambermaid.records.PlacementRecord;
import com.utis.chambermaid.tables.EmpTable;
import com.utis.chambermaid.tables.HotelRoomTable;
import com.utis.chambermaid.tables.OperTable;
import com.utis.chambermaid.tables.PlacementTable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HotelRoomsSwipeActivity extends FragmentActivity implements ConnectionManagerService.ServiceResponseCallback {
    private static final boolean D = true;
    private static final String TAG = "HotelRoomsSwipeActivity";
    private static final int BLINK_INTERVAL = 2;
    private static final String STANDART_TIME = "12:00";

    CollectionPagerAdapter mCollectionPagerAdapter;
    ViewPager mViewPager;
    private DBSchemaHelper dbSch;
    private Context mContext;
    private int mIdHotel, mIdEmp, mFloorCnt, mSelectedFloor;
    private String mIdHotelStr, mIdEmpStr, mHotel, mFloor;
    int[] floorArray;
    boolean debugMode, manualRefresh;
    private ConnectionManagerService connectionService;
    private Intent serviceIntent;
    ActionBar.Tab[] tabArray;
    private SparseArray<Fragment> mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_rooms_swipe);
        mContext = this;
        dbSch = DBSchemaHelper.getInstance(this);
        serviceIntent = new Intent(this, ConnectionManagerService.class);

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
            if (b.containsKey("Floor")) {
                mFloor = b.getString("Floor");
                mSelectedFloor = Integer.parseInt(mFloor);
            }
        }
        debugMode = CommonClass.getDebugMode(mContext);
        mFloorCnt = dbSch.getPlacementFloorCount(mIdHotel);
        floorArray = new int[mFloorCnt];
        tabArray = new ActionBar.Tab[mFloorCnt];
        workwithTabbedActionBar();
        mCollectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the corresponding tab.
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        mViewPager.setAdapter(mCollectionPagerAdapter);
        if (mFloor != null) {  //select floor
            int floorTab = findFloorPos(mSelectedFloor);
            if (mViewPager != null && floorTab >= 0)
                mViewPager.setCurrentItem(floorTab);
        }
    }

    private int findFloorPos(int floor) {
        int res = -1;
        for (int i = 0; i < floorArray.length; i++) {
            if (floorArray[i] == floor) {
                res = i;
                break;
            }
        }
        return res;
    }

    public void workwithTabbedActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(mHotel);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        initTabs(actionBar);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!(connectionService != null && !connectionService.isWorking())) {
            //Starting the service makes it stick, regardless of bindings
            startService(serviceIntent);
        }
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        refreshRooms();
    }

    @Override
    public void onPause() {
        super.onPause();

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
            connectionService.periodicTask(true);
        }

        public void onServiceDisconnected(ComponentName className) {
            connectionService = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hotel_rooms_swipe, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem mi = menu.findItem(R.id.action_refresh);
        if (mi != null) {
            mi.setEnabled(!debugMode && connectionService != null);
            mi.setVisible(!debugMode && connectionService != null);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
//                sendSOAPRequest(STATE_GET_ROOMS);
                sendRefreshRequest();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendRefreshRequest() {
        if (connectionService != null) {
            manualRefresh = true;
            connectionService.reRunTimer();
        }
    }

    private void initTabs(ActionBar actionBar) {
        String query = "SELECT * FROM " + PlacementTable.TABLE_NAME + " WHERE " +
                PlacementTable.ID_ENT + "=" + mIdHotel + " AND " +
                PlacementTable.TTYPE + "=" + PlacementRecord.PLACE_FLOOR;
        Cursor c = null;
        int colid, id, cntr = 0;
        String name;
        MyTabListener tl = new MyTabListener();
        SQLiteDatabase sqdb = dbSch.getWritableDatabase();
        try {
            c = sqdb.rawQuery(query, null);
            while (c.moveToNext()) {
                colid = c.getColumnIndex(PlacementTable.NAME);
                name = c.getString(colid);
                colid = c.getColumnIndex(PlacementTable.ID_EXTERNAL);
                id = c.getInt(colid);
                floorArray[cntr] = id;
                tabArray[cntr] = actionBar.newTab().setText(name);
                tabArray[cntr].setTabListener(tl);
                actionBar.addTab(tabArray[cntr]);
                cntr++;
            }
        } catch(Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
    }

    public class MyTabListener implements ActionBar.TabListener {

        //	constructor	code
        public	MyTabListener() {
        }
        //	callbacks

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
//            mSelectedFloor = Integer.parseInt(tab.getText().toString().substring(0, 1));
            // When the tab is selected, switch to the
            // corresponding page in the ViewPager.
            if (mViewPager != null)
                mViewPager.setCurrentItem(tab.getPosition());

//            showRoomsEven(mSelectedFloor);
//            showRoomsOdd(mSelectedFloor);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }
    }

    private boolean refreshRooms() {
        boolean refreshed = false;
        for (int i = 0; i < mapFragment.size(); i++) {
            FloorObjectFragment fragment = (FloorObjectFragment) mapFragment.valueAt(i);
            fragment.refreshRooms();
            refreshed = true;
        }
        return refreshed;
    }

    @Override
    public void onSrvRequestSuccess(String response) {
        boolean refreshed = refreshRooms();
        if (manualRefresh) {
            manualRefresh = false;
            if (refreshed)
                Toast.makeText(mContext, getString(R.string.m_get_room_status_ok), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSrvRequestError(Exception error) {
        manualRefresh = false;
        Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSrvTaskChanged(int Task) {

    }

    // Since this is an object collection, use a FragmentStatePagerAdapter,
    // and NOT a FragmentPagerAdapter.
    public class CollectionPagerAdapter extends FragmentStatePagerAdapter {

        public CollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = FloorObjectFragment.newInstance(i, "", floorArray[i], mIdHotelStr, mIdEmpStr);
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
            return mFloorCnt;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return floorArray[position] + mContext.getString(R.string.m_floor);
        }

    }

    // Instances of this class are fragments representing a single
    // object in our collection.
    public static class FloorObjectFragment extends Fragment {
        public static final String ARG_NUM = "num";
        public static final String ARG_NAME = "name";
        public static final String ARG_FLOOR = "floor";
        public static final String ARG_HOTEL = "hotel";
        public static final String ARG_EMP = "emp";
        int mNum, idFloor, blinkCntr, blinkAccept, blinkDisturb, blinkExtra, blinkInRoom;
        String fName;
        private int mIdHotel;
        private String mIdEmpStr, mIdHotelStr;
        private Handler mHandler = new Handler();
        private Handler handlerAccept = new Handler();
       // boolean blink = true;

        private boolean exiting;
        private DBSchemaHelper dbSchF;
        private ArrayList<HotelRoomRecord> roomArrayList1, roomArrayList2;
        private ArrayList<OperRecord> operArrayList1, operArrayList2;
        private ListView mDBListView1, mDBListView2;
        private HotelRoomRecord roomRecord;
        private CustomHotelRoomAdapter mDBArrayAdapter1, mDBArrayAdapter2;


        /**
         * Create a new instance of CountingFragment, providing "num" as an argument.
         */
        static FloorObjectFragment newInstance(int num, String name, int idFloor,
                                          String idHotel, String idEmp) {
            FloorObjectFragment f = new FloorObjectFragment();
            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt(ARG_NUM, num);
            args.putString(ARG_NAME, name);
            args.putInt(ARG_FLOOR, idFloor);
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
            idFloor = getArguments() != null ? getArguments().getInt(ARG_FLOOR) : 0;
            mIdHotelStr = getArguments() != null ? getArguments().getString(ARG_HOTEL) : "";
            mIdHotel = Integer.parseInt(mIdHotelStr);
            mIdEmpStr = getArguments() != null ? getArguments().getString(ARG_EMP) : "";
            roomArrayList1 = new ArrayList<HotelRoomRecord>();
            roomArrayList2 = new ArrayList<HotelRoomRecord>();

            operArrayList1 = new ArrayList<OperRecord>();
            operArrayList2 = new ArrayList<OperRecord>();
            dbSchF = DBSchemaHelper.getInstance(getActivity());
            mDBArrayAdapter1 = new CustomHotelRoomAdapter(getActivity(), R.layout.rooms_odd_row, roomArrayList1, true);
            mDBArrayAdapter2 = new CustomHotelRoomAdapter(getActivity(), R.layout.rooms_even_row, roomArrayList2, false);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated properly.
            View rootView = inflater.inflate(R.layout.activity_hotel_rooms_activity2, container, false);

            mDBListView1 = (ListView) rootView.findViewById(R.id.listViewRooms1);
            mDBListView2 = (ListView) rootView.findViewById(R.id.listViewRooms2);
            mDBListView1.setAdapter(mDBArrayAdapter1);
            mDBListView1.setOnItemClickListener(mRooms1ClickListener);
            mDBListView2.setAdapter(mDBArrayAdapter2);
            mDBListView2.setOnItemClickListener(mRooms2ClickListener);

           // showRoomsEven(idFloor);
            showRoomsOdd(idFloor);
            showRoomsEven(idFloor);
            mHandler.postDelayed(blinking, TimeUnit.SECONDS.toMillis(BLINK_INTERVAL));
            handlerAccept.postDelayed(blinkAcceptNumber, TimeUnit.SECONDS.toMillis(BLINK_INTERVAL));
            return rootView;
        }

        @Override
        public void onDestroyView() {
            exiting = true;
            mHandler.removeCallbacks(blinking);
            super.onDestroyView();
        }


        // blink status num
        private Runnable blinkAcceptNumber = new Runnable() {
            @Override
            public void run() {
                if (!exiting) {
                    mDBArrayAdapter1.notifyDataSetChanged();
                    mDBArrayAdapter2.notifyDataSetChanged();
                    handlerAccept.postDelayed(blinkAcceptNumber, TimeUnit.SECONDS.toMillis(BLINK_INTERVAL)/6);
                    blinkAccept++;
                    if(blinkAccept == 3){
                        blinkAccept =+ 0;
                    }

                    blinkDisturb++;
                    if(blinkDisturb == 6){
                        blinkDisturb =+0;
                    }
                    blinkExtra++;
                    if(blinkExtra == 8){
                        blinkExtra =+0;
                    }

                    blinkInRoom++;
                    if(blinkInRoom == 6){
                        blinkInRoom =+0;
                    }
                }
            }
        };


        private Runnable blinking = new Runnable() {
            @Override
            public void run() {
                if (!exiting) {
                    mDBArrayAdapter1.notifyDataSetChanged();
                    mDBArrayAdapter2.notifyDataSetChanged();
                    mHandler.postDelayed(blinking, TimeUnit.SECONDS.toMillis(BLINK_INTERVAL));
                    blinkCntr++;
                }
            }
        };
        public void refreshRooms() {
            if (idFloor > 0) {

                showRoomsOdd(idFloor);
                showRoomsEven(idFloor);
            }
        }


        // The on-click listener for ListViews
        private AdapterView.OnItemClickListener mRooms1ClickListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
                roomRecord = mDBArrayAdapter1.getItem(arg2);
                showRoom(roomRecord);
            }
        };

        private AdapterView.OnItemClickListener mRooms2ClickListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
                roomRecord = mDBArrayAdapter2.getItem(arg2);
                showRoom(roomRecord);
            }
        };




        private void showRoom(HotelRoomRecord roomRec) {
            Intent intent = new Intent(getActivity(), RoomSwipeActivity.class);
            Bundle b = new Bundle();
            b.putString("Room", ""+ roomRec.getRoomNum());
            b.putString("idHotel", roomRec.getHotelIdStr());
            b.putString("Hotel", roomRec.getHotelName());
            b.putString("floor", ""+ roomRec.getRoomFloor());
            b.putString("idEmp", mIdEmpStr);
            b.putString("broom", ""+ roomRec.isServiceNeeded());
            b.putString("person", "" + roomRec.isGuestInRoom());
            b.putString("occupied", "" + roomRec.isRoomOccupied());
            b.putString("quit", ""+ roomRec.isTodayClearence());
            b.putString("door", "" + roomRec.isDoor());
            b.putString("window", "" + roomRec.isWindow());
            b.putString("balcony", ""+ roomRec.getBalcony());
            b.putString("repair", ""+ roomRec.isRepair());
            b.putString("status_nom", ""+ roomRec.isStatusNom());
            b.putString("note_repair", ""+ roomRec.getNoteRepair());
            b.putString("change_bed_extra", ""+ roomRec.getChangeBedExtra());
            b.putString("tipe_clearence", ""+ roomRec.getTipeClearence());
            b.putString("noy_disturb", ""+ roomRec.isNotDisturb());
            b.putString("call_chambermaid", ""+ roomRec.isCallChambermaid());
            b.putString("chambermaid_in_room", ""+ roomRec.isChambermaidInRoom());

            b.putString("oper_num", ""+ roomRec.operNum);
            b.putString("oper_name", ""+ roomRec.operName);
            b.putString("oper_num_check", ""+ roomRec.operNumCheck);
            b.putString("oper_call", ""+ roomRec.operCall);
            b.putString("oper_in_room", ""+ roomRec.operInRoom);
            b.putString("oper_quit", ""+ roomRec.operQuit);
            b.putString("oper__name_gorn", ""+ roomRec.operNameGorn);
            intent.putExtras(b);
            startActivity(intent);
        }

        static class RoomViewHolder {
            public TextView textRoomNum,textRoomTime, tvNotfication;
            public ImageView imgBeds, imgDrops, imgPerson, imgBroom, imgBroom2, imgBalcony, imgWindow;
            public ImageView imgDoor;
            public RelativeLayout rlImages;
        }

        public class CustomHotelRoomAdapter extends ArrayAdapter<HotelRoomRecord> {
            private Random r;
            String a, b, d, c, timeRes, timeDep;
            int timeR, timeD;
            private boolean mOddColumn;
            RoomViewHolder viewHolder;
            View row;
            public CustomHotelRoomAdapter(Context context, int resource,
                                          ArrayList<HotelRoomRecord> litem, boolean oddColumn) {
                super(context, resource, litem);
                mOddColumn = oddColumn;
                r = new Random();
            }

            private void setRoomBedView(RoomViewHolder viewHolder, HotelRoomRecord item) {
                viewHolder.textRoomTime.setVisibility(View.INVISIBLE);
                viewHolder.imgBeds.setVisibility(View.VISIBLE);
                viewHolder.imgBeds.setImageDrawable(item.isTwin() ? getResources().getDrawable(R.drawable.twin_bed_s) :
                        getResources().getDrawable(R.drawable.double_bed_s));
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
               // RoomViewHolder viewHolder;
                row = convertView;
                //Inflate a new row if one isn't recycled
                if (row == null) {
                    if (mOddColumn)
                        row = LayoutInflater.from(getContext()).inflate(R.layout.rooms_odd_row, parent, false);

                    else
                        row = LayoutInflater.from(getContext()).inflate(R.layout.rooms_even_row, parent, false);
                    viewHolder = new RoomViewHolder();
                    viewHolder.textRoomNum = (TextView)row.findViewById(R.id.roomNum);
                    viewHolder.textRoomTime = (TextView)row.findViewById(R.id.time);
                    viewHolder.tvNotfication = (TextView)row.findViewById(R.id.tvNotfication);
                    viewHolder.imgBeds = (ImageView)row.findViewById(R.id.bed);
                    viewHolder.imgDrops = (ImageView)row.findViewById(R.id.drop);
                    viewHolder.imgPerson = (ImageView)row.findViewById(R.id.person);
                    viewHolder.imgBroom = (ImageView)row.findViewById(R.id.broom);
                    viewHolder.imgBroom2 = (ImageView)row.findViewById(R.id.broom2);
                    viewHolder.imgBalcony = (ImageView)row.findViewById(R.id.balcony);
                    viewHolder.imgWindow = (ImageView)row.findViewById(R.id.window);
                    viewHolder.imgDoor = (ImageView)row.findViewById(R.id.door);
                    viewHolder.rlImages = (RelativeLayout) row.findViewById(R.id.rlImages);
                    row.setTag(viewHolder);
                } else {
                    viewHolder = (RoomViewHolder) row.getTag();
                }
                HotelRoomRecord item = getItem(position);
                viewHolder.textRoomNum.setText("" + item.getRoomNum());
                String time = DBSchemaHelper.dateFormatOnlyTimeMM.format(item.getDepartureDate());

                a = String.valueOf(item.getDepartureDate());
                b = String.valueOf(item.getReservationsDate());
                dateRedact();

                viewHolder.textRoomTime.setText(time);
                viewHolder.textRoomTime.setTextColor(time.contentEquals(STANDART_TIME) ? Color.BLACK : Color.RED);
                viewHolder.imgDrops.setVisibility(item.isWaterLeakage() ? View.VISIBLE : View.GONE);

                // ----- if status num occupied -----
                if (item.isRoomOccupied()) {
                    setRoomBedView(viewHolder, item);

                    if (item.isDepartureDay()) {
                        setRoomBedView(viewHolder, item);// в день выселения не показывать смену постели

                        if(time.contentEquals(STANDART_TIME))
                        {
                            viewHolder.imgBeds.setVisibility(View.VISIBLE);
                        }else {
                            if (blinkCntr % 2 != 0) {
                                viewHolder.textRoomTime.setVisibility(View.VISIBLE);
                                viewHolder.imgBeds.setVisibility(View.INVISIBLE);
                            } else {
                                viewHolder.textRoomTime.setVisibility(View.INVISIBLE);
                                viewHolder.imgBeds.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        viewHolder.textRoomTime.setVisibility(View.INVISIBLE);
                        viewHolder.imgBeds.setVisibility(View.VISIBLE);
                    }
                } else {
                    setRoomBedView(viewHolder, item);
                }

                if (item.isGuestInRoom() ){
                    viewHolder.imgPerson.setVisibility(View.VISIBLE);}
                else{
                    viewHolder.imgPerson.setVisibility(View.INVISIBLE);}
                if (item.isServiceNeeded()){
                    viewHolder.imgBroom.setVisibility(View.VISIBLE);}
                else
                { viewHolder.imgBroom.setVisibility(View.INVISIBLE);}
                if(item.getTipeClearence() == 2 ){
                    viewHolder.imgBroom.setVisibility(View.INVISIBLE);
                    viewHolder.imgBroom2.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.imgBroom2.setVisibility(View.INVISIBLE);
                }

                if (mOddColumn) {
                    if (item.getBalcony() == 2) {
                        viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                        viewHolder.imgBalcony.setVisibility(View.VISIBLE);
                    } else if (item.getBalcony() == 1) {
                        viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
                        viewHolder.imgBalcony.setVisibility(View.VISIBLE);
                    } else
                /*viewHolder.imgBalcony.setVisibility(View.INVISIBLE)*/;
                    if (item.getBalcony() == 0) {
                        viewHolder.imgWindow.setVisibility(View.INVISIBLE);
                        if (item.isWindow())
                            viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                        else
                            viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
                    } else {
                        viewHolder.imgWindow.setVisibility(View.VISIBLE);
                        if (item.isWindow())
                            viewHolder.imgWindow.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
                        else
                            viewHolder.imgWindow.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
                    }

                    if (item.isDoor())
                        viewHolder.imgDoor.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
                    else
                        viewHolder.imgDoor.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
                } else {
                    if (item.getBalcony() == 2) {
                        viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                        viewHolder.imgBalcony.setVisibility(View.VISIBLE);
                    } else if (item.getBalcony() == 1) {
                        viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
                        viewHolder.imgBalcony.setVisibility(View.VISIBLE);
                    } else
                /*viewHolder.imgBalcony.setVisibility(View.INVISIBLE)*/;

                    if (item.getBalcony() == 0) {
                        viewHolder.imgWindow.setVisibility(View.INVISIBLE);
                        if (item.isWindow())
                            viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                        else
                            viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
                    } else {
                        viewHolder.imgWindow.setVisibility(View.VISIBLE);
                        if (item.isWindow())
                            viewHolder.imgWindow.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
                        else
                            viewHolder.imgWindow.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
                    }

                    if (item.isDoor())
                        viewHolder.imgDoor.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
                    else
                        viewHolder.imgDoor.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
                }



                // ----- status num not Disturb -----
                if(item.isNotDisturb()) {
                    viewHolder.tvNotfication.setVisibility(blinkDisturb == 0 ? View.VISIBLE : View.INVISIBLE);
                    viewHolder.textRoomNum.setVisibility(blinkDisturb == 0 ? View.INVISIBLE : View.VISIBLE);
                    viewHolder.imgBeds.setVisibility(blinkDisturb == 0 ? View.INVISIBLE : View.VISIBLE);
                    viewHolder.tvNotfication.setText("НЕ БЕСПОКОИТЬ");
                    if(blinkDisturb == 0){
                        viewHolder.imgPerson.setVisibility(View.INVISIBLE);
                        viewHolder.imgPerson.setVisibility(View.INVISIBLE);
                        viewHolder.imgDrops.setVisibility(View.INVISIBLE);
                        viewHolder.imgBroom.setVisibility(View.INVISIBLE);
                        viewHolder.imgBroom2.setVisibility(View.INVISIBLE);
                        viewHolder.imgBalcony.setVisibility(View.INVISIBLE);
                        viewHolder.imgWindow.setVisibility(View.INVISIBLE);
                        viewHolder.imgDoor.setVisibility(View.INVISIBLE);
                    }
                    if (b.equals(a) && c.equals(d) && item.isStatusNom()) {

                        if (blinkDisturb == 0) {
                            row.setBackgroundResource(R.drawable.gradient_bg_yellow);
                        } else if (blinkDisturb == 1) {
                            row.setBackgroundResource(R.drawable.gradient_bg_orange);
                        } else if (blinkDisturb == 2) {
                            row.setBackgroundResource(R.drawable.list_selector_status);
                        }
                    } else if (item.isTodayClearence()) {
                        if (blinkDisturb == 0) {
                            row.setBackgroundResource(R.drawable.gradient_bg_yellow);
                        } else if (blinkDisturb == 1) {
                            row.setBackgroundResource(R.drawable.gradient_bg_orange);
                        } else if (blinkDisturb == 2) {
                            row.setBackgroundResource(R.drawable.list_selector_quit);
                        }
                    } else if (item.isRoomOccupied()) {
                        if (blinkDisturb == 0) {
                            row.setBackgroundResource(R.drawable.gradient_bg_yellow);
                        } else if (blinkDisturb == 1) {
                            row.setBackgroundResource(R.drawable.gradient_bg_orange);
                        } else if (blinkDisturb == 2) {
                            row.setBackgroundResource(R.drawable.list_selector_occupied);
                        }
                    }
                }else

                // ----- срочный вызов горничной -------------
                if(item.operCall ){
                    viewHolder.tvNotfication.setVisibility(blinkExtra == 4 ? View.VISIBLE : View.INVISIBLE);
                    viewHolder.textRoomNum.setVisibility(blinkExtra == 4 ? View.INVISIBLE : View.VISIBLE);
                    viewHolder.imgBeds.setVisibility(blinkExtra == 4 ? View.INVISIBLE : View.VISIBLE);
                    viewHolder.tvNotfication.setText("СРОЧНЫЙ ВЫЗОВ");
                    if(blinkExtra == 4 ){
                        viewHolder.imgPerson.setVisibility(View.INVISIBLE);
                        viewHolder.imgDrops.setVisibility(View.INVISIBLE);
                        viewHolder.imgBroom.setVisibility(View.INVISIBLE);
                        viewHolder.imgBroom2.setVisibility(View.INVISIBLE);
                        viewHolder.imgBalcony.setVisibility(View.INVISIBLE);
                        viewHolder.imgWindow.setVisibility(View.INVISIBLE);
                        viewHolder.imgDoor.setVisibility(View.INVISIBLE);
                    }
                if(b.equals(a) && c.equals(d) && item.isStatusNom()){
                    if (blinkExtra==0 ){
                        row.setBackgroundResource(R.drawable.gradient_bg_red_blink);
                    } else if (blinkExtra==1  ){
                        row.setBackgroundResource(R.drawable.list_selector_status);
                    } else if (blinkExtra==4){
                        row.setBackgroundResource(R.drawable.gradient_bg_yellow_blink);
                    }else if(blinkExtra==5) {
                        row.setBackgroundResource(R.drawable.list_selector_status);
                    }
                } else if(item.isTodayClearence()){
                    if (blinkExtra==0 ){
                        row.setBackgroundResource(R.drawable.gradient_bg_red_blink);
                    } else if (blinkExtra==1  ){
                        row.setBackgroundResource(R.drawable.list_selector_quit);
                    } else if (blinkExtra==4){
                        row.setBackgroundResource(R.drawable.gradient_bg_yellow_blink);
                    }else if(blinkExtra==5) {
                        row.setBackgroundResource(R.drawable.list_selector_quit);
                    }
                } else if(item.isRoomOccupied()){
                    if (blinkExtra==0 ){
                        row.setBackgroundResource(R.drawable.gradient_bg_red_blink);
                    } else if (blinkExtra==1  ){
                        row.setBackgroundResource(R.drawable.list_selector_occupied);
                    } else if (blinkExtra==4){
                        row.setBackgroundResource(R.drawable.gradient_bg_yellow_blink);
                    }else if(blinkExtra==5) {
                        row.setBackgroundResource(R.drawable.list_selector_occupied);
                    }
                }else if(item.getDaysBeforeReservation() == 0 && item.isRoomOccupied()) {
                    if (blinkExtra == 0) {
                        row.setBackgroundResource(R.drawable.gradient_bg_red_blink);
                    } else if (blinkExtra == 1) {
                        row.setBackgroundResource(R.drawable.list_selector_reserved);
                    } else if (blinkExtra == 4) {
                        row.setBackgroundResource(R.drawable.gradient_bg_yellow_blink);
                    } else if (blinkExtra == 5) {
                        row.setBackgroundResource(R.drawable.list_selector_reserved);
                    }
                }

                    // ----- status num Check -----
                }else if (item.operNumCheck) {
                    viewHolder.tvNotfication.setVisibility(blinkAccept == 1 ? View.VISIBLE : View.INVISIBLE);
                    viewHolder.textRoomNum.setVisibility(blinkAccept == 1 ? View.INVISIBLE : View.VISIBLE);
                    viewHolder.imgBeds.setVisibility(blinkAccept == 1 ? View.INVISIBLE : View.VISIBLE);
                    viewHolder.tvNotfication.setText("ПРИНЯТЬ НОМЕР");
                    if(blinkAccept == 1){
                        viewHolder.imgPerson.setVisibility(View.INVISIBLE);
                        viewHolder.imgDrops.setVisibility(View.INVISIBLE);
                        viewHolder.imgBroom.setVisibility(View.INVISIBLE);
                        viewHolder.imgBroom2.setVisibility(View.INVISIBLE);
                        viewHolder.imgBalcony.setVisibility(View.INVISIBLE);
                        viewHolder.imgWindow.setVisibility(View.INVISIBLE);
                        viewHolder.imgDoor.setVisibility(View.INVISIBLE);
                    }
                    if (blinkAccept==0 ){
                        viewHolder.rlImages.setBackgroundColor(getResources().getColor(R.color.blue_blink));
                    } else if (blinkAccept==1  ){
                        viewHolder.rlImages.setBackgroundColor(getResources().getColor(R.color.yellow_blink));

                    } else if (blinkAccept==2){
                        viewHolder.rlImages.setBackgroundColor(getResources().getColor(R.color.red_blink));
                    }
                }

                    // ----- status num chambermaid in room -----
                else if(item.operInRoom){
                        viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.occupied_t));
                        row.setBackgroundResource(R.drawable.list_selector_chambermaid_in_room);
                    viewHolder.tvNotfication.setVisibility(View.INVISIBLE);
                    viewHolder.imgBeds.setVisibility(View.VISIBLE);
                    viewHolder.textRoomNum.setVisibility(View.VISIBLE);
                }

                // ----- status num today clearence and check room -----
                else if(b.equals(a) && c.equals(d) && item.isStatusNom()){
                    viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.occupied_t));
                    if(timeD<timeR){
                        row.setBackgroundResource(R.drawable.list_selector_status);
                    } else {
                    row.setBackgroundResource(R.drawable.list_selector_status2);
                    }

                } else if (item.isTodayClearence()) {
                    viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.quit_t));
                    row.setBackgroundResource(R.drawable.list_selector_quit);
                } else if (item.isRoomOccupied()) {
                    viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.occupied_t));
                    row.setBackgroundResource(R.drawable.list_selector_occupied);
                } else if (item.getDaysBeforeReservation() == 0) {
                    viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.occupied_t));
                    row.setBackgroundResource(R.drawable.list_selector_reserved);
                } else if(item.isRepair()){
                    viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.occupied_t));
                    row.setBackgroundResource(R.drawable.list_selector_repair);
                } else
                     if (item.isStatusNom() ){
                     row.setBackgroundResource(R.drawable.list_selector_quit);
                }  else {
                   viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.empty));
                    row.setBackgroundResource(R.drawable.list_selector);
                }
                    return row;
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
                d = d_sub;
                c = a;
                String c_c = c.substring(6, 8);
                c = c_c;


            }
            }

        // SQLite request Hotel room status
        private void showRoomsOdd(int idFloor) {
            SQLiteDatabase db = dbSchF.getWritableDatabase();
            Cursor c = null;

            String subQueryNm = "(SELECT "  + OperTable.ROOM_NUM + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_NM;
            String subQueryNameGorn = "(SELECT "  + OperTable.NAME + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_NAME_GORN;
            String subQueryName = "(SELECT "  + OperTable.SURNAME + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_NAME;
            String subQueryCheck = "(SELECT "  + OperTable.CHECK_ROOM + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_NUM_CHECK;
            String subQueryCall= "(SELECT "  + OperTable.CALL_CHAMBERMAID + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_CALL;
            String subQueryInRoom= "(SELECT "  + OperTable.CHAMBERMAID_IN_ROOM + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_IN_ROOM;
            String subQueryQuit= "(SELECT "  + OperTable.CHAMBERMAID_QUIT + " FROM " + OperTable.TABLE_NAME +
                    " WHERE " + OperTable.ROOM_NUM + "= h." + HotelRoomTable.ROOM_NUM + " )as "+ HotelRoomRecord.OPER_QUIT;

            String query = "SELECT  h.*, " + subQueryNm +" , "+ subQueryName + " , " + subQueryCheck +
                    " , " + subQueryCall +" , " + subQueryInRoom +" , " + subQueryQuit + " , " + subQueryNameGorn +
                    "  FROM " + HotelRoomTable.TABLE_NAME +
                    " h WHERE " + HotelRoomTable.ID_HOTEL + "=" + mIdHotel +
                    " AND " + HotelRoomTable.ROOM_FLOOR + "=" + idFloor +
                    " AND " + HotelRoomTable.ROOM_NUM + " % 2 = 1" +
                    " ORDER BY " + HotelRoomTable.ROOM_NUM + " DESC";
            try {
                c = db.rawQuery(query, null);
                roomArrayList1.clear();
                while (c.moveToNext()) {
                    roomRecord = new HotelRoomRecord(c);
                    if (roomRecord.getRoomNum() > 0) {
                        roomArrayList1.add(roomRecord);

                        Log.d(TAG, " №= " + roomRecord.operNum + " name: " + roomRecord.operName
                        + " check: " + roomRecord.operNumCheck + " call: " + roomRecord.operCall + " inRoom: " + roomRecord.operInRoom +
                        " quit: " + roomRecord.operQuit + " nameGorn: " + roomRecord.operNameGorn);
                    }
                }
            } catch (Exception e) {
                if (D) Log.e(TAG, "Exception: " + e.getMessage());
            } finally {
                if (c != null) c.close();
                mDBArrayAdapter1.notifyDataSetChanged();
            }
        }
        private void showRoomsEven(int idFloor) {
            SQLiteDatabase db = dbSchF.getWritableDatabase();
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


            String query = "SELECT h.*, " + subQueryNm +" , "+ subQueryName + " , " + subQueryCheck +
                    " , " + subQueryCall  +" , " + subQueryInRoom  +" , " + subQueryQuit + " , " + subQueryNameGorn+
                    " FROM " + HotelRoomTable.TABLE_NAME +
                    " h " + "WHERE " + HotelRoomTable.ID_HOTEL + "=" + mIdHotel +
                    " AND " + HotelRoomTable.ROOM_FLOOR + "=" + idFloor +
                    " AND " + HotelRoomTable.ROOM_NUM + " % 2 <> 1" +
                    " ORDER BY " + HotelRoomTable.ROOM_NUM + " DESC";
            try {
                c = db.rawQuery(query, null);
                roomArrayList2.clear();
                while (c.moveToNext()) {
                    roomRecord = new HotelRoomRecord(c);
                    if (roomRecord.getRoomNum() > 0) {
                        roomArrayList2.add(roomRecord);
                        Log.d(TAG," №= " + roomRecord.operNum + " name: " + roomRecord.operName
                                + " check: " + roomRecord.operNumCheck + " call: " + roomRecord.operCall + " inRoom: " + roomRecord.operInRoom +
                                " quit: " + roomRecord.operQuit + " nameGorn: " + roomRecord.operNameGorn);
                    }
                }
            } catch (Exception e) {
                if (D) Log.e(TAG, "Exception: " + e.getMessage());
            } finally {
                if (c != null) c.close();
                mDBArrayAdapter2.notifyDataSetChanged();
            }
        }

    }

}
