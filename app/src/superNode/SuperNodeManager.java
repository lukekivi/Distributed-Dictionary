package superNode;

import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import pa2.*;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;
import utils.NodeComm;
import utils.NodeConn;
import utils.SuperConn;

/**
 * Does all of extra labor and data storage of the SuperNodeHandler.
 */
public class SuperNodeManager {
    private int M;
    private int maxKey;
    private boolean isBusy = false;
    private final Random r = new Random();
    private ArrayList<NodeDetails> nodes = new ArrayList<NodeDetails>();


    public SuperNodeManager(int M) {
        this.M = M;
        maxKey = ((int) Math.pow(2, M) - 1);
    }


    /**
     * Get a random node from the DHT.
     * @return NodeDetails of a node or null
     */
    public NodeDetails getRandomNode() {
        int size = nodes.size();

        if (size == 0) {
            return null;
        }
        
        int index = r.nextInt(size);

        return nodes.get(index);
    }


    /**
     * Add established node to tracked nodes.
     */
    public StatusData setNode(NodeDetails nodeInfo) {
        StatusData data = new StatusData();

        if (isBusy == false) {
            data.status = Status.ERROR;
            data.msg = "SuperNode wasn't expecting a node to be set.";
        } else if (!isAvailable(nodeInfo.id)) {
            data.status = Status.ERROR;
            data.msg = "Node ID " + nodeInfo.id + " was already taken.";
        } else {
            isBusy = false;
            nodes.add(nodeInfo);
            data.status = Status.SUCCESS;
            data.msg = null;
        }

        return data;
    }


    /**
     * Return a valid random hashed ID between 0 and (2^M)-1.
     */
    public int getHashID() {
        isBusy = true;

        if (nodes.size() == maxKey) {
            System.out.println("ERROR: " + nodes.size() + " exceeded max size of " + maxKey + ".");
            return -1;
        }

        Random r = new Random();
        int id = r.nextInt(maxKey);

        while (!isAvailable(id)) {
            if (id == maxKey) {
                id = 0;
            } else {
                id++;
            }
        }

        return id;        
    }


    /**
     * Check if id is available.
     */
    private boolean isAvailable(int id) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).id == id) {
                return false;
            }
        }
        return true;
    }


    public boolean isBusy() {
        return isBusy;
    }


    public int getM() {
        return M;
    }

    public int getNodesSize() {
        return nodes.size();
    }

    public void killNodes() {
        final String FUNC_ID = "SuperNodeManager.killNodes()";
        for (int i = 0; i < nodes.size(); i++) {
            NodeComm.kill(FUNC_ID, nodes.get(i));
        }
    }

    public NodeDetails getNode(int i) {
        return nodes.get(i);
    }
}
