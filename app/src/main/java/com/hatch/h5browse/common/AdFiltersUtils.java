package com.hatch.h5browse.common;

import android.util.Log;

import com.hatch.h5browse.MyApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 广告过滤规则获取，主要针对资源型的文件，页面同域名的标签广告无能为力，只能定点标记
 * https://github.com/AdguardTeam/AdguardFilters/
 */
public class AdFiltersUtils {
    public static String[] adIP;

    static {
        boolean isSuccess = readAdFile(Utils.getParentFileString(MyApplication.getApplication()) + "ad/ad.txt");
        if (!isSuccess) {
            //git上拉取，zip解压，加载，存储
            File file = new File(Utils.getParentFileString(MyApplication.getApplication()) + "ad/ad.txt");
            Long lastModified = file.lastModified();
            if (System.currentTimeMillis() - lastModified > 1000 * 60 * 60 * 24 * 2) {
                loadFileAndUnzip();
            }
        }
    }

    private static void loadFileAndUnzip() {
        final String url = "https://github.com/AdguardTeam/AdguardFilters/blob/master/MobileFilter/sections/adservers.txt";
        final File parentFile = new File(Utils.getParentFileString(MyApplication.getApplication()) + "ad/adservers.txt");
        DownloadManager.startTask(url, parentFile, new DownloadManager.CallBack() {
            @Override
            public void success(String filePath) {
                readOnLineFile(filePath);
            }
        });
    }

    /**
     * 获取本地存储的文件
     */
    private static boolean readAdFile(String filename) {
        try {
            FileInputStream in = new FileInputStream(filename);
            InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
            BufferedReader bufReader = new BufferedReader(inReader);
            String line;
            int i = 1;
            ArrayList<String> list = new ArrayList<>();
            while ((line = bufReader.readLine()) != null) {
                list.add(line);
                i++;
            }
            bufReader.close();
            inReader.close();
            in.close();
            if (list.size() > 0) {
                adIP = new String[list.size()];
                list.toArray(adIP);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 获取线上的文件
     */
    private static void readOnLineFile(String filePath) {
        try {
            FileInputStream in = new FileInputStream(filePath);
            InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
            BufferedReader bufReader = new BufferedReader(inReader);
            String line = null;
            int i = 1;
            ArrayList<String> list = new ArrayList<>();
            while ((line = bufReader.readLine()) != null) {
                if (line.length() > 2 && line.contains("||")) {
                    if (line.contains("^</td>")) {
                        line = line.substring(line.indexOf("||") + 2, line.indexOf("^</td>"));
                        if (line.length() > 0 && line.contains("^")) {
                            line = line.substring(0, line.indexOf("^"));
                        }
                    } else if (line.contains("</td>")) {
                        line = line.substring(line.indexOf("||") + 2, line.indexOf("</td>"));
                        if (line.length() > 0 && line.contains("^")) {
                            line = line.substring(0, line.indexOf("^"));
                        }
                    } else if (line.contains("^")) {
                        line = line.substring(line.indexOf("||") + 2, line.indexOf("^"));
                    } else {
                        line = line.substring(line.indexOf("||") + 2, line.length());
                    }
                    list.add(line);
                }
                i++;
            }
            bufReader.close();
            inReader.close();
            in.close();
            adIP = new String[list.size()];
            list.toArray(adIP);
            writeLineFile(adIP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 保存线上的文件到本地
     */
    private static void writeLineFile(String[] content) {
        try {
            FileOutputStream out = new FileOutputStream(Utils.getParentFileString(MyApplication.getApplication()) + "ad/ad.txt");
            OutputStreamWriter outWriter = new OutputStreamWriter(out, "UTF-8");
            BufferedWriter bufWrite = new BufferedWriter(outWriter);
            for (String str : content) {
                bufWrite.write(str + "\r\n");
            }
            bufWrite.close();
            outWriter.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 解压文件
     */
    private static void unzipFile(String filePath) {
        try {
            List<File> files = ZipUtils.unzipFile(filePath, Utils.getParentFileString(MyApplication.getApplication()) + "ad/zip/");
            for (File file : files) {
                String fileName = file.getName();
                String fileP = file.getPath();
                if (fileP.contains("MobileFilter") && fileName.contains("adservers")) {
                    readOnLineFile(fileP);
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
