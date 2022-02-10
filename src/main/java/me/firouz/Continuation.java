package me.firouz;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Continuation<T> implements Iterable<T>{
    private final Thread ownerThread;
    private final Thread thread;
    private final ReentrantLock lock;
    private final Condition mainRoutineCondition;
    private final Condition subRoutineCondition;
    private T value;

    public Continuation(Runnable runnable) {
        this.ownerThread = Thread.currentThread();
        this.thread = new Thread(runnable);
        lock = new ReentrantLock();
        mainRoutineCondition = lock.newCondition();
        subRoutineCondition = lock.newCondition();
    }

    public void yield(T value) throws InterruptedException, IllegalCallException {
        if(Thread.currentThread() != this.thread)
            throw new IllegalCallException("yield() can only be called from within continuation Runnable.");
        try {
            this.value = value;
            lock.lock();
            mainRoutineCondition.signal();
            subRoutineCondition.await();
        }finally {
            lock.unlock();
        }
    }

    public T goOn() throws InterruptedException, IllegalCallException {
        if(Thread.currentThread() != ownerThread)
            throw new IllegalCallException("goOn() can only be called from within continuation owner thread.");
        try {
            lock.lock();
            if (thread.getState() == Thread.State.NEW) {
                thread.start();
            } else subRoutineCondition.signal();
            mainRoutineCondition.await();
            return value;
        }finally {
            lock.unlock();
        }
    }

    public static class IllegalCallException extends Exception {
        public IllegalCallException(String message) {
            super(message);
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