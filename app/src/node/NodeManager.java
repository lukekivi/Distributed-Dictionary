package node;

import utils.ReadIn;
import java.io.PrintStream;
import utils.ServerInfo;
import java.math.BigInteger;
import java.util.HashMap;
import java.lang.Math;
import java.security.MessageDigest;
import java.util.Queue;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;
import pa2.NodeDetails;
import pa2.Finger;
import pa2.NodeJoinData;
import pa2.Entry;
import pa2.Status;
import pa2.StatusData;
import pa2.JoinStatus;
import pa2.NodeStructure;
import pa2.EntryData;
import utils.*;


/**
 * NodeManager does all of the extra labor and data storage for NodeHandler.
 */
public class NodeManager {
    private final String LOG_FILE = "log/node";
    private int maxKey = -1; // the max possible key in the DHT node is a member of
    private PrintStream fileOut = null;
    private Finger[] fingers;
    private HashMap<String, String> dict;
    private Cache cache;

    public NodeDetails info;
    public NodeDetails pred = null;
    public ConnFactory factory;

    public NodeManager(NodeDetails nodeInfo) {
        info = nodeInfo;
        factory = new ConnFactory();
    }

     
    /** 
     * node is an arbitrary node in the network used
     * to communicate with the rest of the network
     * @param NodeJoinData data of a node joining
     * @param int size of the cache to be created
     */
    public void Join(NodeJoinData joinData, int cacheSize) {
        System.out.println("Joining");
        maxKey = ((int) Math.pow(2, joinData.m)) - 1;

        fingers = new Finger[joinData.m];

        for (int i = 0; i < fingers.length; i++) {
            fingers[i] = null;
        }
        cache = new Cache(cacheSize);
        dict = new HashMap<String, String>();
        info.id = joinData.id;


        if (joinData.status == JoinStatus.ORIGINAL) { // First node in system
            initNewNode();
        } else {
            // this is a new node in an existing system
            InitFingerTable(joinData.nodeInfo);
            updateOthers();
        }

        NodeStructure nodeStructure = new NodeStructure();
        nodeStructure.id = info.id;
        nodeStructure.predId = pred.id;
        nodeStructure.fingers = getNodeFingers();
        nodeStructure.entries = getNodeEntries();
        
        System.out.println("Joined the DHT:");
        Print.nodeStructure(nodeStructure);
    }


    /**
     * This is the first node in the system. 
     */
    public void initNewNode() {
        for (int i = 0; i < fingers.length; i++) {
            fingers[i] = InitFinger(info, i);
        }
        pred = info;
    }


    /** 
     * node is an existing node in the system, used by this node to enter the DHT system
     * @param NodeDetails existing node
     */
    private void InitFingerTable(NodeDetails node) {
        final String FUNC_ID = "NodeManager.InitFingerTable()";

        fingers[0] = InitFinger(null, 0);
        fingers[0].succ = NodeComm.findSuccessor(FUNC_ID, node, fingers[0].start);
    
        pred = NodeComm.getPred(FUNC_ID, fingers[0].succ);  // set pred to be the succ.pred
        NodeComm.setPred(FUNC_ID, fingers[0].succ, info);   // set succ.pred to be this
        NodeComm.setSucc(FUNC_ID, pred, info);              // set pred.succ to be this

        for (int i = 0; i < fingers.length - 1; i++) {
            Finger nextFinger = InitFinger(null, i + 1);

            if (Range.InRangeInEx(nextFinger.start, info.id, fingers[i].succ.id)) {
                // the previous finger's successor is a valid successor for this finger too
                nextFinger.succ = fingers[i].succ;
            } else {
                // search the DHT for the best successor for nextFinger
                nextFinger.succ = NodeComm.findSuccessor(FUNC_ID, node, nextFinger.start);
            }

            fingers[i + 1] = nextFinger;
        }
    }


