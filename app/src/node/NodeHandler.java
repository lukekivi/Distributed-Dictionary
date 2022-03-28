package node;

import pa2.Node;
import pa2.GetData;
import pa2.StatusData;
import pa2.NodeStructureData;
import pa2.NodeDetails;
import pa2.Entry;
import pa2.Finger;
import pa2.Status;
import pa2.NodeStructure;
import pa2.NodeJoinData;
import utils.*;


public class NodeHandler implements Node.Iface {
    private NodeManager manager;
    private NodeDetails info;
    private int maxKey;
    
    public void InitializeNode(NodeJoinData joinData, int cacheSize) {
        this.manager = new NodeManager(joinData, cacheSize);
        this.info = joinData.nodeInfo;
        this.maxKey = maxKey = ((int) Math.pow(2, joinData.m)) - 1;
    }

    // ADD TO THRIFT
    /** 
     * node is an arbitrary node in the network used
     * to communicate with the rest of the network
     */
    public void Join(NodeDetails node, int id) {
        System.out.println("Node " + id + " joined");
        info.id = id;
        if (node == null) {
            manager.initNewNode();
        } else {
            // this is a new node in an existing system
            InitFingerTable(node);
            UpdateOthers();
        }
    }

    public GetData Get(String word) {
        Entry entry = manager.findWord(word);
        if (entry == null) {
            int wordId = HashHelp.hashFunction(word, maxKey);
            NodeDetails next = manager.ClosestPrecedingFinger(wordId);

            // Set up connection to next 
            return client.Get(word);
            
        } else {
            GetData result = new GetData();
            result.definition = entry.definition;
            result.status = Status.SUCCESS;
            result.msg = "Word found by node " + info.id;
        }
    }

    public StatusData Put(String word, String definition) {
        Status status = manager.putWord(word, definition); // Puts word if responsible
        if (status == Status.ERROR) { // Not responsible
            int wordId = HashHelp.hashFunction(word, maxKey);
            NodeDetails next = manager.ClosestPrecedingFinger(wordId);

            // Set up connection to next 
            return client.Put(word, definition);

        } else {
            StatusData result = new StatusData();
            result.status = Status.SUCCESS;
            result.msg = "Entry put by node " + info.id;
            return result;
        }
    }

    public NodeStructureData GetNodeStructure() {
        NodeStructureData data = new NodeStructureData();

        NodeStructure nodeStruct = new NodeStructure();
        nodeStruct.id = info.id;
        nodeStruct.predId = manager.pred.id;
        nodeStruct.entries = manager.getNodeEntries();
        nodeStruct.fingers = manager.getNodeFingers();

        data.nodeStructure = nodeStruct;
        data.status = Status.SUCCESS;
        data.msg = "Data for node " + manager.id;
    }

    // Add to thrift
    public NodeDetails GetSucc() {
        return manager.fingers[0].succ;
    }

    // add to thrift
    public NodeDetails GetPred() {
        return manager.pred;
    }

    public StatusData SetPredecessor(NodeDetails nodeInfo) {
        manager.pred = nodeInfo;
    }

    public StatusData SetSuccessor(NodeDetails nodeInfo) {
        manager.fingers[0].succ = nodeInfo;
    }

    // find id's successor, PUT IN THRIFT
    public NodeDetails FindSuccessor(int id) {
        NodeDetails pred = manager.FindPredecessor(id);

        // establish connection to pred
        return client.GetSucc(id);

    }

    private void UpdateOthers() {
        for (int i = 0; i < manager.fingers.length; i++) {
            int nId = Range.CircularSubtraction(info.id, (int) Math.pow(2, i) - 1);
            NodeDetails pred = manager.FindPredecessor(nId);

            // Connect to pred
            StatusData data = client.UpdateFingerTable(info, i);

        }
    }

    public StatusData UpdateFingerTable(NodeDetails node, int i) { // Different from design specs doc
        boolean result = manager.updateFingerTableHelper(node, i);
        StatusData data = new StatusData();
        if (result) {
            // Set up connection to pred
            client.updateFingerTable(node, i);

            data.status = Status.SUCCESS;
            data.msg = "updated successfully: node " + info.id;
            return data;
        } else {
            data.status = Status.SUCCESS;
            data.msg = "Didn't need to update: node " + info.id;
            return data;
        }
    }

    /** 
     * node is an arbitrary node in the network used
     * to communicate with the rest of the network
     */

    private void InitFingerTable(NodeDetails node) {

        manager.fingers[0] = manager.InitFinger(null, 0);

        // Connect to node
        NodeDetails result1 = client.FindSuccessor(manager.fingers[0].start);
        manager.fingers[0].succ = result1;

        // Connect to fingers[0].succ
        NodeDetails succPred = client1.GetPred();
        manager.pred = succPred;

        // Connect to fingers[0].succ.pred which is succPred  
        client2.SetPredecessor(info);

        // connect to getPred()
        client3.SetSuccessor(info);

        for (int i = 0; i < manager.fingers.length - 1; i++) {
            Finger nextFinger = manager.InitFinger(null, i + 1);

            if (Range.InRangeInEx(nextFinger.start, info.id, manager.fingers[i].succ.id)) {

                // Connect to nextFinger.succ
                client4. SetSuccessor(manager.fingers[i].succ);
                

            } else {
                // Connect to node
               NodeDetails result2 = client5.FindSuccessor(nextFinger.start);

                // Connect to nextFinger.succ
                client6.SetSuccessor(result2);

            }
            manager.fingers[i + 1] = nextFinger;

        }
    }



}
