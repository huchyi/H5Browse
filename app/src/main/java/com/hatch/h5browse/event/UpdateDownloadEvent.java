package com.hatch.h5browse.event;

import com.liulishuo.okdownload.DownloadTask;

import java.security.PublicKey;

public class UpdateDownloadEvent {
    public int START = 1;
    public int PROGRESS = 2;
    public int END = 3;
    public int STOP = 4;


    public int status = 0;
    public DownloadTask task;
    public long currentLength;
    public long totalLength;
    public int processP;
    public String speed;

}
