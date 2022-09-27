package com.tomge.gmv;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tomge.hotitem.OrderDetail;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.List;
import java.util.Random;

/**
 * @Author Tom哥
 * @create 2022/9/27
 */
public class CreateOrderSourceFunction implements SourceFunction<String> {

    static List<OrderMessage> orderMessageList = Lists.newArrayList();

    /**
     * 初始化数据
     */
    static {
        orderMessageList.add(new OrderMessage(1L, 170170001L, 100d, 1, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(1L, 170170002L, 200d, 2, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(1L, 170170003L, 300d, 3, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(1L, 170170004L, 400d, 1, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(1L, 170170005L, 500d, 5, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(1L, 170170006L, 600d, 1, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(1L, 170170007L, 700d, 3, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(1L, 170170008L, 100d, 9, System.currentTimeMillis()));

        orderMessageList.add(new OrderMessage(2L, 170170009L, 100d, 5, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(3L, 170170010L, 100d, 6, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(2L, 170170011L, 100d, 9, System.currentTimeMillis()));
        orderMessageList.add(new OrderMessage(2L, 170170012L, 100d, 1, System.currentTimeMillis()));

    }


    /**
     * 模拟生产订单数据
     */
    @Override
    public void run(SourceContext<String> ctx) throws Exception {

        for (int i = 0; i < orderMessageList.size(); i++) {
            ctx.collect(JSON.toJSONString(orderMessageList.get(i)));
            //每 4 秒 产生一条数据
            Thread.sleep(5000);
        }
    }

    @Override
    public void cancel() {
    }

}

