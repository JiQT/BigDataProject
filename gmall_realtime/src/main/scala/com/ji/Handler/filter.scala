package com.ji.Handler

import java.text.SimpleDateFormat
import java.util.Date

import com.ji.bean.Startuplog
import com.ji.utils.RedisUtil
import org.apache.spark.SparkContext
import org.apache.spark.streaming.dstream.DStream

object filter {

  private val sdf=new SimpleDateFormat("yyyy-MM-dd")

  def saveMidToRedis(filterByGroupDStream: DStream[Startuplog]) = {
    //将数据转换为RDD进行写库操作
    filterByGroupDStream.foreachRDD(rdd=>{
      //按分区操作rdd
      rdd.foreachPartition(iter=>{
        //获取redis连接
        val jedisClient=RedisUtil.getJedisClient
        //写库操作
        iter.foreach(log=>jedisClient.sadd(s"dau:${log.logDate}",log.mid))
        //释放连接
        jedisClient.close()
      })
    })
  }

  def filterByGroup(filterByRedisDStream: DStream[Startuplog]): DStream[Startuplog] = {
    //对去重后的数据集，按照mid分组
    val midToLogDStream = filterByRedisDStream.map(log=>(log.mid,log))
    //按照mid分组
    val midToLogDStreamgroups = midToLogDStream.groupByKey()
    //按照时间排序，取第一条
    val value = midToLogDStreamgroups.flatMapValues(iter => {
      //自定义的比较函数进行排序, <表示升序
      iter.toList.sortWith(_.ts < _.ts).take(1)
    })
    value.map(_._2)
  }

  def filterByRedis(startUpLogDstream:DStream[Startuplog],sc:SparkContext):DStream[Startuplog]={
//    //数据转换为rdd
//    val res = startUpLogDstream.transform(rdd => {
//      //对rdd的每个分区单独处理，减少创建连接
//      val filterRDD = rdd.mapPartitions(iter => {
//        //获取redis连接
//        val client = RedisUtil.getJedisClient
//        //过滤
//        val logs = iter.filter(log => client.sismember(s"dau:${log.logDate}", log.mid))
//        //关闭连接
//        client.close()
//        //返回
//        logs
//      })
//      filterRDD
//    })
//    //过滤后的返回值
//    res

    //方案二：使用广播变量
    //transform:任意RDD到RDD函数应用于DStream
    val res=startUpLogDstream.transform(rdd=>{
      //获取当前时间
      val ts = System.currentTimeMillis()
      val date = sdf.format(new Date(ts))
      //获取redis连接
      val jedisclient=RedisUtil.getJedisClient
      //获取用户信息并广播
      //Smembers 命令返回集合中的所有的成员
      val uids = jedisclient.smembers(s"dau:$date")
      val uidsBC = sc.broadcast(uids)
      //关闭连接
      jedisclient.close()
      //过滤
      rdd.filter(log => !uidsBC.value.contains(log.mid))
    })
    res
  }
}
