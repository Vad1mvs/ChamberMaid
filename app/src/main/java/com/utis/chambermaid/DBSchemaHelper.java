
package com.utis.chambermaid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import com.utis.chambermaid.records.EmpRecord;
import com.utis.chambermaid.records.EntRecord;
import com.utis.chambermaid.records.HotelRoomRecord;
import com.utis.chambermaid.records.InvoiceContRecord;
import com.utis.chambermaid.records.InvoiceRecord;
import com.utis.chambermaid.records.OperRecord;
import com.utis.chambermaid.records.PlacementRecord;
import com.utis.chambermaid.records.RoomBarGoodsRecord;
import com.utis.chambermaid.tables.BarCodeGoodsTable;
import com.utis.chambermaid.tables.EmpTable;
import com.utis.chambermaid.tables.EntTable;
import com.utis.chambermaid.tables.GoodsTable;
import com.utis.chambermaid.tables.HotelRoomTable;
import com.utis.chambermaid.tables.InvoiceContTable;
import com.utis.chambermaid.tables.InvoiceTable;
import com.utis.chambermaid.tables.LogsTable;
import com.utis.chambermaid.tables.OperTable;
import com.utis.chambermaid.tables.PlacementTable;
import com.utis.chambermaid.tables.RoomBarGoodsTable;
import com.utis.chambermaid.tables.StateTable;
import com.utis.chambermaid.tables.UserTable;
import com.utis.chambermaid.tables.ZToolTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DBSchemaHelper extends SQLiteOpenHelper {
	private static final boolean D = true;
	private static final String TAG = "DBHelper";
	private static final String ZERO_CONDITION = " = 0 ";
	private static final String NOT_ZERO_CONDITION = " <> 0 ";
	public static final String REC_STAT_DEL_CONDITION = " = 2 ";
	public static final String REC_STAT_NODEL_CONDITION = " < 2 ";
	public static final String DATABASE_NAME = "content12.db";
	// TOGGLE THIS NUMBER FOR UPDATING TABLES AND DATABASE
	private static final int DATABASE_VERSION = 11;
    DBSchemaHelper dbSchemaHelper;
    SQLiteDatabase sd;
    public static final SimpleDateFormat dateYear = new SimpleDateFormat("yyyy");
	public static final SimpleDateFormat dateFormatDayYY = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat dateFormatYYT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


	public static final SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd.MM.yyyy");
	
	public static final SimpleDateFormat dateFormatYY = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	public static final SimpleDateFormat dateFormatMM = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    public static final SimpleDateFormat dateFormatOnlyTimeMM = new SimpleDateFormat("HH:mm");

	public static final SimpleDateFormat dateFormatMSYY = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public final SimpleDateFormat dateFormatMS = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
	
	
	private DBSchemaHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	private static DBSchemaHelper INSTANCE = null;


	public synchronized static DBSchemaHelper getInstance(Context context) {
        if (INSTANCE == null)
        	INSTANCE = new DBSchemaHelper(context);
        return INSTANCE;
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		// CREATE DB TABLES
		createCommonTables(db);
		if (D) { Log.d(TAG, "Tables created"); };
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String query;
        if (newVersion == 11) {
            query = "ALTER TABLE " + HotelRoomTable.TABLE_NAME + " ADD COLUMN " +
                    HotelRoomTable.RESERVATIONS_DATE + " TEXT";
            db.execSQL(query);
            query = "ALTER TABLE " + HotelRoomTable.TABLE_NAME + " ADD COLUMN " +
                    HotelRoomTable.CHECK_ROOM + " INTEGER DEFAULT 0";
            db.execSQL(query);
        } else if (newVersion == 10) {
            query = "ALTER TABLE " + EmpTable.TABLE_NAME + " ADD COLUMN " +
                    EmpTable.ID_JOB + " INTEGER DEFAULT " + EmpRecord.MAID_EMP;
            db.execSQL(query);
            query = "ALTER TABLE " + HotelRoomTable.TABLE_NAME + " ADD COLUMN " +
                    HotelRoomTable.CHANGE_OF_BED_DATE + " TEXT";
            db.execSQL(query);
            query = "ALTER TABLE " + HotelRoomTable.TABLE_NAME + " ADD COLUMN " +
                    HotelRoomTable.DEPARTURE_DATE + " TEXT";
            db.execSQL(query);
            query = "ALTER TABLE " + HotelRoomTable.TABLE_NAME + " ADD COLUMN " +
                    HotelRoomTable.TWIN + " INTEGER DEFAULT 0";
            db.execSQL(query);
            query = "ALTER TABLE " + HotelRoomTable.TABLE_NAME + " ADD COLUMN " +
                    HotelRoomTable.WATER_LEAKAGE + " INTEGER DEFAULT 0";
            db.execSQL(query);


        } else if (newVersion == 9) {
			query = "ALTER TABLE " + RoomBarGoodsTable.TABLE_NAME + " ADD COLUMN " +
					RoomBarGoodsTable.TOTAL + " INTEGER DEFAULT 0";
			db.execSQL(query);
		} else if (newVersion == 8) {
            query = "ALTER TABLE " + RoomBarGoodsTable.TABLE_NAME + " ADD COLUMN " +
                    RoomBarGoodsTable.ID_GDS_STR + " TEXT";
            db.execSQL(query);
        } else if (newVersion == 7) {
            query = "ALTER TABLE " + RoomBarGoodsTable.TABLE_NAME + " ADD COLUMN " +
                    RoomBarGoodsTable.SALE_MODE + " INTEGER";
            db.execSQL(query);
        } else if (newVersion == 6) {
            query = "ALTER TABLE " + RoomBarGoodsTable.TABLE_NAME + " ADD COLUMN " +
                    RoomBarGoodsTable.GDS_STORE_QUANTITY + " INTEGER";
            db.execSQL(query);
        } else if (newVersion == 5) {
            query = "ALTER TABLE " + EntTable.TABLE_NAME + " ADD COLUMN " +
                    EntTable.ID_EXT_STR + " TEXT";
            db.execSQL(query);
        } else if (newVersion == 4) {
            query = "ALTER TABLE " + EmpTable.TABLE_NAME + " ADD COLUMN " +
                    EmpTable.ID_EXT_STR + " TEXT";
            db.execSQL(query);
            query = "ALTER TABLE " + HotelRoomTable.TABLE_NAME + " ADD COLUMN " +
                    HotelRoomTable.ID_HOTEL_STR + " TEXT";
            db.execSQL(query);

        } else if (newVersion == 3) {
			query = "ALTER TABLE " + RoomBarGoodsTable.TABLE_NAME + " ADD COLUMN " +
					RoomBarGoodsTable.GDS_SOLD_QUANTITY + " INTEGER";
			db.execSQL(query);
			db.execSQL("CREATE INDEX hotel_room_idx ON " + HotelRoomTable.TABLE_NAME + "(" + HotelRoomTable.ID + ");");
		} else {
			if (D) {
				Log.w(TAG, "Upgrading database FROM version "
						+ oldVersion + " to " + newVersion + ",	which will destroy all old data");
			}
			;
			// Implement how to "move" your application data
			// during an upgrade of schema versions.
			// Move or delete data as required. Your call.
			// KILL PREVIOUS TABLES IF UPGRADED
			dropTable(db, GoodsTable.TABLE_NAME);
			dropTable(db, EntTable.TABLE_NAME);
			dropTable(db, StateTable.TABLE_NAME);
			dropTable(db, EmpTable.TABLE_NAME);
			dropTable(db, UserTable.TABLE_NAME);
			dropTable(db, LogsTable.TABLE_NAME);
			dropTable(db, ZToolTable.TABLE_NAME);
			dropTable(db, PlacementTable.TABLE_NAME);
			dropTable(db, InvoiceTable.TABLE_NAME);
			dropTable(db, InvoiceContTable.TABLE_NAME);
			dropTable(db, BarCodeGoodsTable.TABLE_NAME);

			dropTable(db, HotelRoomTable.TABLE_NAME);                                               // ----- ##### -----
			dropTable(db, RoomBarGoodsTable.TABLE_NAME);


			//dropTable(db, ChambermaidTable.TABLE_NAME);


			// CREATE NEW INSTANCE OF SCHEMA
			onCreate(db);
		}
	}

	public void createCommonTables(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + GoodsTable.TABLE_NAME + 
				" (" + GoodsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ GoodsTable.ID_EXTERNAL + " INTEGER,"
				+ GoodsTable.ID_PARENT + " INTEGER,"
				+ GoodsTable.TTYPE + " INTEGER,"
				+ GoodsTable.GDSTYPE + " INTEGER,"
				+ GoodsTable.NM + " TEXT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS " + EntTable.TABLE_NAME +
				" (" + EntTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ EntTable.ID_EXTERNAL + " INTEGER,"
                + EntTable.ID_EXT_STR + " TEXT,"
				+ EntTable.ID_PARENT + " INTEGER,"
				+ EntTable.LAT + " REAL,"
				+ EntTable.LNG + " REAL,"
				+ EntTable.ADDR	+ " TEXT,"
				+ EntTable.NM + " TEXT);");
			db.execSQL("CREATE TABLE IF NOT EXISTS " + StateTable.TABLE_NAME + 
				" (" + StateTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ StateTable.ID_EXTERNAL + " INTEGER,"
				+ StateTable.ID_DOC + " INTEGER,"
//				+ StateTable.NO + " INTEGER,"
				+ StateTable.NM + " TEXT);");
			createEmpTable(db);
			createUserTable(db);
			createLogsTable(db);
			createZToolTable(db);
			createPlacementTable(db);
			createInvoiceTable(db);
			createInvoiceContTable(db);
			createBarCodeGoodsTable(db);
			createHotelRoomTable(db);
            createRoomBarGoodsTable(db);

            createOperTable(db);

		} catch (Exception e) {
			Log.e("createCommonTables", e.getMessage());
		}
	}
		
	public void dropTable(SQLiteDatabase db, String tableName) {
		try {
			db.execSQL("DROP TABLE IF EXISTS " + tableName);
		} catch (Exception e) {
			Log.e("dropTable: " + tableName, e.getMessage());
		}
	}
		
	public boolean emptyTable(String tableName) {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 0;
		try {
			result = sd.delete(tableName, "1", null);
		} catch (Exception e) {
			Log.e("LOG_TAG", e.getMessage());
		}
		return (result > 0);		
	}
	
	public boolean emptyAllConstTables() {
		SQLiteDatabase sd = getWritableDatabase();
//		String[] whereArgs = new String[] { };
		
		int result = sd.delete(GoodsTable.TABLE_NAME, "1", null);
		result = sd.delete(EntTable.TABLE_NAME, "1", null);
		result = sd.delete(StateTable.TABLE_NAME, "1", null);
		result = sd.delete(EmpTable.TABLE_NAME, "1", null);
		return (result > 0);		
	}

	public void createUserTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + UserTable.TABLE_NAME + 
				" (" + UserTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ UserTable.ID_EXTERNAL + " INTEGER,"
				+ UserTable.ID_ADM + " INTEGER,"
				+ UserTable.LAST_Z_ENT + " INTEGER,"
				+ UserTable.SURNAME + " TEXT,"
				+ UserTable.NAME + " TEXT,"
				+ UserTable.PATRONIMIC + " TEXT);");
		} catch (Exception e) {
			Log.e(TAG, "createUserTable: " + e.getMessage());
		}
	}
	
	public void createEmpTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + EmpTable.TABLE_NAME + 
				" (" + EmpTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ EmpTable.ID_EXTERNAL + " INTEGER,"
				+ EmpTable.ID_ENT + " INTEGER,"
                + EmpTable.ID_JOB + " INTEGER DEFAULT "+ EmpRecord.MAID_EMP + ","
                + EmpTable.ID_EXT_STR + " TEXT,"
				+ EmpTable.SURNAME + " TEXT,"
				+ EmpTable.NAME + " TEXT,"
				+ EmpTable.PATRONIMIC + " TEXT,"
				+ EmpTable.PHOTO + " BLOB);");
		} catch (Exception e) {
			Log.e(TAG, "createEmpTable: " + e.getMessage());
		}
	}

	public void createHotelRoomTable(SQLiteDatabase db) {                                           // ----- ##### -----
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + HotelRoomTable.TABLE_NAME +
                " (" + HotelRoomTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + HotelRoomTable.ROOM_FLOOR + " INTEGER,"
                + HotelRoomTable.ROOM_NUM + " INTEGER,"
                + HotelRoomTable.ID_HOTEL + " INTEGER,"
                + HotelRoomTable.ID_HOTEL_STR + " TEXT,"
                + HotelRoomTable.HOTEL + " TEXT,"
                + HotelRoomTable.OCCUPIED + " INTEGER,"
                + HotelRoomTable.GUEST_IN_ROOM + " INTEGER,"
                + HotelRoomTable.SERVICE_NEEDED + " INTEGER,"
                + HotelRoomTable.TODAY_CLEARENCE + " INTEGER,"
                + HotelRoomTable.DOOR + " INTEGER,"
                + HotelRoomTable.WINDOW + " INTEGER,"
                + HotelRoomTable.BALCONY + " INTEGER,"
                + HotelRoomTable.CHANGE_OF_BED_DATE + " TEXT,"
                + HotelRoomTable.DEPARTURE_DATE + " TEXT,"
                + HotelRoomTable.TWIN + " INTEGER,"
                + HotelRoomTable.WATER_LEAKAGE + " INTEGER,"
                + HotelRoomTable.CHECK_ROOM + " INTEGER,"
                + HotelRoomTable.RESERVATIONS_DATE + " TEXT,"
                + HotelRoomTable.STATUS_NOM + " INTEGER,"
                + HotelRoomTable.REPAIR + " INTEGER,"
                + HotelRoomTable.NOTE_REPAIR + " TEXT,"
                + HotelRoomTable.TIPE_CLEARENCE + " INTEGER,"
                + HotelRoomTable.NOT_DISTURB + " INTEGER,"
                + HotelRoomTable.CALL_CHAMBERMAID + " INTEGER,"
                + HotelRoomTable.CHAMBERMAID_IN_ROOM+ " INTEGER,"

                + HotelRoomTable.CHANGE_BED_EXTRA + " INTEGER,"
                + HotelRoomTable.MODIFIED + " INTEGER);");
		} catch (Exception e) {
			Log.e(TAG, "createHotelRoomTable: " + e.getMessage());
		}
	}


    public void createOperTable(SQLiteDatabase db) {                                                // ----- ##### -----
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + OperTable.TABLE_NAME +
                    //room, check, callChambermaid, idHotel, inRoom , quit, surname
                    " (" + OperTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + OperTable.ROOM_NUM + " INTEGER,"
                    + OperTable.CHECK_ROOM + " INTEGER,"
                    + OperTable.CALL_CHAMBERMAID + " INTEGER,"
                    + OperTable.CHAMBERMAID_IN_ROOM+ " INTEGER,"
                    + OperTable.CHAMBERMAID_QUIT+ " INTEGER,"
                    + OperTable.SURNAME + " TEXT,"
                    + OperTable.NAME + " TEXT,"
                    + OperTable.MODIFIED + " INTEGER);");
        } catch (Exception e) {
            Log.e(TAG, "createHotelRoomTable: " + e.getMessage());
        }
    }


    public void createRoomBarGoodsTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + RoomBarGoodsTable.TABLE_NAME +
                " (" + RoomBarGoodsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RoomBarGoodsTable.ROOM_NUM + " INTEGER,"
                + RoomBarGoodsTable.ID_GDS_STR + " TEXT,"
                + RoomBarGoodsTable.GDS_NAME + " TEXT,"
                + RoomBarGoodsTable.TOTAL + " INTEGER DEFAULT 0,"
                + RoomBarGoodsTable.GDS_QUANTITY + " INTEGER,"
                + RoomBarGoodsTable.GDS_STORE_QUANTITY + " INTEGER,"
                + RoomBarGoodsTable.GDS_SOLD_QUANTITY + " INTEGER,"
                + RoomBarGoodsTable.SALE_MODE + " INTEGER,"
                + RoomBarGoodsTable.MODIFIED + " INTEGER);");
        } catch (Exception e) {
            Log.e(TAG, "createRoomBarGoodsTable: " + e.getMessage());
        }
    }

    public void createLogsTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + LogsTable.TABLE_NAME + 
				" (" + LogsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ LogsTable.LEVEL + " INTEGER,"
				+ LogsTable.MODIFIED + " INTEGER DEFAULT 0,"
				+ LogsTable.MSG_DATE + " TEXT,"
				+ LogsTable.MSG + " TEXT);");
		} catch (Exception e) {
			Log.e(TAG, "createLogsTable: " + e.getMessage());
		}
	}	
	
	public void createZToolTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + ZToolTable.TABLE_NAME + 
				" (" + ZToolTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ ZToolTable.ID_EXTERNAL + " INTEGER,"
				+ ZToolTable.ID_ENT + " INTEGER,"
				+ ZToolTable.VAT + " REAL);");
		} catch (Exception e) {
			Log.e(TAG, "createZToolTable: " + e.getMessage());
		}
	}

	public void createPlacementTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + PlacementTable.TABLE_NAME + 
				" (" + PlacementTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ PlacementTable.ID_EXTERNAL + " INTEGER,"
				+ PlacementTable.ID_ENT + " INTEGER,"
				+ PlacementTable.ID_PARENT + " INTEGER,"
				+ PlacementTable.TTYPE + " INTEGER,"
				+ PlacementTable.REMARK + " TEXT,"
				+ PlacementTable.NAME + " TEXT);");
		} catch (Exception e) {
			Log.e(TAG, "createPlacementTable: " + e.getMessage());
		}
	}	

	public void createInvoiceTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + InvoiceTable.TABLE_NAME + 
				" (" + InvoiceTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ InvoiceTable.ID_EXTERNAL + " INTEGER,"
				+ InvoiceTable.PLACEMENT_ID + " INTEGER,"
				+ InvoiceTable.PLACEMENT2_ID + " INTEGER,"				
				+ InvoiceTable.STATE_ID + " INTEGER,"
				+ InvoiceTable.OWNER_ID + " INTEGER,"
				+ InvoiceTable.DOC_ID + " INTEGER,"
				+ InvoiceTable.EMP_ID + " INTEGER,"
				+ InvoiceTable.MODIFIED + " INTEGER DEFAULT 0,"
				+ InvoiceTable.REC_STAT + " INTEGER DEFAULT 0,"
				+ InvoiceTable.IDATE + " TEXT,"
				+ InvoiceTable.DATE_CHANGE + " TEXT,"
				+ InvoiceTable.NUM + " TEXT);");
		} catch (Exception e) {
			Log.e(TAG, "createInvoiceTable: " + e.getMessage());
		}
	}	
	
	public void createInvoiceContTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + InvoiceContTable.TABLE_NAME + 
				" (" + InvoiceContTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ InvoiceContTable.ID_EXTERNAL + " INTEGER,"
				+ InvoiceContTable.INVOICE_ID + " INTEGER,"
				+ InvoiceContTable.GDS_ID + " INTEGER,"
				+ InvoiceContTable.GDS_COUNT + " REAL,"
				+ InvoiceContTable.GDS_PRICE + " REAL,"
				+ InvoiceContTable.IC_NUM + " INTEGER,"
				+ InvoiceContTable.MARK + " INTEGER DEFAULT 0,"
				+ InvoiceContTable.MODIFIED + " INTEGER DEFAULT 0,"
				+ InvoiceContTable.REC_STAT + " INTEGER DEFAULT 0);");
		} catch (Exception e) {
			Log.e(TAG, "createInvoiceContTable: " + e.getMessage());
		}
	}	

	public void createBarCodeGoodsTable(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE TABLE IF NOT EXISTS " + BarCodeGoodsTable.TABLE_NAME + 
				" (" + BarCodeGoodsTable.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," 
				+ BarCodeGoodsTable.ID_EXTERNAL + " INTEGER,"
				+ BarCodeGoodsTable.ID_GDS + " INTEGER,"
				+ BarCodeGoodsTable.TTYPE + " INTEGER,"
				+ BarCodeGoodsTable.BARCODE + " TEXT);");
		} catch (Exception e) {
			Log.e(TAG, "createBarCodeGoodsTable: " + e.getMessage());
		}
	}



	public boolean isTableExists(String tableName) {
		boolean res = false;
		SQLiteDatabase sd = getReadableDatabase();
		String query = "SELECT COUNT(*) FROM "+ tableName;
		Cursor c = null;
    	try {
    		c = sd.rawQuery(query, null);
    		res = true;
    	} catch (Exception e) {
    		if (D) Log.e(TAG, "isTableExists exception = " + e.getMessage());
		} finally {
			if (c != null)
				c.close();
		}		
		return res;
	}
	
	public boolean emptyEmpTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		result = sd.delete(EmpTable.TABLE_NAME, "1", null);
		
		if (D) { Log.d(TAG, "EmpTable emptied"); };
		return (result > 0);			
	}


    public boolean emptyStateTable() {
		SQLiteDatabase sd = getWritableDatabase();
		int result = 1;
		result = sd.delete(StateTable.TABLE_NAME, "1", null);
		
		if (D) { Log.d(TAG, "StateTable emptied"); };
		return (result > 0);			
	}
	
	public long getTableCount(String query) {
		long result = 0;
		Cursor c = null;
		SQLiteDatabase sd = getReadableDatabase();
    	try {
    		c = sd.rawQuery(query, null);
    		if (c != null && c.moveToFirst()) {
	    		result = c.getLong(0);
    		}
    	} catch (Exception e) {
    		result = -1;
    		if (D) Log.e(TAG, "getTableCount exception = " + e.getMessage());
		} finally {
			if (c != null)
				c.close();
		}
		return result;
	}

	/* ****************************************** */
	public void insEntBulkJSON(JSONArray records) {
		SQLiteDatabase sd = getWritableDatabase();
		InsertHelper ih = new InsertHelper(sd, EntTable.TABLE_NAME);
		final int idExtColumn = ih.getColumnIndex(EntTable.ID_EXTERNAL);
		final int idParentColumn = ih.getColumnIndex(EntTable.ID_PARENT);
		final int idLatColumn = ih.getColumnIndex(EntTable.LAT);
		final int idLngColumn = ih.getColumnIndex(EntTable.LNG);
		final int idAddrColumn = ih.getColumnIndex(EntTable.ADDR);
		final int idNmColumn = ih.getColumnIndex(EntTable.NM);
		
		long id, idParent;
		Double lat, lng;
		String sLat, sLng, nm, addr;
		long startTime = System.currentTimeMillis();
		try {
			sd.execSQL("PRAGMA synchronous=OFF");
		    sd.setLockingEnabled(false);
		    sd.beginTransaction();			
			for (int i = 0; i < records.length(); i++) {			
				JSONObject warrant;
				try {
					warrant = records.getJSONObject(i);
					id = warrant.getLong("id");
					idParent = warrant.getLong("id_p");
					sLat = warrant.getString("lat");
					lat = (double) Float.parseFloat(sLat.replace(",", "."));
					sLng = warrant.getString("lng");
					lng = (double) Float.parseFloat(sLng.replace(",", "."));
					nm = warrant.getString("nm");
					addr = warrant.getString("addr");   					
					// Get the InsertHelper ready to insert a single row
		            ih.prepareForInsert();
		            // Add the data for each column
		            ih.bind(idExtColumn, id);
		            ih.bind(idParentColumn, idParent);
		            ih.bind(idLatColumn, lat);
		            ih.bind(idLngColumn, lng);
		            ih.bind(idAddrColumn, addr);
		            ih.bind(idNmColumn, nm);
		            // Insert the row into the database.
	                ih.execute();
                    Log.d(TAG, String.valueOf(id)+" / "+ nm +" / "+ addr);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			sd.setTransactionSuccessful();
		} finally {

			sd.endTransaction();
		    sd.setLockingEnabled(true);
		    sd.execSQL("PRAGMA synchronous=NORMAL");
		    ih.close();			
		    final long endtime = System.currentTimeMillis();
	        Log.i(TAG, "insEntBulkJSON: Time to insert Members: " + String.valueOf(endtime - startTime));
		}					
	}
	
	/* ****************************************** */
	public void insGoodsBulkJSON(JSONArray records) {
		SQLiteDatabase sd = getWritableDatabase();
		InsertHelper ih = new InsertHelper(sd, GoodsTable.TABLE_NAME);
		final int idExtColumn = ih.getColumnIndex(GoodsTable.ID_EXTERNAL);
		final int idParentColumn = ih.getColumnIndex(GoodsTable.ID_PARENT);
		final int idTtypeColumn = ih.getColumnIndex(GoodsTable.TTYPE);
		final int idGdsTypeColumn = ih.getColumnIndex(GoodsTable.GDSTYPE);
		final int idNmColumn = ih.getColumnIndex(GoodsTable.NM);
		
		long id, idParent;
		int ttype, gdstype;
		String nm;
		long startTime = System.currentTimeMillis();
		try {
			sd.execSQL("PRAGMA synchronous=OFF");
		    sd.setLockingEnabled(false);
		    sd.beginTransaction();			
			for (int i = 0; i < records.length(); i++) {			
				JSONObject warrant;
				try {
					warrant = records.getJSONObject(i);
					id = warrant.getLong("id");
					idParent = warrant.getLong("id_p");
					ttype = warrant.getInt("ttype");
					gdstype = warrant.getInt("gdstype");
					nm = warrant.getString("nm");
					// Get the InsertHelper ready to insert a single row
		            ih.prepareForInsert();
		            // Add the data for each column
		            ih.bind(idExtColumn, id);
		            ih.bind(idParentColumn, idParent);
		            ih.bind(idTtypeColumn, ttype);
		            ih.bind(idGdsTypeColumn, gdstype);
		            ih.bind(idNmColumn, nm);
		            // Insert the row into the database.
	                ih.execute();
                    Log.d(TAG, String.valueOf(id)+" / "+ nm +" / "+ String.valueOf(gdstype));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			sd.setTransactionSuccessful();
		} finally {
			sd.endTransaction();
		    sd.setLockingEnabled(true);
		    sd.execSQL("PRAGMA synchronous=NORMAL");
		    ih.close();	
		    final long endtime = System.currentTimeMillis();
	        Log.i(TAG, "insGoodsBulkJSON: Time to insert Members: " + String.valueOf(endtime - startTime));
		}				
	}
	
	/* ****************************************** */
	public void insBarCodeGoodsBulkJSON(JSONArray records) {
		SQLiteDatabase sd = getWritableDatabase();
		InsertHelper ih = new InsertHelper(sd, BarCodeGoodsTable.TABLE_NAME);
		final int idExtColumn = ih.getColumnIndex(BarCodeGoodsTable.ID_EXTERNAL);
		final int idGdsColumn = ih.getColumnIndex(BarCodeGoodsTable.ID_GDS);
		final int idTtypeColumn = ih.getColumnIndex(BarCodeGoodsTable.TTYPE);
		final int idNmColumn = ih.getColumnIndex(BarCodeGoodsTable.BARCODE);
		
		long id, id_gds; 
        int ttype;
		String nm;
		long startTime = System.currentTimeMillis();
		try {
			sd.execSQL("PRAGMA synchronous=OFF");
		    sd.setLockingEnabled(false);
		    sd.beginTransaction();			
			for (int i = 0; i < records.length(); i++) {			
				JSONObject warrant;
				try {
					warrant = records.getJSONObject(i);
					id = warrant.getInt("id");
					id_gds = warrant.getInt("id_gds");					
					ttype = warrant.getInt("ttype");
					nm = warrant.getString("bc");
					// Get the InsertHelper ready to insert a single row
		            ih.prepareForInsert();
		            // Add the data for each column
		            ih.bind(idExtColumn, id);
		            ih.bind(idGdsColumn, id_gds);
		            ih.bind(idTtypeColumn, ttype);
		            ih.bind(idNmColumn, nm);
		            // Insert the row into the database.
	                ih.execute();			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			sd.setTransactionSuccessful();
		} finally {
			sd.endTransaction();
		    sd.setLockingEnabled(true);
		    sd.execSQL("PRAGMA synchronous=NORMAL");
		    ih.close();	
		    final long endtime = System.currentTimeMillis();
	        Log.i(TAG, "insBarCodeGoodsBulkJSON: Time to insert Members: " + String.valueOf(endtime - startTime));
		}				
	}
		

	/* ****************************************** */
	public void insEmpsBulkJSON(JSONArray records) {
		SQLiteDatabase sd = getWritableDatabase();
		InsertHelper ih = new InsertHelper(sd, EmpTable.TABLE_NAME);
		final int idExtColumn = ih.getColumnIndex(EmpTable.ID_EXTERNAL);
		final int idEntColumn = ih.getColumnIndex(EmpTable.ID_ENT);
		final int idSurnameColumn = ih.getColumnIndex(EmpTable.SURNAME);
		final int idNameColumn = ih.getColumnIndex(EmpTable.NAME);
		final int idPatronimicColumn = ih.getColumnIndex(EmpTable.PATRONIMIC);
		final int idPhotoColumn = ih.getColumnIndex(EmpTable.PHOTO);
		
		int id, id_ent;
		String surname, name, patronimic, sPhoto;
		byte[] photo;
		long startTime = System.currentTimeMillis();
		try {
			sd.execSQL("PRAGMA synchronous=OFF");
		    sd.setLockingEnabled(false);
		    sd.beginTransaction();			
			for (int i = 0; i < records.length(); i++) {			
				JSONObject emp;
				try {
					emp = records.getJSONObject(i);
					id = emp.getInt("id");
					id_ent = emp.getInt("id_ent");					
					surname = emp.getString("fam");
					name = emp.getString("nm");
					patronimic = emp.getString("otch");
					sPhoto = emp.getString("photo");
					photo = Base64.decode(sPhoto, Base64.DEFAULT);
					
					// Get the InsertHelper ready to insert a single row
		            ih.prepareForInsert();
		            // Add the data for each column
		            ih.bind(idExtColumn, id);
		            ih.bind(idEntColumn, id_ent);
		            ih.bind(idSurnameColumn, surname);
		            ih.bind(idNameColumn, name);
		            ih.bind(idPatronimicColumn, patronimic);
		            ih.bind(idPhotoColumn, photo);
		            // Insert the row into the database.
	                ih.execute();			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			sd.setTransactionSuccessful();
		} finally {
			sd.endTransaction();
		    sd.setLockingEnabled(true);
		    sd.execSQL("PRAGMA synchronous=NORMAL");
		    ih.close();	
		    final long endtime = System.currentTimeMillis();
	        Log.i(TAG, "insEmpsBulkJSON: Time to insert Members: " + String.valueOf(endtime - startTime));
		}				
	}


    /* ************ COMMON ****************************** */

    private Boolean isRecordPresent(String query) {
		Boolean result = true;
		Cursor c = null;
		if (query.length() > 0) {
			try {
				SQLiteDatabase sd = getWritableDatabase();
		    	c = sd.rawQuery(query, null);
				result = c.moveToNext();
	    	} catch (Exception ex) {
	        	ex.printStackTrace();
	        } finally {
				if (c != null)
					c.close();
	        }					
		}
		return result;
	}
		
	/* ****************************************** */

	public long getBarCodeGoodsCount() {
		String query = "SELECT COUNT("+ BarCodeGoodsTable.ID +") FROM " + BarCodeGoodsTable.TABLE_NAME;
		return getTableCount(query);
	}

	private Boolean isBarCodeGoodsIdPresent(long idExt) {
		Boolean result = true;
		if (idExt > 0) {
			String query = "SELECT "+ BarCodeGoodsTable.ID +" FROM " +
				BarCodeGoodsTable.TABLE_NAME + " WHERE " + BarCodeGoodsTable.ID_EXTERNAL + " = " + idExt;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long addBarCodeGoodsItem(long idExt, long idGds, int ttype, String barcode) {
		long result = 0;
		if (!isBarCodeGoodsIdPresent(idExt)) {
			ContentValues cv = new ContentValues();
			cv.put(BarCodeGoodsTable.ID_EXTERNAL, idExt);
			cv.put(BarCodeGoodsTable.ID_GDS, idGds);
			cv.put(BarCodeGoodsTable.TTYPE, ttype);
			cv.put(BarCodeGoodsTable.BARCODE, barcode);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.insert(BarCodeGoodsTable.TABLE_NAME, BarCodeGoodsTable.BARCODE, cv);
		}
		if (result < 0) Log.d(TAG, "addBarCodeGoodsItem - Error");
		return result;
	}
	
	private String parseBarcode(String barcode){
		String result = "", validChrs = "0123456789";
		char chr; 
		barcode = barcode.trim();
		for (int i=0; i < barcode.length(); i++) {
			chr = barcode.charAt(i);
			if (validChrs.indexOf(chr) != -1) {
				result += chr;
			}
		}
		return result;
	}
	
	public int getBarCodeGoodsId(String barcode) {
		int result = 0;
		barcode = parseBarcode(barcode);
//		barcode = barcode.trim();
		if (barcode.length() > 0) {
			String query = "SELECT " + BarCodeGoodsTable.ID_GDS +" FROM " +
				BarCodeGoodsTable.TABLE_NAME + " WHERE " + BarCodeGoodsTable.BARCODE + " LIKE \'" + barcode + "\'";
			result = (int) getTableCount(query);
		}
		return result;
	}
	
	public Cursor getGdsBarCode(int idGds) {
		Cursor result = null;
		String query;
		if (idGds > 0) {
			SQLiteDatabase sd = getReadableDatabase();
			try {
				query = "SELECT "+ BarCodeGoodsTable.BARCODE +" FROM " + 
						BarCodeGoodsTable.TABLE_NAME + " WHERE " + BarCodeGoodsTable.ID_GDS + " = " + idGds;
		    	result = sd.rawQuery(query, null);
				if (!result.moveToNext()){
					result = null;
				}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getGdsBarCode exception = " + e.getMessage());
			} 
		}				
		return result;
	}




    /* ****************************************** */
    public void fillHotelInfo(String floorNm) {
        String query = "SELECT DISTINCT "+ HotelRoomTable.ID_HOTEL + ", " + HotelRoomTable.ID_HOTEL_STR + ", " +
                HotelRoomTable.HOTEL + " FROM " + HotelRoomTable.TABLE_NAME;
        Cursor c = null;
        int colid, idExt;
        String name, idStr;
        SQLiteDatabase sqdb = getWritableDatabase();
        try {
            c = sqdb.rawQuery(query, null);
            while (c.moveToNext()) {
                colid = c.getColumnIndex(HotelRoomTable.ID_HOTEL);
                idExt = c.getInt(colid);
                colid = c.getColumnIndex(HotelRoomTable.ID_HOTEL_STR);
                idStr = c.getString(colid);
                colid = c.getColumnIndex(HotelRoomTable.HOTEL);
                name = c.getString(colid);

                addEntItem(idExt, idStr, name);
                fillHotelFloorInfo(idExt, floorNm);
            }
        } catch(Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
    }

    public void fillHotelFloorInfo(int idHotel, String floorNm) {
        String query = "SELECT DISTINCT "+ HotelRoomTable.ROOM_FLOOR +
                " FROM " + HotelRoomTable.TABLE_NAME + " WHERE " +
                HotelRoomTable.ID_HOTEL + "=" + idHotel;
        Cursor c = null;
        int colid, roomFloor;
        String name;
        SQLiteDatabase sqdb = getWritableDatabase();
        try {
            c = sqdb.rawQuery(query, null);
            while (c.moveToNext()) {
                colid = c.getColumnIndex(HotelRoomTable.ROOM_FLOOR);
                roomFloor = c.getInt(colid);
                name = roomFloor + floorNm;

                long id = addPlacementItem(idHotel, roomFloor, 0, PlacementRecord.PLACE_FLOOR, name);
                fillHotelFloorRoomInfo(idHotel, roomFloor, id);
            }
        } catch(Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
    }

    public void fillHotelFloorRoomInfo(int idHotel, int floor, long idFloor) {
        String query = "SELECT "+ HotelRoomTable.ROOM_NUM +
                " FROM " + HotelRoomTable.TABLE_NAME + " WHERE " +
                HotelRoomTable.ID_HOTEL + "=" + idHotel + " AND " +
                HotelRoomTable.ROOM_FLOOR + "=" + floor;
        Cursor c = null;
        int colid, roomNum;
        String name;
        SQLiteDatabase sqdb = getWritableDatabase();
        try {
            c = sqdb.rawQuery(query, null);
            while (c.moveToNext()) {
                colid = c.getColumnIndex(HotelRoomTable.ROOM_NUM);
                roomNum = c.getInt(colid);
                name = ""+ roomNum;

                addPlacementItem(idHotel, roomNum, (int) idFloor, PlacementRecord.PLACE_ROOM, name);
            }
        } catch(Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }
    }

    /* ****************************************** */




    public long getRoomBarGoodsCount() {
        String query = "SELECT COUNT("+ RoomBarGoodsTable.ID +") FROM " + RoomBarGoodsTable.TABLE_NAME;
        return getTableCount(query);
    }

    public long getRoomBarGoodsSoldCount() {
        String query = "SELECT COUNT("+ RoomBarGoodsTable.ID +") FROM " + RoomBarGoodsTable.TABLE_NAME +
                " WHERE " + RoomBarGoodsTable.GDS_SOLD_QUANTITY + NOT_ZERO_CONDITION;
        return getTableCount(query);
    }

    public long addRoomBarGoodsItem(RoomBarGoodsRecord roomRecord) {
        long result = 0;
//        if (!isHotelRoomNumPresent(roomRecord.getHotelId(), roomRecord.getRoomNum())) {
            ContentValues cv = new ContentValues();
            cv.put(RoomBarGoodsTable.ID_GDS_STR, roomRecord.getGdsIdStr());
            cv.put(RoomBarGoodsTable.GDS_NAME, roomRecord.getGdsName());
            cv.put(RoomBarGoodsTable.GDS_STORE_QUANTITY, roomRecord.getGdsQuantityMaid());
            cv.put(RoomBarGoodsTable.GDS_QUANTITY, roomRecord.getGdsQuantity());
            cv.put(RoomBarGoodsTable.ROOM_NUM, roomRecord.getRoom());
            cv.put(RoomBarGoodsTable.SALE_MODE, roomRecord.getSaleMode());
            cv.put(RoomBarGoodsTable.MODIFIED, roomRecord.isModified()?1:0);
            SQLiteDatabase sd = getWritableDatabase();
            result = sd.insert(RoomBarGoodsTable.TABLE_NAME, RoomBarGoodsTable.ROOM_NUM, cv);
//        }
        if (result < 0) Log.d(TAG, "addRoomBarGoodsTableItem - Error");
        return result;
    }

    public boolean updateRoomBarGoodSoldQuantity(long id, int newSoldQuantity) {
        boolean result;
        ContentValues cv = new ContentValues();
        cv.put(RoomBarGoodsTable.GDS_SOLD_QUANTITY, newSoldQuantity);
        SQLiteDatabase sd = getWritableDatabase();
        result = sd.update(RoomBarGoodsTable.TABLE_NAME, cv, RoomBarGoodsTable.ID + "=" +id, null)==1;
        return result;
    }

    public int clearRoomBarGoodSales() {
        int result;
        ContentValues cv = new ContentValues();
        cv.put(RoomBarGoodsTable.GDS_SOLD_QUANTITY, 0);
        SQLiteDatabase sd = getWritableDatabase();
        result = sd.update(RoomBarGoodsTable.TABLE_NAME, cv, "1", null);
        return result;
    }

    /* ****************************************** */                                                // ----- ##### -----


    public long getOperCount() {
        String query = "SELECT COUNT("+ OperTable.ID +") FROM " + OperTable.TABLE_NAME;
        return getTableCount(query);
    }

    public long getOperCount(String hotel, int floor) {
        String query = "SELECT COUNT("+ HotelRoomTable.ID +") FROM " + HotelRoomTable.TABLE_NAME +
                " WHERE " + HotelRoomTable.ID_HOTEL_STR + " = '" + hotel + "' AND " +
                HotelRoomTable.ROOM_FLOOR + " = " + floor;
        return getTableCount(query);
    }

    private boolean isOperPresent( int room) {
        String query = "SELECT "+ OperTable.ID +" FROM " +
                OperTable.TABLE_NAME + " WHERE " + OperTable.ROOM_NUM + " = " + room ;
        return isRecordPresent(query);
    }

    public long getOperCheckingCount() {
        String query = "SELECT COUNT("+ OperTable.ID +") FROM " + OperTable.TABLE_NAME +
                " WHERE " + OperTable.CHECK_ROOM + NOT_ZERO_CONDITION;

        return getTableCount(query);
    }

    public String getOperNeedsChecking() {
        String res = "";
        String query = "SELECT * FROM " + OperTable.TABLE_NAME +
                " WHERE " + OperTable.CHECK_ROOM + NOT_ZERO_CONDITION;
        OperRecord roomRecord;
        Cursor c = null;
        SQLiteDatabase sd = getWritableDatabase();
        try {
            c = sd.rawQuery(query, null);
            while (c.moveToNext()) {
                roomRecord = new OperRecord(c);
                if (roomRecord.getRoomNum() > 0) {
                    if (res.length() == 0)
                        res += roomRecord.getHotelId() + "/№" + roomRecord.getRoomNum();
                    else
                        res += "\n" + roomRecord.getHotelId() + "/№" + roomRecord.getRoomNum();
                }
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }

        return res;
    }

    public ArrayList<OperRecord> getOperArrayNeedsChecking() {                            // ----- ##### -----
        ArrayList<OperRecord> res = new ArrayList<>();
        String query = "SELECT * FROM " + OperTable.TABLE_NAME +
                " WHERE " + OperTable.CHECK_ROOM + NOT_ZERO_CONDITION;
        OperRecord roomRecord;
        Cursor c = null;
        SQLiteDatabase sd = getWritableDatabase();
        try {
            c = sd.rawQuery(query, null);
            while (c.moveToNext()) {
                roomRecord = new OperRecord(c);
                if (roomRecord.getRoomNum() > 0) {
                    res.add(roomRecord);
                    Log.d(TAG + "---", String.valueOf(roomRecord));
                }
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }

        return res;
    }
    //---link table HotelRoomTable, databases and use HotelRoomRecord---
    public long addOperItem(OperRecord roomRecord) {                                      // ----- ##### -----
        long result = 0;
        if (!isOperPresent( roomRecord.getRoomNum())) {
            ContentValues cv = new ContentValues();
           // room, check, callChambermaid, idHotel, inRoom , quit, surname
            cv.put(OperTable.ROOM_NUM, roomRecord.getRoomNum());
            cv.put(OperTable.CHECK_ROOM, roomRecord.isCheckRoom()?1:0);
            cv.put(OperTable.CALL_CHAMBERMAID, roomRecord.isCallChambermaid()?1:0);
            cv.put(OperTable.CHAMBERMAID_IN_ROOM, roomRecord.isChambermaidInRoom()?1:0);
            cv.put(OperTable.CHAMBERMAID_QUIT, roomRecord.isQuitChambermaid()?1:0);
            cv.put(OperTable.SURNAME, roomRecord.getSurname());
            cv.put(OperTable.NAME, roomRecord.getNameChambermaid());
            cv.put(OperTable.MODIFIED, roomRecord.isModified()?1:0);
            SQLiteDatabase sd = getWritableDatabase();
            result = sd.insert(OperTable.TABLE_NAME, OperTable.ROOM_NUM, cv);
        }
        if (result < 0) Log.d(TAG, "addHotelRoomTableItem - Error");
        return result;
    }




    /* ****************************************** */                                                // ----- ##### -----
    public long getHotelRoomCount() {
        String query = "SELECT COUNT("+ HotelRoomTable.ID +") FROM " + HotelRoomTable.TABLE_NAME;
        return getTableCount(query);
    }

    public long getHotelRoomCount(String hotel, int floor) {
        String query = "SELECT COUNT("+ HotelRoomTable.ID +") FROM " + HotelRoomTable.TABLE_NAME +
                " WHERE " + HotelRoomTable.ID_HOTEL_STR + " = '" + hotel + "' AND " +
                HotelRoomTable.ROOM_FLOOR + " = " + floor;
        return getTableCount(query);
    }

    private boolean isHotelRoomNumPresent(int hotelId, int room) {
        String query = "SELECT "+ HotelRoomTable.ID +" FROM " +
                HotelRoomTable.TABLE_NAME + " WHERE " + HotelRoomTable.ROOM_NUM + " = " + room +
                " AND " + HotelRoomTable.ID_HOTEL + "="+ hotelId;
        return isRecordPresent(query);
    }

    public long getHotelRoomNeedsCheckingCount() {
        String query = "SELECT COUNT("+ HotelRoomTable.ID +") FROM " + HotelRoomTable.TABLE_NAME +
            " WHERE " + HotelRoomTable.CHECK_ROOM + NOT_ZERO_CONDITION;

        return getTableCount(query);
    }

    public String getHotelRoomNeedsChecking() {
        String res = "";
        String query = "SELECT * FROM " + HotelRoomTable.TABLE_NAME +
                " WHERE " + HotelRoomTable.CHECK_ROOM + NOT_ZERO_CONDITION;
        HotelRoomRecord roomRecord;
        Cursor c = null;
        SQLiteDatabase sd = getWritableDatabase();
        try {
            c = sd.rawQuery(query, null);
            while (c.moveToNext()) {
                roomRecord = new HotelRoomRecord(c);
                if (roomRecord.getRoomNum() > 0) {
                    if (res.length() == 0)
                        res += roomRecord.getHotelName() + "/№" + roomRecord.getRoomNum();
                    else
                        res += "\n" + roomRecord.getHotelName() + "/№" + roomRecord.getRoomNum();
                }
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }

        return res;
    }

    public ArrayList<HotelRoomRecord> getHotelRoomArrayNeedsChecking() {                            // ----- ##### -----
        ArrayList<HotelRoomRecord> res = new ArrayList<>();
        String query = "SELECT * FROM " + HotelRoomTable.TABLE_NAME +
                " WHERE " + HotelRoomTable.CHECK_ROOM + NOT_ZERO_CONDITION;
        HotelRoomRecord roomRecord;
        Cursor c = null;
        SQLiteDatabase sd = getWritableDatabase();
        try {
            c = sd.rawQuery(query, null);
            while (c.moveToNext()) {
                roomRecord = new HotelRoomRecord(c);
                if (roomRecord.getRoomNum() > 0) {
                    res.add(roomRecord);
                    Log.d(TAG + "---", String.valueOf(roomRecord));
                }
            }
        } catch (Exception e) {
            if (D) Log.e(TAG, "Exception: " + e.getMessage());
        } finally {
            if (c != null) c.close();
        }

        return res;
    }
	//---link table HotelRoomTable, databases and use HotelRoomRecord---
    public long addHotelRoomItem(HotelRoomRecord roomRecord) {                                      // ----- ##### -----
        long result = 0;
        if (!isHotelRoomNumPresent(roomRecord.getHotelId(), roomRecord.getRoomNum())) {
            ContentValues cv = new ContentValues();
            cv.put(HotelRoomTable.ID_HOTEL, roomRecord.getHotelId());
            cv.put(HotelRoomTable.ID_HOTEL_STR, roomRecord.getHotelIdStr());
            cv.put(HotelRoomTable.HOTEL, roomRecord.getHotelName());
            cv.put(HotelRoomTable.ROOM_FLOOR, roomRecord.getRoomFloor());
            cv.put(HotelRoomTable.ROOM_NUM, roomRecord.getRoomNum());
            cv.put(HotelRoomTable.OCCUPIED, roomRecord.isRoomOccupied()?1:0);
            cv.put(HotelRoomTable.GUEST_IN_ROOM, roomRecord.isGuestInRoom()?1:0);
            cv.put(HotelRoomTable.SERVICE_NEEDED, roomRecord.isServiceNeeded()?1:0);
            cv.put(HotelRoomTable.TODAY_CLEARENCE, roomRecord.isTodayClearence()?1:0);
            cv.put(HotelRoomTable.DOOR, roomRecord.isDoor()?1:0);
            cv.put(HotelRoomTable.WINDOW, roomRecord.isWindow()?1:0);
            cv.put(HotelRoomTable.BALCONY, roomRecord.getBalcony());
            cv.put(HotelRoomTable.MODIFIED, roomRecord.isModified()?1:0);
            cv.put(HotelRoomTable.CHANGE_OF_BED_DATE, roomRecord.getChangeBedDateStr());
            cv.put(HotelRoomTable.DEPARTURE_DATE, roomRecord.getDepartureDateStr());
            cv.put(HotelRoomTable.TWIN, roomRecord.isTwin()?1:0);
            cv.put(HotelRoomTable.WATER_LEAKAGE, roomRecord.isWaterLeakage()?1:0);
            cv.put(HotelRoomTable.CHECK_ROOM, roomRecord.isCheckRoom()?1:0);
            cv.put(HotelRoomTable.RESERVATIONS_DATE, roomRecord.getReservationsDateStr());
            cv.put(HotelRoomTable.STATUS_NOM, roomRecord.isStatusNom()?1:0);
            cv.put(HotelRoomTable.REPAIR, roomRecord.isRepair()?1:0);
            cv.put(HotelRoomTable.NOTE_REPAIR, roomRecord.getNoteRepair());
            cv.put(HotelRoomTable.CHANGE_BED_EXTRA, roomRecord.getChangeBedExtra());
            cv.put(HotelRoomTable.TIPE_CLEARENCE, roomRecord.getTipeClearence());
            cv.put(HotelRoomTable.NOT_DISTURB, roomRecord.isNotDisturb());
            cv.put(HotelRoomTable.CALL_CHAMBERMAID, roomRecord.isCallChambermaid());
            cv.put(HotelRoomTable.CHAMBERMAID_IN_ROOM, roomRecord.isChambermaidInRoom());
            SQLiteDatabase sd = getWritableDatabase();
            result = sd.insert(HotelRoomTable.TABLE_NAME, HotelRoomTable.ROOM_NUM, cv);
        }
        if (result < 0) Log.d(TAG, "addHotelRoomTableItem - Error");
        return result;
    }

    public long addCallRoomItem(HotelRoomRecord roomRecord) {
        long result = 0;
        if (!isHotelRoomNumPresent(roomRecord.getHotelId(), roomRecord.getRoomNum())) {
            ContentValues cv = new ContentValues();
            cv.put(HotelRoomTable.ID_HOTEL, roomRecord.getHotelId());
            cv.put(HotelRoomTable.ROOM_NUM, roomRecord.getRoomNum());
            cv.put(HotelRoomTable.CHECK_ROOM, roomRecord.isCheckRoom()?1:0);
            cv.put(HotelRoomTable.CALL_CHAMBERMAID, roomRecord.isCallChambermaid());
            SQLiteDatabase sd = getWritableDatabase();
            result = sd.insert(HotelRoomTable.TABLE_NAME, HotelRoomTable.ROOM_NUM, cv);
        }
        if (result < 0) Log.d(TAG, "addHotelRoomTableItem - Error");
        return result;
    }

	public boolean updateHotelRoomService(boolean service, String idHotelStr, int roomNum) {        // ----- ##### -----
        boolean result = false;
        ContentValues cv = new ContentValues();
        cv.put(HotelRoomTable.SERVICE_NEEDED, service);
        SQLiteDatabase sd = getWritableDatabase();
        result = (sd.update(HotelRoomTable.TABLE_NAME, cv, HotelRoomTable.ID_HOTEL_STR +
                "='" + idHotelStr +"' AND " +HotelRoomTable.ROOM_NUM + "=" + roomNum, null) == 1);
        return result;
    }

    /* ****************************************** */
	public long getPlacementCount() {
		String query = "SELECT COUNT("+ PlacementTable.ID +") FROM " + PlacementTable.TABLE_NAME;
		return getTableCount(query);
	}

	public int getPlacementFloorCount(int idHotel) {
		String query = "SELECT COUNT("+ PlacementTable.ID +") FROM " + PlacementTable.TABLE_NAME + " WHERE " +
				PlacementTable.ID_ENT + "=" + idHotel + " AND " +
				PlacementTable.TTYPE + "=" + PlacementRecord.PLACE_FLOOR;
		return (int) getTableCount(query);
	}

	private Boolean isPlacementIdPresent(long extId) {
		Boolean result = true;
		if (extId > 0) {
			String query = "SELECT "+ PlacementTable.ID +" FROM " +
					PlacementTable.TABLE_NAME + " WHERE " + PlacementTable.ID_EXTERNAL + " = " + extId;
			result = isRecordPresent(query);
		}
		return result;
	}

	private Boolean isFloorPresent(int idEnt, int idExt) {
		Boolean result = true;
		String query = "SELECT "+ PlacementTable.ID +" FROM " +
				PlacementTable.TABLE_NAME + " WHERE " + PlacementTable.ID_EXTERNAL + " = " + idExt +
				" AND " + PlacementTable.ID_ENT + "=" + idEnt;
		result = isRecordPresent(query);
		return result;
	}

	public long addPlacementItem(long extId, long idEnt, int idParent, int ttype,
			String name, String remark) {
		long result = 0;
		if (!isPlacementIdPresent(extId)) {
			ContentValues cv = new ContentValues();
			cv.put(PlacementTable.ID_EXTERNAL, extId);
			cv.put(PlacementTable.ID_ENT, idEnt);
			cv.put(PlacementTable.ID_PARENT, idParent);
			cv.put(PlacementTable.TTYPE, ttype);
			cv.put(PlacementTable.NAME, name);
			cv.put(PlacementTable.REMARK, remark);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.insert(PlacementTable.TABLE_NAME, PlacementTable.NAME, cv);
		}
		if (result < 0) Log.d(TAG, "addPlacementItem - Error");
		return result;
	}

	public long addPlacementItem(int idEnt, int idExt, int idParent, int ttype, String name) {
		long result = 0;
		if (!isFloorPresent(idEnt, idExt)) {
			ContentValues cv = new ContentValues();
			cv.put(PlacementTable.ID_EXTERNAL, idExt);
			cv.put(PlacementTable.ID_ENT, idEnt);
			cv.put(PlacementTable.ID_PARENT, idParent);
			cv.put(PlacementTable.TTYPE, ttype);
			cv.put(PlacementTable.NAME, name);
			cv.put(PlacementTable.REMARK, "");
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.insert(PlacementTable.TABLE_NAME, PlacementTable.NAME, cv);
		}
		if (result < 0) Log.d(TAG, "addPlacementItem - Error");
		return result;
	}

	/* ****************************************** */
	public long getZToolCount() {
		String query = "SELECT COUNT("+ ZToolTable.ID +") FROM " + ZToolTable.TABLE_NAME;
		return getTableCount(query);
	}

	private boolean isZToolIdPresent(int extId) {
		Boolean result = true;
		if (extId > 0) {
			String query = "SELECT "+ ZToolTable.ID +" FROM " +
	    			ZToolTable.TABLE_NAME + " WHERE " + ZToolTable.ID_EXTERNAL + " = " + extId;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long addZToolItem(int extId, int idEnt, double vat) {
		long result = 0;
		if (!isZToolIdPresent(extId)) {
			ContentValues cv = new ContentValues();
			cv.put(ZToolTable.ID_EXTERNAL, extId);
			cv.put(ZToolTable.ID_ENT, idEnt);
			cv.put(ZToolTable.VAT, vat);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.insert(ZToolTable.TABLE_NAME, ZToolTable.ID_ENT, cv);
		}
		if (result < 0) Log.d(TAG, "addZToolItem - Error");
		return result;
	}

	public long getStateCount() {
		String query = "SELECT COUNT("+ StateTable.ID +") FROM " + StateTable.TABLE_NAME;
		return getTableCount(query);
	}

	private boolean isStateIdPresent(int extId) {
		Boolean result = true;
		if (extId > 0) {
			String query = "SELECT "+ StateTable.ID +" FROM " +
	    			StateTable.TABLE_NAME + " WHERE " + StateTable.ID_EXTERNAL + " = " + extId;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addStateItem(int extId, int idDoc, /*int no,*/ String itemName) {
		long result = 0;
		if (!isStateIdPresent(extId)) {
			ContentValues cv = new ContentValues();
			cv.put(StateTable.ID_EXTERNAL, extId);
			cv.put(StateTable.ID_DOC, idDoc);
//			cv.put(StateTable.NO, no);
			cv.put(StateTable.NM, itemName);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.insert(StateTable.TABLE_NAME, StateTable.NM, cv);
		}
		if (result < 0) Log.d(TAG, "addStateItem - Error");
		return result;
	}
	
	public String getStateName(int extId) {
		String result = "";
		if (extId > 0) {
			String query = "SELECT "+ StateTable.NM +" FROM " +
	    			StateTable.TABLE_NAME + " WHERE " + StateTable.ID_EXTERNAL + " = " + extId;
			SQLiteDatabase sd = getReadableDatabase();
			Cursor c = null;
			try {
		    	c = sd.rawQuery(query, null);
		    	if (c.moveToNext()) {
		    		int colid = c.getColumnIndex(StateTable.NM);
		    		result = c.getString(colid); 
		    	}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getStateName exception: " + e.getMessage());
			} 					
		}		
		return result;
	}
	
	/* ****************************************** */
	private Boolean isUserIdPresent(long extId) {
		Boolean result = true;
		if (extId > 0) {
			String query = "SELECT "+ UserTable.ID +" FROM " +
	    			UserTable.TABLE_NAME + " WHERE " + UserTable.ID_EXTERNAL + " = " + extId;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long addUserItem(long extId, int adm_id, String surname, String name, String patronimic) {
		long result = 0;
		if (!isUserIdPresent(extId)) {
			ContentValues cv = new ContentValues();
			cv.put(UserTable.ID_EXTERNAL, extId);
			cv.put(UserTable.ID_ADM, adm_id);
			cv.put(UserTable.SURNAME, surname);
			cv.put(UserTable.NAME, name);
			cv.put(UserTable.PATRONIMIC, patronimic);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.insert(UserTable.TABLE_NAME, UserTable.SURNAME, cv);
		} else {
			ContentValues cv = new ContentValues();
			cv.put(UserTable.ID_ADM, adm_id);
			cv.put(UserTable.SURNAME, surname);
			cv.put(UserTable.NAME, name);
			cv.put(UserTable.PATRONIMIC, patronimic);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.update(UserTable.TABLE_NAME, cv, UserTable.ID_EXTERNAL + "=" + extId, null);			
		}
		if (result < 0) Log.d(TAG, "addUserItem - Error");
		return result;
	}

	public long addUserItem(long extId) {
		long result = 0;
		if (!isUserIdPresent(extId)) {
			ContentValues cv = new ContentValues();
			cv.put(UserTable.ID_EXTERNAL, extId);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.insert(UserTable.TABLE_NAME, UserTable.ID_EXTERNAL, cv);
		}
		if (result < 0) Log.d(TAG, "addUserItem - Error");
		return result;
	}
	
	public boolean updUserLastZEnt(long extId, int entPosition) {
		boolean result = false;
		try {
			ContentValues cv = new ContentValues();
			cv.put(UserTable.LAST_Z_ENT, entPosition);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.update(UserTable.TABLE_NAME, cv, UserTable.ID_EXTERNAL + "=" + extId, null) == 1;
		} catch (Exception e) {
			if (D) Log.e(TAG, "addUserLastZEnt exception = " + e.getMessage());
		}
		if (!result) Log.d(TAG, "addUserLastZEnt - Error");
		return result;
	}
	
	public long getUserCount() {
		String query = "SELECT COUNT("+ UserTable.ID +") FROM " + UserTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public long getUserId() {
		String query = "SELECT "+ UserTable.ID_EXTERNAL +" FROM " + UserTable.TABLE_NAME;
		return getTableCount(query);		
	}

	/* ****************************************** */
	public long getLogsCount() {
		String query = "SELECT COUNT("+ LogsTable.ID +") FROM " + LogsTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public long addLogItem(int level, Date m_date, String msg) {
		java.sql.Date sqlDate = new java.sql.Date(m_date.getTime());
		long result = 0;
		ContentValues cv = new ContentValues();
		cv.put(LogsTable.LEVEL, level);
		cv.put(LogsTable.MSG_DATE, dateFormatMSYY.format(sqlDate));			
		cv.put(LogsTable.MSG, msg);

		SQLiteDatabase sd = getWritableDatabase();
		result = sd.insert(LogsTable.TABLE_NAME, LogsTable.MSG, cv);
		if (result < 0) Log.d(TAG, "addLogItem - Error");
		return result;
	}
	
	public String[] getDistinctLogsDates() {
    	Cursor c;
		String[] dates = null;
		String logDate;
		String query = "SELECT DISTINCT SUBSTR("+ LogsTable.MSG_DATE + ",1,10)AS "+ LogsTable.MSG_DATE +
    			" FROM " + LogsTable.TABLE_NAME;
		SQLiteDatabase sd = getWritableDatabase();

		try {
			int cntr = 0;
	    	c = sd.rawQuery(query, null);
	    	dates = new String[c.getCount()];
	    	while (c.moveToNext()) {
	    		int colid = c.getColumnIndex(LogsTable.MSG_DATE);
	    		logDate = c.getString(colid);
	         	java.util.Date date;
	         	try {
	         		date = dateFormatDayYY.parse(logDate);
		    		dates[cntr] = dateFormatDay.format(date);
		    		cntr++;
	         	} catch (ParseException e) {
	     			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	         	}
	    	}
	    	return dates;
		} catch(Exception e) {
			if (D) Log.e(TAG, "getDistinctLogsDates exception = " + e.getMessage());
		}
    	return dates;
	}
	//---Log doing methods use java.util.Date date---
	public void clearLogs(String logDate) {
		java.util.Date date;
		String sWhere = null, logDateYY;

		if (logDate != null) {
	     	try {
	     		date = dateFormatDay.parse(logDate);
	     		logDateYY = dateFormatDayYY.format(date);
	     		sWhere = "SUBSTR("+ LogsTable.MSG_DATE + ",1,10) = \""+ logDateYY + "\"";
	     	} catch (ParseException e) {
	 			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	     	}
		} else
			sWhere = "1";
		if (sWhere != null) {
			SQLiteDatabase sd = getWritableDatabase();
			sd.delete(LogsTable.TABLE_NAME, sWhere, null);
		}
	}

	public void clearOldLogs(String logDate) {
		java.util.Date date;
		String sWhere = null, logDateYY;
		if (logDate != null) {
	     	try {
	     		date = dateFormatDay.parse(logDate);
	     		logDateYY = dateFormatDayYY.format(date);
	     		sWhere = "SUBSTR("+ LogsTable.MSG_DATE + ",1,10) <= \""+ logDateYY + "\"";
	     	} catch (ParseException e) {
	 			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	     	}
		} else
			sWhere = "1";
		if (sWhere != null) {
			try {
				SQLiteDatabase sd = getWritableDatabase();
				sd.delete(LogsTable.TABLE_NAME, sWhere, null);
			} catch (Exception e) {
				if (D) Log.e(TAG, "clearOldLogs exception = " + e.getMessage());
			}
		}
	}

	public int convertLogDateFormat() {
		String query = "SELECT "+ LogsTable.MSG_DATE + ", " + LogsTable.ID +
				" FROM " + LogsTable.TABLE_NAME;
    	SQLiteDatabase sqdb = getReadableDatabase();
    	Cursor c = null;
    	String dateStr;
    	int cntr = 0, id;
		try {
			c = sqdb.rawQuery(query, null);
	    	while (c.moveToNext()) {
	    		int colid = c.getColumnIndex(LogsTable.ID);
	    		id = c.getInt(colid);
	    		colid = c.getColumnIndex(LogsTable.MSG_DATE);
	    		dateStr = c.getString(colid);
	         	java.util.Date date;
	         	try {
	         		date = dateFormat.parse(dateStr); 
	         		updateLogDate(id, date);
	         		cntr++;
	         	} catch (ParseException e) {
	     			if (D) Log.e(TAG, "Exception: " + e.getMessage());
	         	}		    		    				   
	    	}
		} catch (Exception e) {
			if (D) Log.e(TAG, "Exception: " + e.getMessage());
		} finally {
	    	if (c != null) c.close();
		}
		return cntr;
	}
	
	public boolean updateLogDate(long id, Date m_date) {
		java.sql.Date sqlDate = new java.sql.Date(m_date.getTime());
		Boolean result = false;
		try {
			ContentValues cv = new ContentValues();
			cv.put(LogsTable.MSG_DATE, dateFormatYY.format(sqlDate));
			cv.put(LogsTable.MODIFIED, 1);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.update(LogsTable.TABLE_NAME, cv, LogsTable.ID + "=" + id, null) == 1;
		} catch (Exception e) {
			if (D) Log.e(TAG, "updateLogDate exception = " + e.getMessage());
		}
		return result;		
	}




	/* ****************************************** */
	private Boolean isEmpIdPresent(long extId) {
		Boolean result = true;
		if (extId > 0) {
			String query = "SELECT "+ EmpTable.ID +" FROM " +
	    			EmpTable.TABLE_NAME + " WHERE " + EmpTable.ID_EXTERNAL + " = " + extId;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long addEmpItem(long extId, long idEnt, String surname, String name, String patronimic) {
		long result = 0;
		if (!isEmpIdPresent(extId)) {
			ContentValues cv = new ContentValues();
			cv.put(EmpTable.ID_EXTERNAL, extId);
			cv.put(EmpTable.ID_ENT, idEnt);
			cv.put(EmpTable.SURNAME, surname);
			cv.put(EmpTable.NAME, name);
			cv.put(EmpTable.PATRONIMIC, patronimic);
            cv.put(EmpTable.ID_JOB, EmpRecord.MAID_EMP);
			SQLiteDatabase sd = getWritableDatabase();
			
			result = sd.insert(EmpTable.TABLE_NAME, EmpTable.SURNAME, cv);
		}
		if (result < 0) Log.d(TAG, "addEmpItem - Error");
		return result;
	}

	public long addEmpItem(EmpRecord empRec) {
		long result = 0;
		if (!isEmpIdPresent(empRec.getIdExternal())) {
			ContentValues cv = new ContentValues();
			cv.put(EmpTable.ID_EXTERNAL, empRec.getIdExternal());
            cv.put(EmpTable.ID_EXT_STR, empRec.getIdExtStr());
			cv.put(EmpTable.SURNAME, empRec.getSurname());
            cv.put(EmpTable.ID_JOB, empRec.getIdJob());
//			cv.put(EmpTable.NAME, name);
//			cv.put(EmpTable.PATRONIMIC, patronimic);
			SQLiteDatabase sd = getWritableDatabase();

			result = sd.insert(EmpTable.TABLE_NAME, EmpTable.SURNAME, cv);
		}
		if (result < 0) Log.d(TAG, "addEmpItem - Error");
		return result;
	}

	public long getEmpsCount() {
		String query = "SELECT COUNT("+ EmpTable.ID +") FROM " + EmpTable.TABLE_NAME;
		return getTableCount(query);
	}	
		
	public long getEntEmpsCount(int idEnt) {
		String query = "SELECT COUNT("+ EmpTable.ID +") FROM " + EmpTable.TABLE_NAME +
				" WHERE " + EmpTable.ID_ENT + " = " + idEnt;
		return getTableCount(query);
	}	
		
	public Cursor getEmpName(int extId) {
		Cursor result = null;
		if(extId > 0){
			SQLiteDatabase sd = getReadableDatabase();
			try {
		    	result = sd.rawQuery("SELECT * FROM " +  
		    			EmpTable.TABLE_NAME + " WHERE " + EmpTable.ID_EXTERNAL + " = " + extId, null);
				if (!result.moveToNext()){
					result = null;
			}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getEmpName exception = " + e.getMessage());
			} 
		}				
		return result;
	}
	
	
	/* ****************************************** */
	public long getGdsCount() {
		String query = "SELECT COUNT("+ GoodsTable.ID +") FROM " + GoodsTable.TABLE_NAME;
		return getTableCount(query);
	}

	private Boolean isGdsIdPresent(long extId) {
		Boolean result = true;
		if (extId > 0) {
			String query = "SELECT "+ GoodsTable.ID +" FROM " +
	    			GoodsTable.TABLE_NAME + " WHERE " + GoodsTable.ID_EXTERNAL + " = " + extId;
			result = isRecordPresent(query);
		}
		return result;
	}

	// WRAPPER METHOD FOR ADDING AN ITEM
	public long addGdsItem(long extId, int parent_id, int ttype, int gdsType, 
			String itemName, boolean checkPresent) {
		long result = 0;
		// CREATE A CONTENTVALUE OBJECT		
		ContentValues cv = new ContentValues();
		cv.put(GoodsTable.ID_EXTERNAL, extId);
		cv.put(GoodsTable.ID_PARENT, parent_id);
		cv.put(GoodsTable.TTYPE, ttype);
		cv.put(GoodsTable.GDSTYPE, gdsType);
		cv.put(GoodsTable.NM, itemName);
		// RETRIEVE WRITEABLE DATABASE AND INSERT
		SQLiteDatabase sd = getWritableDatabase();
		if (!checkPresent || !isGdsIdPresent(extId)) {			
			result = sd.insert(GoodsTable.TABLE_NAME, GoodsTable.NM, cv);
		} else {
			result = sd.update(GoodsTable.TABLE_NAME, cv, GoodsTable.ID_EXTERNAL + "=" + extId, null);		
		}
		if (result < 0) Log.d(TAG, "addGdsItem - Error");
		return result;
	}
	
	// METHOD FOR SAFELY REMOVING AN ITEM 
	public boolean removeGdsItem(int gdsId) {
		SQLiteDatabase sd = getWritableDatabase();
		String[] whereArgs = new String[] { String.valueOf(gdsId) };
		int result = sd.delete(GoodsTable.TABLE_NAME, GoodsTable.ID + "= ? ", whereArgs);
		return (result > 0);
	}
	
	public Cursor getGdsName(int extId) {
		Cursor result = null;
		if (extId > 0) {
			SQLiteDatabase sd = getReadableDatabase();
			try {
		    	result = sd.rawQuery("SELECT "+ GoodsTable.NM +" FROM " + 
		    			GoodsTable.TABLE_NAME + " WHERE " + GoodsTable.ID_EXTERNAL + " = " + extId, null);
				if (!result.moveToNext()){
					result = null;
			}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getGoodsName exception = " + e.getMessage());
			} 
		}				
		return result;
	}
	
	/* ****************************************** */
	public long getEntCount() {
		String query = "SELECT COUNT("+ EntTable.ID +") FROM " + EntTable.TABLE_NAME;
		return getTableCount(query);
	}
	
	public Cursor getEntName(int extId) {
		Cursor result = null;
		String q_ent_p = "(SELECT "+ EntTable.NM + " FROM "+ EntTable.TABLE_NAME +
				" WHERE "+ EntTable.ID_EXTERNAL +  " = e."+ EntTable.ID_PARENT +")as "+ EntRecord.ENT_PARENT;
		String query;
		if (extId > 0) {
			SQLiteDatabase sd = getReadableDatabase();
			try {
				query = "SELECT e."+ EntTable.ID_EXTERNAL + ", e." + EntTable.NM + ", e." + EntTable.ADDR + ", e." + 
		    			EntTable.LAT + ", e." + EntTable.LNG  + "," + q_ent_p + " FROM " +
		    			EntTable.TABLE_NAME + " e WHERE e." + EntTable.ID_EXTERNAL + " = " + extId;
		    	result = sd.rawQuery(query, null);
				if (!result.moveToNext()){
					result = null;
				}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getEntName exception = " + e.getMessage());
			} 
		}				
		return result;
	}
	
	public String getEnt_Name(int extId) {
		String result = "";
		Cursor c = null;
		if (extId > 0) {
			SQLiteDatabase sd = getReadableDatabase();
			try {
		    	c = sd.rawQuery("SELECT "+ EntTable.ID_EXTERNAL + "," + EntTable.NM + "," + EntTable.ADDR + "," + 
		    			EntTable.LAT + "," + EntTable.LNG +" FROM " + 
		    			EntTable.TABLE_NAME + " WHERE " + EntTable.ID_EXTERNAL + " = " + extId, null);
				if (c.moveToNext()){
		    		int colid = c.getColumnIndex(EntTable.NM);
					result = c.getString(colid);
				}
			} catch(Exception e) {
				if (D) Log.e(TAG, "getEnt_Name exception = " + e.getMessage());
			} 
		}				
		return result;
	}
	
	private Boolean isEntIdPresent(long extId) {
		Boolean result = true;
		if (extId > 0) {
			String query = "SELECT "+ EntTable.ID +" FROM " +
	    			EntTable.TABLE_NAME + " WHERE " + EntTable.ID_EXTERNAL + " = " + extId;
			result = isRecordPresent(query);
		}
		return result;
	}
	
	public long addEntItem(long extId, int parent_id, double lat, double lng, String Addr,
			String itemName, boolean checkPresent) {
		long result = 0;
		ContentValues cv = new ContentValues();
		cv.put(EntTable.ID_EXTERNAL, extId);
		cv.put(EntTable.ID_PARENT, parent_id);
		cv.put(EntTable.LAT, lat);
		cv.put(EntTable.LNG, lng);
		cv.put(EntTable.ADDR, Addr);
		cv.put(EntTable.NM, itemName);
		SQLiteDatabase sd = getWritableDatabase();
		try {
			if (!checkPresent || !isEntIdPresent(extId)) {
				result = sd.insert(EntTable.TABLE_NAME, EntTable.NM, cv);
			} else {
				result = sd.update(EntTable.TABLE_NAME, cv, EntTable.ID_EXTERNAL + "=" + extId, null);
			}
		} catch(Exception e) {
			if (D) Log.e(TAG, "exception = " + e.getMessage());
		}
		if (result < 0) Log.d(TAG, "addEntItem - Error");
		return result;
	}

	public long addEntItem(int extId, String idStr, String itemName) {
		long result = 0;
		ContentValues cv = new ContentValues();
		cv.put(EntTable.ID_EXTERNAL, extId);
        cv.put(EntTable.ID_EXT_STR, idStr);
		cv.put(EntTable.ID_PARENT, 0);
		cv.put(EntTable.LAT, 0);
		cv.put(EntTable.LNG, 0);
		cv.put(EntTable.ADDR, "");
		cv.put(EntTable.NM, itemName);
		SQLiteDatabase sd = getWritableDatabase();
		try {
			if (!isEntIdPresent(extId)) {
				result = sd.insert(EntTable.TABLE_NAME, EntTable.NM, cv);
			} else {
				result = sd.update(EntTable.TABLE_NAME, cv, EntTable.ID_EXTERNAL + "=" + extId, null);
			}
		} catch(Exception e) {
			if (D) Log.e(TAG, "exception = " + e.getMessage());
		}
		if (result < 0) Log.d(TAG, "addEntItem - Error");
		return result;
	}

	public Boolean setEntCoords(long extId, double lat, double lng) {
		boolean result = false;

		ContentValues cv = new ContentValues();
		cv.put(EntTable.LAT, lat);
		cv.put(EntTable.LNG, lng);
		SQLiteDatabase sd = getWritableDatabase();
		result = sd.update(EntTable.TABLE_NAME, cv, EntTable.ID_EXTERNAL + "=" + extId, null) == 1;
		return result;
	}	

	/* ****************************************** */
	private boolean isInvoiceIdPresent(long extId) {
		boolean result = true;
		if (extId > 0) {
			String query = "SELECT "+ InvoiceTable.ID +" FROM " +
	    		InvoiceTable.TABLE_NAME + " WHERE " + InvoiceTable.ID_EXTERNAL + " = " + extId;			
			result = isRecordPresent(query);
		}
		return result;
	}

	private boolean isInvoiceNumPresent(String num) {
		boolean result = true;
		if (num != null) {
			String query = "SELECT "+ InvoiceTable.ID +" FROM " +
	    		InvoiceTable.TABLE_NAME + " WHERE " + InvoiceTable.NUM + " = \"" + num + "\"";
			result = isRecordPresent(query);
		}
		return result;
	}

	public boolean isInvoiceContMarkPresent(long idInvoice) {
		boolean result = false;
		Cursor c = null;
		if (idInvoice > 0) {
			String query = "SELECT count("+ InvoiceContTable.ID +") FROM " +
	    		InvoiceContTable.TABLE_NAME + " WHERE " + InvoiceContTable.INVOICE_ID + " = " + idInvoice +
	    		" AND " + InvoiceContTable.MARK + " > 0 AND " + InvoiceContTable.MODIFIED + " > 0";
			
			SQLiteDatabase sd = getReadableDatabase();
			try {
		    	c = sd.rawQuery(query, null);
				if (c.moveToNext()){
		    		//int colid = c.getColumnIndex(EntTable.NM);
		    		int cnt = c.getInt(0); 
					result = cnt > 0;
				}
			} catch(Exception e) {
				if (D) Log.e(TAG, "isInvoiceContMarkPresent exception = " + e.getMessage());
			} 
		}
		return result;
	}

	public long _addInvoiceItem(long extId, String num, int idDoc, int state, Date d_open,
			int idOwner, int idWareHouse, int idWareHouse2, int empId, Date changeDate) {
		java.sql.Date sqlChangeDate, sqlDate = new java.sql.Date(d_open.getTime());
		sqlChangeDate = new java.sql.Date(changeDate.getTime());
		long result = 0;
		try {
			if ((idDoc == InvoiceRecord.INVOICE_INCOME_DOC && !isInvoiceNumPresent(num))||
					(idDoc != InvoiceRecord.INVOICE_INCOME_DOC)) {
				ContentValues cv = new ContentValues();
				cv.put(InvoiceTable.ID_EXTERNAL, extId);
				cv.put(InvoiceTable.NUM, num);
				cv.put(InvoiceTable.IDATE, dateFormatYY.format(sqlDate));
				cv.put(InvoiceTable.DOC_ID, idDoc);
				cv.put(InvoiceTable.STATE_ID, state);
				cv.put(InvoiceTable.OWNER_ID, idOwner);
				cv.put(InvoiceTable.PLACEMENT_ID, idWareHouse);
				cv.put(InvoiceTable.PLACEMENT2_ID, idWareHouse2);
				cv.put(InvoiceTable.EMP_ID, empId);
				cv.put(InvoiceTable.DATE_CHANGE, dateFormatYY.format(sqlChangeDate));
				// RETRIEVE WRITEABLE DATABASE AND INSERT
				SQLiteDatabase sd = getWritableDatabase();
				
				result = sd.insert(InvoiceTable.TABLE_NAME, InvoiceTable.NUM, cv);
			}
		} catch (Exception e) {
			if (D) Log.e(TAG, "_addInvoiceItem exception = " + e.getMessage());
		}		
		if (result < 0) Log.d(TAG, "addInvoiceItem - Error");
		return result;
	}
	
	public long addInvoiceItem(long extId, String num, int idDoc, int state, Date d_open,
			int idOwner, int idWareHouse, int idWareHouse2, int empId, Date changeDate) {
		java.sql.Date sqlChangeDate, sqlDate = new java.sql.Date(d_open.getTime());
		sqlChangeDate = new java.sql.Date(changeDate.getTime());
		long result = 0;
		if (extId >= 0) {
			if (!isInvoiceIdPresent(extId)) {
				ContentValues cv = new ContentValues();
				cv.put(InvoiceTable.ID_EXTERNAL, extId);
				cv.put(InvoiceTable.NUM, num);
				cv.put(InvoiceTable.IDATE, dateFormatYY.format(sqlDate));
				cv.put(InvoiceTable.DOC_ID, idDoc);
				cv.put(InvoiceTable.STATE_ID, state);
				cv.put(InvoiceTable.OWNER_ID, idOwner);
				cv.put(InvoiceTable.PLACEMENT_ID, idWareHouse);
				cv.put(InvoiceTable.PLACEMENT2_ID, idWareHouse2);
				cv.put(InvoiceTable.EMP_ID, empId);
				cv.put(InvoiceTable.DATE_CHANGE, dateFormatYY.format(sqlChangeDate));
				// RETRIEVE WRITEABLE DATABASE AND INSERT
				SQLiteDatabase sd = getWritableDatabase();
				
				result = sd.insert(InvoiceTable.TABLE_NAME, InvoiceTable.NUM, cv);
			} else {
				// update
			}
		}
		if (result < 0) Log.d(TAG, "addInvoiceItem - Error");
		return result;
	}
	
	public boolean updateInvoiceItem(int id, String num, int idDoc, int state, Date d_open,
			int idOwner, int idWareHouse, int idWareHouse2, int empId, Date changeDate) {
		java.sql.Date sqlChangeDate, sqlDate = new java.sql.Date(d_open.getTime());
		sqlChangeDate = new java.sql.Date(changeDate.getTime());
		boolean result = false;
		try {
			ContentValues cv = new ContentValues();
			cv.put(InvoiceTable.NUM, num);
			cv.put(InvoiceTable.IDATE, dateFormatYY.format(sqlDate));
			cv.put(InvoiceTable.DOC_ID, idDoc);
			cv.put(InvoiceTable.STATE_ID, state);
			cv.put(InvoiceTable.OWNER_ID, idOwner);
			cv.put(InvoiceTable.PLACEMENT_ID, idWareHouse);
			cv.put(InvoiceTable.PLACEMENT2_ID, idWareHouse2);
			cv.put(InvoiceTable.EMP_ID, empId);
			cv.put(InvoiceTable.DATE_CHANGE, dateFormatYY.format(sqlChangeDate));
			// RETRIEVE WRITEABLE DATABASE AND INSERT
			SQLiteDatabase sd = getWritableDatabase();
			
			result = sd.update(InvoiceTable.TABLE_NAME, cv, InvoiceTable.ID + "=" + id, null) == 1;
		} catch (Exception e) {
			if (D) Log.e(TAG, "updateInvoiceItem exception = " + e.getMessage());
		}
		return result;
	}
	
	public long getInvoiceContCount(long invoice_id) {
		String query = "SELECT COUNT("+ InvoiceContTable.ID +") FROM " + InvoiceContTable.TABLE_NAME +
				" WHERE " + InvoiceContTable.INVOICE_ID + " = " + invoice_id;
		if (invoice_id > 0) return getTableCount(query);		
		else return 0;
	}

	public boolean updateInvoiceExtId(int id, long id_external) {
		boolean result = false;
		if (id >= 0) {
			ContentValues cv = new ContentValues();
			cv.put(InvoiceTable.ID_EXTERNAL, id_external);
			cv.put(InvoiceTable.MODIFIED, 0);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.update(InvoiceTable.TABLE_NAME, cv, InvoiceTable.ID + "=" + id, null) == 1;
		}
		if (!result) Log.d(TAG, "updateInvoiceContItem - Error");
		return result;
	}
	
	public Boolean removeInvoice(long id) {
		Boolean result = false;
		try {
			if (id > 0) {
				if (removeAllInvoiceConts(id)) {
					SQLiteDatabase sd = getWritableDatabase();
					result = sd.delete(InvoiceTable.TABLE_NAME, InvoiceTable.ID + " = " + id, null) > 0;
				}
			}
		} catch (Exception e) {
			if (D) Log.e(TAG, "removeInvoice exception = " + e.getMessage());
		}
		return result;
	}
	
	public Boolean removeAllInvoiceConts(long id) {
		Boolean result = false;
		try {
			if (id > 0) {
				SQLiteDatabase sd = getWritableDatabase();
				result = sd.delete(InvoiceContTable.TABLE_NAME, InvoiceContTable.INVOICE_ID + " = " + id, null) >= 0;
			}
		} catch (Exception e) {
			if (D) Log.e(TAG, "removeInvoice exception = " + e.getMessage());
		}
		return result;
	}

	/* ****************************************** */	
	private boolean isInvoiceContIdGdsPresent(long invoice_id, int gds_id) {
		boolean result = true;
		String query = "SELECT "+ InvoiceContTable.ID +" FROM " +
    		InvoiceContTable.TABLE_NAME + " WHERE " + InvoiceContTable.INVOICE_ID + " = " + invoice_id +
    		" AND " + InvoiceContTable.GDS_ID + " = " + gds_id;			
		result = isRecordPresent(query);
		return result;
	}
	
	public long addInvoiceContItem(long invoice_id, long id_external, int idGds, int icNum, double gdsCount) {
		long result = 0;
		double gdsCnt = 0;
		int idCont = 0;
		if (invoice_id >= 0) {
			String query = "SELECT "+ InvoiceContTable.GDS_COUNT +", "+ InvoiceContTable.ID +" FROM " +
		    		InvoiceContTable.TABLE_NAME + " WHERE " + InvoiceContTable.INVOICE_ID + " = " + invoice_id +
		    		" AND " + InvoiceContTable.GDS_ID + " = " + idGds;			
			SQLiteDatabase sd = getWritableDatabase();
			Cursor c = null;
			try {
		    	c = sd.rawQuery(query, null);
		    	if (c.moveToNext()) {
		    		int colid = c.getColumnIndex(InvoiceContTable.ID);
		    		idCont = c.getInt(colid);
		    		colid = c.getColumnIndex(InvoiceContTable.GDS_COUNT);
		    		gdsCnt = c.getDouble(colid); 
		    	}
			} catch(Exception e) {
				if (D) Log.e(TAG, "addInvoiceContItem exception: " + e.getMessage());
			} 					
						
			if (idCont <= 0) {
				ContentValues cv = new ContentValues();
				cv.put(InvoiceContTable.ID_EXTERNAL, id_external);
				cv.put(InvoiceContTable.INVOICE_ID, invoice_id);
				cv.put(InvoiceContTable.GDS_ID, idGds);
				cv.put(InvoiceContTable.GDS_COUNT, gdsCount);
				cv.put(InvoiceContTable.IC_NUM, icNum);
				cv.put(InvoiceContTable.MODIFIED, 1);			
				result = sd.insert(InvoiceContTable.TABLE_NAME, InvoiceContTable.GDS_ID, cv);
			} else {
				// update
				ContentValues cv = new ContentValues();
				cv.put(InvoiceContTable.GDS_COUNT, gdsCnt + gdsCount);
				cv.put(InvoiceContTable.MODIFIED, 1);
				// RETRIEVE WRITEABLE DATABASE AND INSERT
				result = sd.update(InvoiceContTable.TABLE_NAME, cv, InvoiceContTable.ID + "=" + idCont, null);
			}
		}
		if (result < 0) Log.d(TAG, "addInvoiceContItem - Error");
		return result;
	}

	public boolean updateInvoiceContItem(int id, double gdsCount) {
		boolean result = false;
		if (id >= 0) {
			ContentValues cv = new ContentValues();
			cv.put(InvoiceContTable.GDS_COUNT, gdsCount);
			cv.put(InvoiceContTable.MODIFIED, 1);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.update(InvoiceContTable.TABLE_NAME, cv, InvoiceContTable.ID + "=" + id, null) == 1;
		}
		if (!result) Log.d(TAG, "updateInvoiceContItem - Error");
		return result;
	}

	public boolean updateInvoiceContExtId(int id, long id_external) {
		boolean result = false;
		if (id >= 0) {
			ContentValues cv = new ContentValues();
			cv.put(InvoiceContTable.ID_EXTERNAL, id_external);
			cv.put(InvoiceContTable.MODIFIED, 0);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.update(InvoiceContTable.TABLE_NAME, cv, InvoiceContTable.ID + "=" + id, null) == 1;
		}
		if (!result) Log.d(TAG, "updateInvoiceContItem - Error");
		return result;
	}
	
	public boolean updateInvoiceContClearMark(int id) {
		boolean result = false;
		if (id >= 0) {
			ContentValues cv = new ContentValues();
			cv.put(InvoiceContTable.MARK, 0);
			cv.put(InvoiceContTable.MODIFIED, 0);
			SQLiteDatabase sd = getWritableDatabase();
			result = sd.update(InvoiceContTable.TABLE_NAME, cv, InvoiceContTable.ID + "=" + id, null) == 1;
		}
		if (!result) Log.d(TAG, "updateInvoiceContItem - Error");
		return result;		
	}

	public boolean updateInvoiceContMark(long id_external, int mark) {
		boolean result = false;
		ContentValues cv = new ContentValues();
		cv.put(InvoiceContTable.MARK, mark);
		cv.put(InvoiceContTable.MODIFIED, 0);
		SQLiteDatabase sd = getWritableDatabase();
		result = sd.update(InvoiceContTable.TABLE_NAME, cv, InvoiceContTable.ID_EXTERNAL + "=" + id_external, null) == 1;
		if (!result) Log.d(TAG, "updateInvoiceContMark - Error");
		return result;
	}

	public Boolean removeInvoiceCont(long id) {
		Boolean result = false;
		try {
			if (id > 0) {
				SQLiteDatabase sd = getWritableDatabase();
				result = sd.delete(InvoiceContTable.TABLE_NAME, InvoiceContTable.ID + " = " + id, null) > 0;
			}
		} catch (Exception e) {
			if (D) Log.e(TAG, "removeInvoiceCont exception = " + e.getMessage());
		}
		return result;
	}


	public String getSelectedInvoice(int idInvoice) {
		String result = "";
		Cursor cursorInv;
		Cursor cursorInvCont;
		InvoiceRecord inv;
		InvoiceContRecord invCont;
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    	String i_state = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
    			" WHERE "+ StateTable.ID_EXTERNAL +" = i."+ InvoiceTable.STATE_ID +")as "+ 
    			InvoiceRecord.STATE_NM;
    	String query;
    	query = "SELECT i.*, "+ i_state +" FROM " + InvoiceTable.TABLE_NAME + 
    				" i WHERE " + InvoiceTable.ID + " = " + idInvoice;
    	
    	String i_gds_cont = "(SELECT "+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
    			" WHERE "+ GoodsTable.ID_EXTERNAL +" = i."+ InvoiceContTable.GDS_ID +")as "+ 
    			InvoiceContRecord.GDS_NAME;
    	String query_cont;
    	query_cont = "SELECT i.*, "+ i_gds_cont +" FROM " + InvoiceContTable.TABLE_NAME + 
				" i WHERE " + InvoiceContTable.INVOICE_ID + " = ";
    	
		
		String queryInv = query; //"SELECT * FROM " + InvoiceTable.TABLE_NAME + " WHERE " + InvoiceTable.ID + " = " + idInvoice;
		String queryInvCont = query_cont; //"SELECT * FROM " + InvoiceContTable.TABLE_NAME + " WHERE " +
				//InvoiceContTable.INVOICE_ID + " = ";

		SQLiteDatabase sd = getWritableDatabase();
		JSONArray warrants = new JSONArray();
		try {						
			cursorInv = sd.rawQuery(queryInv, null);
			while (cursorInv.moveToNext()) {	    		
	    		inv = new InvoiceRecord(cursorInv);
    			JSONObject jsonInvObj = new JSONObject();
				jsonInvObj.put("id", inv.getIdExternal());
				jsonInvObj.put("id_loc", inv.getId());
				jsonInvObj.put("id_state", inv.getIdState());
				jsonInvObj.put("id_doc", inv.getIdDoc());
				jsonInvObj.put("id_owner", inv.getIdOwner());
				jsonInvObj.put("id_wh", inv.getIdPlacement());
				jsonInvObj.put("id_wh2", inv.getIdPlacement2());
				jsonInvObj.put("num", inv.getNum());
				jsonInvObj.put("i_d", sDateFormat.format(inv.getiDate()));
				jsonInvObj.put("d_c", sDateFormat.format(inv.getDateChange()));
				jsonInvObj.put("ex_emp_id", inv.getEmpId());

				JSONArray jsonInvConts = new JSONArray();
				cursorInvCont = sd.rawQuery(queryInvCont + inv.getId(), null);
		    	while (cursorInvCont.moveToNext()) {
		    		JSONObject jsonInvContObj = new JSONObject();
		    		invCont = new InvoiceContRecord(cursorInvCont);
					jsonInvContObj.put("id", invCont.getIdExternal());
					jsonInvContObj.put("id_loc", invCont.getId());
					//jsonWContObj.put("id_warr", warrCont.id_warrant);
					jsonInvContObj.put("ic_num", invCont.getIcNum());
					jsonInvContObj.put("id_gds", invCont.getIdGds());
					
					String floatStr = Double.toString(invCont.getGdsCount());
					floatStr = floatStr.replace(".", ",");
					jsonInvContObj.put("gds_cnt", floatStr/*invCont.gds_count*/);
		    	    		
		    		jsonInvConts.put(jsonInvContObj);
		    	}
		    	jsonInvObj.put("inv_cont", jsonInvConts);
		    	warrants.put(jsonInvObj);
			}
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (warrants.length() > 0) {
        		result = warrants.toString();
        	} else {
        		result = "";
        	}
        }
		return result;
	}

	public String getSelectedInvoiceMarked(int idInvoice) {
		String result = "";
		Cursor cursorInv;
		Cursor cursorInvCont;
		InvoiceRecord inv;
		InvoiceContRecord invCont;
    	String i_state = "(SELECT "+ StateTable.NM +" FROM "+ StateTable.TABLE_NAME +
    			" WHERE "+ StateTable.ID_EXTERNAL +" = i."+ InvoiceTable.STATE_ID +")as "+ 
    			InvoiceRecord.STATE_NM;
    	String query;
    	query = "SELECT i.*, "+ i_state +" FROM " + InvoiceTable.TABLE_NAME + 
    				" i WHERE " + InvoiceTable.ID + " = " + idInvoice;
    	
    	String i_gds_cont = "(SELECT "+ GoodsTable.NM +" FROM "+ GoodsTable.TABLE_NAME +
    			" WHERE "+ GoodsTable.ID_EXTERNAL +" = i."+ InvoiceContTable.GDS_ID +")as "+ 
    			InvoiceContRecord.GDS_NAME;
    	String query_cont;
    	query_cont = "SELECT i.*, "+ i_gds_cont +" FROM " + InvoiceContTable.TABLE_NAME + 
				" i WHERE i." + InvoiceContTable.MARK + NOT_ZERO_CONDITION + " AND i." +
    			InvoiceContTable.MODIFIED + NOT_ZERO_CONDITION + " AND i." + InvoiceContTable.INVOICE_ID + " = ";
    			
		String queryInv = query; //"SELECT * FROM " + InvoiceTable.TABLE_NAME + " WHERE " + InvoiceTable.ID + " = " + idInvoice;
		String queryInvCont = query_cont; //"SELECT * FROM " + InvoiceContTable.TABLE_NAME + " WHERE " +
				//InvoiceContTable.INVOICE_ID + " = ";

		SQLiteDatabase sd = getWritableDatabase();
		JSONArray warrants = new JSONArray();
		try {						
			cursorInv = sd.rawQuery(queryInv, null);
			while (cursorInv.moveToNext()) {	    		
	    		inv = new InvoiceRecord(cursorInv);
    			JSONObject jsonInvObj = new JSONObject();
				jsonInvObj.put("id", inv.getIdExternal());
				jsonInvObj.put("id_loc", inv.getId());

				JSONArray jsonInvConts = new JSONArray();
				cursorInvCont = sd.rawQuery(queryInvCont + idInvoice, null);
		    	while (cursorInvCont.moveToNext()) {
		    		JSONObject jsonInvContObj = new JSONObject();
		    		invCont = new InvoiceContRecord(cursorInvCont);
					jsonInvContObj.put("id", invCont.getIdExternal());
					jsonInvContObj.put("id_loc", invCont.getId());
					//jsonWContObj.put("id_warr", warrCont.id_warrant);
					jsonInvContObj.put("ic_num", invCont.getIcNum());
					jsonInvContObj.put("id_gds", invCont.getIdGds());
					
					String floatStr = Double.toString(invCont.getGdsCount());
					floatStr = floatStr.replace(".", ",");
					jsonInvContObj.put("gds_cnt", floatStr/*invCont.gds_count*/);
		    	    		
		    		jsonInvConts.put(jsonInvContObj);
		    	}
		    	jsonInvObj.put("inv_cont", jsonInvConts);
		    	warrants.put(jsonInvObj);
			}
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (warrants.length() > 0) {
        		result = warrants.toString();
        	} else {
        		result = "";
        	}
        }
		return result;
	}

	public String getSelectedInvoiceGetMarks(int idInvoice) {
		String result = "";
		JSONArray warrants = new JSONArray();
		try {						
			JSONObject jsonInvObj = new JSONObject();
			jsonInvObj.put("id", idInvoice);			
	    	warrants.put(jsonInvObj);
    	} catch (JSONException ex) {
        	ex.printStackTrace();
        } finally {
        	if (warrants.length() > 0) {
        		result = warrants.toString();
        	} else {
        		result = "";
        	}
        }
		return result;
	}
	
	
}
