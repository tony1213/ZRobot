package com.robot.et.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.robot.et.entity.FaceInfo;

import java.util.ArrayList;

/*
 * 本地数据库,数据库使用完一定要关闭，不然会报异常！
 */
public class RobotDB {
	private RobotDBHelper helper;
	public static RobotDB instance = null;
	
	private RobotDB(Context context){
		helper = new RobotDBHelper(context);
	}
	
	public static RobotDB getInstance(Context context){
		if(instance == null){
			synchronized (RobotDB.class) {
				if(instance == null){
					instance = new RobotDB(context);
				}
			}
		}
		return instance;
	}
	
	//增加脸部识别的信息
	public void addFaceInfo(FaceInfo info){
		String sql = "insert into faces(robotNum,authorId,authorName,spareInt,spareContent,spareContent2) values(?,?,?,?,?,?)";
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL(sql, new String[]{info.getRobotNum(),info.getAuthorId(),info.getAuthorName(),String.valueOf(info.getSpareInt()),info.getSpareContent(),info.getSpareContent2()});
		db.close();
	}
	
	//查询所有脸部识别信息
	public ArrayList<FaceInfo> getFaceInfos() {
		String sql = "select * from faces order by id desc";
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor c = db.rawQuery(sql, new String[]{});
		ArrayList<FaceInfo> faceInfos = new ArrayList<FaceInfo>();
		while (c.moveToNext()) {
			FaceInfo info = new FaceInfo();
			info.setRobotNum(c.getString(c.getColumnIndex("robotNum")));
			info.setAuthorId(c.getString(c.getColumnIndex("authorId")));
			info.setAuthorName(c.getString(c.getColumnIndex("authorName")));
			info.setSpareInt(c.getInt(c.getColumnIndex("spareInt")));
			info.setSpareContent(c.getString(c.getColumnIndex("spareContent")));
			info.setSpareContent2(c.getString(c.getColumnIndex("spareContent2")));
			faceInfos.add(info);
		}
		
		c.close();
		db.close();
		return faceInfos;
	}

}
