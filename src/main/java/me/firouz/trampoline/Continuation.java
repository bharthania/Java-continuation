package me.firouz.trampoline;

import java.util.function.Supplier;

/*
        Continuation using trampoline pattern
 */

public abstract class Continuation<T> {
    private Supplier<T> next;
    private boolean firstCall = true;

    public abstract T start();

    public void thenRun(Supplier<T> next) {
        this.next = next;
    }

    public final T goOn() {
        if(firstCall) {
            firstCall = false;
            return start();
        }else if(next != null) {
            Supplier<T> nextToCall = next;
            next = null;
            return nextToCall.get();
        }else throw new IllegalStateException("goOn() called on an exhausted Co-routine.");
    }
}
