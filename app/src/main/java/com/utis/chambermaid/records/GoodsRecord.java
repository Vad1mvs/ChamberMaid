package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.GoodsTable;

public class GoodsRecord {
	public static final String GDS_CHILD_CNT = "GDS_CHILD_CNT";
	private long id, idExternal, idParent;
	private int ttype, gdsType, childCnt;
	private String fnm, barcode;

	
	   /**
     * Constructor. 
     */
    public GoodsRecord() {
    	 id = 0;
    }

    public GoodsRecord(Cursor c) {
		int colid = c.getColumnIndex(GoodsTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(GoodsTable.ID_EXTERNAL);
    	idExternal = c.getLong(colid);
		colid = c.getColumnIndex(GoodsTable.ID_PARENT);
    	idParent = c.getLong(colid);
		colid = c.getColumnIndex(GoodsTable.TTYPE);
    	ttype = c.getInt(colid);
		colid = c.getColumnIndex(GoodsTable.GDSTYPE);
    	gdsType = c.getInt(colid);
		colid = c.getColumnIndex(GDS_CHILD_CNT);
		childCnt = c.getInt(colid);
		colid = c.getColumnIndex(GoodsTable.NM);
    	fnm = c.getString(colid);
    	barcode = "";
    }
    
	public String toString() {
		if (childCnt > 0) {
			return String.format("%s (%d)", fnm, childCnt);
		} else {
			return fnm + " " + barcode;
		}
	}
	
	
}
