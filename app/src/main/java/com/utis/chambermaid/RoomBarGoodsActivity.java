package com.utis.chambermaid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.chambermaid.notnow.YesNoDialogFragment;
import com.utis.chambermaid.records.LogsRecord;
import com.utis.chambermaid.records.RoomBarGoodsRecord;
import com.utis.chambermaid.tables.RoomBarGoodsTable;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault12;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class RoomBarGoodsActivity extends FragmentActivity implements YesNoDialogFragment.YesNoDialogListener {
    private static final boolean D = true;
    private static final String TAG = "RoomBarGoodsActivity";
    private static final int STATE_POST_SALES = 1;
    private static final int STATE_POST_STORE = 2;
    private static final int STATE_GET_ROOM_GOODS = 3;
    private static final int STATE_GET_MAID_GOODS = 4;
    private static final int STATE_FINISH = 0;
    private static final String METHOD_POST_DOC = "SendDoc";
    private static final String METHOD_GET_ROOM_GOODS = "Getost";
    private static final String METHOD_GET_MAID_GOODS = "GetosPerem";
    private DBSchemaHelper dbSch;
    private Context mContext;
    private int roomNum = 101;
    private int mIdEmp, mIdHotel = 1;//"000000001";
    private static int mMode;
    private String mIdHotelStr, mIdEmpStr;
    private ListView mDBListView;
    private CustomFilteredRoomBarGoodsAdapter mDBArrayAdapter;
    private TextView caption;
    private EditText textFilter;
    private RoomBarGoodsRecord gdsRecord;
    private ArrayList<RoomBarGoodsRecord> gdsArrayList;
    private boolean mDebugMode, isPostOK;
    private int mSOAPState = STATE_FINISH;
    private String SOAPAction, SOAPMethod;
    int cntQuat, sumQuant;
    //===YES/No Dialog===
    private void showYesNoDialog(String dialogTitle, String dialogText) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        YesNoDialogFragment yesnoDialog = new YesNoDialogFragment();
        yesnoDialog.setCancelable(false);
        yesnoDialog.setDialogTitle(dialogTitle);
        yesnoDialog.setDialogText(dialogText);
        yesnoDialog.show(fragmentManager, "yes/no dialog");
    }

    private void showErrMessage(String dialogTitle, String dialogText) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        YesNoDialogFragment yesnoDialog = new YesNoDialogFragment();
        yesnoDialog.setCancelable(false);
        yesnoDialog.setDialogTitle(dialogTitle);
        yesnoDialog.setDialogText(dialogText);
        yesnoDialog.show(fragmentManager, "yes/no dialog");
    }

    @Override
    public void onFinishYesNoDialog(boolean state) {
    }

    private void showErrorMsg(String dialogTitle, String dialogText) {
        new AlertDialog.Builder(RoomBarGoodsActivity.this)
                .setTitle(dialogTitle)
                .setMessage(dialogText)
                .setCancelable(false)
                .setIcon(R.drawable.ic_action_error)
                .setPositiveButton(getString(R.string.btn_done), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // whatever...
                    }
                }).create().show();
    }



    //-----------------------------------------------------

    private static class CustomFilteredRoomBarGoodsAdapter extends ArrayAdapter<RoomBarGoodsRecord> implements Filterable {
        private int id;
        ArrayList<RoomBarGoodsRecord> gdsArray;
        ArrayList<RoomBarGoodsRecord> gdsFiltered;
        GoodsFilter cardFilter;
        String preConstraint = "";

        static class GoodsViewHolder {
            public TextView textNo;
            public TextView textGoodsName;
            public TextView textStoreQuantity;
            public TextView textQuantity;
            public TextView textSoldQuantity;
        }

        public CustomFilteredRoomBarGoodsAdapter(Context context, int resource, ArrayList<RoomBarGoodsRecord> litem) {
            super(context, resource, litem);
            gdsArray = litem;
            gdsFiltered = litem;
        }

        public RoomBarGoodsRecord getItem(int position) {
            return gdsFiltered.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GoodsViewHolder viewHolder;
            View row = convertView;
            int ert = 0;
            //Inflate a new row if one isn't recycled
            if(row == null) {
                row = LayoutInflater.from(getContext()).inflate(R.layout.gds_row, parent, false);
                viewHolder = new GoodsViewHolder();
                viewHolder.textNo = (TextView)row.findViewById(R.id.gdsNo);
                viewHolder.textGoodsName = (TextView)row.findViewById(R.id.gdsName);
                viewHolder.textStoreQuantity = (TextView)row.findViewById(R.id.gdsStoreQuantity);
                viewHolder.textQuantity = (TextView)row.findViewById(R.id.gdsQuantity);
                viewHolder.textSoldQuantity = (TextView)row.findViewById(R.id.gdsSoldQuantity);

                row.setTag(viewHolder);
            } else {
                viewHolder = (GoodsViewHolder) row.getTag();
            }
            RoomBarGoodsRecord item = getItem(position);
            viewHolder.textNo.setText(String.format("%2d", item.getGdsNo()));
            viewHolder.textGoodsName.setText("" + item.getGdsName());

            if (RoomBarGoodsRecord.DOC_SALE_TYPE == mMode) {
                viewHolder.textStoreQuantity.setVisibility(View.GONE);

                viewHolder.textQuantity.setText(String.format("%2d", item.getGdsLeftQuantity()));

            } else {
                viewHolder.textStoreQuantity.setVisibility(View.VISIBLE);
                viewHolder.textStoreQuantity.setText(String.format("%2d", item.getGdsLeftQuantity()));

                viewHolder.textQuantity.setText(String.format("%2d", item.getGdsQuantity()));
                //viewHolder.textQuantity.setVisibility(View.GONE);

                if (item.getGdsQuantity() == 0 && !item.isTotal())
                    viewHolder.textQuantity.setTextColor(Color.RED);
                else
                    viewHolder.textQuantity.setTextColor(Color.BLACK);
            }
            viewHolder.textSoldQuantity.setText(String.format("%2d", item.getGdsSoldQuantity()));

            if (item.isTotal()) {
                viewHolder.textNo.setText("");
            }

            if (item.getGdsLeftQuantity() <= 0 && !item.isTotal()) {
                viewHolder.textGoodsName.setTextColor(Color.RED);
                if (RoomBarGoodsRecord.DOC_SALE_TYPE == mMode)
                    viewHolder.textQuantity.setTextColor(Color.RED);
                else
                    viewHolder.textStoreQuantity.setTextColor(Color.RED);
                viewHolder.textNo.setTextColor(Color.RED);
            } else {
                viewHolder.textGoodsName.setTextColor(Color.BLACK);
                if (RoomBarGoodsRecord.DOC_SALE_TYPE == mMode)
                    viewHolder.textQuantity.setTextColor(Color.BLACK);
                else
                    viewHolder.textStoreQuantity.setTextColor(Color.BLACK);
                viewHolder.textNo.setTextColor(Color.BLACK);
            }
            if (item.getGdsSoldQuantity() > 0 && !item.isTotal()) {
                row.setBackgroundResource(R.drawable.list_selector_done);
            } else {
                row.setBackgroundResource(R.drawable.list_selector);
            }

            return row;
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
            return gdsFiltered.size();
        }

        private class GoodsFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                ArrayList<RoomBarGoodsRecord> list;

                if (constraint == null || constraint.length() == 0) {
                    results.values = gdsArray;
                    results.count = gdsArray.size();
                } else {
                    if (preConstraint.length() == 0 || preConstraint.length() > constraint.length())
                        list = gdsArray;
                    else
                        list = gdsFiltered;
                    preConstraint = constraint.toString();
                    List<RoomBarGoodsRecord> nEntList = new ArrayList<RoomBarGoodsRecord>();
                    for (RoomBarGoodsRecord ent : list) {
                        if (ent.getGdsName().toLowerCase().contains(filterString))
                            nEntList.add(ent);
                    }
                    results.values = nEntList;
                    results.count = nEntList.size();
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                {
                    gdsFiltered = (ArrayList<RoomBarGoodsRecord>) results.values;
//	                showCounter(results.count);
                    notifyDataSetChanged();
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_bar_goods);
        mContext = this;
        caption = (TextView) findViewById(R.id.titleText);
        dbSch = DBSchemaHelper.getInstance(this);
        gdsArrayList = new ArrayList<RoomBarGoodsRecord>();

        mDBListView = (ListView) findViewById(R.id.listViewGds);
        mDBListView.setOnItemClickListener(mGoodsClickListener);
        mDBListView.setOnItemLongClickListener(mGoodsLongClickListener);
        textFilter = (EditText) findViewById(R.id.editTextFilter);
        textFilter.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                RoomBarGoodsActivity.this.mDBArrayAdapter.getFilter().filter(cs, new Filter.FilterListener() {
                    public void onFilterComplete(int count) {
                        showCounter();
                    }
                });
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

            @Override
            public void afterTextChanged(Editable arg0) {}
        });

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
            if (b.containsKey("Room"))
                roomNum = Integer.parseInt(b.getString("Room"));
            if (b.containsKey("mode"))
                mMode = Integer.parseInt(b.getString("mode"));
            else
                mMode = RoomBarGoodsRecord.DOC_SALE_TYPE;
        }

        mDebugMode = CommonClass.getDebugMode(mContext);
        if (!mDebugMode) {
            if (mMode == RoomBarGoodsRecord.DOC_SALE_TYPE)
                sendSOAPRequest(STATE_GET_ROOM_GOODS);
            else
                sendSOAPRequest(STATE_GET_MAID_GOODS);
        } else {
            showGoods();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room_bar_goods, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean sold = dbSch.getRoomBarGoodsSoldCount() > 0;
        MenuItem mi = menu.findItem(R.id.action_post_gds);
        if (mi != null) {
            mi.setEnabled(sold);
            mi.setVisible(sold);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_post_gds:
                if (RoomBarGoodsRecord.DOC_SALE_TYPE == mMode)
                    sendSoldGoods();
                else
                    sendStoreGoods();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void sendSoldGoods() {
        if (mDebugMode) {
            for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
                RoomBarGoodsRecord rec = mDBArrayAdapter.getItem(i);
                if (rec.getGdsSoldQuantity() > 0)
                    mDBArrayAdapter.remove(rec);
            }
            mDBArrayAdapter.notifyDataSetChanged();
            showCounter();
        } else {
            sendSOAPRequest(STATE_POST_SALES);
        }
    }

    private void sendStoreGoods() {
        if (mDebugMode) {
//            for (int i = mDBArrayAdapter.getCount()-1; i >= 0; i--) {
//                RoomBarGoodsRecord rec = mDBArrayAdapter.getItem(i);
//                if (rec.getGdsSoldQuantity() > 0)
//                    mDBArrayAdapter.remove(rec);
//            }
//            mDBArrayAdapter.notifyDataSetChanged();
//            showCounter(mDBArrayAdapter.getCount());
        } else {
            sendSOAPRequest(STATE_POST_STORE);
        }
    }

    private void sendSOAPRequest(int state) {
        try {
            mSOAPState = state;
            switch (mSOAPState) {
                case STATE_GET_ROOM_GOODS:
                    SOAPMethod = METHOD_GET_ROOM_GOODS;
                    dbSch.emptyTable(RoomBarGoodsTable.TABLE_NAME);
                    SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                    break;
                case STATE_GET_MAID_GOODS:
                    SOAPMethod = METHOD_GET_MAID_GOODS;
                    dbSch.emptyTable(RoomBarGoodsTable.TABLE_NAME);
                    SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                    break;
                case STATE_POST_SALES:
                case STATE_POST_STORE:
                    SOAPMethod = METHOD_POST_DOC;
                    SOAPAction = CommonClass.SOAP_ACTION_PREFIX + SOAPMethod;
                    break;
                default:
                    SOAPMethod = "";
                    SOAPAction = "";
            }
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
        SoapObject list;

        @Override
        protected String doInBackground(String... params) {
            publishProgress(mContext.getString(R.string.title_init_load)); // Calls onProgressUpdate()
            try {
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
                SoapObject request = new SoapObject(CommonClass.NAMESPACE, SOAPMethod);
                switch (mSOAPState) {
                    case STATE_GET_ROOM_GOODS:
                    case STATE_GET_MAID_GOODS:
                        request.addProperty(RoomBarGoodsRecord.ROOM_PARAM, roomNum);
                        request.addProperty(RoomBarGoodsRecord.HOTEL_PARAM, mIdHotelStr);
                        break;
                    case STATE_POST_SALES:
                        request.addProperty(RoomBarGoodsRecord.ROOM_POST_PARAM, roomNum);
                        request.addProperty(RoomBarGoodsRecord.HOTEL_PARAM, mIdHotelStr);
                        request.addProperty(RoomBarGoodsRecord.TYPE_PARAM, RoomBarGoodsRecord.DOC_SALE_TYPE);
                        request.addProperty(RoomBarGoodsRecord.EMP_PARAM, mIdEmpStr);
                        list = new SoapObject(CommonClass.NAMESPACE, "spisdoc");

                        for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
                            RoomBarGoodsRecord rec = mDBArrayAdapter.getItem(i);
                            if (rec.getGdsSoldQuantity() > 0 && !rec.isTotal()) {
                                SoapObject gds = new SoapObject(CommonClass.NAMESPACE, "anyType");
                                PropertyInfo propInfo = new PropertyInfo();
                                propInfo.name = RoomBarGoodsRecord.GDS_ID;
                                propInfo.type = PropertyInfo.STRING_CLASS;
                                propInfo.setValue(rec.getGdsIdStr());
                                propInfo.setNamespace(CommonClass.NAMESPACE);
                                gds.addProperty(propInfo);
//                                gds.addProperty(RoomBarGoodsRecord.GDS_ID, rec.getGdsId());

                                propInfo = new PropertyInfo();
                                propInfo.name = RoomBarGoodsRecord.GDS_QUANTITY;
                                propInfo.type = PropertyInfo.INTEGER_CLASS;
                                propInfo.setValue(rec.getGdsSoldQuantity());
                                propInfo.setNamespace(CommonClass.NAMESPACE);
                                gds.addProperty(propInfo);
//                                gds.addProperty(RoomBarGoodsRecord.GDS_QUANTITY, rec.getGdsSoldQuantity());

                                list.addProperty(RoomBarGoodsRecord.GDS_DOC_PARAM, gds);
                            }
                        }
                        request.addProperty(RoomBarGoodsRecord.GDS_LIST_PARAM, list);
                        break;
                    case STATE_POST_STORE:
                        request.addProperty(RoomBarGoodsRecord.ROOM_POST_PARAM, roomNum);
                        request.addProperty(RoomBarGoodsRecord.HOTEL_PARAM, mIdHotelStr);
                        request.addProperty(RoomBarGoodsRecord.TYPE_PARAM, RoomBarGoodsRecord.DOC_STORE_TYPE);
                        request.addProperty(RoomBarGoodsRecord.EMP_PARAM, mIdEmpStr);
                        list = new SoapObject(CommonClass.NAMESPACE, "spisdoc");
                        for (int i = 0; i < mDBArrayAdapter.getCount(); i++) {
                            RoomBarGoodsRecord rec = mDBArrayAdapter.getItem(i);
                            if (rec.getGdsSoldQuantity() > 0 && !rec.isTotal()) {
                                SoapObject gds = new SoapObject(CommonClass.NAMESPACE, "anyType");
                                PropertyInfo propInfo = new PropertyInfo();
                                propInfo.name = RoomBarGoodsRecord.GDS_ID;
                                propInfo.type = PropertyInfo.STRING_CLASS;
                                propInfo.setValue(rec.getGdsIdStr());
                                propInfo.setNamespace(CommonClass.NAMESPACE);
                                gds.addProperty(propInfo);
//                                gds.addProperty(RoomBarGoodsRecord.GDS_ID, rec.getGdsId());

                                propInfo = new PropertyInfo();
                                propInfo.name = RoomBarGoodsRecord.GDS_QUANTITY;
                                propInfo.type = PropertyInfo.INTEGER_CLASS;
                                propInfo.setValue(rec.getGdsSoldQuantity());
                                propInfo.setNamespace(CommonClass.NAMESPACE);
                                gds.addProperty(propInfo);
//                                gds.addProperty(RoomBarGoodsRecord.GDS_QUANTITY, rec.getGdsSoldQuantity());

                                list.addProperty(RoomBarGoodsRecord.GDS_DOC_PARAM, gds);
                            }
                        }
                        request.addProperty(RoomBarGoodsRecord.GDS_LIST_PARAM, list);
                        break;
                }

                //bodyOut is the body object to be sent out with this envelope
                envelope.bodyOut = request;
//                boolean ad = envelope.isAddAdornments();
                envelope.setAddAdornments(false);
                envelope.implicitTypes = true;

                dbSch.addLogItem(LogsRecord.DEBUG, new Date(), "Request: " + request.toString());

//                envelope.dotNet = true;
                HttpTransportSE transport = new HttpTransportSE(CommonClass.getServerURI(mContext));
//                HttpTransportSE transport = new HttpTransportBasicAuth(URL, LOGIN, PSW);
                transport.debug = true;
//                System.setProperty("http.keepAlive", "false");  //???
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
                        transport.reset();  //???
                        //bodyIn is the body object received with this envelope
                        if (envelope.bodyIn != null) {
                            responseOK = true;
                            if (envelope.bodyIn instanceof SoapObject) {
                                publishProgress(mContext.getString(R.string.title_parse)); // Calls onProgressUpdate()
                                response = (SoapObject) envelope.bodyIn;
                                parseSOAPResponse(response);
                            } else if (envelope.bodyIn instanceof SoapFault12) {
                                resp = ((SoapFault12) envelope.bodyIn).getMessage().toString();
                                String msg = TAG + " " + mSOAPState + "; " + resp;
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
                        dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), resp);
                        Log.e(TAG, resp);
                        resp = "";
                    } catch (XmlPullParserException e) {
                        resp = "XmlParseException: " + e.getMessage();
                        dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), resp);
                        Log.e(TAG, resp);
                    }
                }
            } catch (Exception e) {
                resp = "Exception: " + e.getMessage();
                Log.e(TAG, resp);
                dbSch.addLogItem(LogsRecord.EXCEPTION, new Date(), resp);
            }
            return resp;
        }

        private void parseSOAPResponse(SoapObject response) {
            switch (mSOAPState) {
                case STATE_GET_ROOM_GOODS:
                    parseRoomBarGoodsResponse(response);
                    break;
                case STATE_GET_MAID_GOODS:
                    parseMaidBarGoodsResponse(response);
                    break;
                case STATE_POST_STORE:
                case STATE_POST_SALES:
                    parsePostQuitResponse(response);
                    break;
            }
        }

        private void parseMaidBarGoodsResponse(SoapObject response) {
            dbSch.emptyTable(RoomBarGoodsTable.TABLE_NAME);
            SoapObject t = (SoapObject) response.getProperty("return");
            for (int i = 0; i < t.getPropertyCount(); i++) {
                SoapObject bar = (SoapObject) t.getProperty(i);
                String name = bar.getProperty(RoomBarGoodsRecord.GDS_NAME).toString();
                String room = bar.getProperty(RoomBarGoodsRecord.ROOM_NUM).toString();
                String id = bar.getProperty(RoomBarGoodsRecord.GDS_ID).toString();
                String quantityRoom = bar.getProperty(RoomBarGoodsRecord.GDS_QUANTITY_ROOM).toString();
                String quantityStore = bar.getProperty(RoomBarGoodsRecord.GDS_QUANTITY_STORE).toString();
                RoomBarGoodsRecord roomBarGds = new RoomBarGoodsRecord(room, name, id, quantityStore, quantityRoom);
                dbSch.addRoomBarGoodsItem(roomBarGds);
                Log.d(TAG, String.format("%s; %s: %s - %s/%s", room, id, name, quantityStore, quantityRoom));
            }
            // add total

        }

        private void parseRoomBarGoodsResponse(SoapObject response) {
            dbSch.emptyTable(RoomBarGoodsTable.TABLE_NAME);
            String roomNum = "";
            SoapObject t = (SoapObject) response.getProperty("return");
            for (int i = 0; i < t.getPropertyCount(); i++) {
                SoapObject bar = (SoapObject) t.getProperty(i);
                String name = bar.getProperty(RoomBarGoodsRecord.GDS_NAME).toString();
                String room = bar.getProperty(RoomBarGoodsRecord.ROOM_NUM).toString();
                roomNum = room;
                String id = bar.getProperty(RoomBarGoodsRecord.GDS_ID).toString();
                String quantity = bar.getProperty(RoomBarGoodsRecord.GDS_QUANTITY).toString();
                RoomBarGoodsRecord roomBarGds = new RoomBarGoodsRecord(room, name, id, quantity);
                dbSch.addRoomBarGoodsItem(roomBarGds);
                Log.d(TAG, String.format("%s; %s: %s - %s", room, id, name, quantity));
            }
        }

        private void parsePostQuitResponse(SoapObject response) {
            String result = response.getProperty("return").toString();
            try {
                isPostOK = Boolean.parseBoolean(result);
                dbSch.addLogItem(LogsRecord.DEBUG, new Date(), getString(R.string.m_post_ok) +
                        ": "+ result);
            } catch (Exception e) {
                isPostOK = false;
                dbSch.addLogItem(LogsRecord.ERROR, new Date(), getString(R.string.m_post_err) +
                        ": "+ result);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            String msg = "";
            switch (mSOAPState) {
                case STATE_GET_MAID_GOODS:
                case STATE_GET_ROOM_GOODS:
                    long cntr = dbSch.getRoomBarGoodsCount();
                    String mess = getString(R.string.title_gds_count) + cntr;
                    caption.setText(mess);
                    showGoods();
                    break;
                case STATE_POST_SALES:
                case STATE_POST_STORE:
                    if (isPostOK) {
                        msg = getString(R.string.m_post_ok);
                        closeActivity();
                    } else {
                        msg = getString(R.string.m_post_err);
                    }
                    break;
            }
            mSOAPState = STATE_FINISH;

            msg = result + msg;
            if (msg.length() >  0)
                if (isPostOK)
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                else
                    showErrorMsg(getString(R.string.m_error), msg);

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
            gdsRecord = mDBArrayAdapter.getItem(arg2);
            if (D) Log.d(TAG, "id=" + gdsRecord.getGdsIdStr());
            if (!gdsRecord.isTotal()) {
                if (true/*RoomBarGoodsRecord.DOC_SALE_TYPE == mMode*/) {
                    if (gdsRecord.getGdsLeftQuantity() > 0) {
                        int q = gdsRecord.getGdsSoldQuantity();
                        gdsRecord.setGdsSoldQuantity(++q);
                    }
                    dbSch.updateRoomBarGoodSoldQuantity(gdsRecord.getId(), gdsRecord.getGdsSoldQuantity());
                } else {

                }
                showCounter();
            }
        }
    };

    // The long on-click listener for ListViews
    private AdapterView.OnItemLongClickListener mGoodsLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            gdsRecord = mDBArrayAdapter.getItem(position);
            if (D) Log.d(TAG, "id=" + gdsRecord.getGdsIdStr());
            if (!gdsRecord.isTotal()) {
                if (true/*RoomBarGoodsRecord.DOC_SALE_TYPE == mMode*/) {
                    gdsRecord.setGdsSoldQuantity(0);
                    dbSch.updateRoomBarGoodSoldQuantity(gdsRecord.getId(), 0);
                } else {

                }
                showCounter();
            }
            return true;
        }
    };

