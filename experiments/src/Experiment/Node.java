package Experiment;

import java.lang.Math;

public class Node {
    private int maxKey; // the max possible key in the DHT node is a member of
    private Node pred;
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
        fingers[0].succ = node.FindSuccessor(id);

        pred = fingers[0].succ.pred;
        fingers[0].succ.pred = this;
        
        for (int i = 0; i < fingers.length - 1; i++) {
            Finger nextFinger = fingers[i + 1];
            if (nextFinger.start >= id && fingers[i].succ.id > nextFinger.start) {
                fingers[i + 1] = InitFinger(fingers[i].succ, i + 1);
            } else {
                fingers[i + 1] = InitFinger(node.FindSuccessor(fingers[i + 1].start), i + 1);
            }
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

        while (!(id > node.id && id <= node.GetSucc().id)) {    
            node = node.ClosestPrecedingFinger(id);
        }
        return node;
    }


    // the first fingertable successor between this node and the id
    public Node ClosestPrecedingFinger(int id) {
        for (int i = fingers.length - 1; i >= 0; i++) {
            Finger finger = fingers[i];
            if (finger.succ.GetId() > this.id && finger.succ.id < id) {
                return fingers[i].succ;
            }
        }
        return this;
    }


    private void UpdateOthers() {
        for (int i = 0; i < fingers.length; i++) {
            Node pred = FindPredecessor(this.id - ((int) Math.pow(2, i)));
            pred.UpdateFingerTable(this, i);
        }
    }

    private void UpdateFingerTable(Node node, int i) {
        if (node.id >= this.id && node.id < fingers[i].succ.id) {
            fingers[i].succ = node;
            Node pred = this.pred;
            pred.UpdateFingerTable(node, i);
        }
    }

    public void PrintNode() {
        System.out.println("Node[" + this.id + "]: " );
        System.out.println("\t-  pred: " + this.pred.GetId());
        System.out.println("\t- table:");
        for(int i = 0; i < fingers.length; i++) {
            System.out.print("\t\t");
            fingers[i].Print();
        }

        System.out.println();
    }
    
    private Finger InitFinger(Node node, int i) {
        Finger finger = new Finger(node);
        finger.start = (this.id + ((int) Math.pow(2, i))) % ((int) Math.pow(2, fingers.length));;
        int end = finger.start + ((int) Math.pow(2, i));

        if (end > maxKey) {
            finger.end = end - maxKey - 1;
        } else {
            finger.end = end;
        }

        return finger;
    }

    public int GetId() {
        return this.id;
    }

    public Node GetSucc() {
        return fingers[0].succ;
    }
}
