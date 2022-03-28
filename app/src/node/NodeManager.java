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

import pa2.NodeDetails;
import pa2.Finger;
import pa2.NodeJoinData;
import pa2.Entry;
import pa2.Status;
import pa2.StatusData;
import utils.*;


public class NodeManager {
    private final String LOG_FILE = "log/node";
    private int maxKey = -1; // the max possible key in the DHT node is a member of
    private final Hash hash = new Hash();
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
     */
    public void Join(NodeJoinData joinData, int cacheSize) {
        maxKey = ((int) Math.pow(2, joinData.m)) - 1;

        fingers = new Finger[joinData.m];

        for (int i = 0; i < fingers.length; i++) {
            fingers[i] = null;
        }
        cache = new Cache(cacheSize);
        dict = new HashMap<String, String>();
        info.id = joinData.id;


        if (joinData.nodeInfo == null) {
            initNewNode();
        } else {
            // this is a new node in an existing system
            InitFingerTable(joinData.nodeInfo);
            updateOthers();
        }
        System.out.println("Node " + info.id + " joined");
    }


    /**
    * Returns the word and its definition
    */
    public Entry findWord(String word) {
        int wordId = getHash(word);
        System.out.println("Get request came in for key " + wordId + " at Node " + info.id);
        String def = "";
        Entry entry = new Entry();
        if (isResponsible(wordId)) {
            def = dict.get(word);
            if (def == null) {
                return null;
            } else {
                entry.word = word;
                entry.definition = def;
                return entry;
            }
        } else {
            entry = cache.checkCache(word);
            if (entry != null) {
                System.out.println("Grabbed from Cache");
                return entry;
            } else {
                Entry ans = new Entry();
                NodeDetails nextNode = ClosestPrecedingFinger(wordId);
                try {
                    NodeConn nodeCon = factory.makeNodeConn(nextNode); // connect to nextNode
                    StatusData data = nodeCon.Client.FindWordHelper(word);
                    ans = data.status;
                    factory.closeNodeConn(nodeCon);
                } catch (TTransportException x) {
                    System.out.println("Something went wrong with Node connection.");
                    System.exit(1);
                }
                return ans;
            }
        }
    }


    /**
     * Returns a string describing the put status
     */
    public Status putWord(String word, String def) {
        int wordId = getHash(word);
        // System.out.println("Put request came in for key " + wordId + " at Node " + this.id);
        Status ans = Status.ERROR;

        ans = findPredCaching(word, def, wordId);
        return ans;
    }


    public Status findPredCaching(String word, String def, int wordId) {
        Status ans = Status.ERROR;
        // System.out.println("Adding entry to cache");
        Entry entry = new Entry(word, def);
        cache.addEntry(entry);

        NodeDetails nextNode = ClosestPrecedingFinger(wordId);

        if (nextNode.id == info.id) {
            nextNode = getSucc(); // Update nextNode to the successor

            // System.out.println("Moving key " + wordId + " to node " + nextNode.id);
            try {
                NodeConn nodeCon = factory.makeNodeConn(nextNode); // connect to nextNode
                StatusData insertData = nodeCon.Client.InsertWordHelper(word, def, wordId);
                ans = insertData.status;
                factory.closeNodeConn(nodeCon);
            } catch (TTransportException x) {
                System.out.println("Something went wrong with Node connection.");
                System.exit(1);
            }
            return ans;

        } else {
            try {
            // System.out.println("Moving key " + wordId + " to node " + nextNode.id);
                NodeConn nodeCon = factory.makeNodeConn(nextNode); // connect to nextNode
                StatusData cacheData = nodeCon.Client.FindPredCachingHelper(word, def, wordId);
                ans = cacheData.status;
                factory.closeNodeConn(nodeCon);
            } catch (TTransportException x) {
                System.out.println("Something went wrong with Node connection.");
                System.exit(1);
            }
            return ans;
        }
    }


    public Status insertWord(String word, String def, int wordId) {
        // System.out.println("Word added to node " + this.id + "'s dictionary");
        Status ans = Status.ERROR;
        if (isResponsible(wordId)) {
            dict.put(word, def);
            return Status.SUCCESS;
        } else {
            return Status.ERROR; // Shouldn't ever get here
        }
    }


    // the first fingertable successor between this node and the id
    public NodeDetails ClosestPrecedingFinger(int id) {
        for (int i = fingers.length - 1; i >= 0; i--) {
            Finger finger = fingers[i];
            
            if (Range.InRangeExEx(finger.succ.id, info.id, id)) {
                return fingers[i].succ;
            }
        }
        return info;
    }


    // Find id's predecessor
    public NodeDetails FindPredecessor(int id) {
        NodeDetails node = info;

        while (!(Range.InRangeExIn(id, info.id, fingers[0].succ.id))) {  
            node = ClosestPrecedingFinger(id);
        }
        return node;
    }
    

