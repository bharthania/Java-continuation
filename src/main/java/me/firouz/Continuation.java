package me.firouz;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class Continuation<T> implements Iterable<T>{
    private final Thread mainRoutineThread;
    private final Thread subRoutineThread;
    private final ReentrantLock lock;
    private final Condition mainRoutineCondition;
    private final Condition subRoutineCondition;
    private T value;
    private boolean finished = false;

    public Continuation(Runnable runnable) {
        this.mainRoutineThread = Thread.currentThread();
        lock = new ReentrantLock();
        mainRoutineCondition = lock.newCondition();
        subRoutineCondition = lock.newCondition();

        this.subRoutineThread = new Thread(() -> {
            runnable.run();
            finished = true;
            try {
                lock.lock();
                mainRoutineCondition.signal();
            }finally {
                lock.unlock();
            }
        });
    }

    public void yield(T value) throws InterruptedException {
        if(Thread.currentThread() != this.subRoutineThread)
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

    public T goOn() throws InterruptedException {
        value = null;
        if(Thread.currentThread() != mainRoutineThread)
            throw new IllegalCallException("goOn() can only be called from within continuation owner thread.");
        if(finished)
            throw new IllegalCallException("goOn() can not be called on terminated continuation.");
        try {
            lock.lock();
            if (subRoutineThread.getState() == Thread.State.NEW) {
                subRoutineThread.start();
            } else subRoutineCondition.signal();
            mainRoutineCondition.await();
            return value;
        }finally {
            lock.unlock();
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public static class IllegalCallException extends RuntimeException {
        public IllegalCallException(String message) {
            super(message);
        }
    }


    //=====================================================
    // Iterable implementation
    //=====================================================

    @Override
    public Iterator<T> iterator() {

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                try {
                    goOn();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return !Continuation.this.finished;
            }

            @Override
            public T next() {
                return Continuation.this.value;
            }
        };
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Iterable.super.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }
}