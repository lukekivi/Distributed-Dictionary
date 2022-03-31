package utils;

import org.apache.thrift.transport.TTransport;
import  pa2.Node;

/**
 * Class to encapsulate node thrift connection objects.
 */
public class NodeConn {
    public TTransport transport;
    public Node.Client client;
}
