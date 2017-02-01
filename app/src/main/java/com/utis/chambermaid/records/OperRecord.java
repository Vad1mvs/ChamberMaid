package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.OperTable;

/**
 * Created by Oleg on 22.12.2016.
 */
public class OperRecord {

    public static final String ID_HOTEL = "idHotell";
    public static final String ROOM = "Nоm";
    public static final String SURNAME = "idGorn";
    public static final String CHECK_ROOM = "PrinytNom";
    public static final String CALL_CHAMBERMAID = "vizovGorn";
    public static final String CHAMBERMAID_IN_ROOM = "GornInNom";
    public static final String CHAMBERMAID_QUIT = "GornQuit";
    public static final String NAME = "NameGorn";

    private int id;
    private String hotelIdStr ,surname, nameChambermaid;
    private int hotelId, roomNum;
    private boolean checkRoom, callChambermaid , inRoom , modified, quitChambermaid;

//Nоm=201; PrinytNom=true; vizovGorn=false; idHotell=000000001; GornInNom=false; GornQuit=false; idGorn=0;
//room, check, callChambermaid, idHotel, inRoom , quit, surname
    public OperRecord( String num,
                      String checkRoom, String callChamber, String chambermaidInRoom,
                      // String idHotel,
                       String quit, String name, String nameChamber) {
        this.id = 0;
        //modified = false;
       // hotelIdStr = idHotel;
        surname = name;
        nameChambermaid = nameChamber;

        try {
            roomNum = Integer.parseInt(num);
        } catch (Exception e) {
            roomNum = -1;
        }

        try {
            this.checkRoom = Boolean.parseBoolean(checkRoom);
        } catch (Exception e) {
            this.checkRoom = false;
        }

        try {
            callChambermaid = Boolean.parseBoolean(callChamber);
        } catch (Exception e) {
            callChambermaid = false;
        }

        try {
            inRoom = Boolean.parseBoolean(chambermaidInRoom);
        } catch (Exception e) {
            inRoom = false;
        }
        try {
            quitChambermaid = Boolean.parseBoolean(quit);
        } catch (Exception e) {
            quitChambermaid = false;
        }
    }

    public OperRecord() {
        id = 0;
    }

    public OperRecord(Cursor c) {
        int colid = c.getColumnIndex(OperTable.ID);
        id = c.getInt(colid);
        colid = c.getColumnIndex(OperTable.ROOM_NUM);
        roomNum = c.getInt(colid);
        colid = c.getColumnIndex(OperTable.CHECK_ROOM);
        checkRoom = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(OperTable.CALL_CHAMBERMAID);
        callChambermaid = c.getInt(colid)==0 ? false : true;
     //   colid = c.getColumnIndex(OperTable.ID_HOTEL);
      //  hotelId = c.getInt(colid);
       // colid = c.getColumnIndex(OperTable.ID_HOTEL_STR);
      //  hotelIdStr = c.getString(colid);
        colid = c.getColumnIndex(OperTable.CHAMBERMAID_IN_ROOM);
        inRoom = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(OperTable.CHAMBERMAID_QUIT);
        quitChambermaid = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(OperTable.SURNAME);
        surname = c.getString(colid);
        colid = c.getColumnIndex(OperTable.NAME);
        nameChambermaid = c.getString(colid);
    }

    public boolean isQuitChambermaid() {
        return quitChambermaid;
    }

    public static String getIdHotel() {
        return ID_HOTEL;
    }

    public long getId() {
        return id;
    }

    public String getHotelIdStr() {
        return hotelIdStr;
    }

    public String getSurname() {
        return surname;
    }

    public String getNameChambermaid() {
        return nameChambermaid;
    }

    public int getHotelId() {
        return hotelId;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public boolean isCheckRoom() {
        return checkRoom;
    }

    public boolean isCallChambermaid() {
        return callChambermaid;
    }

    public boolean isChambermaidInRoom() {
        return inRoom;
    }

    public boolean isModified() {
        return modified;
    }

    public String toString() {
        return  id +"/  "+hotelId +"/  "+roomNum +"/  "+surname +"/  "+quitChambermaid ;
    }
}
