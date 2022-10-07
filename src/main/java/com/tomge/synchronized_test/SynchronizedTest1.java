package com.tomge.synchronized_test;

import org.openjdk.jol.info.ClassLayout;

/**
 * @Author Tom哥
 * @create 2022/10/3
 */
public class SynchronizedTest1 {


    public static void main(String[] args) throws InterruptedException {

        test1();



    }

    private static void test1() throws InterruptedException {
        Order order = new Order();

        // 偏向锁（Java 默认是开启了偏向锁的）
        System.out.println("----------第一次---------------");
        System.out.println(ClassLayout.parseInstance(order).toPrintable());

        synchronized (order) {
            // 偏向锁
            System.out.println("----------第二次---------------");
            System.out.println(ClassLayout.parseInstance(order).toPrintable());

            System.out.println("计算hash code 值：" + order.hashCode());

            // 计算hash code，偏向锁会升级为重量级锁
            System.out.println("----------第三次---------------");
            System.out.println(ClassLayout.parseInstance(order).toPrintable());
        }


        synchronized (order) {
            // 重量级锁
            System.out.println("----------第四次---------------");
            System.out.println(ClassLayout.parseInstance(order).toPrintable());
        }

        // 重量级锁
        System.out.println("----------第五次---------------");
        System.out.println(ClassLayout.parseInstance(order).toPrintable());
    }

}
