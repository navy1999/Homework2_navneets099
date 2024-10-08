package edu.vt.ece.locks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import edu.vt.ece.bench.ThreadId;

public class Peterson implements Lock{

    private volatile AtomicBoolean flag[] = new AtomicBoolean[2];
    private volatile AtomicInteger victim;

    Peterson left;
    Peterson right;
    Peterson parent;
    public Peterson( ) {
        flag[0] = new AtomicBoolean();
        flag[1] = new AtomicBoolean();
        victim = new AtomicInteger();


    }

    @Override
    public void lock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId();
        int j = 1-i;
        flag[i].set(true);
        victim.set(i);
        while(flag[j].get() && victim.get() == i) Thread.yield();
    }

    @Override
    public void unlock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId();
        flag[i].set(false);
    }
}