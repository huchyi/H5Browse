package com.hatch.h5browse.database;

import android.util.Log;

import com.hatch.h5browse.bean.CollectionBean;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

import static io.realm.Realm.getDefaultInstance;

public class MyRealmMigration implements RealmMigration {
    private static MyRealmMigration sIntance;

    public MyRealmMigration() {
    }

    /**
     * 双检索单例
     *
     * @return
     */
    public static MyRealmMigration getIntance() {
        if (sIntance == null) {
            synchronized (MyRealmMigration.class) {
                if (sIntance == null) {
                    sIntance = new MyRealmMigration();
                }
            }
        }


        return sIntance;
    }

    /**
     * 获取realm对象
     *
     * @return
     */
    public Realm getRealm() {
        Realm realm = getDefaultInstance();
        return realm;
    }

    /**
     * 版本升级处理
     *
     * @param realm
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
//        RealmSchema schema = realm.getSchema();
//        Log.i("hcy", "oldVersion:" + oldVersion);
//        switch ((int) oldVersion) {
//            case 2: {
//                schema.get(CollectionBean.class.getName()).addField("id", String.class)//添加字段
//                        .addField("iconPath", String.class);//添加字段
////                        .removeField("age");//移除age属性
//                //注意version不要break,因为前面的版本都要升级
//                oldVersion++;
//            }
//        }

    }
}
