package com.tomge.gmv;

import com.alibaba.fastjson.JSON;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

import java.util.Iterator;

/**
 * @Author Tom哥
 * @create 2022/9/27
 */
public class GMVProcessWindowFunctionBitMap extends ProcessWindowFunction<OrderMessage, Tuple3<String, String, String>, String, TimeWindow> {
    private transient ValueState<Double> gmvState;
    private transient ValueState<Roaring64NavigableMap> bitMapState;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        gmvState = this.getRuntimeContext().getState(new ValueStateDescriptor<Double>("gmv", Double.class));
        bitMapState = this.getRuntimeContext().getState(new ValueStateDescriptor("bitMap", TypeInformation.of(new TypeHint<Roaring64NavigableMap>() {
        })));
    }

    @Override
    public void process(String s, Context context, Iterable<OrderMessage> elements, Collector<Tuple3<String, String, String>> out) throws Exception {
        Double gmv = gmvState.value();
        Roaring64NavigableMap bitMap = bitMapState.value();
        if (bitMap == null) {
            bitMap = new Roaring64NavigableMap();
            gmv = 0d;
        }

        Iterator<OrderMessage> iterator = elements.iterator();
        while (iterator.hasNext()) {
            OrderMessage orderMessage = iterator.next();
            System.out.println("GMVProcessWindowFunctionBitMap = " + JSON.toJSONString(orderMessage));
            gmv = gmv + orderMessage.getPrice();
            Long userId = orderMessage.getUserId();
            bitMap.add(userId);

            gmvState.update(gmv);
            bitMapState.update(bitMap);
        }
        out.collect(Tuple3.of(s, "userCount", String.valueOf(bitMap.getIntCardinality())));
        out.collect(Tuple3.of(s, "gmv", String.valueOf(gmv)));

    }
}