package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.IpTables;


public class IpRecord {
    private long id;
    private String ip_name, hotel;

    public IpRecord(Cursor c) {
        int colid = c.getColumnIndex(IpTables._ID);
        id = c.getLong(colid);
        colid = c.getColumnIndex(IpTables.HOTEL);
        hotel = c.getString(colid);
        colid = c.getColumnIndex(IpTables.IP_NAME);
        ip_name = c.getString(colid);

    }

    public long getId() {
        return id;
    }

    public String getIp_name() {
        return ip_name;
    }

    public String getHotel() {
        return hotel;
    }

    @Override
    public String toString() {
        return ip_name;
    }
}
