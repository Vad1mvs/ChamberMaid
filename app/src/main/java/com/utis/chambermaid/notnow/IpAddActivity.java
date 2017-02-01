package com.utis.chambermaid.notnow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.utis.chambermaid.DBSchemaHelper;
import com.utis.chambermaid.R;
import com.utis.chambermaid.records.EntRecord;
import com.utis.chambermaid.records.IpRecord;
import com.utis.chambermaid.tables.EntTable;
import com.utis.chambermaid.tables.IpTables;

import java.util.ArrayList;

public class IpAddActivity extends Activity {
    private static final String TAG = "IpAddActivity";
    Context mContext;
    private DBSchemaHelper dbSch;
    Spinner mEntSpinner;
    private ArrayAdapter<EntRecord> mDBEntArrayAdapter;
    String hotel, idName, strId, hotelName;
    private EntRecord hotelSelected;
    EditText etIp;
    Button btnInsert, btnAdd, btnDelete;
    ListView list;
    IpTables db;
    TextView tvName, tvHotel, tvId;
    SimpleCursorAdapter adapter;

    boolean clickList;
    Cursor cursor;
    TextView tv;
    ArrayList<String> arr = new ArrayList<>();
    CheckBox checkBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_add);
        clickList = true;
        mContext = this;
        dbSch = DBSchemaHelper.getInstance(this);
        db = new IpTables(this);
        db.abrirBaseData();

        mEntSpinner = (Spinner) findViewById(R.id.entChooser);
        mEntSpinner.setOnItemSelectedListener(mEntSpinnerClickListener);
        mDBEntArrayAdapter = new ArrayAdapter<EntRecord>(this, android.R.layout.simple_spinner_item);
        mDBEntArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEntSpinner.setAdapter(mDBEntArrayAdapter);
        getHotels();
        getHotel();

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnInsert = (Button) findViewById(R.id.btnInsert);
        tv = (TextView)findViewById(R.id.tv);
        btnInsert.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        btnAdd.setVisibility(View.VISIBLE);
        etIp = (EditText) findViewById(R.id.etIp);
        list = (ListView) findViewById(R.id.list);
        arrayListAdapter();


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                btnInsert.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.GONE);
                btnAdd.setVisibility(View.GONE);
                tvName = (TextView) view.findViewById(R.id.tvName);
                tvHotel = (TextView) view.findViewById(R.id.tvHotel);
                tvId = (TextView) view.findViewById(R.id.tvId);

                hotel = tvHotel.getText().toString();
                idName = tvName.getText().toString();
                strId = tvId.getText().toString();
                etIp.setText(idName);


            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                    btnInsert.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.VISIBLE);
                    btnAdd.setVisibility(View.GONE);
                     etIp.setVisibility(View.GONE);

                    checkBox = (CheckBox) v.findViewById(R.id.checkBox);
                    tvName = (TextView) v.findViewById(R.id.tvName);
                    tvHotel = (TextView) v.findViewById(R.id.tvHotel);
                    tvId = (TextView) v.findViewById(R.id.tvId);

                    v.setBackgroundColor(Color.GRAY);
                    checkBox.setVisibility(View.VISIBLE);
                    checkBox.setChecked(true);
                    hotel = tvHotel.getText().toString();
                    idName = tvName.getText().toString();
                    strId = tvId.getText().toString();
                    btnDelete.setVisibility(View.VISIBLE);
                    arr.add(strId);
                    adapter.getCursor().requery();
                    adapter.notifyDataSetChanged();

                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private AdapterView.OnItemSelectedListener mEntSpinnerClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            hotelSelected = (EntRecord) mEntSpinner.getSelectedItem();
            hotelName = String.valueOf(hotelSelected);
            tv.setText(" IP гостиницы " + hotelName );
            Log.d(TAG, String.valueOf(hotelName.length()));
            etIp.setText("");
            SQLiteDatabase sqdb = db.getReadableDatabase();
            cursor = sqdb.rawQuery("SELECT * FROM " + IpTables.TABLE_NAME + " WHERE "
                    + IpTables.HOTEL+ " LIKE '" + hotelName + "'" , null);
            arrayListAdapter();


        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private void getHotels() {
        String query = "SELECT * from " + EntTable.TABLE_NAME
                + " ORDER BY " + EntTable.NM + " ASC";
        Cursor c = null;
        EntRecord entRecord;
        SQLiteDatabase sqdb = dbSch.getWritableDatabase();
        ArrayList<String> str = new ArrayList<>();
        try {
            c = sqdb.rawQuery(query, null);
            mDBEntArrayAdapter.clear();
            while (c.moveToNext()) {
                entRecord = new EntRecord(c);
                mDBEntArrayAdapter.add(entRecord);
                str.add(String.valueOf(entRecord));
                for (String d: str)
                Log.d(TAG, d);
            }
        } catch(Exception e) {
           Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            mDBEntArrayAdapter.notifyDataSetChanged();
        }
    }

    public void btnInsert(View view){
        String newName = etIp.getText().toString();
        long id = Long.parseLong(strId);
        db.actualData(id, newName, hotelName);
        adapter.getCursor().requery();
        adapter.notifyDataSetChanged();
        btnInsert.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);
        btnAdd.setVisibility(View.VISIBLE);
    }
    public void btnIpSave(View view){

        String name = etIp.getText().toString();
        if(hotelName!=null && name.length()>3){
        String hotel = hotelName;
        db.insertData(hotel, name);
        adapter.getCursor().requery();
        adapter.notifyDataSetChanged();
        etIp.setText("");
        } else if (hotelName == null){
            Toast.makeText(getApplicationContext(), "Гостиница не указана", Toast.LENGTH_SHORT).show();
        } else if (name == null){
            Toast.makeText(getApplicationContext(), "Ip не указана", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Данные указаны неверно", Toast.LENGTH_SHORT).show();
        }
    }

    public void btnDelete(View view){
        for(String d: arr){
            Log.d(TAG, " --> "+ d);
            long idDel = Long.parseLong(d);
            db.deleteData(idDel);
            intentActivity();
        }
    }

    public void arrayListAdapter(){
        String[] from = new String[] {IpTables._ID, IpTables.HOTEL, IpTables.IP_NAME};
        int[] to = new int[] {R.id.tvId, R.id.tvHotel, R.id.tvName};
        adapter = new SimpleCursorAdapter(IpAddActivity.this, R.layout.list_format, cursor, from, to);
        list.setAdapter(adapter);
    }
    public void intentActivity (){
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public String[] getContacts(){
        SQLiteDatabase sqdb = db.getReadableDatabase();
        cursor = sqdb.rawQuery("SELECT * FROM " + IpTables.TABLE_NAME + " WHERE "
                + IpTables.HOTEL+ " LIKE '" + hotelName + "'" , null);
        cursor.moveToFirst();
        ArrayList<String> names = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            names.add(cursor.getString(cursor.getColumnIndex("name")));
            cursor.moveToNext();
        }
        cursor.close();
        Log.d(TAG,"-->" + String.valueOf(names.toArray(new String[names.size()])));
        return names.toArray(new String[names.size()]);
    }
    private void getHotel() {
        String query = "SELECT * FROM " + IpTables.TABLE_NAME + " WHERE "
                + IpTables.HOTEL+ " LIKE 'Екатерина '";
        Cursor c = null;
        IpRecord entRecord;
        SQLiteDatabase sqdb = db.getWritableDatabase();
        ArrayList<String> str = new ArrayList<>();
        try {
            c = sqdb.rawQuery(query, null);

            while (c.moveToNext()) {
                entRecord = new IpRecord(c);
                str.add(String.valueOf(entRecord));
                for (String d: str)
                    Log.d(TAG,"**> " + d);

            }
        } catch(Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
    }


}



