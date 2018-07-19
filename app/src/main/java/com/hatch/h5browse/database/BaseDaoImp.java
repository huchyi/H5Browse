package com.hatch.h5browse.database;


import java.util.List;

public class BaseDaoImp<T> implements BaseDao<T> {
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
