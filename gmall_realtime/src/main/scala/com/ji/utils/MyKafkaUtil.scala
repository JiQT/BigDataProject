package com.ji.utils

import java.io.InputStreamReader
import java.util.Properties
import java.util.Properties

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

object MyKafkaUtil {
  //1.读取配置文件
  private val properties: Properties = propertiesUtils.load("config.properties")
  //2.kafka 消费者配置
  val kafkaParam = Map[String,Object](
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG->properties.get("kafka.broker.list"),
    ConsumerConfig.GROUP_ID_CONFIG->properties.getProperty("kafka.group.id"),
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG->classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG->classOf[StringDeserializer]
  )

  // 创建 DStream，返回接收到的输入数据
  // LocationStrategies：根据给定的主题和集群地址创建 consumer
  // LocationStrategies.PreferConsistent：持续的在所有 Executor 之间分配分区
  // ConsumerStrategies：选择如何在 Driver 和 Executor 上创建和配置 KafkaConsumer
  // ConsumerStrategies.Subscribe：订阅一系列主题

  def getKafkaStream(topic: Set[String], ssc: StreamingContext):
  InputDStream[ConsumerRecord[String, String]] = {
    val dStream: InputDStream[ConsumerRecord[String, String]] =
      //ssc,位置策略，消费策略
      KafkaUtils.createDirectStream[String, String](
        ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String,String](topic, kafkaParam))
    dStream
  }
}
