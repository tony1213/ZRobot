package com.robot.et.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class RobotDBHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "robot.db";
	private static final int DB_VERSION = 1;

	public RobotDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	
	public RobotDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//脸部识别表
		String faces = "create table faces"+
				"("+ 
				"id integer primary key autoincrement,robotNum varchar(50),authorId varchar(50),authorName varchar(50),spareInt integer," +
				"spareContent varchar(50),spareContent2 varchar(50)" +
				 ")";

		db.execSQL(faces);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
