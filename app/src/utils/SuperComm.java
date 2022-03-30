package utils;

import pa2.NodeDetails;
import pa2.SuperNode;
import pa2.DHTData;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;


public class SuperComm {
    private static final ConnFactory connFactory = new ConnFactory();

    public static DHTData getDHTStructure(String from, ServerInfo superInfo) {
        DHTData dhtData = null;
        try {
            SuperConn superConn = connFactory.makeSuperConn(superInfo);
            dhtData = superConn.client.GetDHTStructure();

            connFactory.closeSuperConn(superConn);
        } catch (TTransportException x) {
            handleException(x, from, "SuperComm.getDHTStructure()");
        } catch (TException x) {
            handleException(x, from, "SuperComm.getDHTStructure()");
        }
        return dhtData;
    }


    private static void handleException(Exception x, String from, String msg) {
        System.out.println("ERROR: " + from + " - " + msg);
        x.printStackTrace();
        System.exit(1);
    }
}
