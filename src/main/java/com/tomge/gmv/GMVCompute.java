package com.tomge.gmv;

import com.alibaba.fastjson.JSON;
import com.tomge.hotitem.OrderDetail;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.evictors.TimeEvictor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @Author Tom哥
 * @create 2022/9/27
 */
public class GMVCompute {

    // 下单消息 topic
    public static String createOrderTopic = "create_order_topic_1";

    public static String timeStampToDate(Long timestamp) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(new Date(timestamp));
        return format.substring(0, 10);
    }


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //采用 Event-Time 来作为 时间特征
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        //设置 Checkpoint 时间周期为 60 秒
        env.enableCheckpointing(60 * 1000, CheckpointingMode.EXACTLY_ONCE);
        env.getCheckpointConfig().setCheckpointTimeout(30 * 1000);
        env.setParallelism(1);

        // 配置 kafka 参数
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "127.0.0.1:9092");

        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(createOrderTopic, new SimpleStringSchema(), properties);
        // 从最早开始消费
        consumer.setStartFromLatest();

        DataStream<String> stream = env.addSource(consumer);

        // 数据反序列化
        DataStream<OrderMessage> orderStream = stream.map(message -> {
            //System.out.println(message);
            return JSON.parseObject(message, OrderMessage.class);
        });


        SingleOutputStreamOperator<Tuple3<String, String, String>> single = orderStream.keyBy(new KeySelector<OrderMessage, String>() {
            @Override
            public String getKey(OrderMessage value) throws Exception {
                return timeStampToDate(value.getTime());
            }
        })
                .window(TumblingProcessingTimeWindows.of(Time.days(1), Time.hours(-8)))
                .trigger(ContinuousProcessingTimeTrigger.of(Time.seconds(4)))
                .evictor(TimeEvictor.of(Time.seconds(0), true))
                .process(new GMVProcessWindowFunctionBitMap());


        // Redis 配置
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("127.0.0.1").setPort(6379).build();
        single.addSink(new RedisSink<>(conf, new RedisMapper<Tuple3<String, String, String>>() {

            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.SET);
            }

            /**
             * 设置Key
             */
            @Override
            public String getKeyFromData(Tuple3<String, String, String> data) {

                System.out.println("统计结果 Tuple3= " + JSON.toJSONString(data));
                if (StringUtils.equalsIgnoreCase(data.f1, "userCount")) {
                    return "userCount = ";
                } else {
                    return "GMV = ";
                }
            }

            /**
             * 设置value
             */
            @Override
            public String getValueFromData(Tuple3<String, String, String> data) {
                return data.f2;
            }
        }));


        env.execute("execute Hot_Item");


    }

}

