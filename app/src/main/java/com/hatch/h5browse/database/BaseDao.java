package com.hatch.h5browse.database;

import com.hatch.h5browse.bean.CollectionBean;

import java.util.List;


public interface BaseDao<T> {

    void insert(T t) throws Exception;

    List<T> findAll() throws Exception;

    T find(String id) throws Exception;

    void update(T t) throws Exception; //通过对象更新

    void delete(String id) throws Exception;

    void deleteAll() throws Exception;

    void close();

}
