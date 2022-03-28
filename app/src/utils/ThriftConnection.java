package utils;

import org.apache.thrift.transport.TTransport;
import  org.apache.thrift.TServiceClient;

public class ThriftConnection {
    public TTransport transport;
    public TServiceClient client;
}