//---------------------------------------------

    private void showGoods() {
        SQLiteDatabase db = dbSch.getWritableDatabase();
        Cursor c = null;
        String query = "SELECT * FROM " + RoomBarGoodsTable.TABLE_NAME +
                " ORDER BY " + RoomBarGoodsTable.GDS_NAME + " ASC";
        try {
            mDBListView.setAdapter(null);
            c = db.rawQuery(query, null);
            gdsArrayList.clear();
            int cntr = 0;
            int gdsCntr = 0; // 1 column
            cntQuat = 0; // 2 column
            sumQuant = 0;
            gdsRecord = null;
            while (c.moveToNext()) {
                gdsRecord = new RoomBarGoodsRecord(c);

                if (gdsRecord.getGdsName().length() > 0) {
                    gdsCntr += gdsRecord.getGdsQuantityMaid();
                        cntQuat += gdsRecord.getGdsQuantity();
                        gdsRecord.setGdsNo(++cntr);
                        gdsArrayList.add(gdsRecord);
                            if (gdsRecord.getGdsQuantity() == 1) {
                                sumQuant += gdsRecord.getGdsQuantity();
                            } else if (gdsRecord.getGdsQuantity() == 2) {
                                sumQuant += gdsRecord.getGdsQuantity()-1;
                            }else if (gdsRecord.getGdsQuantity() == 3){
                                sumQuant += gdsRecord.getGdsQuantity()-2;
                            }else if (gdsRecord.getGdsQuantity() == 4){
                                sumQuant += gdsRecord.getGdsQuantity()-3;
                            }else if (gdsRecord.getGdsQuantity() == 5){
                                sumQuant += gdsRecord.getGdsQuantity()-4;
                            }else if (gdsRecord.getGdsQuantity() == 6){
                                sumQuant += gdsRecord.getGdsQuantity()-5;
                            }

                }
            }

            //----------------- All----------------------
            if (gdsRecord != null) {
                RoomBarGoodsRecord roomBarGds = new RoomBarGoodsRecord(gdsRecord.getRoom(),
                       // mContext.getString(R.string.m_total), gdsRecord.getSaleMode(), gdsArrayList.size(), gdsCntr);

                        mContext.getString(R.string.m_total), gdsRecord.getSaleMode(), cntQuat, gdsCntr);
                gdsArrayList.add(roomBarGds);
            }


        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            mDBArrayAdapter = new CustomFilteredRoomBarGoodsAdapter(this, R.layout.gds_row, gdsArrayList);
            mDBListView.setAdapter(mDBArrayAdapter);
            showCounter();
        }
    }

    //----Counter  goods-------------
    private void showCounter() {
        int cntr = mDBArrayAdapter.getCount() - 1; // remove total
        int soldCntr = (int) dbSch.getRoomBarGoodsSoldCount();
        int leftCntr = cntr - soldCntr;
        int sumPosition = sumQuant;
        caption.setText(String.format(getString(R.string.m_room_cntr2), sumPosition, soldCntr, leftCntr));
        gdsRecord = mDBArrayAdapter.getItem(mDBArrayAdapter.getCount()-1);
        gdsRecord.setGdsSoldQuantity(soldCntr);
        mDBArrayAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    private void closeActivity() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        closeActivity();
    }
}
