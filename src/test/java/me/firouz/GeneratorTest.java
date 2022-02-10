package me.firouz;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneratorTest {
    Generator<Integer> generator;
    int k = 0;

    @Test
    public void forEachLoopTest() {
        k = 0;

        Runnable runnable = () -> {
            try {
                for(int i=0; i<10; i++) {
                    generator.yield(i);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        generator  = new Generator<>(runnable);

        for(int j: generator)    assertEquals(k++, j);
    }

    @Test
    public void forEachMethodTest() {
        k = 0;

        Runnable runnable = () -> {
            try {
                for(int i=0; i<10; i++) {
                    generator.yield(i);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        generator  = new Generator<>(runnable);
        generator.forEach(val -> {
            assertEquals(k++, val);
        });
    }
}
