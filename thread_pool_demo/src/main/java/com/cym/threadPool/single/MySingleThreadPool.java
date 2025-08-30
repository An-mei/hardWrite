package com.cym.threadPool.single;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class MySingleThreadPool {

    BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(1024);

    Thread singleThread = new Thread(() -> {
        while (true) {
            try {
                Runnable command = blockingQueue.take();
                command.run();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }, "SingleThread");

    {
        singleThread.start();
    }

    public synchronized void executor(Runnable command) {
        boolean offer = blockingQueue.offer(command);
    }


    public static void main(String[] args) {
        MySingleThreadPool mySingleThreadPool = new MySingleThreadPool();
        for (int i = 0; i < 10; i++) {
            final int finalI = i;
            mySingleThreadPool.executor(() -> {
                try {
                    Thread.sleep(200);
                    System.out.println(Thread.currentThread().getName() + "执行任务-" + finalI);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        System.out.println("主线程继续执行");
    }
}
