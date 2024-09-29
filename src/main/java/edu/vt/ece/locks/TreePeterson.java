package edu.vt.ece.locks;
import edu.vt.ece.util.Tree;
public class TreePeterson implements Lock {
    private Peterson[] locks; // Each node of the binary tree is a peterson lock
    private int n;
    private int depth;
    public TreePeterson() {
        this(2);
    }

    public TreePeterson(int n) {

    }

    @Override
    public void lock() {
    }

    @Override
    public void unlock() {

    }
}
