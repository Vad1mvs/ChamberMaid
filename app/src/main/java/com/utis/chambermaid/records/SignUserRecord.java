package com.utis.chambermaid.records;


public class SignUserRecord {
	public int id;
	public String userOnline, userName;
	public String userSurName, userSfx, userId;
	
	
	   /**
  * Constructor. 
  */
	public SignUserRecord() {
		id = 0;
	}

//	public SignUserRecord(Cursor c) {
//		int colid = c.getColumnIndex(LocationTable.ID);
//		id = c.getInt(colid);
//	}
 
	public String toString() {
		return String.format("%s %s", userName, userSurName);
	}


}
