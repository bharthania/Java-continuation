package me.firouz;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/***********************************************************************************
 *  Note: This implementation is not following all the Iterable and Iterator
 *          specifications. For example, it expects hasNext() to
 *          be called before every next() call
 **********************************************************************************/

public class Generator<T> implements Iterable<T>{
    private final Continuation<T> continuation;
    private T value;

    public Generator(Runnable runnable) {
        continuation = new Continuation<>(runnable);
    }

    public void yield(T val) throws InterruptedException {
        continuation.yield(val);
    }

    @Override
    public Iterator<T> iterator() {

        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                try {
                    value = continuation.goOn();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return !continuation.isFinished();
            }

            @Override
            public T next() {
                return value;
            }
        };
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        while(true) {
            try {
                T val = continuation.goOn();
                if(continuation.isFinished())   break;
                else action.accept(val);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Spliterator<T> spliterator() {
        throw new UnsupportedOperationException();
    }
}
