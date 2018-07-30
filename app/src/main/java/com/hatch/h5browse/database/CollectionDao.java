package com.hatch.h5browse.database;

import com.hatch.h5browse.bean.CollectionBean;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmResults;

public class CollectionDao extends BaseDaoImp<CollectionBean> {


    private static CollectionDao dao;

    public static CollectionDao getInstance() {
        if (dao == null)
            dao = new CollectionDao();
        return dao;
    }

    @Override
    public void insert(final CollectionBean cc) throws Exception {
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CollectionBean collectionBean = realm.createObject(CollectionBean.class);
                collectionBean.id = cc.id;
                collectionBean.url = cc.url;
                collectionBean.title = cc.title;
                collectionBean.iconPath = cc.iconPath;
            }
        });
    }

    @Override
    public List<CollectionBean> findAll() throws Exception {
        return getRealm().where(CollectionBean.class).findAll();
    }

    @Override
    public CollectionBean find(String url) throws Exception {
        // 或者进行简化
        RealmResults<CollectionBean> result2 = getRealm().where(CollectionBean.class)
                .equalTo("url", url)
                .findAll();
        if (result2.size() > 0) {
            return result2.get(0);
        }
        return null;

    }

    @Override
    public void update(final CollectionBean collectionBean) throws Exception {
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                CollectionBean cb = realm.where(CollectionBean.class).equalTo("id", collectionBean.id).findFirst();
                cb.id = collectionBean.id;
                cb.url = collectionBean.url;
                cb.title = collectionBean.title;
                cb.iconPath = collectionBean.iconPath;
            }
        });
//        getRealm().executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.insertOrUpdate(collectionBean);
//            }
//        });
    }

    @Override
    public void delete(final String id) throws Exception {
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<CollectionBean> result = realm.where(CollectionBean.class).equalTo("id", id).findAll();
                if (result != null)
                    result.deleteAllFromRealm();
            }
        });

    }

    @Override
    public void deleteAll() {
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<CollectionBean> result = realm.where(CollectionBean.class).findAll();
                if (result != null)
                    result.deleteAllFromRealm();
            }
        });
    }

    @Override
    public void close() {
        getRealm().close();
    }

    //分页查询RealmResults <User> result = realm.where(User.class).equalTo("id", id).findAll();
    public static <E extends RealmModel> List<E> getLimitList(RealmResults<E> data, int offset, int limit) {
        List<E> obtainList = new ArrayList();
        Realm realm = Realm.getDefaultInstance();
        if (data.size() == 0) {
            return obtainList;
        }
        for (int i = offset; i < offset + limit; i++) {
            if (i >= data.size()) {
                break;
            }
            E temp = realm.copyFromRealm(data.get(i));
            obtainList.add(temp);
        }
        realm.close();
        return obtainList;
    }
}
