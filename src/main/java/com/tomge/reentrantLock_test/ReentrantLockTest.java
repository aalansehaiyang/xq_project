package com.tomge.reentrantLock_test;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author Tom哥
 */
public class ReentrantLockTest {


    public static void main(String[] args) {

        m2();
    }


    public static void m1() {
        ReentrantLock reentrantLock = new ReentrantLock();
        reentrantLock.lock();
        try {
            // 模拟业务处理
        } catch (Exception e) {
            // 异常处理
        } finally {
            reentrantLock.unlock();
        }
    }

    public static void m2() {
        ReentrantLock reentrantLock = new ReentrantLock();
        try {
            // 一些业务处理，假设此处抛出了异常
            System.out.println("人为制造异常：" + 1 / 0);
            reentrantLock.tryLock();
            // 加锁业务处理
        } catch (Exception e) {
            // 异常处理
        } finally {
            reentrantLock.unlock();
        }
    }
}
