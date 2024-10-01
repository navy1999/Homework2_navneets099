package edu.vt.ece.locks;
import edu.vt.ece.bench.Counter;
import edu.vt.ece.bench.TestThread;
import edu.vt.ece.bench.ThreadId;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class Bakery implements Lock {
    private final int n;
    private volatile AtomicBoolean[] flag;
    private final Object[] locks;
    private final AtomicReference<Timestamp>[] label;
    private volatile TimestampSystem timestampSystem;
    public Bakery() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public Bakery(int n) {
        this.n = n;
        this.flag = new AtomicBoolean[n];
        this.label = new AtomicReference[n];
        this.timestampSystem = new BakeryTimestampSystem(n);
        this.locks = new Object[n];
        for (int i = 0; i < n; i++) {
            locks[i] = new Object();
        }


        for(int i =0 ; i < n ; i++){
            this.flag[i]= new AtomicBoolean();
            this.label[i] = new AtomicReference<>(null);
        }

    }

    private Timestamp findMaxTimestamp(Timestamp[] timestamps) {
        long max = Long.MIN_VALUE;
        Timestamp maxTimestamp = timestamps[0];
        for (Timestamp timestamp : timestamps) {
            if (timestamp!=null && timestamp.getValue() >max) {
                maxTimestamp = timestamp;
                max = timestamp.getValue();
            }
        }
        return maxTimestamp != null ? maxTimestamp : new BakeryTimestamp(0, 0);

    }

    @Override
    public void lock() {
    int i = ((ThreadId)Thread.currentThread()).getThreadId();
    flag[i].set(true);

    Timestamp max = (BakeryTimestamp) findMaxTimestamp(timestampSystem.scan());
    Timestamp newLabel = new BakeryTimestamp(((BakeryTimestamp)max).getValue() + 1, i);
    label[i].set(newLabel);
    timestampSystem.label(newLabel, i);


    for(int k=0; k < n ; k++){
            while(k!=i && flag[k].get() ){
                if(label[k].get() !=null && (label[k].get().compare(label[i].get()) )) {
                    Thread.yield();
                } else{
                    break;
                }
            }
    }

    }

    @Override
    public void unlock() {
        flag[((ThreadId)Thread.currentThread()).getThreadId()].set(false);
    }
    private static class BakeryTimestamp implements Timestamp{
        private long value;
        private int i;
        public BakeryTimestamp(long value, int i) {
            this.value = value;
            this.i = i;
        }
        public long getValue() {
            return value;
        }
        @Override
        public boolean compare(Timestamp t1) {
           if(t1 instanceof BakeryTimestamp) {
               BakeryTimestamp b = (BakeryTimestamp) t1;
               return this.value < b.value || (b.i < this.i && this.value == b.value);
               }
           return false;
        }
    }
    private static class BakeryTimestampSystem implements TimestampSystem{
        private final AtomicReference<Timestamp>[] timestamps;
        @SuppressWarnings("unchecked")
        BakeryTimestampSystem(int n) {
        timestamps = new AtomicReference[n];
        for(int i = 0 ; i < n ; i++){
            timestamps[i] = new AtomicReference<>(new BakeryTimestamp(0, i));
        }
        }
        @Override
        public Timestamp[] scan() {
            Timestamp[] scanList = new Timestamp[timestamps.length];
            for(int i=0 ;i< timestamps.length ; i++){
                scanList[i] = timestamps[i].get();
            }
            return scanList;
        }

        @Override
        public void label(Timestamp timestamp, int i) {
            timestamps[i].set(timestamp);
        }

    }
}
