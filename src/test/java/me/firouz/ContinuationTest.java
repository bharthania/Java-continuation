package me.firouz;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

public class ContinuationTest {
    Continuation<Integer> continuation;
    int step = 0;

    @Test
    public void continuationSteps() throws InterruptedException {
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
            } catch (InterruptedException e) {
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
}
