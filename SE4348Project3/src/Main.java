import java.util.ArrayList;
import java.util.List;

public class Main {

class BTreeNode {
    int t;  //The b-tree should have minimal degree 10. This will give 19 key/value pairs, and 20 child pointers.
            //Each node will be stored in a single block with some header information.
    List<Integer> keys;  // List of the keys
    List<BTreeNode> children;  // List of the children
    boolean leaf;  // if node doesn't have children = true

    public BTreeNode(int t, boolean leaf) {
        this.t = t;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.leaf = leaf;
    }
}
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}