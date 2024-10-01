package edu.vt.ece.locks;

import edu.vt.ece.bench.Counter;
import edu.vt.ece.bench.TestThread;
import edu.vt.ece.bench.ThreadId;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class LBakery implements Lock {
    private final int l;
    private final int n;
    private volatile AtomicInteger activeThreads;
    private volatile AtomicBoolean[][] flag;
    private final AtomicReference<Timestamp>[][] label;
    private volatile TimestampSystem[] timestampSystems;
    public LBakery(){
        this(Runtime.getRuntime().availableProcessors(),Runtime.getRuntime().availableProcessors());
    }
    public LBakery(int l, int n) {
        this.l = l;
        this.n = n;
        this.flag = new AtomicBoolean[l][n];
        this.label = new AtomicReference[l][n];
        this.timestampSystems = new LBakeryTimestampSystem[l];
        this.activeThreads = new AtomicInteger(0);


        for(int i = 0; i < l; i++) {
            timestampSystems[i] = new LBakeryTimestampSystem(n);
            for(int j = 0; j < n; j++) {
                flag[i][j] = new AtomicBoolean(false);
                label[i][j] = new AtomicReference<>(null);
            }
        }
    }

    @Override
    public void lock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId();
        for(int j = 0; j < l; j++) {
            flag[j][i].set(true);
            Timestamp max = findMaxTimeStamp(timestampSystems[j].scan());
            Timestamp newLabel = new LBakeryTimestamp(((LBakeryTimestamp)max).getValue() + 1, i);
            label[j][i].set(newLabel);
            timestampSystems[j].label(newLabel, i);
            for (int k = 0; k < n; k++) {
                while ((flag[j][k].get() && k != i && label[j][i].get().compare(label[j][k].get())) || activeThreads.get() >= l) {

                }
            }
            activeThreads.incrementAndGet();
        }
    }

    private Timestamp findMaxTimeStamp(Timestamp[] timestamps) {
        long max = Long.MIN_VALUE;
        Timestamp maxTimeStamp = null;
        for(Timestamp timestamp : timestamps) {
            if(timestamp !=null && timestamp.getValue() > max) {
                maxTimeStamp = timestamp;
                max = timestamp.getValue();
            }
        }
        return maxTimeStamp;
    }

    @Override
    public void unlock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId();

        for(int j = l-1; j >= 0 ;j--){
            flag[j][i].set(false);
            activeThreads.decrementAndGet();
        }
    }

    private static class LBakeryTimestamp implements Timestamp{
        private long value;
        private int i;
        public LBakeryTimestamp(long value, int i) {
            this.value = value;
            this.i = i;
        }
        public long getValue() {
            return value;
        }
        @Override
        public boolean compare(Timestamp t1) {
            if(t1 instanceof LBakeryTimestamp) {
                LBakeryTimestamp b = (LBakeryTimestamp) t1;
                if(this.value < b.value || (this.i < b.i && this.value == b.value))
                {
                    return true;
                }
            }
            return false;
        }
    }
    private static class LBakeryTimestampSystem implements TimestampSystem{
        private Timestamp[] timestamps;
        LBakeryTimestampSystem(int n) {
            timestamps = new Timestamp[n];
            for(int i = 0 ; i < n ; i++){
                timestamps[i] = new LBakeryTimestamp(0,i);
            }
        }
        @Override
        public Timestamp[] scan() {

            return timestamps;
        }

        @Override
        public void label(Timestamp timestamp, int i) {
            timestamps[i] = timestamp;
        }

    }
}
