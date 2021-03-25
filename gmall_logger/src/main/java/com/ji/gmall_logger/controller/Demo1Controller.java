package com.ji.gmall_logger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ji.constants.GmallConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

//spring识别为Controller
//@RestController=@Controller+@ResponseBody
@RestController
//构建log对象
@Slf4j
public class Demo1Controller {
    //自动注入
    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    @RequestMapping("testDemo")//（地址）
    public String testDemo(){
        return "hello demo";
    }

    //从外部接收参数
    //http://localhost:8089/test2?aa=123
    @RequestMapping("test2")
    public String test2(@RequestParam("aa") String a){
        System.out.println(a);
        return "success";
    }
    //打印日志数据
    @RequestMapping("log")
    public String sendLogKafka(@RequestParam("logString") String logString){
        //创建Json对象，添加时间戳数据
        JSONObject jsonObject = JSON.parseObject(logString);
        jsonObject.put("ts",System.currentTimeMillis());
        //保存到本地
        log.info(jsonObject.toString());
        //根据type字段进行分类
        if("startup".equals(jsonObject.getString("type"))){
            kafkaTemplate.send(GmallConstant.startup,jsonObject.toString());
        }else {
            kafkaTemplate.send(GmallConstant.event,jsonObject.toString());
        }
        return "success";
    }
}
