package com.tomge.hotitem;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;


import java.util.Properties;

/**
 * 业务系统，模拟创建下单消息
 */
public class KafkaProducer {


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.enableCheckpointing(5000);

        DataStreamSource<String> orderStream = env.addSource(new MakeOrderSourceFunction()).setParallelism(1);

        // Kafka 配置参数
        FlinkKafkaProducer<String> producer = new FlinkKafkaProducer<String>(
                "127.0.0.1:9092",   //broker列表
                "create_order_topic", //topic
                new SimpleStringSchema());    // 消息序列化

        //写入Kafka时附加记录的事件时间戳
        producer.setWriteTimestampToKafka(true);

        orderStream.addSink(producer);
        env.execute();
    }

}

