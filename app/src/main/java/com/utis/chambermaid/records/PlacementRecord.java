package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.PlacementTable;

public class PlacementRecord {
	public static final int PLACE_FLOOR = 1;
	public static final int PLACE_ROOM = 2;
	private long id, idExternal, idEnt, idParent;
	private int ttype;
	private String nm, remark;

	
	   /**
     * Constructor. 
     */
    public PlacementRecord() {
    	 id = 0;
    }

    public PlacementRecord(Cursor c) {
		int colid = c.getColumnIndex(PlacementTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(PlacementTable.ID_EXTERNAL);
    	idExternal = c.getLong(colid);
		colid = c.getColumnIndex(PlacementTable.ID_ENT);
    	idEnt = c.getLong(colid);
		colid = c.getColumnIndex(PlacementTable.ID_PARENT);
    	idParent = c.getLong(colid);
		colid = c.getColumnIndex(PlacementTable.TTYPE);
    	ttype = c.getInt(colid);
		colid = c.getColumnIndex(PlacementTable.NAME);
    	nm = c.getString(colid);
		colid = c.getColumnIndex(PlacementTable.REMARK);
    	remark = c.getString(colid);
    }

	public long getId() {
		return id;
	}

	public long getIdExternal() {
		return idExternal;
	}

	public long getIdEnt() {
		return idEnt;
	}

	public long getIdParent() {
		return idParent;
	}

	public int getTtype() {
		return ttype;
	}

	public String getNm() {
		return nm;
	}

	public String getRemark() {
		return remark;
	}

	public String toString() {
		return nm;
	}

}
