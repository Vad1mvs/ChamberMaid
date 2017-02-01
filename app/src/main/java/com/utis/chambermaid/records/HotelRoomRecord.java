package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.CommonClass;
import com.utis.chambermaid.DBSchemaHelper;
import com.utis.chambermaid.tables.HotelRoomTable;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Oleg on 10.07.2015.
 */
public class HotelRoomRecord {
    public static final String ID_HOTEL = "idHotell";
    public static final String HOTEL_NAME = "Hotellname";
    public static final String FLOOR = "Level";
    public static final String ROOM = "Nоm";  // буква "o" русская
    public static final String OCCUPIED = "satuszanyt";
    public static final String GUEST = "satusGost";
    public static final String SERVICE = "Uborka";
    public static final String CLEARENCE = "TimeExit";
    public static final String DOOR = "Door";
    public static final String WINDOW = "Windows";
    public static final String BALCONY = "Balkon";

    public static final String CHANGE_BED = "DatаPostel";  // вторая буква "а" русская
    public static final String DEPARTURE_DATE = "Datout";
    public static final String TWIN = "twin";
    public static final String WATER_LEAKAGE = "datchikZatop";
    public static final String RESERVATIONS = "DatBron";
    public static final String CHECK_ROOM = "PrinytNom";
    public static final String STATUS_NOM = "statusNOM";

//    public static final String TIPE_CLEARENCE = "tipe_clearence"; // int / null = true
    public static final String REPAIR = "Remont"; // bool
    public static final String NOTE_REPAIR = "NoteRemont"; // str
    public static final String CHANGE_BED_EXTRA = "KvoDatPosel"; //
    public static final String TIPE_CLEARENCE = "TipUborka"; // int
    public static final String NOT_DISTURB = "nodistr"; // int
    public static final String CALL_CHAMBERMAID = "vizovGorn"; // int
    public static final String CHAMBERMAID_IN_ROOM = "GornInNom"; // int

    public static final String OPER_NM = "OPER_NM";
   // public static final String OPER_STR = "OPER_STR";

    public static final String OPER_NUM_CHECK = "OPER_NUM_CHECK";
    public static final String OPER_CALL = "OPER_CALL";
    public static final String OPER_NAME = "OPER_NAME";
    public static final String OPER_IN_ROOM = "OPER_IN_ROOM";
    public static final String OPER_QUIT = "OPER_QUIT";
    public static final String OPER_NAME_GORN = "OPER__NAME_GORN";

    private String hotelName, hotelIdStr, changeBedDateStr, departureDateStr, reservationsDateStr, noteRepair;
    public  String operNum = "", operName = "", operNameGorn;
    public boolean operNumCheck, operCall, operInRoom, operQuit , operStr;
    private long id;
    private int hotelId, roomFloor, roomNum, balcony,tipeClearence , changeBedExtra,
            daysAfterChangeBed, daysBeforeDeparture, daysBeforeReservation;
    private Date changeBedDate, departureDate, reservationsDate;
    private boolean roomOccupied, guestInRoom, departureDay, repair, notDisturb, callChambermaid, inRoom;
    private boolean serviceNeeded, todayClearence, statusNom;
    private boolean door, window, modified, twin, waterLeakage, checkRoom, changeBed;

