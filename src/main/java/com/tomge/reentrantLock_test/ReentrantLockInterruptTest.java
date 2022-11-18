package com.tomge.reentrantLock_test;

import lombok.SneakyThrows;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @create 2022/11/18
 */
public class ReentrantLockInterruptTest {

    private ReentrantLock lock = new ReentrantLock();

    public void lockInterrupt() {
        try {
            lock.lockInterruptibly();
            System.out.println(Thread.currentThread().getName() + " 抢到了锁！");

            if (Thread.currentThread().isInterrupted()) {
                System.out.println(Thread.currentThread().getName() + " 锁被中断了！");
            }
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " 抢占锁被中断了！");
        } finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }

    @SneakyThrows
    public static void main(String[] args) {
        ReentrantLockInterruptTest reentrantLockInterruptTest = new ReentrantLockInterruptTest();
        Thread threadA = new Thread(() -> {
            reentrantLockInterruptTest.lockInterrupt();
        }, "threadA");


        Thread threadB = new Thread(() -> {
            reentrantLockInterruptTest.lockInterrupt();
        }, "threadB");

        threadA.start();
        threadB.start();
        Thread.sleep(300);

        threadA.interrupt();
        threadB.interrupt();

        Thread.sleep(10000);


    }


}
