package com.ji.app


import java.text.SimpleDateFormat
import java.util.Date

import com.alibaba.fastjson.JSON
import com.ji.Handler.filter
import com.ji.bean.Startuplog
import com.ji.constants.GmallConstant
import com.ji.utils.MyKafkaUtil
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}


object dailyUserApp {
  def main(args: Array[String]): Unit = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd HH")
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("streaming")
    //创建streamingContext
    val ssc = new StreamingContext(sparkConf,Seconds(3))

    //读取kafka数据，将时间戳转换为日期和时间并在样例类中添加相关数据
    val kafkaDStream = MyKafkaUtil.getKafkaStream(Set(GmallConstant.startup),ssc)
    val startUpLogDstream = kafkaDStream.map(record=>
      {
        //获取json，使用样例类类型
        val log = JSON.parseObject(record.value(),classOf[Startuplog])
        //获取时间戳
        val time = log.ts
        //将时间戳转换为时间
        val str = sdf.format(new Date(time))
        //分割出日期和时间
        val datas = str.split(" ")
        //json赋值
        log.logDate=datas(0)
        log.logHour=datas(1)
        //返回
        log
      }
    )

    //结合redis做跨批次去重
    val filterByRedisDStream:DStream[Startuplog] = filter.filterByRedis(startUpLogDstream,ssc.sparkContext)
    //同批次分组去重
    val filterByGroupDStream:DStream[Startuplog] = filter.filterByGroup(filterByRedisDStream)
    filterByGroupDStream.cache()
    //mid写入redis
    filter.saveMidToRedis(filterByGroupDStream)
    import org.apache.phoenix.spark._
    //数据写入HBase
    //把数据写入 hbase+phoenix
    filterByGroupDStream.foreachRDD{rdd=>{
        rdd.saveToPhoenix("GMALL2020_DAU",
        Seq("MID", "UID", "APPID", "AREA", "OS", "CH", "TYPE", "VS", "LOGDATE", "LOGHOUR", "TS"),
        new Configuration,Some("slaver1,slaver2,slaver3:2181")
        )}
    }
    startUpLogDstream.print()
    ssc.start()
    ssc.awaitTermination()
  }
}