    /**
     * Update other nodes in the DHT that may require this new node in their fingerTables
     */
    public void updateOthers() {
        final String FUNC_ID = "NodeManager.updateOthers()";

        for (int i = 0; i < fingers.length; i++) {
            int nId = Range.CircularSubtraction(info.id, (int) Math.pow(2, i) - 1, maxKey);
            NodeDetails pred = FindPredecessor(nId);

            if (pred.id != info.id) {
                NodeComm.updateFingerTable(FUNC_ID, pred, info, i);
            }
        }
    }

    
    /** 
     * Find the predecessor of the index thats passed in
     * @param int id value that we want to find the predecessor of
     */
    public NodeDetails FindPredecessor(int id) {
        final String FUNC_ID = "NodeManager.FindPredecessor()";

        NodeDetails nodeInfo = info;
        int nodeSuccId = fingers[0].succ.id;

        while (!(Range.InRangeExIn(id, nodeInfo.id, nodeSuccId))) {

            if (nodeInfo.id == info.id) {
                nodeInfo = ClosestPrecedingFinger(id);
            } else {
                nodeInfo = NodeComm.closestPrecedingFinger(FUNC_ID, nodeInfo, id);
            }
            nodeSuccId = NodeComm.getSucc(FUNC_ID, nodeInfo).id;
        }

        return nodeInfo;
    }


    /** 
     * The first fingertable successor between this node and the id
     * @param int id to use as an upperbound for checking if  a node is in range
     * @return Finger table entry successor that precedes id (closest)
     */
    public NodeDetails ClosestPrecedingFinger(int id) {
        for (int i = fingers.length - 1; i >= 0; i--) {
            Finger finger = fingers[i];
            
            if (Range.InRangeExEx(finger.succ.id, info.id, id)) {
                return fingers[i].succ;
            }
        }
        return info;
    }

    

    /** 
     * Searches the either Cache or dictionary for the word
     * @param String the word that we want to look for
     * @return EntryData that encapsulates a word, its def, and status
     */
    public EntryData findWord(String word) {
        EntryData data = new EntryData();
        Entry entry = new Entry();
        String def = "";
        int wordId = getHash(word);
        System.out.println("Node " + info.id + ": Get request came in for word " + word + "(key " + wordId + ")");
        
        if (isResponsible(wordId)) {
            def = dict.get(word);
            if (def == null) {
                System.out.println("Node " + info.id + ": " + word + " not in proper dict, done");
                data.entry = null;
                data.status = Status.SUCCESS;
            } else {
                System.out.println("Node " + info.id + ": " + word + " grabbed from dict");
                entry.word = word;
                entry.definition = def;
                data.entry = entry;
                data.status = Status.SUCCESS;
            }
        } else {
            entry = cache.checkCache(word);
            if (entry != null) {
                System.out.println("Node " + info.id + ": " + word + " grabbed from cache");
                data.entry = entry;
                data.status = Status.SUCCESS;
            } else {
                data.entry = null;
                data.status = Status.ERROR;
            }
        }
        return data;
    }


    /** 
     * Puts the word and def into its cache if it's not the proper node, dictionary if it is
     * @param String word 
     * @param String definition
     * @return Status which says if it was put in a dictionary or not. Used to decide whether or not to forward the request
     */
    public Status putWord(String word, String def) {
        int wordId = getHash(word);
        System.out.println("Node " + info.id + ": Put request came in for word " + word + "(key " + wordId + ")");
        Status ans = Status.ERROR;

        if (isResponsible(wordId)) { // Node's successor is itself meaninng only one node, therefore don't cache
            Status insertData = insertWord(word, def, wordId);
            ans = insertData;
        } else {
            ans = findPredCaching(word, def, wordId);
        }
        return ans;
    }


    /** 
     * Puts the word into its cache
     * @param String word 
     * @param String definition
     * @param int wordId which is the key value of the word
     * @return Status which says if it was put in the cache or not. Used to decide whether or not to forward the request
     */
    public Status findPredCaching(String word, String def, int wordId) {
        Status ans = Status.ERROR;
        System.out.println("Node " + info.id + ": adding " + word + "(key " + wordId + ") to cache");
        Entry entry = new Entry(word, def);
        cache.addEntry(entry);
        return ans;
    }

