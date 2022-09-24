package com.tomge.list_grow_test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author Tom哥
 * @create 2022/9/23
 */
public class ListOptimize {

    public static void main(String[] args) {

        List<String> lists = new ArrayList<>(1000000);

        Long beginTime = System.currentTimeMillis();
        for (int i = 1; i <= 1000000; i++) {
            lists.add("tom哥-" + i);
        }
        Long end = System.currentTimeMillis();

        System.out.println("1000000 次插入 List，花费时间：" + (end - beginTime));
    }
}
