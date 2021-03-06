package utils;

import pa2.NodeDetails;
import pa2.Node;
import pa2.NodeStructureData;
import pa2.StatusData;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;


/**
 * NodeComm is a class that conveniently handles node communication. It
 * essentially just abstracts away thrift boiler plate and truly allows 
 * users to treat RPC calls like simple function calls.
 * 
 * It creates thrift connections between system entites via the ConnFactory.
 * Then it calls RPC functions and handles errors. Each function gets a 
 * [from] field which helps build better error messages. It is supposed to be
 * of the form ClassName.functionName().
 */
public class NodeComm {
    private static final ConnFactory connFactory = new ConnFactory();

    public static NodeDetails getSucc(String from, NodeDetails nodeInfo) {
        NodeDetails succInfo = null;
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            succInfo = nodeConn.client.GetSucc();

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            handleException(x, from, "calling NodeComm.getSucc() onto node" + nodeInfo.id);
        } catch (TException x) {
            handleException(x, from, "calling NodeComm.getSucc() onto node" + nodeInfo.id);
        }
        return succInfo;
    }


    public static void setSucc(String from, NodeDetails nodeInfo, NodeDetails succInfo) {
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            nodeConn.client.SetSucc(succInfo);

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            handleException(x, from, "NodeComm.setSucc() onto node" + nodeInfo.id);
        } catch (TException x) {
            handleException(x, from, "NodeComm.setSucc() onto node" + nodeInfo.id);
        }
    }


    public static NodeDetails findSuccessor(String from, NodeDetails nodeInfo, int id) {
        NodeDetails succInfo = null;
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            succInfo = nodeConn.client.FindSuccessor(id);

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            handleException(x, from, "NodeComm.findSuccessor(" + id + ") onto node" + nodeInfo.id);
        } catch (TException x) {
            handleException(x, from, "NodeComm.findSuccessor(" + id + ") onto node" + nodeInfo.id);
        }
        return succInfo;
    }


    public static NodeDetails getPred(String from, NodeDetails nodeInfo) {
        NodeDetails predInfo = null;
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            predInfo = nodeConn.client.GetPred();

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            handleException(x, from, "NodeComm.getPred() onto node" + nodeInfo.id);
        } catch (TException x) {
            handleException(x, from, "NodeComm.getPred() onto node" + nodeInfo.id);
        }
        return predInfo;
    }


    public static void setPred(String from, NodeDetails nodeInfo, NodeDetails predInfo) {
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            nodeConn.client.SetPred(predInfo);

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            handleException(x, from, "NodeComm.setPred() onto node" + nodeInfo.id + ". Setting pred to " + predInfo.id);
        } catch (TException x) {
            handleException(x, from, "NodeComm.setPred() onto node" + nodeInfo.id + ". Setting pred to " + predInfo.id);
        }
    }


    public static StatusData updateFingerTable(String from, NodeDetails nodeInfo, NodeDetails newNodeInfo, int fingerTableIndex) {
        StatusData statusData = null;
        
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            statusData = nodeConn.client.UpdateFingerTable(newNodeInfo, fingerTableIndex);

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            handleException(x, from, "NodeComm.updateFingerTable() onto node" + nodeInfo.id + ". Updating node" + nodeInfo.id + " finger" + fingerTableIndex + ".succ to node" + newNodeInfo.id);
        } catch (TException x) {
            handleException(x, from, "NodeComm.updateFingerTable() onto node" + nodeInfo.id + ". Updating node" + nodeInfo.id + " finger" + fingerTableIndex + ".succ to node" + newNodeInfo.id);
        }

        return statusData;
    }


    public static NodeDetails closestPrecedingFinger(String from, NodeDetails nodeInfo, int id) {
        NodeDetails closestNodeInfo = null;
        
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            closestNodeInfo = nodeConn.client.ClosestPrecedingFinger(id);

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            handleException(x, from, "NodeComm.closestPrecedingFinger() onto node" + nodeInfo.id + " on index " + id);
        } catch (TException x) {
            handleException(x, from, "NodeComm.closestPrecedingFinger() onto node" + nodeInfo.id + " on index " + id);
        }

        return closestNodeInfo;
    }


    public static NodeStructureData getNodeStructure(String from, NodeDetails nodeInfo) {
        NodeStructureData nodeStructureData = null;
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            nodeStructureData = nodeConn.client.GetNodeStructure();

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            handleException(x, from, "NodeComm.getNodeStructure() onto node" + nodeInfo.id);
        } catch (TException x) {
            handleException(x, from, "NodeComm.getNodeStructure() onto node" + nodeInfo.id);
        }
        return nodeStructureData;
    }


    public static void kill(String from, NodeDetails nodeInfo) {
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            nodeConn.client.Kill();

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            handleException(x, from, "NodeComm.kill() onto node" + nodeInfo.id);
        } catch (TException x) {
            handleException(x, from, "NodeComm.kill() onto node" + nodeInfo.id);
        }
    }


    /**
     * Exception handling helper.
     */
    private static void handleException(Exception x, String from, String msg) {
        System.out.println("ERROR: " + from + " - " + msg);
        x.printStackTrace();
        System.exit(1);
    }
}
