package superNode;

import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import pa2.NodeDetails;
import pa2.Status;

public class SuperNodeManager {
    private final int M = 4;
    private final Random r = new Random();
    private final int MAX_KEY = ((int) Math.pow(2, M) - 1);
    private boolean isBusy = false;

    private ArrayList<NodeDetails> nodes = new ArrayList<NodeDetails>();

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
            data.status = Status.FAILURE;
            data.msg = "SuperNode wasn't expecting a node to be set.";
        } else if (!isAvailable(nodeInfo.id)) {
            data.status = Status.FAILURE;
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

        if (nodes.size() == MAX_KEY) {
            System.out.println("ERROR: exceeded max size.");
            return -1;
        }

        Random r = new Random();
        int id = r.nextInt(MAX_KEY);

        while (!IsAvailable(id)) {
            if (id == MAX_SIZE) {
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
            if (nodes.get(i).GetId() == id) {
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
}
