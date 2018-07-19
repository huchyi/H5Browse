package com.hatch.h5browse;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.hatch.h5browse.common.Config;
import com.hatch.h5browse.database.MyRealmMigration;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {

    private static MyApplication myApplication = null;

    public static Context getApplication() {
        return myApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        ZXingLibrary.initDisplayOpinion(this);
        initRealm();
    }

    private void initWeb(){}

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(Config.dbName)
                .schemaVersion(Config.dbVersion)
//                .encryptionKey(Config.dbKey.getBytes())
                .migration(MyRealmMigration.getIntance())//升级数据库处理类（实现RealmMigration接口）
                .build();
        Realm.setDefaultConfiguration(config);
    }


    private static Toast mToast;
    @SuppressLint("ShowToast")
    public static void showToast(String msg) {
        if (mToast != null) {
            mToast.setText(msg);
        } else {
            /*
             * 第一个参数：当前的上下文环境，用this或getApplicationContext()表示。
             * 第二个参数：显示的字符串，用R.string表示。
             * 第三个参数：显示的时间长短。用LENGTH_LONG(长)或LENGTH_SHORT(短)表示，也可以用毫秒。
             */
            mToast = Toast.makeText(MyApplication.getApplication().getApplicationContext(), msg, Toast.LENGTH_SHORT);
        }
        mToast.show(); //显示toast信息
    }
}
