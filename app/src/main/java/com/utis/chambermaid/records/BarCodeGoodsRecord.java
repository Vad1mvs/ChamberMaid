package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.BarCodeGoodsTable;

public class BarCodeGoodsRecord {
	private long id, idExternal, idGds;
	private int ttype;
	private String barcode;

	
	   /**
     * Constructor. 
     */
    public BarCodeGoodsRecord() {
    	 id = 0;
    }

    public BarCodeGoodsRecord(Cursor c) {
		int colid = c.getColumnIndex(BarCodeGoodsTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(BarCodeGoodsTable.ID_EXTERNAL);
    	idExternal = c.getLong(colid);
		colid = c.getColumnIndex(BarCodeGoodsTable.ID_GDS);
    	idGds = c.getLong(colid);
		colid = c.getColumnIndex(BarCodeGoodsTable.TTYPE);
    	ttype = c.getInt(colid);
		colid = c.getColumnIndex(BarCodeGoodsTable.BARCODE);
    	barcode = c.getString(colid);
    }

	public long getId() {
		return id;
	}

	public long getIdExternal() {
		return idExternal;
	}

	public long getIdGds() {
		return idGds;
	}

	public int getTtype() {
		return ttype;
	}

	public String getBarcode() {
		return barcode;
	}

	public String toString() {
		return barcode;
	}
	

}
