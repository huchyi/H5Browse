package com.hatch.h5browse.database;

import android.util.Log;

import com.hatch.h5browse.bean.CollectionBean;
import com.hatch.h5browse.bean.KeyHistoryBean;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class KeyHistoryDao extends BaseDaoImp<KeyHistoryBean> {


    private static KeyHistoryDao dao;

    public static KeyHistoryDao getInstance() {
        if (dao == null)
            dao = new KeyHistoryDao();
        return dao;
    }

    @Override
    public void insert(final KeyHistoryBean keyHistoryBean) throws Exception {
        Log.i("hcy", "KeyHistoryDao");
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                KeyHistoryBean kk = realm.createObject(KeyHistoryBean.class);
                kk.id = keyHistoryBean.id;
                kk.key = keyHistoryBean.key;
                kk.url = keyHistoryBean.url;
            }
        });
    }

    @Override
    public void deleteAll() throws Exception {
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<KeyHistoryBean> result = realm.where(KeyHistoryBean.class).findAll();
                if (result != null)
                    result.deleteAllFromRealm();
            }
        });
    }

    @Override
    public List<KeyHistoryBean> findAll() throws Exception {
        return getRealm().where(KeyHistoryBean.class).findAll();
    }
}
