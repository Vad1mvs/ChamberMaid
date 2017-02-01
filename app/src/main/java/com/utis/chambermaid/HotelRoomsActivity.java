package com.utis.chambermaid;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.utis.chambermaid.records.HotelRoomRecord;
import com.utis.chambermaid.records.PlacementRecord;
import com.utis.chambermaid.tables.HotelRoomTable;
import com.utis.chambermaid.tables.PlacementTable;

import java.util.ArrayList;
//import com.utis.chambermaid.R;


public class HotelRoomsActivity extends  Activity {
    private static final boolean D = true;
    private static final String TAG = "HotelRoomsActivity";
    private DBSchemaHelper dbSch;
    private Context mContext;
    private int mIdHotel, mIdEmp, mFloorCnt;
    private String mHotel;
    ActionBar.Tab[] tabArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_room);

        mContext = this;
        dbSch = DBSchemaHelper.getInstance(this);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            if (b.containsKey("idEmp"))
                mIdEmp = Integer.parseInt(b.getString("idEmp"));
            if (b.containsKey("idHotel"))
                mIdHotel = Integer.parseInt(b.getString("idHotel"));
            if (b.containsKey("Hotel"))
                mHotel = b.getString("Hotel");
            Log.d(TAG, mIdEmp+" / "+ mIdHotel +" / "+ mHotel );
        }

        mFloorCnt = dbSch.getPlacementFloorCount(mIdHotel);
        tabArray = new ActionBar.Tab[mFloorCnt];
        workwithTabbedActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hotel_room, menu);
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
        //---Placement person use PlacementTable----
    private void initTabs(ActionBar actionBar) {
        String query = "SELECT * FROM " + PlacementTable.TABLE_NAME + " WHERE " +
                PlacementTable.ID_ENT + "=" + mIdHotel + " AND " +
                PlacementTable.TTYPE + "=" + PlacementRecord.PLACE_FLOOR;
        Cursor c = null;
        int colid, id, cntr = 0;
        String name;
        Fragment fragmentTab;
        SQLiteDatabase sqdb = dbSch.getWritableDatabase();
        try {
            c = sqdb.rawQuery(query, null);
            while (c.moveToNext()) {
                colid = c.getColumnIndex(PlacementTable.NAME);
                name = c.getString(colid);
                colid = c.getColumnIndex(PlacementTable.ID_EXTERNAL);
                id = c.getInt(colid);
                fragmentTab = FragmentTab.newInstance(cntr, name, id, mIdHotel);// new FragmentTab(name, id);
                tabArray[cntr] = actionBar.newTab().setText(name);
                tabArray[cntr].setTabListener(new MyTabListener(fragmentTab));
                actionBar.addTab(tabArray[cntr]);
                cntr++;
            }
        } catch(Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
    }

    public void workwithTabbedActionBar() {
        ActionBar actionBar = this.getActionBar();
        actionBar.setTitle(mHotel);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        initTabs(actionBar);
    }



    public static class FragmentTab extends Fragment {
        int mNum;
        String fName;
        int idFloor, idHotel;
        private ArrayList<HotelRoomRecord> roomArrayList;
        private ListView mDBListView;
        private DBSchemaHelper dbSchF;
        private HotelRoomRecord roomRecord;
        private CustomHotelRoomAdapter mDBArrayAdapter;
        private ArrayAdapter<HotelRoomRecord> mArrayAdapter;


        public class CustomHotelRoomAdapter extends ArrayAdapter<HotelRoomRecord> {
            private int id;

            public CustomHotelRoomAdapter(Context context, int resource, ArrayList<HotelRoomRecord> litem) {
                super(context, resource, litem);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View mView =  super.getView(position, convertView, parent);
                TextView tv = (TextView) mView.findViewById(android.R.id.text1);
                if (tv != null) {
                    HotelRoomRecord item = getItem(position);
                }
                return mView;
            }

        }

        /**
         * Create a new instance of CountingFragment, providing "num" as an argument.
         */
        static FragmentTab newInstance(int num, String name, int idFloor, int idHotel) {
            FragmentTab f = new FragmentTab();
            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            args.putString("name", name);
            args.putInt("floor", idFloor);
            args.putInt("hotel", idHotel);
            f.setArguments(args);
            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mNum = getArguments() != null ? getArguments().getInt("num") : 0;
            fName = getArguments() != null ? getArguments().getString("name") : "";
            idFloor = getArguments() != null ? getArguments().getInt("floor") : 0;
            idHotel = getArguments() != null ? getArguments().getInt("hotel") : 0;
            roomArrayList = new ArrayList<HotelRoomRecord>();
            dbSchF = DBSchemaHelper.getInstance(getActivity());

        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState){
            View view = inflater.inflate(R.layout.floor_rooms_tab, container, false);
            TextView textview = (TextView) view.findViewById(R.id.tabTitleText);
            textview.setText(fName);
            mDBListView = (ListView) view.findViewById(R.id.tabListViewRooms);

            mDBArrayAdapter = new CustomHotelRoomAdapter(getActivity(), R.layout.db_data, roomArrayList);
//            mArrayAdapter = new ArrayAdapter<HotelRoomRecord>(getActivity(),
//                    R.layout.db_data, roomArrayList);
            mDBListView.setAdapter(mDBArrayAdapter);
            showRooms();
            return view;
        }

        private void showRooms() {
            SQLiteDatabase db = dbSchF.getWritableDatabase();
            Cursor c = null;
            String query;

            query = "SELECT * FROM " + HotelRoomTable.TABLE_NAME +
                    " WHERE " + HotelRoomTable.ID_HOTEL + "=" + idHotel +
                    " AND " + HotelRoomTable.ROOM_FLOOR + "=" +idFloor +
                    " ORDER BY " + HotelRoomTable.ROOM_NUM + " ASC";
            try {
                c = db.rawQuery(query, null);
                roomArrayList.clear();
                while (c.moveToNext()) {
                    roomRecord = new HotelRoomRecord(c);
                    if (roomRecord.getRoomNum() > 0) {
                        roomArrayList.add(roomRecord);
                    }
                }
            } catch (Exception e) {
                if (D) Log.e(TAG, "Exception: " + e.getMessage());
            } finally {
                if (c != null) c.close();
                mDBArrayAdapter.notifyDataSetChanged();
                showCounter(mDBArrayAdapter.getCount());
            }
        }

        private void showCounter(int cntr) {
//            caption.setText(String.format(getString(R.string.m_cntr), cntr));
        }

    }

    public class MyTabListener implements ActionBar.TabListener {
        Fragment fragment;

        //	constructor	code
        public	MyTabListener(Fragment fragment) {
            this.fragment = fragment;
        }
        //	callbacks

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.replace(R.id.fragment_container, fragment);
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            ft.remove(fragment);
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

        }
    }
}
