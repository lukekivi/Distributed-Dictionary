package node;

import pa2.Node;
import pa2.GetData;
import pa2.StatusData;
import pa2.NodeStructureData;
import pa2.NodeDetails;

public class NodeHandler implements Node.Iface {
    NodeManager manager;
    NodeDetails info;
    int maxKey;
    
    public InitializeNode(NodeJoinData joinData, int cacheSize) {
        this.manager = new NodeManager(data, cacheSize);
        this.info = joinData.nodeInfo;
        this.maxKey = maxKey = ((int) Math.pow(2, joinData.m)) - 1;
    }

    @Override
    public GetData Get(String word) {
        Entry entry = findWord(word);
        if (entry == null) {
            int wordId = utils.hashFunction(word, maxKey);
            NodeDetails next = nextJump(wordId);
            // Set up connection to next 
            // call Get() on next node
            // return that
        } else {
            GetData result = new GetData();
            result.definition = entry.def;
            result.status = Status.SUCCESS;
            result.msg = "Word found by node " + info.id;
        }
    }

    @Override
    public StatusData Put(String word, String definition) {
        Status status = putWord(word, definition); // Puts word if responsible
        if (status == Status.FAILURE) { // Not responsible
            int wordId = utils.hashFunction(word, maxKey);
            NodeDetails next = manager.ClosestPrecedingFinger(wordId);
            // Set up connection to next 
            // call Put() on next node
            // return that
        } else {
            StatusData result = new StatusData();
            result.status = Status.SUCCESS;
            result.msg = "Entry put by node " + info.id;
            return result;
        }
    }

    @Override
    public NodeStructureData GetNodeStructure() {
        NodeStructureData data = new NodeStructureData();

        NodeStructure nodeStruct = new NodeStructure();
        nodeStruct.id = info.id;
        nodeStruct.predId = manager.pred.id;
        nodeStruct.entries = manager.getList();
        nodeStruct.fingers = manager.getFingers();

        data.nodeStructure = nodeStruct;
        data.status = Status.SUCCESS;
        data.msg = "Data for node " + manager.id;

    }

    // Add to thrift
    public NodeDetails GetSucc() {
        return fingers[0].succ;
    }

    @Override
    public StatusData SetPredecessor(NodeDetails nodeInfo) {
        return null;
    }

    @Override
    public StatusData SetSuccessor(NodeDetails nodeInfo) {
        return null;
    }

    // find id's successor, PUT IN THRIFT
    public NodeDetails FindSuccessor(int id) {
        Node pred = manager.FindPredecessor(id);
        // establish connection to pred
        // call getSucc() on pred
    }

    private void UpdateOthers() {
        for (int i = 0; i < manager.fingers.length; i++) {
            int nId = utils.CircularSubtraction(this.id, (int) Math.pow(2, i) - 1);
            Node pred = FindPredecessor(nId);

            pred.UpdateFingerTable(this, i);
        }
    }

    @Override
    public StatusData UpdateFingerTable(NodeDetails node, int i) { // Different from design specs doc
        manager.updateFingerTableHelper(node, i);
        // Set up connection to pred
        // call updateFingerTable(node, i) on pred
    }

    /** 
     * node is an arbitrary node in the network used
     * to communicate with the rest of the network
     */
    private void InitFingerTable(NodeDetails node) {

        // fingers[0] = InitFinger(null, 0);
        // fingers[0].succ = node.FindSuccessor(fingers[0].start);

        // pred = fingers[0].succ.pred;
        
        // fingers[0].succ.pred = this;
        // pred.fingers[0].succ = this; 
        
        // for (int i = 0; i < fingers.length - 1; i++) {
        //     Finger nextFinger = InitFinger(null, i + 1);

        //     if (InRangeInEx(nextFinger.start, id, fingers[i].succ.id)) {
        //         nextFinger.succ = fingers[i].succ;

        //     } else {
        //         nextFinger.succ = node.FindSuccessor(nextFinger.start);
        //     }
        //     fingers[i + 1] = nextFinger;
        // }

    }



}
