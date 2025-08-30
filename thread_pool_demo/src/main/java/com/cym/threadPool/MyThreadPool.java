package com.cym.threadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class MyThreadPool {

    private final Integer corePoolSize;
    private final Integer maxPoolSize;
    private final Long timeout;
    private final TimeUnit timeUnit;
    private final BlockingQueue<Runnable> blockingQueue;
    private final RejectHandle rejectHandle;

    private final List<Thread> coreThreadList = new ArrayList<>();
    private final List<Thread> supporThreadList = new ArrayList<>();

    public MyThreadPool() {
        this(10,
                20,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1024),
                new ThrowRejectHandle());
    }

    public MyThreadPool(Integer corePoolSize,
                        Integer maxPoolSize,
                        Long timeout,
                        TimeUnit timeUnit,
                        BlockingQueue<Runnable> blockingQueue,
                        RejectHandle rejectHandle) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.blockingQueue = blockingQueue;
        this.rejectHandle = rejectHandle;
    }

    public synchronized void execute(Runnable command) {
        if (coreThreadList.size() < corePoolSize) {
            CoreThread coreThread = new CoreThread(command);
            coreThread.start();
            coreThreadList.add(coreThread);
        } else if (!blockingQueue.offer(command)) {
            if (coreThreadList.size() + supporThreadList.size() < maxPoolSize) {
                SupperThread supperThread = new SupperThread(command);
                supperThread.start();
                supporThreadList.add(supperThread);
            } else if (!blockingQueue.offer(command)) {
                rejectHandle.call(command, this);
            }
        }
    }

    public Runnable discardFirstCommand() {
        return blockingQueue.poll();
    }


    class CoreThread extends Thread {
        private Runnable command;

        CoreThread(Runnable runnable) {
            this.command = runnable;
        }

        @Override
        public void run() {
            if (command != null) {
                command.run();
            }
            while (true) {
                try {
                    command = blockingQueue.take();
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    class SupperThread extends Thread {

        private Runnable command;

        SupperThread(Runnable runnable) {
            this.command = runnable;
        }

        @Override
        public void run() {
            if (command != null) {
                command.run();
            }
            while (true) {
                try {
                    command = blockingQueue.poll(timeout, timeUnit);
                    if (command == null) {
                        break;
                    }
                    command.run();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(Thread.currentThread().getName() + "结束了！");
            supporThreadList.remove(Thread.currentThread());
        }
    }


    public static void main(String[] args) {
        MyThreadPool threadPool = new MyThreadPool(2,
                4,
                1L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2),
                new DiscardRejectHandle());
        for (int i = 0; i < 8; i++) {
            final int finalI = i;
            threadPool.execute(() -> {
                try {
                    Thread.sleep(1000);
                    System.out.println(Thread.currentThread().getName() + "执行 任务-" + finalI);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        System.out.println("主线程还在继续执行...");
    }

}
