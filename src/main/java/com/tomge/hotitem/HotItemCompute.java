package com.tomge.hotitem;

import org.apache.flink.configuration.Configuration;


import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
import org.apache.flink.table.sources.wmstrategies.WatermarkStrategy;
import org.apache.flink.util.Collector;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.*;


/**
 * 热点商品计算
 */
public class HotItemCompute {

    // 下单消息 topic
    public static String createOrderTopic = "create_order_topic";

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
        DataStream<OrderDetail> orderStream = stream.map(message -> {
            //System.out.println(message);
            return JSON.parseObject(message, OrderDetail.class);
        });


        //DataStream<OrderDetail> dataStream = orderStream.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<OrderDetail>() {
        //
        //    private Long currentTimeStamp = 0L;
        //    // 订单消息流可能存在乱序，这里设置允许乱序时间为 30 秒，并且设置周期性水印
        //    private Long maxOutOfOrderness = 30_000L;
        //
        //    @Override
        //    public Watermark getCurrentWatermark() {
        //
        //        return new Watermark(currentTimeStamp - maxOutOfOrderness);
        //    }
        //
        //    @Override
        //    public long extractTimestamp(OrderDetail element, long previousElementTimestamp) {
        //        return element.getTimeStamp();
        //    }
        //});

        // 按 商品id 进行聚合
        KeyedStream<OrderDetail, Long> orderDetailStringKeyedStream = orderStream.keyBy(new KeySelector<OrderDetail, Long>() {
            @Override
            public Long getKey(OrderDetail deviceInfo) {
                return deviceInfo.getItemId();
            }
        });

        // 滚动处理窗口 5 秒一个窗口
        WindowedStream<OrderDetail, Long, TimeWindow> window = orderDetailStringKeyedStream.window(SlidingProcessingTimeWindows.of(Time.seconds(3600), Time.seconds(5)));


        SingleOutputStreamOperator<OrderDetail> reduce = window.reduce(new ReduceFunction<OrderDetail>() {
            @Override
            public OrderDetail reduce(OrderDetail value1, OrderDetail value2) throws Exception {

                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setItemId(value1.getItemId());
                orderDetail.setCount(value1.getCount() + value2.getCount());
                System.out.println("聚合结果 ===" + JSON.toJSONString(orderDetail));
                return orderDetail;
            }
        });

        //reduce.addSink(new RichSinkFunction<OrderDetail>() {
        //
        //                   @Override
        //                   public void invoke(OrderDetail orderDetail, Context context) throws Exception {
        //                       System.out.println("结果输出：item_id=" + orderDetail.getItemId() + " , 数量=" + orderDetail.getCount());
        //                   }
        //               }
        //);


        //////每 10 秒计算一次
        //DataStream<Tuple2<Long, Long>> process = reduce.timeWindowAll(Time.seconds(5))
        //        .process(new ProcessAllWindowFunction<OrderDetail, Tuple2<Long, Long>, TimeWindow>() {
        //                     @Override
        //                     public void process(Context context, Iterable<OrderDetail> elements, Collector<Tuple2<Long, Long>> out) throws Exception {
        //
        //                         System.out.println("每 10 秒计算一次");
        //                         Iterator<OrderDetail> iterator = elements.iterator();
        //                         if (iterator.hasNext()) {
        //                             OrderDetail orderDetail = iterator.next();
        //                             System.out.println("结果输出：item_id=" + orderDetail.getItemId() + " , 数量=" + orderDetail.getCount());
        //                             out.collect(Tuple2.of(orderDetail.getItemId(), orderDetail.getCount()));
        //                         }
        //
        //                     }
        //                 }
        //        );


        // Redis 配置
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("127.0.0.1").setPort(6379).build();
        reduce.addSink(new RedisSink<>(conf, new RedisMapper<OrderDetail>() {

            @Override
            public RedisCommandDescription getCommandDescription() {
                return new RedisCommandDescription(RedisCommand.SET);
            }

            /**
             * 设置Key
             */
            @Override
            public String getKeyFromData(OrderDetail data) {
                return "sku_id=" + String.valueOf(data.getItemId());
            }

            /**
             * 设置value
             */
            @Override
            public String getValueFromData(OrderDetail data) {
                return String.valueOf(data.getCount());
            }
        }));


        env.execute("execute Hot_Item");


    }
}
