package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.ZToolTable;

public class ZToolRecord {
	public static final String ENT_NM = "ENT_NM";
	private long id, idExternal, idEnt;
	private double vat;
	private String nm;

	
	   /**
     * Constructor. 
     */
    public ZToolRecord() {
    	 id = 0;
    }

    public ZToolRecord(Cursor c) {
		int colid = c.getColumnIndex(ZToolTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(ZToolTable.ID_EXTERNAL);
    	idExternal = c.getLong(colid);
		colid = c.getColumnIndex(ZToolTable.ID_ENT);
    	idEnt = c.getLong(colid);
		colid = c.getColumnIndex(ZToolTable.VAT);
    	vat = c.getDouble(colid);
		colid = c.getColumnIndex(ENT_NM);
    	nm = c.getString(colid);
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

    public double getVat() {
        return vat;
    }

    public String getNm() {
        return nm;
    }

    public String toString() {
//		return idExternal + " " + nm;
		if (nm != null && nm.length() > 0)
			return nm;
		else 
			return ""+ idEnt;
	}
}
