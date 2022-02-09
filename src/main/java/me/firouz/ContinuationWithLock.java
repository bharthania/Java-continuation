package me.firouz;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class ContinuationWithLock<T> implements Iterable<T>{
    private Thread thread;
    private ReentrantLock lock;
    private Condition mainRountinCondition;
    private Condition subRountinCondition;
    private T value;

    public ContinuationWithLock(Runnable runnable) {
        this.thread = new Thread(runnable);
        lock = new ReentrantLock();
        mainRountinCondition = lock.newCondition();
        subRountinCondition = lock.newCondition();
    }

    public void yield(T value) throws InterruptedException {
        try {
            this.value = value;
            lock.lock();
//        subRountineLatch = new CountDownLatch(1);
//        mainRountineLatch.countDown();
            mainRountinCondition.signal();
//        subRountineLatch.await();
            subRountinCondition.await();
        }finally {
            lock.unlock();
        }
    }

    public T goOn() throws InterruptedException {
        try {
            lock.lock();
//        mainRountineLatch = new CountDownLatch(1);
            if (thread.getState() == Thread.State.NEW) {
                thread.start();
            } else subRountinCondition.signal(); //subRountineLatch.countDown();
//        mainRountineLatch.await();
            mainRountinCondition.await();
            return value;
        }finally {
            lock.unlock();
        }
    }


    //=====================================================
    // Iterable implementation
    //=====================================================

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Iterable.super.spliterator();
    }
}


/*
foo() {
...
yeild(3);
...
}

Continuation c(foo);
c.run();

 */
