## Simulating Continuation in Java Via Threads

Following is an example of how to use the Continuation.java. Its constructor receives a Runnable as the body of the routine that calls yield(s).  
The very first goOn() call would start running the Runnable until it yields upon which the control is transferred back to the main routine where goOn() was invoked. This ping pong transfer of control between main and sub routine continues until Runnable finishes through the very last yield() call.
```java
Continuation<Integer> cont;
...
cont = new Continuation<>(
    () -> {
        //Sub Routine
        System.out.println("2:subRoutine");
        cont.yield(1);   //return 1
        System.out.println("4:subRoutine");
        cont.yield(2);   //return 2
        System.out.println("6:subRoutine");
        cont.yield(3);   //return 3
    }
);

//Main Routine
int result;
System.out.println("1:mainRoutine");
result = cont.goOn();   //result = 1
System.out.println("3:mainRoutine");
result = cont.goOn();   //result = 2
System.out.println("5:mainRoutine");
result = cont.goOn();   //result = 3
System.out.println("7:mainRoutine");

/*
    Output:
        1:mainRoutine
        2:subRoutine    
        3:mainRoutine
        4:subRoutine
        5:mainRoutine
        6:subRoutine
        7:mainRoutine
*/
```

Generator.java utilizes Continuation.java to implement Iterable interface and act as a ***lazy generator***. Its yield() method emits values as the ***Iterator*** asks for next value.  
Note: The current Iterable and Iterator implementation does not comply with the Iterable and Iterator specifications completely. For example it requires hasNext() method to be called before every next() call.

Bellow is an example of a generator that can emit integers starting from 0 up to ```Long.MAX_VALUE``` in a lazy way.
```java
Generator<Long> intGenerator;
...
intGenerator = new Generator<>(
    () -> {
        for(long i=0; i<Long.MAX_VALUE; i++) {
            intGenerator.yield(i);
        }    
    }        
);

for(int j: intGenerator)
    if(j <= 100) System.out.println(j);
    else break;
```

Following is the stripped down version of the Continuation.java to provide an idea on how the yield mechanism is simulated. Note that goOn() and yield() methods are being called from two different threads. using lock and conditions the class simulates the back and forth transfer of control between two different routines.
```java
public void yield(T value) throws InterruptedException {
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
```
