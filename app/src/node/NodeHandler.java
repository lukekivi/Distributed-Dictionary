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
    public NodeManager manager;
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
            // return client.Get(word);
            return null;
            
        } else {
            GetData result = new GetData();
            result.definition = entry.definition;
            result.status = Status.SUCCESS;
            result.msg = "Word found by node " + info.id;
            return result;
        }
    }

    public StatusData Put(String word, String definition) {
        Status status = manager.putWord(word, definition); // Puts word if responsible
        if (status == Status.ERROR) { // Not responsible
            int wordId = HashHelp.hashFunction(word, maxKey);
            NodeDetails next = manager.ClosestPrecedingFinger(wordId);

            // Set up connection to next 
            // return client.Put(word, definition);
            return null;

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
        return data;
    }

    public NodeDetails GetSucc() {
        return manager.fingers[0].succ;
    }


    public StatusData SetSucc(NodeDetails nodeInfo) {
        manager.fingers[0].succ = nodeInfo;
        StatusData data = new StatusData();
        data.status = Status.SUCCESS;
        data.msg = "Successfully set the successor";
        return data;
    }
    

    public NodeDetails GetPred() {
        return manager.pred;
    }


    public StatusData SetPred(NodeDetails nodeInfo) {
        manager.pred = nodeInfo;
        StatusData data = new StatusData();
        data.status = Status.SUCCESS;
        data.msg = "Successfully set the predecessor";
        return data;
    }


    // find id's successor, PUT IN THRIFT
    public NodeDetails FindSuccessor(int id) {
        NodeDetails pred = manager.FindPredecessor(id);

        // establish connection to pred
        // return client.GetSucc(id);
        return null;

    }

    private void UpdateOthers() {
        for (int i = 0; i < manager.fingers.length; i++) {
            int nId = Range.CircularSubtraction(info.id, (int) Math.pow(2, i) - 1, maxKey);
            NodeDetails pred = manager.FindPredecessor(nId);

            // Connect to pred
            // StatusData data = client.UpdateFingerTable(info, i);

        }
    }

    public StatusData UpdateFingerTable(NodeDetails node, int i) { // Different from design specs doc
        boolean result = manager.updateFingerTableHelper(node, i);
        StatusData data = new StatusData();
        if (result) {
            // Set up connection to pred
            // client.updateFingerTable(node, i);

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
        // NodeDetails result1 = client.FindSuccessor(manager.fingers[0].start);
        // manager.fingers[0].succ = result1;

        // Connect to fingers[0].succ
        // NodeDetails succPred = client1.GetPred();
        // manager.pred = succPred;

        // Connect to fingers[0].succ.pred which is succPred  
        // client2.SetPred(info);

        // connect to getPred()
        // client3.SetSucc(info);

        for (int i = 0; i < manager.fingers.length - 1; i++) {
            Finger nextFinger = manager.InitFinger(null, i + 1);

            if (Range.InRangeInEx(nextFinger.start, info.id, manager.fingers[i].succ.id)) {

                // Connect to nextFinger.succ
                // client4. SetSucc(manager.fingers[i].succ);
                

            } else {
                // Connect to node
            //    NodeDetails result2 = client5.FindSuccessor(nextFinger.start);

                // Connect to nextFinger.succ
                // client6.SetSucc(result2);

            }
            manager.fingers[i + 1] = nextFinger;

        }
    }



}
