package com.utis.chambermaid;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.utis.chambermaid.records.RoomBarGoodsRecord;
import com.utis.chambermaid.tables.RoomBarGoodsTable;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault12;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainRoomsActivity extends Activity {
    private static final boolean D = true;
    private static final String TAG = "MainRoomsActivity";
    private static final String LOGIN = "sait";
    private static final String PSW = "sait555";
//    public final static String URL = "http://192.168.9.250/gostproba/ws/GetnomObmen1_ws1.1cws?wsdl";//   /WebService/services/WebService?wsdl";
    public final static String URL = "http://192.168.9.250/gostproba/ws/GetnomObmen_ws1.1cws";//   /WebService/services/WebService?wsdl";
    public static final String NAMESPACE = "Otel";//http://sample";
    public static final String SOAP_ACTION_PREFIX = "/";
    private static final String METHOD = "Getost";
    public static final String SOAP_ACTION = "Otel#GetNomobmen:"+ METHOD;
    private TextView textView;
    private DBSchemaHelper dbSch;
    private Context mContext;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_rooms);
        mContext = this;
        textView = (TextView) findViewById(R.id.titleText);
        dbSch = DBSchemaHelper.getInstance(this);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_rooms, menu);
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
            publishProgress("Loading contents..."); // Calls onProgressUpdate()
            try {
                // SoapEnvelop.VER11 is SOAP Version 1.1 constant
                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
                SoapObject request = new SoapObject(NAMESPACE, METHOD);
                request.addProperty("номер", 203);

                //bodyOut is the body object to be sent out with this envelope
                envelope.bodyOut = request;
                boolean ad = envelope.isAddAdornments();
                envelope.setAddAdornments(false);
                envelope.implicitTypes = true;

//                envelope.dotNet = true;
                HttpTransportSE transport = new HttpTransportSE(URL);
//                HttpTransportSE transport = new HttpTransportBasicAuth(URL, LOGIN, PSW);
                transport.debug = true;
                try {
                    String auth = LOGIN + ":" + PSW;
                    List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
                    headerList.add(new HeaderProperty("Authorization", "Basic " + org.kobjects.base64.Base64.encode(auth.getBytes())));

                    SoapObject response = null;
                    transport.call(SOAP_ACTION/*NAMESPACE + SOAP_ACTION_PREFIX + METHOD*/, envelope, headerList);
                    //bodyIn is the body object received with this envelope
                    if (envelope.bodyIn != null) {
                        if (envelope.bodyIn instanceof SoapObject) {
                            response = (SoapObject) envelope.bodyIn;
                            parseResponse2(response);
//                            String responseDump = transport.responseDump;
//                            parseResponse(responseDump);
                            if (response != null)
                                resp = response.getInnerText();
                        } else if (envelope.bodyIn instanceof SoapFault12)
                            resp = ((SoapFault12) envelope.bodyIn).getMessage().toString();
                    }
                } catch (IOException e) {
                    resp = "Exception: " + e.getMessage();
                    Log.e(TAG, resp);
//                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    resp = "Exception Xml: " + e.getMessage();
                    Log.e(TAG, resp);
//                    e.printStackTrace();
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
//                e.printStackTrace();
                resp = e.getMessage();
            }
            return resp;
        }

        private void parseResponse2(SoapObject response) {
            dbSch.emptyTable(RoomBarGoodsTable.TABLE_NAME);
            SoapObject t = (SoapObject) response.getProperty("return");
            for (int i = 0; i < t.getPropertyCount(); i++) {
                SoapObject bar = (SoapObject) t.getProperty(i);
                String name = bar.getProperty(RoomBarGoodsRecord.GDS_NAME).toString();
                String room = bar.getProperty(RoomBarGoodsRecord.ROOM_NUM).toString();
                String id = bar.getProperty(RoomBarGoodsRecord.GDS_ID).toString();
                String quantity = bar.getProperty(RoomBarGoodsRecord.GDS_QUANTITY).toString();
                RoomBarGoodsRecord roomBarGds = new RoomBarGoodsRecord(room, name, id, quantity);
                dbSch.addRoomBarGoodsItem(roomBarGds);
                Log.d(TAG, String.format("%s; %s: %s - %s", room, id, name, quantity));
            }
        }

        private void parseResponse(String dump) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = null;
            try {
                db = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(dump));

            Document doc;
            try {
                doc = db.parse(is);
                doc.getDocumentElement().normalize();
                // Node hh=doc.getElementsByTagName("hhCompany").item(0).;

                org.w3c.dom.Element ee = doc.getDocumentElement();

            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            // In this example it is the return value from the web service
            textView.setText(result);
        }

        @Override
        protected void onPreExecute() {
            // Things to be done before execution of long running operation. For
            // example showing ProgessDialog
        }

        @Override
        protected void onProgressUpdate(String... text) {
            textView.setText(text[0]);
            // Things to be done while execution of long running operation is in
            // progress. For example updating ProgessDialog
        }

    }

}
