package com.hatch.h5browse.database;

import android.util.Log;

import com.hatch.h5browse.bean.CollectionBean;
import com.hatch.h5browse.bean.DownloadBean;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class DownloadDao extends BaseDaoImp<DownloadBean>  {

    private static DownloadDao dao;

    public static DownloadDao getInstance() {
        if (dao == null)
            dao = new DownloadDao();
        return dao;
    }

    @Override
    public void insert(final DownloadBean downloadBean) throws Exception {
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DownloadBean dd = realm.createObject(DownloadBean.class);
                dd.id = downloadBean.id;
                dd.url = downloadBean.url;
                dd.fileName = downloadBean.fileName;
                dd.filePath = downloadBean.filePath;
                dd.fileIcon = downloadBean.fileIcon;
                dd.status = downloadBean.status;
                dd.progress = downloadBean.progress;
                dd.currentLength = downloadBean.currentLength;
                dd.totalLength = downloadBean.currentLength;
            }
        });
    }


    @Override
    public List<DownloadBean> findAll() throws Exception {
        return getRealm().where(DownloadBean.class).findAll();
    }

    @Override
    public DownloadBean find(String url) throws Exception {
        // 或者进行简化
        RealmResults<DownloadBean> result2 = getRealm().where(DownloadBean.class)
                .equalTo("url", url)
                .findAll();
        if (result2.size() > 0) {
            return result2.get(0);
        }
        return null;
    }

    @Override
    public void update(final DownloadBean downloadBean) throws Exception {
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                DownloadBean dd = realm.where(DownloadBean.class).equalTo("url", downloadBean.url).findFirst();
                if (dd != null) {
                    dd.id = downloadBean.id;
                    dd.url = downloadBean.url;
                    dd.fileName = downloadBean.fileName;
                    dd.filePath = downloadBean.filePath;
                    dd.fileIcon = downloadBean.fileIcon;
                    dd.status = downloadBean.status;
                    dd.progress = downloadBean.progress;
                    dd.currentLength = downloadBean.currentLength;
                    dd.totalLength = downloadBean.currentLength;
                }

            }
        });
    }

    @Override
    public void delete(final String url) throws Exception {
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<DownloadBean> result = realm.where(DownloadBean.class).equalTo("url", url).findAll();
                if (result != null)
                    result.deleteAllFromRealm();
            }
        });
    }

    @Override
    public void deleteAll() throws Exception {
        getRealm().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmResults<DownloadBean> result = realm.where(DownloadBean.class).findAll();
                if (result != null)
                    result.deleteAllFromRealm();
            }
        });
    }

    @Override
    public void close() {
        getRealm().close();
    }
}
