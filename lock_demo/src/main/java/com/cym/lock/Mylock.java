package com.cym.lock;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class Mylock {

    private final AtomicBoolean locked = new AtomicBoolean(false);

    private Thread owner = null;

    // CH 假头节点，保证对头尾的操作是安全的
    private final AtomicReference<Node> head = new AtomicReference<>(new Node());
    private final AtomicReference<Node> tail = new AtomicReference<>(head.get());


    public void lock() {
        // 使用CAS尝试获取锁
        if (locked.compareAndSet(false, true)) {
            // 记住当前获取到锁的线程
            owner = Thread.currentThread();
            System.out.println(System.currentTimeMillis() + " - "+ owner + "获取到了锁");
            return;
        }
        // 没有获取到锁，使用容器将线程收集起来
        Node current = new Node();
        current.thread = Thread.currentThread();

        while(true) {
            Node currentTail = tail.get();
            if (tail.compareAndSet(currentTail, current)) {
                current.prev = currentTail;
                currentTail.next = current;
                System.out.println(System.currentTimeMillis() + " - "+ current.thread + "加入到等待队列中");
                break;
            }
        }

        while (true) {
            if (head.get().next.equals(current) && locked.compareAndSet(false, true)) {
                head.set(current);
                current.prev.next = null;
                current.prev = null;
                owner = current.thread;
                System.out.println(System.currentTimeMillis() + " - "+ current.thread + "获取到了锁");
                break;
            }
            LockSupport.park();
        }

    }

    public void unlock() {
        if (!owner.equals(Thread.currentThread())) {
            throw new IllegalMonitorStateException("当前线程" + Thread.currentThread() + "没有获取到锁");
        }
        locked.set(false);
        if (!head.get().equals(tail.get())) {
            System.out.println(Thread.currentThread() + "释放锁，唤醒" + head.get().next.thread);
            LockSupport.unpark(head.get().next.thread);
        }
    }

    class Node {
        Node prev;
        Node next;
        Thread thread;
    }


    public static void main(String[] args) throws InterruptedException {
        int[] counts = new int[]{1000};

        List<Thread> threads = new LinkedList<>();

        Mylock lock = new Mylock();
        for (int i = 0; i < 100; i++) {
            Thread thread = new Thread(() -> {
                lock.lock();
                for (int j = 0; j < 10; j++) {
                    try {
                        Thread.sleep(2);
                        counts[0]--;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                lock.unlock();
            });
            threads.add(thread);
        }


        for (int i = 0; i < 100; i++) {
            threads.get(i).start();
        }

        for (int i = 0; i < 100; i++) {
            threads.get(i).join();
        }

        System.out.println("counts[0] = " + counts[0]);
    }

}
