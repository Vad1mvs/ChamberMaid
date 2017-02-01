package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.InvoiceContTable;

public class InvoiceContRecord {
	public static final String GDS_NAME = "GDS_NAME";
	private long id, idInvoice, idExternal;
	private double gdsCount, gdsPrice;
	private int idGds, icNum, modified, mark, recStat;
	private String gdsName;
	private boolean selected;

	
	   /**
     * Constructor. 
     */
    public InvoiceContRecord() {
    	 id = 0;
    }

    public InvoiceContRecord(Cursor c) {
		int colid = c.getColumnIndex(InvoiceContTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(InvoiceContTable.ID_EXTERNAL);
    	idExternal = c.getLong(colid);
		colid = c.getColumnIndex(InvoiceContTable.INVOICE_ID);
    	idInvoice = c.getLong(colid);
		colid = c.getColumnIndex(InvoiceContTable.IC_NUM);
    	icNum = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceContTable.GDS_ID);
    	idGds = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceContTable.GDS_COUNT);
    	gdsCount = c.getDouble(colid);
		colid = c.getColumnIndex(InvoiceContTable.GDS_PRICE);
    	gdsPrice = c.getDouble(colid);
		colid = c.getColumnIndex(InvoiceContTable.MODIFIED);
    	modified = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceContTable.MARK);
    	mark = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceContTable.REC_STAT);
    	recStat = c.getInt(colid);
		colid = c.getColumnIndex(GDS_NAME);
    	gdsName = c.getString(colid);

    }

	public long getId() {
		return id;
	}

	public long getIdInvoice() {
		return idInvoice;
	}

	public long getIdExternal() {
		return idExternal;
	}

	public double getGdsCount() {
		return gdsCount;
	}

	public double getGdsPrice() {
		return gdsPrice;
	}

	public int getIdGds() {
		return idGds;
	}

	public int getIcNum() {
		return icNum;
	}

	public int getModified() {
		return modified;
	}

	public int getMark() {
		return mark;
	}

	public int getRecStat() {
		return recStat;
	}

	public String getGdsName() {
		return gdsName;
	}

	public boolean isSelected() {
		return selected;
	}

	public String toString() {
		if (gdsName.length() > 0)
			return gdsName;
		else
			return idExternal + "; " + idGds + "; " + gdsCount;
	}

}
