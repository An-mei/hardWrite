package com.cym.threadPool;

@FunctionalInterface
public interface RejectHandle {

    void call(Runnable runnable, MyThreadPool myThreadPool);

}
