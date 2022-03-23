package Experiment;

import java.lang.Math;

public class Node {
    private int maxKey; // the max possible key in the DHT node is a member of
    private Node pred = null;
    private int id;
    private Finger[] fingers;

    public Node(int m) {
        maxKey = ((int) Math.pow(2, m)) - 1;

        fingers = new Finger[m];

        for (int i = 0; i < fingers.length; i++) {
            fingers[i] = null;
        }
    }


    /** 
     * node is an arbitrary node in the network used
     * to communicate with the rest of the network
     */
    public void Join(Node node, int id) {
        this.id = id;
        if (node == null) {
            // this is the first node in the system
            for (int i = 0; i < fingers.length; i++) {
                fingers[i] = InitFinger(this, i);
            }
            pred = this;
        } else {
            // this is a new node in an existing system
            InitFingerTable(node);
            UpdateOthers();
        }
    }


    /** 
     * node is an arbitrary node in the network used
     * to communicate with the rest of the network
     */
    private void InitFingerTable(Node node) {

        fingers[0] = InitFinger(null, 0);
        fingers[0].succ = node.FindSuccessor(fingers[0].start);

        pred = fingers[0].succ.pred;
        
        fingers[0].succ.pred = this;
        pred.fingers[0].succ = this; 
        
        for (int i = 0; i < fingers.length - 1; i++) {
            Finger nextFinger = InitFinger(null, i + 1);

            if (InRangeInEx(nextFinger.start, id, fingers[i].succ.id)) {
                nextFinger.succ = fingers[i].succ;

            } else {
                nextFinger.succ = node.FindSuccessor(nextFinger.start);
            }
            fingers[i + 1] = nextFinger;
        }
    }


    // find id's successor
    public Node FindSuccessor(int id) {
        Node pred = FindPredecessor(id);
        return pred.GetSucc();
    }


    // Find id's predecessor
    private Node FindPredecessor(int id) {
        Node node = this;

        while (!InRangeExIn(id, node.id, node.GetSucc().id)) {  
            node = node.ClosestPrecedingFinger(id);
        }
        return node;
    }


    // the first fingertable successor between this node and the id
    public Node ClosestPrecedingFinger(int id) {
        for (int i = fingers.length - 1; i >= 0; i--) {
            Finger finger = fingers[i];
            
            if (InRangeExEx(finger.succ.GetId(), this.id, id)) {
                return fingers[i].succ;
            }
        }
        return this;
    }


    private void UpdateOthers() {
        for (int i = 0; i < fingers.length; i++) {
            int nId = CircularSubtraction(this.id, (int) Math.pow(2, i) - 1);
            Node pred = FindPredecessor(nId);

            pred.UpdateFingerTable(this, i);
        }
    }

    /**
     * Update FingerTables of a given index. Updates happen recursively until it 
     * is determined that they are no longer needed. [this] is the predecessor of
     * [node]. Once [node] is no longer in the range of [this] and its successor, return.
     */
    private void UpdateFingerTable(Node node, int i) {
        if (InRangeInEx(node.id, this.fingers[i].start, this.fingers[i].succ.id)) { 
            
            fingers[i].succ = node;
            Node pred = this.pred;

            pred.UpdateFingerTable(node, i);
        }
    }
    
    private Finger InitFinger(Node node, int i) {
        Finger finger = new Finger(node);
        finger.start = (this.id + ((int) Math.pow(2, i))) % ((int) Math.pow(2, fingers.length));
        int end = finger.start + ((int) Math.pow(2, i));

        if (end > maxKey) {
            finger.end = end - maxKey - 1;
        } else {
            finger.end = end;
        }
    
        return finger;
    }

    /**
     * exclusive start, inclusive end. Handles circular ranges.
     */
    private boolean InRangeExIn(int id, int start, int end) {
        if (end < start) {
            if (id <= end) {
                return true;
            } else if (id > start) {
                return true;
            } else {
                return false;
            }
        } else if (end == start) {
            return true;
        }
        return ((id > start) && (id <= end));
    }


    /**
     * inclusive start, exclusive end. Handles circular ranges.
     */
    private boolean InRangeInEx(int id, int start, int end) {
        if(end < start) {
            if (id < end || id >= start) {
                return true;
            } else {
                return false;
            }
        } else if (end == start) {
            return false;
        }
        return ((id >= start) && (id < end));
    }

    
    /**
     * exclusive start, exclusive end. Handles circular ranges.
     */
    private boolean InRangeExEx(int id, int start, int end) {
        if(end < start) {
            if (id < end || id > start) {
                return true;
            } else {
                return false;
            }
        } else if (end == start) {
            return id != start;
        }
        return ((id > start) && (id < end));
    }

    /**
     * Perform circular subtaction on an id.
     * id of 0 - 1 == maxKey
     */
    private int CircularSubtraction(int id, int val) {
        int result;
        
        result = this.id - val;

        if (result < 0) {
            result = maxKey + result + 1;
        }
        
        return result;
    }


    public int GetId() {
        return this.id;
    }

    public Node GetSucc() {
        return fingers[0].succ;
    }

    public void PrintNode() {
        System.out.println("Node[" + this.id + "]: " );
        if (pred == null) {
            System.out.println("\t- pred: " + "null");    
        } else {
            System.out.println("\t- pred: " + this.pred.GetId());
        }
        System.out.println("\t- table:");
        for(int i = 0; i < fingers.length; i++) {
            System.out.print("\t\t");
            if (fingers[i] == null) {   
                System.out.print("null");
            } else {
                fingers[i].Print();
            }
        }

        System.out.println();
    }

    public int GetPredId() {
        return pred.id;
    }

    public Finger[] GetFingers() {
        return fingers;
    }
}