    public Finger InitFinger(NodeDetails node, int i) {
        Finger finger = new Finger();
        finger.succ = fingers[0].succ;
        finger.start = (info.id + ((int) Math.pow(2, i))) % ((int) Math.pow(2, fingers.length));
        int end = finger.start + ((int) Math.pow(2, i));

        if (end > maxKey) {
            finger.last = end - maxKey - 1;
        } else {
            finger.last = end;
        }
    
        return finger;
    }


    public boolean updateFingerTableHelper(NodeDetails node, int i) {
        if (Range.InRangeInEx(node.id, fingers[i].start, fingers[i].succ.id)) { 
            fingers[i].succ = node;
            return true;
        } else {
            return false;
        }
    }


    /** 
     * node is an arbitrary node in the network used
     * to communicate with the rest of the network
     */
    private void InitFingerTable(NodeDetails node) {

        fingers[0] = InitFinger(null, 0);

        try {

            // Connect to node
            NodeConn con1 = factory.makeNodeConn(node);
            NodeDetails result1 = con1.Client.FindSuccessor(fingers[0].start);
            factory.closeNodeConn(con1);
            fingers[0].succ = result1;

            // Connect to Successor (fingers[0].succ)
            NodeConn con2 = factory.makeNodeConn(fingers[0].succ);
            NodeDetails succPred = con2.Client.GetPred();
            pred = succPred;
            factory.closeNodeConn(con2);

            // Connect to Successor's Predecessor (fingers[0].succ.pred)
            NodeConn con3 = factory.makeNodeConn(succPred);
            StatusData con3Data = con3.Client.SetPred(info);
            factory.closeNodeConn(con3);

            // connect to Predecessor
            NodeConn con4 = factory.makeNodeConn(pred);
            StatusData con4Data = con4.Client.SetSucc(info);
            factory.closeNodeConn(con4);

        } catch (TTransportException x) {
            System.out.println("Something went wrong with Node connection.");
            System.exit(1);
        }

        for (int i = 0; i < fingers.length - 1; i++) {
            Finger nextFinger = InitFinger(null, i + 1);

            if (Range.InRangeInEx(nextFinger.start, info.id, fingers[i].succ.id)) {
                try {
                    
                    // Connect to nextFinger.succ
                    // client4. SetSucc(manager.fingers[i].succ);
                    NodeConn con5 = factory.makeNodeConn(nextFinger.succ);
                    StatusData con5Data = con5.Client.SetSucc(fingers[i].succ);
                    factory.closeNodeConn(con5);

                } catch (TTransportException x) {
                    System.out.println("Something went wrong with Node connection.");
                    System.exit(1);
                }

            } else {
                try {
                    // Connect to node
                    // NodeDetails result2 = client5.FindSuccessor(nextFinger.start);
                    NodeConn con6 = factory.makeNodeConn(node);
                    NodeDetails result2 = con6.Client.FindSuccessor(nextFinger.start);
                    factory.closeNodeConn(con6);
                    

                    // Connect to nextFinger.succ
                    // client6.SetSucc(result2);
                    NodeConn con7 = factory.makeNodeConn(nextFinger.succ);
                    StatusData con7Data = con7.Client.SetSucc(result2);
                    factory.closeNodeConn(con7);

                } catch (TTransportException x) {
                    System.out.println("Something went wrong with Node connection.");
                    System.exit(1);
                }
            }

            fingers[i + 1] = nextFinger;
        }
    }

    
    
    public void updateOthers() {
        for (int i = 0; i < fingers.length; i++) {
            int nId = Range.CircularSubtraction(info.id, (int) Math.pow(2, i) - 1, maxKey);
            NodeDetails pred = FindPredecessor(nId);

            // Connect to pred
            try {
                NodeConn nodeCon = factory.makeNodeConn(pred); // connect to nextNode
                nodeCon.Client.UpdateFingerTable(info, i);
                factory.closeNodeConn(nodeCon);
            } catch (TTransportException x) {
                System.out.println("Something went wrong with Node connection.");
                System.exit(1);
            }

        }
    }


    /**
    * @param id the key we are checking if the current node is responsible for 
    * Checks if the current node is the successor for the given id
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


    public void initNewNode() {
        // this is the first node in the system
        for (int i = 0; i < fingers.length; i++) {
            fingers[i] = InitFinger(info, i);
        }
        pred = info;
    }


    public int GetId() {
        return info.id;
    }


    public ArrayList<Entry> getNodeEntries() {
        return cache.getList();
    }


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


    public int getHash(String word) {
        if (maxKey == -1) {
            System.out.println("ERROR: NodeManager.getHash() - attempted to hash prior to having a maxKey set");
            System.exit(1);
        }
        return hash.makeKey(word, maxKey);
    }


    public void setLog(int id)  {
        try {
            fileOut = new PrintStream(LOG_FILE + id + ".txt");
            System.setOut(fileOut);  
        } catch (FileNotFoundException x) {
            System.out.println("Not able to establish a log file.");
            System.exit(1);
        }
    }


    public void closeLog() {
        if (fileOut != null) {
            fileOut.close();
        }
    }


    public NodeDetails getSucc() {
        return fingers[0].succ;
    }


    public void setSucc(NodeDetails nodeInfo) {
        fingers[0].succ = nodeInfo;
    }
}
