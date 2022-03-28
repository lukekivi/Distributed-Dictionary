package utils;

import org.apache.thrift.transport.TTransport;
import  pa2.Node;

public class NodeConn {
    public TTransport transport;
    public Node.Client client;
}
