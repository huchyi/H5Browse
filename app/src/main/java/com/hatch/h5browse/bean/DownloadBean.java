package com.hatch.h5browse.bean;

import io.realm.RealmObject;

public class DownloadBean extends RealmObject {

    public String id;
    public String url;
    public String fileName;
    public String filePath;
    public String fileIcon;
    public int status; //下载的状态 0:未下载  1：正在下载  2：下载成功
    public int progress; //当前下载的百分比
    public String currentLength; //当前下载的大小
    public String totalLength; //总大小
    public String speed; //下载速度

}
