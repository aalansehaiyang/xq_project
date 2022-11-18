package com.tomge.reentrantLock_test;

import lombok.SneakyThrows;

import java.util.concurrent.locks.LockSupport;

/**
 * @Author Tom哥
 */
public class LockSupportTest {


    @SneakyThrows
    public static void main(String[] args) {
        //m1();
        m2();
    }

    @SneakyThrows
    private static void m1() {
        Thread threadA = new Thread(() -> {
            LockSupport.park();
        });
        threadA.start();

        Thread.sleep(1000L);
        System.out.println("线程 A 的状态：" + threadA.getState());
    }

    @SneakyThrows
    private static void m2() {
        Thread threadA = new Thread(() -> {
            LockSupport.park();
            System.out.println("线程 A 继续执行......");

            int sum = 0;
            for (int i = 1; i < 100000; i++) {
                sum += i;
            }
        });
        threadA.start();

        Thread.sleep(1000L);
        LockSupport.unpark(threadA);
        System.out.println("线程 A 的状态：" + threadA.getState());

    }
}

