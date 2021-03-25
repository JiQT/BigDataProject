package com.ji.gmall_publication.service.impl;

import com.ji.gmall_publication.mapper.DauMapper;
import com.ji.gmall_publication.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublisherServiceImpl implements PublisherService {
    //通过 @Autowired的使用来消除 set ，get方法
    @Autowired
    private DauMapper dauMapper;
    @Override
    public Integer getRealtimeTotal(String date) {
        return dauMapper.selectDauTotal(date);
    }

    @Override
    public Map getDauTotalHourMap(String date) {
        //查询phoenix数据
        List<Map> list = dauMapper.selectDauTotalHourMap(date);
        //创建map存放调整完结构的数据
        HashMap<String, Long> resMap = new HashMap<>();
        //调整结构并遍历list
        for(Map map:list){
            resMap.put((String)map.get("LH"),(Long)map.get("CT"));
        }
        return resMap;
    }
}
