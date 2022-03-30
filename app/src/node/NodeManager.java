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
import utils.*;


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

        System.out.println("Node " + info.id + " joined");
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
     * node is an arbitrary node in the network used
     * to communicate with the rest of the network
     */
    private void InitFingerTable(NodeDetails node) {
        final String FUNC_ID = "NodeManager.InitFingerTable()";
        System.out.println(FUNC_ID + ": Initing finger table");

        fingers[0] = InitFinger(null, 0);
        fingers[0].succ = NodeComm.findSuccessor(FUNC_ID, node, fingers[0].start);
    
        pred = NodeComm.getPred(FUNC_ID, fingers[0].succ);  // set pred to be the succ.pred
        NodeComm.setPred(FUNC_ID, fingers[0].succ, info);   // set succ.pred to be this
        NodeComm.setSucc(FUNC_ID, pred, info);              // set pred.succ to be this

        for (int i = 0; i < fingers.length - 1; i++) {
            System.out.println(FUNC_ID + ": Init finger " + (i+1));
            Finger nextFinger = InitFinger(null, i + 1);
            System.out.println(FUNC_ID + ": need to find the successor of " + nextFinger.start);
            System.out.println(FUNC_ID + ": init comparison " + info.id + " <= " + nextFinger.start + " < " + fingers[i].succ.id);
            if (Range.InRangeInEx(nextFinger.start, info.id, fingers[i].succ.id)) {
                System.out.println(FUNC_ID + ": init finger, true: handled locally");
                // the previous finger's successor is a valid successor for this finger too
                nextFinger.succ = fingers[i].succ;
            } else {
                System.out.println(FUNC_ID + ": init finger, false: handled remotely via node " + node.id);
                // search the DHT for the best successor for nextFinger
                nextFinger.succ = NodeComm.findSuccessor(FUNC_ID, node, nextFinger.start);
            }

            fingers[i + 1] = nextFinger;
        }

        NodeStructure nodeStructure = new NodeStructure();
        nodeStructure.id = info.id;
        nodeStructure.predId = pred.id;
        nodeStructure.fingers = getNodeFingers();
        nodeStructure.entries = getNodeEntries();

        Print.nodeStructure(nodeStructure);
    }


    /**
     * Update other nodes in the DHT that may require this new node in their fingerTables
     */
    public void updateOthers() {
        final String FUNC_ID = "NodeManager.updateOthers()";

        System.out.println(FUNC_ID + ": Updating others");

        for (int i = 0; i < fingers.length; i++) {
            int nId = Range.CircularSubtraction(info.id, (int) Math.pow(2, i) - 1, maxKey);
            NodeDetails pred = FindPredecessor(nId);
            System.out.println(FUNC_ID + ": Predecessor of " + nId + " is " + pred.id);

            if (pred.id != info.id) {
                System.out.println(FUNC_ID + ": Attempting to update\n\tfinger: " + i + "\n\tnode: " + pred.id);
                NodeComm.updateFingerTable(FUNC_ID, pred, info, i);
            }
        }
    }

    
    // Find id's predecessor
    public NodeDetails FindPredecessor(int id) {
        final String FUNC_ID = "NodeManager.FindPredecessor()";
        System.out.println(FUNC_ID + ": find pred of id " + id);

        NodeDetails nodeInfo = info;
        int nodeSuccId = fingers[0].succ.id;

        while (!(Range.InRangeExIn(id, nodeInfo.id, nodeSuccId))) {
            System.out.println(FUNC_ID + ": comparison " + nodeInfo.id + " < " + id + " <= " + nodeSuccId); 
            if (nodeInfo.id == info.id) {
                nodeInfo = ClosestPrecedingFinger(id);
            } else {
                nodeInfo = NodeComm.closestPrecedingFinger(FUNC_ID, nodeInfo, id);
            }
            nodeSuccId = NodeComm.getSucc(FUNC_ID, nodeInfo).id;
        }
        
        System.out.println(FUNC_ID + ": closest preceding finger was " + nodeInfo.id);

        return nodeInfo;
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

    

    /**
    * Returns the word and its definition
    */
    public Entry findWord(String word) {
        int wordId = getHash(word);
        // System.out.println("Get request came in for key " + wordId + " at Node " + info.id);
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
                    ans = nodeCon.client.FindWordHelper(word);
                    factory.closeNodeConn(nodeCon);
                } catch (TTransportException x) {
                    System.out.println("Error: Node " + info.id + " connect to Node " + nextNode.id + " inside findWord() - nodeCon: " + x.getStackTrace());
                    System.exit(1);
                } catch (TException e) {
                    System.out.println("Error: Node " + info.id + ": RPC FindWordHelper() call to Node " + nextNode.id + " inside findWord() - nodeCon: " + e.getStackTrace());
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
                StatusData insertData = nodeCon.client.InsertWordHelper(word, def, wordId);
                ans = insertData.status;
                factory.closeNodeConn(nodeCon);
            } catch (TTransportException x) {
                System.out.println("Error: Node " + info.id + " connect to Node " + nextNode.id + " inside findPredCaching() - nodeCon: " + x.getStackTrace());
                System.exit(1);
            } catch (TException e) {
                System.out.println("Error: Node " + info.id + ": RPC InsertWordHelper() call to Node " + nextNode.id + " inside findPredCaching() - nodeCon: " + e.getStackTrace());
                System.exit(1);
            }
            return ans;

        } else {
            try {
            // System.out.println("Moving key " + wordId + " to node " + nextNode.id);
                NodeConn nodeCon = factory.makeNodeConn(nextNode); // connect to nextNode
                StatusData cacheData = nodeCon.client.FindPredCachingHelper(word, def, wordId);
                ans = cacheData.status;
                factory.closeNodeConn(nodeCon);
            } catch (TTransportException x) {
                System.out.println("Error: Node " + info.id + " connect to Node " + nextNode.id + " inside findPredCaching() - nodeCon: " + x.getStackTrace());
                System.exit(1);
            } catch (TException e) {
                System.out.println("Error: Node " + info.id + ": RPC FindPredCachingHelper() call to Node " + nextNode.id + " inside findPredCaching() - nodeCon: " + e.getStackTrace());
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


    public int GetId() {
        return info.id;
    }


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


    public NodeDetails getSucc() {
        return fingers[0].succ;
    }


    public void setSucc(NodeDetails nodeInfo) {
        fingers[0].succ = nodeInfo;
    }

    
    public Finger getFinger(int i) {
        return fingers[i];
    }


    public void setFingerSucc(int i, NodeDetails nodeInfo) {
        fingers[i].succ = nodeInfo;
    }
}
