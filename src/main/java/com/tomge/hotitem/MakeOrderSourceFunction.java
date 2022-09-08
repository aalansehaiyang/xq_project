package com.tomge.hotitem;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.List;
import java.util.Random;

/**
 * @Author Tom哥
 * @create 2022/9/8
 */
public class MakeOrderSourceFunction implements SourceFunction<String> {

    private boolean isRunning = true;

    List<Long> itemId = Lists.newArrayList(100L);

    /**
     * 无限循环，模拟生产订单数据
     */
    @Override
    public void run(SourceContext<String> ctx) throws Exception {

        while (isRunning) {

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setItemId(itemId.get(new Random().nextInt(itemId.size())));
            orderDetail.setCount(1L);
            orderDetail.setUserId(1L);
            orderDetail.setTimeStamp(System.currentTimeMillis());
            ctx.collect(JSON.toJSONString(orderDetail));

            //每 1秒 产生一条数据
            Thread.sleep(1000);
        }
    }

    //取消一个cancel的时候会调用的方法
    @Override
    public void cancel() {
        isRunning = false;
    }
}
