package com.hatch.h5browse.test;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class MainTestClass {
    public static void main(String[] arg) {
        readLineFile("D:\\Tencent data\\503683716\\FileRecv\\MobileFile\\AdguardFilters-master\\MobileFilter\\sections\\adservers.txt");
    }

    public static void readLineFile(String filename) {
        try {
            FileInputStream in = new FileInputStream(filename);
            InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
            BufferedReader bufReader = new BufferedReader(inReader);
            String line = null;
            int i = 1;
            String[] content;
            ArrayList<String> list = new ArrayList<>();
            while ((line = bufReader.readLine()) != null) {
                System.out.println("第" + i + "行：" + line);
                if (line.length() > 2 && line.startsWith("||")) {
                    if (line.contains("^")) {
                        line = line.substring(2, line.indexOf("^"));
                    } else {
                        line = line.substring(2, line.length());
                    }
                    list.add(line);
                }
                i++;
            }
            bufReader.close();
            inReader.close();
            in.close();
            content = new String[list.size()];
            list.toArray(content);
            writeLineFile("D:\\Tencent data\\503683716\\FileRecv\\MobileFile\\AdguardFilters-master\\MobileFilter\\sections\\test.txt", content);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取" + filename + "出错！");
        }
    }


    public static void writeLineFile(String filename, String[] content) {
        try {
            FileOutputStream out = new FileOutputStream(filename);
            OutputStreamWriter outWriter = new OutputStreamWriter(out, "UTF-8");
            BufferedWriter bufWrite = new BufferedWriter(outWriter);
            for (int i = 0; i < content.length; i++) {
                bufWrite.write("<item>" + content[i] + "</item>\r\n");
            }
            bufWrite.close();
            outWriter.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取" + filename + "出错！");
        }
    }
}
