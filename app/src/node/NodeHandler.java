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
        Status status = putWord(word, definition);
        if (status == Status.FAILURE) {
            int wordId = utils.hashFunction(word, maxKey);
            NodeDetails next = nextJump(wordId);
            // Set up connection to next 
            // call Put() on next node
            // return that
        } else {
            StatusData result = new StatusData();
            result.status = Status.SUCCESS;
            result.msg = "Entry put by node " + info.id;
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

    @Override
    public StatusData UpdatePredecessor(NodeDetails nodeInfo) {
        return null;
    }

    @Override
    public StatusData UpdateSuccessor(NodeDetails nodeInfo) {
        return null;
    }

    // find id's successor
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
}
