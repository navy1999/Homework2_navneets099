package edu.vt.ece;

import edu.vt.ece.bench.Counter;
import edu.vt.ece.bench.SharedCounter;
import edu.vt.ece.bench.TestThread;
import edu.vt.ece.locks.*;


public class Test {

    private static final int THREAD_COUNT = 16;
    private static final String BAKERY = "Bakery";
    private static final String LOCK_ONE = "LockOne";
    private static final String LOCK_TWO = "LockTwo";
    private static final String PETERSON = "Peterson";
    private static final String FILTER = "Filter";
    private static final String LBAKERY = "LBakery";

    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String lockClass = (args.length==0 ? BAKERY : args[0]);
        final Counter counter = new SharedCounter(0, (Lock)Class.forName("edu.vt.ece.locks." + lockClass).newInstance());
        for(int t=0; t<THREAD_COUNT; t++)
            new TestThread(counter).start();
    }
}


