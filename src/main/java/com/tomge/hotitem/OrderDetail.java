package com.tomge.hotitem;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail implements Serializable {

    private Long userId; //用户id
    private Long itemId; //商品id
    private Double price;//订单金额
    private Long count;//购买数量
    private Long timeStamp;//下单时间

}
