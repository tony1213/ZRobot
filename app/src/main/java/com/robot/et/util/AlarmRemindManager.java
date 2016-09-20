package com.robot.et.util;

import android.text.TextUtils;
import android.util.Log;

import com.robot.et.common.DataConfig;
import com.robot.et.core.software.system.alarm.AlarmClock;
import com.robot.et.db.RobotDB;
import com.robot.et.entity.JpushInfo;
import com.robot.et.entity.RemindInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmRemindManager {
    private final static String TAG = "alarm";

    //设置闹铃
    public static void setAlarmClock(String date, String time) {
        Calendar calendar = DateTools.getCalendar(date, time);
        long currentMinute = System.currentTimeMillis();
        StringBuffer buffer = new StringBuffer(1024);
        String action = buffer.append(DateTools.getCurrentDate(currentMinute)).append(DataConfig.ACTION_REMIND_SIGN).append(DateTools.getCurrentTime(currentMinute)).toString();
        AlarmClock.getInstance().setOneAlarm(action, calendar);
    }

    //设置闹铃时间的格式   yyyy-MM-dd HH:mm:ss--->>转 yyyy-MM-dd HH:mm:00
    private static String setAlarmTimeFormat(String time) {
        String result = "";
        if (!TextUtils.isEmpty(time)) {
            result = time.substring(0, time.length() - 2) + "00";
        }
        return result;
    }

    //增加极光推送发来的闹铃
    public static void addAppAlarmClock(JpushInfo info) {
        // yyyy-MM-dd HH:mm:ss
        String alarmTime = info.getAlarmTime();
        String originalTime = alarmTime;
        alarmTime = setAlarmTimeFormat(alarmTime);
        String alarmContent = info.getAlarmContent();
        int remindNum = info.getRemindNum();
        int remindInteval = info.getRemindInteval();
        int frequency = info.getFrequency();

        Log.i(TAG, "alarmTime===" + alarmTime);
        Log.i(TAG, "alarmContent===" + alarmContent);
        Log.i(TAG, "remindNum===" + remindNum);
        Log.i(TAG, "remindInteval===" + remindInteval);
        Log.i(TAG, "frequency===" + frequency);
        if (!TextUtils.isEmpty(alarmTime)) {
            if (remindNum > 0) {
                for (int i = 0; i < remindNum; i++) {
                    long minute = DateTools.getlongTime(alarmTime);
                    minute = minute + i * remindInteval * 60 * 1000;
                    String tepmTime = DateTools.getCurrenTimeDetail(minute);
                    String[] times = tepmTime.split(" ");
                    String date = times[0];
                    String time = times[1];
                    Log.i(TAG, "date===" + date);
                    Log.i(TAG, "time===" + time);
                    setAlarmClock(date, time);
                    RemindInfo mInfo = new RemindInfo();
                    mInfo.setRobotNum(SharedPreferencesUtils.getInstance().getString(SharedPreferencesKeys.ROBOT_NUM, ""));
                    mInfo.setDate(date);
                    mInfo.setTime(time);
                    mInfo.setContent(alarmContent);
                    mInfo.setRemindInt(DataConfig.REMIND_NO_ID);
                    mInfo.setFrequency(frequency);
                    mInfo.setOriginalAlarmTime(originalTime);
                    addAlarm(mInfo);
                }
            }
        }
    }

    //增加App推送来的提醒信息
    public static void addAppAlarmRemind(RemindInfo info) {
        if (info != null) {
            String alarmTime = info.getOriginalAlarmTime();
            alarmTime = setAlarmTimeFormat(alarmTime);
            if (TextUtils.isEmpty(alarmTime)) {
                return;
            }
            String[] times = alarmTime.split(" ");
            String date = times[0];
            String time = times[1];
            Log.i(TAG, "date===" + date);
            Log.i(TAG, "time===" + time);
            setAlarmClock(date, time);
            info.setDate(date);
            info.setTime(time);
            info.setRemindInt(DataConfig.REMIND_NO_ID);
            addAlarm(info);
        }
    }

    //设置多次提醒
    public static void setMoreAlarm(long minute) {
        String tepmTime = DateTools.getCurrenTimeDetail(minute);
        String[] times = tepmTime.split(" ");
        String date = times[0];
        String time = times[1];
        Log.i(TAG, "date===" + date);
        Log.i(TAG, "time===" + time);
        setAlarmClock(date, time);
    }

    //获取提醒的内容
    public static List<RemindInfo> getRemindTips(long minute) {
        String date = DateTools.getCurrentDate(minute);
        int currentHour = DateTools.getCurrentHour(minute);
        String minuteTwo = DateTools.get2DigitMinute(minute);
        String time = new StringBuffer(1024).append(currentHour).append(":").append(minuteTwo).append(":").append("00").toString();
        RobotDB mDao = RobotDB.getInstance();
        List<RemindInfo> infos = null;
        try {
            infos = mDao.getRemindInfos(date, time, DataConfig.REMIND_NO_ID);
        } catch (Exception e) {
            Log.i(TAG, "dbutils  getRemindTips() Exception===" + e.getMessage());
            infos = new ArrayList<RemindInfo>();
        }
        return infos;
    }

    // 更新已经提醒的条目
    public static void updateRemindInfo(RemindInfo info, long minute, int frequency) {
        String date = DateTools.getCurrentDate(minute);
        RobotDB.getInstance().updateRemindInfo(info, date, frequency);
    }

    // 删除当前时间提醒的条目
    public static void deleteCurrentRemindTips(long minute) {
        String date = DateTools.getCurrentDate(minute);
        int currentHour = DateTools.getCurrentHour(minute);
        String currentMinute = DateTools.get2DigitMinute(minute);
        String time = new StringBuffer(1024).append(currentHour).append(":").append(currentMinute).append(":").append("00").toString();
        RobotDB.getInstance().deleteRemindInfo(date, time, DataConfig.REMIND_NO_ID);
    }

    // 删除app传来的提醒
    public static void deleteAppRemindTips(String originalTime) {
        if (!TextUtils.isEmpty(originalTime)) {
            RobotDB.getInstance().deleteAppRemindInfo(originalTime);
        }
    }

    // 增加Ifly提醒的操作 格式：日期 + 时间 + 做什么事 + 说的日期 + 说的时间
    private static boolean addIflyRemind(RemindInfo remindInfo) {
        if (remindInfo != null) {
            RemindInfo info = new RemindInfo();
            info.setRobotNum(SharedPreferencesUtils.getInstance().getString(SharedPreferencesKeys.ROBOT_NUM, ""));
            info.setDate(remindInfo.getDate());
            info.setTime(setAlarmTimeFormat(remindInfo.getTime()));
            info.setContent(remindInfo.getContent());
            info.setRemindInt(DataConfig.REMIND_NO_ID);
            info.setFrequency(1);
            boolean flag = addAlarm(info);
            if (flag) {
                setAlarmClock(remindInfo.getDate(), remindInfo.getTime());
            }
            return flag;
        }
        return false;
    }

    //讯飞提醒提示
    public static String getIflyRemindTips(RemindInfo info) {
        // 日期 + 时间 + 做什么事 + 说的日期 + 说的时间
        boolean flag = addIflyRemind(info);
        String content = "";
        if (flag) {
            // 格式：好的，明天xx时间提醒xx
            StringBuffer buffer = new StringBuffer();
            buffer.append("好的，");
            String speakDate = info.getSpeakDate();//要说的日期
            if (!TextUtils.isEmpty(speakDate)) {
                buffer.append(speakDate);
            }
            String speakTime = info.getSpeakTime();//要说的时间
            if (!TextUtils.isEmpty(speakTime)) {
                buffer.append(speakTime);
            }
            buffer.append("提醒");
            buffer.append(info.getContent());
            content = buffer.toString();
        } else {
            content = "主人，这个提醒我已经记住了呢，不用重复提醒哦";
        }
        return content;
    }

    //多个闹铃提示
    public static String getMoreAlarmContent() {
        String content = "";
        List<String> datas = getAlarmDatas();
        int size = datas.size();
        Log.i(TAG, "datas.size====" + size);
        if (datas != null && size > 0) {
            content = "主人，要" + datas.get(size - 1) + "了";
            datas.remove(size - 1);
            setAlarmDatas(datas);
        }
        // 设置单次闹铃
        setSpeakRemindContent(content);
        return content;
    }

    //增加闹铃
    private static boolean addAlarm(RemindInfo info) {
        RobotDB mDb = RobotDB.getInstance();
        RemindInfo mInfo = mDb.getRemindInfo(info);
        if (mInfo != null) {
            Log.i(TAG, "闹铃已存在");
            return false;
        }
        mDb.addRemindInfo(info);
        Log.i(TAG, "增加闹铃成功");
        return true;
    }

    //多次提醒的数据
    private static List<String> alarmDatas = new ArrayList<String>();

    public static List<String> getAlarmDatas() {
        return alarmDatas;
    }

    public static void setAlarmDatas(List<String> alarmDatas) {
        AlarmRemindManager.alarmDatas = alarmDatas;
    }

    //要求回答的内容
    private static String requireAnswer;
    //不回答时要求做的类型
    private static int spareType;
    //不回答时要求做的内容
    private static String spareContent;
    //提醒的人
    private static String remindMen;
    //原始的时间
    private static String originalAlarmTime;
    // 提醒要反复说的话
    private static String speakRemindContent;

    public static String getRequireAnswer() {
        return requireAnswer;
    }

    public static void setRequireAnswer(String requireAnswer) {
        AlarmRemindManager.requireAnswer = requireAnswer;
    }

    public static int getSpareType() {
        return spareType;
    }

    public static void setSpareType(int spareType) {
        AlarmRemindManager.spareType = spareType;
    }

    public static String getSpareContent() {
        return spareContent;
    }

    public static void setSpareContent(String spareContent) {
        AlarmRemindManager.spareContent = spareContent;
    }

    public static String getRemindMen() {
        return remindMen;
    }

    public static void setRemindMen(String remindMen) {
        AlarmRemindManager.remindMen = remindMen;
    }

    public static String getOriginalAlarmTime() {
        return originalAlarmTime;
    }

    public static void setOriginalAlarmTime(String originalAlarmTime) {
        AlarmRemindManager.originalAlarmTime = originalAlarmTime;
    }

    public static String getSpeakRemindContent() {
        return speakRemindContent;
    }

    public static void setSpeakRemindContent(String speakRemindContent) {
        AlarmRemindManager.speakRemindContent = speakRemindContent;
    }
}
