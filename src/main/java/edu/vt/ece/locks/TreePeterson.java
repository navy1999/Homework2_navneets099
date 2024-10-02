package edu.vt.ece.locks;

import edu.vt.ece.bench.ThreadId;

public class TreePeterson implements Lock {

    private volatile Peterson[] locks;
    private int n;
    private volatile int[] path;

    public TreePeterson() {
        this(Runtime.getRuntime().availableProcessors());
    }
    public TreePeterson(int n) {
        this.n = n;
        this.locks = new Peterson[n - 1];
        this.path = new int[(int)(Math.log(n) / Math.log(2)) + 1];


        for (int i = 0; i < locks.length; i++) {
            locks[i] = new Peterson();
        }
    }

    @Override
    public void lock() {
        int i = ((ThreadId) Thread.currentThread()).getThreadId();
        int node = i + n - 1;
        int level = 0;


        while (node > 0) {
            int parent = (node - 1) / 2;
            locks[parent].lock();
            path[level++] = parent;
            node = parent;
        }
        System.out.println("Thread :" + Thread.currentThread().getId() + " locked " + locks.length + " lock nodes" );
    }

    @Override
    public void unlock() {
        int level = path.length - 1;


        while (level >= 0) {
            int node = path[level--];
            locks[node].unlock();
        }
    }
}