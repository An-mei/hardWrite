package com.cym.threadPool;

public class DiscardRejectHandle implements RejectHandle{
    @Override
    public void call(Runnable runnable, MyThreadPool threadPool) {
        Runnable discardCommand = threadPool.discardFirstCommand();
        threadPool.execute(runnable);
    }
}
