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
            NodeDetails result1 = con1.client.FindSuccessor(fingers[0].start);
            factory.closeNodeConn(con1);
            fingers[0].succ = result1;
        } catch (TTransportException x) {
            System.out.println("Error: Node " + info.id + " connect to Node " + node.id + " inside InitFingerTable() - con1: " + x.getStackTrace());
            System.exit(1);
        } catch (TException e) {
            System.out.println("Error: Node " + info.id + ": RPC FindSuccessor(fingers[0].start) or call to Node " + node.id + " inside InitFingerTable() - con1: " + e.getStackTrace());
            System.exit(1);
        }    

        NodeDetails succPred = new NodeDetails();
        try {
            // Connect to Successor (fingers[0].succ)
            NodeConn con2 = factory.makeNodeConn(fingers[0].succ);
            succPred = con2.client.GetPred();
            pred = succPred;
            factory.closeNodeConn(con2);

        } catch (TTransportException x) {
            System.out.println("Error: Node " + info.id + " connect to Node " + fingers[0].succ.id + " inside InitFingerTable() - con2: " + x.getStackTrace());
            System.exit(1);
        } catch (TException e) {
            System.out.println("Error: Node " + info.id + ": RPC GetPred() or call to Node " + fingers[0].succ.id + " inside InitFingerTable() - con2: " + e.getStackTrace());
            System.exit(1);
        }    

        try {

            // Connect to Successor's Predecessor (fingers[0].succ.pred)
            NodeConn con3 = factory.makeNodeConn(succPred);
            StatusData con3Data = con3.client.SetPred(info);
            factory.closeNodeConn(con3);

        } catch (TTransportException x) {
            System.out.println("Error: Node " + info.id + " connect to Node " + succPred.id + " inside InitFingerTable() - con3: " + x.getStackTrace());
            System.exit(1);
        } catch (TException e) {
            System.out.println("Error: Node " + info.id + ": RPC SetPred(info) or call to Node " + succPred.id + " inside InitFingerTable() - con3: " + e.getStackTrace());
            System.exit(1);
        }    

        try {

            // connect to Predecessor
            NodeConn con4 = factory.makeNodeConn(pred);
            StatusData con4Data = con4.client.SetSucc(info);
            factory.closeNodeConn(con4);

        } catch (TTransportException x) {
            System.out.println("Error: Node " + info.id + " connect to Node " + pred.id + " inside InitFingerTable() - con4: " + x.getStackTrace());
            System.exit(1);
        } catch (TException e) {
            System.out.println("Error: Node " + info.id + ": RPC SetSucc(info) or call to Node " + pred.id + " inside InitFingerTable() - con4: " + e.getStackTrace());
            System.exit(1);
        }

        for (int i = 0; i < fingers.length - 1; i++) {
            Finger nextFinger = InitFinger(null, i + 1);

            if (Range.InRangeInEx(nextFinger.start, info.id, fingers[i].succ.id)) {
                try {
                    
                    // Connect to nextFinger.succ
                    // client4. SetSucc(manager.fingers[i].succ);
                    NodeConn con5 = factory.makeNodeConn(nextFinger.succ);
                    StatusData con5Data = con5.client.SetSucc(fingers[i].succ);
                    factory.closeNodeConn(con5);

                } catch (TTransportException x) {
                    System.out.println("Error: Node " + info.id + " connect to Node " + nextFinger.succ.id + " inside InitFingerTable() - con5: " + x.getStackTrace());
                    System.exit(1);
                } catch (TException e) {
                    System.out.println("Error: Node " + info.id + ": RPC SetSucc() call to Node " + nextFinger.succ.id + " inside InitFingerTable() - con5: " + e.getStackTrace());
                    System.exit(1);
                }
            } else {
                NodeDetails result2 = new NodeDetails();
                try {
                    // Connect to node
                    // NodeDetails result2 = client5.FindSuccessor(nextFinger.start);
                    NodeConn con6 = factory.makeNodeConn(node);
                    result2 = con6.client.FindSuccessor(nextFinger.start);
                    factory.closeNodeConn(con6);
                } catch (TTransportException x) {
                    System.out.println("Error: Node " + info.id + " connect to Node " + node.id + " inside InitFingerTable() - con6: " + x.getStackTrace());
                    System.exit(1);
                } catch (TException e) {
                    System.out.println("Error: Node " + info.id + ": RPC FindSuccessor(nextFingers.start) call to Node " + node.id + " inside InitFingerTable() - con6: " + e.getStackTrace());
                    System.exit(1);
                }

                try {
                    // Connect to nextFinger.succ
                    // client6.SetSucc(result2);
                    NodeConn con7 = factory.makeNodeConn(nextFinger.succ);
                    StatusData con7Data = con7.client.SetSucc(result2);
                    factory.closeNodeConn(con7);

                } catch (TTransportException x) {
                    System.out.println("Error: Node " + info.id + " connect to Node " + nextFinger.succ.id + " inside InitFingerTable() for loop else statement - con7: ");
                    x.printStackTrace();
                    System.exit(1);
                } catch (TException e) {
                    System.out.println("Error: Node " + info.id + ": RPC SetSucc(result2) call to Node " + nextFinger.succ.id + " inside InitFingerTable() for loop else statement - con7: " + e.getStackTrace());
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
                nodeCon.client.UpdateFingerTable(info, i);
                factory.closeNodeConn(nodeCon);
            } catch (TTransportException x) {
                System.out.println("Error: Node " + info.id + " connect to Node " + pred.id + " inside updateOthers() - nodeCon: " + x.getStackTrace());
                System.exit(1);
            } catch (TException e) {
                System.out.println("Error: Node " + info.id + ": RPC UpdateFingerTable() call to Node " + pred.id + " inside udpateOthers() - nodeCon: " + e.getStackTrace());
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
}
