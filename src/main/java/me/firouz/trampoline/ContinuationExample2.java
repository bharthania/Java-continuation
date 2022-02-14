package me.firouz.trampoline;

public class ContinuationExample2 {

    public static void main(String... args) {
        Continuation<String> continuation = new Continuation<String>() {
            @Override
            public String start() {
                thenRun(() -> {
                    thenRun(() -> {
                        return "third part of continuation";
                    });
                    return "Second part of continuation";
                });
                return "First part of continuation";
            }
        };

        System.out.println("About to start calling continuation...");
        String p1 = continuation.goOn();
        System.out.println(p1);
        System.out.println("calling second part of continuation...");
        String p2 = continuation.goOn();
        System.out.println(p2);
        System.out.println("calling third part of continuation...");
        String p3 = continuation.goOn();
        System.out.println(p3);
    }
}
