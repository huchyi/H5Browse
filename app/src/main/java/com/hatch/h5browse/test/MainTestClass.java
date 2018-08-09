package com.hatch.h5browse.test;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainTestClass {
    public static void main(String[] arg) {
        String url = "http://bbs.tianya.cn/m/post-house-252774-";//"http://bbs.tianya.cn/m/post-house-252774-50.shtml";
        getData(url);
    }


    private static void getData(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> list = new ArrayList<>();
                //建立头文件
                list.add("<html>");
                list.add("<body style=\"margin: 0 auto; width: 800px; text-align: center;\">");


                for (int i = 1; i <= 521; i++) {
                    ArrayList<String> list2 = get(url + i + ".shtml");
                    if (list.size() > 0) {
                        list.addAll(list2);
                    }
                    try {
                        int time = (int) (Math.random() * 6);
                        if (time < 3) {
                            time = 3;
                        }
                        System.out.println("第" + i + "页，等待时间为：" + time + ",数据条数：" + list2.size());
                        Thread.sleep(1000 * time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                //建立结尾的文件
                list.add("</body></html>");

                String[] content = new String[list.size()];
                System.out.println("===========结束，开始写数据，数据总体条数" + content.length);
                list.toArray(content);
                writeLineFile("D:\\getData.html", content);

            }
        }).start();
    }


    private static ArrayList<String> get(String urlStr) {
        ArrayList<String> list = new ArrayList<>();
        try {
            //创建URL对象
            URL url = new URL(urlStr);
            //url.openConnection()是调用url对象上的openConnection方法，将得到一个URLConnection对象赋值给connection
            URLConnection connection = url.openConnection();
            //获取当前网络输入流
            InputStream is = connection.getInputStream();
            //字节到字符的转换
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            boolean isMain = false;
            boolean isStart = false;
            while ((line = br.readLine()) != null) {
                if (line.contains("data-id=\"10391542\"")) {
                    isMain = true;
                }
                if (isMain) {
                    if (line.contains("<div class=\"reply-div\">")) {
                        isStart = true;
                    } else if (line.contains("class=\"time fc-gray\"")) {
                        list.add(line);
                    }
                    if (isStart) {
                        if (line.length() > 0) {
                            list.add(line);
                        }
                        if (line.contains("</div>")) {
                            list.add("<p>--------------------------------------------------------------------------------------------------------------------</p>");
                            list.add("<p>--------------------------------------------------------分割线-------------------------------------------------------</p>");
                            list.add("<p>--------------------------------------------------------------------------------------------------------------------</p>");
                            isMain = false;
                            isStart = false;
                        }
                    }
                }
//                System.out.print(line + "\n\r");
            }
            br.close();
            isr.close();
            is.close();
        } catch (MalformedURLException e) {
            System.out.println("-------MalformedURLException:" + e);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("-------IOException:" + e);
            e.printStackTrace();
        }

        return list;
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
