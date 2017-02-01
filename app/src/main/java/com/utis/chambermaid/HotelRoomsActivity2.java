package com.utis.chambermaid;

import android.app.ActionBar;
import android.app.Activity;
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
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.chambermaid.records.HotelRoomRecord;
import com.utis.chambermaid.records.PlacementRecord;
import com.utis.chambermaid.tables.HotelRoomTable;
import com.utis.chambermaid.tables.PlacementTable;

import java.util.ArrayList;


public class HotelRoomsActivity2 extends Activity implements ConnectionManagerService.ServiceResponseCallback {
    private static final boolean D = true;
    private static final String TAG = "HotelRoomsActivity2";
    public static final int STATE_FINISH = 0; // process finishes
    private static final int STATE_GET_ROOMS = 1;
    private static final String METHOD_STATUS = "GetStatusNom";
    private DBSchemaHelper dbSch;
    private Context mContext;
    private int mIdHotel, mIdEmp, mFloorCnt, mSelectedFloor;
    private String mIdHotelStr, mIdEmpStr, mHotel;
    ActionBar.Tab[] tabArray;
    int[] floorArray;
    private ArrayList<HotelRoomRecord> roomArrayList1, roomArrayList2;
    private ListView mDBListView1, mDBListView2;
    private DBSchemaHelper dbSchF;
    private HotelRoomRecord roomRecord;
    private CustomHotelRoomAdapter1 mDBArrayAdapter1;
    private CustomHotelRoomAdapter2 mDBArrayAdapter2;
    private ArrayAdapter<HotelRoomRecord> mArrayAdapter;
    boolean debugMode, isPostOK;
    private int mState = 0, mSOAPState = STATE_FINISH;
    private String SOAPAction, SOAPMethod;
    private ConnectionManagerService connectionService;
    private Intent serviceIntent;
    private boolean manualRefresh;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_rooms_activity2);
        mContext = this;
        dbSch = DBSchemaHelper.getInstance(this);

        roomArrayList1 = new ArrayList<HotelRoomRecord>();
        roomArrayList2 = new ArrayList<HotelRoomRecord>();
        mDBListView1 = (ListView) findViewById(R.id.listViewRooms1);
        mDBListView2 = (ListView) findViewById(R.id.listViewRooms2);
        mDBArrayAdapter1 = new CustomHotelRoomAdapter1(this, R.layout.rooms_odd_row, roomArrayList1);
        mDBListView1.setAdapter(mDBArrayAdapter1);
        mDBListView1.setOnItemClickListener(mRooms1ClickListener);
        mDBArrayAdapter2 = new CustomHotelRoomAdapter2(this, R.layout.rooms_even_row, roomArrayList2);
        mDBListView2.setAdapter(mDBArrayAdapter2);
        mDBListView2.setOnItemClickListener(mRooms2ClickListener);
        serviceIntent = new Intent(this, ConnectionManagerService.class);

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
            Log.d(TAG, mIdEmp+" / "+ mIdHotel +" / "+ mHotel );
        }
        Toast.makeText(HotelRoomsActivity2.this, mIdEmpStr, Toast.LENGTH_SHORT).show();
        debugMode = CommonClass.getDebugMode(mContext);
        mFloorCnt = dbSch.getPlacementFloorCount(mIdHotel);
        tabArray = new ActionBar.Tab[mFloorCnt];
        floorArray = new int[mFloorCnt];
        initTabbedActionBar();
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
        getMenuInflater().inflate(R.menu.menu_hotel_rooms_activity2, menu);
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
            case R.id.action_settings:
                return true;
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

    // The on-click listener for ListViews
    private AdapterView.OnItemClickListener mRooms1ClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            roomRecord = mDBArrayAdapter1.getItem(arg2);
            if (D) Log.d(TAG, "num=" + roomRecord.getRoomNum());
            showRoom(roomRecord);
        }
    };

    private AdapterView.OnItemClickListener mRooms2ClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            roomRecord = mDBArrayAdapter2.getItem(arg2);
            if (D) Log.d(TAG, "num=" + roomRecord.getRoomNum());
            showRoom(roomRecord);
        }
    };

    private void showRoom(HotelRoomRecord roomRec) {
        Intent intent = new Intent(mContext, RoomActivity.class);
        Bundle b = new Bundle();
        b.putString("Room", ""+ roomRec.getRoomNum());
        b.putString("idHotel", roomRec.getHotelIdStr());
        b.putString("Hotel", roomRec.getHotelName());
        b.putString("idEmp", mIdEmpStr);
        b.putString("broom", ""+ roomRec.isServiceNeeded());
        b.putString("person", ""+ roomRec.isGuestInRoom());
        b.putString("occupied", ""+ roomRec.isRoomOccupied());
        b.putString("quit", ""+ roomRec.isTodayClearence());
        b.putString("door", ""+ roomRec.isDoor());
        b.putString("window", ""+ roomRec.isWindow());
        b.putString("balcony", ""+ roomRec.getBalcony());
        intent.putExtras(b);
        startActivity(intent);
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

    public void initTabbedActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(mHotel);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        initTabs(actionBar);
    }

    @Override
    public void onSrvRequestSuccess(String response) {
        refreshRooms();
        if (manualRefresh) {
            manualRefresh = false;
            Toast.makeText(mContext, getString(R.string.m_get_room_status_ok),
                    Toast.LENGTH_SHORT).show();
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

    static class RoomViewHolder {
        public TextView textRoomNum;
        public TextView textRoomTime;
        public ImageView imgBeds;
        public ImageView imgPerson;
        public ImageView imgBroom;
        public ImageView imgBalcony;
        public ImageView imgWindow;
        public ImageView imgDoor;


    }

    public class CustomHotelRoomAdapter1 extends ArrayAdapter<HotelRoomRecord> {
        private int id;

        public CustomHotelRoomAdapter1(Context context, int resource, ArrayList<HotelRoomRecord> litem) {
            super(context, resource, litem);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RoomViewHolder viewHolder;
            View row = convertView;
            //Inflate a new row if one isn't recycled
            if(row == null) {
                row = LayoutInflater.from(getContext()).inflate(R.layout.rooms_odd_row, parent, false);
                viewHolder = new RoomViewHolder();
                viewHolder.textRoomNum = (TextView)row.findViewById(R.id.roomNum);
                viewHolder.textRoomTime = (TextView)row.findViewById(R.id.time);
                viewHolder.imgBeds = (ImageView)row.findViewById(R.id.bed);
                viewHolder.imgPerson = (ImageView)row.findViewById(R.id.person);
                viewHolder.imgBroom = (ImageView)row.findViewById(R.id.broom);
                viewHolder.imgBalcony = (ImageView)row.findViewById(R.id.balcony);
                viewHolder.imgWindow = (ImageView)row.findViewById(R.id.window);
                viewHolder.imgDoor = (ImageView)row.findViewById(R.id.door);

                row.setTag(viewHolder);
            } else {
                viewHolder = (RoomViewHolder) row.getTag();
            }
            HotelRoomRecord item = getItem(position);
            viewHolder.textRoomNum.setText("" + item.getRoomNum());
            if (item.isGuestInRoom())
                viewHolder.imgPerson.setVisibility(View.VISIBLE);
            else
                viewHolder.imgPerson.setVisibility(View.INVISIBLE);
            if (item.isServiceNeeded())
                viewHolder.imgBroom.setVisibility(View.VISIBLE);
            else
                viewHolder.imgBroom.setVisibility(View.INVISIBLE);

            if (item.getBalcony() == 2) {
                viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
                viewHolder.imgBalcony.setVisibility(View.VISIBLE);
            } else if (item.getBalcony() == 1) {
                viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));
                viewHolder.imgBalcony.setVisibility(View.VISIBLE);
            } else
                /*viewHolder.imgBalcony.setVisibility(View.INVISIBLE)*/;

            if (item.getBalcony() == 0) {
                viewHolder.imgWindow.setVisibility(View.INVISIBLE);
                if (item.isWindow())
                    viewHolder.imgBalcony.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
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
                viewHolder.imgDoor.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
            else
                viewHolder.imgDoor.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
           if (item.isTodayClearence()) {
                viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.quit_t));
                row.setBackgroundResource(R.drawable.list_selector_quit);
            } else if (item.isRoomOccupied()) {
                viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.occupied_t));
                row.setBackgroundResource(R.drawable.list_selector_occupied);
            } else {
                viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.empty));
                row.setBackgroundResource(R.drawable.list_selector);
            }

            return row;
        }

    }

    public class CustomHotelRoomAdapter2 extends ArrayAdapter<HotelRoomRecord> {
        private int id;
        View row;

        public CustomHotelRoomAdapter2(Context context, int resource, ArrayList<HotelRoomRecord> litem) {
            super(context, resource, litem);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RoomViewHolder viewHolder;
            row = convertView;
            //Inflate a new row if one isn't recycled
            if(row == null) {
                row = LayoutInflater.from(getContext()).inflate(R.layout.rooms_even_row, parent, false);
                viewHolder = new RoomViewHolder();
                viewHolder.textRoomNum = (TextView)row.findViewById(R.id.roomNum);
                viewHolder.imgPerson = (ImageView)row.findViewById(R.id.person);
                viewHolder.imgBroom = (ImageView)row.findViewById(R.id.broom);
                viewHolder.imgBalcony = (ImageView)row.findViewById(R.id.balcony);
                viewHolder.imgWindow = (ImageView)row.findViewById(R.id.window);
                viewHolder.imgDoor = (ImageView)row.findViewById(R.id.door);
                row.setTag(viewHolder);
            } else {
                viewHolder = (RoomViewHolder) row.getTag();
            }
            HotelRoomRecord item = getItem(position);
            viewHolder.textRoomNum.setText("" + item.getRoomNum());
            if (item.isGuestInRoom())
                viewHolder.imgPerson.setVisibility(View.VISIBLE);
            else
                viewHolder.imgPerson.setVisibility(View.INVISIBLE);
            if (item.isServiceNeeded())
                viewHolder.imgBroom.setVisibility(View.VISIBLE);
            else
                viewHolder.imgBroom.setVisibility(View.INVISIBLE);

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
                    viewHolder.imgWindow.setImageDrawable(getResources().getDrawable(R.drawable.opened_r));
                else
                    viewHolder.imgWindow.setImageDrawable(getResources().getDrawable(R.drawable.closed_r));
            }

            if (item.isDoor())
                viewHolder.imgDoor.setImageDrawable(getResources().getDrawable(R.drawable.opened_l));
            else
                viewHolder.imgDoor.setImageDrawable(getResources().getDrawable(R.drawable.closed_l));

            if (item.isTodayClearence()) {
                viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.quit_t));
                row.setBackgroundResource(R.drawable.list_selector_quit);
            } else if (item.isRoomOccupied()) {
                viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.occupied_t));
                row.setBackgroundResource(R.drawable.list_selector_occupied);
            } else {
                viewHolder.textRoomNum.setTextColor(getResources().getColor(R.color.empty));
                row.setBackgroundResource(R.drawable.list_selector);

            }

            return row;
        }

    }

    private void showRoomsOdd(int idFloor) {
        SQLiteDatabase db = dbSch.getWritableDatabase();
        Cursor c = null;
        String query = "SELECT * FROM " + HotelRoomTable.TABLE_NAME +
                " WHERE " + HotelRoomTable.ID_HOTEL + "=" + mIdHotel +
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
                }
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            mDBArrayAdapter1.notifyDataSetChanged();
            showCounter(mDBArrayAdapter1.getCount());
        }
    }

    private void showRoomsEven(int idFloor) {
        SQLiteDatabase db = dbSch.getWritableDatabase();
        Cursor c = null;
        String query = "SELECT * FROM " + HotelRoomTable.TABLE_NAME +
                " WHERE " + HotelRoomTable.ID_HOTEL + "=" + mIdHotel +
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
                }
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            mDBArrayAdapter2.notifyDataSetChanged();
            showCounter(mDBArrayAdapter2.getCount());
        }
    }

    private void showCounter(int cntr) {
//            caption.setText(String.format(getString(R.string.m_cntr), cntr));
    }

    public class MyTabListener implements ActionBar.TabListener {
        public	MyTabListener() {
        }
        //	callbacks

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mSelectedFloor = Integer.parseInt(tab.getText().toString().substring(0, 1));
            showRoomsEven(mSelectedFloor);
            showRoomsOdd(mSelectedFloor);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }
    }
    private void refreshRooms() {
        if (mSelectedFloor > 0) {
            showRoomsEven(mSelectedFloor);
            showRoomsOdd(mSelectedFloor);
        }
    }

}
