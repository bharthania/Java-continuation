package me.firouz;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class ContinuationTest {
    Continuation<Integer> continuation;
    int step = 0;

    @Test
    public void continuationSteps() throws InterruptedException, Continuation.IllegalCallException {
        ArrayList<String> steps = new ArrayList<>();
        int numberOfIterations = 10;

        Runnable runnable = () -> {
            try {
                for(int i=0; i<numberOfIterations; i++) {
                    steps.add("2:" + step++);
                    continuation.yield(i);
                    steps.add("4:" + step++);
                }
                continuation.yield(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        continuation  = new Continuation<>(runnable);

        Integer i = -1;
        do {
            steps.add("1:" + step++);
            i = continuation.goOn();
            steps.add("3:" + step++);
        }while(i != null);


        IntStream.range(1, 4*numberOfIterations).forEach(
                (j) -> {
                    int substep = j%4;
                    if(substep == 0)    assertEquals("4:"+j, steps.get(j));
                    else if(substep == 1)    assertEquals("2:"+j, steps.get(j));
                    else if(substep == 2)    assertEquals("3:"+j, steps.get(j));
                    else if(substep == 3)    assertEquals("1:"+j, steps.get(j));
                }
        );
    }

    @Test
    public void illegalYieldCallTest() {
        Runnable runnable = () -> {};

        continuation  = new Continuation<>(runnable);
        assertThrows(Continuation.IllegalCallException.class, () -> continuation.yield(3));
    }

    @Test
    public void illegalGoOnCallTest() {
        Runnable runnable = () -> {
            assertThrows(Continuation.IllegalCallException.class, () -> continuation.goOn());
        };

        continuation  = new Continuation<>(runnable);
    }

    @Test
    public void goOnWhenContinuationTerminatedTest() throws Continuation.IllegalCallException, InterruptedException {
        Runnable runnable = () -> {
            try {
                continuation.yield(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Continuation.IllegalCallException e) {
                throw new RuntimeException(e);
            }
        };

        continuation  = new Continuation<>(runnable);
        continuation.goOn();
        continuation.goOn();
        assertThrows(Continuation.IllegalCallException.class, () -> continuation.goOn());
    }

    @Test
    public void isFinishedTest() throws Continuation.IllegalCallException, InterruptedException {
        Runnable runnable = () -> {
            try {
                continuation.yield(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Continuation.IllegalCallException e) {
                throw new RuntimeException(e);
            }
        };

        continuation  = new Continuation<>(runnable);
        continuation.goOn();
        assertFalse(continuation.isFinished());
        continuation.goOn();
        assertTrue(continuation.isFinished());
    }

    @Test
    public void forEachTest() {
        Runnable runnable = () -> {
            try {
                for(int i=0; i<10; i++) {
                    continuation.yield(i);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        continuation  = new Continuation<>(runnable);

        int k = 0;
        for(int j: continuation)    assertEquals(k++, j);
    }

}
