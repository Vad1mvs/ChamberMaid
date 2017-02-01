package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.StateTable;

public class StateRecord {
	public static final int EQ_STATE_IN_ORDER = 49;
	public static final int EQ_STATE_NOTIN_ORDER = 48;
	public static final int JOB_STATE_IS_DONE = 22;
	public static final int JOB_STATE_TODO = 21;
	public static final int JOB_STATE_IS_NOTDONE = 38;

	private long id, idExternal;
	private int idDoc;
//	public int no;
	private String nm;

	
	   /**
     * Constructor. 
     */
    public StateRecord() {
    	 id = 0;
    }

    public StateRecord(Cursor c) {
		int colid = c.getColumnIndex(StateTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(StateTable.ID_EXTERNAL);
    	idExternal = c.getLong(colid);
		colid = c.getColumnIndex(StateTable.ID_DOC);
    	idDoc = c.getInt(colid);
//		colid = c.getColumnIndex(StateTable.NO);
//    	no = c.getInt(colid);
		colid = c.getColumnIndex(StateTable.NM);
    	nm = c.getString(colid);
    }

	public long getId() {
		return id;
	}

	public long getIdExternal() {
		return idExternal;
	}

	public int getIdDoc() {
		return idDoc;
	}

	public String getNm() {
		return nm;
	}

	public String toString() {
		return idExternal + " " + nm;
	}
}
