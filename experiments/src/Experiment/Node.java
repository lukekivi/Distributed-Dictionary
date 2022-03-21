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
        // System.out.println("InitFingerTable() " + id);

        fingers[0] = InitFinger(null, 0);
        fingers[0].succ = node.FindSuccessor(fingers[0].start);

        pred = fingers[0].succ.pred;
        
        fingers[0].succ.pred = this;
        pred.fingers[0].succ = this; 

        // node.PrintNode();
        // PrintNode();
        // System.out.println("*******************************");
        
        for (int i = 0; i < fingers.length - 1; i++) {
            Finger nextFinger = InitFinger(null, i + 1);

            if (InRangeInEx(nextFinger.start, id, fingers[i].succ.id)) {
                // System.out.println(nextFinger.start + " IS in [" + id + "," + fingers[i].succ.id + "]");
                nextFinger.succ = fingers[i].succ;

            } else {
                // System.out.println(nextFinger.start + " IS NOT in [" + id + "," + fingers[i].succ.id + "]");
                nextFinger.succ = node.FindSuccessor(nextFinger.start);
            }
            fingers[i + 1] = nextFinger;
        }
        //node.PrintNode();
        //PrintNode();
    }


    // find id's successor
    public Node FindSuccessor(int id) {
        // System.out.println("FindSuccessor(): " + id);
        Node pred = FindPredecessor(id);
        // System.out.println("FindSucccessor() " + id + ": returning " + pred.GetSucc().id);
        return pred.GetSucc();
    }


    // Find id's predecessor
    private Node FindPredecessor(int id) {
        Node node = this;
        // System.out.println("FindPredecessor(): " + id);
        // System.out.println("\t" + node.id + " < " + id + " <= " + node.GetSucc().id);

        int index =0;
        while (!InRangeExIn(id, node.id, node.GetSucc().id)) {  
            index++;
            if (index ==9) {
                System.exit(-1);
            }
            // System.out.println("FindPredecessor(): loop\n\t " + node.id + " < " + id + " <= " + node.GetSucc().id);
            node = node.ClosestPrecedingFinger(id);
        }
        // System.out.println("FindPredecessor(): returning " + node.id);
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
        // PrintNode();
        // System.out.println("*********************");
        // System.out.println("UpdatingOthers()");
        // PrintNode();
        for (int i = 0; i < fingers.length; i++) {
            int nId = CircularSubtraction(this.id, (int) Math.pow(2, i));

            // System.out.println("UpdatingOthers(): find pred of " + nId);
            
            Node pred = FindPredecessor(nId);
            // System.out.println("UpdatingOthers(): pred is " + pred.id);
            pred.UpdateFingerTable(this, i);
        }
    }

    private void UpdateFingerTable(Node node, int i) {
        // System.out.println("UpdateFingerTable() node[" + this.id + "] finger[" + i + "]=" + fingers[i].succ.id);
        // System.out.println("\t" + this.id + " < " + node.id + " < " + fingers[i].succ.id);
        System.out.println("Update called on fingertable[" + i + "] of node " + this.id + " by node " + node.id);
        if (InRangeInEx(node.id, this.id, fingers[i].succ.id)) { // Changed from ExEx to InEx
            System.out.println("Lower: " + this.id + " Upper: " + fingers[i].succ.id);
            if (!(IsCloserInEx(node.id, this.id, fingers[i].start))) {
                System.out.println("NO UPDATE for Node " + this.id + ": Fingers[" + i + "] = " + fingers[i].succ.id + " and new = " + node.id);
                return;
            }
            Node old = fingers[i].succ;
            fingers[i].succ = node;
            Node pred = this.pred;
            System.out.println("Node " + this.id + ": updated Fingers[" + i + "] = " + old.id + " to " + node.id);

            pred.UpdateFingerTable(node, i);
            
        }
        if (i + 1 < fingers.length) {
            this.UpdateFingerTable(node, i + 1);
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
            return true;
        }
        return ((id >= start) && (id < end));
    }

    private boolean IsCloserInEx(int id, int current, int start) {
        int curDist = current - start;
        int newDist = id - start;
        if (current < start) {
            curDist = maxKey + (current - start);
        }
        if (id < start) {
            newDist = maxKey + (id - start);
        }
        return (newDist < curDist);
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
}
