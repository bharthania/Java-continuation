package me.firouz;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Continuation<T> implements Iterable<T>{
    private final Thread thread;
    private final ReentrantLock lock;
    private final Condition mainRountinCondition;
    private final Condition subRountinCondition;
    private T value;

    public Continuation(Runnable runnable) {
        this.thread = new Thread(runnable);
        lock = new ReentrantLock();
        mainRountinCondition = lock.newCondition();
        subRountinCondition = lock.newCondition();
    }

    public void yield(T value) throws InterruptedException {
        try {
            this.value = value;
            lock.lock();
            mainRountinCondition.signal();
            subRountinCondition.await();
        }finally {
            lock.unlock();
        }
    }

    public T goOn() throws InterruptedException {
        try {
            lock.lock();
            if (thread.getState() == Thread.State.NEW) {
                thread.start();
            } else subRountinCondition.signal();
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