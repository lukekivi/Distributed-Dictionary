package Experiment;

import java.math.BigInteger;
import java.util.HashMap;
import java.lang.Math;
import java.security.MessageDigest;
import java.util.Queue;

public class Node {
    private int maxKey; // the max possible key in the DHT node is a member of
    private Node pred = null;
    private int id;
    private Finger[] fingers;
    private HashMap<String, String> dict;
    private Cache cache;

    public Node(int m, int cacheSize) {
        maxKey = ((int) Math.pow(2, m)) - 1;

        fingers = new Finger[m];

        for (int i = 0; i < fingers.length; i++) {
            fingers[i] = null;
        }
        cache = new Cache(cacheSize);
        dict = new HashMap<String, String>();
    }


    /** 
     * node is an arbitrary node in the network used
     * to communicate with the rest of the network
     */
    public void Join(Node node, int id) {
        System.out.println("Node " + id + " joined");
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
            
            if (InRangeExEx(finger.succ.id, this.id, id)) {
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

/**
 * Returns the word and its definition
 */
    public String findWord(String word) {
        int wordId = utils.hashFunction(word, maxKey);
        System.out.println("Get request came in for key " + wordId + " at Node " + this.id);
        String ans = "FAILURE";
        CacheEntry entry;
        if (isResponsible(wordId)) {
            ans = dict.get(word);
            if (ans == null) {
                return word + ": NOT IN DHT!";
            } else {
                return word + ": " + ans;
            }
        } else {
            entry = cache.checkCache(word);
            if (entry != null) {
                System.out.println("Grabbed from Cache");
                return entry.getEntry();
            } else {
                Node nextNode = ClosestPrecedingFinger(wordId);
                ans = nextNode.findWord(word);
                return ans;
            }
        }
    }


    private String insertWord(String word, String def, int wordId) {
        System.out.println("Word added to node " + this.id + "'s dictionary");
        String ans = "";
        if (isResponsible(wordId)) {
            ans = dict.put(word, def);
            if (ans == null) {
                return word + " added SUCCESSFULLY";
            } else {
                return "ALREADY IN DHT.";
            }
        } else {
            return "Wrong node";
        }
    }


    /**
     * Returns a string describing the put status
     */
    public String putWord(String word, String def) {
        int wordId = utils.hashFunction(word, maxKey);
        System.out.println("Put request came in for key " + wordId + " at Node " + this.id);
        String ans = "FAILURE";

        ans = findPredCaching(word, def, wordId);
        return ans;
    }


    private String findPredCaching(String word, String def, int wordId) {
        String ans = "FAILURE";
        System.out.println("Adding entry to cache");
        CacheEntry entry = new CacheEntry(word, def);
        cache.addEntry(entry);

        Node nextNode = ClosestPrecedingFinger(wordId);
        if (nextNode.id == this.id) {
            nextNode = GetSucc();
            System.out.println("Moving key " + wordId + " to node " + nextNode.id);
            ans = nextNode.insertWord(word, def, wordId);
            return ans;
        } else {
            System.out.println("Moving key " + wordId + " to node " + nextNode.id);
            ans = nextNode.findPredCaching(word, def, wordId);
            return ans;
        }
    }


    /**
    * @param id the key we are checking if the current node is responsible for 
    * Checks if the current node is the successor for the given id
    */
    public boolean isResponsible(int id) {
        // if (this.FindSuccessor(id) == this) {
        //     return true;
        // } else {
        //     return false;
        // }

        // if (id == this.id) {
        //     return true;
        // }
        if (id == this.id) {
            return true;
        }
        if (InRangeExEx(id, pred.id, this.id)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Find the best finger table entry to send the dict entry
     * @param id key that will be sent to proper node
    */
    public Node nextJump(int id) {
    //     for (int i = fingers.length - 1; i >= 0; i--) {
    //         int prev = i - 1;
    //         if (prev < 0) {
    //             prev = fingers.length - 1;
    //         }
    //         if (fingers[i].succ.id == id) {
    //             return fingers[i].succ;
    //         }
    //         if (!InRangeExIn(id, this.id, finger[i].succ.id)) {
    //             return fingers[i].succ;
    //         }
    //     }
    //     return null;
        for (int i = fingers.length - 1; i >= 0; i--) {
            if (InRangeInEx(id, fingers[i].start, fingers[i].end)) {
                return fingers[i].succ;
            }
        }
        return null;
    }



    public int GetPredId() {
        return pred.id;
    }

    public Finger[] GetFingers() {
        return fingers;
    }
}
