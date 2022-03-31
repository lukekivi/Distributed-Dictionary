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
import pa2.EntryData;
import utils.Hash;
import utils.NodeComm;
import utils.ConnFactory;
import utils.NodeConn;
import utils.Range;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;


/**
 * Landing site for RPCs to Node. Uses the NodeManager as an assistant.
 */
public class NodeHandler implements Node.Iface {
    private NodeManager manager;

    public NodeHandler(NodeManager manager) {
        this.manager = manager;
    }

    
    /** 
     * Starts the process of a Get() in the DHT system
     * @param String word that we want to grab the definition of
     */
    @Override
    public GetData Get(String word) {
        EntryData result = manager.findWord(word);
        GetData data = new GetData();
        if (result.entry == null && result.status == Status.ERROR) { // Not in cache, isnt' correct dict
            int wordId = manager.getHash(word);
            NodeDetails nextNode = manager.ClosestPrecedingFinger(wordId);

            if (nextNode.id == manager.info.id) { // This node is closest preceding node to the word, send to its successor
                nextNode = manager.getSucc();
            }
            // Set up connection to next 
            System.out.println("Node " + manager.info.id + ": forwarding Get() for " + word + "(key " + wordId + ") to node " + nextNode.id + " since it wasn't in the cache and not correct node");
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
            
        } else if (result.entry == null && result.status == Status.SUCCESS) { // Correct node, not in proper dict
            // System.out.println("Node " + info.id +": " + word + "(key " + wordId + ") not in the dictionary. This is the proper node";
            data.definition = null;
            data.status = Status.ERROR;
            data.msg = word + " not in the dictionary";
            return data;
        }
        else { // Found it
            data.definition = result.entry.definition;
            data.status = Status.SUCCESS;
            data.msg = "Word found by node " + manager.info.id;
            return data;
        }
    }

    /** 
     * Starts the process of a Put() request into the DHT system
     * @param String word that we want to store
     * @param String definition of the word
     */
    @Override
    public StatusData Put(String word, String definition) {
        Status status = manager.putWord(word, definition); // Puts word if responsible
        StatusData data = new StatusData();

        if (status == Status.ERROR) { // Not responsible
            int wordId = manager.getHash(word);
            NodeDetails nextNode = manager.ClosestPrecedingFinger(wordId);
            if (nextNode.id == manager.info.id) {
                nextNode = manager.getSucc();
            }
            try {
                // Set up connection to next 
                // return client.Put(word, definition);
                System.out.println("Node " + manager.info.id + ": forwarding Put() for " + word + "(key " + wordId + ") to node " + nextNode.id + " since this isn't the proper node");
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


    /** 
     * Returns the NodeStructure of this node along with a message and status
     */
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


    /** 
     * Grabs the successor from the Node Manager
     */
    @Override
    public NodeDetails GetSucc() {
        return manager.getSucc();
    }


    /** 
     * Sets the successor using the input NodeDetails
     */
    @Override
    public StatusData SetSucc(NodeDetails nodeInfo) {
        manager.setSucc(nodeInfo);
        StatusData data = new StatusData();
        data.status = Status.SUCCESS;
        data.msg = "Successfully set the successor";
        return data;
    }
    

    /** 
     * Uses the node manager to get the node's predecessor
     */
    @Override
    public NodeDetails GetPred() {
        return manager.pred;
    }


    /** 
     * Uses the input node to set the current node's predecessor
     */
    @Override
    public StatusData SetPred(NodeDetails nodeInfo) {
        manager.pred = nodeInfo;
        StatusData data = new StatusData();
        data.status = Status.SUCCESS;
        data.msg = "Successfully set the predecessor";
        return data;
    }


    /** 
     * Finds the successor of the id passed in
     * @param int the id of which we want the successor
     */
    @Override
    public NodeDetails FindSuccessor(int id) {
        final String FUNC_ID = "NodeHandler.FindSuccessor()";

        NodeDetails predInfo = manager.FindPredecessor(id);

        if (predInfo.id == manager.info.id) {
            return manager.getSucc();
        }

        return NodeComm.getSucc(FUNC_ID, predInfo);
    }


    /** 
     * Updates the ith index of the finger table using node
     * @param NodeDetails node
     * @param Sint i
     */
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

    /** 
     * Get the closest preceding finger of id
     * @param int id of which we want to the find the closest predecessor
     */
    @Override
    public NodeDetails ClosestPrecedingFinger(int id) {
        return manager.ClosestPrecedingFinger(id);
    }


    /** 
     * Closes down the system
     */
    @Override
    public void Kill() {
        System.out.println("Node " + manager.info.id + ": Received kill command.");
        manager.closeLog();
        System.exit(0);
    }
}
