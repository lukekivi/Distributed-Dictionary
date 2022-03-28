package utils;

import pa2.SuperNode;
import pa2.Node;
import pa2.NodeDetails;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;



public class ConnFactory {
    /**
     * Return an active node connection. 
     * - Check if it is open programatically with: nodeConn.transport.isOpen()
     * - closeNodeConn() must be called on this connection or nodeConn.transport.close() 
     *   must be called at some point.
     * @param nodeInfo node to set up connection with
     * @return active node connection
     */
    public NodeConn makeNodeConn(NodeDetails nodeInfo) throws TTransportException {
        NodeConn nodeConn = new NodeConn();
        
        nodeConn.transport = new TSocket(nodeInfo.ip, nodeInfo.port);
        nodeConn.transport.open();

        TProtocol protocol = new  TBinaryProtocol(nodeConn.transport);
        nodeConn.client = new Node.Client(protocol);

        return nodeConn;
    }

    
    /**
     * Close an open NodeConn
     * @param nodeConn
     */
    public void closeNodeConn(NodeConn nodeConn) {
        if (nodeConn.transport.isOpen()) {
            nodeConn.transport.close();
        }
        nodeConn = null;
    }


    /**
     * Return an active supernode connection. 
     * - Check if it is open programatically with: superConn.transport.isOpen()
     * - closeSupeConn() must be called on this connection or superConn.transport.close() 
     *   must be called at some point.
     * @param superNodeInfo the supernode ip and port num
     * @return active supernode connection
     */
    public SuperConn makeSuperConn(ServerInfo superNodeInfo) throws TTransportException {
        SuperConn superConn = new SuperConn();;

        superConn.transport = new TSocket(superNodeInfo.ip, superNodeInfo.port);
        superConn.transport.open();

        TProtocol protocol = new  TBinaryProtocol(superConn.transport);
        superConn.client = new SuperNode.Client(protocol);

        return superConn;
    }

    
    /**
     * Close an open SuperNodeConn
     * @param superConn
     */
    public void closeSuperConn(SuperConn superConn) {
        if (superConn.transport.isOpen()) {
            superConn.transport.close();
        }
        superConn = null;
    }
}