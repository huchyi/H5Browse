package com.hatch.h5browse.database;


import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import io.realm.Realm;

public class BaseDaoImp<T> implements BaseDao<T> {




    public Realm getRealm() {
        return MyRealmMigration.getIntance().getRealm();
    }

    @Override
    public void insert(T t) throws Exception {

    }

    @Override
    public List<T> findAll() throws Exception {
        return null;
    }

    @Override
    public T find(String id) throws Exception {
        return null;
    }

    @Override
    public void update(T t) throws Exception {

    }


    @Override
    public void delete(String id) throws Exception {

    }

    @Override
    public void deleteAll() throws Exception {

    }

    @Override
    public void close() {

    }
}
