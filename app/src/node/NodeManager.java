package node;

import utils.ReadIn;
import java.io.PrintStream;
import utils.ServerInfo;
import java.math.BigInteger;
import java.util.HashMap;
import java.lang.Math;
import java.security.MessageDigest;
import java.util.Queue;

public class NodeManager {
    private final String LOG_FILE = "log/node";

    private int maxKey; // the max possible key in the DHT node is a member of
    private NodeDetails pred = null;
    private int id;
    private Finger[] fingers;
    private HashMap<String, String> dict;
    private Cache cache;
    private NodeDetails info;


// Should it be updatePred() and updateSucc() or should it be updateOthers()?
// IsResponsible() requires FindSuccessor() which requires connections, can we do it in NodeManager?
// Currently, FindPredecessor() is in manager, FindSuccessor() is in handler. Can manager establish connections?



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
        int wordId = utils.hashFunction(word, maxKey);
        System.out.println("Get request came in for key " + wordId + " at Node " + this.id);
        String def = "";
        Entry entry;
        if (isResponsible(wordId)) {
            def = dict.get(word);
            if (def == null) {
                return null;
            } else {
                entry.word = word;
                entry.def = def;
                return entry;
            }
        } else {
            entry = cache.checkCache(word);
            if (entry != null) {
                System.out.println("Grabbed from Cache");
                return entry;
            } else {
                // NodeDetails nextNode = nextJump(wordId);
                // ans = nextNode.findWord(word);
                // return ans; 
                return null;
            }
        }
    }

    /**
    * Returns a string describing the put status
    */
    public Status putWord(String word, String def) {
        int wordId = utils.hashFunction(word, maxKey);
        // System.out.println("Put request came in for key " + wordId + " at Node " + this.id);
        String ans = "FAILURE";
        if (isResponsible(wordId)) {
            ans = dict.put(word, def);
            return Status.SUCCESS;
        } else {
            // System.out.println("Adding entry to cache");
            Entry entry = new Entry(word, def);
            cache.addEntry(entry);

            return Status.FAILURE;

            // ans = nextNode.putWord(word, def);
            // if (ans.equals(word + " added SUCCESSFULLY")) {
            //     return word + " added SUCCESSFULLY";
            // } else {
            //     return word + " added UNSUCCESSFULLY";
            // }
        }
    }



    /**
     * Find the best finger table entry to send the dict entry
     * @param id key that will be sent to proper node
    */
    public NodeDetails nextJump(int id) {
        for (int i = fingers.length - 1; i >= 0; i--) {
            if (InRangeInEx(id, fingers[i].start, fingers[i].end)) {
                return fingers[i].succ;
            }
        }
        return null;
    }


    // the first fingertable successor between this node and the id
    public NodeDetails ClosestPrecedingFinger(int id) {
        for (int i = fingers.length - 1; i >= 0; i--) {
            Finger finger = fingers[i];
            
            if (InRangeExEx(finger.succ.id, this.id, id)) {
                return fingers[i].succ.id;
            }
        }
        return this;
    }

    // Find id's predecessor
    public NodeDetails FindPredecessor(int id) {
        NodeDetails node = info;

        while (!utils.InRangeExIn(id, info.id, GetSucc().id)) {  
            node = ClosestPrecedingFinger(id);
        }
        return node;
    }
    


    private Finger InitFinger(NodeDetails node, int i) {
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

    // the first fingertable successor between this node and the id
    public NodeDetails ClosestPrecedingFinger(int id) {
        for (int i = fingers.length - 1; i >= 0; i--) {
            Finger finger = fingers[i];
            
            if (utils.InRangeExEx(finger.succ.id, this.id, id)) {
                return fingers[i].succ;
            }
        }
        return info;
    }

    public int GetId() {
        return this.id;
    }

    public NodeDetails GetSucc() {
        return fingers[0].succ;
    }


    public List<Entry> getNodeEntries() {
        return cache.getList();
    }

    public List<Finger> getNodeFingers() {
        return cache.getFingers();
    }

    public void updateFingerTableHelper(NodeDetails node, int i) {
        if (utils.InRangeInEx(node.id, fingers[i].start, fingers[i].succ.id)) { 
            fingers[i].succ = node;
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
