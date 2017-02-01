package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.EntTable;

public class EntRecord {
	public static final String ENT_CHILD_CNT = "ENT_CHILD_CNT";
	public static final String ENT_PARENT = "PARENT";
	private long id, idExternal, idParent;
	private double lat, lng;
	private String nm, addr, idStr;
	private int childCnt;
	
	   /**
     * Constructor. 
     */
    public EntRecord() {
    	 id = 0;
    }

    public EntRecord(Cursor c) {
		int colid = c.getColumnIndex(EntTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(EntTable.ID_EXTERNAL);
    	idExternal = c.getLong(colid);
		colid = c.getColumnIndex(EntTable.ID_EXT_STR);
		idStr = c.getString(colid);
		colid = c.getColumnIndex(EntTable.ID_PARENT);
    	idParent = c.getLong(colid);
//		colid = c.getColumnIndex(ENT_CHILD_CNT);
//		childCnt = c.getInt(colid);
		colid = c.getColumnIndex(EntTable.LAT);
    	lat = c.getDouble(colid);
		colid = c.getColumnIndex(EntTable.LNG);
    	lng = c.getDouble(colid);
		colid = c.getColumnIndex(EntTable.ADDR);
    	addr = c.getString(colid);
		colid = c.getColumnIndex(EntTable.NM);
    	nm = c.getString(colid).trim();
    }

	public long getId() {
		return id;
	}

	public long getIdExternal() {
		return idExternal;
	}

	public String getIdExtStr() {
		return idStr;
	}

	public long getIdParent() {
		return idParent;
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}

	public String getNm() {
		return nm;
	}

	public String getAddr() {
		return addr;
	}

	public int getChildCnt() {
		return childCnt;
	}

	public String toString() {
		String e_addr = " ";
		if (addr != null && addr.length() > 0 && !addr.contains("null"))
			e_addr = "\n" + addr;		
		
		if (childCnt > 0) {
			return nm+ String.format(" (%d)", childCnt) + e_addr;
		} else {
			return nm + e_addr;
		}
	}

}
