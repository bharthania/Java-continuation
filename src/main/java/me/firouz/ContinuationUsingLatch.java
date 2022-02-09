package me.firouz;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class ContinuationUsingLatch<T> implements Iterable<T>{
    private Thread thread;
    private CountDownLatch mainRountineLatch;
    private CountDownLatch subRountineLatch;
    private T value;

    public ContinuationUsingLatch(Runnable runnable) {
        this.thread = new Thread(runnable);
        mainRountineLatch = new CountDownLatch(1);
    }

    public void yield(T value) throws InterruptedException {
        this.value = value;
        subRountineLatch = new CountDownLatch(1);
        mainRountineLatch.countDown();
        subRountineLatch.await();
    }

    public T goOn() throws InterruptedException {
        mainRountineLatch = new CountDownLatch(1);
        if(thread.getState() == Thread.State.NEW) {
            thread.start();
        }else subRountineLatch.countDown();
        mainRountineLatch.await();
        return value;
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
