package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.tables.UserTable;

public class UserRecord {
	private long id, idExternal;
	private String surname, name, patronimic;
	private int idAdm, last_z_ent;
	
	   /**
     * Constructor. 
     */
    public UserRecord() {
    	 id = 0;
    	 surname = "";
    	 name = "";
    	 patronimic = "";
    }

    public UserRecord(Cursor c) {
		int colid = c.getColumnIndex(UserTable.ID);
    	id = c.getLong(colid);
		colid = c.getColumnIndex(UserTable.ID_EXTERNAL);
    	idExternal = c.getLong(colid);
		colid = c.getColumnIndex(UserTable.ID_ADM);
    	idAdm = c.getInt(colid);
		colid = c.getColumnIndex(UserTable.LAST_Z_ENT);
    	last_z_ent = c.getInt(colid);
		colid = c.getColumnIndex(UserTable.SURNAME);
    	surname = c.getString(colid);
		colid = c.getColumnIndex(UserTable.NAME);
    	name = c.getString(colid);
		colid = c.getColumnIndex(UserTable.PATRONIMIC);
    	patronimic = c.getString(colid);
    }

	public long getIdExternal() {
		return idExternal;
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

	public int getIdAdm() {
		return idAdm;
	}

    public long getId() {
        return id;
    }

	public int getLast_z_ent() {
		return last_z_ent;
	}

    public void setSurname(String sname) {
        surname = sname;
    }

	public String toString() {
//		return surname + " " + name.substring(0, 1) + ". " + patronimic.substring(0, 1) + ".";
		return surname + " " + name + " " + patronimic;
	}

}
