package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.EmpTable;

public class EmpRecord {
	public static final String E_ID = "id";
	public static final String FIO = "FIO";
	public static final int MAID_EMP = 0;
    public static final int TECH_EMP = 1;

	private long id, idExternal, idEnt;
    private int idJob;
	private String surname, name, patronimic, idExtStr;
	private byte[] photo;
	private boolean selected;
	
	   /**
     * Constructor. 
     */
    public EmpRecord() {
    	 id = 0;
    }

	public EmpRecord(String id, String fullName) {
        this.id = 0;
        idEnt = 0;
        idJob = MAID_EMP;
		idExtStr = id;
        try {
            this.idExternal = Integer.parseInt(id);
        } catch (Exception e) {
            this.idExternal = -1;
        }
		surname = fullName;
	}

    public EmpRecord(Cursor c) {
		int colid = c.getColumnIndex(EmpTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(EmpTable.ID_EXTERNAL);
    	idExternal = c.getLong(colid);
		colid = c.getColumnIndex(EmpTable.ID_ENT);
    	idEnt = c.getLong(colid);
        colid = c.getColumnIndex(EmpTable.ID_JOB);
        idJob = c.getInt(colid);
        colid = c.getColumnIndex(EmpTable.ID_EXT_STR);
        idExtStr = c.getString(colid);
		colid = c.getColumnIndex(EmpTable.SURNAME);
    	surname = c.getString(colid);
		colid = c.getColumnIndex(EmpTable.NAME);
    	name = c.getString(colid);
		colid = c.getColumnIndex(EmpTable.PATRONIMIC);
    	patronimic = c.getString(colid);
		colid = c.getColumnIndex(EmpTable.PHOTO);
    	photo = c.getBlob(colid);
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

	public String getSurname() {
		return surname;
	}

	public String getName() {
		return name;
	}

	public String getPatronimic() {
		return patronimic;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public boolean isSelected() {
		return selected;
	}

	public String getFullName() {
		return surname;
	}

	public String getIdExtStr() {
		return idExtStr;
	}

    public int getIdJob() {
        return idJob;
    }

    public String toString() {
		String res = surname;
		if (name != null && name.length() > 0) {
			res += " " + name.substring(0, 1) + ". ";
			if (patronimic != null && patronimic.length() > 0) {
				res += " " + patronimic.substring(0, 1) + ". ";
			}
		}
		return res;
	}

}