    public HotelRoomRecord(String id, String name, String floor, String num,
                           String occup, String guest, String service, String clearence,
                           String door, String window, String balcony,
                           String changeBedDate, String departureDate, String twin,
                           String water, String reservationsDate, String checkRoom
                          , String statNom, String rem, String noteRem, String bedExtra
                          ,String tipeClear, String notDist, String callChamber, String chambermaidInRoom

    ) {
        this.id = 0;
        modified = false;
        hotelIdStr = id;
        hotelName = name;
        noteRepair = noteRem;
     //   noteRepair = note;
        try {
            hotelId = Integer.parseInt(id);
        } catch (Exception e) {
            hotelId = -1;
        }
        try {
            roomNum = Integer.parseInt(num);
        } catch (Exception e) {
            roomNum = -1;
        }
        try {
            roomFloor = Integer.parseInt(floor);
        } catch (Exception e) {
            roomFloor = -1;
        }
        try {
            roomOccupied = Boolean.parseBoolean(occup);
        } catch (Exception e) {
            roomOccupied = false;
        }
        try {
            guestInRoom = Boolean.parseBoolean(guest);
        } catch (Exception e) {
            guestInRoom = false;
        }
        try {
            serviceNeeded = Boolean.parseBoolean(service);
        } catch (Exception e) {
            serviceNeeded = false;
        }
        try {
            todayClearence = Boolean.parseBoolean(clearence);
        } catch (Exception e) {
            todayClearence = false;
        }
        try {
            this.door = Boolean.parseBoolean(door);
        } catch (Exception e) {
            this.door = false;
        }
        try {
            this.window = Boolean.parseBoolean(window);
        } catch (Exception e) {
            this.window = false;
        }
        try {
            this.balcony = Integer.parseInt(balcony);
        } catch (Exception e) {
            this.balcony = 0;
        }

    //-------------------------------------------------
        this.changeBedDateStr = changeBedDate;
        try {
            this.changeBedDate = DBSchemaHelper.dateFormatDayYY.parse(changeBedDateStr);
        } catch (Exception e) {
            this.changeBedDate = new Date();
        }
        daysAfterChangeBed = CommonClass.daysBetween(this.changeBedDate, new Date());

    //-------------------------------------------------

        this.departureDateStr = departureDate;
        try {
            if (this.roomOccupied) {
                String date = departureDateStr.replace('T', ' ');
                this.departureDate = DBSchemaHelper.dateFormatYY.parse(date);
            } else
                this.departureDate = new Date();
        } catch (Exception e) {
            this.departureDate = new Date();
        }
        daysBeforeDeparture = CommonClass.daysBetween(new Date(), this.departureDate);
        Calendar calNow = Calendar.getInstance();
        Calendar calDepDate = Calendar.getInstance();
        calDepDate.setTime(this.departureDate);
        departureDay = calNow.get(Calendar.YEAR) == calDepDate.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calDepDate.get(Calendar.DAY_OF_YEAR);
        try {
            this.twin = Boolean.parseBoolean(twin);
        } catch (Exception e) {
            this.twin = false;
        }
        try {
            this.waterLeakage = Boolean.parseBoolean(water);
        } catch (Exception e) {
            this.waterLeakage = false;
        }
        this.reservationsDateStr = reservationsDate;
        try {
            String date = reservationsDateStr.replace('T', ' ');
            this.reservationsDate = DBSchemaHelper.dateFormatYYT.parse(date);
            checkReservationDate();
        } catch (Exception e) {
//            this.reservationsDate = new Date();
        }
        try {
            this.checkRoom = Boolean.parseBoolean(checkRoom);
        } catch (Exception e) {
            this.checkRoom = false;
        }

        try {
            this.statusNom = Boolean.parseBoolean(statNom);
        } catch (Exception e) {
            this.statusNom = false;
        }

        try {
            this.repair = Boolean.parseBoolean(rem);
        } catch (Exception e) {
            this.repair = false;
        }
        try {
            this.changeBedExtra = Integer.parseInt(bedExtra);
        } catch (Exception e) {
            this.changeBedExtra = 0;
        }
        try {
            this.tipeClearence = Integer.parseInt(tipeClear);
        } catch (Exception e) {
            this.tipeClearence = Integer.parseInt(null);
        }

        try {
            notDisturb = Boolean.parseBoolean(notDist);
        } catch (Exception e) {
            notDisturb = false;
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

    }


    public HotelRoomRecord(Cursor c) {
        int tmp;
        int colid = c.getColumnIndex(HotelRoomTable.ID);
        id = c.getLong(colid);
        colid = c.getColumnIndex(HotelRoomTable.ID_HOTEL);
        hotelId = c.getInt(colid);
        colid = c.getColumnIndex(HotelRoomTable.ID_HOTEL_STR);
        hotelIdStr = c.getString(colid);
        colid = c.getColumnIndex(HotelRoomTable.ROOM_FLOOR);
        roomFloor = c.getInt(colid);
        colid = c.getColumnIndex(HotelRoomTable.ROOM_NUM);
        roomNum = c.getInt(colid);
        colid = c.getColumnIndex(HotelRoomTable.HOTEL);
        hotelName = c.getString(colid);



        colid = c.getColumnIndex(OPER_NM);
        if (colid >= 0) operNum = c.getString(colid);
        colid = c.getColumnIndex(OPER_NAME);
        if (colid >= 0) operName = c.getString(colid);

        colid = c.getColumnIndex(OPER_NAME_GORN);
        if (colid >= 0) operNameGorn = c.getString(colid);

        colid = c.getColumnIndex(OPER_NUM_CHECK);
        operNumCheck = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(OPER_CALL);
        operCall = c.getInt(colid)==0 ? false : true;

        colid = c.getColumnIndex(OPER_IN_ROOM);
        operInRoom = c.getInt(colid)==0 ? false : true;

        colid = c.getColumnIndex(OPER_QUIT);
        operQuit = c.getInt(colid)==0 ? false : true;

//        colid = c.getColumnIndex(OPER_STR);
//        operStr = c.getInt(colid)==0 ? false : true;

        colid = c.getColumnIndex(HotelRoomTable.OCCUPIED);
        roomOccupied = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.GUEST_IN_ROOM);
        guestInRoom = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.SERVICE_NEEDED);
        serviceNeeded = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.TODAY_CLEARENCE);
        todayClearence = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.DOOR);
        door = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.WINDOW);
        window = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.BALCONY);
        balcony = c.getInt(colid);   // должно быть int 0, 1, 2
        colid = c.getColumnIndex(HotelRoomTable.STATUS_NOM);
        statusNom = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.REPAIR);
        repair = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.NOTE_REPAIR);
        noteRepair = c.getString(colid);
        colid = c.getColumnIndex(HotelRoomTable.CHANGE_BED_EXTRA);
        changeBedExtra = c.getInt(colid);   // должно быть int 0, 1, 2


        colid = c.getColumnIndex(HotelRoomTable.TIPE_CLEARENCE);
        tipeClearence = c.getInt(colid);   // должно быть int 0, 1, 2
        colid = c.getColumnIndex(HotelRoomTable.NOT_DISTURB);
        notDisturb = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.CALL_CHAMBERMAID);
        callChambermaid = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.CHAMBERMAID_IN_ROOM);
        inRoom = c.getInt(colid)==0 ? false : true;

        colid = c.getColumnIndex(HotelRoomTable.CHANGE_OF_BED_DATE);
        changeBed = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.CHANGE_OF_BED_DATE);
        changeBedDateStr = c.getString(colid);
        colid = c.getColumnIndex(HotelRoomTable.DEPARTURE_DATE);
        departureDateStr = c.getString(colid);
        colid = c.getColumnIndex(HotelRoomTable.TWIN);
        twin = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.WATER_LEAKAGE);
        waterLeakage = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.CHECK_ROOM);
        checkRoom = c.getInt(colid)==0 ? false : true;
        colid = c.getColumnIndex(HotelRoomTable.RESERVATIONS_DATE);
        reservationsDateStr = c.getString(colid);
        try {
            this.changeBedDate = DBSchemaHelper.dateFormatDayYY.parse(changeBedDateStr);
        } catch (Exception e) {
            this.changeBedDate = new Date();
        }
        daysAfterChangeBed = CommonClass.daysBetween(this.changeBedDate, new Date());
        //-----------------------------------------------


        try {
            if (this.roomOccupied) {
                String date = departureDateStr.replace('T', ' ');
                this.departureDate = DBSchemaHelper.dateFormatYY.parse(date);
            } else
                this.departureDate = new Date();
        } catch (Exception e) {
            this.departureDate = new Date();
        }
        daysBeforeDeparture = CommonClass.daysBetween(new Date(), this.departureDate);
        Calendar calNow = Calendar.getInstance();
        Calendar calDepDate = Calendar.getInstance();
        calDepDate.setTime(this.departureDate);
        departureDay = calNow.get(Calendar.YEAR) == calDepDate.get(Calendar.YEAR) &&
                calNow.get(Calendar.DAY_OF_YEAR) == calDepDate.get(Calendar.DAY_OF_YEAR);

        try {
            String date = reservationsDateStr.replace('T', ' ');
            this.reservationsDate = DBSchemaHelper.dateFormatYYT.parse(date);
            checkReservationDate();
        } catch (Exception e) {
//            this.reservationsDate = new Date();
        }
        modified = false;
    }

    private void checkReservationDate() {
        String year = DBSchemaHelper.dateYear.format(this.reservationsDate);
        if (year.contentEquals("0001")) {
            this.reservationsDate = null;
            daysBeforeReservation = -1;
        } else {
            daysBeforeReservation = CommonClass.daysBetween(new Date(), this.reservationsDate);
        }
    }


    public String getHotelIdStr() {
        return hotelIdStr;
    }

    public int getHotelId() {
        return hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public int getRoomFloor() {
        return roomFloor;
    }

    public int getRoomNum() {
        return roomNum;
    }

    public boolean isRoomOccupied() {
        return roomOccupied;
    }

    public boolean isGuestInRoom() {
        return guestInRoom;
    }

    public boolean isServiceNeeded() {
        return serviceNeeded;
    }

    public void setServiceNeeded(boolean serviceNeeded) {
        this.serviceNeeded = serviceNeeded;
    }

    public boolean isTodayClearence() {
        return todayClearence;
    }

    public boolean isDoor() {
        return door;
    }

    public boolean isWindow() {
        return window;
    }

    public int getBalcony() {
        return balcony;
    }

    public Date getChangeBedDate() {
        return changeBedDate;
    }

    public String getChangeBedDateStr() {
        return changeBedDateStr;
    }

    public String getDepartureDateStr() {
        return departureDateStr;
    }

    public String getReservationsDateStr() {
        return reservationsDateStr;
    }

    public boolean isTwin() {
        return twin;
    }

    public boolean isWaterLeakage() {
        return waterLeakage;
    }

    public int getDaysAfterChangeBed() {
        return daysAfterChangeBed;
    }

    public int getDaysBeforeDeparture() {
        return daysBeforeDeparture;
    }

    public int getDaysBeforeReservation() {
        return daysBeforeReservation;
    }

    public boolean isDepartureDay() {
        return departureDay;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public Date getReservationsDate() {
        return reservationsDate;
    }

    public boolean isStatusNom() {
        return statusNom;
    }

    public boolean isCheckRoom() {
        return checkRoom;
    }

    public boolean isRepair() {
        return repair;
    }

    public String getNoteRepair() {
        return noteRepair;
    }

    public int getChangeBedExtra() {
        return changeBedExtra;
    }

    public int getTipeClearence() {
        return tipeClearence;
    }

    public boolean isCallChambermaid() {
        return callChambermaid;
    }

    public boolean isNotDisturb() {
        return notDisturb;
    }

    public boolean isChambermaidInRoom() {
        return inRoom;
    }

    public String toString() {
        return String.format("%d: %s; этаж %d №%d занят %b",
                hotelId, hotelName, roomFloor, roomNum, roomOccupied,
                door, window
        );
    }
}
