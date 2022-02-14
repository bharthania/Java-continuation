package me.firouz.trampoline;

import java.util.stream.IntStream;

public class ContinuationExample1 {

    public static void main(String... args) {

        Continuation<Integer> myCont = new Continuation<Integer>() {
            int i = -1;

            @Override
            public Integer start() {
                i++;
                if(i < 100) {
                    thenRun(this::start);
                }
                return i;
            }
        };

        IntStream.range(0, 10000).forEach(
                i -> System.out.println(myCont.goOn())
        );
    }
}

