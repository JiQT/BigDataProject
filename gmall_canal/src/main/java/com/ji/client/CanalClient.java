package com.ji.client;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.google.protobuf.InvalidProtocolBufferException;
import com.ji.utils.MyKafkaSender;

import java.net.InetSocketAddress;
import java.util.List;

public class CanalClient {
    public static void main(String[] args) {
        //获取canal连接器,没有用户名和密码
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("master", 11111), "example", "", "");
        //抓取数据
        while (true){
            //连接
            canalConnector.connect();
            //指定订阅的数据库
            canalConnector.subscribe("gmall.*");
            //执行抓取数据操作
            Message message = canalConnector.get(1024);
            //判断当前抓取的数据是否为空，空则休息
            System.out.println(message.getEntries().size());
            if (message.getEntries().size()==0){
                System.out.println("没有数据，休息");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                //解析message
                for(CanalEntry.Entry entry:message.getEntries()){
                    //对数据类型做判断
                    if (CanalEntry.EntryType.ROWDATA.equals(entry.getEntryType())){
                        //获取当前数据表明
                        String tableName = entry.getHeader().getTableName();
                        //将StoreValue反序列化
                        try {
                            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                            List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                            CanalEntry.EventType eventType = rowChange.getEventType();
                            //处理数据
                            handel(tableName,rowDatasList,eventType);
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    //处理监控mysql数据，解析
    private static void handel(String tableName, List<CanalEntry.RowData> rowDatasList, CanalEntry.EventType eventType) {
        System.out.println("start");
        //判断是否为订单表
        if("order_info".equals(tableName)){
            if (CanalEntry.EventType.INSERT.equals(eventType)){
                for (CanalEntry.RowData rowData:rowDatasList){
                    //JSON对象存放多个列数据
                    JSONObject jsonObject = new JSONObject();
                    for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
                        jsonObject.put(column.getName(),column.getValue());
                    }
                    System.out.println(jsonObject.toString());
                    MyKafkaSender.send("gmall_order_info",jsonObject.toJSONString());
                }
            }
        }
    }
}
