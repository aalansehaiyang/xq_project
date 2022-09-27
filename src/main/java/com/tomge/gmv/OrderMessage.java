package com.tomge.gmv;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author Tom哥
 * @create 2022/9/27
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderMessage {

    private Long userId; //用户id
    private Long itemId; //商品id
    private Double price;//订单金额
    private Integer count;//购买数量
    private Long time;
}
