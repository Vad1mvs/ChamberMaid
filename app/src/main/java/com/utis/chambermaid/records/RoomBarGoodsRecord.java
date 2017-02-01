package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.RoomBarGoodsTable;

/**
 * Created by Oleg on 10.07.2015.
 */
public class RoomBarGoodsRecord {
    public static final int DOC_SALE_TYPE = 1;
    public static final int DOC_STORE_TYPE = 2;
    public static final String ROOM_PARAM = "номер";
    public static final String HOTEL_PARAM = "idHotell";
    public static final String GDS_LIST_PARAM = "tabl";//"spisdoc";//"tabl";
    public static final String GDS_DOC_PARAM = "sostav";//"doc";
    public static final String ROOM_POST_PARAM = "nom";
    public static final String EMP_PARAM = "idGorn";
    public static final String TYPE_PARAM = "tip";
    public static final String GOODS_POST_PARAM = "tabl";

    public static final String GDS_NAME = "tovar";
    public static final String ROOM_NUM = "nom";
    public static final String GDS_ID = "idtovar";
    public static final String GDS_QUANTITY = "kvo";
    public static final String GDS_QUANTITY_STORE = "kvosklad";
    public static final String GDS_QUANTITY_ROOM = "kvominbar";
    private String room, gdsName, gdsIdStr;
    private long id;
    private int gdsQuantity, gdsSoldQuantity, gdsNo;
    private int gdsQuantityRoom, gdsQuantityMaid, saleMode;
    boolean modified, total;

    public RoomBarGoodsRecord(String room, String name, int saleMode, int quantity, int quantityMaid) {
        this.room = room;
        modified = false;
        total = true;
        this.saleMode = saleMode;
        this.id = 0;
        gdsIdStr = "";
        gdsName = name;
        gdsQuantityMaid = quantityMaid;
        gdsQuantityRoom = 0;
        gdsQuantity = quantity;
    }

    public RoomBarGoodsRecord(String room, String name, String id, String quantity) {
        this.room = room;
        modified = false;
        total = false;
        saleMode = DOC_SALE_TYPE;
        this.id = 0;
        gdsIdStr = id;
        gdsName = name;
        gdsQuantityMaid = 0;
        gdsQuantityRoom = 0;
        try {
            gdsQuantity = Integer.parseInt(quantity);
        } catch (Exception e) {
            gdsQuantity = -1;
        }
    }

    public RoomBarGoodsRecord(String room, String name, String id, String quantityMaid, String quantityRoom) {
        this.room = room;
        modified = false;
        total = false;
        saleMode = DOC_STORE_TYPE;
        this.id = 0;
        gdsIdStr = id;
        gdsName = name;
        gdsQuantity = 0;
        try {
            gdsQuantityMaid = Integer.parseInt(quantityMaid);
        } catch (Exception e) {
            gdsQuantityMaid = -1;
        }
        try {
            gdsQuantityRoom = Integer.parseInt(quantityRoom);
        } catch (Exception e) {
            gdsQuantityRoom = -1;
        }
        try {
            gdsQuantity = Integer.parseInt(quantityRoom);
        } catch (Exception e) {
            gdsQuantity = -1;
        }
    }

    public RoomBarGoodsRecord(Cursor c) {
        int colid = c.getColumnIndex(RoomBarGoodsTable.ID);
        id = c.getLong(colid);
        colid = c.getColumnIndex(RoomBarGoodsTable.ID_GDS_STR);
        gdsIdStr = c.getString(colid);
        colid = c.getColumnIndex(RoomBarGoodsTable.GDS_NAME);
        gdsName = c.getString(colid);
        colid = c.getColumnIndex(RoomBarGoodsTable.GDS_QUANTITY);
        gdsQuantity = c.getInt(colid);
        colid = c.getColumnIndex(RoomBarGoodsTable.GDS_SOLD_QUANTITY);
        gdsSoldQuantity = c.getInt(colid);
        colid = c.getColumnIndex(RoomBarGoodsTable.GDS_STORE_QUANTITY);
        gdsQuantityMaid = c.getInt(colid);
        colid = c.getColumnIndex(RoomBarGoodsTable.ROOM_NUM);
        room = c.getString(colid);
        colid = c.getColumnIndex(RoomBarGoodsTable.SALE_MODE);
        saleMode = c.getInt(colid);
        colid = c.getColumnIndex(RoomBarGoodsTable.TOTAL);
        total = (c.getInt(colid) > 0) ? true : false;
        modified = false;
        gdsNo = 0;
    }

    public long getId() {
        return id;
    }

    public String getRoom() {
        return room;
    }

    public String getGdsName() {
        return gdsName;
    }

    public int getGdsQuantity() {
       return gdsQuantity;
       }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public int getGdsSoldQuantity() {
        return gdsSoldQuantity;
    }

    public int getGdsLeftQuantity() {
        if (DOC_SALE_TYPE == saleMode)
            return gdsQuantity - gdsSoldQuantity;
        else
            return gdsQuantityMaid - gdsSoldQuantity;
    }

    public void setGdsSoldQuantity(int gdsSoldQuantity) {
        this.gdsSoldQuantity = gdsSoldQuantity;
    }

    public int getGdsQuantityRoom() {
        return gdsQuantityRoom;
    }

    public int getGdsQuantityMaid() {
        return gdsQuantityMaid;
    }

    public int getGdsNo() {
        return gdsNo;
    }

    public void setGdsNo(int gdsNo) {
        this.gdsNo = gdsNo;
    }

    public int getSaleMode() {
        return saleMode;
    }

    public String getGdsIdStr() {
        return gdsIdStr;
    }

    public boolean isTotal() {
        return total;
    }

    public String toString() {
//        return String.format("%s; %s: %s - %d", room, gdsId, gdsName, gdsQuantity);
//        return String.format("%d: %s - %d", gdsId, gdsName, gdsQuantity);
        return String.format("%s - %d", gdsName, gdsQuantity);
    }


}
