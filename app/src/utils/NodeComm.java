package utils;

import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;

public class NodeComm {
    private static final ConnFactory connFactory = new ConnFactory();

    public static NodeDetails getSucc(NodeDetails nodeInfo) {
        NodeDetails succInfo = null;
        try {
            NodeConn nodeConn = connFactory.makeNodeConn(nodeInfo);
            succInfo = nodeConn.client.getSucc();

            connFactory.closeNodeConn(nodeConn);
        } catch (TTransportException x) {
            System.out.println("Error: Node " + manager.info.id + " connect to Node " + pred.id + " inside FindSuccessor() - con: " + x.getStackTrace());
            System.exit(1);
        } catch (TException e) {
            System.out.println("Error: Node " + manager.info.id + ": RPC GetSucc() call to Node " + pred.id + " inside FindSuccessor() - con: " + e.getStackTrace());
            System.exit(1);
        }
        return succInfo;
    }
}
