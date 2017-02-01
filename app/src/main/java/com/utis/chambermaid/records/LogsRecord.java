package com.utis.chambermaid.records;

import android.database.Cursor;

import com.utis.chambermaid.DBSchemaHelper;
import com.utis.chambermaid.tables.LogsTable;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class LogsRecord {
	public static final int EXCEPTION = 1;
	public static final int ERROR = 2;
	public static final int WARNING = 3;
	public static final int DEBUG = 4;
	public static final int INFO = 5;
	private long id;
	private int level, modified, num;
	private Date mDate;
	private String msg;

 	public LogsRecord() {
 	 id = 0;
 }

	public LogsRecord(Cursor c) {
		int colid = c.getColumnIndex(LogsTable.ID);
		id = c.getLong(colid);
		colid = c.getColumnIndex(LogsTable.LEVEL);
		level = c.getInt(colid);
		colid = c.getColumnIndex(LogsTable.MSG_DATE);
		String date_str = c.getString(colid);
		java.util.Date date;
		try {
			date = DBSchemaHelper.dateFormatMSYY.parse(date_str);
			mDate = new Date(date.getTime());
		} catch (ParseException e) {
			try {
				date = DBSchemaHelper.dateFormatYY.parse(date_str);
				mDate = new Date(date.getTime());
			} catch (ParseException pe) {
				// TODO Auto-generated catch block
				pe.printStackTrace();
			}
		}
		colid = c.getColumnIndex(LogsTable.MSG);
		msg = c.getString(colid);
		colid = c.getColumnIndex(LogsTable.MODIFIED);
		modified = c.getInt(colid);
	}

	public long getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getModified() {
		return modified;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public Date getmDate() {
		return mDate;
	}

	public String getMsg() {
		return msg;
	}

	public String toString() {
		return String.format("%s; %s", getTime(), msg);
	}

	private String getDate() {
		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
		String text = df.format(mDate);
		return text;
	}

	public String getTime() {
		SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
		String text = df.format(mDate);
		return text;
	}

}
