package utils;

import org.apache.thrift.transport.TTransport;
import  pa2.SuperNode;

/**
 * Class to encapsulate thrift connection objects.
 */
public class SuperConn {
    public TTransport transport;
    public SuperNode.Client client;
}