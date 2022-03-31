package superNode;

import utils.Print;
import utils.NodeComm;
import pa2.DHTData;
import pa2.NodeDetails;
import pa2.NodeForClientData;
import pa2.NodeJoinData;
import pa2.Status;
import pa2.JoinStatus;
import pa2.StatusData;
import pa2.SuperNode;
import pa2.NodeStructureData;
import pa2.NodeStructure;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;
import java.util.ArrayList;
import utils.NodeConn;


/**
 * Landing site for RPCs to SuperNode. Uses the SuperNodeHandler as an assistant.
 */
public class SuperNodeHandler implements SuperNode.Iface {
    private SuperNodeManager manager;
    
    public SuperNodeHandler(int M) {
        manager = new SuperNodeManager(M);
    }
    
    @Override
    public NodeForClientData GetNodeForClient() {
        NodeForClientData nodeData = new NodeForClientData();
        nodeData.nodeInfo = manager.getRandomNode();

        if (nodeData.nodeInfo == null) {
            nodeData.status = Status.ERROR;
            nodeData.msg = "There are no nodes in the DHT";
        } else {
            nodeData.status = Status.SUCCESS;
            nodeData.msg = null;
        }

        return nodeData;
    }

    @Override
    public NodeJoinData GetNodeForJoin() {
        NodeJoinData nodeJoinData = new NodeJoinData(); 
        nodeJoinData.m = manager.getM();

        if (manager.isBusy()) {
            // a node is already establishin itself. Cannot do two at once.
            nodeJoinData.id = -1;
            nodeJoinData.nodeInfo = null;
            nodeJoinData.status = JoinStatus.BUSY;
            nodeJoinData.msg = null;
        } else {
            nodeJoinData.id = manager.getHashID();

            if (nodeJoinData.id == -1) {
                // DHT is full
                nodeJoinData.nodeInfo = null;
                nodeJoinData.status = JoinStatus.ERROR;
                nodeJoinData.msg = "DHT is full; cannot add more nodes.";
            } else {
                // Success state
                nodeJoinData.nodeInfo = manager.getRandomNode();
                nodeJoinData.msg = null;

                if (nodeJoinData.nodeInfo == null) {
                    // this node is the first one in the DHT
                    nodeJoinData.status = JoinStatus.ORIGINAL;
                } else {
                    nodeJoinData.status = JoinStatus.SUCCESS;
                }
            }
        }

        return nodeJoinData;
    }

    @Override
    public StatusData PostJoin(NodeDetails nodeInfo) {
        return manager.setNode(nodeInfo);
    }

    @Override
    public DHTData GetDHTStructure() {
        final String FUNC_ID = "SuperNodeHandler.GetDHTStructure()";
        DHTData data = new DHTData();
        ArrayList<NodeStructure> nodeStructures = new ArrayList<NodeStructure>();
        for (int i = 0; i < manager.getNodesSize(); i++) {
            NodeDetails node = manager.getNode(i);
            System.out.print("Getting details for ");
            Print.nodeDetails(node);
    
            NodeStructureData nodeStructureData = NodeComm.getNodeStructure(FUNC_ID, node);
            nodeStructures.add(nodeStructureData.nodeStructure);
            System.out.print("Got details");
        }

        data.nodeStructures = nodeStructures;
        data.status = Status.SUCCESS;
        data.msg = "Node structures added for " + nodeStructures.size() + "nodes";
        return data;

    }

    @Override
    public void KillDHT() {
        System.out.println("Received KillDHT commant. Bye bye.");
        manager.killNodes();
        System.exit(0);
    }
}
