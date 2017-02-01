package com.utis.chambermaid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.utis.chambermaid.records.HotelRoomRecord;
import com.utis.chambermaid.records.LogsRecord;
import com.utis.chambermaid.tables.HotelRoomTable;

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


public class RoomsListActivity extends Activity {
    private static final boolean D = true;
    private static final String TAG = "RoomsListActivity";
    private static final String METHOD = "GetStatusNom";
    private static final String SOAP_ACTION = CommonClass.SOAP_ACTION_PREFIX + METHOD;
    private DBSchemaHelper dbSch;
    private Context mContext;
    private ListView mDBListView;
    private CustomFilteredHotelRoomAdapter mDBArrayAdapter;
    private TextView caption;
    private EditText textFilter;
    private HotelRoomRecord roomRecord;
    private ArrayList<HotelRoomRecord> roomArrayList;

    private class CustomFilteredHotelRoomAdapter extends ArrayAdapter<HotelRoomRecord> implements Filterable {
        private int id;
        ArrayList<HotelRoomRecord> gdsArray;
        ArrayList<HotelRoomRecord> roomsFiltered;
        GoodsFilter cardFilter;
        String preConstraint = "";

        public CustomFilteredHotelRoomAdapter(Context context, int resource, ArrayList<HotelRoomRecord> litem) {
            super(context, resource, litem);
            gdsArray = litem;
            roomsFiltered = litem;
        }

        public HotelRoomRecord getItem(int position) {
            return roomsFiltered.get(position);
        }

        public long getItemId(int position) {
            return position;
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

        @Override
        public Filter getFilter() {
            if (cardFilter == null) {
                cardFilter = new GoodsFilter();
            }
            return cardFilter;
        }

        @Override
        public int getCount () {
            return roomsFiltered.size();
        }

        private class GoodsFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                ArrayList<HotelRoomRecord> list;

                if (constraint == null || constraint.length() == 0) {
                    results.values = gdsArray;
                    results.count = gdsArray.size();
                } else {
                    if (preConstraint.length() == 0 || preConstraint.length() > constraint.length())
                        list = gdsArray;
                    else
                        list = roomsFiltered;
                    preConstraint = constraint.toString();
                    List<HotelRoomRecord> roomsList = new ArrayList<HotelRoomRecord>();
                    for (HotelRoomRecord ent : list) {
                        if (String.valueOf(ent.getRoomNum()).contains(filterString))
                            roomsList.add(ent);
                    }
                    results.values = roomsList;
                    results.count = roomsList.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                {
                    roomsFiltered = (ArrayList<HotelRoomRecord>) results.values;
                    notifyDataSetChanged();
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms_list);
        mContext = this;
        caption = (TextView) findViewById(R.id.titleText);
        dbSch = DBSchemaHelper.getInstance(this);

        roomArrayList = new ArrayList<HotelRoomRecord>();

        mDBListView = (ListView) findViewById(R.id.listViewRooms);

        mDBListView.setOnItemClickListener(mGoodsClickListener);
        textFilter = (EditText) findViewById(R.id.editTextFilter);
        textFilter.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                RoomsListActivity.this.mDBArrayAdapter.getFilter().filter(cs, new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        showCounter(count);
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

            @Override
            public void afterTextChanged(Editable arg0) {}
        });
        showRooms();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rooms_list, menu);
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

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        private String resp = "";

        @Override
        protected String doInBackground(String... params) {
            publishProgress(mContext.getString(R.string.title_init_load)); // Calls onProgressUpdate()
            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
                SoapObject request = new SoapObject(CommonClass.NAMESPACE, METHOD);
                envelope.bodyOut = request;
                envelope.setAddAdornments(false);
                envelope.implicitTypes = true;

                dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "Request: " + request.toString());

                HttpTransportSE transport = new HttpTransportSE(CommonClass.getServerURI(mContext));
                transport.debug = true;
                int cntr = 0; boolean responseOK = false;
                while (cntr < CommonClass.FAIL_REPETITIONS && !responseOK) {
                    try {
                        cntr++;
                        String auth = CommonClass.LOGIN + ":" + CommonClass.PSW;
                        List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
                        headerList.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode(auth.getBytes())));

