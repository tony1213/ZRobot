package com.robot.et.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.robot.et.entity.FaceInfo;
import com.robot.et.entity.LearnAnswerInfo;
import com.robot.et.entity.LearnQuestionInfo;

import java.util.ArrayList;
import java.util.List;

/*
 * 本地数据库,数据库使用完一定要关闭，不然会报异常！
 */
public class RobotDB {
    private RobotDBHelper helper;
    public static RobotDB instance = null;

    private RobotDB(Context context) {
        helper = new RobotDBHelper(context);
    }

    public static RobotDB getInstance(Context context) {
        if (instance == null) {
            synchronized (RobotDB.class) {
                if (instance == null) {
                    instance = new RobotDB(context);
                }
            }
        }
        return instance;
    }

    //增加脸部识别的信息
    public void addFaceInfo(FaceInfo info) {
        String sql = "insert into faces(robotNum,authorId,authorName,spareInt,spareContent,spareContent2) values(?,?,?,?,?,?)";
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql, new String[]{info.getRobotNum(), info.getAuthorId(), info.getAuthorName(), String.valueOf(info.getSpareInt()), info.getSpareContent(), info.getSpareContent2()});
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

    //新增智能学习的问题
    public void addLearnQuestion(LearnQuestionInfo info) {
        String sql = "insert into question(robotNum,question,learnType) values(?,?,?)";
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql, new String[]{info.getRobotNum(), info.getQuestion(), String.valueOf(info.getLearnType())});
        db.close();
    }

    //新增智能学习的答案
    public void addLearnAnswer(LearnAnswerInfo info) {
        String sql = "insert into answer(questionId,robotNum,answer,action,learnType) values(?,?,?,?,?)";
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql, new String[]{String.valueOf(info.getQuestionId()), info.getRobotNum(), info.getAnswer(), info.getAction(), String.valueOf(info.getLearnType())});
        db.close();
    }

    //更新智能学习的问题
    public void updateLearnQuestion(LearnQuestionInfo info) {
        String sql = "update question set question=? where id=? and learnType=?";
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql, new String[]{info.getQuestion(), String.valueOf(info.getId()), String.valueOf(info.getLearnType())});
        db.close();
    }

    //更新智能学习的答案
    public void updateLearnAnswer(LearnAnswerInfo info) {
        String sql = "update answer set answer=?,action=? where questionId=? and learnType=?";
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL(sql, new String[]{info.getAnswer(), info.getAction(), String.valueOf(info.getQuestionId()), String.valueOf(info.getLearnType())});
        db.close();
    }

    //获取智能学习问题的答案
    public List<LearnAnswerInfo> getLearnAnswers(int questionId) {
        String sql = "select * from answer where questionId=? order by id desc";
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(questionId)});
        List<LearnAnswerInfo> infos = new ArrayList<LearnAnswerInfo>();
        while (c.moveToNext()) {
            LearnAnswerInfo info = new LearnAnswerInfo();
            info.setAnswer(c.getString(c.getColumnIndex("answer")));
            info.setAction(c.getString(c.getColumnIndex("action")));
            infos.add(info);
        }
        c.close();
        db.close();
        return infos;
    }

    //获取问题id
    public int getQuesstionId(String quesstion) {
        String sql = "select id from question where question like ?";
        String[] selectionArgs = new String[]{"%" + quesstion + "%"};
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor c = db.rawQuery(sql, selectionArgs);
        int questionId = -1;
        if (c.moveToNext()) {
            questionId = c.getInt(c.getColumnIndex("id"));
        }
        c.close();
        db.close();
        return questionId;
    }

}