    /** 
     * Puts the word into its dictionary
     * @param String word 
     * @param String definition
     * @param int wordId which is the key value of the word
     * @return Status which says if it was put in a dictionary or not. Used to decide whether or not to forward the request
     */
    public Status insertWord(String word, String def, int wordId) {
        dict.put(word, def);
        System.out.println("Node " + info.id + ": adding " + word + "(key " + wordId + ") to the dictionary");
        return Status.SUCCESS;
    }

    /** 
     * Initializes a finger entry, creates a start, end, and succ value.
     * @param NodeDetails node which is the successor/entry
     * @param int i which is the index of the entry
     * @return the initialized finger entry
     */
    public Finger InitFinger(NodeDetails node, int i) {
        Finger finger = new Finger();
        finger.succ = node;
        finger.start = (info.id + ((int) Math.pow(2, i))) % ((int) Math.pow(2, fingers.length));
        int end = finger.start + ((int) Math.pow(2, i));

        if (end > maxKey) {
            finger.last = end - maxKey - 1;
        } else {
            finger.last = end;
        }
    
        return finger;
    }


    /**
    * Checks if the node is responsible for the key id
    * @param id the key we are checking if the current node is responsible for 
    */
    public boolean isResponsible(int id) {
        if (id == info.id) {
            return true;
        }
        if (Range.InRangeExEx(id, pred.id, info.id)) {
            return true;
        } else {
            return false;
        }
    }


    public int GetId() {
        return info.id;
    }


    /** 
     * Creates a list of all the dictionary entries
     */
    public ArrayList<Entry> getNodeEntries() {
        ArrayList<Entry> ans = new ArrayList<Entry>();
        for (java.util.Map.Entry mapElement : dict.entrySet()) {
            String word = (String)mapElement.getKey();
            String def = (String)mapElement.getValue();
            Entry entry = new Entry();
            entry.word = word;
            entry.definition = def;
            ans.add(entry);
        }
        return ans;
    }

    /** 
     * Creates a list of all the Finger table entries
     */
    public ArrayList<Finger> getNodeFingers() {
        ArrayList<Finger> list = new ArrayList<Finger>();
        for (int i = 0; i < fingers.length; i++) {
            list.add(fingers[i]);
        }
        return list;
    }
    

    public String getIpAddressOfSelf() {
        return info.ip;
    }


    /**
     * read in portnumber from the config file
     */
    public int getPortOfSelf() {
        return info.port;
    }

    /** 
     * Gets the hash value (key) of the word
     * @param String word to be hashed
     */
    public int getHash(String word) {
        if (maxKey == -1) {
            System.out.println("ERROR: Node + " + info.id + " getHash() - attempted to hash prior to having a maxKey set");
            System.exit(1);
        }
        return Hash.makeKey(word, maxKey);
    }


    public void setLog(int id)  {
        try {
            fileOut = new PrintStream(LOG_FILE + id + ".txt");
            System.setOut(fileOut);  
        } catch (FileNotFoundException x) {
            System.out.println("Error: Node " + info.id + " not able to establish a log file.");
            System.exit(1);
        }
    }


    public void closeLog() {
        if (fileOut != null) {
            fileOut.close();
        }
    }

    /** 
     * Returns the node's successor
     */
    public NodeDetails getSucc() {
        return fingers[0].succ;
    }

    /** 
     * Sets the node's successor
     */
    public void setSucc(NodeDetails nodeInfo) {
        fingers[0].succ = nodeInfo;
    }

    /** 
     * Gets the finger entry as index i
     */
    public Finger getFinger(int i) {
        return fingers[i];
    }

    /** 
     * Sets the finger entry at index i
     */
    public void setFingerSucc(int i, NodeDetails nodeInfo) {
        fingers[i].succ = nodeInfo;
    }
}
