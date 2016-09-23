package com.robot.et.core.software.ros.connect.second;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.github.rosjava.android_remocons.common_tools.master.MasterId;
import com.github.rosjava.android_remocons.common_tools.master.RoconDescription;
import com.robot.et.common.DataConfig;
import com.robot.et.core.software.common.speech.SpeechImpl;

import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MasterFactory extends Activity {

    private List<RoconDescription> masters;
    private List<MasterItem> masterItems;
    private Yaml yaml = new Yaml();

    private static final String masterUri = "http://192.168.2.158:11311/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        masters = new ArrayList<RoconDescription>();
        readMasterList();

    }

    private void readMasterList() {
        String str = null;
        Cursor c = getContentResolver().query(Database.CONTENT_URI, null, null, null, null);
        if (c == null) {
            Log.e("Remocon", "master chooser provider failed!!!");
            return;
        }
        if (c.getCount() > 0) {
            c.moveToFirst();
            str = c.getString(c.getColumnIndex(Database.TABLE_COLUMN));
            Log.i("Remocon", "[MasterChooser] master chooser found a rocon master: " + str);
        }
        if (str != null) {
            Log.e("Remocon","str!=null");
            masters = (List<RoconDescription>) yaml.load(str);
            Log.e("Remocon","master.size()="+masters.size());
            if (masters.size()==0){
                enterMasterInfo();
            }else {
                for (int i = 0;i<masters.size();i++){
                    RoconDescription concert=masters.get(i);
                    if (concert.getMasterUri().equals(masterUri)){
                        if (concert == null || concert.getConnectionStatus() == null || concert.getConnectionStatus().equals(RoconDescription.ERROR)) {
                            Log.e("Remocon","Failed: Cannot contact concert");
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT,"连接异常，请检查。错误码1");
                        } else if ( concert.getConnectionStatus().equals(RoconDescription.UNAVAILABLE) ) {
                            Log.e("Remocon","Master Unavailable!   Currently busy serving another.");
                            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT,"连接异常，请检查。错误码2");
                        } else {
                            choose(0);
                        }
                    }
                }
            }
        } else {
            Log.e("Remocon","str==null");
            enterMasterInfo();
        }
    }

    public void writeMasterList() {
        Log.i("Remocon", "master chooser saving rocon master details...");
        String str = null;
        final List<RoconDescription> tmp = masters; // Avoid race conditions
        if (tmp != null) {
            str = yaml.dump(tmp);
        }
        ContentValues cv = new ContentValues();
        cv.put(Database.TABLE_COLUMN, str);
        Uri newEmp = getContentResolver().insert(Database.CONTENT_URI, cv);
        if (newEmp != Database.CONTENT_URI) {
            Log.e("Remocon", "master chooser could not save concert, non-equal URI's");
        }
    }

    private void enterMasterInfo(){
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("URL", masterUri);
        try {
            addMaster(new MasterId(data));
        } catch (Exception e) {
            Log.e("Remocon","Invalid Parameters.");
        }
    }

    private void addMaster(MasterId masterId) {
        addMaster(masterId, false);
    }

    private void addMaster(MasterId masterId, boolean connectToDuplicates) {
        Log.i("MasterChooserActivity", "adding master to the concert master chooser [" + masterId.toString() + "]");
        if (masterId == null || masterId.getMasterUri() == null) {
            SpeechImpl.getInstance().startSpeak(DataConfig.SPEAK_TYPE_CHAT,"连接异常，请检查。错误码3");
        } else {
            for (int i = 0; i < masters.toArray().length; i++) {
                RoconDescription concert = masters.get(i);
                if (concert.getMasterId().equals(masterId)) {
                    if (connectToDuplicates) {
                        choose(0);
                        return;
                    } else {
                        Toast.makeText(this, "That concert is already listed.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            Log.i("MasterChooserActivity", "creating concert description: " + masterId.toString());
            masters.add(RoconDescription.createUnknown(masterId));
            Log.i("MasterChooserActivity", "description created");
            onMastersChanged();
        }
    }

    private void onMastersChanged(){
        writeMasterList();
        masterItems = new ArrayList<MasterItem>();
        if (masters != null) {
            for (int i = 0; i < masters.size(); i++) {
                masterItems.add(new MasterItem(masters.get(i), this));
            }
        }
        //bug：当时没有获取到NameSpace，所以在这里面等待了5秒
//        try {
//            Thread.sleep(5000);
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }finally {
//            choose(0);
//        }
        choose(0);
    }

    private void choose(int position){
        RoconDescription concert = masters.get(position);
        Log.e("Remocon","Master in database");
        Intent resultIntent = new Intent();
        resultIntent.putExtra(RoconDescription.UNIQUE_KEY, concert);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
