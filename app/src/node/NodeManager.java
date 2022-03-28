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

import pa2.NodeDetails;
import pa2.Finger;
import pa2.NodeJoinData;
import pa2.Entry;
import pa2.Status;
import utils.*;


public class NodeManager {
    private final String LOG_FILE = "log/node";

    public int maxKey; // the max possible key in the DHT node is a member of
    public NodeDetails pred = null;
    public int id;
    public Finger[] fingers;
    public HashMap<String, String> dict;
    public Cache cache;
    public NodeDetails info;


    public NodeManager(NodeJoinData data, int cacheSize) {
        maxKey = ((int) Math.pow(2, data.m)) - 1;

        fingers = new Finger[data.m];

        for (int i = 0; i < fingers.length; i++) {
            fingers[i] = null;
        }
        cache = new Cache(cacheSize);
        dict = new HashMap<String, String>();
        id = data.id;
        info = data.nodeInfo;
    }

    /**
    * Returns the word and its definition
    */
    public Entry findWord(String word) {
        int wordId = HashHelp.hashFunction(word, maxKey);
        System.out.println("Get request came in for key " + wordId + " at Node " + this.id);
        String def = "";
        Entry entry;
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
                return null; // Not in cache, go to next node
            }
        }
    }

    /**
    * Returns a string describing the put status
    */
    public Status putWord(String word, String def) {
        int wordId = HashHelp.hashFunction(word, maxKey);
        // System.out.println("Put request came in for key " + wordId + " at Node " + this.id);
        String ans = "FAILURE";
        if (isResponsible(wordId)) {
            ans = dict.put(word, def);
            return Status.SUCCESS;
        } else {
            // System.out.println("Adding entry to cache");
            Entry entry = new Entry(word, def);
            cache.addEntry(entry);

            return Status.ERROR;
        }
    }



    /**
     * Find the best finger table entry to send the dict entry
     * @param id key that will be sent to proper node
    */
    public NodeDetails nextJump(int id) {
        for (int i = fingers.length - 1; i >= 0; i--) {
            if (Range.InRangeInEx(id, fingers[i].start, fingers[i].end)) {
                return fingers[i].succ;
            }
        }
        return null;
    }


    // the first fingertable successor between this node and the id
    public NodeDetails ClosestPrecedingFinger(int id) {
        for (int i = fingers.length - 1; i >= 0; i--) {
            Finger finger = fingers[i];
            
            if (Range.InRangeExEx(finger.succ.id, this.id, id)) {
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
        Finger finger = new Finger(node);
        finger.start = (this.id + ((int) Math.pow(2, i))) % ((int) Math.pow(2, fingers.length));
        int end = finger.start + ((int) Math.pow(2, i));

        if (end > maxKey) {
            finger.last = end - maxKey - 1;
        } else {
            finger.last = end;
        }
    
        return finger;
    }

    public int GetId() {
        return this.id;
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

    public boolean updateFingerTableHelper(NodeDetails node, int i) {
        if (Range.InRangeInEx(node.id, fingers[i].start, fingers[i].succ.id)) { 
            fingers[i].succ = node;
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
        pred = this;
    }

    /**
    * @param id the key we are checking if the current node is responsible for 
    * Checks if the current node is the successor for the given id
    */
    public boolean isResponsible(int id) {
        if (id == this.id) {
            return true;
        }
        if (Range.InRangeExEx(id, pred.id, this.id)) {
            return true;
        } else {
            return false;
        }
    }













    private ReadIn readIn = new ReadIn();

    public void setLog(int id)  {
        try {
            PrintStream fileOut = new PrintStream(LOG_FILE + id + ".txt");
            System.setOut(fileOut);  
        } catch (FileNotFoundException x) {
            System.out.println("Not able to establish a log file.");
            System.exit(1);
        }
    }

    public String getIpAddressOfSelf() {

    }

    public int getPortOfSelf() {
        // read in portnumber from the config file
        return readIn.getNodePort();
    }

    public ServerInfo getSuperNodeInfo() {
        return readIn.getSuperNodeInfo();
    }
}
