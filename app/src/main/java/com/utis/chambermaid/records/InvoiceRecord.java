package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.DBSchemaHelper;
import com.utis.chambermaid.tables.InvoiceTable;

import java.text.ParseException;
import java.util.Date;

public class InvoiceRecord {
	public static final int INVOICE_INCOME_DOC = 1;
	public static final int INVOICE_INCOME_DELAYED = 29;
	public static final int INVOICE_OUTGO_DOC = 18;
	public static final int INVOICE_OUTGO_DELAYED = 42;
	public static final int INVOICE_MOVE_DOC = 19;
	public static final int INVOICE_MOVE_DELAYED = 45;
	public static final int INVOICE_INVENTORY_DOC = 79; 
	public static final int INVOICE_INVENTORY_DELAYED = 29;
	public static final int INVOICE_STATE = 1;
	public static final String STATE_NM = "STATE_NM";
	
	private long id, idExternal;
	private int idState;
	private int idPlacement, idPlacement2, idOwner;
	private int idDoc, modified, rec_stat;
	private int empId;
	private Date iDate, dateChange;
	private String num, iDateStr, dateStrLbl, dateChangeStr, dateChangeStrLbl;
	private String entName, entAddress, stateName, execEmpName, execEmpFromName;
	private int gdsCount, invoiceType;
	

	
	   /**
     * Constructor. 
     */
    public InvoiceRecord() {
    	 id = 0;
    }

    public InvoiceRecord(Cursor c) {
		int colid = c.getColumnIndex(InvoiceTable.ID);
    	id = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceTable.ID_EXTERNAL);
    	idExternal = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceTable.STATE_ID);
    	idState = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceTable.OWNER_ID);
    	idOwner = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceTable.DOC_ID);
    	idDoc = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceTable.PLACEMENT_ID);
    	idPlacement = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceTable.PLACEMENT2_ID);
    	idPlacement2 = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceTable.MODIFIED);
    	modified = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceTable.REC_STAT);
    	rec_stat = c.getInt(colid);
		colid = c.getColumnIndex(InvoiceTable.NUM);
    	num = c.getString(colid);
		colid = c.getColumnIndex(STATE_NM);
    	stateName = c.getString(colid);

		colid = c.getColumnIndex(InvoiceTable.EMP_ID);
    	empId = c.getInt(colid);

    	colid = c.getColumnIndex(InvoiceTable.IDATE);
     	iDateStr = c.getString(colid);
     	java.util.Date date;
     	try {
     		date = DBSchemaHelper.dateFormatYY.parse(iDateStr);
     		iDate = new Date(date.getTime());
     		dateStrLbl = DBSchemaHelper.dateFormatMM.format(iDate);
     	} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
     	}
    	colid = c.getColumnIndex(InvoiceTable.DATE_CHANGE);
     	dateChangeStr = c.getString(colid);
     	try {
     		date = DBSchemaHelper.dateFormatYY.parse(dateChangeStr); 
     		dateChange = new Date(date.getTime());
     		dateChangeStrLbl = DBSchemaHelper.dateFormatMM.format(dateChange);
     	} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
     	}
     	switch (idDoc) {
			case INVOICE_INCOME_DOC:
				invoiceType = 0;
				break;
			case INVOICE_OUTGO_DOC:
				invoiceType = 1;
				break;
			case INVOICE_MOVE_DOC:
				invoiceType = 2;
				break;
	//     	case INVOICE_INVENTORY_DOC:
			default:
				invoiceType = 3;
				break;
     	}
    }

	public long getId() {
		return id;
	}

	public long getIdExternal() {
		return idExternal;
	}

	public int getIdState() {
		return idState;
	}

	public int getIdPlacement() {
		return idPlacement;
	}

	public int getIdPlacement2() {
		return idPlacement2;
	}

	public int getIdOwner() {
		return idOwner;
	}

	public int getIdDoc() {
		return idDoc;
	}

	public int getModified() {
		return modified;
	}

	public int getRec_stat() {
		return rec_stat;
	}

	public int getEmpId() {
		return empId;
	}

	public Date getiDate() {
		return iDate;
	}

	public Date getDateChange() {
		return dateChange;
	}

    public String getNum() {
        return num;
    }

    public String toString() {
		return idExternal + "; " + num + "; " + dateStrLbl;
	}

}
