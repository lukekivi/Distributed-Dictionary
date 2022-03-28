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
import utils.ConnFactory;
import utils.NodeConn;


public class NodeHandler implements Node.Iface {
    private NodeManager manager;

    public NodeHandler(NodeManager manager) {
        this.manager = manager;
    }


    @Override
    public Entry FindWordHelper(String word) {
        return manager.findWord(word);
    }


    @Override
    public StatusData InsertWordHelper(String word, String definition, int wordId) {
        StatusData result = new StatusData();
        Status status = manager.insertWord(word, definition, wordId);
        if (status == Status.SUCCESS) {
            result.msg = "Successfully inserted the word";
        } else {
            result.msg = "Something went wrong inserting the word";
        }
        result.status = status;
        return result;
    }


    @Override
    public StatusData FindPredCachingHelper(String word, String definition, int wordId) {
        StatusData result = new StatusData();
        Status status = manager.findPredCaching(word, definition, wordId);
        if (status == Status.SUCCESS) {
            result.msg = "Successfully cached the word";
        } else {
            result.msg = "Something went wrong caching the word";
        }
        result.status = status;
        return result;
    }
    

    @Override
    public GetData Get(String word) {
        Entry entry = manager.findWord(word);
        GetData data = new GetData();
        if (entry == null) {
            int wordId = manager.getHash(word);
            NodeDetails nextNode = manager.ClosestPrecedingFinger(wordId);

            // Set up connection to next 
            try {
                NodeConn con = factory.makeNodeConn(nextNode);
                data = con.Client.Get(word);
                factory.closeNodeConn(con);
            } catch (TTransportException x) {
                System.out.println("Something went wrong with Node connection.");
                System.exit(1);
            }
            return data;
            
        } else {
            data.definition = entry.definition;
            data.status = Status.SUCCESS;
            data.msg = "Word found by node " + manager.info.id;
            return data;
        }

        // *** THIS IS TEMPORARY FOR TESTING - START TESTING
        // System.out.print("Get() - " + word);

        // GetData data = new GetData();
        
        // data.status = Status.SUCCESS;
        // data.msg = word;
        // data.definition = "this is the definition";

        // return data;
        // *** END TESTING
    }

    @Override
    public StatusData Put(String word, String definition) {
        Status status = manager.putWord(word, definition); // Puts word if responsible
        StatusData data = new StatusData();

        if (status == Status.ERROR) { // Not responsible
            int wordId = manager.getHash(word);
            NodeDetails nextNode = manager.ClosestPrecedingFinger(wordId);

            try {
                // Set up connection to next 
                // return client.Put(word, definition);
                NodeConn con = factory.makeNodeConn(nextNode);
                data = con.Client.Put(word, definition);
                factory.closeNodeConn(con);
            } catch (TTransportException x) {
                System.out.println("Something went wrong with Node connection.");
                System.exit(1);
            }
            return data;

        } else {
            data.status = Status.SUCCESS;
            data.msg = "Entry put by node " + manager.info.id;
            return data;
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
        NodeDetails succ = new NodeDetails();
        try {
            NodeConn nodeCon = manager.factory.makeNodeConn(pred); // Connect to pred

            succ = nodeCon.Client.GetSucc(id);
            manager.factory.closeNodeConn(nodeCon);
        } catch (TTransportException x) {
            System.out.println("Something went wrong with Node connection.");
            System.exit(1);
        }
        return succ;
    }


    @Override
    public StatusData UpdateFingerTable(NodeDetails node, int i) { // Different from design specs doc
        boolean result = manager.updateFingerTableHelper(node, i);
        StatusData data = new StatusData();
        try {
            if (result) {

                NodeConn nodeCon = manager.factory.makeNodeConn(manager.pred); // Connect to pred
                StatusData connData = nodeCon.Client.UpdateFingerTable(node, i);
                manager.factory.closeNodeConn(connData);

                data.status = Status.SUCCESS;
                data.msg = "updated successfully: node " + manager.info.id;
                return data;
            } else {
                data.status = Status.SUCCESS;
                data.msg = "Didn't need to update: node " + manager.info.id;
                return data;
            }
        } catch (TTransportException x) {
            System.out.println("Something went wrong with Node connection.");
            System.exit(1);
        }
        return null;
    }

    @Override
    public void Kill() {
        System.out.println("Received kill command.");
        manager.closeLog();
        System.exit(0);
    }
}
