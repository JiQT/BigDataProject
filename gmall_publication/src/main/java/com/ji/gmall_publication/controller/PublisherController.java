package com.ji.gmall_publication.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.internal.cglib.core.$LocalVariablesSorter;
import com.ji.gmall_publication.service.PublisherService;
import com.ji.gmall_publication.service.impl.PublisherServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class PublisherController {
    //自动注入,找到实现类
    @Autowired
    private PublisherService publisherService;

    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-HH-dd");
    @RequestMapping("realtime-total")
    public String getRealtimeTotal(@RequestParam("date") String date){
        System.out.println("success getRealtimeTotal");
        //构建service实现类对象，调用getRealtimeTotal
        //获取phoenix数据
        Integer dauTotal = publisherService.getRealtimeTotal(date);
        //创建集合存放结果Map
        ArrayList<Map> res = new ArrayList<>();
        //创建Map存放日活数据
        Map<String, Object> dauMap = new HashMap<>();
        dauMap.put("id","dau");
        dauMap.put("name","新增日活");
        dauMap.put("value",dauTotal);

        Map<String, Object> newMidMap = new HashMap<>();
        newMidMap.put("id","new_mid");
        newMidMap.put("name","新增设备");
        newMidMap.put("value",233);

        //map数据放入集合
        res.add(dauMap);
        res.add(newMidMap);
        return JSONObject.toJSONString(res);
    }


    @RequestMapping("realtime-hours")
    public String getDauTotalHourMap(@RequestParam("id") String id,@RequestParam("date") String date){
        //创建map存放最终结果
        System.out.println("success getDauTotalHourMap");
        HashMap<String, Map> res = new HashMap<>();

        //查询今天的日活数据
        Map todayDauTotalHour = publisherService.getDauTotalHourMap(date);
        Calendar calendar = Calendar.getInstance();
        String yesterday=null;
        try {
            calendar.setTime(sdf.parse(date));
            calendar.add(Calendar.DAY_OF_MONTH,-1);
            yesterday = sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //查询昨天的日活数据
        Map yesterdayDauTotalHour = publisherService.getDauTotalHourMap(yesterday);
        res.put("yesterday",yesterdayDauTotalHour);
        res.put("today",todayDauTotalHour);
        return JSONObject.toJSONString(res);
    }
}
