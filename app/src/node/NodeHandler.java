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
import utils.Hash;


public class NodeHandler implements Node.Iface {
    private NodeManager manager;

    public NodeHandler(NodeManager manager) {
        this.manager = manager;
    }
    

    @Override
    public GetData Get(String word) {
        // Entry entry = manager.findWord(word);
        // if (entry == null) {
        //     int wordId = manager.getHash(word);
        //     NodeDetails next = manager.ClosestPrecedingFinger(wordId);

        //     // Set up connection to next 
        //     // return client.Get(word);
        //     return null;
            
        // } else {
        //     GetData result = new GetData();
        //     result.definition = entry.definition;
        //     result.status = Status.SUCCESS;
        //     result.msg = "Word found by node " + manager.info.id;
        //     return result;
        // }

        // *** THIS IS TEMPORARY FOR TESTING - uncomment above
        System.out.print("Get() - " + word);

        GetData data = new GetData();
        
        data.status = Status.SUCCESS;
        data.msg = word;
        data.definition = "this is the definition";

        return data;
        // *** END TESTING
    }

    @Override
    public StatusData Put(String word, String definition) {
        Status status = manager.putWord(word, definition); // Puts word if responsible
        if (status == Status.ERROR) { // Not responsible
            int wordId = manager.getHash(word);
            NodeDetails next = manager.ClosestPrecedingFinger(wordId);

            // Set up connection to next 
            // return client.Put(word, definition);
            return null;

        } else {
            StatusData result = new StatusData();
            result.status = Status.SUCCESS;
            result.msg = "Entry put by node " + manager.info.id;
            return result;
        }
    }

    @Override
    public NodeStructureData GetNodeStructure() {
        NodeStructureData data = new NodeStructureData();

        NodeStructure nodeStruct = new NodeStructure();
        nodeStruct.id = manager.info.id;
        nodeStruct.predId = manager.pred.id;
        nodeStruct.entries = manager.getNodeEntries();
        nodeStruct.fingers = manager.getNodeFingers();

        data.nodeStructure = nodeStruct;
        data.status = Status.SUCCESS;
        data.msg = "Data for node " + manager.info.id;
        return data;
    }


    @Override
    public NodeDetails GetSucc() {
        return manager.getSucc();
    }


    @Override
    public StatusData SetSucc(NodeDetails nodeInfo) {
        manager.setSucc(nodeInfo);;
        StatusData data = new StatusData();
        data.status = Status.SUCCESS;
        data.msg = "Successfully set the successor";
        return data;
    }
    

    @Override
    public NodeDetails GetPred() {
        return manager.pred;
    }


    @Override
    public StatusData SetPred(NodeDetails nodeInfo) {
        manager.pred = nodeInfo;
        StatusData data = new StatusData();
        data.status = Status.SUCCESS;
        data.msg = "Successfully set the predecessor";
        return data;
    }


    @Override
    public NodeDetails FindSuccessor(int id) {
        NodeDetails pred = manager.FindPredecessor(id);

        // establish connection to pred
        // return client.GetSucc(id);
        return null;

    }


    @Override
    public StatusData UpdateFingerTable(NodeDetails node, int i) { // Different from design specs doc
        boolean result = manager.updateFingerTableHelper(node, i);
        StatusData data = new StatusData();
        if (result) {
            // Set up connection to pred
            // client.updateFingerTable(node, i);

            data.status = Status.SUCCESS;
            data.msg = "updated successfully: node " + manager.info.id;
            return data;
        } else {
            data.status = Status.SUCCESS;
            data.msg = "Didn't need to update: node " + manager.info.id;
            return data;
        }
    }

    @Override
    public void Kill() {
        System.out.println("Received kill command.");
        manager.closeLog();
        System.exit(0);
    }
}
