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
import utils.NodeComm;
import utils.ConnFactory;
import utils.NodeConn;
import utils.Range;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;


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
                NodeConn con = manager.factory.makeNodeConn(nextNode);
                data = con.client.Get(word);
                manager.factory.closeNodeConn(con);
            } catch (TTransportException x) {
                System.out.println("Error: Node " + manager.info.id + " connect to Node " + nextNode.id + " inside Get() - con: " + x.getStackTrace());
                System.exit(1);
            } catch (TException e) {
                System.out.println("Error: Node " + manager.info.id + ": RPC Get() call to Node " + nextNode.id + " inside Get() - con: " + e.getStackTrace());
                System.exit(1);
            }
            return data;
            
        } else {
            data.definition = entry.definition;
            data.status = Status.SUCCESS;
            data.msg = "Word found by node " + manager.info.id;
            return data;
        }
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
                NodeConn con = manager.factory.makeNodeConn(nextNode);
                data = con.client.Put(word, definition);
                manager.factory.closeNodeConn(con);
            } catch (TTransportException x) {
                System.out.println("Error: Node " + manager.info.id + " connect to Node " + nextNode.id + " inside Put() - con: " + x.getStackTrace());
                System.exit(1);
            } catch (TException e) {
                System.out.println("Error: Node " + manager.info.id + ": RPC Put() call to Node " + nextNode.id + " inside Put() - con: " + e.getStackTrace());
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
        manager.setSucc(nodeInfo);
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
        final String FUNC_ID = "NodeHandler.FindSuccessor()";

        NodeDetails predInfo = manager.FindPredecessor(id);

        if (predInfo.id == manager.info.id) {
            return manager.getSucc();
        }

        return NodeComm.getSucc(FUNC_ID, predInfo);
    }


    @Override
    public StatusData UpdateFingerTable(NodeDetails node, int i) { // Different from design specs doc
        final String FUNC_ID = "NodeHandler.UpdateFingerTable()";

        StatusData data = new StatusData();

        Finger finger = manager.getFinger(i);

        if (Range.InRangeInEx(node.id, finger.start, finger.succ.id)) {
            
            manager.setFingerSucc(i, node);

            if (manager.pred.id != node.id) {
                data = NodeComm.updateFingerTable(FUNC_ID, manager.pred, node, i);
            }
            
        } else {
            data.status = Status.SUCCESS;
            data.msg = "Succesfull update.";
        }

        return data;
    }


    @Override
    public NodeDetails ClosestPrecedingFinger(int id) {
        return manager.ClosestPrecedingFinger(id);
    }


    @Override
    public void Kill() {
        System.out.println("Node " + manager.info.id + ": Received kill command.");
        manager.closeLog();
        System.exit(0);
    }
}
