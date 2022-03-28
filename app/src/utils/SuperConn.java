package utils;

import org.apache.thrift.transport.TTransport;
import  pa2.SuperNode;

public class SuperConn {
    public TTransport transport;
    public SuperNode.Client client;
}