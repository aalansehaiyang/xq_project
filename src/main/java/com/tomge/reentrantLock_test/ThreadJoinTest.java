package com.tomge.reentrantLock_test;

/**
 * @Author Tom哥
 */
public class ThreadJoinTest {

    public static void main(String[] args) throws InterruptedException {
        //获取主线程
        Thread mainThread = Thread.currentThread();
        Thread subThreadA = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6_000);
                    System.out.println("主线程状态: " + mainThread.getState());
                    System.out.println("子线程状态: " + Thread.currentThread().getState());
                    System.out.println("子线程运行结束!!!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, "子线程");

        subThreadA.start();
        subThreadA.join();

        System.out.println("主线程运行结束!!!");
    }
}