                        SoapObject response = null;
                        transport.call(SOAP_ACTION, envelope, headerList);
                        transport.reset();
                        if (envelope.bodyIn != null) {
                            responseOK = true;
                            if (envelope.bodyIn instanceof SoapObject) {
                                publishProgress(mContext.getString(R.string.title_parse)); // Calls onProgressUpdate()
                                response = (SoapObject) envelope.bodyIn;

                                parseRoomStateResponse(response);                                   // ----- ##### -----

                                long cntrRoom = dbSch.getHotelRoomCount();
                                resp = getString(R.string.title_gds_count) + cntrRoom;
                            } else if (envelope.bodyIn instanceof SoapFault12) {
                                resp = ((SoapFault12) envelope.bodyIn).getMessage().toString();
                                String msg = TAG + " " + METHOD + "; " + resp;
                                dbSch.addLogItem(LogsRecord.ERROR, new Date(), msg);
                            }
                        } else {
                            if (transport.responseDump != null) {
                                resp = getString(R.string.m_post_err) +
                                        ": " + transport.responseDump.toString();
                            } else {
                                resp = getString(R.string.m_post_err) + ": responseDump is NULL";
                            }
                            dbSch.addLogItem(LogsRecord.ERROR, new Date(), resp);
                        }
                    } catch (IOException e) {
                        resp = "IOException: " + e.getMessage();
                        Log.e(TAG, resp);
                        dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), resp);
                    } catch (XmlPullParserException e) {
                        resp = "XmlParseException: " + e.getMessage();
                        Log.e(TAG, resp);
                        dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), resp);
                    }
                }
            } catch (Exception e) {
                resp = "Exception: " + e.getMessage();
                Log.e(TAG, resp);
                dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), resp);
            }
            return resp;
        }




        private void parseRoomStateResponse(SoapObject response) {                                  // ----- ##### -----
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
                String tipeClearence = bar.getProperty(HotelRoomRecord.TIPE_CLEARENCE).toString();
                String repair = bar.getProperty(HotelRoomRecord.REPAIR).toString();
                String noteRepair = bar.getProperty(HotelRoomRecord.NOTE_REPAIR).toString();
                String changeBedExtra = bar.getProperty(HotelRoomRecord.CHANGE_BED_EXTRA).toString();
                String notDesturb = bar.getProperty(HotelRoomRecord.NOT_DISTURB).toString();
                String callChambermaid = bar.getProperty(HotelRoomRecord.CALL_CHAMBERMAID).toString();
                String inRoom = bar.getProperty(HotelRoomRecord.CHAMBERMAID_IN_ROOM).toString();
                HotelRoomRecord hotelRoomRecord = new HotelRoomRecord(idHotel, hotel, floor,
                        room, occup, guest, service, clear, door, window, balcony,
                        changeBedDate, departureDate, twin, water, reservDate, check
                        ,statNom, repair, noteRepair, changeBedExtra
                        , tipeClearence, notDesturb, callChambermaid, inRoom

                );

                dbSch.addHotelRoomItem(hotelRoomRecord);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            caption.setText(result);
            showRooms();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(String... text) {
            caption.setText(text[0]);
        }

    }

    // The on-click listener for ListViews
    private AdapterView.OnItemClickListener mGoodsClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            roomRecord = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "num=" + roomRecord.getRoomNum());

    		Intent intent = new Intent(mContext, RoomBarGoodsActivity.class);
            Bundle b = new Bundle();
            b.putString("num", ""+ roomRecord.getRoomNum());
            b.putString("hotel", ""+ roomRecord.getHotelId());
            intent.putExtras(b);
            startActivity(intent);
        }
    };

    private void showRooms() {
        SQLiteDatabase db = dbSch.getWritableDatabase();
        Cursor c = null;
        String query;

        query = "SELECT * FROM " + HotelRoomTable.TABLE_NAME + " ORDER BY " +
                HotelRoomTable.ID_HOTEL + " ASC, " + HotelRoomTable.ROOM_NUM + " ASC";
        try {
            mDBListView.setAdapter(null);
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
            mDBArrayAdapter = new CustomFilteredHotelRoomAdapter(this, R.layout.db_data, roomArrayList);
            mDBListView.setAdapter(mDBArrayAdapter);
            showCounter(mDBArrayAdapter.getCount());
            String filter = textFilter.getText().toString();
            if (filter.length() >= 0) {
                RoomsListActivity.this.mDBArrayAdapter.getFilter().filter(filter, new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        showCounter(count);
                    }
                });
            }

        }
    }

    private void showCounter(int cntr) {
        caption.setText(String.format(getString(R.string.m_cntr), cntr));
    }

}
