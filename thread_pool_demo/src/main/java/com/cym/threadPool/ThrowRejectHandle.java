package com.cym.threadPool;

public class ThrowRejectHandle implements RejectHandle {

    @Override
    public void call(Runnable runnable, MyThreadPool myThreadPool) {
        throw new RuntimeException("阻塞队列已满！");
    }
}
