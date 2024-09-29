package edu.vt.ece.locks;

import edu.vt.ece.bench.Counter;
import edu.vt.ece.bench.TestThread;
import edu.vt.ece.bench.ThreadId;

import java.util.concurrent.atomic.AtomicBoolean;

public class LBakery implements Lock {
    private int l;
    private int n;
    private volatile AtomicBoolean[][] flag;
    private volatile Timestamp[][] label;
    private TimestampSystem[] timestampSystems;
    private ThreadId threadId;
    private Counter counter;
    public LBakery(int l, int n) {
        this.l = l;
        this.n = n;
        this.flag = new AtomicBoolean[l][n];
        this.label = new Timestamp[l][n];
        this.timestampSystems = new LBakeryTimestampSystem[l];
        this.threadId = new TestThread(counter);

        for(int i = 0; i < l; i++) {
            timestampSystems[i] = new LBakeryTimestampSystem(n);
            for(int j = 0; j < n; j++) {
                flag[i][j] = new AtomicBoolean(false);
                label[i][j] = null;
            }
        }
    }

    @Override
    public void lock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId();
        for(int j = 0; j < l; j++) {
            flag[j][i].set(true);
            Timestamp max = findMaxTimeStamp(timestampSystems[j].scan());
            label[j][i] = new LBakeryTimestamp(max.getValue() +1, i);
            timestampSystems[j].label(label[j][i],i);
            for(int k = 0; k < n; k++) {
                while(flag[j][k].get() && k!=i && label[j][k] != null && label[j][k].compare(label[j][i])) {

                }
            }

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
        return maxTimeStamp != null ? maxTimeStamp : new LBakeryTimestamp(0, 0);
    }

    @Override
    public void unlock() {
        int i = ((ThreadId)Thread.currentThread()).getThreadId();
        for(int j = l-1; j >= 0 ;j--){
            flag[j][i].set(false);
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
                if(this.value < b.value || (b.i < this.i && this.value == b.value))
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
