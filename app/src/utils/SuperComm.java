package utils;

import pa2.NodeDetails;
import pa2.SuperNode;
import pa2.DHTData;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.TException;

/**
 * SuperComm is a class that conveniently handles SuperNode communication. It
 * essentially just abstracts away thrift boiler plate and truly allows 
 * users to treat RPC calls like simple function calls.
 * 
 * It creates thrift connections between system entites via the ConnFactory.
 * Then it calls RPC functions and handles errors. Each function gets a 
 * [from] field which helps build better error messages. It is supposed to be
 * of the form ClassName.functionName().
 */
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


    public static void killDHT(String from, ServerInfo superInfo) {
        try {
            SuperConn superConn = connFactory.makeSuperConn(superInfo);
            superConn.client.KillDHT();

            connFactory.closeSuperConn(superConn);
        } catch (TTransportException x) {
            handleException(x, from, "SuperComm.KillDHT()");
        } catch (TException x) {
            handleException(x, from, "SuperComm.KillDHT()");
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
