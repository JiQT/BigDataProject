package com.ji.app;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogUploader {
    public static void sendLogStream(String log){
        try{
            //不同的日志类型对应不同的 URL
            //端口号要改为80连接nginx，否则为直连，只有slaver1的本地有数据，2，3，没有数据
            URL url =new URL("http://slaver1:80/log");
            HttpURLConnection conn = (HttpURLConnection)
            url.openConnection();
            //设置请求方式为 post
            conn.setRequestMethod("POST");
            //时间头用来供 server 进行时钟校对的
            conn.setRequestProperty("clientTime",System.currentTimeMillis() + "");
            //允许上传数据
            conn.setDoOutput(true);
            //设置请求的头信息,设置内容类型为 JSON
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            System.out.println("upload" + log);
            //输出流
            OutputStream out = conn.getOutputStream();
            out.write(("logString="+log).getBytes());
            out.flush();
            out.close();
            int code = conn.getResponseCode();
            System.out.println(code);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
